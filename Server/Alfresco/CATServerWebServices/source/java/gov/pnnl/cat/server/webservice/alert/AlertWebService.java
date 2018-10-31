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
package gov.pnnl.cat.server.webservice.alert;

import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.internal.RepositoryAlertImpl;
import gov.pnnl.cat.server.webservice.subscription.SubscriptionFault;
import gov.pnnl.cat.server.webservice.util.ExceptionUtils;
import gov.pnnl.cat.util.CatConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class AlertWebService extends AbstractWebService implements AlertServiceSoapPort {
  private static Log logger = LogFactory.getLog(AlertWebService.class);
	private AlertManagementService alertService;
	private TransactionService transactionService;
	private PermissionService permissionService;
  
  /**
   * Method setPermissionService.
   * @param permissionService PermissionService
   */
  public void setPermissionService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

	/**
	 * Method setAlertService.
	 * @param alertService AlertManagementService
	 */
	public void setAlertService(AlertManagementService alertService) {
		this.alertService = alertService;
	}

	// web service method
	/**
	 * Method deleteAlerts.
	 * @param wsRepoAlerts RepositoryAlert[]
	 * @throws RemoteException
	 * @throws AlertFault
	 * @see gov.pnnl.cat.server.webservice.alert.AlertServiceSoapPort#deleteAlerts(RepositoryAlert[])
	 */
	public void deleteAlerts(final RepositoryAlert[] wsRepoAlerts) throws RemoteException,
			AlertFault {
	  
	   // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
              List<gov.pnnl.cat.alerting.alerts.RepositoryAlert> repositoryAlerts = fromWebServiceRepositoryAlertArray(wsRepoAlerts);
              alertService.deleteAlerts(repositoryAlerts);
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

	// web service method
	/**
	 * Method getAlerts.
	 * @return RepositoryAlert[]
	 * @throws RemoteException
	 * @throws AlertFault
	 * @see gov.pnnl.cat.server.webservice.alert.AlertServiceSoapPort#getAlerts()
	 */
	public RepositoryAlert[] getAlerts() throws RemoteException, AlertFault {

    // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<RepositoryAlert[]> callback = new RetryingTransactionCallback<RepositoryAlert[]>()
        {
            public RepositoryAlert[] execute() throws Exception
            {
              List<gov.pnnl.cat.alerting.alerts.RepositoryAlert> repoAlerts = alertService.getAlerts();
              RepositoryAlert[] wsRepoAlerts = toWebServiceRepositoryAlertArray(repoAlerts);
              return wsRepoAlerts;
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

	// web service method
	/**
	 * Method markAlertsAsRead.
	 * @param wsRepoAlerts RepositoryAlert[]
	 * @param isRead Boolean
	 * @throws RemoteException
	 * @throws AlertFault
	 * @see gov.pnnl.cat.server.webservice.alert.AlertServiceSoapPort#markAlertsAsRead(RepositoryAlert[], Boolean)
	 */
	public void markAlertsAsRead(final RepositoryAlert[] wsRepoAlerts, final Boolean isRead)
			throws RemoteException, AlertFault {
	  
    // Wrap in a retrying transaction handler in case of db deadlock
    try
    {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
              for (RepositoryAlert wsRepoAlert : wsRepoAlerts) {
                gov.pnnl.cat.alerting.alerts.RepositoryAlert repositoryAlert = fromWebServiceRepositoryAlert(wsRepoAlert);
                alertService.setAlertRead(repositoryAlert, isRead.booleanValue());
              }
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
	
	// utility methods below
	/** convert a RepositoryAlert to a web service RepositoryAlert * @param repositoryAlert gov.pnnl.cat.alerting.alerts.RepositoryAlert
	 * @return RepositoryAlert
	 */
	private RepositoryAlert toWebServiceRepositoryAlert(gov.pnnl.cat.alerting.alerts.RepositoryAlert repositoryAlert) {
		RepositoryAlert wsRepoAlert = new RepositoryAlert();
		
		wsRepoAlert.setAlertRead(repositoryAlert.isAlertRead());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(repositoryAlert.getCreated());
		wsRepoAlert.setCreated(calendar);
		
		Event[] wsEvents = toWebServiceEventList(repositoryAlert.getEvents());
		wsRepoAlert.setEvents(wsEvents);
		
		wsRepoAlert.setFrequency(repositoryAlert.getFrequency().toString());
		wsRepoAlert.setName(repositoryAlert.getName());
		
		Reference ref = Utils.convertToReference(nodeService, namespaceService, repositoryAlert.getNodeRef());
		ref.setPath(nodeService.getPath(repositoryAlert.getNodeRef()).toString());
		wsRepoAlert.setNode(ref);

		wsRepoAlert.setRecipients(toWebServiceActorArray(repositoryAlert.getRecipients()));		
		wsRepoAlert.setSender(toWebServiceActor(repositoryAlert.getSender()));
		wsRepoAlert.setSubscriptionType(repositoryAlert.getSubscriptionType().toString());
		
		wsRepoAlert.setSummary(repositoryAlert.getSummary());
		wsRepoAlert.setTitle(repositoryAlert.getTitle());
		
		return wsRepoAlert;
	}
	
	
	/** convert a web service RepositoryAlert array to a RepositoryAlert list * @param wsAlerts RepositoryAlert[]
	 * @return List<gov.pnnl.cat.alerting.alerts.RepositoryAlert>
	 */
	private List<gov.pnnl.cat.alerting.alerts.RepositoryAlert> fromWebServiceRepositoryAlertArray(RepositoryAlert[] wsAlerts) {
		List<gov.pnnl.cat.alerting.alerts.RepositoryAlert> repoAlerts = new ArrayList<gov.pnnl.cat.alerting.alerts.RepositoryAlert>();
		for (RepositoryAlert wsRepoAlert : wsAlerts) {
			gov.pnnl.cat.alerting.alerts.RepositoryAlert repoAlert = fromWebServiceRepositoryAlert(wsRepoAlert);
			repoAlerts.add(repoAlert);
		}
		return repoAlerts;
	}
	
	/** convert a RepositoryAlert list to a web service RepositoryAlert array * @param repositoryAlerts List<gov.pnnl.cat.alerting.alerts.RepositoryAlert>
	 * @return RepositoryAlert[]
	 */
	private RepositoryAlert[] toWebServiceRepositoryAlertArray(List<gov.pnnl.cat.alerting.alerts.RepositoryAlert> repositoryAlerts) {
		RepositoryAlert[] wsRepoAlerts = new RepositoryAlert[repositoryAlerts.size()];
		for (int i=0; i<repositoryAlerts.size(); i++) {
			gov.pnnl.cat.alerting.alerts.RepositoryAlert repoAlert = repositoryAlerts.get(i);
			RepositoryAlert wsRepoAlert = toWebServiceRepositoryAlert(repoAlert);
			wsRepoAlerts[i] = wsRepoAlert;
		}
		return wsRepoAlerts;
	}
	
	/** convert a web service RepositoryAlert to a RepositoryAlert * @param wsAlert RepositoryAlert
	 * @return gov.pnnl.cat.alerting.alerts.RepositoryAlert
	 */
	private gov.pnnl.cat.alerting.alerts.RepositoryAlert fromWebServiceRepositoryAlert(final RepositoryAlert wsAlert) {
		
		if (wsAlert.getNode() != null) {
			NodeRef nodeRef = Utils.convertToNodeRef(wsAlert.getNode(), nodeService, searchService, namespaceService);
			
			// since the web service methods that require the creation of a RepositoryAlert object
			// are only dependent on the NodeRef stored within the RepositoryAlert object
			// we do not need to convert all of the properties and fields from the web service
			// object to a RepositoryAlert object.  Setting the NodeRef is sufficient
			RepositoryAlertImpl alert = new RepositoryAlertImpl(nodeRef, null, this.alertService, this.contentService);
			return alert;
		}
		return null;
	}
	
	/** convert a list of Events to a web service Event array * @param events List<gov.pnnl.cat.alerting.alerts.Event>
	 * @return Event[]
	 */
	private Event[] toWebServiceEventList(List<gov.pnnl.cat.alerting.alerts.Event> events) {
		Event[] wsEvents = new Event[events.size()];

		for (int i = 0; i < events.size(); i++) {
		
		  gov.pnnl.cat.alerting.alerts.Event event = events.get(i);
		  String uuid = event.getUUID();
		  NodeRef eventRef = new NodeRef(CatConstants.SPACES_STORE, uuid);
		  
		  Event wsEvent = new Event();
		  
		  // First check if the event document is valid
		  // Make sure it exists and that the user has read permissions on it
		  boolean valid = false;
		  if(nodeService.exists(eventRef)) {
		    AccessStatus access = permissionService.hasPermission(eventRef, PermissionService.READ);
		    if(access.equals(AccessStatus.ALLOWED)) {
		      valid = true;
		    }
		  }
		  wsEvent.setValid(valid);
			wsEvent.setChangeType(event.getChangeType());
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(event.getEventTime());
			wsEvent.setEventTime(calendar);
			
			wsEvent.setPerpetrator(toWebServiceActor(event.getEventPerpetrator()));
			wsEvent.setResourceName(event.getResourceName());
			if (event.getResourceURL() != null) {
				wsEvent.setResourceUrl(event.getResourceURL().toExternalForm());
			}
			wsEvent.setUuid(uuid);
			if(uuid != null && valid) {
			  wsEvent.setResourcePath(nodeService.getPath(eventRef).toString());
			}
			wsEvents[i] = wsEvent;
		}
		return wsEvents;
	}
	
	/** convert an Actor to a web service Actpr * @param actor gov.pnnl.cat.alerting.alerts.Actor
	 * @return Actor
	 */
	private Actor toWebServiceActor(gov.pnnl.cat.alerting.alerts.Actor actor) {
		Actor wsActor = new Actor();
		wsActor.setId(actor.getAccountId());
		wsActor.setType(actor.getAccountType());
		
		return wsActor;
	}
	
	/** convert a list of Actor objects to a web service Actor array * @param actorList List<gov.pnnl.cat.alerting.alerts.Actor>
	 * @return Actor[]
	 */
	private Actor[] toWebServiceActorArray(List<gov.pnnl.cat.alerting.alerts.Actor> actorList) {
		Actor[] wsActors = new Actor[actorList.size()];
		for (int i=0; i<actorList.size(); i++) {
			gov.pnnl.cat.alerting.alerts.Actor actor = actorList.get(i);
			Actor wsActor = toWebServiceActor(actor);
			wsActors[i] = wsActor;
		}
		return wsActors;
	}



}
