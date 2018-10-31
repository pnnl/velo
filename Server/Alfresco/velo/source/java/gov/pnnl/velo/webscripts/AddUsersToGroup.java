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

import java.io.File;

import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


public class AddUsersToGroup extends AbstractVeloWebScript {
	
  public static final String PARAM_USER_NAMES = "userNames";  
	 public static final String PARAM_GROUP = "groupName"; 

	@Override
	protected Object executeImpl(WebScriptRequest req, WebScriptResponse res,
			File requestContent) throws Exception {
		 // Get the request parameters:
	    String userNameStr = req.getParameter(PARAM_USER_NAMES);
	    String groupName = req.getParameter(PARAM_GROUP);
	    String[] userNames = userNameStr.split(",");
    
      // switch to admin privs
      //AuthenticationUtil.setRunAsUserSystem();
	    
      for(int i=0;i<userNames.length;i++ ){
      String userAuthority = this.authorityService.getName(AuthorityType.USER, userNames[i]);
      this.authorityService.addAuthority(groupName, userAuthority);  
      }
		return null;
	}
}

