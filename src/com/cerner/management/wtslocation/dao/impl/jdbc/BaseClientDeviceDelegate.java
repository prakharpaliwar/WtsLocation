/*
 * File: BaseClientDeviceDelegate.java
 * Package: com.cerner.management.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLTimestampUtil;
import com.cerner.management.data.sql.SQLTransactionDelegate;
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
 * Base delegate for retrieving {@link ClientDevice} objects
 */
public abstract class BaseClientDeviceDelegate<T> implements SQLTransactionDelegate<T>
{  
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao");
   
   protected List<Column> existingColumns =null;
   protected boolean updtDtTmExists = false;
   protected boolean lastUpdatedByExists = false;
   
   protected BaseClientDeviceDelegate(){
      try
      {
         existingColumns = Arrays.asList(WTSLocationMbeanImpl.getInstance().listColumns());
      }
      catch (ManagementException e)
      {
         logger.log(Level.INFO, "Could not retrieve the list of columns from Database.");
      }
      updtDtTmExists = (existingColumns!=null && existingColumns.contains(new Column("UPDT_DT_TM")));
      lastUpdatedByExists = (existingColumns!=null && existingColumns.contains(new Column("Last_Updated_By")));
   }
   protected void resolveClientDevices(ResultSet resultSet) throws SQLException, ManagementException
   {
      ResultSetMetaData rsmd = resultSet.getMetaData();
      int numColumns = rsmd.getColumnCount();

      while (resultSet.next())
      {
         String localClientMnemonic = resultSet.getString("clientmnemonic");
         String localMillenniumEnvironment = resultSet.getString("millenvironment");
         String localDeviceName = resultSet.getString("clientname");
         String logicalDomainId = resultSet.getString("logical_domain_id");
         String defaultLocation = resultSet.getString("Default_Location");
         String deviceLocation = resultSet.getString("Device_Location");
         Date updtDtTmDate = null;
         if(updtDtTmExists) {
            TimeZone timeZone= TimeZone.getTimeZone("UTC");
            Timestamp timeStamp = resultSet.getTimestamp("UPDT_DT_TM");
            if (timeStamp!=null) {
               updtDtTmDate = SQLTimestampUtil.getTime(timeStamp,timeZone);
            }
         }
         String lastUpdatedByValue = null;
         if(lastUpdatedByExists) {
            lastUpdatedByValue = resultSet.getString("Last_Updated_By");
         }
         
         String logicalDomainName = "";
         if (logicalDomainId != null)
         {
            try
            {
               long id = Long.parseLong(logicalDomainId);
               LogicalDomain ld = LogicalDomainCache.getInstance(localMillenniumEnvironment).getLogicalDomain(id);
               
               if (ld != null)
               {
                  logicalDomainName = ld.getLogicalDomain();
               }
            }
            catch (NumberFormatException e)
            {
               // Don't set logical domain name
            }
         }

         List<ClientDeviceAttribute> attributes = new ArrayList<ClientDeviceAttribute>();

         for (int i = 4; i <= numColumns; i++)
         {
            String columnName = rsmd.getColumnName(i);
            
            if ((!columnName.equalsIgnoreCase("UPDT_DT_TM")) && (!columnName.equalsIgnoreCase("Last_Updated_By"))) {
               attributes.add(new ClientDeviceAttribute(columnName, resultSet.getString(columnName)));
            }
         }

         ClientDevice device = new ClientDevice(localClientMnemonic, localMillenniumEnvironment, localDeviceName,
            localDeviceName, attributes.toArray(new ClientDeviceAttribute[attributes.size()]), logicalDomainName, defaultLocation, deviceLocation,updtDtTmDate,lastUpdatedByValue);

         handleClientDevice(device);
      }
   }

   public int getTransactionMode()
   {
      return MODE_NO_COMMIT;
   }

   abstract public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection)
      throws ManagementException, SQLException;

   abstract public T getResult();
   
   abstract protected void handleClientDevice(ClientDevice clientDevice);
}