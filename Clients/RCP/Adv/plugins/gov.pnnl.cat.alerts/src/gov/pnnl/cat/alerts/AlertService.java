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
package gov.pnnl.cat.alerts;

import gov.pnnl.cat.alerts.model.Alert;
import gov.pnnl.cat.alerts.model.IAlert;
import gov.pnnl.cat.alerts.model.IEvent;
import gov.pnnl.cat.alerts.model.ISubscription;
import gov.pnnl.cat.alerts.model.ISubscription.ChangeType;
import gov.pnnl.cat.alerts.model.ISubscription.Channel;
import gov.pnnl.cat.alerts.model.ISubscription.Frequency;
import gov.pnnl.cat.alerts.model.ISubscription.Type;
import gov.pnnl.cat.alerts.model.RepositorySubscription;
import gov.pnnl.cat.alerts.model.SearchSubscription;
import gov.pnnl.cat.alerts.model.SearchSubscription.Trigger;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoUtils;
import gov.pnnl.cat.webservice.alert.Actor;
import gov.pnnl.cat.webservice.alert.Event;
import gov.pnnl.cat.webservice.alert.RepositoryAlert;
import gov.pnnl.cat.webservice.subscription.Subscription;
import gov.pnnl.cat.webservice.subscription.SubscriptionOwner;
import gov.pnnl.cat.webservice.util.AlertingConstants;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Reference;

/**
 * TODO: Alerts and subscriptions should be returned as IResource objects, not separate objects.
 */
public class AlertService {
  
  /**
   * Adds a subscription.
   * This method requires a call to the server.
   * @param subscription
  
   * @throws ServerException */
  public void addSubscription(ISubscription subscription) throws ServerException {
    Subscription subscriptionBean = convert(subscription)[0];

    try {
      ResourcesPlugin.getDefault().getResourceManager().createSubscription(subscriptionBean);
    } catch (Exception e) {
      throw new ServerException(e);
    }
  }

  /**
   * Deletes a subscription.
   * This method requires a call to the server.
   * @param subscriptions
  
   * @throws ServerException */
  public void deleteSubscription(ISubscription... subscriptions) throws ServerException {
    Subscription[] subscriptionBeans = convert(subscriptions);

    try {
      ResourcesPlugin.getDefault().getResourceManager().deleteSubscriptions(subscriptionBeans);
    } catch (Exception e) {
      throw new ServerException(e);
    }
  }

  /**
   * Returns all subscriptions visible to the current user.
   * This method requires a call to the server.
  
  
   * @return ISubscription[]
   * @throws ServerException */
  public ISubscription[] getSubscriptions() throws ServerException {
    try {
      Subscription[] subscriptionBeans = ResourcesPlugin.getDefault().getResourceManager().getSubscriptions();

      return convert(subscriptionBeans);
    } catch (Exception e) {
      throw new ServerException(e);
    }
  }

  /**
   * Returns all alerts visible to the current user.
   * This method requires a call to the server.
   * @return IAlert[]
   * @throws ServerException */
  public IAlert[] getAlerts() throws ServerException {
    try {
      RepositoryAlert[] alertBeans = ResourcesPlugin.getDefault().getResourceManager().getAlerts();
      //return convert(alertBeans);
      return getFakeTestData();
      
    } catch (Exception e) {
      throw new ServerException(e);
    }
  }
  
  private IAlert[] getFakeTestData() throws Exception {
    IResource resource = ResourcesPlugin.getResourceManager().getHomeFolder();
    Alert alert = new Alert();
    alert.setRead(false);
    alert.setCreated(resource.getPropertyAsDate(VeloConstants.PROP_CREATED));
    alert.setFrequency(Frequency.DAILY);
    alert.setName("Bogus Alert");
    alert.setSubscriptionType(Type.SEARCH);
    alert.setSummary("blah blah");
    alert.setTitle("this is a title");
    IUser curUser = ResourcesPlugin.getSecurityManager().getActiveUser();
    alert.setSender(curUser);
    IUser[] recipients = new IUser[] {curUser};
    alert.setRecipients(recipients);
    alert.setId(resource.getPropertyAsString(VeloConstants.PROP_UUID));

    gov.pnnl.cat.alerts.model.Event event = new gov.pnnl.cat.alerts.model.Event();
    event.setChangeType(ChangeType.NEW);
    event.setPerpetrator(curUser);
    event.setResourceName(resource.getName());
    event.setTime(resource.getPropertyAsDate(VeloConstants.PROP_CREATED));
    event.setUrl("http://www.google.com");
    event.setId(resource.getPropertyAsString(VeloConstants.PROP_UUID));  
    event.setValid(true);     
    event.setResourcePath(resource.getPath());
    IEvent[] events = new IEvent[]{event};
    alert.setEvents(events);
    
    return new IAlert[] {alert};

  }

  /**
   * Deletes the specified alerts from the repository.
   * This method requires a call to the server.
   * @param alerts
  
   * @throws ServerException */
  public void deleteAlerts(IAlert... alerts) throws ServerException {
    try {
      RepositoryAlert[] alertBeans = convert(alerts);

      ResourcesPlugin.getDefault().getResourceManager().deleteAlerts(alertBeans);
    } catch (Exception e) {
      throw new ServerException(e);
    }
  }

  /**
   * Marks the specified alerts as read.
   * This method requires a call to the server
   * @param alerts
  
   * @param read boolean
   * @throws ServerException */
  public void markAlertsAsRead(IAlert[] alerts) throws ServerException {
    //removed second param, boolean isRead for now to keep service impl easy, can add back later if its needed
    try {
      RepositoryAlert[] alertBeans = convert(alerts);

      ResourcesPlugin.getDefault().getResourceManager().markAlertsAsRead(alertBeans);
    } catch (Exception e) {
      throw new ServerException(e);
    }
  }


  //
  // private helper methods
  //


  /**
   * Method convertFrequency.
   * @param frequency Frequency
   * @return String
   */
  private String convertFrequency(Frequency frequency) {
    String frequencyStr = null;

    switch (frequency) {
      case DAILY:
        frequencyStr = VeloConstants.SUBSCRIPTION_FREQ_DAILY;
        break;
      case WEEKLY:
        frequencyStr = VeloConstants.SUBSCRIPTION_FREQ_WEEKLY;
        break;
      case HOURLY:
        frequencyStr = VeloConstants.SUBSCRIPTION_FREQ_HOURLY;
        break;
    }

    return frequencyStr;
  }

  /**
   * Method convertFrequency.
   * @param frequencyStr String
   * @return Frequency
   */
  private Frequency convertFrequency(String frequencyStr) {
    Frequency frequency = null;

    if (frequencyStr.equals(VeloConstants.SUBSCRIPTION_FREQ_DAILY)) {
      frequency = Frequency.DAILY;
    } else if (frequencyStr.equals(VeloConstants.SUBSCRIPTION_FREQ_WEEKLY)) {
      frequency = Frequency.WEEKLY;
    } else if (frequencyStr.equals(VeloConstants.SUBSCRIPTION_FREQ_HOURLY)) {
      frequency = Frequency.HOURLY;
    }

    return frequency;
  }

  /**
   * Method convertType.
   * @param type Type
   * @return String
   */
  private String convertType(Type type) {
    String typeStr = null;

    switch (type) {
      case SEARCH:
        typeStr = VeloConstants.TYPE_SUBSCRIPTION_REPOSITORY_SEARCH;
        break;
      case REPOSITORY:
        typeStr = VeloConstants.TYPE_SUBSCRIPTION_REPOSITORY;
        break;
    }

    return typeStr;
  }

  /**
   * Method convertType.
   * @param typeStr String
   * @return Type
   */
  private Type convertType(String typeStr) {
    Type type = null;

    if (typeStr.equals(VeloConstants.TYPE_SUBSCRIPTION_REPOSITORY_SEARCH)) {
      type = Type.SEARCH;
    } else if (typeStr.equals(VeloConstants.TYPE_SUBSCRIPTION_REPOSITORY)) {
      type = Type.REPOSITORY;
    } else {
      // TODO: support other subscription types
    }

    return type;
  }

  /**
   * Method convertChangeTypes.
   * @param changeTypes ChangeType[]
   * @return String[]
   */
  private String[] convertChangeTypes(ChangeType... changeTypes) {
    String[] changeTypeStrs = new String[changeTypes.length];

    for (int i = 0; i < changeTypeStrs.length; i++) {
      switch (changeTypes[i]) {
        case NEW:
          changeTypeStrs[i] = VeloConstants.SUBSCRIPTION_CHANGE_TYPE_NEW;
          break;
        case DELETED:
          changeTypeStrs[i] = VeloConstants.SUBSCRIPTION_CHANGE_TYPE_DELETED;
          break;
        case MODIFIED:
          changeTypeStrs[i] = VeloConstants.SUBSCRIPTION_CHANGE_TYPE_MODIFIED;
          break;
        case EXPIRED:
          changeTypeStrs[i] = VeloConstants.SUBSCRIPTION_CHANGE_TYPE_EXPIRED;
          break;
        case EXPIRING:
          changeTypeStrs[i] = VeloConstants.SUBSCRIPTION_CHANGE_TYPE_ABOUT_TO_EXPIRE;
          break;
      }
    }

    return changeTypeStrs;
  }

  /**
   * Method convertChangeTypes.
   * @param changeTypeStrs String[]
   * @return ChangeType[]
   */
  private ChangeType[] convertChangeTypes(String... changeTypeStrs) {
    List<ChangeType> changeTypes = new ArrayList<ChangeType>();

    for (String changeTypeStr : changeTypeStrs) {
      if (changeTypeStr.equals(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_NEW)) {
        changeTypes.add(ChangeType.NEW);
      } else if (changeTypeStr.equals(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_MODIFIED)) {
        changeTypes.add(ChangeType.MODIFIED);
      } else if (changeTypeStr.equals(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_DELETED)) {
        changeTypes.add(ChangeType.DELETED);
      } else if (changeTypeStr.equalsIgnoreCase(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_ABOUT_TO_EXPIRE)){
    	changeTypes.add(ChangeType.EXPIRING);
      } else if (changeTypeStr.equalsIgnoreCase(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_EXPIRED)){
    	changeTypes.add(ChangeType.EXPIRED);
      }
    }    

    return changeTypes.toArray(new ChangeType[changeTypes.size()]);
  }

  /**
   * Method convertUsers.
   * @param users IUser[]
   * @return Actor[]
   * @throws ServerException
   * @throws ResourceException
   */
  private Actor[] convertUsers(IUser... users) throws ServerException, ResourceException {
    Actor[] actors = new Actor[users.length];

    for (int i = 0; i < actors.length; i++) {
      if (users[i] == null) {
        actors[i] = null;
      } else {
        actors[i] = new Actor(users[i].getUsername(), null);
      }
    }

    return actors;
  }

  /**
   * Method convertUsers.
   * @param actors Actor[]
   * @return IUser[]
   * @throws ServerException
   * @throws ResourceException
   */
  private IUser[] convertUsers(Actor... actors) throws ServerException, ResourceException {
    IUser[] users = new IUser[actors.length];

    for (int i = 0; i < users.length; i++) {
      Actor actor = actors[i];
      users[i] = null;
      String username = actor.getId();
      IUser user = ResourcesPlugin.getSecurityManager().getUser(username);
      users[i] = user;
    }

    return users;
  }

  /**
   * Method convert.
   * @param events IEvent[]
   * @return Event[]
   * @throws ServerException
   * @throws ResourceException
   */
  private Event[] convert(IEvent... events) throws ServerException, ResourceException {
    Event[] eventBeans = new Event[events.length];

    for (int i = 0; i < eventBeans.length; i++) {
      IEvent event = events[i];
      Event eventBean = new Event();

      eventBean.setChangeType(convertChangeTypes(event.getChangeType())[0]);
      eventBean.setPerpetrator(convertUsers(event.getPerpetrator())[0]);
      eventBean.setResourceName(event.getResourceName());
      eventBean.setEventTime(event.getTime());
      eventBean.setResourceUrl(event.getUrl());
      eventBean.setUuid(event.getId());

      eventBeans[i] = eventBean;
    }

    return eventBeans;
  }

  /**
   * Method convert.
   * @param eventBeans Event[]
   * @return IEvent[]
   * @throws ServerException
   * @throws ResourceException
   */
  private IEvent[] convert(Event... eventBeans) throws ServerException, ResourceException {
    IEvent[] events = new IEvent[eventBeans.length];

    for (int i = 0; i < events.length; i++) {
      gov.pnnl.cat.alerts.model.Event event = new gov.pnnl.cat.alerts.model.Event();
      Event eventBean = eventBeans[i];

      event.setChangeType(convertChangeTypes(eventBean.getChangeType())[0]);
      event.setPerpetrator(convertUsers(eventBean.getPerpetrator())[0]);
      event.setResourceName(eventBean.getResourceName());
      event.setTime(eventBean.getEventTime());
      event.setUrl(eventBean.getResourceUrl());
      event.setId(eventBean.getUuid());  
      event.setValid(eventBean.getValid());
      String resourcePath = eventBean.getResourcePath();
      if(resourcePath != null) {
        event.setResourcePath(new CmsPath(resourcePath));
      }
      events[i] = event;
    }

    return events;
  }

  /**
   * Method convert.
   * @param alerts IAlert[]
   * @return RepositoryAlert[]
   * @throws ServerException
   * @throws ResourceException
   */
  private RepositoryAlert[] convert(IAlert... alerts) throws ServerException, ResourceException {
    RepositoryAlert[] alertBeans = new RepositoryAlert[alerts.length];

    for (int i = 0; i < alertBeans.length; i++) {
      IAlert alert = alerts[i];
      RepositoryAlert alertBean = new RepositoryAlert();

      alertBean.setAlertRead(alert.isRead());
      alertBean.setCreated(alert.getCreated());
      alertBean.setFrequency(convertFrequency(alert.getFrequency()));
      alertBean.setName(alert.getName());
      Reference reference = AlfrescoUtils.getReference(alert.getId());
      alertBean.setNode(reference);
      alertBean.setRecipients(convertUsers(alert.getRecipients()));
      alertBean.setSender(convertUsers(alert.getSender())[0]);
      alertBean.setSubscriptionType(convertType(alert.getSubscriptionType()));
      alertBean.setSummary(alert.getSummary());
      alertBean.setTitle(alert.getTitle());
      alertBean.setEvents(convert(alert.getEvents()));

      alertBeans[i] = alertBean;
    }

    return alertBeans;
  }

  /**
   * Method convert.
   * @param alertBeans RepositoryAlert[]
   * @return IAlert[]
   * @throws ServerException
   * @throws ResourceException
   */
  private IAlert[] convert(RepositoryAlert... alertBeans) throws ServerException, ResourceException {
    if (alertBeans == null) {
      return new Alert[0];
    }

    List<IAlert> alerts = new ArrayList<IAlert>();

    for (RepositoryAlert alertBean : alertBeans) {
      Alert alert = new Alert();
      alert.setRead(alertBean.isAlertRead());
      alert.setCreated(alertBean.getCreated());
      alert.setFrequency(convertFrequency(alertBean.getFrequency()));
      alert.setName(alertBean.getName());
      alert.setSubscriptionType(convertType(alertBean.getSubscriptionType()));
      alert.setSummary(alertBean.getSummary());
      alert.setTitle(alertBean.getTitle());
      alert.setSender(convertUsers(alertBean.getSender())[0]);
      alert.setRecipients(convertUsers(alertBean.getRecipients()));
      alert.setId(alertBean.getNode().getUuid());

      alert.setEvents(convert(alertBean.getEvents()));
      alerts.add(alert);
    }
    return alerts.toArray(new IAlert[alerts.size()]);
  }

  /**
   * Method convert.
   * @param beans Subscription[]
   * @return ISubscription[]
   * @throws ServerException
   * @throws ResourceException
   */
  private ISubscription[] convert(Subscription[] beans) throws ServerException, ResourceException {
    if (beans == null) {
      return new ISubscription[0];
    }

    List<ISubscription> subscriptions = new ArrayList<ISubscription>();

    for (int i = 0; i < beans.length; i++) {
      Subscription bean = beans[i];

      if (bean.getType().equals(VeloConstants.TYPE_SUBSCRIPTION_REPOSITORY_SEARCH)) {
        subscriptions.add(createSearchSubscription(bean));
      } else if(bean.getType().equals(AlertingConstants.TYPE_SUBSCRIPTION_REPOSITORY)) {
        subscriptions.add(createRepositorySubscription(bean));
      }
    }

    return subscriptions.toArray(new ISubscription[subscriptions.size()]);
  }
  
  /**
   * Method createRepositorySubscription.
   * @param bean Subscription
   * @return RepositorySubscription
   * @throws ServerException
   * @throws ResourceException
   */
  private RepositorySubscription createRepositorySubscription(Subscription bean) throws ServerException, ResourceException{
	  RepositorySubscription repoSub = new RepositorySubscription();
	  NamedValue[] properties = bean.getProperties();
	  
	  repoSub.setId(bean.getNode().getUuid());
	  
	  Calendar created = bean.getCreated();
	  repoSub.setCreated(created);
	  
	  for(NamedValue prop : properties){
		  if(prop.getName().equals(AlertingConstants.PROP_SUB_REP_CHANGE_TYPE)){
			  String[] changeTypeStrings = prop.getValues();
			  Collection<ChangeType> changeTypes = new ArrayList<ChangeType>();
			  
			  for(String changeTypeString : changeTypeStrings){
				  if(changeTypeString.equals(AlertingConstants.CHANGE_TYPE_NEW)){
					  changeTypes.add(ChangeType.NEW);
				  }else if(changeTypeString.equals(AlertingConstants.CHANGE_TYPE_MODIFIED)){
					  changeTypes.add(ChangeType.MODIFIED);
				  }else if(changeTypeString.equals(AlertingConstants.CHANGE_TYPE_DELETED)){
					  changeTypes.add(ChangeType.DELETED);
				  }
			  }
			  
			  repoSub.setChangeTypes(changeTypes.toArray(new ChangeType[changeTypes.size()]));
		  }
		  
		  String[] channelStrings = bean.getDeliveryChannel();
		  Collection<Channel> channels = new ArrayList<Channel>();
		  
		  for (String channelString : channelStrings) {
		      if (channelString.equals(VeloConstants.SUBSCRIPTION_CHANNEL_EMAIL)) {
		        channels.add(Channel.EMAIL);
		      } else if (channelString.equals(VeloConstants.SUBSCRIPTION_CHANNEL_REPOSITORY)) {
		        channels.add(Channel.REPOSITORY);
		      }
		    }
		  
		  repoSub.setChannels(channels.toArray(new Channel[channels.size()]));
		  
		  repoSub.setFrequency(convertFrequency(bean.getFrequency()));
		  repoSub.setName(bean.getName());
		  repoSub.setTitle(bean.getTitle());
		  
		  SubscriptionOwner owner = bean.getOwner();
		    if (owner.getType().equals(VeloConstants.SUBSCRIPTION_OWNER_ACCOUNT_USER)) {
		      String username = owner.getId();
		      IUser user = ResourcesPlugin.getSecurityManager().getUser(username);
		      repoSub.setUser(user);
		    }
	  }
	  
	  return repoSub;
  }

  /**
   * Method createSearchSubscription.
   * @param bean Subscription
   * @return SearchSubscription
   * @throws ServerException
   * @throws ResourceException
   */
  private SearchSubscription createSearchSubscription(Subscription bean) throws ServerException, ResourceException {
    SearchSubscription searchSub = new SearchSubscription();
    NamedValue[] properties = bean.getProperties();

    searchSub.setId(bean.getNode().getUuid());

    Calendar created = bean.getCreated();
    searchSub.setCreated(created);

    for (NamedValue prop : properties) {
      if (prop.getName().equals(VeloConstants.PROP_SUB_SEARCH_QUERY)) {
        searchSub.setQuery(prop.getValue());
      } else if (prop.getName().equals(VeloConstants.PROP_SUB_SEARCH_CHANGE_TYPE)) {
        String[] triggerStrings = prop.getValues();
        Collection<Trigger> triggers = new ArrayList<Trigger>(3);

        for (String triggerString : triggerStrings) {
          if (triggerString.equals(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_NEW)) {
            triggers.add(Trigger.NEW);
          } else if (triggerString.equals(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_MODIFIED)) {
            triggers.add(Trigger.MODIFIED);
          } else if (triggerString.equals(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_DELETED)) {
            triggers.add(Trigger.DELETED);
          }
        }

        searchSub.setTriggers(triggers.toArray(new Trigger[triggers.size()]));
      }
    }

    String[] channelStrings = bean.getDeliveryChannel();
    Collection<Channel> channels = new ArrayList<Channel>(2);

    for (String channelString : channelStrings) {
      if (channelString.equals(VeloConstants.SUBSCRIPTION_CHANNEL_EMAIL)) {
        channels.add(Channel.EMAIL);
      } else if (channelString.equals(VeloConstants.SUBSCRIPTION_CHANNEL_REPOSITORY)) {
        channels.add(Channel.REPOSITORY);
      }
    }

    searchSub.setChannels(channels.toArray(new Channel[channels.size()]));

    searchSub.setFrequency(convertFrequency(bean.getFrequency()));
    searchSub.setName(bean.getName());
    searchSub.setTitle(bean.getTitle());

    SubscriptionOwner owner = bean.getOwner();
    if (owner.getType().equals(VeloConstants.SUBSCRIPTION_OWNER_ACCOUNT_USER)) {
      String username = owner.getId();
      IUser user = ResourcesPlugin.getSecurityManager().getUser(username);
      searchSub.setUser(user);
    }

    return searchSub;
  }

  /**
   * Converts an <code>ISubscription[]</code> to its web service <code>Subscription</code> equivalent.
  
  
   * @param subscriptions ISubscription[]
   * @return Subscription[]
   */
  private Subscription[] convert(ISubscription... subscriptions) {
    Subscription[] subscriptionBeans = new Subscription[subscriptions.length];

    for (int i = 0; i < subscriptionBeans.length; i++) {
      ISubscription subscription = subscriptions[i];
      Subscription subscriptionBean = new Subscription();

      // set the node attribute if a uuid is supplied
      if (subscription.getId() != null) {
        subscriptionBean.setNode(AlfrescoUtils.getReference(subscription.getId()));
      }

      subscriptionBean.setCreated(subscription.getCreated());

      Collection<NamedValue> props = new ArrayList<NamedValue>();

      if (subscription instanceof SearchSubscription) {
        String query = ((SearchSubscription) subscription).getQuery();

        subscriptionBean.setType(VeloConstants.TYPE_SUBSCRIPTION_REPOSITORY_SEARCH);
        props.add(new NamedValue(VeloConstants.PROP_SUB_SEARCH_QUERY, false, query, null));

        Trigger[] triggers = ((SearchSubscription) subscription).getTriggers();
        Collection<String> triggerConstants = new ArrayList<String>();

        for (Trigger trigger : triggers) {
          switch (trigger) {
            case NEW:
              triggerConstants.add(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_NEW);
              break;
            case MODIFIED:
              triggerConstants.add(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_MODIFIED);
              break;
            case DELETED:
              // deleted won't work, so disallow it
              throw new RuntimeException("Invalid trigger: DELETED");
              //            triggerConstants.add(VeloConstants.SUBSCRIPTION_CHANGE_TYPE_DELETED);
              //            break;
          }
        }

        props.add(new NamedValue(
            VeloConstants.PROP_SUB_SEARCH_CHANGE_TYPE,
            true,
            null, 
            triggerConstants.toArray(new String[triggerConstants.size()])));

      } else if(subscription instanceof RepositorySubscription){
    	  subscriptionBean.setType(AlertingConstants.TYPE_SUBSCRIPTION_REPOSITORY);
    	  
    	  ChangeType[] changeTypes = ((RepositorySubscription) subscription).getChangeTypes();
    	  Collection<String> changeTypeContstants = new ArrayList<String>();
    	  
    	  for(ChangeType changeType : changeTypes){
    		  switch(changeType){
    		  case NEW:
    			  changeTypeContstants.add(AlertingConstants.CHANGE_TYPE_NEW);
    			  break;
    		  case MODIFIED:
    			  changeTypeContstants.add(AlertingConstants.CHANGE_TYPE_MODIFIED);
    			  break;
    		  case DELETED:
    			  changeTypeContstants.add(AlertingConstants.CHANGE_TYPE_DELETED);
    			  break;
    		  }
    	  }
    	  
    	  props.add(new NamedValue(
    		AlertingConstants.PROP_SUB_REP_CHANGE_TYPE,
    		true,
    		null,
    		changeTypeContstants.toArray(new String[changeTypeContstants.size()])
    	  ));
    	  
    	  if(((RepositorySubscription) subscription).getResource() != null){
    		  Reference ref = AlfrescoUtils.getReference(((RepositorySubscription) subscription).getResource().getPropertyAsString(VeloConstants.PROP_UUID));
    		  String nodeRef = ref.getStore().getScheme() + "://" + ref.getStore().getAddress() + "/" + ref.getUuid();
    		  
    		  props.add(new NamedValue(
    				  AlertingConstants.PROP_SUB_REP_SUBSCRIPTION_NODE,
    				  false,
    				  nodeRef,
    				  null
    		  ));
    	  }
    	  
    	  if(((RepositorySubscription) subscription).getResource() != null && ((RepositorySubscription) subscription).getResource().isType(IResource.FOLDER)){
    		  props.add(new NamedValue(
    				  AlertingConstants.PROP_SUB_REP_INCLUDE_CHILDREN,
    				  false,
    				  Boolean.toString(true),
    				  null
    		  ));
    	  }else if(((RepositorySubscription) subscription).getResource() != null && ((RepositorySubscription) subscription).getResource().isType(IResource.FILE)){
          props.add(new NamedValue(
              AlertingConstants.PROP_SUB_REP_INCLUDE_CHILDREN,
              false,
              Boolean.toString(false),
              null
          ));
        }
      }else{
    	  throw new RuntimeException("Unexpected subscription type: " + subscription.getClass());
      }

      subscriptionBean.setName(subscription.getName());
      subscriptionBean.setTitle(subscription.getTitle());
      subscriptionBean.setFrequency(convertFrequency(subscription.getFrequency()));

      subscriptionBean.setProperties(props.toArray(new NamedValue[props.size()]));

      Channel[] channels = subscription.getChannels();
      Collection<String> channelConstants = new ArrayList<String>();

      for (Channel channel : channels) {
        switch (channel) {
          case EMAIL:
            channelConstants.add(VeloConstants.SUBSCRIPTION_CHANNEL_EMAIL);
            break;
          case REPOSITORY:
            channelConstants.add(VeloConstants.SUBSCRIPTION_CHANNEL_REPOSITORY);
            break;
        }
      }

      subscriptionBean.setDeliveryChannel(channelConstants.toArray(new String[channelConstants.size()]));

      SubscriptionOwner owner = new SubscriptionOwner();

      owner.setId(subscription.getUser().getUsername());
      owner.setType(VeloConstants.SUBSCRIPTION_OWNER_ACCOUNT_USER);
      subscriptionBean.setOwner(owner);

      subscriptionBeans[i] = subscriptionBean;
    }

    return subscriptionBeans;
  }
}
