/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.velo.tif.service;

import gov.pnnl.velo.tif.model.Machine;

import java.util.Collection;
import java.util.Properties;

/**
 */
public interface MachineRegistry {

  /**
   * Method to initialize the registry by loading entries from appropriate source.
   */
  public void init();
  
  /**
   * wipe clean and reload machines
   */
  public void reload();
  
  /**
   * Registry providers add machine files to be registered on init()
   * @param provider
   */
  public void addRegistryProvider(RegistryConfigFileProvider provider);
  
  /**
   * Returns list of registered machines sorted alphabetically.
   * @return List<String>
   */
  public Collection<String> getMachineIDs();
  
  /**
   * Get machine of the give id (name - eg., hopper)
   * @param machineID
   * @return
   */
  public Machine get(String machineID);
  
  public Machine getMachineByFullyQualifiedDomainName(String fullyQualifiedDomainName);
  
  /**
   * For the current user, generate the remote user home directory for the given machine.
   * @param machineName
   * @return String
   */
  public String getRemoteUserHomeDirectory(String machineName);
  
  /**
   * Save global parameters for the current Velo user that are constant for the user and the given machine such
   * as login name and allocation account.
   * @param machineName
   * @param userMachinePrefs
   */
  public void saveUserMachinePrefs(String machineName, Properties userMachinePrefs);
  
  /**
   * For the current Velo user, get global parameters that are constant for the user and the given machine such
   * as login name and allocation account.
   * @param username
   * @param machineName
  
   * @return Properties
   */
  public Properties getUserMachinePrefs(String machineName);
  
}
