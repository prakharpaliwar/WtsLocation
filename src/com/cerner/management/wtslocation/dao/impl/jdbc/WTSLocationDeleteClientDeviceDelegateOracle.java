/*
 * File: WTSLocationDeleteClientDeviceDelegate.java
 * Package: com.cerner.management.mbean.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTransactionDelegate;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.ClientDevice;
import com.cerner.management.wtslocation.LogicalDomain;
import com.cerner.management.wtslocation.LogicalDomainCache;

/**
 * This delegate will delete a client device from the environment.
 */
public class WTSLocationDeleteClientDeviceDelegateOracle implements SQLTransactionDelegate<Object>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private ClientDevice device;

   /**
    * @param deviceName
    * @param millenniumEnvironment
    * @param clientMnemonic
    */
   public WTSLocationDeleteClientDeviceDelegateOracle(ClientDevice device)
   {
      this.device = device;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#executeTransaction(com.cerner.management.datastore.DataStoreConfig,
    *      com.cerner.management.data.sql.impl.JDBCConnection)
    */
   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection) throws ManagementException,
      SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile = "sql/wtslocation.deleteClientDeviceOracle.sql";
      
      long logicalDomainId = -1;
      if (device.getLogicalDomain() != null)
      {
         LogicalDomain ld = LogicalDomainCache.getInstance(device.getMillenniumEnvironment()).getLogicalDomain(device.getLogicalDomain());
         if(ld != null)
         {
            logicalDomainId = ld.getId();
         }
      }
      
      String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);
      PreparedStatement stmt = null;

      try
      {
         stmt = connection.prepareStatement(sql);
         stmt.setString(1, device.getDeviceName().toLowerCase());
         stmt.setString(2, device.getClientMnemonic().toLowerCase());
         stmt.setString(3, device.getMillenniumEnvironment().toLowerCase());
         stmt.setString(4, Long.toString(logicalDomainId));
         stmt.execute();
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
