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


import gov.pnnl.cat.web.scripts.SendMail;
import gov.pnnl.velo.model.Email;

import java.io.File;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


public class EmailUsername extends AbstractVeloWebScript {
  public static final String PARAM_USERS_EMAIL = "usersEmail";  
  public static final String PARAM_SITENAME = "sitename";  
  public static final String PARAM_FROM_EMAIL = "fromEmail";  
  private SendMail sendMailWebscript;
  
  //should these properties instead be passed in from the client?  or set as properties on the server?
  //going with server for now since (for example akuna, akuna-dev) there could be one client used for multiple server instances 
  private String fromEmailAddress;
  private String subjectLine;
  private String signature;
  
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
    //run as system since the user/request can't be authenticated as they don't know their username
    
    AuthenticationUtil.setRunAsUserSystem();
    String usersEmail = req.getParameter(PARAM_USERS_EMAIL);
    String siteName = req.getParameter(PARAM_SITENAME);
    String fromEmailParam = req.getParameter(PARAM_FROM_EMAIL);
    
    //let UI override server props for case of IIC and PP where they both share 
    //same server but need to have different 'from' email addresses...
    if(fromEmailParam != null){
      fromEmailAddress = fromEmailParam;
    }
    
    
    //look up the username from the email, then send an email to that address with the user's username 
    Set<NodeRef> matches = personService.getPeopleFilteredByProperty(ContentModel.PROP_EMAIL, usersEmail, 1);
    if(matches.isEmpty()){
      throw new RuntimeException("No users found matching email address " + usersEmail);
    }
    NodeRef person = matches.iterator().next();
    
    Email email = new Email();
    email.setFrom(fromEmailAddress);
    email.setSubject(subjectLine);
    email.setTo(usersEmail);
    
    String fname = (String)nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
    String lname = (String)nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
    
    StringBuilder message = new StringBuilder(); 
    if(fname != null && !fname.isEmpty() && lname != null && !lname.isEmpty()){
      message.append("Dear ");
      message.append(fname + " " + lname);
      message.append(",\n\n");
    }else{
      message.append("Salutations\n\n");
    }
    message.append("Your username for "+siteName+" is:\n");
    message.append((String)nodeService.getProperty(person, ContentModel.PROP_USERNAME));
    
    message.append("\n\n-----------------------------------");
    message.append("\n"+signature);

    email.setMessage(message.toString());
    
    sendMailWebscript.sendEmailMessage(email);
    return null;
  }



  public void setSendMailWebscript(SendMail sendMailWebscript) {
    this.sendMailWebscript = sendMailWebscript;
  }

  public void setFromEmailAddress(String fromEmailAddress) {
    this.fromEmailAddress = fromEmailAddress;
  }

  public void setSubjectLine(String subjectLine) {
    this.subjectLine = subjectLine;
  }


  public void setSignature(String signature) {
    this.signature = signature;
  }

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }
  
}
