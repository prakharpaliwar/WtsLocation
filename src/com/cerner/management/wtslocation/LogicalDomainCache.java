/*
 * File: LogicalDomainCache.java
 * Package: com.cerner.management.wtslocation
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cerner.management.data.dao.DAOException;
import com.cerner.management.domain.DomainConfig;
import com.cerner.management.domain.DomainConfigManager;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.management.wtslocation.dao.LogicalDomainDAO;
import com.cerner.management.wtslocation.dao.impl.LogicalDomainDAOImpl;
import com.cerner.management.wtslocation.configuration.WTSLocationRegistry;

/**
 * A cache for {@link LogicalDomain}s that is refreshed every 15 minutes
 */
public class LogicalDomainCache
{
   private static Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.dao");

   // 15 minute refresh rate
   private static final long REFRESH_RATE = 1000 * 60 * 15;

   private static Map<String, LogicalDomainCache> instances = new HashMap<String, LogicalDomainCache>();

   private long lastRefresh = 0;
   private DomainConfig config = null;
   private LogicalDomainDAO dao = new LogicalDomainDAOImpl();

   private Map<Long, LogicalDomain> idMap = new HashMap<Long, LogicalDomain>();
   private Map<String, LogicalDomain> nameMap = new HashMap<String, LogicalDomain>();

   /**
    * @return the instance of the cache
    * @throws ManagementException
    */
   public synchronized static LogicalDomainCache getInstance(String domain) throws ManagementException
   {
      if (!instances.containsKey(domain.toLowerCase()))
      {
         instances.put(domain.toLowerCase(), new LogicalDomainCache(domain));
      }

      return instances.get(domain.toLowerCase());
   }

   private LogicalDomainCache(String millEnviroment) throws ManagementException
   {
      // Mill_Domain get priority for getting the value over MILL_ENVIRONMENT
      String multiTenant = WTSLocationRegistry.getInstance().getValue("MULTI-TENANT");
      String millDomain = null;
      if (multiTenant != null && multiTenant.trim().length() != 0 && multiTenant.equalsIgnoreCase("TRUE"))
      {
         try
         {
            millDomain = WTSLocationRegistry.getInstance().getValue("Mill_Domain");
            if (millDomain == null || millDomain.trim().length() == 0)
            {
               logger.log(Level.INFO, "Mill_Domain  value  from the registry is null or empty.");
            }
            else
            {
               config = getConfig(millDomain);
            }
         }
         catch (ManagementException e)
         {
            logger.log(Level.WARNING, "Could not find a configured domain with the name Mill_Domain " + millDomain);
         }
         if (config == null)
         {
            try
            {
               if (millEnviroment == null || millEnviroment.trim().length() == 0)
               {
                  logger.log(Level.INFO, "MILL_ENVIRONMENT  value  from the registry is null or empty.");
               }
               else
               {
                  config = getConfig(millEnviroment);
               }
            }
            catch (ManagementException e)
            {
               logger.log(Level.WARNING,
                  "Could not find a configured domain with the name MILL_ENVIRONMENT " + millEnviroment);
            }
         }
      }
   }

   private DomainConfig getConfig(String domain) throws ManagementException
   {
      config = DomainConfigManager.getDomainConfig(domain);
      if (config == null)
      {
         config = DomainConfigManager.getDomainConfig(domain.toLowerCase());
      }
      if (config == null)
      {
         config = DomainConfigManager.getDomainConfig(domain.toUpperCase());
      }
      if (config == null)
      {
         throw new ManagementException(logger, "Could not find a configured domain with the name " + domain);
      }
      return config;
   }

   private void refreshLogicalDomains()
   {
      if (lastRefresh + REFRESH_RATE < System.currentTimeMillis())
      {
         idMap.clear();
         nameMap.clear();

         final CountDownLatch latch = new CountDownLatch(1);
         final List<LogicalDomain> domains = new ArrayList<>();
         new Thread(() -> {
            try
            {
               if (config != null)
               {
                  for (LogicalDomain logicalDomain : dao.listLogicalDomains(config))
                  {

                     domains.add(logicalDomain);
                  }
               }
            }
            catch (DAOException e)
            {
               logger.log(Level.INFO, "Could not retrive logical domains for domain " + config.getName(), e);
            }
            finally
            {
               latch.countDown();
            }
         }).start();

         try
         {
            latch.await();
         }
         catch (InterruptedException e)
         {
            logger.log(Level.INFO, "Could not retrive logical domains for domain " + config.getName(), e);
         }

         for (LogicalDomain domain : domains)
         {
            idMap.put(domain.getId(), domain);
            nameMap.put(domain.getLogicalDomain(), domain);
         }

         lastRefresh = System.currentTimeMillis();
      }
   }

   /**
    * Retrieve a {@link LogicalDomain} based on its id
    * 
    * @param id
    * @return the logical domain or null
    */
   public LogicalDomain getLogicalDomain(Long id)
   {
      synchronized (this)
      {
         refreshLogicalDomains();
         return idMap.get(id);
      }
   }

   /**
    * Retrieve a {@link LogicalDomain} based on its name
    * 
    * @param name
    * @return the logical domain or null
    */
   public LogicalDomain getLogicalDomain(String name)
   {
      synchronized (this)
      {
         refreshLogicalDomains();
         return nameMap.get(name);
      }
   }

   /**
    * Retrieves all {@link LogicalDomain} objects in the cache
    * 
    * @return all logical domains
    */
   public LogicalDomain[] getLogicalDomains()
   {
      synchronized (this)
      {
         refreshLogicalDomains();
         return idMap.values().toArray(new LogicalDomain[idMap.size()]);
      }
   }
}
