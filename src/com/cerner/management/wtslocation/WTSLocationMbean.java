/*
 * File: WTSLocationMbean.java
 * Package: com.cerner.management.mbean.wtslocation
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation;

import com.cerner.management.MBeanService;
import com.cerner.management.data.dao.DAOException;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.wtslocation.configuration.Attribute;
import com.cerner.management.wtslocation.configuration.ConfigurationSettings;

/**
 * WTS MBean.
 */
public interface WTSLocationMbean extends MBeanService
{
   /**
    * MBean Service Type
    */
   public static final String WTS_MBEAN_TYPE = "WTSLocationManagementService";

   /**
    * Retrieves a client device in the environment.
    * 
    * @param deviceName
    * @return a client device or null if it cannot be found
    * @throws ManagementException
    */
   public ClientDevice retrieveClientDevice(String deviceName) throws ManagementException;
   
   /**
    * Retrieves a client device with the name and logical domain
    * @param deviceName
    * @param logicalDomain
    * @return the client device or null
    * @throws ManagementException
    */
   public ClientDevice retrieveClientDeviceWithLogicalDomain(String deviceName, String logicalDomain) throws ManagementException;

   /**
    * Retrieves a wrapper for the client devices in the environment. Limits the number of client
    * devices that can be returned based upon a property set by the user.
    * 
    * @param filter
    * @return the wrapped client devices
    * @throws ManagementException
    */
   public BoundedClientDevice listBoundedClientDevices(ClientDeviceFilter filter) throws ManagementException;

   /**
    * Adds a client device to the database.
    * 
    * @param device The client device to add
    * @throws ManagementException
    */
   public void addClientDevice(ClientDevice device) throws ManagementException;

   /**
    * Updates a client device in the database.
    * 
    * @param device The client device to update
    * @throws ManagementException
    */
   public void updateClientDevice(ClientDevice device) throws ManagementException;

   /**
    * Deletes a client device from the database.
    * 
    * @param device The client device to delete
    * @throws ManagementException
    */
   public void deleteClientDevice(ClientDevice device) throws ManagementException;

   /**
    * Exports all client devices and their attributes into a CSV file.
    * 
    * @return A String of the CSV file in serialized format.
    * @throws ManagementException
    */
   public String exportClientDevices() throws ManagementException;

   /**
    * Imports a client device and its attributes. The import will determine whether the device needs
    * to be created or updated.
    * 
    * @param path to create or modify
    * @return the stub of the import file
    * @throws ManagementException if an error occurs processing the import
    */
   public String importClientDevices(String path) throws ManagementException;

   /**
    * Lists the attributes defined for wtslocation
    * 
    * @return the list of attributes
    * @throws ManagementException 
    */
   public Attribute[] listAttributes() throws ManagementException;

   /**
    * Retrieves a single attribute that matches the supplied string
    * 
    * @param attributeName
    * @return the attribute or null
    */
   public Attribute retrieveAttribute(String attributeName);

   /**
    * Updates an attributes enabledness
    * 
    * @param attributeName
    * @param enabled
    * @throws ManagementException
    */
   public void updateAttribute(String attributeName, boolean enabled) throws ManagementException;

   /**
    * Retrieves the configuration settings
    * 
    * @return the configuration settings
    * @throws ManagementException
    */
   public ConfigurationSettings retrieveConfigurationSettings() throws ManagementException;

   /**
    * Updates the configuration settings
    * 
    * @param settings
    * @throws ManagementException
    */
   public void updateConfigurationSettings(ConfigurationSettings settings) throws ManagementException;

   /**
    * Lists the logical domains available for the millennium environment
    * 
    * @return the logical domains
    * @throws ManagementException
    */
   public LogicalDomain[] listLogicalDomains() throws ManagementException;

   /**
    * Retrieves the logical domain that matches an id
    * 
    * @param id
    * @return the logical domain or null
    * @throws ManagementException
    */
   public LogicalDomain retrieveLogicalDomain(long id) throws ManagementException;
   
   /**
    * Updates the logical domain for a device
    * 
    * @param deviceName The name of the device to update
    * @param oldLogicalDomain The old logical domain name
    * @param newLogicalDomain The new logical domain name
    * @throws ManagementException 
    */
   public void updateLogicalDomain(String deviceName, String oldLogicalDomain, String newLogicalDomain) throws ManagementException;
   
   /**
    * Lists the columns defined for wtslocation
    * 
    * @return the list of columns
    * @throws ManagementException 
    */
   public Column[] listColumns() throws ManagementException;
}
