/*
o * File: DSNFile.java
 * Package: com.cerner.management.wtslocation
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.cerner.management.configuration.DefaultTnsNamesParser;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.wtslocation.configuration.WTSLocationRegistry;
import com.cerner.windows.library.registry.NoSuchKeyException;
import com.cerner.windows.library.registry.RegStringValue;
import com.cerner.windows.library.registry.Registry;
import com.cerner.windows.library.registry.RegistryException;
import com.cerner.windows.library.registry.RegistryKey;
import com.cerner.windows.library.registry.RegistryValue;

/**
 * A DSN file used by WTSLocation
 */
public class DSNFile
{
   private Map<String, String> entryMap = new HashMap<String, String>();

   private static final String ADDR = "addr";
   private static final String ADDRESS = "address";
   private static final String DATABASE = "database";
   private static final String DRIVER = "driver";
   private static final String SERVER = "server";

   private static final String UID = "uid";
   private static final String PWD = "pwd";

   private static final String DBQ = "dbq";

   private static final int SQL_SERVER_DEFAULT_PORT = 1433;
   private static final String SQL_SERVER_DEFAULT_VENDOR = "microsoft";
   private static final String SQL_SERVER_INTEGRATED_DEFAULT_VENDOR = "microsoft_integrated";
   private static final String SQL_SERVER_DEFAULT_VERSION = "2012";

   private static final String ORACLE_DEFAULT_VENDOR = "oracle";
   private static final String ORACLE_DEFAULT_VERSION = "12";

   private static final String DEFAULT_DATABASE_NAME = "wtslocation";
   private static final String DEFAULT_DATABASE_PURPOSE = "WTSLocation";

   private static final String LOCALHOST = "localhost";
   private static final String ORACLE_HOME = "ORACLE_HOME";

   /**
    * tnsnames.ora override system property
    */
   private final String overrideTNSNames = System.getProperty(OVERRIDE_TNSNAMES_PROPERTY, null);

   private static final String OVERRIDE_TNSNAMES_PROPERTY = "com.cerner.management.wtslocation.override.tnsnames";

   private static final String TNS_NAMES_FILE_NAME = "tnsnames.ora";

   /**
    * Constructor
    * 
    * @param path The path to the DSN file
    * @throws ManagementException When the dsn file provided cannot be read
    */
   public DSNFile(Path path) throws ManagementException
   {
      try (BufferedReader reader = Files.newBufferedReader(path))
      {
         // parse DSN file
         String line;
         while ((line = reader.readLine()) != null)
         {
            String[] dsnEntry = line.split("=");
            if (dsnEntry.length > 1)
            {
               entryMap.put(dsnEntry[0].toLowerCase(), dsnEntry[1]);
            }
         }
      }
      catch (IOException e)
      {
         throw new ManagementException("Error reading WTSLocation DSN file at " + path.toAbsolutePath().toString(), e);
      }
   }

   /**
    * Create a {@link DataStoreConfig} object based on the data in the DSN file
    * 
    * @return The {@link DataStoreConfig} created from the attributes in the file
    * @throws ManagementException
    */
   public DataStoreConfig generateDataStoreConfig() throws ManagementException
   {
      String integratedSecurityString = WTSLocationRegistry.getInstance().getValue("UseIntegratedSecurity");
      boolean useIntegratedSecurity = (integratedSecurityString != null
         && integratedSecurityString.equalsIgnoreCase("true") ? true : false);

      // Get user
      String user = null;
      String regKey = null;
      Exception caught = null;

      // first get the registry user to check for exceptions
      try
      {
         regKey = WTSLocationRegistry.getInstance().getValue(WTSLocationRegistry.DB_USER_KEY);
      }
      catch (RegistryException e)
      {
         caught = e;
      }

      // check registry first then the file map
      if (regKey != null && regKey.length() > 0)
      {
         user = regKey;
      }
      else if (entryMap.containsKey(UID))
      {
         user = entryMap.get(UID);
      }

      // if we got an exception earlier we make sure we pass it along with the error
      if (user == null && !useIntegratedSecurity)
      {
         if (caught != null)
         {
            throw new ManagementException(
               "Could not auto-discover datastore user. Neither '" + WTSLocationRegistry.DB_USER_KEY
                  + "' WTSLocation registry key nor the '" + UID + "' field in the DSN file found.",
               caught);
         }

         throw new ManagementException(
            "Could not auto-discover datastore user. Neither '" + WTSLocationRegistry.DB_USER_KEY
               + "' WTSLocation registry key nor the '" + UID + "' field in the DSN file found.");
      }

      // Get password
      String password = null;

      // reset registry values
      regKey = null;
      caught = null;

      // attempt registry first and catch any exceptions
      try
      {
         password = WTSLocationRegistry.getInstance().getPassword();
      }
      catch (RegistryException e)
      {
         caught = e;
      }

      // use registry value if applicable then try dsn map
      if (password == null && entryMap.containsKey(PWD))
      {
         password = entryMap.get(PWD);
      }

      // if there isn't a password make sure the earlier caught exception is passed if applicable
      if (password == null && !useIntegratedSecurity)
      {
         if (caught != null)
         {
            throw new ManagementException(
               "Could not auto-discover datastore password. Neither the WTSLocation registry key nor the '" + PWD
                  + "' field in the DSN file found.",
               caught);
         }

         throw new ManagementException(
            "Could not auto-discover datastore password. Neither the WTSLocation registry key nor the '" + PWD
               + "' field in the DSN file found.");
      }

      // Get driver
      String driver = null;
      if (entryMap.containsKey(DRIVER))
      {
         driver = entryMap.get(DRIVER);
      }

      if (driver == null)
      {
         throw new ManagementException(
            "Could not auto-discover datastore connection driver. Missing '" + DRIVER + "' field in DSN file.");
      }

      // If the driver is oracle we'll follow a different process, otherwise we only support SQL
      // Server
      if (driver.toUpperCase().contains("ORACLE"))
      {
         // get path to the tnsnames.ora file
         Path tnsNamesDirPath = null;
         String oracleHome = System.getenv(ORACLE_HOME);

         // if no environment variable present, try registry
         if (oracleHome == null)
         {
            try
            {
               // registry should always have HKLM\SOFTWARE\Oracle\Key_<client> keys with
               // ORACLE_HOME in them
               RegistryKey oracleKey;
               int accessPath = RegistryKey.ACCESS_ALL_32;
               try
               {
                  oracleKey = Registry.HKEY_LOCAL_MACHINE.openSubKey("Software\\Oracle", RegistryKey.ACCESS_ALL_32);
               }
               catch (NoSuchKeyException e)
               {
                  try
                  {
                     oracleKey = Registry.HKEY_LOCAL_MACHINE.openSubKey("Software\\Oracle", RegistryKey.ACCESS_ALL_64);
                     accessPath = RegistryKey.ACCESS_ALL_64;
                  }
                  catch (NoSuchKeyException e2)
                  {
                     throw new ManagementException(
                        "Could not find HKLM\\SOFTWARE\\ORACLE in 64-bit or 32-bit registry");
                  }
               }

               List<String> potentialHomes = new ArrayList<String>();
               for (String subKey : oracleKey.listSubKeyNames())
               {
                  if (subKey.toUpperCase().startsWith("KEY_"))
                  {
                     potentialHomes.add(subKey);
                  }
               }

               // if multiple homes were found we will default to the latest version
               String subKey;
               if (potentialHomes.size() > 1)
               {
                  Collections.sort(potentialHomes);

                  subKey = potentialHomes.get(potentialHomes.size() - 1);
               }
               else if (potentialHomes.size() == 1)
               {
                  subKey = potentialHomes.get(0);
               }
               else
               {
                  throw new ManagementException("Could not find ORACLE_HOME in registry.");
               }

               RegistryKey homeKey = oracleKey.openSubKey(subKey, accessPath);
               RegistryValue homeValue = homeKey.getValue(ORACLE_HOME);

               if (homeValue != null)
               {
                  oracleHome = ((RegStringValue) homeValue).getData();
               }
            }
            catch (RegistryException e)
            {
               throw new ManagementException("Error determining ORACLE_HOME from the registry during auto-discovery.",
                  e);
            }
         }

         // first try the override
         if (overrideTNSNames != null)
         {
            if (Files.exists(Paths.get(overrideTNSNames)))
            {
               tnsNamesDirPath = Paths.get(overrideTNSNames);
            }
            else
            {
               throw new ManagementException(
                  "The overridden tnsnames.ora directory '" + overrideTNSNames + "' could not be found.");
            }
         }
         // then based on ORACLE_HOME
         else
         {
            if (oracleHome == null)
            {
               throw new ManagementException(
                  "Error during auto-discovery of tnsnames.ora file. Could not determin ORACLE_HOME.");
            }

            // the file should be ORACLE_HOME/network/admin
            if (Files.exists(Paths.get(oracleHome, "network", "admin")))
            {
               tnsNamesDirPath = Paths.get(oracleHome, "network", "admin");
            }
            else
            {
               throw new ManagementException("Could not find default tnsnames.ora directory.");
            }
         }

         if (tnsNamesDirPath == null)
         {
            throw new ManagementException("Could not auto-discover the tnsnames.ora file directory.");
         }

         // now try to get the file itself
         if (Files.exists(Paths.get(tnsNamesDirPath.toAbsolutePath().toString(), TNS_NAMES_FILE_NAME)))
         {

            if (entryMap.containsKey(DBQ))
            {
               String listenerName = entryMap.get(DBQ);

               DataStoreConfig config = new DataStoreConfig(DataStoreConfig.TYPE_MILLENNIUM, DEFAULT_DATABASE_NAME,
                  user, password, TimeZone.getDefault(), ORACLE_DEFAULT_VENDOR, ORACLE_DEFAULT_VERSION, null);

               DefaultTnsNamesParser parser = new DefaultTnsNamesParser(config,
                  Paths.get(tnsNamesDirPath.toAbsolutePath().toString(), TNS_NAMES_FILE_NAME).toString(), listenerName);

               config = parser.parse();
               if (config != null)
               {
                  return config;
               }

               throw new ManagementException(
                  "The listener name " + listenerName + " could not be found in the tnsnames.ora file at "
                     + Paths.get(tnsNamesDirPath.toAbsolutePath().toString(), TNS_NAMES_FILE_NAME).toString());
            }

            throw new ManagementException("The TNS service name could not be found in the DSN file.");

         }

         throw new ManagementException(
            "Could not find tnsnames.ora file in directory '" + tnsNamesDirPath.toAbsolutePath().toString() + "'");

      }
      else if (driver.equalsIgnoreCase("SQL Server"))
      {

         // try the three applicable address fields in order of precedence "ADDRESS", "ADDR",
         // "SERVER"
         String addressKey = null;
         if (entryMap.containsKey(ADDRESS))
         {
            addressKey = ADDRESS;
         }
         else if (entryMap.containsKey(ADDR))
         {
            addressKey = ADDR;
         }
         else if (entryMap.containsKey(SERVER))
         {
            addressKey = SERVER;
         }

         if (addressKey == null)
         {
            throw new ManagementException("Could not auto-discover hostname or port. Missing '" + ADDRESS + "', '"
               + ADDR + "', or '" + SERVER + "' field in DSN file.");
         }

         String address = entryMap.get(addressKey);
         String host = null;
         int port = -1;

         /*
          * First check for special local host considerations when the address is one of the
          * following: ";", " ;", ".;", "local", "localhost", "(local)", "(localhost)", or
          * "(localdb)"
          */
         if (address.equals(";") || address.equals(" ;") || address.equals(".;") || address.startsWith("local")
            || address.startsWith("(local"))
         {
            host = LOCALHOST;
            port = SQL_SERVER_DEFAULT_PORT;
         }
         else
         {
            // the address is commonly like this "<host>\<host>,<port>" or just "<host>,<port>"
            if (address.contains("\\"))
            {

               String[] addresses = address.split("\\\\");
               if (address.length() > 1)
               {
                  address = addresses[1];
               }
               else
               {
                  throw new ManagementException("Error parsing auto-discovered SQL Server host '" + address + "'");
               }
            }

            if (address.contains(","))
            {
               String[] hostPort = address.split(",");
               if (hostPort.length > 1)
               {
                  host = hostPort[0];

                  try
                  {
                     port = Integer.parseInt(hostPort[1]);
                  }
                  catch (NumberFormatException e)
                  {
                     throw new ManagementException("Error determining SQL Server connection port from DSN file.", e);
                  }
               }
            }
            else
            {
               host = address;
               port = SQL_SERVER_DEFAULT_PORT;
            }
         }

         if (host == null)
         {
            throw new ManagementException("Could not determine SQL Server host from DSN file.");
         }
         else if (port == -1)
         {
            throw new ManagementException("Could not determine SQL Server port from DSN file.");
         }

         // get the instance
         String instance = null;
         if (entryMap.containsKey(DATABASE))
         {
            instance = entryMap.get(DATABASE);
         }

         if (instance == null)
         {
            throw new ManagementException("Could not auto-discover instance name from DSN file.");
         }

         if (!useIntegratedSecurity)
         {
            return new DataStoreConfig(DEFAULT_DATABASE_NAME + '_' + instance + '_' + host + '_' + port, user, password,
               TimeZone.getDefault(), instance, host, port, SQL_SERVER_DEFAULT_VENDOR, SQL_SERVER_DEFAULT_VERSION,
               DEFAULT_DATABASE_PURPOSE);
         }

         return new DataStoreConfig(DEFAULT_DATABASE_NAME + '_' + instance + '_' + host + '_' + port, user, password,
            TimeZone.getDefault(), instance, host, port, SQL_SERVER_INTEGRATED_DEFAULT_VENDOR,
            SQL_SERVER_DEFAULT_VERSION, DEFAULT_DATABASE_PURPOSE);
      }

      throw new ManagementException("Unsupported driver type '" + driver + "' found in DSN file.");
   }
}
