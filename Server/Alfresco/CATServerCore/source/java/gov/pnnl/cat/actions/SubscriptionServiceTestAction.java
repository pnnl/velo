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
package gov.pnnl.cat.actions;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.RepositoryAlert;
import gov.pnnl.cat.alerting.detection.internal.rss.RSSEventProcessor;
import gov.pnnl.cat.alerting.detection.internal.search.SearchEventProcessor;
import gov.pnnl.cat.alerting.subscriptions.AlertDigester;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * When we clean up links for deleted content nodes, we have to perform
 * a search, and this takes too long to embed inside a transaction.
 * Therefore, we call this action so we can do the cleanup 
 * asynchronously.
 * @version $Revision: 1.0 $
 */
public class SubscriptionServiceTestAction extends ActionExecuterAbstractBase {

	private static final Log logger = LogFactory.getLog(DeleteLinksAction.class);
	private SubscriptionService subscriptionService;
	private AlertManagementService alertService;
	private AuthenticationComponent authenticationComponent;
	private AlertDigester alertDigester;
	private SearchEventProcessor searchEventProcessor;
	private RSSEventProcessor rssEventProcessor;

	// FYI - the NAME property is the bean name as registered in the Spring config files

	/**
	 * Method setSubscriptionService.
	 * @param subscriptionService SubscriptionService
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}
	
	

	/**
	 * Method setRssEventProcessor.
	 * @param rssEventProcessor RSSEventProcessor
	 */
	public void setRssEventProcessor(RSSEventProcessor rssEventProcessor) {
		this.rssEventProcessor = rssEventProcessor;
	}



	/**
	 * Method setAuthenticationComponent.
	 * @param authenticationComponent AuthenticationComponent
	 */
	public void setAuthenticationComponent(
			AuthenticationComponent authenticationComponent) {
		this.authenticationComponent = authenticationComponent;
	}



	/**
	 * Method setSearchEventProcessor.
	 * @param searchEventProcessor SearchEventProcessor
	 */
	public void setSearchEventProcessor(SearchEventProcessor searchEventProcessor) {
		this.searchEventProcessor = searchEventProcessor;
	}



	/**
	 * Method setAlertService.
	 * @param alertService AlertManagementService
	 */
	public void setAlertService(AlertManagementService alertService) {
		this.alertService = alertService;
	}



	/**
	 * Method setAlertDigester.
	 * @param alertDigester AlertDigester
	 */
	public void setAlertDigester(AlertDigester alertDigester) {
		this.alertDigester = alertDigester;
	}



	/**
	 * Define the parameters that can be passed into this action
	 * @param paramList List<ParameterDefinition>
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		if (logger.isDebugEnabled())
			logger.debug("setting parameter list");
		ParameterDefinitionImpl param = new ParameterDefinitionImpl(
				"action", 
				DataTypeDefinition.TEXT,
				true,
				"action");
		paramList.add(param);
	}

	/**
	 * Method executeImpl.
	 * @param ruleAction Action
	 * @param nodeActedUpon NodeRef
	 */
	@Override
	protected void executeImpl(final Action ruleAction, final NodeRef nodeActedUpon) {
		new Thread(new Runnable() {

			public void run() {
				try {
					AuthenticationUtil.setRunAsUserSystem();
					String action = (String)ruleAction.getParameterValue("action");
					if (action.equals("create")) {
						createRepositorySubscription(nodeActedUpon);
					} else if (action.equals("create-search")) {
						createSearchSubscription(nodeActedUpon);
					} else if (action.equals("create-rss")) {
						createRSSSubscription(nodeActedUpon);
						} else if (action.equals("update")) {
						updateSubscription(nodeActedUpon);
					} else if (action.equals("digest")) {
						runDigester();
					} else if (action.equals("runsearch")) {
						runDailySearch();
					} else if (action.equals("runrss")) {
						runHourlyRSS();
						} else if (action.equals("getAlerts")) {
						getAlerts();
					} else {
						removeSubscriptions();
					}
					
//					List<SubscriptionType> types = subscriptionService.getSubscriptionTypes();
//					List<RepositoryAlert> alerts = alertService.getAlerts("dave");
//					String text = alerts.get(alerts.size() - 1).getBkmsDisplay();
					System.out.println("");
				} catch (Exception e) {
					e.printStackTrace();
				}

				
			}}).start();
	}

	/**
	 * Method createRepositorySubscription.
	 * @param nodeActedUpon NodeRef
	 * @throws Exception
	 */
	private void createRepositorySubscription(NodeRef nodeActedUpon) throws Exception {
		SubscriptionType subscriptionType = subscriptionService.getSubscriptionType(AlertingConstants.TYPE_SUBSCRIPTION_REPOSITORY);
		
		Subscription sub = subscriptionService.newSubscriptionInstance(subscriptionType);
		
		String name = "Sub1 x" + System.currentTimeMillis() + "x";
		List<QName> deliveryChannels = new ArrayList();
		deliveryChannels.add(AlertingConstants.CHANNEL_EMAIL);
		deliveryChannels.add(AlertingConstants.CHANNEL_REPOSITORY);
		sub.setDeliveryChannels(deliveryChannels);

		sub.setFrequency(Frequency.HOURLY);

		sub.setName(name);

		sub.setSubscriptionOwner(new SubscriptionOwner() {

			public String getAccountId() {
				// TODO Auto-generated method stub
				return "zoe";
			}

			public String getAccountType() {
				// TODO Auto-generated method stub
				return SubscriptionOwner.ACCOUNT_USER;
			}
		});

		sub.setTitle(name);
		
		Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
		parameters.put(AlertingConstants.PROP_SUB_REP_SUBSCRIPTION_NODE, nodeActedUpon);
		parameters.put(AlertingConstants.PROP_SUB_REP_INCLUDE_CHILDREN, Boolean.TRUE);
		List<String> changeTypes = new ArrayList<String>();
		changeTypes.add(AlertingConstants.CHANGE_TYPE_DELETED);
		changeTypes.add(AlertingConstants.CHANGE_TYPE_NEW);
		changeTypes.add(AlertingConstants.CHANGE_TYPE_MODIFIED);
		
		parameters.put(AlertingConstants.PROP_SUB_REP_CHANGE_TYPE, (Serializable)changeTypes);
		sub.setParameters(parameters);
		
		subscriptionService.createSubscription(sub);
	}
	
	/**
	 * Method createSearchSubscription.
	 * @param nodeActedUpon NodeRef
	 * @throws Exception
	 */
	private void createSearchSubscription(NodeRef nodeActedUpon) throws Exception {
		SubscriptionType subscriptionType = subscriptionService.getSubscriptionType(AlertingConstants.TYPE_SUBSCRIPTION_REPOSITORY_SEARCH);
		
		Subscription sub = subscriptionService.newSubscriptionInstance(subscriptionType);
		
		String name = "Search Sub1 x" + System.currentTimeMillis() + "x";
		List<QName> deliveryChannels = new ArrayList();
		deliveryChannels.add(AlertingConstants.CHANNEL_EMAIL);
		deliveryChannels.add(AlertingConstants.CHANNEL_REPOSITORY);
		sub.setDeliveryChannels(deliveryChannels);

		sub.setFrequency(Frequency.DAILY);

		sub.setName(name);

		sub.setSubscriptionOwner(new SubscriptionOwner() {

			public String getAccountId() {
				// TODO Auto-generated method stub
				return "dave";
			}

			public String getAccountType() {
				// TODO Auto-generated method stub
				return SubscriptionOwner.ACCOUNT_USER;
			}
		});

		sub.setTitle(name);
		
		Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
		parameters.put(AlertingConstants.PROP_SUB_SEARCH_QUERY, "TEXT:gillen");

		List<String> changeTypes = new ArrayList<String>();
		changeTypes.add(AlertingConstants.CHANGE_TYPE_NEW);
		changeTypes.add(AlertingConstants.CHANGE_TYPE_MODIFIED);
		
		parameters.put(AlertingConstants.PROP_SUB_SEARCH_CHANGE_TYPE, (Serializable)changeTypes);
		sub.setParameters(parameters);
		
		subscriptionService.createSubscription(sub);
	}
	
	/**
	 * Method createRSSSubscription.
	 * @param nodeActedUpon NodeRef
	 * @throws Exception
	 */
	private void createRSSSubscription(NodeRef nodeActedUpon) throws Exception {
		SubscriptionType subscriptionType = subscriptionService.getSubscriptionType(AlertingConstants.TYPE_SUBSCRIPTION_RSS);
		
		Subscription sub = subscriptionService.newSubscriptionInstance(subscriptionType);
		
		String name = "RSS Sub1 x" + System.currentTimeMillis() + "x";
		List<QName> deliveryChannels = new ArrayList();
		deliveryChannels.add(AlertingConstants.CHANNEL_EMAIL);
		deliveryChannels.add(AlertingConstants.CHANNEL_REPOSITORY);
		sub.setDeliveryChannels(deliveryChannels);

		sub.setFrequency(Frequency.HOURLY);

		sub.setName(name);

		sub.setSubscriptionOwner(new SubscriptionOwner() {

			public String getAccountId() {
				// TODO Auto-generated method stub
				return "dave";
			}

			public String getAccountType() {
				// TODO Auto-generated method stub
				return SubscriptionOwner.ACCOUNT_USER;
			}
		});

		sub.setTitle(name);
		
		Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
		parameters.put(AlertingConstants.PROP_SUB_RSS_URL, "http://dsgillen.blogspot.com/feeds/posts/default");

		List<String> changeTypes = new ArrayList<String>();
		changeTypes.add(AlertingConstants.CHANGE_TYPE_NEW);
		changeTypes.add(AlertingConstants.CHANGE_TYPE_MODIFIED);
		
		parameters.put(AlertingConstants.PROP_SUB_RSS_CHANGE_TYPE, (Serializable)changeTypes);
		sub.setParameters(parameters);
		
		subscriptionService.createSubscription(sub);
	}
	
	private void removeSubscriptions() {
		List<Subscription> subscriptions = subscriptionService.getSubscriptions("dave");
		subscriptionService.deleteSubscriptions(subscriptions);	
	}
	
	/**
	 * Method updateSubscription.
	 * @param nodeActedUpon NodeRef
	 * @throws Exception
	 */
	private void updateSubscription(NodeRef nodeActedUpon) throws Exception {
		List<Subscription> subscriptions = subscriptionService.getSubscriptions();
		Subscription sub = subscriptions.get(0);
		
		Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
		parameters.put(AlertingConstants.PROP_SUB_REP_SUBSCRIPTION_NODE, nodeActedUpon);
		parameters.put(AlertingConstants.PROP_SUB_REP_INCLUDE_CHILDREN, Boolean.FALSE);
		sub.setParameters(parameters);
		
		subscriptionService.updateSubscription(sub);
		
	}
	
	private void getAlerts() {
		List<RepositoryAlert> alerts = alertService.getAlerts("dave");
		RepositoryAlert lastAlert = alerts.get(alerts.size() - 1);
		List<Event> events = alertService.getEvents(lastAlert);
		
		boolean isRead = lastAlert.isAlertRead();
		alertService.setAlertRead(lastAlert, true);
		isRead = lastAlert.isAlertRead();
		int i = 1;
	}
	
	/**
	 * Method runDigester.
	 * @throws Exception
	 */
	private void runDigester() throws Exception {
		alertDigester.digestHourlyAlerts();
	}
	
	/**
	 * Method runDailySearch.
	 * @throws Exception
	 */
	private void runDailySearch() throws Exception {
		searchEventProcessor.performDailySearches();
	}
	
	/**
	 * Method runHourlyRSS.
	 * @throws Exception
	 */
	private void runHourlyRSS() throws Exception {
		rssEventProcessor.performHourlyRSS();
	}



}
