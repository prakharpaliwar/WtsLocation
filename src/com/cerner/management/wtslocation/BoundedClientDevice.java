
package com.cerner.management.wtslocation;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * The bounded object for client devices. Limits the number of client devices that can be returned
 * to 5000.
 */
public class BoundedClientDevice
{
   private final TreeSet<ClientDevice> devices;
   private boolean overflow = false;

   private final int max = Integer.getInteger(MAX_RECORDS_PROPERTY, MAX_RECORDS);

   /** The total number of records that this wrapper can contain */
   private static final int MAX_RECORDS = 1000;

   private static final String MAX_RECORDS_PROPERTY = "com.cerner.management.wtslocation.devices.max.records";

   /**
    * Constructor
    */
   public BoundedClientDevice()
   {
      devices = new TreeSet<ClientDevice>(deviceComparator);
   }

   /**
    * @return The wrapped client devices
    */
   public ClientDevice[] getClientDevice()
   {
      return devices.toArray(new ClientDevice[devices.size()]);
   }

   /**
    * @return True if there were more devices than could be displayed
    */
   public boolean getOverflow()
   {
      return overflow;
   }

   /**
    * @return The number of devices wrapped in the object
    */
   public long getNumberOfResults()
   {
      return devices.size();
   }

   /**
    * @param device
    */
   public void add(ClientDevice device)
   {
      devices.add(device);

      if (devices.size() > max)
      {
         devices.remove(devices.last());

         overflow = true;
      }
   }
   
   /**
    * @param device
    */
   public void remove(ClientDevice device)
   {
      devices.remove(device);
   }

   /**
    * @param device
    * @return true if the device is bounded
    */
   public boolean contains(ClientDevice device)
   {
      return devices.contains(device);
   }

   private Comparator<ClientDevice> deviceComparator = new Comparator<ClientDevice>()
   {
      public int compare(ClientDevice cd1, ClientDevice cd2)
      {
         int compare = cd1.getDeviceName().compareTo(cd2.getDeviceName());
         if(compare == 0)
         {
            compare = cd1.getClientMnemonic().compareTo(cd2.getClientMnemonic());
         }
         if(compare == 0)
         {
            compare = cd1.getLogicalDomain().compareTo(cd2.getLogicalDomain());
         }
         
         return compare;
      }
   };

}