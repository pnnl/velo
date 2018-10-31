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

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Called by VELO wiki to create a new user account in Alfresco.
 * Need to have admin authority in Alfresco in order to create a user.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CreateUser extends AbstractVeloWebScript {
  public static final String PARAM_USER_NAME = "userName";  
  public static final String PARAM_PASSWORD = "password";  
  public static final String PARAM_DISPLAY_NAME = "displayName";  
  public static final String PARAM_EMAIL = "email";  
  public static final String PARAM_GROUP = "groupName";
  
  private PersonService personService;
  private MutableAuthenticationService mutableAuthenticationService; 
  
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
    String email = req.getParameter(PARAM_EMAIL);
    String displayName = req.getParameter(PARAM_DISPLAY_NAME);
    String groupName = req.getParameter(PARAM_GROUP);
    
    // make sure user has admin authority
    if(!authorityService.hasAdminAuthority()) {
      throw new AccessDeniedException("Only admins can create a new user account.");
    }

    // Make sure the user doesn't already exist, if so, just return
    if(mutableAuthenticationService.authenticationExists(userName) == false) {
      WikiUtils.createUserAccount(userName, password, email, displayName, mutableAuthenticationService, personService);
      if (groupName != null) {  
        String userAuthority = this.authorityService.getName(AuthorityType.USER, userName);
 	this.authorityService.addAuthority(groupName, userAuthority);
      }
   
    } else {
      // TODO: update user properties
      
    }
    return null;
  }

  /**
   * Method setPersonService.
   * @param personService PersonService
   */
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  /**
   * Method setMutableAuthenticationService.
   * @param mutableAuthenticationService MutableAuthenticationService
   */
  public void setMutableAuthenticationService(MutableAuthenticationService mutableAuthenticationService) {
    this.mutableAuthenticationService = mutableAuthenticationService;
  }
  
  
}
