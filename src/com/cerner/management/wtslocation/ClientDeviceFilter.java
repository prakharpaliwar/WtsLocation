package com.cerner.management.wtslocation;

import javax.management.openmbean.CompositeData;

import com.cerner.management.CompositeDataHelper;

/**
 * Filter used when listing {@link ClientDevice} objects
 */
public class ClientDeviceFilter
{   
   private String attributeName;
   private String attributeValue;
   private String deviceName;
   private String deviceLocation;
   private String defaultLocation;
   private String logicalDomain;
   private String operator;
   private final Long startUpdtDtTm;
   private final Long endUpdtDtTm;
   private final String lastUpdatedBy;
   
   protected ClientDeviceFilter(CompositeData cd)
   {
      CompositeDataHelper cdh = new CompositeDataHelper(cd);
      attributeName = cdh.getString("AttributeName");
      attributeValue = cdh.getString("AttributeValue");
      deviceName = cdh.getString("DeviceName");
      deviceLocation= cdh.getString("DeviceLocation");
      defaultLocation= cdh.getString("DefaultLocation");
      logicalDomain= cdh.getString("LogicalDomain");
      operator = cdh.getString("Operator");
      startUpdtDtTm= cdh.getLong("StartUpdtDtTm");
      endUpdtDtTm= cdh.getLong("EndUpdtDtTm");
      lastUpdatedBy= cdh.getString("LastUpdatedBy");
   }
   
   /**
    * @return the name of the attribute to filter or null
    */
   public String getAttributeName()
   {
      return attributeName;
   }
   
   /**
    * @return the value of the attribute/device name to filter or null
    */
   public String getAttributeValue()
   {
      return attributeValue;
   }
   
   /**
    * @return the name of the device to filter or null
    */
   public String getDeviceName()
   {
      return deviceName;
   }
   
   /**
    * @return the operator to use when filtering
    */
   public String getOperator()
   {
      return operator;
   }

   /**
    * @return the device location of the filter
    */ 
   public String getDeviceLocation()
   {
      return deviceLocation;
   }

   /**
    * @return the default location of the filter
    */
   public String getDefaultLocation()
   {
      return defaultLocation;
   }

   /**
    * @return the logical domain of the filter 
    */
   public String getLogicalDomain()
   {
      return logicalDomain;
   }
   
   /**
    * @param logicalDomain
    */
   public void setLogicalDomain(String logicalDomain)
   {
      this.logicalDomain = logicalDomain;
   }
   
   /**
    * @return the From Date or Start Date of the UpdtDtTm of the filter
    */
   public Long getStartUpdtDtTm()
   {
      return startUpdtDtTm;
   }

   /**
    * @return the To Date or End Date of the UpdtDtTm of the filter
    */
   public Long getEndUpdtDtTm()
   {
      return endUpdtDtTm;
   }

   /**
    * @return the Last Updated By Value of the filter
    */ 
   public String getLastUpdatedBy()
   {
      return lastUpdatedBy;
   }
   
  /**
    * Required to created object.
    * @param cd
    * @return a ClientDeviceFilter object
    */
   public static ClientDeviceFilter from(CompositeData cd)
   {
      return new ClientDeviceFilter(cd);
   }
}