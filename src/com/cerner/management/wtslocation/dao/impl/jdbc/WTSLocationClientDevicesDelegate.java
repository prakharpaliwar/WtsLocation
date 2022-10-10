/*
 * File: WTSLocationClientDeviceDelegate.java
 * Package: com.cerner.management.mbean.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.ClientDevice;

/**
 * This delegate will retrieve the client devices for a environment.
 */
public class WTSLocationClientDevicesDelegate extends BaseClientDeviceDelegate<ClientDevice[]>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private List<ClientDevice> results = new ArrayList<ClientDevice>();
   protected String millenniumEnvironment;
   protected String clientMnemonic;

   /**
    * @param clientMnemonic
    * @param millenniumEnvironment
    */
   public WTSLocationClientDevicesDelegate(String clientMnemonic, String millenniumEnvironment)
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

      String sqlStatementFile = "sql/wtslocation.getClientDevices.sql";
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
   public ClientDevice[] getResult()
   {
      return results.toArray(new ClientDevice[results.size()]);
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
      results.add(clientDevice);
   }
}
