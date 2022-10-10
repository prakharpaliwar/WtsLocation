package com.cerner.management.wtslocation.configuration;

import java.util.logging.Logger;

import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;

/**
 * The definition of an attribute
 */
public class Attribute
{
   /**
    * the logger for the class
    */
   static final Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.configuration");
   
   private String name;
   private String enabledKeyName;
   private boolean configurable;

   /**
    * @param name
    * @param enabledKeyName
    */
   protected Attribute(String name, String enabledKeyName)
   {
      this.name = name;
      this.enabledKeyName = enabledKeyName;

      if (enabledKeyName == null)
      {
         this.configurable = false;
      }
      else
      {
         this.configurable = true;
      }
   }   

   /**
    * @return the name of the attribute
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return True if the attribute is enabled, false otherwise
    * @throws ManagementException 
    */
   public boolean getEnabled() throws ManagementException
   {
      if(getEnabledKeyName() == null)
      {
         return true;
      }
      
      return WTSLocationRegistry.getInstance().getValue(getEnabledKeyName()).equals("TRUE");
   }

   /**
    * @return the registry key name that controls whether the attribute is enabled
    */
   public String getEnabledKeyName()
   {
      return enabledKeyName;
   }
   
   /**
    * @return True if this attribute can be modified
    */
   public boolean getConfigurable()
   {
      return configurable;
   }
   
   /**
    * Enables or disables an attribute
    * 
    * @param enabled True to enbale the attribute, false otherwise
    * @throws ManagementException
    *
    */
   public void enableAttribute(boolean enableAttribute) throws ManagementException
   {
      if (enableAttribute)
      {
         WTSLocationRegistry.getInstance().setValue(getEnabledKeyName(), "TRUE");
      }
      else
      {
         WTSLocationRegistry.getInstance().setValue(getEnabledKeyName(), "FALSE");
      }
   }

}
