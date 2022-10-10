/*
 * File: WTSLocationClientDeviceOffsetDelegateOracle.java
 * Package: com.cerner.management.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */
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

/**
 * Delegate for retrieving client devices using offsets for pagination
 */
public class WTSLocationClientDevicesOffsetDelegateOracle extends WTSLocationClientDevicesDelegate
{
   // logger
   final private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");
   
   final private int startOffset;
   final private int endOffset;

   /**
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @param startOffset
    * @param endOffset
    */
   public WTSLocationClientDevicesOffsetDelegateOracle(String clientMnemonic, String millenniumEnvironment, int startOffset, int endOffset)
   {
      super(clientMnemonic, millenniumEnvironment);

      this.startOffset = startOffset;
      this.endOffset = endOffset;
   }

   @Override
   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection)
      throws ManagementException, SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile = "sql/wtslocation.getClientDevicesOffsetOracle.sql";
      PreparedStatement stmt = null;
      ResultSet resultSet = null;
      try
      {
         String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);

         stmt = connection.prepareStatement(sql);
         stmt.setString(1, clientMnemonic);
         stmt.setString(2, millenniumEnvironment);
         stmt.setString(3, clientMnemonic);
         stmt.setString(4, millenniumEnvironment);
         stmt.setInt(5, startOffset);
         stmt.setInt(6, endOffset);
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

}
