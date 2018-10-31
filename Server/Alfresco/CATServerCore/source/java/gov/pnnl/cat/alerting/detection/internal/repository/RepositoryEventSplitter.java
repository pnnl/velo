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
package gov.pnnl.cat.alerting.detection.internal.repository;

import gov.pnnl.cat.jms.common.AbstractMessageHandler;
import gov.pnnl.cat.jms.common.MessagePayload;
import gov.pnnl.cat.jms.common.RepositoryEventMessageSender;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.transaction.TransactionService;

/**
 * Needs to listen to JMS repository events which
 * are returned as an RepositoryEventList, and send
 * each one out individually so they can be filtered
 * appropriately by the alert subscription.
 * 
 * Since raw JMS messages will include an entry for every 
 * property changed, need to filter out redundant events.
 * 
 * This listener should be bound using Jencks via Spring
 * @version $Revision: 1.0 $
 */
public class RepositoryEventSplitter extends AbstractMessageHandler {

	private RepositoryEventMessageSender messageSender;
	private NodeService nodeService;
	private NamespacePrefixResolver namespacePrefixResolver;
	private TransactionService transactionService;
	
	
	/**
	 * Method setTransactionService.
	 * @param transactionService TransactionService
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Method setNamespacePrefixResolver.
	 * @param namespacePrefixResolver NamespacePrefixResolver
	 */
	public void setNamespacePrefixResolver(
			NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	/**
	 * Method setMessageSender.
	 * @param messageSender RepositoryEventMessageSender
	 */
	public void setMessageSender(RepositoryEventMessageSender messageSender) {
		this.messageSender = messageSender;
	}


	/**
	 * Method onMessage.
	 * @param payload MessagePayload
	 * @param rawMessage Message
	 */
	@Override
	public void onMessage(MessagePayload payload, Message rawMessage) {
		// make sure we have a valid message
		if ((payload instanceof RepositoryEventMessage) == false) {
			return;
		}
		RepositoryEventMessage messageIn = (RepositoryEventMessage)payload;
		RepositoryEventList eventList = messageIn.getEvents();
		if (eventList == null || eventList.size() == 0) {
			return;
		}
	

		// iterate through the incoming message, create a new message for each event
		// send them out individually.  Filter out any messages that are redundant
		Map<String, Object> redundantMessageCheck = new HashMap<String, Object>();
		for (RepositoryEvent event : eventList) {
			String nodeid = event.getNodeId();
			String eventType = event.getClass().getName(); // the event type is the classname
			String key = nodeid + ":" + eventType;
			if(event.getPropertyName() != null) {
				key += ":" + event.getPropertyName();
			}
			
			// TODO: we might need something more sophisticated
			// like looking for certain properties (mod date, content node)
			if (redundantMessageCheck.containsKey(key) == false) {		
				RepositoryEventMessage newMessage = new RepositoryEventMessage();
				RepositoryEventList newEventList = new RepositoryEventList();
				newEventList.add(event);
				newMessage.setEvents(newEventList);
				String shortPath = event.getNodePath(); //getShortPath(new NodeRef(CatConstants.SPACES_STORE, nodeid));
				newMessage.setNodePath(shortPath);
				
				messageSender.sendMessage(newMessage);
				
				redundantMessageCheck.put(key, new Object());
			}
		}
		
	}

  /**
   * Method onMessage.
   * @param message Message
   * @see javax.jms.MessageListener#onMessage(Message)
   */
  @Override
  public void onMessage(Message message) {
    onMessage(message, RepositoryEventMessage.class);
    
  }


}
