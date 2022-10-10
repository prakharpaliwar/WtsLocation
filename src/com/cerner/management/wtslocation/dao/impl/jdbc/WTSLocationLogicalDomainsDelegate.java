/*
 * File: WTSLocationLogicalDomainDelegate.java
 * Package: com.cerner.management.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */
package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.cerner.management.data.sql.BaseSQLDelegate;
import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.LogicalDomain;

/**
 * Delegate for retrieving logical domains
 */
public class WTSLocationLogicalDomainsDelegate extends BaseSQLDelegate<LogicalDomain[]>
{
   public WTSLocationLogicalDomainsDelegate()
   {
      super("sql/wtslocation.getLogicalDomains");
   }

   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private List<LogicalDomain> results = new ArrayList<LogicalDomain>();

   public void prepare(PreparedStatement stmt, DataStoreConfig dataStore) throws SQLException, ManagementException
   {
      logger.entering(this.getClass().getName(), "prepare");
      logger.exiting(this.getClass().getName(), "prepare");
   }

   public LogicalDomain[] translate(ResultSet resultSet, DataStoreConfig dataStore)
      throws SQLException, ManagementException
   {
      logger.entering(this.getClass().getName(), "translate");
      try
      {
         while (resultSet.next())
         {
            long id = resultSet.getLong("logical_domain_id");
            String logicalDomain = resultSet.getString("mnemonic");

            results.add(new LogicalDomain(id, logicalDomain));
         }
      }
      finally
      {
         SQLHelper.safeCloseResultSet(resultSet);
      }
      
      logger.exiting(this.getClass().getName(), "translate");
      return results.toArray(new LogicalDomain[results.size()]);
   }
}
