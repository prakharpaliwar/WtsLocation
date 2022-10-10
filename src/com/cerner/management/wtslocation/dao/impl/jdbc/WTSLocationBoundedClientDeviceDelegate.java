
package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTimestampUtil;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.BoundedClientDevice;
import com.cerner.management.wtslocation.ClientDevice;
import com.cerner.management.wtslocation.ClientDeviceFilter;

/**
 * Delegate for retrieving a {@link BoundedClientDevice} from the database.
 */
public class WTSLocationBoundedClientDeviceDelegate extends BaseClientDeviceDelegate<BoundedClientDevice>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private BoundedClientDevice bounded;
   private ClientDeviceFilter filter;
   private String millenniumEnvironment;
   private String clientMnemonic;

   /**
    * @param filter
    * @param millenniumEnvironment
    * @param clientMnemonic
    */
   public WTSLocationBoundedClientDeviceDelegate(ClientDeviceFilter filter, String millenniumEnvironment,
      String clientMnemonic)
   {
      this.filter = filter;
      this.millenniumEnvironment = millenniumEnvironment;
      this.clientMnemonic = clientMnemonic;
      this.bounded = new BoundedClientDevice();
   }

   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection)
      throws ManagementException, SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile = "sql/wtslocation.getBoundedClientDevices.sql";
      PreparedStatement stmt = null;
      ResultSet resultSet = null;
      try
      {
         String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);

         StringBuffer buffer = new StringBuffer();

         String value = null;
         Timestamp startUpdtDtTmValue=null;
         Timestamp endUpdtDtTmValue=null;
         if (filter.getAttributeName() != null)
         {
            buffer.append("and lower(");
            buffer.append(filter.getAttributeName());
            buffer.append(") ");
            buffer.append(filter.getOperator());
            buffer.append(" lower(?)");

            value = filter.getAttributeValue();
         }
         else if (filter.getDeviceName() != null)
         {
            buffer.append("and lower(clientname) ");
            buffer.append(filter.getOperator());
            buffer.append(" lower(?)");

            value = filter.getDeviceName();
         }
         else if (filter.getDefaultLocation() != null)
         {
            buffer.append("and lower(Default_Location) ");
            buffer.append(filter.getOperator());
            buffer.append(" lower(?)");

            value = filter.getDefaultLocation();
         }
         else if (filter.getDeviceLocation() != null)
         {
            buffer.append("and lower(Device_Location) ");
            buffer.append(filter.getOperator());
            buffer.append(" lower(?)");

            value = filter.getDeviceLocation();
         }
         else if (filter.getLogicalDomain() != null)
         {
            buffer.append("and logical_domain_id ");
            buffer.append(filter.getOperator());
            buffer.append(" ?");
            
            value = filter.getLogicalDomain();
         }
         else if (lastUpdatedByExists && (filter.getLastUpdatedBy() != null))
         {
            buffer.append("and lower(Last_Updated_By) ");
            buffer.append(filter.getOperator());
            buffer.append(" ?");
            
            value = filter.getLastUpdatedBy();
         }
         if(updtDtTmExists && (filter.getStartUpdtDtTm()!= null) && (filter.getStartUpdtDtTm()!= 0) && (filter.getEndUpdtDtTm()!= null) && (filter.getEndUpdtDtTm() != 0))
         {
             Long startUpdtDtTm = filter.getStartUpdtDtTm();
             Long endUpdtDtTm = filter.getEndUpdtDtTm();
             
             buffer.append(" and lower(Updt_Dt_Tm)");
             buffer.append(">=");
             buffer.append("?");
             buffer.append(" and lower(Updt_Dt_Tm)");
             buffer.append("<");
             buffer.append("?");
             
             startUpdtDtTmValue= SQLTimestampUtil.getTimestamp(new java.util.Date(startUpdtDtTm*1000), TimeZone.getTimeZone("UTC"));
             endUpdtDtTmValue= SQLTimestampUtil.getTimestamp(new java.util.Date(endUpdtDtTm*1000), TimeZone.getTimeZone("UTC"));
         }
         
         sql = MessageFormat.format(sql, new Object[]{buffer.toString()});

         stmt = connection.prepareStatement(sql);
         stmt.setString(1, clientMnemonic);
         stmt.setString(2, millenniumEnvironment);
         
         int index = 3;
         if (value != null)
         {
            stmt.setString(index++, value);
         }
         if (updtDtTmExists && (startUpdtDtTmValue !=null) && (endUpdtDtTmValue !=null))
         {
            stmt.setTimestamp(index++, startUpdtDtTmValue);
            stmt.setTimestamp(index++, endUpdtDtTmValue);
         }
         stmt.execute();

         resultSet = stmt.getResultSet();

         resolveClientDevices(resultSet);
      }
      finally
      {
         SQLHelper.safeCloseResultSet(resultSet);
         SQLHelper.safeReleaseStatement(connection, stmt);
      }

      logger.exiting(this.getClass().getName(), "executeTransaction");
      return true;
   }

   public BoundedClientDevice getResult()
   {
      return bounded;
   }

   public int getTransactionMode()
   {
      return MODE_NO_COMMIT;
   }

   protected void handleClientDevice(ClientDevice clientDevice)
   {
      bounded.add(clientDevice);
   }

}