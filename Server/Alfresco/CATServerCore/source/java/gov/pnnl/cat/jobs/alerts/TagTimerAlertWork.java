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
package gov.pnnl.cat.jobs.alerts;

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
import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.TagTimerConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;

/**
 */
public class TagTimerAlertWork extends ExtensiblePolicyAdapter {
  private SubscriptionService subscriptionService;

  private NodeService nodeService;

  private AlertManagementService alertService;

  private NamespaceService namespaceService;

  private TransactionService transactionService;

  @Override
  public void init() {

  }

  public void run() {
    try {
      RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>() {
        public Object execute() throws Throwable {
          AuthenticationUtil.setRunAsUserSystem();
          Calendar cal = Calendar.getInstance();
          cal.setTime(new Date());
          cal.add(Calendar.DATE, +7);
          Date endDate = cal.getTime();

          String startDateString = ISO8601DateFormat.format(new Date());
          String endDateString = ISO8601DateFormat.format(endDate);

          // do the search and send the alerts about US Person data first:
          StringBuilder query = new StringBuilder();
          query.append("+@\\{http\\://www.pnl.gov/cat/model/tagtimer/1.0\\}expireDate:[" + startDateString + " TO " + endDateString + "]");

          ResultSet results = null;
          try {
            results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());

            List<NodeRef> nodes = results.getNodeRefs();
            HashMap<String, List<NodeRef>> usPersonAlertsOwnerMap = addNodesToMap(nodes);
            for (String owner : usPersonAlertsOwnerMap.keySet()) {
              createUSPersonAlert(usPersonAlertsOwnerMap.get(owner), owner);
            }
          } finally {
            if (results != null) {
              // MUST close the results or Alfresco will keep the index file handle open
              results.close();
            }
          }

          // next, do the search and send the alerts about criminal intel:
          query = new StringBuilder();
          query.append("+@\\{http\\://www.pnl.gov/cat/model/criminal/1.0\\}expireDate:[" + startDateString + " TO " + endDateString + "]");

          results = null;
          try {
            results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());

            List<NodeRef> nodes = results.getNodeRefs();
            HashMap<String, List<NodeRef>> criminalIntelAlertsOwnerMap = addNodesToMap(nodes);
            for (String owner : criminalIntelAlertsOwnerMap.keySet()) {
              createCriminalIntelAlert(criminalIntelAlertsOwnerMap.get(owner), owner);
            }
          } finally {
            if (results != null) {
              // MUST close the results or Alfresco will keep the index file handle open
              results.close();
            }
          }

          return null;
        }

        private HashMap<String, List<NodeRef>> addNodesToMap(List<NodeRef> nodes) {
          HashMap<String, List<NodeRef>> ownerMap = new HashMap<String, List<NodeRef>>();

          for (NodeRef nodeRef : nodes) {
            Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
            String owner = (String) nodeProperties.get(ContentModel.PROP_OWNER);
            if (ownerMap.containsKey(owner)) {
              List<NodeRef> ownerList = ownerMap.get(owner);
              ownerList.add(nodeRef);
            } else {
              List<NodeRef> ownerList = new ArrayList<NodeRef>();
              ownerList.add(nodeRef);
              ownerMap.put(owner, ownerList);
            }
          }
          return ownerMap;
        }

      };
      transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Method createUSPersonAlert.
   * @param nodeRefs List<NodeRef>
   * @param owner String
   * @throws DeliveryException
   */
  protected void createUSPersonAlert(List<NodeRef> nodeRefs, String owner) throws DeliveryException {
    AlertImpl alert = createAlert(nodeRefs, owner);
    alert.setTitle("US Persons Data Expiration Alert");

    alert.setEvents(createEvents(nodeRefs, TagTimerConstants.PROP_EXPIRE_DATE));

    List<DeliveryChannel> deliveryChannels = subscriptionService.getDeliveryChannels();
    for (DeliveryChannel deliveryChannel : deliveryChannels) {
      deliveryChannel.send(alert);
    }
  }

  /**
   * Method createCriminalIntelAlert.
   * @param nodeRefs List<NodeRef>
   * @param owner String
   * @throws DeliveryException
   */
  protected void createCriminalIntelAlert(List<NodeRef> nodeRefs, String owner) throws DeliveryException {
    AlertImpl alert = createAlert(nodeRefs, owner);
    alert.setTitle("Criminal Intel Expiration Alert");

    alert.setEvents(createEvents(nodeRefs, TagTimerConstants.PROP_INTEL_EXPIRE_DATE));

    List<DeliveryChannel> deliveryChannels = subscriptionService.getDeliveryChannels();
    for (DeliveryChannel deliveryChannel : deliveryChannels) {
      deliveryChannel.send(alert);
    }
  }

  /**
   * Method createAlert.
   * @param nodeRefs List<NodeRef>
   * @param owner String
   * @return AlertImpl
   * @throws DeliveryException
   */
  protected AlertImpl createAlert(List<NodeRef> nodeRefs, String owner) throws DeliveryException {
    AlertImpl alert = new AlertImpl();

    alert.setFrequency(Frequency.DAILY);
    List<Actor> recipients = new ArrayList<Actor>();

    // not sure if i need to do this:
    // Get the group authority
    // String groupPath = new Path(unformattedGroupPath).toString();
    // String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, owner);

    Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, owner, true);
    if (members != null && members.size() > 0) {
      for (String member : members) {
        ActorImpl actor = new ActorImpl();
        actor.setUsername(member);
        recipients.add(actor);
      }
    } else {
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
      sub = createRepositorySubscription(nodeRefs.get(0));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    alert.setSubscriptionType(sub.getType());

    return alert;
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
   * Method createEvents.
   * @param nodeRefs List<NodeRef>
   * @param propExpireDate QName
   * @return List<Event>
   */
  private List<Event> createEvents(List<NodeRef> nodeRefs, QName propExpireDate) {
    List<Event> events = new ArrayList<Event>(nodeRefs.size());

    Date today = new Date();

    for (NodeRef nodeRef : nodeRefs) {
      Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
      Event event = alertService.newEvent();

      Date nodeLastModified = (Date) nodeProperties.get(ContentModel.PROP_MODIFIED);
      String creator = (String) nodeProperties.get(ContentModel.PROP_OWNER);
      String title = (String) nodeProperties.get(ContentModel.PROP_TITLE);
      if (title == null) {
        title = (String) nodeProperties.get(ContentModel.PROP_NAME);
      }

      Date expireDate = (Date) nodeService.getProperty(nodeRef, propExpireDate);

      if (expireDate.after(today)) {
        event.setChangeType(AlertingConstants.CHANGE_TYPE_TAGTIMER_ABOUT_TO_EXPIRE);
        ActorImpl actor = new ActorImpl();
        actor.setUsername(creator);
        event.setEventPerpetrator(actor);
      } else {
        event.setChangeType(AlertingConstants.CHANGE_TYPE_TAGTIMER_EXPIRED);
        event.setEventPerpetrator(ActorImpl.getSystemActor());
      }

      event.setEventTime(nodeLastModified);
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

    parameters.put(AlertingConstants.PROP_SUB_REP_CHANGE_TYPE, (Serializable) changeTypes);
    sub.setParameters(parameters);

    // subscriptionService.createSubscription(sub);
    return sub;
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
