/*
 * File: WTSLocationManagementDAOImpl.java
 * Package: com.cerner.management.mbean.wtslocation.dao.impl
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.cerner.management.data.dao.DAOException;
import com.cerner.management.data.sql.SQLTransaction;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.datastore.DataStoreConfigManager;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.BoundedClientDevice;
import com.cerner.management.wtslocation.ClientDevice;
import com.cerner.management.wtslocation.ClientDeviceFilter;
import com.cerner.management.wtslocation.ClientDeviceKey;
import com.cerner.management.wtslocation.DSNFile;
import com.cerner.management.wtslocation.LogicalDomain;
import com.cerner.management.wtslocation.LogicalDomainCache;
import com.cerner.management.wtslocation.dao.WTSLocationManagementDAO;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationAttributeMappingDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationBoundedClientDeviceDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationBoundedClientDeviceDelegateOracle;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationClientDeviceDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationClientDeviceDelegateOracle;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationClientDeviceKeysDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationClientDevicesOffsetDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationClientDevicesOffsetDelegateOracle;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationClientDevicesDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationColumnsDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationDeleteClientDeviceDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationDeleteClientDeviceDelegateOracle;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationInsertClientDeviceDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationUpdateClientDeviceDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationUpdateClientDeviceDelegateOracle;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationUpdateLogicalDomainDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationUpdateLogicalDomainDelegateOracle;
import com.cerner.management.wtslocation.impl.WTSLocationMbeanImpl;

/**
 * Management DAO implementation for maintaining WTSLocation.
 */
public class WTSLocationManagementDAOImpl implements WTSLocationManagementDAO
{
   private static final String LOC_FIELD_REG_MAPPINGS_TABLE_NAME = "LOC_FIELD_REG_MAPPINGS";

   private static final String[] JDBC_META_TABLE_TYPE = new String[]{"TABLE"};

   private final long batchSize = Long.getLong(BATCH_SIZE_PROPERTY, 500);

   private static final String BATCH_SIZE_PROPERTY = "com.cerner.management.wtslocation.batch.size";
   /**
    * the logger for the class
    */
   private static final Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl");

   private DataStoreConfig primaryConfig = null;
   private boolean isPrimaryOracle = false;
   private DataStoreConfig secondaryConfig = null;
   private boolean isSecondaryOracle = false;

   /**
    * @param primaryDatastore
    * @param secondaryDatastore
    * @throws ManagementException
    */
   public WTSLocationManagementDAOImpl(String primaryDatastore, String secondaryDatastore) throws ManagementException
   {
      this.primaryConfig = DataStoreConfigManager.getDataStoreConfig(primaryDatastore);
      this.isPrimaryOracle = primaryConfig.getDatabaseVendor().equalsIgnoreCase("oracle");

      if (secondaryDatastore != null)
      {
         this.secondaryConfig = DataStoreConfigManager.getDataStoreConfig(secondaryDatastore);
         this.isSecondaryOracle = secondaryConfig.getDatabaseVendor().equalsIgnoreCase("oracle");
      }
   }

   /**
    * @param primaryFile
    * @param secondaryFile
    * @throws ManagementException
    */
   public WTSLocationManagementDAOImpl(DSNFile primaryFile, DSNFile secondaryFile) throws ManagementException
   {
      this.primaryConfig = primaryFile.generateDataStoreConfig();
      this.isPrimaryOracle = primaryConfig.getDatabaseVendor().equalsIgnoreCase("oracle");

      if (secondaryFile != null)
      {
         this.secondaryConfig = secondaryFile.generateDataStoreConfig();
         this.isSecondaryOracle = secondaryConfig.getDatabaseVendor().equalsIgnoreCase("oracle");
      }
   }

   public ClientDevice getClientDevice(String deviceName, String clientMnemonic, String millenniumEnvironment)
      throws DAOException
   {
      logger.entering(this.getClass().getName(), "getClientDevice");
      ClientDevice clientDevice = getClientDevice(deviceName, clientMnemonic, null, millenniumEnvironment);
      logger.exiting(this.getClass().getName(), "getClientDevice");
      return clientDevice;
   }

   public ClientDevice getClientDevice(String deviceName, String clientMnemonic, Long logicalDomainId,
      String millenniumEnvironment) throws DAOException
   {
      logger.entering(this.getClass().getName(), "getClientDevice");

      try
      {
         SQLTransaction<ClientDevice> transact;
         if (isPrimaryOracle)
         {
            transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationClientDeviceDelegateOracle(deviceName,
               clientMnemonic, logicalDomainId, millenniumEnvironment));
         }
         else
         {
            transact = SQLTransactionFactory.getSQLTransaction(
               new WTSLocationClientDeviceDelegate(deviceName, clientMnemonic, logicalDomainId, millenniumEnvironment));
         }

         transact.execute(primaryConfig);

         return transact.getResult();
      }
      catch (ManagementException e)
      {
         throw new DAOException(logger, e);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "getClientDevice");
      }
   }

   public ClientDevice[] getClientDevices(String clientMnemonic, String millenniumEnvironment) throws DAOException
   {
      logger.entering(this.getClass().getName(), "getClientDevices");

      try
      {
         SQLTransaction<ClientDevice[]> transact = SQLTransactionFactory
            .getSQLTransaction(new WTSLocationClientDevicesDelegate(clientMnemonic, millenniumEnvironment));

         transact.execute(primaryConfig);

         return transact.getResult();
      }
      catch (ManagementException e)
      {
         throw new DAOException(logger, e);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "getClientDevices");
      }
   }

   public ClientDeviceKey[] getClientDeviceKeys(String clientMnemonic, String millenniumEnvironment) throws DAOException
   {
      logger.entering(this.getClass().getName(), "getClientDeviceNames");

      try
      {
         SQLTransaction<ClientDeviceKey[]> transact = SQLTransactionFactory
            .getSQLTransaction(new WTSLocationClientDeviceKeysDelegate(clientMnemonic, millenniumEnvironment));

         transact.execute(primaryConfig);

         return transact.getResult();
      }
      catch (ManagementException e)
      {
         throw new DAOException(logger, e);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "getClientDeviceNames");
      }
   }

   public BoundedClientDevice getBoundedClientDevices(ClientDeviceFilter filter, String clientMnemonic,
      String millenniumEnvironment) throws DAOException
   {
      logger.entering(this.getClass().getName(), "getBoundedClientDevices");

      try
      {

         if (filter.getLogicalDomain() != null)
         {
            if (filter.getLogicalDomain().trim().length() == 0)
            {
               filter.setLogicalDomain("-1");
            }
            else
            {
               try
               {
                  Long.parseLong(filter.getLogicalDomain());
               }
               catch (NumberFormatException e)
               {
                  // not a number, get one
                  LogicalDomain ld = LogicalDomainCache.getInstance(millenniumEnvironment)
                     .getLogicalDomain(filter.getLogicalDomain());

                  if (ld != null)
                  {
                     filter.setLogicalDomain(Long.toString(ld.getId()));
                  }
               }
            }
         }

         SQLTransaction<BoundedClientDevice> transact;
         if (isPrimaryOracle)
         {
            transact = SQLTransactionFactory.getSQLTransaction(
               new WTSLocationBoundedClientDeviceDelegateOracle(filter, millenniumEnvironment, clientMnemonic));
         }
         else
         {
            transact = SQLTransactionFactory.getSQLTransaction(
               new WTSLocationBoundedClientDeviceDelegate(filter, millenniumEnvironment, clientMnemonic));
         }

         transact.execute(primaryConfig);

         return  transact.getResult();
      }
      catch (ManagementException e)
      {
         throw new DAOException(logger, e);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "getBoundedClientDevices");
      }
   }

   public void addClientDevice(ClientDevice device, String clientMnemonic, String millenniumEnvironment)
      throws DAOException
   {
      logger.entering(this.getClass().getName(), "addClientDevice");

      SQLTransaction<Object> transact = SQLTransactionFactory
         .getSQLTransaction(new WTSLocationInsertClientDeviceDelegate(new ClientDevice[]{device}, clientMnemonic,
            millenniumEnvironment, batchSize));

      try
      {
         try
         {
            transact.execute(primaryConfig);
         }
         catch (ManagementException e)
         {
            throw new DAOException(logger,
               "Unable to add client device to primary datastore. No changes will be made to secondary datastore.", e);
         }

         if (secondaryConfig != null)
         {
            try
            {
               transact.execute(secondaryConfig);
            }
            catch (ManagementException e)
            {
               throw new DAOException(logger,
                  "Unable to add client device to secondary datastore. Changes to the primary datastore will not be rolled back.",
                  e);
            }
         }
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "addClientDevice");
      }
   }

   public void updateClientDevice(ClientDevice device, String clientMnemonic) throws DAOException
   {
      logger.entering(this.getClass().getName(), "updateClientDevice");

      SQLTransaction<int[]> transact;
      if (isPrimaryOracle)
      {
         transact = SQLTransactionFactory
            .getSQLTransaction(new WTSLocationUpdateClientDeviceDelegateOracle(new ClientDevice[]{device}, batchSize));
      }
      else
      {
         transact = SQLTransactionFactory
            .getSQLTransaction(new WTSLocationUpdateClientDeviceDelegate(new ClientDevice[]{device}, batchSize));
      }

      try
      {
         try
         {
            transact.execute(primaryConfig);
         }
         catch (ManagementException e)
         {
            throw new DAOException(logger,
               "Unable to update client device to primary datastore. No changes will be made to secondary datastore.",
               e);
         }

         if (secondaryConfig != null)
         {
            if (isPrimaryOracle != isSecondaryOracle && isSecondaryOracle)
            {
               transact = SQLTransactionFactory.getSQLTransaction(
                  new WTSLocationUpdateClientDeviceDelegateOracle(new ClientDevice[]{device}, batchSize));
            }
            else if (isPrimaryOracle != isSecondaryOracle && !isSecondaryOracle)
            {
               transact = SQLTransactionFactory
                  .getSQLTransaction(new WTSLocationUpdateClientDeviceDelegate(new ClientDevice[]{device}, batchSize));
            }

            try
            {
               transact.execute(secondaryConfig);
            }
            catch (ManagementException e)
            {
               throw new DAOException(logger,
                  "Unable to update client device to secondary datastore. Changes to the primary datastore will not be rolled back.",
                  e);
            }
         }
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "updateClientDevice");
      }
   }

   public void deleteClientDevice(ClientDevice device) throws DAOException
   {
      logger.entering(this.getClass().getName(), "deleteClientDevice");

      SQLTransaction<Object> transact;
      if (isPrimaryOracle)
      {
         transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationDeleteClientDeviceDelegateOracle(device));
      }
      else
      {
         transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationDeleteClientDeviceDelegate(device));
      }

      try
      {
         try
         {
            transact.execute(primaryConfig);
         }
         catch (ManagementException e)
         {
            throw new DAOException(logger,
               "Unable to delete client device from primary datastore. No changes will be made to secondary datastore.",
               e);
         }

         if (secondaryConfig != null)
         {
            if (isPrimaryOracle != isSecondaryOracle && isSecondaryOracle)
            {
               transact = SQLTransactionFactory
                  .getSQLTransaction(new WTSLocationDeleteClientDeviceDelegateOracle(device));
            }
            else if (isPrimaryOracle != isSecondaryOracle && !isSecondaryOracle)
            {
               transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationDeleteClientDeviceDelegate(device));
            }

            try
            {
               transact.execute(secondaryConfig);
            }
            catch (ManagementException e)
            {
               throw new DAOException(logger,
                  "Unable to delete client device from secondary datastore. Changes to the primary datastore will not be rolled back.",
                  e);
            }
         }

      }
      finally
      {
         logger.exiting(this.getClass().getName(), "deleteClientDevice");
      }
   }

   public String[] getClientDeviceTableColumns() throws DAOException
   {
      logger.entering(this.getClass().getName(), "getClientDeviceTableColumns");

      try
      {
         SQLTransaction<String[]> transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationColumnsDelegate());

         transact.execute(primaryConfig);

         return transact.getResult();
      }
      catch (ManagementException e)
      {
         throw new DAOException(logger, e);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "getClientDeviceTableColumns");
      }
   }

   @Override
   public Map<String, String> getColumnRegistryMapping() throws DAOException
   {
      logger.entering(this.getClass().getName(), "getColumnRegistryMapping");
      SQLTransaction<Map<String, String>> transaction = SQLTransactionFactory
         .getSQLTransaction(new WTSLocationAttributeMappingDelegate());
      try
      {
         transaction.execute(primaryConfig);
      }
      catch (ManagementException e)
      {
         if (secondaryConfig != null)
         {
            try
            {
               transaction.execute(secondaryConfig);
            }
            catch (ManagementException e1)
            {
               throw new DAOException(logger,
                  "Unable to retrieve column registry mappings from either the primary or secondary database.", e);
            }
         }
         else
         {
            throw new DAOException(logger,
               "Unable to retrieve column registry mappings from either the primary or secondary database.", e);
         }
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "getColumnRegistryMapping");
      }
      return transaction.getResult();
   }

   @Override
   public boolean columnRegistryTableExists() throws ManagementException
   {
      try
      {
         DataSource dataSource = primaryConfig.getDataSource(WTSLocationMbeanImpl.class.getClassLoader());
         Connection connection = dataSource.getConnection();
         DatabaseMetaData meta = connection.getMetaData();
         ResultSet res = meta.getTables(null, null, LOC_FIELD_REG_MAPPINGS_TABLE_NAME, JDBC_META_TABLE_TYPE);
         if (res.next())
         {
            return true;
         }
      }
      catch (SQLException e)
      {
         if (secondaryConfig != null)
         {
            logger.log(Level.WARNING, "Could not query primary database using secondary database", e);
            try
            {
               DataSource dataSource = secondaryConfig.getDataSource(WTSLocationMbeanImpl.class.getClassLoader());
               Connection connection = dataSource.getConnection();
               DatabaseMetaData meta = connection.getMetaData();
               ResultSet res = meta.getTables(null, null, LOC_FIELD_REG_MAPPINGS_TABLE_NAME, JDBC_META_TABLE_TYPE);
               if (res.next())
               {
                  return true;
               }
            }
            catch (SQLException e1)
            {
               throw new DAOException(logger, "Error accessing the secondary database.", e);
            }
         }
         else
         {
            throw new DAOException(logger, "Error accessing the primary database.", e);
         }
      }
      return false;
   }

   public void batchAddClientDevices(ClientDevice[] devices, String clientMnemonic, String millenniumEnvironment)
      throws DAOException
   {
      logger.entering(this.getClass().getName(), "batchAddClientDevices");

      SQLTransaction<Object> transact = SQLTransactionFactory.getSQLTransaction(
         new WTSLocationInsertClientDeviceDelegate(devices, clientMnemonic, millenniumEnvironment, batchSize));

      try
      {
         try
         {
            transact.execute(primaryConfig);
         }
         catch (ManagementException e)
         {
            throw new DAOException(logger,
               "Unable to update client devices to primary datastore. No changes will be made to secondary datastore.",
               e);
         }

         if (secondaryConfig != null)
         {
            try
            {
               transact.execute(secondaryConfig);
            }
            catch (ManagementException e)
            {
               throw new DAOException(logger,
                  "Unable to update client devices to secondary datastore. Changes to the primary datastore will not be rolled back.",
                  e);
            }
         }
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "batchAddClientDevices");
      }

   }

   public int[] batchUpdateClientDevices(ClientDevice[] devices, String clientMnemonic) throws DAOException
   {
      logger.entering(this.getClass().getName(), "batchUpdateClientDevices");

      int[] ret = new int[0];
      SQLTransaction<int[]> transact;
      if (isPrimaryOracle)
      {
         transact = SQLTransactionFactory
            .getSQLTransaction(new WTSLocationUpdateClientDeviceDelegateOracle(devices, batchSize));
      }
      else
      {
         transact = SQLTransactionFactory
            .getSQLTransaction(new WTSLocationUpdateClientDeviceDelegate(devices, batchSize));
      }

      try
      {
         try
         {
            transact.execute(primaryConfig);
            ret = transact.getResult();
         }
         catch (ManagementException e)
         {
            throw new DAOException(logger,
               "Unable to update client device to primary datastore. No changes will be made to secondary datastore.",
               e);
         }

         if (secondaryConfig != null)
         {
            if (isPrimaryOracle != isSecondaryOracle && isSecondaryOracle)
            {
               transact = SQLTransactionFactory
                  .getSQLTransaction(new WTSLocationUpdateClientDeviceDelegateOracle(devices, batchSize));
            }
            else if (isPrimaryOracle != isSecondaryOracle && !isSecondaryOracle)
            {
               transact = SQLTransactionFactory
                  .getSQLTransaction(new WTSLocationUpdateClientDeviceDelegate(devices, batchSize));
            }

            try
            {
               transact.execute(secondaryConfig);
            }
            catch (ManagementException e)
            {
               throw new DAOException(logger,
                  "Unable to update client device to secondary datastore. Changes to the primary datastore will not be rolled back.",
                  e);
            }
         }
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "batchUpdateClientDevices");
      }

      return ret;
   }

   public void updateLogicalDomain(String deviceName, String clientMnemonic, String domain, String oldLogicalDomain,
      String newLogicalDomain) throws DAOException
   {
      logger.entering(this.getClass().getName(), "updateLogicalDomain");

      SQLTransaction<Object> transact;
      if (isPrimaryOracle)
      {
         transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationUpdateLogicalDomainDelegateOracle(deviceName,
            clientMnemonic, domain, oldLogicalDomain, newLogicalDomain));
      }
      else
      {
         transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationUpdateLogicalDomainDelegate(deviceName,
            clientMnemonic, domain, oldLogicalDomain, newLogicalDomain));
      }

      try
      {
         try
         {
            transact.execute(primaryConfig);
         }
         catch (ManagementException e)
         {
            throw new DAOException(logger, "Unable to update logical domain for device " + deviceName
               + " to primary datastore. No changes will be made to secondary datastore.", e);
         }

         if (secondaryConfig != null)
         {
            if (isPrimaryOracle != isSecondaryOracle && isSecondaryOracle)
            {
               transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationUpdateLogicalDomainDelegate(deviceName,
                  clientMnemonic, domain, oldLogicalDomain, newLogicalDomain));
            }
            else if (isPrimaryOracle != isSecondaryOracle && !isSecondaryOracle)
            {
               transact = SQLTransactionFactory.getSQLTransaction(new WTSLocationUpdateLogicalDomainDelegate(deviceName,
                  clientMnemonic, domain, oldLogicalDomain, newLogicalDomain));
            }

            try
            {
               transact.execute(secondaryConfig);
            }
            catch (ManagementException e)
            {
               throw new DAOException(logger, "Unable to update logical domain for device " + deviceName
                  + " to secondary datastore. Changes to the primary datastore will not be rolled back.", e);
            }
         }
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "updateLogicalDomain");
      }
   }

   @Override
   public ClientDevice[] getClientDevices(String clientMnemonic, String millenniumEnvironment, int startOffset,
      int endOffset) throws DAOException
   {
      logger.entering(this.getClass().getName(), "getClientDevices");
	try
	{
	      SQLTransaction<ClientDevice[]> transact ;
	      if (isPrimaryOracle)
	      {
		  transact = SQLTransactionFactory
			    .getSQLTransaction(new WTSLocationClientDevicesOffsetDelegateOracle(clientMnemonic.toLowerCase(), millenniumEnvironment.toLowerCase(), startOffset, endOffset));
	      }
	      else
	      {
		  transact = SQLTransactionFactory
			    .getSQLTransaction(new WTSLocationClientDevicesOffsetDelegate(clientMnemonic, millenniumEnvironment, startOffset, endOffset));
	      }

	 transact.execute(primaryConfig); 
	 return transact.getResult();

	}
	catch (ManagementException e)
	{
	throw new DAOException(logger, e);
	}
	finally
	{
	logger.exiting(this.getClass().getName(), "getClientDevices");
	}
   }
}
