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

import gov.pnnl.velo.tif.model.Code;

import java.util.Collection;
import java.util.Map;

/**
 */
public interface CodeRegistry {
  
  // default version if a registered code does not specify a version
  public String VERSION_DEFAULT = "default";
  
  /**
   * Load provided config files and merge as appropriate
   */
  public void init();
  
  /**
   * wipe clean and reload configurations from all providers
   */
  public void reload();
  
  /**
   * Registry providers add code files to be registered on init()
   * @param provider
   */
  public void addRegistryProvider(RegistryConfigFileProvider provider);
  
  /**
   * Method getCodeIDs.
   * @return Collection<String>
   */
  public Collection<String> getCodeIDs();
  
  /**
   * Get all the versions of the given code id.
   * Version number is the key to the map.
   * @param toolID String
   * @return Tool
   */
  public Map<String, Code> get(String codeID);
  
  public Code get(String codeId, String version);
    
  /**
   * If this code ID has a local executable folder (i.e., a folder bundled inside
   * a plugin or a module) then get it dynamically so we can make
   * variable substitutions.
   * @param codeId
   * @return
   */
  public String getExeFolderPath(String codeId);
  
}
