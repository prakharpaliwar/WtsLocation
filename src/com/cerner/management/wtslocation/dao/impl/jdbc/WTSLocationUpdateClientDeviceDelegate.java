/*
 * File: WTSLocationUpdateClientDeviceDelegate.java
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
 * This delegate will update a client device.
 */
public class WTSLocationUpdateClientDeviceDelegate implements SQLTransactionDelegate<int[]>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private ClientDevice[] devices;
   private int[] updatedIdx = new int[0];
   private long batchSize;

   private Comparator<ClientDeviceAttribute> attributeComparator = new Comparator<ClientDeviceAttribute>()
   {
      public int compare(ClientDeviceAttribute o1, ClientDeviceAttribute o2)
      {
         return o1.getName().compareTo(o2.getName());
      }
   };

   /**
    * Constructor.
    * 
    * @param devices
    */
   public WTSLocationUpdateClientDeviceDelegate(ClientDevice[] devices, long batchSize)
   {
      if (devices == null || devices.length == 0)
      {
         throw new IllegalArgumentException("There must be some devices to update");
      }

      this.devices = devices;
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

      String sqlStatementFile = "sql/wtslocation.updateClientDevice.sql";
      String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);
      PreparedStatement stmt = null;

      try
      {
         StringBuffer buffer = new StringBuffer();

         ClientDeviceAttribute[] attrs = devices[0].getDeviceAttributes();
         Arrays.sort(attrs, attributeComparator);

         for (ClientDeviceAttribute attr : attrs)
         {
            if (!attr.getName().equalsIgnoreCase("Logical_Domain_id"))
            {
               buffer.append(',').append(attr.getName()).append(" = ?");
            }
         }
         List<Column> existingColumns = Arrays.asList(WTSLocationMbeanImpl.getInstance().listColumns());
         boolean lastUpdatedByColumnExistsInDB = (existingColumns!=null) && existingColumns.contains(new Column("Last_Updated_By"));
         if(lastUpdatedByColumnExistsInDB) 
         {
            buffer.append(',').append("Last_Updated_By = ?");
         }
         if((existingColumns!=null) && existingColumns.contains(new Column("UPDT_DT_TM"))) 
         {
            buffer.append(',').append("UPDT_DT_TM = getutcdate()");
         }
         
         sql = MessageFormat.format(sql, new Object[]{buffer.toString()});

         stmt = connection.prepareStatement(sql);
         
         long size = 0;
         for (ClientDevice device : devices)
         {
            int index = 1;
            stmt.setString(index++, device.getDeviceName().trim());

            ClientDeviceAttribute[] deviceAttributes = device.getDeviceAttributes();
            Arrays.sort(deviceAttributes, attributeComparator);

            // set prepared statement values
            for (ClientDeviceAttribute attr : deviceAttributes)
            {
               if (!attr.getName().equalsIgnoreCase("Logical_Domain_id"))
               {
                  String value = attr.getValue();
                  
                  if (attr.getName().equalsIgnoreCase("Default_Location"))
                  {
                     value = device.getDefaultLocation();
                  }
                  else if (attr.getName().equalsIgnoreCase("Device_Location"))
                  {
                     value = device.getDeviceLocation();
                  }
                  
                  if (value != null)
                  {
                     value.trim();
                  }
                  
                  stmt.setString(index++, value);
               }
            }
            if(lastUpdatedByColumnExistsInDB)
            {
               stmt.setString(index++, device.getLastUpdatedBy());
            }
            
            stmt.setString(index++, device.getDeviceId());
            stmt.setString(index++, device.getClientMnemonic());
            stmt.setString(index++, device.getMillenniumEnvironment());

            long ldi = -1l;

            if (device.getLogicalDomain() != null)
            {
               LogicalDomain ld = LogicalDomainCache.getInstance(device.getMillenniumEnvironment())
                  .getLogicalDomain(device.getLogicalDomain());
               if (ld != null)
               {
                  ldi = ld.getId();
               }
            }

            stmt.setString(index++, Long.toString(ldi));

            stmt.addBatch();
            size++;
            
            if(size >= batchSize)
            {
               stmt.executeBatch();
               stmt.clearBatch();
               size = 0;
            }
         }

         updatedIdx = stmt.executeBatch();
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
   public int[] getResult()
   {
      return updatedIdx;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#getTransactionMode()
    */
   public int getTransactionMode()
   {
      return MODE_MANUAL_COMMIT;
   }

}