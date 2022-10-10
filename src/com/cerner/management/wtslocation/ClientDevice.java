/*
 * File: ClientDevice.java
 * Package: com.cerner.management.mbean.wtslocation
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;

import com.cerner.management.CompositeDataHelper;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.security.SecurityUtil;

/**
 * WTSLocation Client Device Object
 */
public class ClientDevice
{
   private String clientMnemonic;
   private String millenniumEnvironment;
   private String deviceName;
   private String deviceId;
   private String logicalDomain;
   private String defaultLocation;
   private String deviceLocation;
   private final Date updtDtTm;
   private final String lastUpdatedBy;
   private Map<String, ClientDeviceAttribute> attributes = new HashMap<String, ClientDeviceAttribute>();
   

   /**
    * Constructor.
    * 
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @param deviceName
    * @param deviceId
    * @param logicalDomain
    * @param attributes
    * @param defaultLocation 
    * @param deviceLocation 
    * @param updtDtTm
    * @param lastUpdatedBy
    * @throws ManagementException
    */
   public ClientDevice(String clientMnemonic, String millenniumEnvironment, String deviceName, String deviceId,
      ClientDeviceAttribute[] attributes, String logicalDomain, String defaultLocation, String deviceLocation, Date updtDtTm, String lastUpdatedBy) throws ManagementException
   {
      this.clientMnemonic = clientMnemonic;      
      this.millenniumEnvironment = millenniumEnvironment;      
      this.deviceId = deviceId;      
      this.logicalDomain = logicalDomain;
      
      this.deviceName = deviceName;
      if(deviceName != null)
      {
         this.deviceName = this.deviceName.trim();
      }
      
      this.defaultLocation = defaultLocation;
      if(defaultLocation != null)
      {
         this.defaultLocation = this.defaultLocation.trim();
      }
      
      this.deviceLocation = deviceLocation;
      if(deviceLocation != null)
      {
         this.deviceLocation = this.deviceLocation.trim();
      }
      
      for (ClientDeviceAttribute attribute : attributes)
      {
         this.attributes.put(attribute.getName().toUpperCase(), attribute);
      }
      this.updtDtTm = updtDtTm;
      this.lastUpdatedBy = lastUpdatedBy;
   }

   /**
    * Constructor.
    * 
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @param deviceName
    * @param deviceId
    * @param logicalDomain
    * @param attributes
    * @param defaultLocation 
    * @param deviceLocation 
    * @throws ManagementException
    */
   public ClientDevice(String clientMnemonic, String millenniumEnvironment, String deviceName, String deviceId,
         ClientDeviceAttribute[] attributes, String logicalDomain, String defaultLocation, String deviceLocation) throws ManagementException
   { 
      this(clientMnemonic, millenniumEnvironment,deviceName,deviceId, attributes, logicalDomain, 
            defaultLocation,deviceLocation, null, SecurityUtil.getCurrentUser());
   }
   
   /**
    * Creates a {@link ClientDevice} from {@link CompositeData}.
    * 
    * @param cd {@link CompositeData} object that represents a {@link ClientDevice} object.
    * @return a ClientDevice object
    * @throws ManagementException
    * @throws InvalidKeyException
    */
   public static ClientDevice from(CompositeData cd) throws InvalidKeyException, ManagementException
   {
      ClientDevice device = null;
      if (cd != null)
      {
         CompositeDataHelper cdh = new CompositeDataHelper(cd);
         CompositeData[] attributes = (CompositeData[]) cdh.get("DeviceAttributes");

         ClientDeviceAttribute[] deviceAttributes = new ClientDeviceAttribute[0];

         if (attributes != null && attributes.length > 0)
         {
            deviceAttributes = new ClientDeviceAttribute[attributes.length];
            for (int i = 0; i < deviceAttributes.length; i++)
            {
               deviceAttributes[i] = ClientDeviceAttribute.from(attributes[i]);
            }
         }
         
         String logicalDomain = "";
         String defaultLocation="";
         String deviceLocation="";
         if (cdh.containsKey("LogicalDomain"))
         {
            logicalDomain = cdh.getString("LogicalDomain");
         }
         if (cdh.containsKey("DefaultLocation"))
         {
            defaultLocation = cdh.getString("DefaultLocation");
         }
         if (cdh.containsKey("DeviceLocation"))
         {
            deviceLocation = cdh.getString("DeviceLocation");
         }
         device = new ClientDevice(cdh.getString("ClientMnemonic"), cdh.getString("MillenniumEnvironment"),
            cdh.getString("DeviceName"), cdh.getString("DeviceId"), deviceAttributes, logicalDomain, defaultLocation, deviceLocation);
      }
      
      return device;
   }

   /**
    * Retrieves the client mnemonic.
    * 
    * @return the client mnemonic
    */
   public String getClientMnemonic()
   {
      return clientMnemonic;
   }

   /**
    * Retrieves the millennium environment
    * 
    * @return the millennium environment
    */
   public String getMillenniumEnvironment()
   {
      return millenniumEnvironment;
   }

   /**
    * Retrieves the name of the client device.
    * 
    * @return the device name
    */
   public String getDeviceName()
   {
      return deviceName;
   }

   /**
    * Retrieves the id of the client device (most likely the device name).
    * 
    * @return the device id
    */
   public String getDeviceId()
   {
      return deviceId;
   }

   /**
    * Retrieves the logical domain of the client device if applicable
    * 
    * @return the logical domain
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
    * @param lastUpdatedBy
    */
   public String getLastUpdatedBy()
   {
      return lastUpdatedBy;
   }
   
   /**
    * @param updtDtTm
    */
   public Date getUpdtDtTm()
   {
      return updtDtTm;
   }

   /**
    * @return the attributes of the client device
    */
   public ClientDeviceAttribute[] getDeviceAttributes()
   {
      return attributes.values().toArray(new ClientDeviceAttribute[attributes.size()]);
   }

   /**
    * Adds an attribute to the attributes of the device
    * 
    * @param name
    * @param value
    */
   public void addAttribute(String name, String value)
   {
      attributes.put(name.toUpperCase(), new ClientDeviceAttribute(name, value));
   }

   /**
    * Get an attribute for this device
    * 
    * @param name
    * @return the attribute that matches the name or null
    */
   public ClientDeviceAttribute getAttribute(String name)
   {
      return attributes.get(name.toUpperCase());
   }
   /**
    * @return The default location
    */
   public String getDefaultLocation()
   {
      return defaultLocation;
   }

   /**
    * @return The device location
    */
   public String getDeviceLocation()
   {
      return deviceLocation;
   }

   @Override
   public int hashCode()
   {
      
      int result = 1;
      result =  result + ((clientMnemonic == null) ? 0 : clientMnemonic.toLowerCase().hashCode() * 31);
      result =  result + ((deviceId == null) ? 0 : deviceId.toLowerCase().hashCode() + 41);
      result =  result + ((logicalDomain == null) ? 0 : logicalDomain.toLowerCase().hashCode() * 43);
      result =  result + ((millenniumEnvironment == null) ? 0 : millenniumEnvironment.toLowerCase().hashCode() + 47);
      return result;
   }
   
   @Override
      public boolean equals(Object o)
      {
         boolean val = false;
         if(o instanceof ClientDevice)
         {
            ClientDevice device = (ClientDevice) o;
            if(!this.getDeviceId().toLowerCase().equals(device.getDeviceId().toLowerCase())) {
               return false;
            }
            if(!this.getClientMnemonic().toLowerCase().equals(device.getClientMnemonic().toLowerCase())) {
               return false;
            }
            if(!this.getMillenniumEnvironment().toLowerCase().equals(device.getMillenniumEnvironment().toLowerCase())) {
               return false;
            }
            if(!this.getLogicalDomain().toLowerCase().equals(device.getLogicalDomain().toLowerCase())) {
               return false;
            }
            return true;
         }
         return val;
      }
    
}