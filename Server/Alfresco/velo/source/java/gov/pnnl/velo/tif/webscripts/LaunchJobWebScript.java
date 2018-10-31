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

import java.io.File;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.ssh.AuthFailedException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.pnnl.cat.util.XmlUtility;
import gov.pnnl.velo.tif.model.JobLaunchParameters;
import gov.pnnl.velo.tif.model.KeyboardInteractiveCredentials;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.tif.service.impl.JobLaunchServiceVelo;
import gov.pnnl.velo.webscripts.AbstractVeloWebScript;

/**
 */
public class LaunchJobWebScript extends AbstractVeloWebScript{

  protected JobLaunchServiceVelo jobLaunchingService;
  protected Log logger = LogFactory.getLog(this.getClass());
  protected HashMap<String, String> userAtHostToPassword = new HashMap<String, String>();
  protected HashMap<String, String> userAtHostToAlfUser = new HashMap<String, String>();
  
  
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
    JobLaunchParameters launchObject = XmlUtility.deserializeFile(requestContent);
    String remoteUserName = launchObject.getJobConfig().getUserName();
    String machine = launchObject.getJobConfig().getMachine().getFullDomainName();
    String alfUsername = authenticationComponent.getCurrentUserName();
    String cachedPassword = userAtHostToPassword.get(remoteUserName + "@" + machine);
    String cachedAlfUser = userAtHostToAlfUser.get(remoteUserName + "@" + machine);

    boolean usedCachedCreds = false;
    try {
      // if a password isn't passed in AND if we have a cached password, use the cached one
      if ((launchObject.getCredentials() == null || launchObject.getCredentials().getCredential() == null) && 
          cachedPassword != null && cachedAlfUser != null && cachedAlfUser.equalsIgnoreCase(alfUsername)) {
        KeyboardInteractiveCredentials creds = new KeyboardInteractiveCredentials();
        creds.setUsername(remoteUserName);
        creds.setCredential(cachedPassword);
        launchObject.setCredentials(creds);
        usedCachedCreds = true;
      }

      String jobID = jobLaunchingService.launchJob(launchObject.getJobConfig(), launchObject.getCredentials());
      writeMessage(res, "Job ID:" + jobID);

      // it should be safe to assume launchObject.getCredentials() won't be null (it is null the first time launch job is called)
      // if we get this far becuase that means we successfully authenticated, but checking just in case so as not to fail the whole job launching call
      if (!usedCachedCreds && launchObject.getCredentials() != null) {
        // now cache that this alf user was sucessfully authentication as this remote user
        // and also cache this remote user's password
        userAtHostToPassword.put(remoteUserName + "@" + machine, ((KeyboardInteractiveCredentials) launchObject.getCredentials()).getCredential());
        userAtHostToAlfUser.put(remoteUserName + "@" + machine, alfUsername);
      }
    } catch (AuthFailedException e) {

      if (usedCachedCreds) {
        // wipe out this password from cache - using cached password didn't work (might have been changed)
        userAtHostToPassword.remove(remoteUserName + "@" + machine);
      }

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
