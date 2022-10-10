
package com.cerner.management.wtslocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cerner.management.data.dao.DAOException;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.file.RMIFileTransfer;
import com.cerner.management.file.ResultFileTransferListener;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.util.CSVReader;
import com.cerner.management.wtslocation.configuration.AttributeManager;
import com.cerner.management.wtslocation.dao.WTSLocationManagementDAO;

/**
 * File transfer listener for importing client devices
 */
public class ClientDeviceFileTransferListener implements ResultFileTransferListener
{
   private static final Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation");

   private static final long serialVersionUID = -5448094665396654422L;

   private static final String CLIENT_MNEMONIC = "CLIENTMNEMONIC";
   private static final String MILL_ENVIRONMENT = "MILLENVIRONMENT";
   private static final String CLIENT_NAME = "CLIENTNAME";
   private static final String DEVICE_STATUS = "DEVICESTATUS";
   private static final String DEFAULT_LOCATION = "DEFAULT_LOCATION";
   private static final String DEVICE_LOCATION = "DEVICE_LOCATION";
   private static final String LOGICAL_DOMAIN_ID = "LOGICAL_DOMAIN_ID";

   private static final String UPDATED = "updated";
   private static final String CREATED = "created";
   private static final String IGNORED = "ignored";

   private static final String DUPLICATES = "duplicates";
   private static final String UPDATEDDEVICES = "updatedDevices";
   private static final String CREATEDDEVICES = "createdDevices";
   private static final String IGNOREDDEVICES = "ignoredDevices";

   private static final String UPDATEDSTATUS = "Updated";
   private static final String CREATEDSTATUS = "Created";
   private static final String IGNOREDSTATUS = "Ignored";

   private List<String> updatedList = new ArrayList<String>();
   private List<String> createdList = new ArrayList<String>();
   private List<String> ignoredList = new ArrayList<String>();
   private Set<String> duplicatesSet = new HashSet<String>();
   private List<String> updatedDevicesList = new ArrayList<>();
   private List<String> createdDevicesList = new ArrayList<>();
   private List<String> ignoredDevicesList = new ArrayList<>();

   private WTSLocationManagementDAO dao;

   private String clientMnemonic;
   private String millEnvironment;

   /**
    * @param dao
    * @param clientMnemonic
    * @param millEnvironment
    * @throws ManagementException
    */
   public ClientDeviceFileTransferListener(WTSLocationManagementDAO dao, String clientMnemonic, String millEnvironment)
      throws ManagementException
   {
      this.clientMnemonic = clientMnemonic;
      this.millEnvironment = millEnvironment;
      this.dao = dao;
   }

   public void notify(File file, RMIFileTransfer fileImpl)
   {
      FileReader fr = null;
      BufferedReader br = null;

      try
      {
         fr = new FileReader(file);
         br = new BufferedReader(fr);
      }
      catch (FileNotFoundException e)
      {
         logger.log(Level.WARNING, "Unable to find file: " + file.getName(), e);
         return;
      }

      CSVReader reader = new CSVReader(br);

      List<ClientDevice> deviceList = new ArrayList<ClientDevice>();
      Set<ClientDevice> deviceSet = new HashSet<ClientDevice>();

      try
      {
         // read headers
         String[] headers = reader.readNext();

         List<String> allAttributes = Arrays.asList(AttributeManager.getAttributeNames());

         // make default attributes blank so we can ensure all devices have all of the
         // attributes represented in the headers
         Map<String, ClientDeviceAttribute> defaultAttributes = new HashMap<String, ClientDeviceAttribute>();
         for (String header : headers)
         {
            if (allAttributes.contains(header.toUpperCase()))
            {
               defaultAttributes.put(header, new ClientDeviceAttribute(header, ""));
            }
         }

         String[] csvLineArray = null;
         // Read File Line By Line and create devices
         while ((csvLineArray = reader.readNext()) != null)
         {
            if (!(csvLineArray.length <= headers.length))
            {
               logger.log(Level.WARNING,
                  "File " + file.getName() + " has an incorrectly formatted line that will not be imported.");
               continue;
            }

            String deviceName = null;
            String deviceLocation = null;
            String defaultLocation = null;
            String mnemonic = null;
            String env = null;
            Long logicalDomainId = null;

            Map<String, ClientDeviceAttribute> deviceAttributes = new HashMap<>(
               defaultAttributes);
            for (int i = 0; i < csvLineArray.length; i++)
            {
               String curToken = csvLineArray[i];
               if (curToken == null)
               {
                  curToken = "";
               }

               String curHeader = headers[i];

               if (CLIENT_MNEMONIC.equalsIgnoreCase(curHeader))
               {
                  mnemonic = curToken;
               }
               else if (MILL_ENVIRONMENT.equalsIgnoreCase(curHeader))
               {
                  env = curToken;
               }
               else if (CLIENT_NAME.equalsIgnoreCase(curHeader))
               {
                  deviceName = curToken.trim();
               }
               else if (DEFAULT_LOCATION.equalsIgnoreCase(curHeader))
               {
                  defaultLocation = curToken.trim();
                  deviceAttributes.put(curHeader, new ClientDeviceAttribute(curHeader, curToken));
               }
               else if (DEVICE_LOCATION.equalsIgnoreCase(curHeader))
               {
                  deviceLocation = curToken.trim();
                  deviceAttributes.put(curHeader, new ClientDeviceAttribute(curHeader, curToken));
               }
               else if (LOGICAL_DOMAIN_ID.equalsIgnoreCase(curHeader))
               {
                  try
                  {
                     logicalDomainId = Long.parseLong(curToken.trim());
                  }
                  catch (NumberFormatException e)
                  {
                     // Keep logicalDomainId set to -1
                     curToken = "-1";
                  }

                  deviceAttributes.put(curHeader, new ClientDeviceAttribute(curHeader, curToken));
               }
               else if (allAttributes.contains(curHeader.toUpperCase()))
               {
                  deviceAttributes.put(curHeader, new ClientDeviceAttribute(curHeader, curToken));
               }

            }

            // if the fields are blank, set the defaults
            if (mnemonic == null || mnemonic.trim().length() == 0)
            {
               mnemonic = clientMnemonic;
            }
            if (env == null || env.trim().length() == 0)
            {
               env = millEnvironment;
            }

            String logicalDomainName = "";
            if (logicalDomainId != null)
            {
               LogicalDomain ld = LogicalDomainCache.getInstance(env).getLogicalDomain(logicalDomainId);
               logicalDomainName = (ld != null) ? ld.getLogicalDomain() : "";
            }

            if (mnemonic != null && mnemonic.toLowerCase().startsWith(clientMnemonic.toLowerCase()) && env != null
               && env.equalsIgnoreCase(millEnvironment) && deviceName != null)
            {

               ClientDevice device = new ClientDevice(mnemonic, env, deviceName, deviceName,
                  deviceAttributes.values().toArray(new ClientDeviceAttribute[deviceAttributes.size()]),
                  logicalDomainName, defaultLocation, deviceLocation);

               boolean added = deviceSet.add(device);

               if (added)
               {
                  deviceList.add(device);
               }
               else
               {
                  duplicatesSet.add(createKey(device.getClientMnemonic(), device.getMillenniumEnvironment(),
                     device.getLogicalDomain(), device.getDeviceName()));
               }
            }
            else
            {
               ignoredList.add(createKey(mnemonic, env, logicalDomainName, deviceName));
               ignoredDevicesList.add(createKeyForDevices(mnemonic, env, logicalDomainName, deviceName,
                  deviceAttributes.values().toArray(new ClientDeviceAttribute[deviceAttributes.size()]),
                  IGNOREDSTATUS));
            }
         }
      }
      catch (IOException e)
      {
         logger.log(Level.WARNING, "Unable to import client devices.", e);
         return;
      }
      catch (ManagementException e)
      {
         logger.log(Level.WARNING, "Unable to import client devices.", e);
         return;
      }

      try
      {
         ClientDevice[] existingDevices = dao.getClientDevices(clientMnemonic, millEnvironment);
         List<ClientDevice> batchList = new ArrayList<ClientDevice>();
         for (ClientDevice existingDevice : existingDevices)
         {
            int idx = deviceList.indexOf(existingDevice);
            if (idx >= 0)
            {
               boolean same = compareDevices(existingDevice, deviceList.get(idx));
               ClientDevice device = deviceList.get(idx);
               deviceList.remove(idx);
               if (!same)
               {
                  batchList.add(device);

                  updatedList.add(createKey(device.getClientMnemonic(), device.getMillenniumEnvironment(),
                     device.getLogicalDomain(), device.getDeviceName()));
                  updatedDevicesList.add(createKeyForDevices(device.getClientMnemonic(),
                     device.getMillenniumEnvironment(), device.getLogicalDomain(), device.getDeviceName(),
                     device.getDeviceAttributes(), UPDATEDSTATUS));

               }
               else
               {
                  ignoredList.add(createKey(device.getClientMnemonic(), device.getMillenniumEnvironment(),
                     device.getLogicalDomain(), device.getDeviceName()));
                  ignoredDevicesList.add(createKeyForDevices(device.getClientMnemonic(),
                     device.getMillenniumEnvironment(), device.getLogicalDomain(), device.getDeviceName(),
                     device.getDeviceAttributes(), IGNOREDSTATUS));

               }
            }
         }

         // only update if updates were found
         if (batchList.size() > 0)
         {
            try
            {
               dao.batchUpdateClientDevices(batchList.toArray(new ClientDevice[batchList.size()]), clientMnemonic);
            }
            catch (ManagementException e)
            {
               logger.log(Level.WARNING, "Unable to update client devices into primary datastore during import.", e);
            }
         }

         // add new devices
         for (ClientDevice device : deviceList)
         {

            createdList.add(createKey(device.getClientMnemonic(), device.getMillenniumEnvironment(),
               device.getLogicalDomain(), device.getDeviceName()));
            createdDevicesList.add(createKeyForDevices(device.getClientMnemonic(), device.getMillenniumEnvironment(),
               device.getLogicalDomain(), device.getDeviceName(), device.getDeviceAttributes(), CREATEDSTATUS));
         }

         // add new devices
         if (createdList.size() > 0)
         {
            try
            {
               dao.batchAddClientDevices(deviceList.toArray(new ClientDevice[deviceList.size()]), clientMnemonic,
                  millEnvironment);
            }
            catch (ManagementException e)
            {
               logger.log(Level.WARNING, "Unable to add client devices during import.", e);
            }
         }

      }
      catch (DAOException e)
      // the rest are ignored
      {
         logger.log(Level.WARNING, "Unable to retrieve existing client devices for import comparison.", e);
      }

   }

   private boolean compareDevices(ClientDevice existing, ClientDevice update)
   {
      Map<String, String> map = new HashMap<String, String>();

      for (ClientDeviceAttribute attr : existing.getDeviceAttributes())
      {
         map.put(attr.getName().toLowerCase(), attr.getValue());
      }

      boolean compare = true;
      for (ClientDeviceAttribute attr : update.getDeviceAttributes())
      {
         compare = compare && map.containsKey(attr.getName().toLowerCase())
            && (map.get(attr.getName().toLowerCase()) == null ? attr.getValue() == null
               : map.get(attr.getName().toLowerCase()).equalsIgnoreCase(attr.getValue()));

         if (!compare)
         {
            return false;
         }
      }

      compare = compare && ((existing.getDefaultLocation() != null && update.getDefaultLocation() != null
         && existing.getDefaultLocation().equalsIgnoreCase(update.getDefaultLocation()))
         || (existing.getDefaultLocation() == null && update.getDefaultLocation() == null));

      compare = compare && ((existing.getDeviceLocation() != null && update.getDeviceLocation() != null
         && existing.getDeviceLocation().equalsIgnoreCase(update.getDeviceLocation()))
         || (existing.getDeviceLocation() == null && update.getDeviceLocation() == null));

      return compare;
   }

   private String createKeyForDevices(String mnemonic, String env, String logicalDomain, String device,
      ClientDeviceAttribute[] clientDeviceAttributes, String deviceStatus)
   {
      StringBuffer buffer = new StringBuffer();
      if (mnemonic != null)
      {
         if (logicalDomain != null)
         {
            mnemonic = mnemonic.substring(0, mnemonic.lastIndexOf("_"));
         }
         buffer.append(CLIENT_MNEMONIC);
         buffer.append("::");
         buffer.append(mnemonic.toUpperCase());
      }

      buffer.append("::");

      if (env != null)
      {
         buffer.append(MILL_ENVIRONMENT);
         buffer.append("::");
         buffer.append(env.toUpperCase());
      }

      buffer.append("::");

      if (logicalDomain != null)
      {
         buffer.append(LOGICAL_DOMAIN_ID);
         buffer.append("::");
         buffer.append(logicalDomain.toUpperCase());
      }

      buffer.append("::");

      if (device != null)
      {
         buffer.append(CLIENT_NAME);
         buffer.append("::");
         buffer.append(device.toLowerCase());
      }
      buffer.append("::");
      buffer.append(DEVICE_STATUS);
      buffer.append("::");
      buffer.append(deviceStatus);
      buffer.append("::");

      for (int i = 0; i < clientDeviceAttributes.length; i++)
      {
         buffer.append(clientDeviceAttributes[i].getName());
         buffer.append("::");
         buffer.append(clientDeviceAttributes[i].getValue());
         if (i != clientDeviceAttributes.length - 1)
         {
            buffer.append("::");
         }
      }

      return buffer.toString();
   }

   private String createKey(String mnemonic, String env, String logicalDomain, String device)
   {
      StringBuffer buffer = new StringBuffer();

      if (mnemonic != null)
      {
         if (logicalDomain != null)
         {
            mnemonic = mnemonic.substring(0, mnemonic.lastIndexOf("_"));
         }

         buffer.append(mnemonic.toUpperCase());
      }

      buffer.append("::");

      if (env != null)
      {
         buffer.append(env.toUpperCase());
      }

      buffer.append("::");

      if (logicalDomain != null)
      {
         buffer.append(logicalDomain.toUpperCase());
      }

      buffer.append("::");

      if (device != null)
      {
         buffer.append(device.toLowerCase());
      }

      return buffer.toString();
   }

   public Map<String, Object> result()
   {
      Map<String, Object> result = new HashMap<String, Object>();

      result.put(UPDATED, updatedList.toArray(new String[updatedList.size()]));
      result.put(CREATED, createdList.toArray(new String[createdList.size()]));
      result.put(IGNORED, ignoredList.toArray(new String[ignoredList.size()]));
      result.put(DUPLICATES, duplicatesSet.toArray(new String[duplicatesSet.size()]));
      result.put(CREATEDDEVICES, createdDevicesList.toArray(new String[createdDevicesList.size()]));
      result.put(UPDATEDDEVICES, updatedDevicesList.toArray(new String[updatedDevicesList.size()]));
      result.put(IGNOREDDEVICES, ignoredDevicesList.toArray(new String[ignoredDevicesList.size()]));

      return result;
   }
}
