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
package gov.pnnl.cat.policy.notifiable.filter;

import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;
import gov.pnnl.cat.util.CatConstants;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class SecureUserEventFilterFunctionality extends AbstractFilterFunctionality {

	private static final Log logger = LogFactory.getLog(SecureUserEventFilterFunctionality.class);

	private NodeService nodeService;
	private AuthenticationComponent authenticationComponent = null;
	private TransactionService transactionService;
	private PermissionService permissionService;
	private NamespacePrefixResolver namespacePrefixResolver;
	private boolean generateSpecialParentRefreshEvents = false;


	/**
	 * Method setNamespacePrefixResolver.
	 * @param namespacePrefixResolver NamespacePrefixResolver
	 */
	public void setNamespacePrefixResolver(
			NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	/**
	 * Method setGenerateSpecialParentRefreshEvents.
	 * @param generateSpecialParentRefreshEvents boolean
	 */
	public void setGenerateSpecialParentRefreshEvents(
			boolean generateSpecialParentRefreshEvents) {
		this.generateSpecialParentRefreshEvents = generateSpecialParentRefreshEvents;
	}

	/**
	 * Method setAuthenticationComponent.
	 * @param authComponent AuthenticationComponent
	 */
	public void setAuthenticationComponent(AuthenticationComponent authComponent) {
		this.authenticationComponent = authComponent;
	}

	/**
	 * Method setTransactionService.
	 * @param service TransactionService
	 */
	public void setTransactionService(TransactionService service) {
		this.transactionService = service;
	}

	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Method setPermissionService.
	 * @param permissionService PermissionService
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * Method filterMessage.
	 * @param rawMessage RepositoryEventMessage
	 * @return RepositoryEventMessage
	 * @see gov.pnnl.cat.policy.notifiable.filter.IFilterFunctionality#filterMessage(RepositoryEventMessage)
	 */
	public RepositoryEventMessage filterMessage(RepositoryEventMessage rawMessage) {

    long start = System.currentTimeMillis();
		SecureFilterTransactionWork work = new SecureFilterTransactionWork(this.getUser(), rawMessage);
		RepositoryEventMessage filteredMessage = transactionService.getRetryingTransactionHelper().doInTransaction(work, true);
		long end = System.currentTimeMillis();

    logger.debug("event filter took " + (end - start) + " ms  for " + rawMessage.hashCode());

    return filteredMessage;
	}



	/**
	 */
	public class SecureFilterTransactionWork implements RetryingTransactionCallback<RepositoryEventMessage> {
		private String username;
		private RepositoryEventMessage rawMessage;

		/**
		 * Constructor for SecureFilterTransactionWork.
		 * @param username String
		 * @param rawMessage RepositoryEventMessage
		 */
		public SecureFilterTransactionWork(String username, RepositoryEventMessage rawMessage) {
			this.username = username;
			this.rawMessage = rawMessage;
		}


		/**
		 * Method execute.
		 * @return RepositoryEventMessage
		 * @throws Throwable
		 * @see org.alfresco.repo.transaction.RetryingTransactionHelper$RetryingTransactionCallback#execute()
		 */
		public RepositoryEventMessage execute() throws Throwable {

		    RepositoryEventMessage filteredMessage = new RepositoryEventMessage();
		    filteredMessage.setProperties(rawMessage.getProperties());
		    
		    if (username == null) {
		    	// why this would happen, I don't know
		    	// but at least we won't have a NullPointerException
		    	// Dave Gillen Apr-7-2011
		    	return filteredMessage;
		    }
		    
			Set<NodeRef> modifiedParents = new HashSet<NodeRef>();
		  try {
		    // execute this with the context of the user who has the subscription
		    AuthenticationUtil.setRunAsUser(username);
		    
		    // Note: Since 3.0 - this method tries to run in a write tx, so we don't want that
		    //AuthenticationUtil.setRunAsUser(username);

		    RepositoryEventList filteredEventList = new RepositoryEventList();
		    filteredMessage.setEvents(filteredEventList);

		    // step through each event, check if current user has read access to this node
		    RepositoryEventList rawEventList = rawMessage.getEvents();
		    for (RepositoryEvent event : rawEventList) {
		      String nodeId = event.getNodeId();
		      NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, nodeId);

		      AccessStatus permission = permissionService.hasPermission(nodeRef, PermissionService.READ);
		      if (permission.equals(AccessStatus.ALLOWED)) {
		        filteredEventList.add(event);
		      } else {
		      	// denied access to this node.
		      	if (generateSpecialParentRefreshEvents) {
		      		// if we are generating the special unauth parent events, do the following:
		      		// See if we can access the parent of this node
		      		// if so, send an event that requests clients to refresh the parent
		      		// this will make any unauthorized child nodes "disappaear"
		      		NodeRef parentNode = nodeService.getPrimaryParent(nodeRef).getParentRef();
		      		permission = permissionService.hasPermission(parentNode, PermissionService.READ);
		      		if (permission.equals(AccessStatus.ALLOWED)) {
		      			if (modifiedParents.contains(parentNode) == false) {
		      			  RepositoryEvent propChanged = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED);
		      				propChanged.setEventPerpetrator(event.getEventPerpetrator());
		      				propChanged.setEventTimestamp(event.getEventTimestamp());
		      				propChanged.setNodeId(parentNode.getId());
		      				propChanged.setNodePath(nodeService.getPath(parentNode).toString());
		      				propChanged.setPropertyName(ContentModel.PROP_MODIFIED.toString());
		      				propChanged.setPropertyValue(new Date().toString());
		      				filteredMessage.getEvents().add(propChanged);
		      				modifiedParents.add(parentNode);
		      			}
		      		}
		      	}
		      }

		      // else skip the event since we're not authorized
		      // TODO: if a node no longer exists, we are sending events for it
		      // this is good for valid deletes, but not for cbc taxonomy auto-categorization
		      // since a node is created then immediately deleted, and events are sent
		      // for these.
		    }
		    	      
		    return filteredMessage;

		  } catch (Exception e) {
		    // an exception occurred, return the raw event list
		    logger.error("Unable to filter events", e);
		    return rawMessage;
		  }
		}

	}




}
