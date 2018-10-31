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
package gov.pnnl.velo.tif.webscripts;

import gov.pnnl.cat.util.XmlUtility;
import gov.pnnl.velo.tif.model.JobLaunchParameters;
import gov.pnnl.velo.tif.service.impl.JobLaunchServiceVelo;
import gov.pnnl.velo.webscripts.AbstractVeloWebScript;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.ssh.AuthFailedException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 */
public class KillJobWebScript extends AbstractVeloWebScript{

  protected JobLaunchServiceVelo jobLaunchingService;
  protected Log logger = LogFactory.getLog(this.getClass());
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    try {
      JobLaunchParameters launchObject = XmlUtility.deserializeFile(requestContent);
      boolean status = jobLaunchingService.kill(launchObject.getJobConfig(), launchObject.getCredentials());
      writeMessage(res, "Job Killed: " + String.valueOf(status));

    } catch (AuthFailedException e) {
      // If authentication fails, write the exception to the response so we can prompt for correct credentials
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(res.getOutputStream(), e);
    }
    return null;
  }

  /**
   * Method setJobLaunchingService.
   * @param jobLaunchingService JobLaunchingService
   */
  public void setJobLaunchingService(JobLaunchServiceVelo jobLaunchingService) {
    this.jobLaunchingService = jobLaunchingService;
  }

}
