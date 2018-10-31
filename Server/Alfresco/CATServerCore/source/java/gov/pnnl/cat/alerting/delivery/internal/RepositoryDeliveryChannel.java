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

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.RepositoryAlert;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;

/**
 * This class saves the alert persistently to the repository using the 
 * AlertManagementService.
 *
 * @version $Revision: 1.0 $
 */
public class RepositoryDeliveryChannel extends AbstractDeliveryChannel {

	private AlertManagementService alertService;
	private ContentService contentService;
	
	private static Log logger = LogFactory.getLog(RepositoryDeliveryChannel.class); 

	/**
	 * Needs to be injected by spring
	 * @param alertService the alertService to set
	 */
	public void setAlertService(AlertManagementService alertService) {
		this.alertService = alertService;
	}

	/**
	 * Method setContentService.
	 * @param contentService ContentService
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}



	/**
	 * Method setEmailUtils.
	 * @param emailUtils EmailUtils
	 */
	public void setEmailUtils(EmailUtils emailUtils) {
		this.emailUtils = emailUtils;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.delivery.DeliveryChannel#getName()
	 */
	public QName getName() {
		return AlertingConstants.CHANNEL_REPOSITORY;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.delivery.DeliveryChannel#getTitle()
	 */
	public String getTitle() {
		return "Repository and RSS";
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.delivery.DeliveryChannel#send(gov.pnl.dmi.alerting.alerts.Alert)
	 */
	public void send(Alert alert) throws DeliveryException {
		// see if the recipient has exceeded the max number of alerts
		// TODO: Iterate through recipient list and check for all recipients
		String username = alert.getRecipients().get(0).getAccountId();
		if (alertService.areAlertsMaxed(username)) {
			sendWarningEmail(username);
		}


		RepositoryAlert repAlert = alertService.createAlert(alert);
    TransformationOptions options = new TransformationOptions();
		options.toMap().put(AlertingConstants.TRANSFORM_OPTION_ALERT, alert);
		String targetMimetype = AlertingConstants.MIME_TYPE_DELIVERY_REPOSITORY;
		String sourceMimetype = subscriptionService.getSubscriptionType(alert.getSubscriptionType()).getMimetype();
		ContentTransformer transformer = contentTransformerRegistry.getTransformer(sourceMimetype, targetMimetype, options);

		StringContentReader reader = new StringContentReader(alert);
		reader.setMimetype(sourceMimetype);
		ContentWriter writer = contentService.getWriter(repAlert.getNodeRef(), AlertingConstants.PROP_ALERT_BKMS_DISPLAY, true);
		writer.setMimetype(targetMimetype);

		transformer.transform(reader, writer, options);
	}
	
	/**
	 * Method sendWarningEmail.
	 * @param username String
	 */
	private void sendWarningEmail(String username) {
		String emailAddress = null;
		try {
			emailAddress = emailUtils.getEmailForUsername(username);
		} catch (DeliveryException de) {
			logger.warn("Exception sending max alerts email to " + username, de);
			return;
		}
		// we have a valid email address
		try {
			String subject = "WARNING: Too many alerts";
			String body = "Warning: You have too many alerts stored in your BKMS repository." +
			"  A maximum of " + alertService.getMaxAlerts() + " is permitted.";
			emailUtils.sendEmail(emailAddress, subject, body);
		} catch (MailException me) {
			logger.warn("Exception sending max alerts email to " + emailAddress, me);
		}

	}

}
