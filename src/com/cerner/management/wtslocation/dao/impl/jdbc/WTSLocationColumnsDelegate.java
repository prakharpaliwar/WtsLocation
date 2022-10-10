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

/**
 * Delegate for retrieveing the column names for WTSLocation
 */
public class WTSLocationColumnsDelegate implements SQLTransactionDelegate<String[]>
{
   // logger
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");

   private String[] columns;
   

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#executeTransaction(com.cerner.management.datastore.DataStoreConfig,
    *      com.cerner.management.data.sql.impl.JDBCConnection)
    */
   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection) throws ManagementException,
      SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");

      String sqlStatementFile;
      if(config.getDatabaseType().getVendor().contains("oracle"))
      {
         sqlStatementFile = "sql/wtslocation.getColumnNamesOracle.sql";
      }
      else
      {
         sqlStatementFile = "sql/wtslocation.getColumnNames.sql";
      }
      
      PreparedStatement stmt = null;
      ResultSet resultSet = null;
      
      try
      {
         String sql = SQLTransactionFactory.loadSQL(getClass(), sqlStatementFile);

         stmt = connection.prepareStatement(sql);
         stmt.execute();
         
         List<String> columnList = new ArrayList<String>();
         resultSet = stmt.getResultSet();
         while (resultSet.next())
         {
            columnList.add(resultSet.getString("column_name").toUpperCase());
         }

         columns = columnList.toArray(new String[columnList.size()]);
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
   public String[] getResult()
   {
      return columns;
   }

   /**
    * @see com.cerner.management.data.sql.SQLTransactionDelegate#getTransactionMode()
    */
   public int getTransactionMode()
   {
      return MODE_NO_COMMIT;
   }

}
