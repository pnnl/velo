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
/**
 * 
 */
package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.XmlUtility;
import gov.pnnl.velo.model.Email;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Send an email with optional attachments.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class SendMail extends AbstractCatWebScript {

  /**
   * The spring java mail sender
   */
  private JavaMailSender javaMailSender;


  /**
   * Method setJavaMailSender.
   * @param javaMailSender JavaMailSender
   */
  public void setJavaMailSender(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    
    Email email = XmlUtility.deserializeFile(requestContent);

    sendEmailMessage(email);

    return null;

  }
  
  public void sendEmailMessage(Email email) throws Exception {
    // Create a default MimeMessage object.
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();

    // Set From: header field of the header.
    mimeMessage.setFrom(new InternetAddress(email.getFrom()));

    // Set To: header field of the header.
    mimeMessage.addRecipient(Message.RecipientType.TO,
        new InternetAddress(email.getTo()));

    // Set Subject: header field
    mimeMessage.setSubject(email.getSubject());

    // Set sent date
    mimeMessage.setSentDate(new Date());

    // create message body
    MimeBodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setText(email.getMessage());

    // creates multi-part
    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messageBodyPart);

    // adds attachments
    for (File aFile : email.getAttachments()) {
      MimeBodyPart attachPart = new MimeBodyPart();

      try {
        attachPart.attachFile(aFile);

      } catch (IOException e) {
        e.printStackTrace();
      }

      multipart.addBodyPart(attachPart);
    }


    // sets the multi-part as e-mail's content
    mimeMessage.setContent(multipart);


    // Send message
    javaMailSender.send(mimeMessage);   
  }

}
