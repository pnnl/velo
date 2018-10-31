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
package gov.pnnl.cat.web.app.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 */
public class CatAuthenticationHelper {
  

  private static final String AUTHENTICATION_SERVICE = "AuthenticationService";
  private static final String UNPROTECTED_AUTH_SERVICE = "authenticationService";
  
  /**
   * CAT method used to authenticate CAT servlets without using a write tx to set home folder ID (which CAT doesn't need because all users will already have a home folder)
   * @param context
   * @param httpRequest
   * @param httpResponse
   * @param ticket
  
  
   * @return AuthenticationStatus
   * @throws IOException */
  public static AuthenticationStatus authenticateExternalTicket(ServletContext context, HttpServletRequest httpRequest, HttpServletResponse httpResponse, String ticket) throws IOException {
   // setup the authentication context
   WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
   AuthenticationService auth = (AuthenticationService) wc.getBean(AUTHENTICATION_SERVICE);
   try {
     auth.validate(ticket);
   } catch (AuthenticationException authErr) {
     return AuthenticationStatus.Failure;
   } catch (Throwable e) {
     // Some other kind of serious failure
     AuthenticationService unprotAuthService = (AuthenticationService) wc.getBean(UNPROTECTED_AUTH_SERVICE);
     unprotAuthService.invalidateTicket(unprotAuthService.getCurrentTicket());
     unprotAuthService.clearCurrentSecurityContext();
     return AuthenticationStatus.Failure;
   }

   return AuthenticationStatus.Success;
 }
}
