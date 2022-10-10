/*
 * File: LogicalDomainDAOImpl.java
 * Package: com.cerner.management.wtslocation.dao.impl
 * Project: management-wtslocation
 */
package com.cerner.management.wtslocation.dao.impl;

import java.util.logging.Logger;

import com.cerner.management.data.dao.DAOException;
import com.cerner.management.data.sql.SQLCall;
import com.cerner.management.data.sql.SQLCallException;
import com.cerner.management.data.sql.SQLCallFactory;
import com.cerner.management.data.sql.SQLDelegate;
import com.cerner.management.domain.DomainConfig;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.LogicalDomain;
import com.cerner.management.wtslocation.dao.LogicalDomainDAO;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationLogicalDomainDelegate;
import com.cerner.management.wtslocation.dao.impl.jdbc.WTSLocationLogicalDomainsDelegate;

/**
 * Standard implementation of the {@link LogicalDomainDAO}
 */
public class LogicalDomainDAOImpl implements LogicalDomainDAO
{
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao");

   public LogicalDomain[] listLogicalDomains(DomainConfig config) throws DAOException
   {
      logger.entering(this.getClass().getName(), "listLogicalDomains");

      try
      {
         SQLDelegate delegate = new WTSLocationLogicalDomainsDelegate();
         SQLCall logicalDomainsCall = SQLCallFactory.getSQLCall(config.getDataStoreConfig(), delegate);
         
         LogicalDomain[] logicalDomains = (LogicalDomain[]) logicalDomainsCall.call();

         return logicalDomains;
      }
      catch (SQLCallException e)
      {
         throw new DAOException(logger, e);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "listLogicalDomains");
      }
   }

   public LogicalDomain getLogicalDomain(DomainConfig config, long id) throws DAOException
   {
      logger.entering(this.getClass().getName(), "getLogicalDomain");

      try
      {
         SQLDelegate delegate = new WTSLocationLogicalDomainDelegate(id);
         SQLCall logicalDomainsCall = SQLCallFactory.getSQLCall(config.getDataStoreConfig(), delegate);
         
         LogicalDomain logicalDomain = (LogicalDomain) logicalDomainsCall.call();
         
         return logicalDomain;
      }
      catch (SQLCallException e)
      {
         throw new DAOException(logger, e);
      }
      finally
      {
         logger.exiting(this.getClass().getName(), "getLogicalDomain");
      }
   }

}
