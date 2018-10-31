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
package gov.pnnl.cat.alerting.detection.internal.rss;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.detection.internal.AbstractEventProcessor;
import gov.pnnl.cat.alerting.detection.internal.search.SearchEventProcessor;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.util.NodeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 */
public class RSSEventProcessor extends AbstractEventProcessor {

	private Set<Subscription> rssSubscriptions = new HashSet<Subscription>();
	private NodeUtils nodeUtils;
	private Actor sender;

	private static Log logger = LogFactory.getLog(SearchEventProcessor.class); 

	/**
	 * Method setSender.
	 * @param sender Actor
	 */
	public void setSender(Actor sender) {
		this.sender = sender;
	}

	/**
	 * Method setNodeUtils.
	 * @param nodeUtils NodeUtils
	 */
	public void setNodeUtils(NodeUtils nodeUtils) {
		this.nodeUtils = nodeUtils;
	}


	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionListener#subscriptionChanged(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public void subscriptionChanged(NodeRef subscription) {
		// remove and re-add subscription.  re-adding will check the type and process accordingly
		if (nodeService.exists(subscription)) {
	    Subscription subscriptionObj = subscriptionService.getSubscription(subscription);
			subscriptionDeleted(subscriptionObj);
			subscriptionCreated(subscription);
		}
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionListener#subscriptionCreated(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public void subscriptionCreated(NodeRef subscription) {
		Subscription subscriptionObj = subscriptionService.getSubscription(subscription);
		if (!isHandledSubscriptionType(subscriptionObj.getType())) {
			// this subscription type is handled by someone else
			return; 
		}
		// add to the list of subscriptions to process
		synchronized(rssSubscriptions) {
			rssSubscriptions.add(subscriptionObj);
		}

	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionListener#subscriptionDeleted(org.alfresco.service.cmr.repository.NodeRef)
	 */
	/**
	 * Method subscriptionDeleted.
	 * @param subscription Subscription
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionListener#subscriptionDeleted(Subscription)
	 */
	public void subscriptionDeleted(Subscription subscription) {
		// remove from the list of subscriptions to process
		synchronized(rssSubscriptions) {
			rssSubscriptions.remove(subscription);
		}
	}  

	public void performHourlyRSS() {
		// wrap in a transaction
		RetryingTransactionCallback cb = new RetryingTransactionCallback() {

			public Object execute() throws Throwable {
				performRSSProcessing(Frequency.HOURLY);
				return null;
			}
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);

	}

	public void performDailyRSS() {
		// wrap in a transaction
		RetryingTransactionCallback cb = new RetryingTransactionCallback() {

			public Object execute() throws Throwable {
				performRSSProcessing(Frequency.DAILY);
				return null;
			}
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);

	}

	/**
	 * For each subscription, digests temporary alerts into one alert. 
	 * For each digested alert, creates the alert object and sends it to all
	 * the delivery channels registered for that subscription.
	 * 
	
	 */  
	public void performWeeklyRSS() {
		// wrap in a transaction
		RetryingTransactionCallback cb = new RetryingTransactionCallback() {

			public Object execute() throws Throwable {
				performRSSProcessing(Frequency.WEEKLY);
				return null;
			}
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);
	}
	/**
	 * Method performRSSProcessing.
	 * @param frequency Frequency
	 */
	private void performRSSProcessing(final Frequency frequency) {
		// iterate through all of the subscriptions
		// if the frequency matches the provided frequency
		// get the search string from the subscription
		// add a time-based criteria - get all changes since last time we searched
		// TODO: what time do we use?
		// perform the search
		// if results, construct an alert
		// call logic to deliver the alert
		// save last performed search time?
		List<Subscription> subscriptions;
		synchronized(rssSubscriptions) {
			subscriptions = new ArrayList<Subscription>(rssSubscriptions);
		}
		for (Subscription subscription : subscriptions) {
			if (isHandledSubscriptionType(subscription.getType()) && 
					subscription.getFrequency().equals(frequency)) {
				performRSSProcessing(subscription, frequency);
			}
		}
	}

	/**
	 * Method performRSSProcessing.
	 * @param subscription Subscription
	 * @param frequency Frequency
	 */
	private void performRSSProcessing(Subscription subscription, Frequency frequency) {
		// this is a subscription we care about
		// get the url, last search date
		// ping the feed
		// look at entries, parse through them
		// use util method to convert SyndEntry to our Entry

		String url = (String)subscription.getParameters().get(AlertingConstants.PROP_SUB_RSS_URL);
		Date date = (Date)subscription.getParameters().get(AlertingConstants.PROP_SUB_LAST_ALERT_SENT);

		List<SyndEntry> rssEntries;
		try {
			rssEntries = RomeRssUtils.getRSSEntriesAfterDate(url, date);
		} catch (Exception e) {
			logger.error("Exception fetching from RSS", e);
			rssEntries = null;
		}
		if (rssEntries != null && rssEntries.size() > 0) {
			List<Event> events = RomeRssUtils.convertSyndEntriesToEvents(alertManagementService, rssEntries);

			try {
				super.createAlert(subscription, events, sender);
				
			} catch (DeliveryException de) {
				logger.error("Exception creating alert", de);
			}
		}
	}


}
