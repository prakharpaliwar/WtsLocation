/*
 * File: WTSMbeanImpl.java 
 * Package: com.cerner.management.mbean.wtslocation.impl 
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

import javax.management.ObjectName;

import com.cerner.management.MBeanRegistry;
import com.cerner.management.ManagementMBeanService;
import com.cerner.management.RMIContainer;
import com.cerner.management.configuration.ProfileEntry;
import com.cerner.management.configuration.ProfileManager;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.file.FileTransferListener;
import com.cerner.management.file.RMIFileTransfer;
import com.cerner.management.file.impl.RMIFileTransferImpl;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.BoundedClientDevice;
import com.cerner.management.wtslocation.ClientDevice;
import com.cerner.management.wtslocation.ClientDeviceFileTransferListener;
import com.cerner.management.wtslocation.ClientDeviceFilter;
import com.cerner.management.wtslocation.Column;
import com.cerner.management.wtslocation.DSNFile;
import com.cerner.management.wtslocation.LogicalDomain;
import com.cerner.management.wtslocation.LogicalDomainCache;
import com.cerner.management.wtslocation.WTSLocationMbean;
import com.cerner.management.wtslocation.configuration.Attribute;
import com.cerner.management.wtslocation.configuration.AttributeManager;
import com.cerner.management.wtslocation.configuration.ConfigurationSettings;
import com.cerner.management.wtslocation.configuration.ConfigurationSettingsManager;
import com.cerner.management.wtslocation.configuration.WTSLocationRegistry;
import com.cerner.management.wtslocation.dao.WTSLocationManagementDAO;
import com.cerner.management.wtslocation.dao.impl.WTSLocationManagementDAOImpl;

/**
 * WTSLocation MBean Implementation.
 */
public class WTSLocationMbeanImpl extends ManagementMBeanService implements WTSLocationMbean
{
   /**
    * the logger for the class
    */
   static final Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.impl");

   private static Map<String, WTSLocationMbean> instances = new HashMap<>();

   /**
    * WTSLocation MBean instance name
    */
   private ObjectName objectName;

   private String domain;
   private String clientMnemonic;

   private static final String SERVICES = "Services";
   private static final String WTSLOCATION = "WTSLocationManagementService";
   private static final String PRIMARY_DATASTORE = "PrimaryDatastore";
   private static final String SECONDARY_DATASTORE = "SecondaryDatastore";

   /**
    * WTSLocation Management DAO
    */
   private WTSLocationManagementDAO dao = null;

   private static final String HEADER_CLIENT_MNEMONIC = "CLIENTMNEMONIC";
   private static final String HEADER_MILL_ENVIRONMENT = "MILLENVIRONMENT";
   private static final String HEADER_DEVICE_NAME = "CLIENTNAME";
   private static final String HEADER_UPDT_DT_TM = "UPDT_DT_TM";
   private static final String HEADER_LAST_UPDATED_BY = "LAST_UPDATED_BY";

   /**
    * Override properties for domains and datastores
    */
   private final String displayDomain = System.getProperty(DISPLAY_DOMAIN_PROPERTY, null);
   private final String overridePrimary = System.getProperty(OVERRIDE_PRIMARY_PROPERTY, null);
   private final String overrideSecondary = System.getProperty(OVERRIDE_SECONDARY_PROPERTY, null);

   private static final String DISPLAY_DOMAIN_PROPERTY = "com.cerner.management.wtslocation.display.domain";
   private static final String OVERRIDE_PRIMARY_PROPERTY = "com.cerner.management.wtslocation.override.primary";
   private static final String OVERRIDE_SECONDARY_PROPERTY = "com.cerner.management.wtslocation.override.secondary";

   /**
    * Override properties for auto-discovery
    */
   private final boolean overrideAutoDiscovery = Boolean.getBoolean(OVERRIDE_AUTO_DISCOVERY_PROPERTY);
   private final String overrideDSNLocation = System.getProperty(OVERRIDE_DSN_LOCATION_PROPERTY, null);

   private static final String OVERRIDE_AUTO_DISCOVERY_PROPERTY = "com.cerner.management.wtslocation.override.auto.discovery";
   private static final String OVERRIDE_DSN_LOCATION_PROPERTY = "com.cerner.management.wtslocation.override.dsn.location";

   private static final String CERNER_DIRECTORY = "C:\\Program Files\\Cerner";
   private static final String WTSLOCATION_DIRECTORY = "C:\\Program Files (x86)\\WTSLocation";

   private static final String WTSLOCATION_DSN_FILE_NAME = "WTSLocation.dsn";
   private static final String WTSLOCATION_BACKUP_DSN_FILE_NAME = "WTSLocation_Backup.dsn";

   /**
    * Override export properties
    */
   private final int exportOffset = Integer.getInteger(OVERRIDE_EXPORT_OFFSET, 2000);

   private static final String OVERRIDE_EXPORT_OFFSET = "com.cerner.management.wtslocation.override.export.offset";

   /**
    * Returns the singleton instance (per domain).
    * 
    * @return Instance of WTSLocationMBean
    * @throws ManagementException
    */
   public static synchronized WTSLocationMbean getInstance() throws ManagementException
   {
      String domain = WTSLocationRegistry.getInstance().getLogicalValue("MILL_ENVIRONMENT");
      if (domain == null)
      {
         throw new ManagementException(
            "Could not auto-discover the WTS Location domain from the MILL_ENVIRONMENT logical.");
      }
      if (!instances.containsKey(domain.toLowerCase()))
      {
         // create the new instance of the WTSLocationMbean
         instances.put(domain.toLowerCase(), new WTSLocationMbeanImpl(domain));
      }
      return instances.get(domain.toLowerCase());
   }

   /*
    * Private constructor.
    */
   private WTSLocationMbeanImpl(String domain) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "WTSLocationMbeanImpl");

      this.clientMnemonic = WTSLocationRegistry.getInstance().getLogicalValue("CLIENT_MNEMONIC");
      if (this.clientMnemonic == null || this.clientMnemonic.trim().length() == 0)
      {
         throw new ManagementException(
            "Could not read the CLIENT_MNEMONIC logical from the registry or the value was empty.");
      }

      this.domain = domain;
      if (this.domain == null)
      {
         this.domain = WTSLocationRegistry.getInstance().getLogicalValue("MILL_ENVIRONMENT");
         if (this.domain == null || this.domain.trim().length() == 0)
         {
            throw new ManagementException(
               "Could not read the MILL_ENVIRONMENT from the registry or the value was empty.");
         }

         logger.log(Level.FINE, "Using the MILL_ENVIRONMENT logical " + domain + ".");
      }
      else
      {
         logger.log(Level.FINE, "Using the auto-start domain " + domain + ".");
      }

      String localDomain = domain;
      if (displayDomain != null)
      {
         localDomain = displayDomain;
      }

      Map<String, String> props = new HashMap<>();
      props.put(PROPERTY_MBEAN_TYPE, WTSLocationMbean.WTS_MBEAN_TYPE);
      props.put(PROPERTY_VERSION, "8");
      props.put(PROPERTY_TARGET_NAME, localDomain.toLowerCase());
      props.put(PROPERTY_SERVICE_CUSTOM_1, clientMnemonic);
      loadConfiguration();
      this.objectName = MBeanRegistry.registerService(WTSLocationMbean.WTS_MBEAN_TYPE, TYPE_NODE_TARGET, this,
         WTSLocationMbean.class, props);

      logger.exiting(this.getClass().getName(), "WTSLocationMbeanImpl");
   }

   private void loadConfiguration() throws ManagementException
   {
      // if we override auto discovery then we use the old method
      if (overrideAutoDiscovery)
      {
         String primaryDatastore = null;
         String secondaryDatastore = null;

         ProfileEntry activeProfile = ProfileManager.getActiveProfile();
         if (activeProfile.hasChildEntry(SERVICES))
         {
            ProfileEntry services = activeProfile.getChildEntry(SERVICES);

            if (services.hasChildEntry(WTSLOCATION))
            {
               ProfileEntry wtsProfileEntry = services.getChildEntry(WTSLOCATION);
               if (!wtsProfileEntry.hasValue(PRIMARY_DATASTORE))
               {
                  throw new ManagementException(
                     "Must specify a primary datastore for the WTSLocationManagementService.");
               }

               primaryDatastore = wtsProfileEntry.getString(PRIMARY_DATASTORE);

               try
               {
                  secondaryDatastore = wtsProfileEntry.getString(SECONDARY_DATASTORE);
               }
               catch (ManagementException e)
               {
                  logger.log(Level.FINE, "No secondary datastore configured for WTSLocationManagementService.");
               }
            }
         }

         if (overridePrimary != null)
         {
            primaryDatastore = overridePrimary;
         }
         if (overrideSecondary != null)
         {
            secondaryDatastore = overrideSecondary;
         }

         if (primaryDatastore != null)
         {
            this.dao = new WTSLocationManagementDAOImpl(primaryDatastore, secondaryDatastore);
         }
      }
      // otherwise we look at the DSN file
      else
      {
         DSNFile primaryDSN = null;
         DSNFile secondaryDSN = null;

         // if the override is specified
         if (overrideDSNLocation != null)
         {
            // check for the primary first
            if (Files.exists(Paths.get(overrideDSNLocation))
               && Files.exists(Paths.get(overrideDSNLocation, WTSLOCATION_DSN_FILE_NAME)))
            {
               primaryDSN = new DSNFile(Paths.get(overrideDSNLocation, WTSLOCATION_DSN_FILE_NAME));

               logger.log(Level.INFO, "Using primary datastore found in DSN file at '"
                  + Paths.get(overrideDSNLocation, WTSLOCATION_DSN_FILE_NAME).toString() + "'.");

               // try the secondary but it's not necessary
               if (Files.exists(Paths.get(overrideDSNLocation, WTSLOCATION_BACKUP_DSN_FILE_NAME)))
               {
                  secondaryDSN = new DSNFile(Paths.get(overrideDSNLocation, WTSLOCATION_BACKUP_DSN_FILE_NAME));

                  logger.log(Level.INFO, "Using secondary datastore found in DSN file at '"
                     + Paths.get(overrideDSNLocation, WTSLOCATION_BACKUP_DSN_FILE_NAME).toString() + "'.");
               }
               else
               {
                  logger.log(Level.INFO, "No secondary datastore DSN file found at '"
                     + Paths.get(overrideDSNLocation, WTSLOCATION_BACKUP_DSN_FILE_NAME).toString() + "'.");
               }

            }
            // specific override error
            else
            {
               throw new ManagementException(
                  "Could not find DSN file at override location '" + overrideDSNLocation + "'.");
            }
         }
         // next check the Cerner directory (64 bit WTS)
         else if (Files.exists(Paths.get(CERNER_DIRECTORY))
            && Files.exists(Paths.get(CERNER_DIRECTORY, WTSLOCATION_DSN_FILE_NAME)))
         {
            primaryDSN = new DSNFile(Paths.get(CERNER_DIRECTORY, WTSLOCATION_DSN_FILE_NAME));

            logger.log(Level.INFO, "Using primary datastore found in DSN file at '"
               + Paths.get(CERNER_DIRECTORY, WTSLOCATION_DSN_FILE_NAME).toString() + "'.");

            // try the secondary but it's not necessary
            if (Files.exists(Paths.get(CERNER_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME)))
            {
               secondaryDSN = new DSNFile(Paths.get(CERNER_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME));

               logger.log(Level.INFO, "Using secondary datastore found in DSN file at '"
                  + Paths.get(CERNER_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME).toString() + "'.");
            }
            else
            {
               logger.log(Level.INFO, "No secondary datastore DSN file found at '"
                  + Paths.get(CERNER_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME).toString() + "'.");
            }
         }
         // last check the WTSLocation directory (32 bit WTS)
         else if (Files.exists(Paths.get(WTSLOCATION_DIRECTORY))
            && Files.exists(Paths.get(WTSLOCATION_DIRECTORY, WTSLOCATION_DSN_FILE_NAME)))
         {
            primaryDSN = new DSNFile(Paths.get(WTSLOCATION_DIRECTORY, WTSLOCATION_DSN_FILE_NAME));

            logger.log(Level.INFO, "Using primary datastore found in DSN file at '"
               + Paths.get(WTSLOCATION_DIRECTORY, WTSLOCATION_DSN_FILE_NAME).toString() + "'.");

            // try the secondary but it's not necessary
            if (Files.exists(Paths.get(WTSLOCATION_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME)))
            {
               secondaryDSN = new DSNFile(Paths.get(WTSLOCATION_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME));

               logger.log(Level.INFO, "Using secondary datastore found in DSN file at '"
                  + Paths.get(WTSLOCATION_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME).toString() + "'.");
            }
            else
            {
               logger.log(Level.INFO, "No secondary datastore DSN file fournd at '"
                  + Paths.get(WTSLOCATION_DIRECTORY, WTSLOCATION_BACKUP_DSN_FILE_NAME).toString() + "'.");
            }
         }
         // no DSN file found
         else
         {
            throw new ManagementException("Could not find a DSN file for auto-discovery.");
         }

         this.dao = new WTSLocationManagementDAOImpl(primaryDSN, secondaryDSN);
      }

      if (this.dao == null)
      {
         throw new ManagementException("The WTSLocation service could not retrieve datastore connection information.");
      }
   }

   public synchronized void disposeInternal()
   {
      logger.entering(this.getClass().getName(), "dispose");
      if (objectName != null)
      {
         MBeanRegistry.unregister(objectName);

         objectName = null;
      }

      domain = null;
      clientMnemonic = null;

      logger.exiting(this.getClass().getName(), "dispose");
   }

   public Column[] listColumns() throws ManagementException
   {
      logger.entering(this.getClass().getName(), "getColumns");

      // get columns
      String[] columns = dao.getClientDeviceTableColumns();
      List<Column> columnsList = new ArrayList<>();
      for(String column: columns) {
         columnsList.add(new Column(column));
      }
      
      logger.exiting(this.getClass().getName(), "getColumns");
      return columnsList.toArray(new Column[columnsList.size()]);
   }
   
   public ClientDevice retrieveClientDevice(String deviceName) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "retrieveClientDevice");

      // get client device
      ClientDevice device = dao.getClientDevice(deviceName, clientMnemonic, domain);

      logger.exiting(this.getClass().getName(), "retrieveClientDevice");
      return device;
   }

   public ClientDevice retrieveClientDeviceWithLogicalDomain(String deviceName, String logicalDomain)
      throws ManagementException
   {
      logger.entering(this.getClass().getName(), "retrieveClientDeviceWithLogicalDomain");

      try
      {
         if (logicalDomain == null)
         {
            return dao.getClientDevice(deviceName, clientMnemonic, domain);
         }

         LogicalDomain ld = LogicalDomainCache.getInstance(domain).getLogicalDomain(logicalDomain);

         if (ld != null)
         {
            // get client device
            return dao.getClientDevice(deviceName, clientMnemonic, ld.getId(), domain);
         }

         return null;
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "retrieveClientDeviceWithLogicalDomain");
      }
   }

   public BoundedClientDevice listBoundedClientDevices(ClientDeviceFilter filter) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "listBoundedClientDevices");

      // get the bounded devices
      BoundedClientDevice bounded = dao.getBoundedClientDevices(filter, clientMnemonic, domain);

      logger.exiting(this.getClass().getName(), "listBoundedClientDevices");
      return bounded;
   }

   public void addClientDevice(ClientDevice device) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "addClientDevice");

      dao.addClientDevice(device, clientMnemonic, domain);

      logger.exiting(this.getClass().getName(), "addClientDevice");
   }

   public void updateClientDevice(ClientDevice device) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "updateClientDevice");

      dao.updateClientDevice(device, clientMnemonic);

      logger.exiting(this.getClass().getName(), "updateClientDevice");
   }

   public void deleteClientDevice(ClientDevice device) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "deleteClientDevice");

      dao.deleteClientDevice(device);

      logger.exiting(this.getClass().getName(), "deleteClientDevice");
   }

   public String exportClientDevices() throws ManagementException
   {
      logger.entering(this.getClass().getName(), "exportClientDevices");

      String stub = null;
      RMIFileTransfer transfer = null;
      try
      {
         File exportFile = File.createTempFile("WTSLocationExport", ".csv");
         exportData(exportFile);
         transfer = new RMIFileTransferImpl(RMIFileTransfer.MODE_SERVER_SEND, exportFile.getCanonicalPath());
      }
      catch (IOException e)
      {
         throw new ManagementException(logger, "Unable to export client devices.", e);
      }

      try
      {
         transfer.addListener(new FileTransferListener()
         {
            private static final long serialVersionUID = 1080836262796816440L;

            public void notify(File file, RMIFileTransfer fileImpl)
            {
               file.delete();
            }
         });
      }
      catch (RemoteException e)
      {
         logger.logp(Level.WARNING, this.getClass().getName(), "exportClientDevices",
            "Unable to attach a listener to file transfer object.", e);
      }

      RMIContainer stuff = exportRMIFileTransfer(transfer);
      stub = stuff.getRmiStub();

      // Serialize the stub.

      logger.exiting(this.getClass().getName(), "exportClientDevices");
      // Return the stub in serialized format.
      return stub;
   }

   void exportData(File file) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "exportData");

      List<String> attributes = new ArrayList<>(Arrays.asList(dao.getClientDeviceTableColumns()));
      Collections.sort(attributes);
      // we know that these three columns must be in the table, therefore we remove them from the
      // list and
      // populate them manually
      attributes.remove(HEADER_CLIENT_MNEMONIC);
      attributes.remove(HEADER_MILL_ENVIRONMENT);
      attributes.remove(HEADER_DEVICE_NAME);

      // Write to the file.
      BufferedWriter writer = null;

      try
      {
         writer = new BufferedWriter(new FileWriter(file));

         // Row #1: The column headers (attribute names).
         writer.write(HEADER_CLIENT_MNEMONIC);
         writer.write(",");
         writer.write(HEADER_MILL_ENVIRONMENT);
         writer.write(",");
         writer.write(HEADER_DEVICE_NAME);
         for (String attributeName : attributes)
         {
            writer.write(',');
            writer.write(attributeName);
         }

         int maxOffset = 0;
         ClientDevice[] devices = dao.getClientDevices(clientMnemonic, domain, 1, exportOffset);
         while (devices.length > 0)
         {
            // Row #2 - n: A row for each device and its attribute values.
            for (ClientDevice device : devices)
            {
               writer.newLine();

               // Write device name.
               writer.write(device.getClientMnemonic().toUpperCase());
               writer.write(',');
               writer.write(device.getMillenniumEnvironment().toUpperCase());
               writer.write(',');
               writer.write(device.getDeviceName());

               // Write device attribute values.
               for (String attributeName : attributes)
               {
                  writer.write(',');

                  String value = null;
                  if(attributeName.equalsIgnoreCase(HEADER_UPDT_DT_TM)) {
                     value= "N/A";
                     if(device.getUpdtDtTm()!=null) {
                        SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                        value = DATE_FORMATTER.format(device.getUpdtDtTm());
                     }
                     
                  }
                  else if(attributeName.equalsIgnoreCase(HEADER_LAST_UPDATED_BY)) {
                     value= (device.getLastUpdatedBy()!=null)? device.getLastUpdatedBy() : "N/A" ;
                  }
                  else {
                     value = device.getAttribute(attributeName).getValue();
                  }
                  if (value != null)
                  {
                     if (value.contains(","))
                     {
                        value = "\"" + value + "\"";
                     }

                     writer.write(value);
                  }
               }
            }
            
            maxOffset += devices.length;
            devices = dao.getClientDevices(clientMnemonic, domain, maxOffset+1, maxOffset+exportOffset);
         }
      }
      catch (Exception e)
      {
         // delete the incomplete file
         if(file != null && file.exists())
         {
            file.delete();
         }
         
         throw new ManagementException("An error occurred while exporting device data. The incomplete file was deleted.", e);
      }
      finally
      {
         if (writer != null)
         {
            try
            {
               writer.close();
            }
            catch (IOException e)
            {
               logger.logp(Level.WARNING, this.getClass().getName(), "getExportData",
                  "An exception occurred while writing export data.", e);
            }
         }
      }

      logger.exiting(this.getClass().getName(), "exportData");
   }

   public String importClientDevices(String path) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "importClientDevices");
      String stub = null;

      File newFile = new File(path);

      RMIFileTransfer ft = null;
      try
      {
         ft = new RMIFileTransferImpl(RMIFileTransfer.MODE_SERVER_RETRIEVE, newFile.getCanonicalPath());
      }
      catch (IOException e2)
      {
         throw new ManagementException(logger,
            "Cannot continue importing client devices, because the RMIFileTransfer class could not be constructed.",
            e2);
      }

      // Associate the listener.
      try
      {
         ft.addListener(new ClientDeviceFileTransferListener(dao, clientMnemonic, domain));
      }
      catch (RemoteException e1)
      {
         logger.logp(Level.WARNING, this.getClass().getName(), "importClientDevices",
            "Unable to attach a listener to file transfer object.", e1);
      }

      RMIContainer container = exportRMIFileTransfer(ft);
      stub = container.getRmiStub();

      // Serialize the stub.

      logger.exiting(this.getClass().getName(), "importClientDevices");

      // Return the stub in serialized format.
      return stub;
   }

   public Attribute[] listAttributes() throws ManagementException
   {
      logger.entering(this.getClass().getName(), "listAttributes");

      // get the potential attributes
      Attribute[] attributes = AttributeManager.getAttributes(dao);

      // get the column names
      List<String> list = Arrays.asList(dao.getClientDeviceTableColumns());
      Set<String> columns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      columns.addAll(list);

      // loop through the attributes to see if their column name is present
      // only take those with a valid column name
      List<Attribute> ret = new ArrayList<>();
      for (Attribute attr : attributes)
      {
         if (columns.contains(attr.getName()))
         {
            ret.add(attr);
         }
      }

      logger.exiting(this.getClass().getName(), "listAttributes");

      return ret.toArray(new Attribute[ret.size()]);
   }

   public Attribute retrieveAttribute(String attributeName)
   {
      logger.entering(this.getClass().getName(), "retrieveAttribute");

      Attribute attr = AttributeManager.getAttribute(attributeName);

      logger.exiting(this.getClass().getName(), "retrieveAttribute");

      return attr;
   }

   public void updateAttribute(String attributeName, boolean enabled) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "retrieveAttribute");

      Attribute attr = AttributeManager.getAttribute(attributeName);

      attr.enableAttribute(enabled);

      logger.exiting(this.getClass().getName(), "retrieveAttribute");
   }

   public ConfigurationSettings retrieveConfigurationSettings() throws ManagementException
   {
      logger.entering(this.getClass().getName(), "retrieveConfigurationSettings");

      ConfigurationSettings settings = ConfigurationSettingsManager.getInstance().generateConfigurationSettings();

      logger.exiting(this.getClass().getName(), "retrieveConfigurationSettings");

      return settings;
   }

   public void updateConfigurationSettings(ConfigurationSettings settings) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "updateConfigurationSettings");

      ConfigurationSettingsManager.getInstance().updateConfigurationSettings(settings);

      logger.exiting(this.getClass().getName(), "updateConfigurationSettings");
   }

   public LogicalDomain[] listLogicalDomains() throws ManagementException
   {
      logger.entering(this.getClass().getName(), "listLogicalDomains");

      try
      {
         return LogicalDomainCache.getInstance(domain).getLogicalDomains();
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "listLogicalDomains");
      }
   }

   public LogicalDomain retrieveLogicalDomain(long id) throws ManagementException
   {
      logger.entering(this.getClass().getName(), "retrieveLogicalDomain");

      try
      {
         return LogicalDomainCache.getInstance(domain).getLogicalDomain(id);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "retrieveLogicalDomain");
      }
   }

   public void updateLogicalDomain(String deviceName, String oldLogicalDomain, String newLogicalDomain)
      throws ManagementException
   {
      logger.entering(this.getClass().getName(), "retrieveLogicalDomain");

      try
      {
         dao.updateLogicalDomain(deviceName, clientMnemonic, domain, oldLogicalDomain, newLogicalDomain);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "retrieveLogicalDomain");
      }
   }
}
