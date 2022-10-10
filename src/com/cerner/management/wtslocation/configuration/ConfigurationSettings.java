
package com.cerner.management.wtslocation.configuration;

import javax.management.openmbean.CompositeData;

import com.cerner.management.CompositeDataHelper;

/**
 * Settings for a specific wtslocation configuration
 */
public class ConfigurationSettings
{
   private String databaseUsername;
   private String databasePassword;
   private String csvLocation;
   private String logFileLocation;
   private String debugLevel;
   private String maxPrinterAttemptCount;
   private String millenniumUser;
   private String millenniumDomain;
   private String millenniumPassword;

   private boolean useSeparateTempDir;
   private boolean deleteFilesInTempDir;
   private boolean terminalServiceSecurity;
   private boolean useLocalLogicals;
   private boolean isMultiTenant;
   private boolean useIntegratedSecurity;
   private boolean useUnicodeEncrpytion;

   /**
    * 
    * @param username
    * @param password
    * @param csvLocation
    * @param logLocation
    * @param debugLevel
    * @param useSeparateTempDir
    * @param deleteFilesInTempDir
    * @param terminalServiceSecurity
    * @param localLogicals
    * @param isMultiTenant
    * @param maxPrinterAttemptCount
    * @param millenniumUser
    * @param millenniumDomain
    * @param millenniumPassword
    * @param useIntegratedSecurity
    * @param useUnicodeEncryption
    */
   public ConfigurationSettings(String username, String password, String csvLocation, String logLocation,
      String debugLevel, boolean useSeparateTempDir, boolean deleteFilesInTempDir, boolean terminalServiceSecurity,
      boolean localLogicals, boolean isMultiTenant, String maxPrinterAttemptCount, String millenniumUser,
      String millenniumDomain, String millenniumPassword, boolean useIntegratedSecurity, boolean useUnicodeEncryption)
   {
      this.databaseUsername = username;
      this.databasePassword = password;
      this.csvLocation = csvLocation;
      this.logFileLocation = logLocation;
      this.debugLevel = debugLevel;
      this.maxPrinterAttemptCount = maxPrinterAttemptCount;
      this.millenniumUser = millenniumUser;
      this.millenniumDomain = millenniumDomain;
      this.millenniumPassword = millenniumPassword;

      this.useSeparateTempDir = useSeparateTempDir;
      this.deleteFilesInTempDir = deleteFilesInTempDir;
      this.terminalServiceSecurity = terminalServiceSecurity;
      this.useLocalLogicals = localLogicals;
      this.isMultiTenant = isMultiTenant;
      this.useIntegratedSecurity = useIntegratedSecurity;
      this.useUnicodeEncrpytion = useUnicodeEncryption;
   }

   /**
    * Creates a {@link ConfigurationSettings} object from {@link CompositeData}.
    * 
    * @param cd {@link CompositeData} object that represents a {@link ConfigurationSettings} object.
    * @return a ConfigurationSettings object
    */
   public static ConfigurationSettings from(CompositeData cd)
   {
      ConfigurationSettings settings = null;
      if (cd != null)
      {
         CompositeDataHelper cdh = new CompositeDataHelper(cd);

         settings = new ConfigurationSettings(getString(cdh, "DatabaseUsername"), getString(cdh, "DatabasePassword"),
            getString(cdh, "CSVLocation"), getString(cdh, "LogFileLocation"), getString(cdh, "DebugLevel"),
            getBoolean(cdh, "UseSeparateTempDir"), getBoolean(cdh, "DeleteFilesInTempDir"),
            getBoolean(cdh, "TerminalServiceSecurity"), getBoolean(cdh, "UseLocalLogicals"),
            getBoolean(cdh, "isMultiTenant"), getString(cdh, "MaxPrinterAttemptCount"),
            getString(cdh, "MillenniumUser"), getString(cdh, "MillenniumDomain"), getString(cdh, "MillenniumPassword"),
            getBoolean(cdh, "UseIntegratedSecurity"), getBoolean(cdh, "UseUnicodeEncryption"));
      }
      return settings;
   }

   private static String getString(CompositeDataHelper cdh, String key)
   {
      if (cdh.containsKey(key))
      {
         return cdh.getString(key);
      }

      return null;
   }

   private static boolean getBoolean(CompositeDataHelper cdh, String key)
   {
      if (cdh.containsKey(key))
      {
         return cdh.getBoolean(key);
      }

      return false;
   }

   /**
    * @return the databaseUsername
    */
   public String getDatabaseUsername()
   {
      return databaseUsername;
   }

   /**
    * @return the databasePassword
    */
   public String getDatabasePassword()
   {
      return databasePassword;
   }

   /**
    * @return the csvLocation
    */
   public String getCSVLocation()
   {
      return csvLocation;
   }

   /**
    * @return the logFileLocation
    */
   public String getLogFileLocation()
   {
      return logFileLocation;
   }

   /**
    * @return the debug level
    */
   public String getDebugLevel()
   {
      return debugLevel;
   }

   /**
    * @return The HNA Millennium username
    */
   public String getMillenniumUser()
   {
      return millenniumUser;
   }

   /**
    * @return The HNA Millennium domain
    */
   public String getMillenniumDomain()
   {
      return millenniumDomain;
   }

   /**
    * @return The HNA Millennium password
    */
   public String getMillenniumPassword()
   {
      return millenniumPassword;
   }

   /**
    * @return the useSeparateTempDir
    */
   public boolean getUseSeparateTempDir()
   {
      return useSeparateTempDir;
   }

   /**
    * @return the deleteFilesInTempDir
    */
   public boolean getDeleteFilesInTempDir()
   {
      return deleteFilesInTempDir;
   }

   /**
    * @return the terminalServiceSecurity
    */
   public boolean getTerminalServiceSecurity()
   {
      return terminalServiceSecurity;
   }

   /**
    * @return the useLocalLogicals
    */
   public boolean getUseLocalLogicals()
   {
      return useLocalLogicals;
   }

   /**
    * @return whether the device is setup for multi-tenant support
    */
   public boolean getIsMultiTenant()
   {
      return isMultiTenant;
   }

   public String getMaxPrinterAttemptCount()
   {
      return maxPrinterAttemptCount;
   }

   /**
    * @return The value of the "UseIntegratedSecurity" registry value. True if Integrated
    *         Security/Windows Auth should be used to connect to the DB
    */
   public boolean getUseIntegratedSecurity()
   {
      return useIntegratedSecurity;
   }

   /**
    * @return The value of the "useUnicodeEncryption" registry value. True if the new Unicode
    *         encryption method should be used.
    */
   public boolean getUseUnicodeEncryption()
   {
      return useUnicodeEncrpytion;
   }

}