/*
 * File: ClientDeviceKey.java
 * Package: com.cerner.management.wtslocation
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation;

/**
 * Basic requirement to match a client device
 */
public class ClientDeviceKey
{
   private String clientMnemonic;
   private String deviceName;
   private String logicalDomainId;

   /**
    * @param clientMnemonic
    * @param deviceName
    * @param logicalDomainId
    */
   public ClientDeviceKey(String clientMnemonic, String deviceName, String logicalDomainId)
   {
      if (clientMnemonic == null || deviceName == null)
      {
         throw new IllegalArgumentException("The client mnemonic and device name of a ClientDeviceKey cannot be null.");
      }

      this.clientMnemonic = clientMnemonic.toUpperCase();
      this.deviceName = deviceName.toLowerCase();
      this.logicalDomainId = logicalDomainId;
   }

   /**
    * @return the client mnemonic, logical domain included if applicable
    */
   public String getClientMnemonic()
   {
      return clientMnemonic;
   }

   /**
    * @return the device name
    */
   public String getDeviceName()
   {
      return deviceName;
   }

   /**
    * @return the id of the logical domain of the device or null
    */
   public String getLogicalDomainId()
   {
      return logicalDomainId;
   }

   public boolean equals(Object o)
   {
      if (!(o instanceof ClientDeviceKey))
      {
         return false;
      }

      ClientDeviceKey key = (ClientDeviceKey) o;

      boolean value = this.clientMnemonic.equals(key.getClientMnemonic());
      value = value && this.deviceName.equals(key.getDeviceName());
      
      if (this.logicalDomainId == null)
      {
         return value && key.logicalDomainId == null;
      }

      return value && this.logicalDomainId.equals(key.getLogicalDomainId());
   }

   public int hashCode()
   {
      return clientMnemonic.hashCode() * 7 + deviceName.hashCode() * 11 + (logicalDomainId != null ? logicalDomainId.hashCode() * 13 : 0);
   }
}
