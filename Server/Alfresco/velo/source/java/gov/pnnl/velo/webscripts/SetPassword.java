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
package gov.pnnl.velo.webscripts;


import gov.pnnl.velo.util.WikiUtils;

import java.io.File;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * TODO: this is not secure - username/password need to be passed in the request body
 * instead of as a URL parameter so they can be encrypted.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class SetPassword extends AbstractVeloWebScript {
  public static final String PARAM_USER_NAME = "userName";  
  public static final String PARAM_PASSWORD = "password";  
  
  private MutableAuthenticationService mutableAuthenticationService; 
  private PersonService personService;
  
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

    // Get the request parameters:
    String userName = req.getParameter(PARAM_USER_NAME);
    String password = req.getParameter(PARAM_PASSWORD);
    
    if(!authorityService.hasAdminAuthority()) {
      // Allow user to change his own password
      String currentUser = authenticationComponent.getCurrentUserName();
      
      if(currentUser.equalsIgnoreCase(userName)) {
        // run as admin user
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
      } else {
        throw new AccessDeniedException("Access denied. " + currentUser +
            " does not have the authority to change " + userName + "'s password.");
      }
    }
    
    if(mutableAuthenticationService.authenticationExists(userName)) {
      // If user already exists, just reset his password
      logger.debug("User " + userName + " resetting password");
      this.mutableAuthenticationService.setAuthentication(userName, password.toCharArray());

    } else {
      // otherwise, create a new user
      WikiUtils.createUserAccount(userName, password, null, null, mutableAuthenticationService, personService);
    }
    
    return null;
  }

  /**
   * Method setMutableAuthenticationService.
   * @param mutableAuthenticationService MutableAuthenticationService
   */
  public void setMutableAuthenticationService(MutableAuthenticationService mutableAuthenticationService) {
    this.mutableAuthenticationService = mutableAuthenticationService;
  }

  /**
   * Method setPersonService.
   * @param personService PersonService
   */
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }
  
  
}
