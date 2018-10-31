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

import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.delivery.DeliveryChannel;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;

import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.service.cmr.repository.ContentReader;

/**
 */
public abstract class AbstractDeliveryChannel implements DeliveryChannel {

	protected EmailUtils emailUtils;
	protected SubscriptionService subscriptionService;
	protected ContentTransformerRegistry contentTransformerRegistry;

	
	/**
	 * Method setContentTransformerRegistry.
	 * @param contentTransformerRegistry ContentTransformerRegistry
	 */
	public void setContentTransformerRegistry(
			ContentTransformerRegistry contentTransformerRegistry) {
		this.contentTransformerRegistry = contentTransformerRegistry;
	}

	/**
	 * Method setEmailUtils.
	 * @param emailUtils EmailUtils
	 */
	public void setEmailUtils(EmailUtils emailUtils) {
		this.emailUtils = emailUtils;
	}

	/**
	 * Method setSubscriptionService.
	 * @param subscriptionService SubscriptionService
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	/**
	 * Method makeContentReaderFromAlert.
	 * @param alert Alert
	 * @return ContentReader
	 */
	protected ContentReader makeContentReaderFromAlert(Alert alert) {
		
		return null;
	}

}
