
package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTransactionDelegate;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.ClientDeviceKey;

/**
 * Delegate for retrieving all the device names
 */
public class WTSLocationClientDeviceKeysDelegate implements SQLTransactionDelegate<ClientDeviceKey[]>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private List<ClientDeviceKey> results = new ArrayList<ClientDeviceKey>();
   private String millenniumEnvironment;
   private String clientMnemonic;

   /**
    * @param clientMnemonic
    * @param millenniumEnvironment
    */
   public WTSLocationClientDeviceKeysDelegate(String clientMnemonic, String millenniumEnvironment)
   {
      this.clientMnemonic = clientMnemonic;
      this.millenniumEnvironment = millenniumEnvironment;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#executeTransaction(com.cerner.management.datastore.DataStoreConfig,
    *      com.cerner.management.data.sql.impl.JDBCConnection)
    */
   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection) throws ManagementException,
      SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile = "sql/wtslocation.getClientDeviceKeys.sql";
      PreparedStatement stmt = null;
      ResultSet resultSet = null;
      try
      {
         String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);

         stmt = connection.prepareStatement(sql);
         stmt.setString(1, clientMnemonic.toLowerCase() + "%");
         stmt.setString(2, millenniumEnvironment.toLowerCase());
         stmt.execute();

         resultSet = stmt.getResultSet();
         while (resultSet.next())
         {
            String mnemonic = resultSet.getString("clientmnemonic");
            String deviceName = resultSet.getString("clientname");
            String logicalDomainId = resultSet.getString("logical_domain_id");

            results.add(new ClientDeviceKey(mnemonic.toUpperCase(), deviceName, logicalDomainId));
         }
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
   public ClientDeviceKey[] getResult()
   {
      return results.toArray(new ClientDeviceKey[results.size()]);
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#getTransactionMode()
    */
   public int getTransactionMode()
   {
      return MODE_NO_COMMIT;
   }

}
