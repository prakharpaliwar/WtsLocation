/*
 * File: WTSLocationAttributeMappingDelegate.java
 * Package: com.cerner.management.wtslocation.dao.impl.jdbc
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao.impl.jdbc;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.cerner.management.data.sql.SQLTransactionDelegate;
import com.cerner.management.data.sql.SQLTransactionFactory;
import com.cerner.management.data.sql.impl.JDBCConnection;
import com.cerner.management.datastore.DataStoreConfig;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;


/**
 * This delegate returns an array of mapping attribute objects which can be consumed by the
 * AttributeManager to build the mapping array for the GUI
 */
public class WTSLocationAttributeMappingDelegate implements SQLTransactionDelegate<Map<String, String>>
{
   private static final String REGISTRY_KEY_NAME = "registry_key_name";
   private static final String DB_COLUMN_NAME = "db_column_name";
   private static final String WTSLOCATION_GET_ATTRIBUTE_MAPPING_SQL_FILE = "sql/wtslocation.getAttributeMapping.sql";
   private static final Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao.impl.jdbc");
   private final Map<String, String> result = new HashMap<String, String>();

   @Override
   public int getTransactionMode()
   {
      return MODE_NO_COMMIT;
   }

   @Override
   public boolean executeTransaction(DataStoreConfig config, JDBCConnection connection)
      throws ManagementException, SQLException
   {
      logger.entering(this.getClass().getName(), "executeTransaction");
      String getAllAttributeMapping = SQLTransactionFactory.loadSQL(getClass(),
         WTSLOCATION_GET_ATTRIBUTE_MAPPING_SQL_FILE);
      PreparedStatement stmt = connection.prepareStatement(getAllAttributeMapping);
      stmt.execute();
      ResultSet resultSet = stmt.getResultSet();
      while (resultSet.next())
      {
         result.put(resultSet.getString(DB_COLUMN_NAME), resultSet.getString(REGISTRY_KEY_NAME));
      }
      logger.exiting(this.getClass().getName(), "executeTransaction");
      return true;
   }

   @Override
   public Map<String, String> getResult()
   {
      return result;
   }

}
