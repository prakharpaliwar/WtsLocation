
package com.cerner.management.wtslocation.configuration;

import com.cerner.management.exception.ManagementException;

/**
 * Manager for interacting with configuration settings
 */
public class ConfigurationSettingsManager
{
   private static ConfigurationSettingsManager instance;

   /**
    * @return an instance of the manager
    */
   public synchronized static ConfigurationSettingsManager getInstance()
   {
      if (instance == null)
      {
         instance = new ConfigurationSettingsManager();
      }

      return instance;
   }

   /**
    * Generates the configuration settings
    * 
    * @return the configuration settings as defined in the registry
    * @throws ManagementException
    */
   public ConfigurationSettings generateConfigurationSettings() throws ManagementException
   {
      WTSLocationRegistry registry = WTSLocationRegistry.getInstance();

      String val = registry.getValue("UseUnicodeEncryption");
      boolean useUnicodeEncryption = val != null && val.equalsIgnoreCase("true");

      String dbUser = registry.getValue("DBUser");

      String dbPassword;
      if (useUnicodeEncryption)
      {
         String encrpytedPassword = registry.getValue("DBPasswordUnicode");
         dbPassword = WTSLocationRegistry.unicodeDecrypt(encrpytedPassword);
      }
      else
      {
         String encrpytedPassword = registry.getValue("DBPassword");
         dbPassword = WTSLocationRegistry.decrypt(encrpytedPassword);
      }

      String csvLocation = registry.getValue("CSVDataLocation");
      String maxPrinterAttemptCount = registry.getValue("AddPrinterAttemptMaxCount");
      String logFileLocation = registry.getValue("LogFileLocation");
      String debugLevel = registry.getValue("DebugLevel");

      val = registry.getValue("UseSeparateTempDirs");
      boolean separateTempDirs = val != null && val.equalsIgnoreCase("true");

      val = registry.getValue("DeleteFilesInTempDir");
      boolean deleteFilesInTempDir = val != null && val.equalsIgnoreCase("true");

      val = registry.getValue("SecurityLevel");
      boolean fullSecurity = val != null && val.equalsIgnoreCase("full security");

      val = registry.getValue("UseLocalLogicals");
      boolean useLocalLogicals = val != null && val.equalsIgnoreCase("true");

      val = registry.getValue("MULTI-TENANT");
      boolean isMultiTenant = val != null && val.equalsIgnoreCase("true");

      String millenniumUser = registry.getValue("Mill_User");

      String millenniumDomain = registry.getValue("Mill_Domain");
      
      val = registry.getValue("UseIntegratedSecurity");
      boolean useIntegratedSecurity = val != null && val.equalsIgnoreCase("true");

      String millenniumPassword;
      if (useUnicodeEncryption)
      {
         String encryptedMillenniumPassword = registry.getValue("Mill_Password_Unicode");
         millenniumPassword = WTSLocationRegistry.unicodeDecrypt(encryptedMillenniumPassword);
      }
      else
      {
         String encryptedMillenniumPassword = registry.getValue("Mill_Password");
         millenniumPassword = WTSLocationRegistry.decrypt(encryptedMillenniumPassword);
      }

      return new ConfigurationSettings(dbUser, dbPassword, csvLocation, logFileLocation, debugLevel, separateTempDirs,
         deleteFilesInTempDir, fullSecurity, useLocalLogicals, isMultiTenant, maxPrinterAttemptCount, millenniumUser,
         millenniumDomain, millenniumPassword, useIntegratedSecurity, useUnicodeEncryption);
   }

   /**
    * Updates the configuration settings to the registry
    * 
    * @param settings
    * @throws ManagementException
    */
   public void updateConfigurationSettings(ConfigurationSettings settings) throws ManagementException
   {
      WTSLocationRegistry registry = WTSLocationRegistry.getInstance();

      registry.setValue("DBUser", settings.getDatabaseUsername());
      
      if(settings.getUseUnicodeEncryption())
      {
         registry.setValue("DBPasswordUnicode", WTSLocationRegistry.unicodeEncrypt(settings.getDatabasePassword()));
      }
      else
      {
         registry.setValue("DBPassword", WTSLocationRegistry.encrypt(settings.getDatabasePassword()));
      }
      
      registry.setValue("CSVDataLocation", settings.getCSVLocation());
      registry.setValue("AddPrinterAttemptMaxCount", settings.getMaxPrinterAttemptCount());
      registry.setValue("LogFileLocation", settings.getLogFileLocation());
      registry.setValue("DebugLevel", settings.getDebugLevel());
      registry.setValue("UseSeparateTempDirs", (settings.getUseSeparateTempDir() ? "TRUE" : "FALSE"));
      registry.setValue("DeleteFilesInTempDir", (settings.getDeleteFilesInTempDir() ? "TRUE" : "FALSE"));
      registry.setValue("SecurityLevel",
         (settings.getTerminalServiceSecurity() ? "FULL SECURITY" : "RELAXED SECURITY"));
      registry.setValue("UseLocalLogicals", (settings.getUseLocalLogicals() ? "TRUE" : "FALSE"));
      registry.setValue("MULTI-TENANT", (settings.getIsMultiTenant() ? "TRUE" : "FALSE"));
      registry.setValue("Mill_User", settings.getMillenniumUser());
      registry.setValue("Mill_Domain", settings.getMillenniumDomain());
      registry.setValue("UseIntegratedSecurity", (settings.getUseIntegratedSecurity() ? "TRUE" : "FALSE"));
      
      if(settings.getUseUnicodeEncryption())
      {
         registry.setValue("Mill_Password_Unicode", WTSLocationRegistry.unicodeEncrypt(settings.getMillenniumPassword()));
      }
      else
      {
         registry.setValue("Mill_Password", WTSLocationRegistry.encrypt(settings.getMillenniumPassword()));
      }
      
      registry.setValue("UseUnicodeEncryption", (settings.getUseUnicodeEncryption() ? "TRUE" : "FALSE"));
   }
}
