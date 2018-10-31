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
package gov.pnnl.cat.alerting.delivery.internal;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;

/**
 * This class sends Alerts via email.  Some configuration parameters
 * regarding the email server need to be added to this bean's 
 * spring config file.
 * @version $Revision: 1.0 $
 */
public class EmailDeliveryChannel extends AbstractDeliveryChannel {


	private static Log logger = LogFactory.getLog(EmailDeliveryChannel.class); 
	

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.delivery.DeliveryChannel#getName()
	 */
	public QName getName() {
		return AlertingConstants.CHANNEL_EMAIL;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.delivery.DeliveryChannel#getTitle()
	 */
	public String getTitle() {
		return "Email";
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.delivery.DeliveryChannel#send(gov.pnl.dmi.alerting.alerts.Alert)
	 */
	public void send(Alert alert) throws DeliveryException {
		for (Actor recipient : alert.getRecipients()) {
			String recipientUsername = recipient.getAccountId();
			String recipientEmail = emailUtils.getEmailForUsername(recipientUsername);
			

			// TODO: replace with a call to convert the alert into a display body
		//	final String messageBody = "This is the alert";
			//TODO test this now that we're using TransformationOptions instead of a hashmap
			TransformationOptions options = new TransformationOptions();
			options.toMap().put(AlertingConstants.TRANSFORM_OPTION_ALERT, alert);
			String targetMimetype = AlertingConstants.MIME_TYPE_DELIVERY_EMAIL;
			String sourceMimetype = subscriptionService.getSubscriptionType(alert.getSubscriptionType()).getMimetype();
			ContentTransformer transformer = contentTransformerRegistry.getTransformer(sourceMimetype, targetMimetype, options);
			
			
			StringContentReader reader = new StringContentReader(alert);
			reader.setMimetype(sourceMimetype);
			StringContentWriter writer = new StringContentWriter();
			writer.setMimetype(targetMimetype);
			transformer.transform(reader, writer, options);
			
			String eventContent = writer.getContent();
			
			StringBuffer bodyBuffer = new StringBuffer();
			bodyBuffer.append("Alert " + alert.getTitle() + EmailUtils.LINEFEED);
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String alertDateString = format.format(new Date(System.currentTimeMillis()));
			bodyBuffer.append("Date Generated: " + alertDateString + EmailUtils.LINEFEED + EmailUtils.LINEFEED);
			
			bodyBuffer.append(eventContent);
			bodyBuffer.append(EmailUtils.LINEFEED).append(EmailUtils.LINEFEED);
			bodyBuffer.append("-------------------------------------------------------------").append(EmailUtils.LINEFEED).append(EmailUtils.LINEFEED);
			bodyBuffer.append("This message was automatically generated.  Do not reply to this message").append(EmailUtils.LINEFEED);
			
			

			
			final String messageBody = bodyBuffer.toString();
			
			final String subject = alert.getTitle();

			try{
				emailUtils.sendEmail(recipientEmail, subject, messageBody);
			}
			catch (MailException ex) {
				logger.error("Email Send Failed", ex);
				throw new DeliveryException(ex);
			}
		}
	}

}
