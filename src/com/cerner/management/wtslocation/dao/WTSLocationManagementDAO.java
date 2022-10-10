/*
 * File: WTSLocationManagementDAO.java
 * Package: com.cerner.management.mbean.wtslocation.dao
 * Project: management-wtslocation
 */

package com.cerner.management.wtslocation.dao;

import java.util.Map;

import com.cerner.management.data.dao.DAOException;
import com.cerner.management.data.dao.ManagementDAO;
import com.cerner.management.exception.ManagementException;
import com.cerner.management.wtslocation.BoundedClientDevice;
import com.cerner.management.wtslocation.ClientDevice;
import com.cerner.management.wtslocation.ClientDeviceFilter;
import com.cerner.management.wtslocation.ClientDeviceKey;

/**
 * Management DAO for maintaining WTSLocation.
 */
public interface WTSLocationManagementDAO extends ManagementDAO
{
   /**
    * Retrieve a client device in the environment.
    * 
    * @param deviceName
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @return a client device
    * @throws DAOException
    */
   public ClientDevice getClientDevice(String deviceName, String clientMnemonic, String millenniumEnvironment) throws DAOException;
   
   /**
    * Retrieve a client device in the environment.
    * 
    * @param deviceName
    * @param clientMnemonic
    * @param logicalDomainId
    * @param millenniumEnvironment
    * @return a client device
    * @throws DAOException
    */
   public ClientDevice getClientDevice(String deviceName, String clientMnemonic, Long logicalDomainId, String millenniumEnvironment) throws DAOException;

   /**
    * Retrieves all client devices in the environment.
    * 
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @return an array of client devices
    * @throws DAOException
    */
   public ClientDevice[] getClientDevices(String clientMnemonic, String millenniumEnvironment)
      throws DAOException;

   /**
    * Retrieves the keys that represent the client devices
    * 
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @return the client device keys
    * @throws DAOException
    */
   public ClientDeviceKey[] getClientDeviceKeys(String clientMnemonic, String millenniumEnvironment) throws DAOException;

   /**
    * Retrieves a wrapper for a limited number of client devices.
    * 
    * @param filter
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @return the wrapped client devices
    * @throws DAOException
    */
   public BoundedClientDevice getBoundedClientDevices(ClientDeviceFilter filter, String clientMnemonic,
      String millenniumEnvironment) throws DAOException;

   /**
    * Adds a device to the database.
    * 
    * @param device The device to add
    * @param clientMnemonic
    * @param millenniumEnvironment
    * 
    * @throws DAOException
    */
   public void addClientDevice(ClientDevice device, String clientMnemonic, String millenniumEnvironment)
      throws DAOException;

   /**
    * Updates a device in the database.
    * 
    * @param device The device to update
    * @param clientMnemonic the client mnemonic of the device
    * 
    * @throws DAOException
    */
   public void updateClientDevice(ClientDevice device, String clientMnemonic) throws DAOException;

   /**
    * Deletes a device from the database.
    * 
    * @param device The client device to delete
    * 
    * @throws DAOException
    */
   public void deleteClientDevice(ClientDevice device) throws DAOException;

   /**
    * Retrieves the column names from the client device table
    * 
    * @return the names of the columns
    * @throws DAOException
    */
   public String[] getClientDeviceTableColumns() throws DAOException;
   
   /**
    * This method returns a map of WTS database columns to their corresponding Registry Keys.
    * 
    * @return Map where key is the database column and value registry entry.
    * @throws DAOException
    */
   public Map<String,String> getColumnRegistryMapping() throws DAOException;

   /**
    * This method checks to see if The Column Registry mapping table exists
    * @return true if the table exists otherwise false;
    * @throws ManagementException 
    */
   public boolean columnRegistryTableExists() throws ManagementException;
   
   /**
    * Adds multiple devices in one query
    * 
    * @param devices The devices to add
    * @param clientMnemonic the mnemonic of the devices
    * @param millenniumEnvironment the millennium environment in which to add the devices
    * @throws DAOException
    */
   public void batchAddClientDevices(ClientDevice[] devices, String clientMnemonic, String millenniumEnvironment) throws DAOException;
   
   /**
    * Updates multiple devices in one query 
    * 
    * @param devices The devices to update
    * @param clientMnemonic the mnemonic of the devices
    * @return the success or failure of each device update
    * @throws DAOException
    */
   public int[] batchUpdateClientDevices(ClientDevice[] devices, String clientMnemonic) throws DAOException;
   
   /**
    * Updates the logical domain for a device
    * @param deviceName The device to update
    * @param clientMnemonic The client mnemonic for the device being updated
    * @param domain The domain for the device being updated
    * @param oldLogicalDomain The old logical domain of the device
    * @param newLogicalDomain The new logical domain of the device
    * @throws DAOException
    */
   public void updateLogicalDomain(String deviceName, String clientMnemonic, String domain, String oldLogicalDomain, String newLogicalDomain) throws DAOException;
   
   /**
    * Retrieves all the client devices that match a client mnemonic and Millennium environment (domain) that are between two offsets. Used to paginate the results.
    * 
    * @param clientMnemonic
    * @param millenniumEnvironment
    * @param startOffset
    * @param endOffset
    * @return The devices that match the mnemonic and envrionment within the offsets
    * @throws DAOException 
    */
   public ClientDevice[] getClientDevices(String clientMnemonic, String millenniumEnvironment, int startOffset, int endOffset) throws DAOException;
}
