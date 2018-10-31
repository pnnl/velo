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
package gov.pnnl.cat.alerting.detection.internal.search;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.internal.ActorImpl;
import gov.pnnl.cat.alerting.detection.internal.AbstractEventProcessor;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Starts a job that runs searches at the appropriate 
 * frequency.  Immediately create alerts - no need for
 * temporary alerts in this case.
 * @version $Revision: 1.0 $
 */
public class SearchEventProcessor extends AbstractEventProcessor {

	private Set<Subscription> searchSubscriptions = new HashSet<Subscription>();
	private SearchService searchService;
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

	/**
	 * Method setSearchService.
	 * @param searchService SearchService
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.subscriptions.SubscriptionListener#subscriptionChanged(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public void subscriptionChanged(NodeRef subscription) {
		// remove and re-add subscription
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
		synchronized(searchSubscriptions) {
			searchSubscriptions.add(subscriptionObj);
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
		// remove from the list of subscriptinos to process
		synchronized(searchSubscriptions) {
			searchSubscriptions.remove(subscription);
		}
	}  

	public void performDailySearches() {
		// wrap in a transaction
		RetryingTransactionCallback cb = new RetryingTransactionCallback() {

			public Object execute() throws Throwable {
				performSearches(Frequency.DAILY);
				return null;
			}
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
	}

	/**
	 * For each subscription, digests temporary alerts into one alert. 
	 * For each digested alert, creates the alert object and sends it to all
	 * the delivery channels registered for that subscription.
	 * 
	
	 */  
	public void performWeeklySearches() {
		// wrap in a transaction
		RetryingTransactionCallback cb = new RetryingTransactionCallback() {

			public Object execute() throws Throwable {
				performSearches(Frequency.WEEKLY);
				return null;
			}
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);
	}

	/**
	 * Method performSearches.
	 * @param frequency Frequency
	 */
	private void performSearches(final Frequency frequency) {
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
		synchronized(searchSubscriptions) {
			subscriptions = new ArrayList<Subscription>(searchSubscriptions);
		}
		for (Subscription subscription : subscriptions) {
			if (isHandledSubscriptionType(subscription.getType()) && 
					subscription.getFrequency().equals(frequency)) {
				performSearch(subscription, frequency);
			}
		}
	}

	/**
	 * Method performSearch.
	 * @param subscription Subscription
	 * @param frequency Frequency
	 */
	private void performSearch(Subscription subscription, Frequency frequency) {
		// this is a subscription we care about
		String query = (String)subscription.getParameters().get(AlertingConstants.PROP_SUB_SEARCH_QUERY);
		// add a date component to the query
		Date startDate;
		Date endDate;
		if (frequency.equals(Frequency.DAILY)) {
			// for a daily search, we want to bound our search to start date=yesterday, end date=yesterday
			// Alfresco will automatically convert this to a start time of 00:00, and an end time of 23:59
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			endDate = cal.getTime();
			cal.add(Calendar.DATE, -1);
			startDate = cal.getTime();
		} else if (frequency.equals(Frequency.WEEKLY)) {
			// for a weekly search, we want to bound our search to start date=today-7 days, end date=yesterday
			// Alfresco will automatically convert this to a start time of 00:00, and an end time of 23:59
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, -7);
			startDate = cal.getTime();

			cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, -1);
			endDate = cal.getTime();	
		} else {
			// this should never happen :)
			throw new AlfrescoRuntimeException("Invalid Frequency: " + frequency);
		}

		String startDateString = ISO8601DateFormat.format(startDate);
		String endDateString = ISO8601DateFormat.format(endDate);
		String dateClause = "+@\\{http\\://www.alfresco.org/model/content/1.0\\}modified:[" + startDateString + " TO " + endDateString + "]";

		// now append our clause with the original query
		query = "(" + query + ") AND " + dateClause;

		// perform the query!
		// TODO: make compatible with groups
		AuthenticationUtil.setRunAsUser(subscription.getOwner().getAccountId());
		
		ResultSet results = null;
		try {
		  results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);

		  List<NodeRef> matchedNodeRefs = results.getNodeRefs();

		  if (matchedNodeRefs.size() > 0) {
		    List<Event> events = createEvents(matchedNodeRefs, startDate);

		    try {
		      super.createAlert(subscription, events, sender);
		    } catch (DeliveryException de) {
		      logger.error("Exception creating alert", de);
		    }
		  }
		} finally {
		  if(results != null) {
		    results.close();
		  }
		}
	}

	/**
	 * Method createEvents.
	 * @param nodeRefs List<NodeRef>
	 * @param startDate Date
	 * @return List<Event>
	 */
	private List<Event> createEvents(List<NodeRef> nodeRefs, Date startDate) {
		List<Event> events = new ArrayList<Event>(nodeRefs.size());
		Set<NodeRef> visitedTopicNodes = new HashSet<NodeRef>();

		for (NodeRef nodeRef : nodeRefs) {
			Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
			Event event = alertManagementService.newEvent();

			Date nodeCreatedDate = (Date)nodeProperties.get(ContentModel.PROP_CREATED);
			Date nodeLastModified = (Date)nodeProperties.get(ContentModel.PROP_MODIFIED);
			String creator = (String)nodeProperties.get(ContentModel.PROP_CREATOR);
			String title = (String)nodeProperties.get(ContentModel.PROP_TITLE);
			if (title == null) {
			  title = (String)nodeProperties.get(ContentModel.PROP_NAME);
			}
			// if the creation date of this node is after the start date of our search,
			// assume this is a newly created node.  Otherwise, assume a modification
			// to an existing node
			if (nodeCreatedDate.after(startDate)) {
				event.setChangeType(AlertingConstants.CHANGE_TYPE_NEW);

	      ActorImpl actor = new ActorImpl();
	      actor.setUsername(creator);
				event.setEventPerpetrator(actor);
			} else {
				event.setChangeType(AlertingConstants.CHANGE_TYPE_MODIFIED);
				// we don't know who modified this doc, so set to the system actor
				event.setEventPerpetrator(ActorImpl.getSystemActor());
			}

			event.setEventTime(nodeLastModified);

			QName nodeType = nodeService.getType(nodeRef);
			// do something special if this is a forum:topic or forum:post node
			if (nodeType.equals(ForumModel.TYPE_TOPIC)) {
				// make sure we haven't handled this topic or a post in this topic already
				if (visitedTopicNodes.contains(nodeRef) == false) {
					visitedTopicNodes.add(nodeRef);
					
					// we haven't visited this forum yet, process it, 
					// but grab some of the info from the node being discussed				
					populateEventFromTopicNodeRef(event, nodeRef);

					events.add(event);
				}
			} else if (nodeType.equals(ForumModel.TYPE_POST)) {
				// this is a post to a topic to a forum.  Find the topic.
				NodeRef topicNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
				
				// make sure we haven't handled this topic or a post in this topic already
				if (visitedTopicNodes.contains(topicNodeRef) == false) {
					visitedTopicNodes.add(topicNodeRef);
					
					// we haven't visited this forum yet, process it, 
					// but grab some of the info from the node being discussed				
					populateEventFromTopicNodeRef(event, topicNodeRef);

					events.add(event);
				}

			} else {
			
			event.setResourceName(title);
			event.setResourceURL(nodeUtils.getBrowseUrlForNodeRef(nodeRef));
			event.setUUID(nodeRef.getId());
			events.add(event);
			}


		}
		return events;
	}

	/**
	 * Method populateEventFromTopicNodeRef.
	 * @param event Event
	 * @param topicNodeRef NodeRef
	 */
	private void populateEventFromTopicNodeRef(Event event, NodeRef topicNodeRef) {
		NodeRef forumNodeRef = nodeService.getPrimaryParent(topicNodeRef).getParentRef();
		NodeRef discussedNodeRef = nodeService.getPrimaryParent(forumNodeRef).getParentRef();
		
		// grab a copy of the properties of the node being discussed
		Map<QName, Serializable> discussedProps = nodeService.getProperties(discussedNodeRef);
		Map<QName, Serializable> topicProps = nodeService.getProperties(topicNodeRef);

		String discussedTitle = (String)discussedProps.get(ContentModel.PROP_TITLE);
		if (discussedTitle == null) {
			discussedTitle = (String)discussedProps.get(ContentModel.PROP_NAME);
		}
		
		String forumTitle = (String)topicProps.get(ContentModel.PROP_TITLE);
		if (forumTitle == null) {
			forumTitle = (String)topicProps.get(ContentModel.PROP_NAME);
		}
		
		event.setResourceName("Comment " + forumTitle + " from " + discussedTitle);
		event.setResourceURL(nodeUtils.getBrowseUrlForNodeRef(discussedNodeRef));
		event.setUUID(discussedNodeRef.getId());
	}
	

}
