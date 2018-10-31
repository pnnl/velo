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
/**
 * 
 */
package gov.pnnl.cat.alerting.alerts.internal;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Actor;
import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.RepositoryAlert;
import gov.pnnl.cat.alerting.alerts.TemporaryAlert;
import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

/**
 * Implementation of the AlertManagementService interface.
 * @version $Revision: 1.0 $
 */
public class AlertManagementServiceImpl extends ExtensiblePolicyAdapter implements AlertManagementService {

	private int maxAlerts;
	private ContentService publicContentService;

	/**
	 * Required by ExtensiblePolicyAdapter
	 */
	public void init() {
	}


	/**
	 * Method setPublicContentService.
	 * @param publicContentService ContentService
	 */
	public void setPublicContentService(ContentService publicContentService) {
		this.publicContentService = publicContentService;
	}


	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#areAlertsMaxed(java.lang.String)
	 */
	public boolean areAlertsMaxed(String username) {
		NodeRef userAlertsFolder = getUserAlertsFolder(username);
		
		List<RepositoryAlert> alerts = new ArrayList<RepositoryAlert>();
		List<ChildAssociationRef> children = nodeService.getChildAssocs(userAlertsFolder);
		
		return (children.size() >= maxAlerts);
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#createAlert(gov.pnl.dmi.alerting.alerts.Alert)
	 */
	public RepositoryAlert createAlert(Alert alert) {

		//TODO: how to handle other recipients on this alert?
		String username = alert.getRecipients().get(0).getAccountId();
		RepositoryAlertImpl repAlert = new RepositoryAlertImpl();
		repAlert.setAlertManagementService(this);
		repAlert.setContentService(publicContentService);

		// set the default properties for this Repository Alert
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_TITLE, alert.getTitle());
		properties.put(AlertingConstants.PROP_ALERT_SUMMARY, alert.getSummary());
		properties.put(AlertingConstants.PROP_ALERT_SUBSCRIPTION_TYPE, alert.getSubscriptionType());
		properties.put(AlertingConstants.PROP_ALERT_SENDER, alert.getSender().getAccountId());
		properties.put(AlertingConstants.PROP_ALERT_FREQUENCY, alert.getFrequency().toString());
		properties.put(AlertingConstants.PROP_ALERT_WAS_READ, Boolean.FALSE);
		
		List<String> recipients = new ArrayList<String>();
		for (Actor actor : alert.getRecipients()) {
			recipients.add(actor.getAccountId());
		}
		properties.put(AlertingConstants.PROP_ALERT_RECIPIENTS, (Serializable)recipients);
		
		// set all of the properties from the alert object

		NodeRef alertsFolder = getUserAlertsFolder(username);
		String alertName = alert.getTitle() + " " + System.currentTimeMillis();


		NodeRef alertNode = nodeService.createNode(
				alertsFolder,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(AlertingConstants.NAMESPACE_ALERT, alertName),
				AlertingConstants.TYPE_ALERT,
				properties).getChildRef();

		repAlert.setNodeRef(alertNode);
		repAlert.setProperties(properties);

		repAlert.setEvents(this.getEvents(alert));

		return repAlert;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#deleteAlerts(java.util.List)
	 */
	public void deleteAlerts(List<RepositoryAlert> alerts) {
		for (RepositoryAlert alert : alerts) {
			NodeRef alertNodeRef = alert.getNodeRef();
			nodeService.deleteNode(alertNodeRef);
		}
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#getAlerts()
	 */
	public List<RepositoryAlert> getAlerts() {
		// perform a search for all nodes with alrt:alert type
	  ResultSet results = null;
	  try {
	    String query = "TYPE:\"" + AlertingConstants.TYPE_ALERT.toString() + "\"";
	    results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());

	    List<NodeRef> alertNodeRefs = results.getNodeRefs();
	    List<RepositoryAlert> alerts = new ArrayList<RepositoryAlert>();
	    for (NodeRef nodeRef : alertNodeRefs) {
	      RepositoryAlert repositoryAlert = getRepositoryAlert(nodeRef);
	      alerts.add(repositoryAlert);
	    }
	    return alerts;
	  } finally {
	    if(results != null) {
	      results.close();
	    }
	  }
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#getAlerts(java.lang.String)
	 */
	public List<RepositoryAlert> getAlerts(String username) {
		NodeRef userAlertsFolder = getUserAlertsFolder(username);
		
		List<RepositoryAlert> alerts = new ArrayList<RepositoryAlert>();
		List<ChildAssociationRef> children = nodeService.getChildAssocs(userAlertsFolder);
		for (ChildAssociationRef child : children) {
			NodeRef repositoryAlertNodeRef = child.getChildRef();
			RepositoryAlert repositoryAlert = getRepositoryAlert(repositoryAlertNodeRef);
			alerts.add(repositoryAlert);
		}
		return alerts;
	}
	
	/**
	 * Method getRepositoryAlert.
	 * @param repositoryAlertNodeRef NodeRef
	 * @return RepositoryAlert
	 */
	private RepositoryAlert getRepositoryAlert(NodeRef repositoryAlertNodeRef) {
		Map<QName, Serializable> properties = nodeService.getProperties(repositoryAlertNodeRef);
		RepositoryAlertImpl repositoryAlert = new RepositoryAlertImpl(repositoryAlertNodeRef, properties, this, publicContentService);
		return repositoryAlert;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#getMaxAlerts()
	 */
	public int getMaxAlerts() {
		return maxAlerts;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#getRssUrl(java.lang.String)
	 */
	public URL getRssUrl(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#setAlertTimeToLive(int)
	 */
	public void setAlertTimeToLive(int timeToLive) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see gov.pnl.dmi.alerting.alerts.AlertManagementService#setMaxAlerts(int)
	 */
	public void setMaxAlerts(int maxAlerts) {
		this.maxAlerts = maxAlerts;
	}

	/**
	 * Method newTemporaryAlert.
	 * @return TemporaryAlert
	 * @see gov.pnnl.cat.alerting.alerts.AlertManagementService#newTemporaryAlert()
	 */
	public TemporaryAlert newTemporaryAlert() {
		return new TemporaryAlertImpl();
	}

	/**
	 * Method newEvent.
	 * @return Event
	 * @see gov.pnnl.cat.alerting.alerts.AlertManagementService#newEvent()
	 */
	public Event newEvent() {
		return new EventImpl();
	}

	/**
	 * Method newAlert.
	 * @return Alert
	 * @see gov.pnnl.cat.alerting.alerts.AlertManagementService#newAlert()
	 */
	public Alert newAlert() {
		return new AlertImpl();
	}

	/**
	 * Get the Company Home/Subscriptions folder.  Create if it doesn't exist.
	
	 * @return NodeRef
	 */
	private NodeRef getAlertsFolder() {
		NodeRef companyHomeNode = nodeUtils.getCompanyHome();
		NodeRef alertsNode = nodeUtils.getChildByName(companyHomeNode, AlertingConstants.NAME_ALERTS_FOLDER.getLocalName());

		if (alertsNode == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, AlertingConstants.NAME_ALERTS_FOLDER.getLocalName());

			String currentUser = authenticationComponent.getCurrentUserName();

			// switch to admin privs
			AuthenticationUtil.setRunAsUserSystem();

			alertsNode = nodeService.createNode(
					companyHomeNode,
					ContentModel.ASSOC_CONTAINS,
					AlertingConstants.NAME_ALERTS_FOLDER,
					ContentModel.TYPE_SYSTEM_FOLDER,
					properties).getChildRef();

			// go back to user privs
			if (currentUser != null) {
				AuthenticationUtil.setRunAsUser(currentUser);
			}
		}

		return alertsNode;
	}



	/**
	 * Method getUserAlertsFolder.
	 * @param username String
	 * @return NodeRef
	 */
	private NodeRef getUserAlertsFolder(String username) {
		NodeRef alertsFolder = getAlertsFolder();
		NodeRef userAlertsNode = nodeUtils.getChildByName(alertsFolder, username);

		if (userAlertsNode == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, username);

			String currentUser = authenticationComponent.getCurrentUserName();
			// switch to admin privs
			AuthenticationUtil.setRunAsUserSystem();

			userAlertsNode = nodeService.createNode(
					alertsFolder,
					ContentModel.ASSOC_CONTAINS,
					QName.createQName(AlertingConstants.NAMESPACE_ALERT, username),
					ContentModel.TYPE_SYSTEM_FOLDER,
					properties).getChildRef();
			
			permissionService.setPermission(userAlertsNode, username, PermissionService.ALL_PERMISSIONS, true);
			permissionService.setInheritParentPermissions(userAlertsNode, false);

			// go back to user privs
			if (currentUser != null) {
				AuthenticationUtil.setRunAsUser(currentUser);
			}
		}

		return userAlertsNode;

	}




	/**
	 * Method getEvents.
	 * @param alert Alert
	 * @return List<Event>
	 * @see gov.pnnl.cat.alerting.alerts.AlertManagementService#getEvents(Alert)
	 */
	public List<Event> getEvents(Alert alert) {
		if (alert instanceof RepositoryAlert) {
			RepositoryAlertImpl impl = (RepositoryAlertImpl)alert;
			return impl.getEvents();
		} else if (alert instanceof AlertImpl) {
			AlertImpl impl = (AlertImpl)alert;
			return impl.getEvents();
		}
		return null;
	}
	
	/**
	 * Method setAlertRead.
	 * @param repositoryAlert RepositoryAlert
	 * @param alertRead boolean
	 * @see gov.pnnl.cat.alerting.alerts.AlertManagementService#setAlertRead(RepositoryAlert, boolean)
	 */
	public void setAlertRead(RepositoryAlert repositoryAlert, boolean alertRead) {
		RepositoryAlertImpl repAlertImpl = (RepositoryAlertImpl)repositoryAlert;
		repAlertImpl.setAlertRead(alertRead);
	}
	
	/**
	 * Method setAlertRead.
	 * @param nodeRef NodeRef
	 * @param alertRead boolean
	 * @see gov.pnnl.cat.alerting.alerts.AlertManagementService#setAlertRead(NodeRef, boolean)
	 */
	public void setAlertRead(NodeRef nodeRef, boolean alertRead) {
		nodeService.setProperty(nodeRef, AlertingConstants.PROP_ALERT_WAS_READ, new Boolean(alertRead));
	}
	


}
