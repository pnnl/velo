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
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.internal.ActorImpl;
import gov.pnnl.cat.alerting.alerts.internal.AlertImpl;
import gov.pnnl.cat.alerting.delivery.DeliveryChannel;
import gov.pnnl.cat.alerting.exceptions.DeliveryException;
import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.Subscription;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionType;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.TagTimerConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 */
public class FakeAlertAction extends ActionExecuterAbstractBase {

	private SubscriptionService subscriptionService;
	private NodeService nodeService;
	private AlertManagementService alertService;
	private NamespaceService namespaceService;
	private TransactionService transactionService;
	private AuthorityService authorityService;
	
	

	  /**
	   * Method setAuthorityService.
	   * @param authorityService AuthorityService
	   */
	  public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * Method createAlert.
	 * @param nodeRef NodeRef
	 * @throws DeliveryException
	 */
	protected void createAlert(NodeRef nodeRef) throws DeliveryException {
		String owner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER);
	    List<DeliveryChannel> allDeliveryChannels = subscriptionService.getDeliveryChannels();
		List<DeliveryChannel> deliveryChannels = new ArrayList<DeliveryChannel>();
		
		for (DeliveryChannel deliveryChannel : allDeliveryChannels) {
		  if(deliveryChannel.getName().equals(AlertingConstants.CHANNEL_REPOSITORY)){
		    deliveryChannels.add(deliveryChannel);
		    break;
		  }
    }
		
	    AlertImpl alert = new AlertImpl();
	    
	    alert.setFrequency(Frequency.DAILY);
	    List<Actor> recipients = new ArrayList<Actor>();

	    //not sure if i need to do this:
	//  Get the group authority
//	    String groupPath = new Path(unformattedGroupPath).toString();
//	    String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, owner);

	    Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, owner, true);
	    if(members != null && members.size() > 0){
	    	for (String member : members) {
	    	  ActorImpl actor = new ActorImpl();
	    	  actor.setUsername(member);
	    		recipients.add(actor);
			}
	    }else{
	      ActorImpl actor = new ActorImpl();
	      actor.setUsername(owner);
	    	recipients.add(actor);
	    }    
	    
	    alert.setRecipients(recipients);
	    
	    
	    Actor sender = new ActorImpl();
	    ((ActorImpl)sender).setUsername(owner);
	    alert.setSender(sender);

	    Subscription sub = null;
	    try {
	      sub = createRepositorySubscription(nodeRef);
	    } catch (Exception e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    
	    alert.setSubscriptionType(sub.getType());
	    alert.setTitle("US Persons Data Expiration Alert");

	    List<Event> events = createEvents(nodeRef);
	    if(!events.isEmpty()){
	      alert.setEvents(events);      

	      for (DeliveryChannel deliveryChannel : deliveryChannels) {
	        deliveryChannel.send(alert);
	      }
	    }else{
	      System.out.println("not sending alert, node does not have an expire date property set");
	    }
	  }
	  
	  /**
	   * Method createEvents.
	   * @param nodeRef NodeRef
	   * @return List<Event>
	   */
	  private List<Event> createEvents(NodeRef nodeRef) {
		    List<Event> events = new ArrayList<Event>(1);

		    Date today = new Date();
		    
		      Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
		      Event event = alertService.newEvent();

		      Date nodeLastModified = (Date)nodeProperties.get(ContentModel.PROP_MODIFIED);
		      String creator = (String)nodeProperties.get(ContentModel.PROP_OWNER);
		      String title = (String)nodeProperties.get(ContentModel.PROP_TITLE);
		      if (title == null) {
		        title = (String)nodeProperties.get(ContentModel.PROP_NAME);
		      }
		      
		      
		      Date expireDate = (Date)nodeService.getProperty(nodeRef, TagTimerConstants.PROP_EXPIRE_DATE);
		      if(expireDate == null){
		        expireDate = (Date)nodeService.getProperty(nodeRef, TagTimerConstants.PROP_INTEL_EXPIRE_DATE);
		      }
		      
		      if(expireDate != null){
  		      if(expireDate.after(today)){
  		    	  event.setChangeType(AlertingConstants.CHANGE_TYPE_TAGTIMER_ABOUT_TO_EXPIRE);
  		    	  ActorImpl actor = new ActorImpl();
  		        actor.setUsername(creator);
  		        event.setEventPerpetrator(actor); 
  		      }else{
  		    	  event.setChangeType(AlertingConstants.CHANGE_TYPE_TAGTIMER_EXPIRED);
  		          event.setEventPerpetrator(ActorImpl.getSystemActor()); 
  		      }
  
  		      event.setEventTime(nodeLastModified);
  
  		      QName nodeType = nodeService.getType(nodeRef);
  		      // do something special if this is a forum:topic or forum:post node
  		      
  		      event.setResourceName(title);
  		      event.setResourceURL(NodeUtils.getBrowseUrlForNodeRef(nodeRef, nodeService, namespaceService));
  		      event.setUUID(nodeRef.getId());
  		      
  		      
  		      events.add(event);
		      }
		    return events;
		  }
		  
		  /**
		   * Method createRepositorySubscription.
		   * @param nodeActedUpon NodeRef
		   * @return Subscription
		   * @throws Exception
		   */
		  private Subscription createRepositorySubscription(NodeRef nodeActedUpon) throws Exception {
		    SubscriptionType subscriptionType = subscriptionService.getSubscriptionType(AlertingConstants.TYPE_SUBSCRIPTION_REPOSITORY);
		    
		    Subscription sub = subscriptionService.newSubscriptionInstance(subscriptionType);
		    sub.setNodeRef(nodeActedUpon);
		    
		    String name = "Sub1 x" + System.currentTimeMillis() + "x";
		    List<QName> deliveryChannels = new ArrayList<QName>();
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
		    parameters.put(AlertingConstants.PROP_SUB_REP_SUBSCRIPTION_NODE, nodeActedUpon);
		    parameters.put(AlertingConstants.PROP_SUB_REP_INCLUDE_CHILDREN, Boolean.TRUE);
		    List<String> changeTypes = new ArrayList<String>();
		    changeTypes.add(AlertingConstants.CHANGE_TYPE_DELETED);
		    changeTypes.add(AlertingConstants.CHANGE_TYPE_NEW);
		    changeTypes.add(AlertingConstants.CHANGE_TYPE_MODIFIED);
		    
		    parameters.put(AlertingConstants.PROP_SUB_REP_CHANGE_TYPE, (Serializable)changeTypes);
		    sub.setParameters(parameters);
		    
//		    subscriptionService.createSubscription(sub);
		    return sub;
		  }

	  
	
	

	/**
	 * Method setSubscriptionService.
	 * @param subscriptionService SubscriptionService
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}


	/**
	 * Method setAlertService.
	 * @param alertService AlertManagementService
	 */
	public void setAlertService(AlertManagementService alertService) {
		this.alertService = alertService;
	}


	/**
	 * Method executeImpl.
	 * @param ruleAction Action
	 * @param nodeActedUpon NodeRef
	 */
	@Override
	protected void executeImpl(final Action ruleAction, final NodeRef nodeActedUpon) {
				try {
					RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>() {
						public Object execute() throws Throwable {
					AuthenticationUtil.setRunAsUserSystem();
							createAlert(nodeActedUpon);
							return null;
						}
					};
					transactionService.getRetryingTransactionHelper().doInTransaction(cb,
							false, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
	}




	/**
	 * Method addParameterDefinitions.
	 * @param paramList List<ParameterDefinition>
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	/**
	 * Method setNamespaceService.
	 * @param namespaceService NamespaceService
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	/**
	 * Method setTransactionService.
	 * @param transactionService TransactionService
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
}
