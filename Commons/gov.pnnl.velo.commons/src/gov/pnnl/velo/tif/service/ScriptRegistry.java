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

import gov.pnnl.velo.tif.model.JobConfig;

import java.io.File;

/**
 */
public interface ScriptRegistry {
  
  public static final String SCRIPT_NAME_FORK = "fork";
  public static final String SCRIPT_NAME_DEFAULT = "default";
  
  /**
   * Method to initialize the registry by loading entries from appropriate source.
   */
  public void init();
  
  /**
   * wipe clean and reload scripts
   */
  public void reload();
  
  /**
   * Registry providers add script files to be registered on init()
   * @param provider
   */
  public void addRegistryProvider(RegistryConfigFileProvider provider);

  /**
   * Get the appropriate job submit script for the given code and machine
   * @param codeId
   * @param codeVersion
   * @param MachineId
   * @return
   */
  public File getJobScriptTemplate(String codeId, String codeVersion, String MachineId);

  /**
   * Get the special fork script named fork.sh
   * @return File
   */
  public File getForkScript();
  
  /**
   * Given the provided script template, perform variable substitutions to create
   * the real job submit script input file.
   * @param jobScriptTemplate
   * @param jobScript
   */
  public void createJobScript(File jobScriptTemplate, File jobScript, JobConfig runConfig);

}
