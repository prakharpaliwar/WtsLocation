
package com.cerner.management.wtslocation.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.cerner.management.exception.ManagementException;
import com.cerner.management.wtslocation.dao.WTSLocationManagementDAO;

/**
 * The definition of attributes for a device
 */
public class AttributeManager
{

   private static Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();

   static
   {
      attributeMap.put("DEFAULT_PRINTER", new Attribute("Default_Printer", "UseDefaultPrinter"));
      attributeMap.put("POWERCHART_PRINTER", new Attribute("Powerchart_Printer", "UsePowerchartDefaultPrinter"));
      attributeMap.put("PCID", new Attribute("PCID", "UsePersonMgmtPCID"));
      attributeMap.put("DOCSDEFAULTPRINTER", new Attribute("DocsDefaultPrinter", "UsePersonMgmtDocsDefaultPrinter"));
      attributeMap.put("REPORTDEFAULTPRINTER", new Attribute("ReportDefaultPrinter",
         "UsePersonMgmtReportDefaultPrinter"));
      attributeMap.put("DISPFROMLOC", new Attribute("DispFromLoc", "UseDispFromLoc"));
      
      attributeMap.put("PHARETAILSR", new Attribute("PhaRetailSR", "UsePhaRetailSR"));
      attributeMap.put("PHARETAILWS", new Attribute("PhaRetailWS", "UsePhaRetailWS"));
      attributeMap.put("PHALTCSR", new Attribute("PhaLTCSR", "UsePhaLTCSR"));
      attributeMap.put("PHALTCWS", new Attribute("PhaLTCWS", "UsePhaLTCWS"));
      attributeMap.put("PHAHOMESR", new Attribute("PhaHomeSR", "UsePhaHomeSR"));
      attributeMap.put("PHAHOMEWS", new Attribute("PhaHomeWS", "UsePhaHomeWS"));
      attributeMap.put("PHAMAILSR", new Attribute("PhaMailSR", "UsePhaMailSR"));
      attributeMap.put("PHAMAILWS", new Attribute("PhaMailWS", "UsePhaMailWS"));

      attributeMap.put("WINDEFAULTPRINTER", new Attribute("WinDefaultPrinter", "UseWinDefaultPrinter"));
      attributeMap.put("WINBACKUPPRINTER", new Attribute("WinBackupPrinter", "UseWinBackupPrinter"));

      attributeMap.put("AUTHENTICATIONMETHOD", new Attribute("AuthenticationMethod", "UseAuthenticationMethod"));
      attributeMap.put("IDLESESSIONTIMEOUT", new Attribute("IdleSessionTimeout", "UseIdleSessionTimeout"));
      attributeMap.put("IDLESESSIONTIMEOUTCOUNTDOWN", new Attribute("IdleSessionTimeoutCountdown",
         "UseIdleSessionTimeoutCountdown"));
      attributeMap.put("SECUREDSESSIONTIMEOUT", new Attribute("SecuredSessionTimeout", "UseSecuredSessionTimeout"));
      attributeMap.put("MANUALLYSECUREDSESSIONTIMEOUT", new Attribute("ManuallySecuredSessionTimeout",
         "UseManuallySecuredSessionTimeout"));
      attributeMap.put("SINGLEACTIVESESSION", new Attribute("SingleActiveSession", "UseSingleActiveSession"));
      attributeMap.put("SECUREDSESSIONMANUALCLOSE", new Attribute("SecuredSessionManualClose",
         "UseSecuredSessionManualClose"));
      attributeMap.put("DIALOGTIMEOUT", new Attribute("DialogTimeout", "UseDialogTimeout"));
      attributeMap.put("SECUREDSESSIONMANUALCLOSEAUTH", new Attribute("SecuredSessionManualCloseAuth",
         "UseSecuredSessionManualCloseAuthenticate"));
      attributeMap.put("CRMTIMERENABLED", new Attribute("CrmTimerEnabled", "UseCrmTimer"));

      attributeMap.put("LOGGINGSEVERITY", new Attribute("LoggingSeverity", "UseLoggingSeverity"));

      attributeMap.put("BMDI_DATA", new Attribute("BMDI_Data", "UseBMDIData"));
      attributeMap.put("BMDI_ODBC_ADR", new Attribute("BMDI_ODBC_Adr", "UseBMDIODBCAdr"));
      attributeMap.put("BMDI_ODBC_ACT", new Attribute("BMDI_ODBC_Act", "UseBMDIODBCAct"));

      attributeMap.put("TWAINMODEL", new Attribute("TWAINModel", "UseTWAINModel"));

      attributeMap.put("AP_IMAGE_CAPTURE", new Attribute("AP_Image_Capture", "UseAPImageCapture"));
      attributeMap.put("AP_IMAGE_STATION", new Attribute("AP_Image_Station", "UseAPImageStation"));

      attributeMap.put("DEFAULT_TAMPERPROOF_PRINTER", new Attribute("Default_Tamperproof_Printer",
         "Default_Tamperproof_Printer"));
      attributeMap.put("DEFAULT_TAMPERPROOF_PRINT_TRAY", new Attribute("Default_Tamperproof_Print_Tray",
         "Default_Tamperproof_Printer"));
      attributeMap.put("DEFAULT_TAMPERPROOF_PRINT_IMPL", new Attribute("Default_Tamperproof_Print_Impl",
                 "Default_Tamperproof_Printer"));

      attributeMap.put("TAMPERPROOF_PRINTER2", new Attribute("Tamperproof_Printer2", "Tamperproof_Printer2"));
      attributeMap.put("TAMPERPROOF_PRINTER2_TRAY", new Attribute("Tamperproof_Printer2_Tray", "Tamperproof_Printer2"));
      attributeMap.put("TAMPERPROOF_PRINTER2_IMPL", new Attribute("Tamperproof_Printer2_Impl", "Tamperproof_Printer2"));

      attributeMap.put("TAMPERPROOF_PRINTER3", new Attribute("Tamperproof_Printer3", "Tamperproof_Printer3"));
      attributeMap.put("TAMPERPROOF_PRINTER3_TRAY", new Attribute("Tamperproof_Printer3_Tray", "Tamperproof_Printer3"));
      attributeMap.put("TAMPERPROOF_PRINTER3_IMPL", new Attribute("Tamperproof_Printer3_Impl", "Tamperproof_Printer3"));

      attributeMap.put("TAMPERPROOF_PRINTER4", new Attribute("Tamperproof_Printer4", "Tamperproof_Printer4"));
      attributeMap.put("TAMPERPROOF_PRINTER4_TRAY", new Attribute("Tamperproof_Printer4_Tray", "Tamperproof_Printer4"));
      attributeMap.put("TAMPERPROOF_PRINTER4_IMPL", new Attribute("Tamperproof_Printer4_Impl", "Tamperproof_Printer4"));

      attributeMap.put("TAMPERPROOF_PRINTER5", new Attribute("Tamperproof_Printer5", "Tamperproof_Printer5"));
      attributeMap.put("TAMPERPROOF_PRINTER5_TRAY", new Attribute("Tamperproof_Printer5_Tray", "Tamperproof_Printer5"));
      attributeMap.put("TAMPERPROOF_PRINTER5_IMPL", new Attribute("Tamperproof_Printer5_Impl", "Tamperproof_Printer5"));

      attributeMap.put("PCS_TRACKING_LOCATION", new Attribute("PCS_Tracking_Location", "UsePCSTrackingLocation"));
      
      attributeMap.put("PHAIPPRINTER", new Attribute("PhaIPPRINTER", "UsePhaIPPRINTER"));
      attributeMap.put("PHAIPWS", new Attribute("PhaIPWS", "UsePhaIPWS"));
      attributeMap.put("PHAIPINVSR1", new Attribute("PhaIPInvSR1", "UsePhaIPInvSR1"));
      attributeMap.put("PHAIPINVSR2", new Attribute("PhaIPInvSR2", "UsePhaIPInvSR2"));
      
      attributeMap.put("SCS_DEFAULT_LOGIN_LOCATION", new Attribute("SCS_Default_Login_Location", "UseSCSDefaultLoginLocation"));
      attributeMap.put("SPECIMEN_LABEL_PRINTER", new Attribute("Specimen_Label_Printer", "UseSpecimenLabelPrinter"));
      attributeMap.put("PHARETAILDEVICE", new Attribute("PhaRetailDevice", "UsePhaRetailDevice"));
   }

   /**
    * @return the attributes from a map of Attributes created in a static block.
    */
   public static Attribute[] getAttributes(WTSLocationManagementDAO dao) throws ManagementException
   {
      synchronized (attributeMap)
      {
         Attribute[] attrs = null;
         if (dao.columnRegistryTableExists())
         {
            Map<String, String> map = dao.getColumnRegistryMapping();
            attributeMap.clear();
            for (Entry<String, String> entry : map.entrySet())
            {
               String name = entry.getKey();
               Attribute attribute = new Attribute(name, entry.getValue());
               attributeMap.put(name.toUpperCase(), attribute);
            }
         }
         attrs = attributeMap.values().toArray(new Attribute[attributeMap.size()]);
         return attrs;
      }
   }

   /**
    * @param name
    * @return the attribute that matches the name or null
    */
   public static Attribute getAttribute(String name)
   {
      Attribute attr = null;

      synchronized (attributeMap)
      {
         attr = attributeMap.get(name.toUpperCase());
      }

      return attr;
   }

   /**
    * @return the names of available attributes
    */
   public static String[] getAttributeNames()
   {
      String[] names = null;

      synchronized (attributeMap)
      {
         names = attributeMap.keySet().toArray(new String[attributeMap.size()]);
      }

      return names;
   }

}