
package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.ClientDevice;

/**
 * Delegate for retrieving a single client device
 */
public class WTSLocationClientDeviceDelegateOracle extends BaseClientDeviceDelegate<ClientDevice>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private ClientDevice device;
   private String deviceName;
   private String millenniumEnvironment;
   private String clientMnemonic;
   private Long logicalDomainId;

   /**
    * @param deviceName
    * @param clientMnemonic
    * @param millenniumEnvironment
    */
   public WTSLocationClientDeviceDelegateOracle(String deviceName, String clientMnemonic, Long logicalDomainId, String millenniumEnvironment)
   {
      this.deviceName = deviceName;
      this.clientMnemonic = clientMnemonic;
      this.millenniumEnvironment = millenniumEnvironment;
      this.logicalDomainId = logicalDomainId;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#executeTransaction(com.cerner.management.datastore.DataStoreConfig,
    *      com.cerner.management.data.sql.impl.JDBCConnection)
    */
   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection) throws ManagementException,
      SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile = "sql/wtslocation.getClientDeviceOracle.sql";
      PreparedStatement stmt = null;
      ResultSet resultSet = null;
      try
      {
         String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);

         stmt = connection.prepareStatement(sql);
         stmt.setString(1, deviceName);
         stmt.setString(2, clientMnemonic.toLowerCase());
         stmt.setString(3, millenniumEnvironment.toLowerCase());
         stmt.setString(4, logicalDomainId != null ? "" + logicalDomainId : "-1");
         
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

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#getResult()
    */
   public ClientDevice getResult()
   {
      return device;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#getTransactionMode()
    */
   public int getTransactionMode()
   {
      return MODE_NO_COMMIT;
   }

   protected void handleClientDevice(ClientDevice clientDevice)
   {
      device = clientDevice;
   }
}
