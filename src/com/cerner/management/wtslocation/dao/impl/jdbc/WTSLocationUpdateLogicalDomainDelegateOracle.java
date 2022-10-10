/*
 * File: WTSLocationUpdate.java
 * Package: com.cerner.management.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLHelper;
import com.cerner.management.data.sql.SQLTransactionDelegate;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.security.SecurityUtil;
import com.cerner.management.wtslocation.Column;
import com.cerner.management.wtslocation.LogicalDomain;
import com.cerner.management.wtslocation.LogicalDomainCache;
import com.cerner.management.wtslocation.impl.WTSLocationMbeanImpl;

/**
 * Delegate for updating the logical domain for a device
 */
public class WTSLocationUpdateLogicalDomainDelegateOracle implements SQLTransactionDelegate<Object>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private String deviceName;
   private String clientMnemonic;
   private String domain;
   private String oldLogicalDomain;
   private String newLogicalDomain;

   /**
    * @param deviceName
    * @param clientMnemonic
    * @param domain
    * @param oldLogicalDomain
    * @param newLogicalDomain
    */
   public WTSLocationUpdateLogicalDomainDelegateOracle(String deviceName, String clientMnemonic, String domain,
      String oldLogicalDomain, String newLogicalDomain)
   {
      this.deviceName = deviceName;
      this.clientMnemonic = clientMnemonic;
      this.domain = domain;
      this.oldLogicalDomain = oldLogicalDomain;
      this.newLogicalDomain = newLogicalDomain;
   }

   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection)
      throws ManagementException, SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile = "sql/wtslocation.updateLogicalDomainOracle.sql";

      String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);
      PreparedStatement stmt = null;

      LogicalDomainCache cache = LogicalDomainCache.getInstance(domain);
      StringBuffer buffer = new StringBuffer();
      
      long oldLogicalDomainId = -1;
      if (oldLogicalDomain != null)
      {
         LogicalDomain ld = cache.getLogicalDomain(oldLogicalDomain);
         if (ld != null)
         {
            oldLogicalDomainId = ld.getId();
         }
      }

      long newLogicalDomainId = -1;
      if (newLogicalDomain != null)
      {
         LogicalDomain ld = cache.getLogicalDomain(newLogicalDomain);
         if (ld != null)
         {
            newLogicalDomainId = ld.getId();
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
         buffer.append(',').append("UPDT_DT_TM = (select cast(sys_extract_utc(systimestamp) as date) from dual)");
      }
      sql = MessageFormat.format(sql, new Object[]{buffer.toString()});
      
      try
      {
         stmt = connection.prepareStatement(sql);
         int index = 1;
         stmt.setString(index++, Long.toString(newLogicalDomainId));
         if(lastUpdatedByColumnExistsInDB)
         {
            stmt.setString(index++, SecurityUtil.getCurrentUser());
         }
         stmt.setString(index++, deviceName);
         stmt.setString(index++, clientMnemonic);
         stmt.setString(index++, domain);
         stmt.setString(index++, Long.toString(oldLogicalDomainId));
         stmt.execute();
      }
      finally
      {
         SQLHelper.safeReleaseStatement(connection, stmt);
      }

      logger.exiting(this.getClass().getName(), "executeTransaction");
      return true;
   }

   public Object getResult()
   {
      return null;
   }

   public int getTransactionMode()
   {
      return MODE_NO_COMMIT;
   }

}
