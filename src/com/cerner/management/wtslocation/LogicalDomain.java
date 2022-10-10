/*
 * File: LogicalDomain.java
 * Package: com.cerner.management.wtslocation
 * Project: management-wtslocation
 */
package com.cerner.management.wtslocation;

/**
 * A logical domain for a multi-tenant environment
 */
public class LogicalDomain
{
   private long id;
   private String logicalDomain;
   
   /**
    * @param id
    * @param logicalDomain
    */
   public LogicalDomain(long id, String logicalDomain)
   {
      this.id = id;
      this.logicalDomain = logicalDomain;
   }
   
   /**
    * @return the id of the logical domain
    */
   public long getId()
   {
      return id;
   }
   
   /**
    * @return the name of the logical domain
    */
   public String getLogicalDomain()
   {
      return logicalDomain;
   }

}
