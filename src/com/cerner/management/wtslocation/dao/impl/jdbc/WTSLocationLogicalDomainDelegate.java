/*
 * File: WTSLocationLogicalDomainDelegate.java
 * Package: com.cerner.management.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */
package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.cerner.management.data.sql.BaseSQLDelegate;
import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTransactionDelegate;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.LogicalDomain;

/**
 * Retrieve a single logical domain that matches an id
 */
public class WTSLocationLogicalDomainDelegate extends BaseSQLDelegate
{
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private long id;
   private LogicalDomain result = null;
   
   /**
    * @param id
    */
   public WTSLocationLogicalDomainDelegate(long id)
   {
      super("sql/wtslocation.getLogicalDomain");
      this.id = id;
   }

   public void prepare(PreparedStatement stmt, DataStoreConfig dataStore) throws SQLException, ManagementException
   {
      logger.entering(this.getClass().getName(), "prepare");
      
      stmt.setLong(1, this.id);
      
      logger.exiting(this.getClass().getName(), "prepare");
   }

   public LogicalDomain translate(ResultSet resultSet, DataStoreConfig dataStore) throws SQLException, ManagementException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");
      try
      {
         while (resultSet.next())
         {
            String logicalDomain = resultSet.getString("mnemonic");

            result = new LogicalDomain(id, logicalDomain);
         }
      }
      finally
      {
         SQLHelper.safeCloseResultSet(resultSet);
      }

      logger.exiting(this.getClass().getName(), "executeTransaction");
      return result;
   }

}
