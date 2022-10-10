/*
 * File: Attribute.java
 * Package: com.cerner.management.mbean.wtslocation
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation;

import javax.management.openmbean.CompositeData;

import com.cerner.management.CompositeDataHelper;

/**
 * WTSLocation Device Attribute Data Object
 */
public class ClientDeviceAttribute
{
   private final String name;
   private final String value;

   /**
    * Constructor
    
    * @param name
    * @param value
    */
   public ClientDeviceAttribute(String name, String value)
   {
      this.name = name;
      
      if(value != null)
      {
         this.value = value.trim();
      }
      else
      {
         this.value = value;
      }
   }

   /**
    * Creates a {@link ClientDeviceAttribute} from {@link CompositeData}.
    * 
    * @param cd {@link CompositeData} object that represents a {@link ClientDeviceAttribute} object.
    * @return a ClientDeviceAttribute object
    */
   public static ClientDeviceAttribute from(CompositeData cd)
   {
      ClientDeviceAttribute attribute = null;
      if (cd != null)
      {
         CompositeDataHelper cdh = new CompositeDataHelper(cd);

         attribute = new ClientDeviceAttribute(cdh.getString("Name"), cdh.getString("Value"));
      }
      return attribute;
   }

   /**
    * Retrieves the name of the attribute.
    * 
    * @return the attribute name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Retrieves the value of the attribute.
    * 
    * @return the attribute value
    */
   public String getValue()
   {
      return value;
   }
   
   public String toString()
   {
      return name + ":" + value;
   }
}
