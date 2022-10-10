/*
 * File: WTSLocationInsertClientDeviceDelegate.java
 * Package: com.cerner.management.mbean.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTransactionDelegate;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.ClientDevice;
import com.cerner.management.wtslocation.ClientDeviceAttribute;
import com.cerner.management.wtslocation.Column;
import com.cerner.management.wtslocation.LogicalDomain;
import com.cerner.management.wtslocation.LogicalDomainCache;
import com.cerner.management.wtslocation.impl.WTSLocationMbeanImpl;

/**
 * This delegate will add a client device to an environment.
 */
public class WTSLocationInsertClientDeviceDelegate implements SQLTransactionDelegate<Object>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private ClientDevice[] devices;
   private String clientMnemonic;
   private String millenniumEnvironment;
   private long batchSize;

   /**
    * @param devices
    * @param clientMnemonic
    * @param millenniumEnvironment
    */
   public WTSLocationInsertClientDeviceDelegate(ClientDevice[] devices, String clientMnemonic,
      String millenniumEnvironment, long batchSize)
   {
      if (devices == null || devices.length == 0)
      {
         throw new IllegalArgumentException("There must be some devices to insert");
      }

      this.devices = devices;
      this.clientMnemonic = clientMnemonic;
      this.millenniumEnvironment = millenniumEnvironment;
      this.batchSize = batchSize;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#executeTransaction(com.cerner.management.datastore.DataStoreConfig,
    *      com.cerner.management.data.sql.impl.JDBCConnection)
    */
   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection)
      throws ManagementException, SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile = "sql/wtslocation.insertClientDevice.sql";
      String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);
      PreparedStatement stmt = null;

      try
      {
         StringBuffer columnBuffer = new StringBuffer();
         StringBuffer valueBuffer = new StringBuffer();
         boolean logicalDomainInAttrs = false;

         ClientDeviceAttribute[] attrs = devices[0].getDeviceAttributes();
         Comparator<ClientDeviceAttribute> comparator = new Comparator<ClientDeviceAttribute>()
         {
            public int compare(ClientDeviceAttribute o1, ClientDeviceAttribute o2)
            {
               return o1.getName().compareTo(o2.getName());
            }
         };
         
         Arrays.sort(attrs, comparator);

         for (ClientDeviceAttribute attr : attrs)
         {
            if (attr.getName().equalsIgnoreCase("Logical_Domain_Id"))
            {
               logicalDomainInAttrs = true;
            }
            columnBuffer.append(attr.getName()).append(',');
            valueBuffer.append("?,");
         }

         columnBuffer.append("ClientMnemonic,MillEnvironment,ClientName");
         valueBuffer.append("?,?,?");

         if (!logicalDomainInAttrs)
         {
            columnBuffer.append(",Logical_Domain_Id");
            valueBuffer.append(",?");
         }

         List<Column> existingColumns = Arrays.asList(WTSLocationMbeanImpl.getInstance().listColumns());
         boolean lastUpdatedByColumnExistsInDB = (existingColumns!=null) && existingColumns.contains(new Column("Last_Updated_By"));
         if(lastUpdatedByColumnExistsInDB) 
         {    
            columnBuffer.append(",Last_Updated_By");
            valueBuffer.append(",?");
         }
         if(config.getDatabaseType().getVendor().contains("oracle") && (existingColumns!=null) && existingColumns.contains(new Column("UPDT_DT_TM")))
         {
            columnBuffer.append(",UPDT_DT_TM");
            valueBuffer.append(",(select cast(sys_extract_utc(systimestamp) as date) from dual)");
         }
         
         sql = MessageFormat.format(sql, new Object[]{columnBuffer.toString(), valueBuffer.toString()});

         stmt = connection.prepareStatement(sql);

         long size = 0;
         for (ClientDevice device : devices)
         {
            int index = 1;
            long logicalDomainId = -1l;
            if (device.getLogicalDomain() != null)
            {
               LogicalDomain ld = LogicalDomainCache.getInstance(millenniumEnvironment)
                  .getLogicalDomain(device.getLogicalDomain());
               if (ld != null)
               {
                  logicalDomainId = ld.getId();
               }
            }
            
            ClientDeviceAttribute[] deviceAttrs = device.getDeviceAttributes();
            Arrays.sort(deviceAttrs, comparator);

            for (ClientDeviceAttribute attr : deviceAttrs)
            {
               if (attr.getName().equalsIgnoreCase("logical_domain_id") && logicalDomainInAttrs)
               {
                  stmt.setString(index++, Long.toString(logicalDomainId));
               }
               else if (attr.getName().equalsIgnoreCase("Default_Location") && device.getDefaultLocation() != null)
               {
                  stmt.setString(index++, device.getDefaultLocation().trim());
               }
               else if (attr.getName().equalsIgnoreCase("Device_Location") && device.getDeviceLocation() != null)
               {
                  stmt.setString(index++, device.getDeviceLocation().trim());
               }
               else
               {
                  String value = attr.getValue();
                  if(value != null)
                  {
                     value = value.trim();
                  }
                  
                  stmt.setString(index++, value);
               }
            }

            stmt.setString(index++, clientMnemonic.toUpperCase());
            stmt.setString(index++, millenniumEnvironment.toUpperCase());
            stmt.setString(index++, device.getDeviceName().trim());

            if (!logicalDomainInAttrs)
            {
               stmt.setString(index++, Long.toString(logicalDomainId));
            }
            
            if(lastUpdatedByColumnExistsInDB) 
            {
               stmt.setString(index++, device.getLastUpdatedBy());
            }
            
            stmt.addBatch();
            size++;
            
            if(size >= batchSize)
            {
               stmt.executeBatch();
               stmt.clearBatch();
               size = 0;
            }

         }

         stmt.executeBatch();
      }
      finally
      {
         SQLHelper.safeReleaseStatement(connection, stmt);
      }

      logger.exiting(this.getClass().getName(), "executeTransaction");
      return true;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#getResult()
    */
   public Object getResult()
   {
      return null;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#getTransactionMode()
    */
   public int getTransactionMode()
   {
      return MODE_MANUAL_COMMIT;
   }
}