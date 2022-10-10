/*
 * File: LogicalDomainDAO.java
 * Package: com.cerner.management.wtslocation.dao
 * Project: management-wtslocation
 */
package com.cerner.management.wtslocation.dao;

import com.cerner.management.data.dao.DAOException;
import com.cerner.management.data.dao.ManagementDAO;
import com.cerner.management.domain.DomainConfig;
import com.cerner.management.wtslocation.LogicalDomain;

/**
 * DAO for retrieving logical domains
 */
public interface LogicalDomainDAO extends ManagementDAO
{   
   /**
    * Retrieves the logical domains for a given domain
    * 
    * @param config
    * @return the logical domains
    * @throws DAOException
    */
   public LogicalDomain[] listLogicalDomains(DomainConfig config) throws DAOException;
   
   /**
    * Retrieves a logical domain that matches an id
    * 
    * @param config
    * @param id
    * @return the logical domain or null
    * @throws DAOException
    */
   public LogicalDomain getLogicalDomain(DomainConfig config, long id) throws DAOException;
}
