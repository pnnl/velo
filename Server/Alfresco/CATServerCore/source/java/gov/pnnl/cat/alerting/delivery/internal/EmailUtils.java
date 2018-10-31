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
package gov.pnnl.cat.alerting.delivery.internal;

import gov.pnnl.cat.alerting.exceptions.DeliveryException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 */
public class EmailUtils {
	
	/**
	 * The java mail sender
	 */
	private JavaMailSender javaMailSender;  
	private PersonService personService;
	private NodeService nodeService;
	private String fromAddress;

	private static Log logger = LogFactory.getLog(EmailUtils.class); 
	
	public static char LINEFEED = '\n';


	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Method setPersonService.
	 * @param personService PersonService
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * Method setFromAddress.
	 * @param fromAddress String
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	/**
	 * Method setJavaMailSender.
	 * @param javaMailSender JavaMailSender
	 */
	public void setJavaMailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	/**
	 * Method sendEmail.
	 * @param sendto String
	 * @param subject String
	 * @param body String
	 * @throws MailException
	 */
	public void sendEmail(String sendto, String subject, String body) throws MailException {
		sendEmail(sendto, fromAddress, subject, body);
	}
	
	/**
	 * Method sendEmail.
	 * @param sendto String
	 * @param fromAddress String
	 * @param subject String
	 * @param body String
	 * @throws MailException
	 */
	public void sendEmail(final String sendto, final String fromAddress, final String subject, final String body) throws MailException {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				mimeMessage.setRecipient(Message.RecipientType.TO,
						new InternetAddress(sendto));
				mimeMessage.setFrom(new InternetAddress(fromAddress));
				mimeMessage.setText(body);
				mimeMessage.setSubject(subject);
			}
		};

		javaMailSender.send(preparator);
	}
	
	/**
	 * Method getEmailForUsername.
	 * @param username String
	 * @return String
	 * @throws DeliveryException
	 */
	public String getEmailForUsername(String username) throws DeliveryException {
		NodeRef personNode = personService.getPerson(username);
		if (personNode == null) {
			throw new DeliveryException("Unknown recipient: " + username);
		}
		String recipientEmail = (String)nodeService.getProperty(personNode, ContentModel.PROP_EMAIL);
		if (recipientEmail == null || recipientEmail.trim().equals("")) {
			throw new DeliveryException("No email address defined for " + username);
		}
		return recipientEmail;
	}


}
