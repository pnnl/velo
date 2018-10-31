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
package gov.pnnl.cat.server.webservice.subscription;

import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionService;
import gov.pnnl.cat.server.webservice.util.ExceptionUtils;
import gov.pnnl.cat.server.webservice.util.NodeClassConverter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.PropertyDefinition;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class SubscriptionWebService extends AbstractWebService implements SubscriptionServiceSoapPort {

	/** inject with Spring */
	private SubscriptionService subscriptionService;
	private DictionaryService dictionaryService;
	private TransactionService transactionService;
	
	private static Log logger = LogFactory.getLog(SubscriptionWebService.class);
	
	
	/**
	 * Method setDictionaryService.
	 * @param dictionaryService DictionaryService
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * Method setSubscriptionService.
	 * @param subscriptionService SubscriptionService
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	/**
   * @param transactionService the transactionService to set
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
	 * Create a new subscription
	 * @param subscription Subscription
   * @throws RemoteException
   * @throws SubscriptionFault
   * @see gov.pnnl.cat.server.webservice.subscription.SubscriptionServiceSoapPort#createSubscription(Subscription)
   */
	public void createSubscription(final Subscription subscription) throws RemoteException, SubscriptionFault {
	  
	  // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
              gov.pnnl.cat.alerting.subscriptions.Subscription sub = fromWebServiceSubscription(subscription);
              subscriptionService.createSubscription(sub);
              return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
    }
    catch (Throwable exception)
    {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new SubscriptionFault(0, rootCause.toString());
    }
	  
	}

	/**
	 * Delete a subscription
	 * @param subscriptions Subscription[]
	 * @throws RemoteException
	 * @throws SubscriptionFault
	 * @see gov.pnnl.cat.server.webservice.subscription.SubscriptionServiceSoapPort#deleteSubscriptions(Subscription[])
	 */
	public void deleteSubscriptions(final Subscription[] subscriptions) throws RemoteException, SubscriptionFault {
	  
	   // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
              List<gov.pnnl.cat.alerting.subscriptions.Subscription> subs = new ArrayList<gov.pnnl.cat.alerting.subscriptions.Subscription>();
              for (Subscription subscription : subscriptions) {
                gov.pnnl.cat.alerting.subscriptions.Subscription sub = fromWebServiceSubscription(subscription);
                subs.add(sub);
              }
              subscriptionService.deleteSubscriptions(subs);
              return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
    }
    catch (Throwable exception)
    {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new SubscriptionFault(0, rootCause.toString());
    }
	}

	/**
	 * Get all registered delivery channels
	 * @return DeliveryChannel[]
	 * @throws RemoteException
	 * @throws SubscriptionFault
	 * @see gov.pnnl.cat.server.webservice.subscription.SubscriptionServiceSoapPort#getDeliveryChannels()
	 */
	public DeliveryChannel[] getDeliveryChannels() throws RemoteException, SubscriptionFault {
	  
	   // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<DeliveryChannel[]> callback = new RetryingTransactionCallback<DeliveryChannel[]>()
        {
            public DeliveryChannel[] execute() throws Exception
            {
              List<gov.pnnl.cat.alerting.delivery.DeliveryChannel> channels = subscriptionService.getDeliveryChannels();
              return toWebServiceDeliveryChannels(channels);
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
    }
    catch (Throwable exception)
    {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new SubscriptionFault(0, rootCause.toString());
    }
	}

	/**
	 * Get all registered subscription types
	 * @return SubscriptionType[]
	 * @throws RemoteException
	 * @throws SubscriptionFault
	 * @see gov.pnnl.cat.server.webservice.subscription.SubscriptionServiceSoapPort#getSubscriptionTypes()
	 */
	public SubscriptionType[] getSubscriptionTypes() throws RemoteException, SubscriptionFault {
	  
    // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<SubscriptionType[]> callback = new RetryingTransactionCallback<SubscriptionType[]>()
        {
            public SubscriptionType[] execute() throws Exception
            {
              List<gov.pnnl.cat.alerting.subscriptions.SubscriptionType> types = subscriptionService.getSubscriptionTypes();
              return toWebServiceSubscriptionTypes(types);
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
    }
    catch (Throwable exception)
    {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new SubscriptionFault(0, rootCause.toString());
    }
	  
	}

	/** 
	 * get all subscriptions the current user has access to 
	 * @return Subscription[]
	 * @throws RemoteException
	 * @throws SubscriptionFault
	 * @see gov.pnnl.cat.server.webservice.subscription.SubscriptionServiceSoapPort#getSubscriptions()
	 */
	public Subscription[] getSubscriptions() throws RemoteException, SubscriptionFault {
	  
    // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<Subscription[]> callback = new RetryingTransactionCallback<Subscription[]>()
        {
            public Subscription[] execute() throws Exception
            {
              List<gov.pnnl.cat.alerting.subscriptions.Subscription> subs = subscriptionService.getSubscriptions();
              Subscription[] wsSubs = new Subscription[subs.size()];
              for (int i=0; i<subs.size(); i++) {
                Subscription wsSub = toWebServiceSubscription(subs.get(i));
                wsSubs[i] = wsSub;
              }
              return wsSubs;
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
    }
    catch (Throwable exception)
    {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new SubscriptionFault(0, rootCause.toString());
    }
	}

	/** 
	 * update an existing subscription 
	 * @param subscription Subscription
	 * @throws RemoteException
	 * @throws SubscriptionFault
	 * @see gov.pnnl.cat.server.webservice.subscription.SubscriptionServiceSoapPort#updateSubscription(Subscription)
	 */
	public void updateSubscription(final Subscription subscription) throws RemoteException, SubscriptionFault {
	  
    // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
              gov.pnnl.cat.alerting.subscriptions.Subscription sub = fromWebServiceSubscription(subscription);
              subscriptionService.updateSubscription(sub);
              return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
    }
    catch (Throwable exception)
    {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new SubscriptionFault(0, rootCause.toString());
    }
		
	}
	
	/** convert a DeliveryChannel to web service DeliveryChannel * @param channel gov.pnnl.cat.alerting.delivery.DeliveryChannel
	 * @return DeliveryChannel
	 */
	private DeliveryChannel toWebServiceDeliveryChannel(gov.pnnl.cat.alerting.delivery.DeliveryChannel channel) {
		DeliveryChannel wsChannel = new DeliveryChannel();
		wsChannel.setName(channel.getName().toString());
		wsChannel.setTitle(channel.getTitle());
		return wsChannel;
	}
	
	/** convert a List<DeliveryChannel> to a web service DeliveryChannel[] * @param channels List<gov.pnnl.cat.alerting.delivery.DeliveryChannel>
	 * @return DeliveryChannel[]
	 */
	private DeliveryChannel[] toWebServiceDeliveryChannels(List<gov.pnnl.cat.alerting.delivery.DeliveryChannel> channels) {
		DeliveryChannel[] wsChannels = new DeliveryChannel[channels.size()];
		for (int i=0; i<channels.size(); i++) {
			DeliveryChannel wsChannel = toWebServiceDeliveryChannel(channels.get(i));
			wsChannels[i] = wsChannel;
		}
		return wsChannels;
	}
	
	/** convert a generic String[] to a List<Qname> * @param stringList String[]
	 * @return List<QName>
	 */
	private List<QName> convertStringArrayToQNameList(String[] stringList) {
		List<QName> qnames = new ArrayList<QName>();
		for (String str : stringList) {
			qnames.add(QName.createQName(str));
		}
		return qnames;
	}
	
	/** convert a List<QName> to a String[] * @param qnames List<QName>
	 * @return String[]
	 */
	private String[] convertQNameListToStringArray(List<QName> qnames) {
		String[] array = new String[qnames.size()];
		for (int i=0; i<qnames.size(); i++) {
			QName qname = qnames.get(i);
			array[i] = qname.toString();
		}
		return array;
	}
	
	/** convert a SubscriptionType to a web service SubscriptionType * @param type gov.pnnl.cat.alerting.subscriptions.SubscriptionType
	 * @return SubscriptionType
	 */
	private SubscriptionType toWebServiceSubscriptionType(gov.pnnl.cat.alerting.subscriptions.SubscriptionType type) {
		SubscriptionType wsType = new SubscriptionType();
		String[] allowedFrequenciesArray = new String[type.getAllowedFrequencies().size()];
		for (int i=0; i<type.getAllowedFrequencies().size(); i++) {
			allowedFrequenciesArray[i] = type.getAllowedFrequencies().get(i).toString();
		}
		wsType.setAllowedFrequencies(allowedFrequenciesArray);
		wsType.setName(type.getName().toString());
		
		PropertyDefinition[] propDefs = new PropertyDefinition[type.getParameterDefinitions().size()];
        int pos = 0;
        for (org.alfresco.service.cmr.dictionary.PropertyDefinition ddPropDef : type.getParameterDefinitions().values())
        {
           PropertyDefinition propDef = Utils.setupPropertyDefObject(ddPropDef);
           propDefs[pos] = propDef;
           pos++;
        }
		wsType.setParameters(propDefs);

		return wsType;
	}
	
	/** convert a List<SubscriptionType> to a web service SubscriptionType[] * @param types List<gov.pnnl.cat.alerting.subscriptions.SubscriptionType>
	 * @return SubscriptionType[]
	 */
	private SubscriptionType[] toWebServiceSubscriptionTypes(List<gov.pnnl.cat.alerting.subscriptions.SubscriptionType> types) {
		SubscriptionType[] wsTypes = new SubscriptionType[types.size()];
		for (int i=0; i<types.size(); i++) {
			SubscriptionType wsType = toWebServiceSubscriptionType(types.get(i));
			wsTypes[i] = wsType;
		}
		return wsTypes;
	}
	
	/** convert a Subscription to a web service Subscription * @param subscription gov.pnnl.cat.alerting.subscriptions.Subscription
	 * @return Subscription
	 */
	private Subscription toWebServiceSubscription(gov.pnnl.cat.alerting.subscriptions.Subscription subscription) {
		Subscription wsSubscription = new Subscription();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(subscription.getCreated());
		wsSubscription.setCreated(calendar);
		
		List<QName> channelQNames = subscription.getDeliveryChannels();
		wsSubscription.setDeliveryChannel(convertQNameListToStringArray(channelQNames));
		
		wsSubscription.setFrequency(subscription.getFrequency().toString());
		
		calendar = Calendar.getInstance();
		calendar.setTime(subscription.getLastAlertSent());
		wsSubscription.setLastAlertSent(calendar);
		
		wsSubscription.setName(subscription.getName());
		
		Reference ref = Utils.convertToReference(nodeService, namespaceService, subscription.getNodeRef());
		wsSubscription.setNode(ref);
		
		SubscriptionOwner wsSubscriptionOwner = new SubscriptionOwner();
		gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner owner = subscription.getOwner();
		wsSubscriptionOwner.setId(owner.getAccountId());
		wsSubscriptionOwner.setType(owner.getAccountType());
		
		wsSubscription.setOwner(wsSubscriptionOwner);
		
		NamedValue[] properties = NodeClassConverter.convertPropertyMapToNamedValues(subscription.getParameters(), dictionaryService);
		wsSubscription.setProperties(properties);
		
		wsSubscription.setTitle(subscription.getTitle());
		
		gov.pnnl.cat.alerting.subscriptions.SubscriptionType type = subscriptionService.getSubscriptionType(subscription.getType());
		wsSubscription.setType(subscription.getType().toString());
		return wsSubscription;
	}
	
	/** convert a web service Subscription to a Subscription * @param wsSub Subscription
	 * @return gov.pnnl.cat.alerting.subscriptions.Subscription
	 */
	private gov.pnnl.cat.alerting.subscriptions.Subscription fromWebServiceSubscription(final Subscription wsSub) {
		QName subTypeQName = QName.createQName(wsSub.getType());
		gov.pnnl.cat.alerting.subscriptions.SubscriptionType subType = subscriptionService.getSubscriptionType(subTypeQName);
		gov.pnnl.cat.alerting.subscriptions.Subscription subscription = subscriptionService.newSubscriptionInstance(subType);
		
		List<QName> deliveryChannelQNames = convertStringArrayToQNameList(wsSub.getDeliveryChannel());
		subscription.setDeliveryChannels(deliveryChannelQNames);
		subscription.setFrequency(new Frequency(wsSub.getFrequency()));
		subscription.setName(wsSub.getName());
		
		if (wsSub.getNode() != null) {
			NodeRef nodeRef = Utils.convertToNodeRef(wsSub.getNode(), nodeService, searchService, namespaceService);
			subscription.setNodeRef(nodeRef);
		}
		
		PropertyMap parameters = NodeClassConverter.getPropertyMap(wsSub.getProperties(), dictionaryService);
		subscription.setParameters(parameters);
		
		subscription.setSubscriptionOwner(newSubscriptionOwner(wsSub.getOwner().getId(), wsSub.getOwner().getType()));
		subscription.setTitle(wsSub.getTitle());
		
		return subscription;
	}
	
	/** create a SubscriptionOwner object * @param name String
	 * @param type String
	 * @return gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner
	 */
	private gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner newSubscriptionOwner(final String name, final String type) {
		gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner owner = new gov.pnnl.cat.alerting.subscriptions.SubscriptionOwner() {

			public String getAccountId() {
				// TODO Auto-generated method stub
				return name;
			}

			public String getAccountType() {
				// TODO Auto-generated method stub
				return type;
			}
		};
		return owner;
	}
	
}
