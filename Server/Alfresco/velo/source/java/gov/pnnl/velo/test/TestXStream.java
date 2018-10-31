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
package gov.pnnl.velo.test;


import gov.pnnl.cat.util.XmlUtility;
import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.JobConfigService;
import gov.pnnl.velo.tif.util.RegistryUtils;

/**
 */
public class TestXStream {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    String jobDirectory = "/lustre/atlas/proj-shared/csc121/bmayer";
    boolean useQueue = false;
    String remoteUsername = "zoe";
    String cmsContextPath = "/company_home/User Documents/zoeguillen";
    String cmsUsername = "zoeguillen";
    Machine titan = new Machine("titan.ccs.ornl.gov");
    titan.setName("titan");
    Code workflow = new Code();
    workflow.setId("workflow");

    JobConfig launchConfig = new JobConfig(JobConfigService.CONFIG_NAME_SAVED);
    Machine machine = RegistryUtils.getMachine("titan");
    launchConfig.setMachine(machine);
    launchConfig.setCode(RegistryUtils.getCodeForMachine(machine, "workflow", null));
    launchConfig.setRemoteDir(jobDirectory);
    launchConfig.setUserName(remoteUsername);
    launchConfig.setContextPath(cmsContextPath);

    try {
      String xml = XmlUtility.serialize(launchConfig);
      System.out.println(xml);

      JobConfig deserialized = XmlUtility.deserialize(xml);
      
      System.out.println("Remote user name: " + deserialized.getUserName());

    } catch (Throwable e) {
      e.printStackTrace();
    }


  }

}
