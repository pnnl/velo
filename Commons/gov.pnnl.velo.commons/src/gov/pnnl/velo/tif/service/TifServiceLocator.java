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

import gov.pnnl.velo.tif.model.JobHandler;


/**
 * Instead of having static methods everywhere,
 * use ServiceLocator to find the
 * services provided by the Velo TIF framework.
 * Services provided by spring dependency injection,
 * so deployments can override any class they want to.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
// TODO: get rid of this class and use direct dependency injection into the services
public class TifServiceLocator {

  // TODO: for now we are not using CmsService - may resurrect it later
  //private static CmsService cmsService;
  private static CodeRegistry codeRegistry;
  private static MachineRegistry machineRegistry;
  private static ScriptRegistry scriptRegistry;
  private static JobLaunchService jobLaunchingService;
  private static JobConfigService jobConfigService;
  private static VeloWorkspace veloWorkspace;
  private static CredentialsPrompter credentialsPrompter;
  private static JobHandler defaultJobHandler;
  private static CmsService cmsService;
  
  public static CmsService getCmsService() {
    return cmsService;
  }

  public void setCmsService(CmsService cmsService) {
    TifServiceLocator.cmsService = cmsService;
  }

  /**
   * @return the codeRegistry
   */
  public static CodeRegistry getCodeRegistry() {
    return codeRegistry;
  }
  
  /**
   * @param codeRegistry the codeRegistry to set
   * set via spring injection
   */
  public void setCodeRegistry(CodeRegistry codeRegistry) {
    TifServiceLocator.codeRegistry = codeRegistry;
  }
  
  /**
   * @return the machineRegistry
   */
  public static MachineRegistry getMachineRegistry() {
    return machineRegistry;
  }
  
  /**
   * @param machineRegistry the machineRegistry to set
   * set via spring injection
   */
  public void setMachineRegistry(MachineRegistry machineRegistry) {
    TifServiceLocator.machineRegistry = machineRegistry;
  }
  
  /**
   * @return the scriptRegistry
   */
  public static ScriptRegistry getScriptRegistry() {
    return scriptRegistry;
  }
  
  /**
   * @param scriptRegistry the scriptRegistry to set
   * set via spring injection
   */
  public void setScriptRegistry(ScriptRegistry scriptRegistry) {
    TifServiceLocator.scriptRegistry = scriptRegistry;
  }
  
  /**
   * @return the jobLaunchingService
   */
  public static JobLaunchService getJobLaunchingService() {
    return jobLaunchingService;
  }
  
  /**
   * @param jobLaunchingService the jobLaunchingService to set
   * set via spring injection
   */
  public void setJobLaunchingService(JobLaunchService jobLaunchingService) {
    TifServiceLocator.jobLaunchingService = jobLaunchingService;
  }
  
  /**
   * @return the jobConfigService
   */
  public static JobConfigService getJobConfigService() {
    return jobConfigService;
  }
  
  /**
   * @param jobConfigService the jobConfigService to set
   * set via spring injection
   */
  public void setJobConfigService(JobConfigService jobConfigService) {
    TifServiceLocator.jobConfigService = jobConfigService;
  }
  
  /**
   * @return the veloWorkspace
   */
  public static VeloWorkspace getVeloWorkspace() {
    return veloWorkspace;
  }
  
  /**
   * @param veloWorkspace the veloWorkspace to set
   * set via spring injection
   */
  public void setVeloWorkspace(VeloWorkspace veloWorkspace) {
    TifServiceLocator.veloWorkspace = veloWorkspace;
  }
  
  /**
   * @return
   */
  public static CredentialsPrompter getCredentialsPrompter() {
    return credentialsPrompter;
  }

  /**
   * @param passwordDialogProvider
   */
  public void setCredentialsPrompter(CredentialsPrompter credentialsPrompter) {
    TifServiceLocator.credentialsPrompter = credentialsPrompter;
  }

  /**
   * @return the defaultJobHandler
   */
  public static JobHandler getDefaultJobHandler() {
    return defaultJobHandler;
  }

  /**
   * This is the default job handler class that will be used if no code-specific
   * job handler is set.
   * @param defaultJobHandler the defaultJobHandler to set
   */
  public void setDefaultJobHandler(JobHandler defaultJobHandler) {
    TifServiceLocator.defaultJobHandler = defaultJobHandler;
  }

  
}
