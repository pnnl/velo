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
package gov.pnnl.cat.policy.notifiable;


import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.NotificationUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Aspect that enables a content item to be subscribed to. Upon update of the
 * item, all subscribers will be notified.
 * 
 * Note that this class does not cover adding or removing aspect events,
 * because aspect policy listeners must be bound to the aspect type, 
 * not the content node type.  This seems confusing to me.
 * 
 * All content nodes from the version store are completely ignored, so we don't spam the client.  
 * When new versions are created, the original node properties get updated, so the client will be notified,
 * so I don't see any reason to be sending additional version notifications to the client unless for some 
 * reason we start caching version nodes in the client cache.
 * 
 * @version $Revision: 1.0 $
 */
public class NotifiableAspectBehavior extends ExtensiblePolicyAdapter implements
NodeServicePolicies.OnUpdatePropertiesPolicy, 
NodeServicePolicies.BeforeDeleteNodePolicy,
NodeServicePolicies.OnCreateNodePolicy,
NodeServicePolicies.OnMoveNodePolicy,
NodeServicePolicies.OnCreateChildAssociationPolicy,
TransactionListener {

  // Logger
  private static final Log logger = LogFactory.getLog(NotifiableAspectBehavior.class);

  private static final String REPOSITORY_EVENT_LIST = "NotifiableAspectBehavior.RepositoryEventList";
  private static final String MOVED_NODES = "NotifiableAspectBehavior.MovedNodes";
  private static final String PROPERTY_UPDATED_NODES = "NotifiableAspectBehavior.PropertyUpdatedNodes";
  private static final String CREATED_NODES = "NotifiableAspectBehavior.CreatedNodes"; // new nodes that were just created
  public static final String NOTIFICATIONS_DISABLED = "NotifiableAspectBehavior.NotificationsDisabled"; 

  // Dependencies
  private List<String> propertySkipList;

  protected NotificationUtils notificationUtils;
  
  private boolean enabled = true;

  /**
   * Method setNotificationUtils.
   * @param notificationUtils NotificationUtils
   */
  public void setNotificationUtils(NotificationUtils notificationUtils) {
    this.notificationUtils = notificationUtils;
  }

  /**
   * Method addEvent.
   * @param event RepositoryEvent
   */
  protected void addEvent(RepositoryEvent event) {
    RepositoryEventList eventList = getEventList();
    eventList.add(event);
  }	

  /**
   * Run all notification processing in a separate thread to not hold up the 
   * original transaction.  Also, process moves in it's own thread because
   * it takes too long to do the search.
   *
   * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
   */
  @Override
  public void afterCommit() {
    if(isNotificationsDisabled()){
      return;
    }

    if(logger.isDebugEnabled())
      logger.debug(" *** afterCommit ***");

    RepositoryEventList eventList = getEventList();
    notificationUtils.sendRepositoryEventList(eventList);

    Set<NodeRef> movedNodes = getMovedNodes();
    if(movedNodes.size() > 0) {
      notificationUtils.sendMovedNodesEvents(movedNodes);
    }

//    // We only want to check for links if this is NOT a new node, since we know 
//    // new nodes won't have any links
//    Set<NodeRef> propUpdatedNodes = getPropertyUpdatedNodes();
//    Set<NodeRef>createdNodes = getCreatedNodes();
//    Set<NodeRef> updatedNodes = new HashSet<NodeRef>();
//    for (NodeRef nodeRef : propUpdatedNodes) {
//      if(!createdNodes.contains(nodeRef)) {
//        updatedNodes.add(nodeRef);
//      }
//    }
//    if (updatedNodes.size() > 0) {
//      notificationUtils.sendLinkPropertyChangedEvents(updatedNodes);
//    }
  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
   */
  @Override
  public void afterRollback() {
  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
   */
  @Override
  public void beforeCommit(boolean readOnly) {
    if(logger.isDebugEnabled())
      logger.debug(" *** beforeCommit ***");

  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
   */
  @Override
  public void beforeCompletion() {
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
    if(isNotificationsDisabled()){
      return;
    }

    // Do not do anything if this node is from the version store or if this node is a thumbnail or if this node has the cat:ignore aspect:
    if(NodeUtils.isVersionNode(nodeRef) 
        || nodeService.getType(nodeRef).equals(ContentModel.TYPE_THUMBNAIL) 
        || nodeService.hasAspect(nodeRef, CatConstants.ASPECT_IGNORE)) {
      return;
    }

    long start = System.currentTimeMillis();

    AlfrescoTransactionSupport.bindListener(this);
    RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_NODE_REMOVED);
    event.setNodeId(nodeRef.getId());
    event.setNodePath(notificationUtils.getNodePath(nodeRef));
    event.setEventPerpetrator(authenticationComponent.getCurrentUserName());
    event.setEventTimestamp(System.currentTimeMillis());
    addEvent(event);

    if(logger.isDebugEnabled())
      logger.debug(" %%% onDeleteNode %%%: " + event.getNodePath());      
    

//    List<NodeRef> children = nodeUtils.getAllFolderChildren(nodeRef);
//    for (NodeRef child : children) {
//      NodeRemovedEvent event = new NodeRemovedEvent();
//      event.setNodeId(child.getId());
//      event.setNodePath(notificationUtils.getNodePath(child));
//      event.setEventPerpetrator(authenticationComponent.getCurrentUserName());
//      event.setEventTimestamp(System.currentTimeMillis());
//      addEvent(event);
//
//      if(logger.isDebugEnabled())
//        logger.debug(" %%% onDeleteNode %%%: " + event.getNodePath());      
//    }


    long end = System.currentTimeMillis();    
    logger.debug("beforeDeleteNode time = " + (end - start));
  }

  // other methods to implement for TransactionListener
  /* (non-Javadoc)
   * @see org.alfresco.repo.transaction.TransactionListener#flush()
   */
  @Override
  public void flush() {
  }

  /**
   * Method isNotificationsDisabled.
   * @return boolean
   */
  protected boolean isNotificationsDisabled(){
    Boolean disabledFlag = (Boolean) AlfrescoTransactionSupport.getResource(NOTIFICATIONS_DISABLED);
    return disabledFlag != null;
  }

  /**
   * Method getEventList.
   * @return RepositoryEventList
   */
  protected RepositoryEventList getEventList() {
    RepositoryEventList eventList = (RepositoryEventList) AlfrescoTransactionSupport.getResource(REPOSITORY_EVENT_LIST);

    if (eventList == null) {
      eventList = new RepositoryEventList();
      AlfrescoTransactionSupport.bindResource(REPOSITORY_EVENT_LIST, eventList);
    }

    return eventList;
  }

  /**
   * This should be called on afterCommit to add the additional link
   * events that need to use a lucene search.  The lucene search slows
   * down as the repository gets bigger, so it should not be done inside
   * a transaction, and rather afterCommit.
  
   * @return Set<NodeRef>
   */
  //	private RepositoryEventList getEventListWithLinksEventsProcessed() {
  //		RepositoryEventList eventList = getEventList();
  //		Set<NodeRef> movedNodes = getMovedNodes();
  //
  //		for(NodeRef node : movedNodes) {
  //			String newChildPath = getNodePath(node);
  //
  //			List<NodeRef> nodesLinkedToTarget = getNodesLinkedToTarget(node);
  //			for (NodeRef link : nodesLinkedToTarget) {
  //				if (nodeService.exists(link)) {
  //					if (logger.isDebugEnabled())        
  //						logger.debug("processing linked node " + link.getId());
  //					TargetNodeMovedEvent targetMovedEvent = new TargetNodeMovedEvent();
  //					targetMovedEvent.setNodeId(link.getId());
  //					targetMovedEvent.setNodePath(getNodePath(link)); 
  //					targetMovedEvent.setEventPerpetrator(authenticationComponent.getCurrentUserName());
  //					targetMovedEvent.setEventTimestamp(System.currentTimeMillis());
  //					targetMovedEvent.setPropertyName(TargetNodeMovedEvent.PROPERTY_NEW_TARGET_LOCATION);
  //					targetMovedEvent.setPropertyValue(newChildPath);
  //					eventList.add(targetMovedEvent);
  //				}
  //			}
  //		}    
  //		return eventList;
  //	}

  private Set<NodeRef> getMovedNodes() {
    @SuppressWarnings("unchecked")
    Set<NodeRef> movedNodes = (Set<NodeRef>)AlfrescoTransactionSupport.getResource(MOVED_NODES);

    if (movedNodes == null) {
      movedNodes = new HashSet<NodeRef>();
      AlfrescoTransactionSupport.bindResource(MOVED_NODES, movedNodes);
    }

    return movedNodes;
  }
  
  /**
   * Bind to all types
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init() {
    logger.debug("initializing");
    if(enabled){
      policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"), 
          this, new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
      
      //policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), this, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
      policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), this, new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT));
  
      policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"), this, new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));
      policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), this, new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
  
      policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
          this,
          new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
      policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRemoveAspect"),
          this,
          new JavaBehaviour(this, "beforeRemoveAspect", NotificationFrequency.FIRST_EVENT));
    }else{
      logger.warn("Notifications have been diabled");
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    // If notifications are disabled, don't go any further
    if(isNotificationsDisabled()){
      return;
    }
    
    NodeRef childRef = childAssocRef.getChildRef();

    // Do not do anything if this node is from the version store or if this node is a thumbnail or if this node has the cat:ignore aspect:
    if( !nodeService.exists(childRef) 
        || NodeUtils.isVersionNode(childRef) 
        || nodeService.getType(childRef).equals(ContentModel.TYPE_THUMBNAIL) 
        || nodeService.hasAspect(childRef, CatConstants.ASPECT_IGNORE)) {
      return;
    }

    long start = System.currentTimeMillis();

    // first check if this is a cm:systemfolder; if it is, then just
    // ignore it because we don't want notifications for this type
    if( nodeService.getType(childRef).equals(ContentModel.TYPE_SYSTEM_FOLDER) ) {
      return;
    }

    AlfrescoTransactionSupport.bindListener(this);

    RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED);
    event.setNodeId(childRef.getId());
    event.setNodePath(notificationUtils.getNodePath(childRef));
    event.setEventPerpetrator(authenticationComponent.getCurrentUserName());
    event.setEventTimestamp(System.currentTimeMillis());
    addEvent(event);

    if(logger.isDebugEnabled()) {
      logger.debug(" %%% onCreateNode %%%: " + event.getNodePath());
    }

//    getCreatedNodes().add(childRef);
    long end = System.currentTimeMillis();

    logger.debug("onCreateNode time = " + (end - start));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
   */
  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps) {
    if(isNotificationsDisabled()){
      return;
    }

    // Do not do anything if this node is from the version store or if this node is a thumbnail or if this node has the cat:ignore aspect:
    if(!nodeService.exists(nodeRef)
        ||NodeUtils.isVersionNode(nodeRef) 
        || nodeService.getType(nodeRef).equals(ContentModel.TYPE_THUMBNAIL) 
        || nodeService.hasAspect(nodeRef, CatConstants.ASPECT_IGNORE)) {
      return;
    }

    // oldProps has a list of all of the properties that existed before the update occured
    // newProps has a list of all of the properties that now exist after the update has occured
    // compare the two and see what has changed

    long start = System.currentTimeMillis();

    // TODO: This is a temporary hack to avoid an alfresco bug.
    // Sometimes if this method is triggered because of a create node that is happening inside 
    // behavior code that is running in the beforeCommit phase of a transaction,
    // the parent node is coming up null, which crashes getPath(), so we have to just ignore this message.
    // This case will only occur for some special hidden nodes, so skipping them should be ok.
    List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
    if(parents.size() == 0) {
      logger.warn("Skipping update properties notification for " + nodeRef.getId()
          + " because it has no parents.");
      return;
    }

    String nodePath = notificationUtils.getNodePath(nodeRef);
    
    if(logger.isDebugEnabled()) {   
      logger.debug(" %%% onUpdateProperties %%%: " + nodePath);
    }

    AlfrescoTransactionSupport.bindListener(this);

    for (Iterator<QName> i = oldProps.keySet().iterator(); i.hasNext();) {
      QName propQName = (QName)i.next();
      Object oldPropValue = oldProps.get(propQName);

      if (propertySkipList.contains(propQName.toString()) == false) {
        // just make sure we are dealing with a property that we care about

        if (newProps.containsKey(propQName) == false) {
          // property existed in the old list, but not in the new list
          // so an existing prop must have been removed
          RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_REMOVED);
          event.setNodeId(nodeRef.getId());
          event.setNodePath(nodePath);
          event.setPropertyName(propQName.toString());
          event.setEventPerpetrator(authenticationComponent.getCurrentUserName());
          event.setEventTimestamp(System.currentTimeMillis());
          addEvent(event);
        } else {
          // prop exists in both old and new list
          // so value was simply changed
          Object newVal = newProps.get(propQName);
          if (newVal != null && newVal.equals(oldPropValue)==false) {
            RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED);
            event.setNodeId(nodeRef.getId());
            event.setNodePath(nodePath);
            event.setPropertyName(propQName.toString());
            event.setPropertyValue(newProps.get(propQName).toString());
            event.setEventPerpetrator(authenticationComponent.getCurrentUserName());
            event.setEventTimestamp(System.currentTimeMillis());
            addEvent(event);
          }
        }
      }
    }

    // now, look through the new list for any props that didn't exist in the old list
    // this would be a list of new properites that have been added
    for (Iterator<QName> i = newProps.keySet().iterator(); i.hasNext(); ) {
      QName propQName = (QName)i.next();
      if (propertySkipList.contains(propQName.toString()) == false) {
        // just make sure we are dealing with a property that we care about

        if (oldProps.containsKey(propQName) == false) {
          RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_ADDED);
          event.setNodeId(nodeRef.getId());
          event.setNodePath(nodePath);
          event.setPropertyName(propQName.toString());
          Object value = newProps.get(propQName);
          if (value != null) {
            event.setPropertyValue(newProps.get(propQName).toString());
          }
          addEvent(event);
        }
      }
    }

//    Set<NodeRef>updatedNodes = getPropertyUpdatedNodes();
//    updatedNodes.add(nodeRef);

    long end = System.currentTimeMillis();
    logger.debug("onUpdateProperties time = " + (end - start));

  }

  /**
   * Note that as of Alfresco 3.0, adding or removing an aspect no longer generates an OnUpdateProperties policy
   * event, so we must listen to this event separately
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#beforeRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    if(isNotificationsDisabled()){
      return;
    }

    // Do not do anything if this node is from the version store or if this node is a thumbnail or if this node has the cat:ignore aspect:
    if( !nodeService.exists(nodeRef) 
        || NodeUtils.isVersionNode(nodeRef) 
        || nodeService.getType(nodeRef).equals(ContentModel.TYPE_THUMBNAIL) 
        || nodeService.hasAspect(nodeRef, CatConstants.ASPECT_IGNORE)) {
      return;
    }
    
    AlfrescoTransactionSupport.bindListener(this);

    // Since we don't have an "aspect removed" event, let's try
    // passing this off as a property removed event instead
    RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_REMOVED);
    event.setNodeId(nodeRef.getId());
    event.setNodePath(notificationUtils.getNodePath(nodeRef));
    event.setPropertyName(aspectTypeQName.toString());
    event.setEventPerpetrator(AuthenticationUtil.getRunAsUser());
    event.setEventTimestamp(System.currentTimeMillis());
    addEvent(event);

  }

  /**
   * Note that as of Alfresco 3.0, adding or removing an aspect no longer generates an OnUpdateProperties policy
   * event, so we must listen to this event separately
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
   */
  @Override
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
    if(isNotificationsDisabled()){
      return;
    }
    
    // Do not do anything if this node is from the version store or if this node is a thumbnail or if this node has the cat:ignore aspect:
    if( !nodeService.exists(nodeRef) 
        || NodeUtils.isVersionNode(nodeRef) 
        || nodeService.getType(nodeRef).equals(ContentModel.TYPE_THUMBNAIL) 
        || nodeService.hasAspect(nodeRef, CatConstants.ASPECT_IGNORE)) {
      return;
    }
    
    AlfrescoTransactionSupport.bindListener(this);

    // Since we don't have an "aspect added" event, let's try
    // passing this off as a property added event
    RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_ADDED);
    event.setNodeId(nodeRef.getId());
    event.setNodePath(notificationUtils.getNodePath(nodeRef));
    event.setPropertyName(aspectTypeQName.toString());
    event.setPropertyValue("");
    event.setEventPerpetrator(AuthenticationUtil.getRunAsUser());
    event.setEventTimestamp(System.currentTimeMillis());
    addEvent(event);

  }


  /**
   * @param propertySkipList
   */
  public void setPropertySkipList(List<String> propertySkipList) {
    this.propertySkipList = propertySkipList;
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    if(isNotificationsDisabled()){
      return;
    }

    NodeRef oldParent = oldChildAssocRef.getParentRef();
    // oldChild-getChild() and newChild-getChild() return the same node
    NodeRef newChild = newChildAssocRef.getChildRef(); 

    // Do not do anything if this node is from the version store or if this node is a thumbnail or if this node has the cat:ignore aspect:
    if( !nodeService.exists(newChild)
        || NodeUtils.isVersionNode(newChild) 
        || nodeService.getType(newChild).equals(ContentModel.TYPE_THUMBNAIL) 
        || nodeService.hasAspect(newChild, CatConstants.ASPECT_IGNORE)) {
      return;
    }

    long start = System.currentTimeMillis();

    AlfrescoTransactionSupport.bindListener(this);
    String newChildPath = notificationUtils.getNodePath(newChild);
    
    if(logger.isDebugEnabled()) {
      logger.debug(" %%% onMoveNode %%%: " + newChildPath);
    }
    
    // save the moved nodes for later link processing after the tx commits
    Set<NodeRef>movedNodes = getMovedNodes();
    movedNodes.add(newChild);

    // CAT client needs the moved event for now, or else the cache isn't updating correctly
    // Later when the client caches nodes by uuid, not path, we can put back this performance
    // enhancment
    //		if (oldParent == newParent) {
    //		// nothing was moved, a node was just renamed
    //		// this will be caught in a property change event
    //		return;
    //		}

    String oldParentPath = notificationUtils.getNodePath(oldParent);
    String childNodeId = newChild.getId();

    // first, an event to delete the node at the source location

    // compute the path where this node was moved from
    RepositoryEvent removeEvent = new RepositoryEvent(RepositoryEvent.TYPE_NODE_REMOVED);
    removeEvent.setNodeId(childNodeId);
    removeEvent.setNodePath(oldParentPath + "/" + oldChildAssocRef.getQName().toString());
    removeEvent.setEventPerpetrator(authenticationComponent.getCurrentUserName());
    removeEvent.setEventTimestamp(System.currentTimeMillis());
    addEvent(removeEvent);

    // we don't need to create an event for the new node, since that will be done in the 
    // onCreateChildAssociation policy 
    
//    // now, an event to make the node "reappear" at the new location
//    RepositoryEvent addEvent = new RepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED);
//    addEvent.setNodeId(childNodeId);
//    addEvent.setNodePath(newChildPath);
//    addEvent.setEventTimestamp(System.currentTimeMillis());
//    addEvent.setEventPerpetrator(authenticationComponent.getCurrentUserName());
//    addEvent(addEvent);      

    long end = System.currentTimeMillis();
    logger.debug("onMoveNode time = " + (end - start));
  }

  /**
   * Method onCreateChildAssociation.
   * @param childAssocRef ChildAssociationRef
   * @param isNewNode boolean
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateChildAssociationPolicy#onCreateChildAssociation(ChildAssociationRef, boolean)
   */
  @Override
  public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {

    // If notifications are disabled, don't go any further
    if(isNotificationsDisabled()){
      return;
    }
    
    long start = System.currentTimeMillis();

    NodeRef childRef = childAssocRef.getChildRef();
    NodeRef parentRef = childAssocRef.getParentRef();

    if(nodeService.exists(childRef) && nodeService.exists(parentRef) ) {
      // Do not do anything if this node is from the version store or if this node is a thumbnail or if this node has the cat:ignore aspect:
      if(NodeUtils.isVersionNode(parentRef) 
          || NodeUtils.isVersionNode(childRef) 
          || nodeService.getType(parentRef).equals(ContentModel.TYPE_THUMBNAIL) 
          || nodeService.getType(childRef).equals(ContentModel.TYPE_THUMBNAIL) 
          || nodeService.hasAspect(parentRef, CatConstants.ASPECT_IGNORE)
          || nodeService.hasAspect(childRef, CatConstants.ASPECT_IGNORE)) {
        return;
      }
      // In case another policy has already removed this node
      if(logger.isDebugEnabled()){
        logger.debug(" %%% onCreateChildAssociation %%% parent: " + nodeService.getPath(parentRef));
        logger.debug(" %%% onCreateChildAssociation %%% child: " + nodeService.getPath(childRef));
      }

      // first check if this is a cm:systemfolder; if it is, then just
      // ignore it because we don't want notifications for this type
      if( nodeService.getType(childRef).equals(ContentModel.TYPE_SYSTEM_FOLDER) || 
          nodeService.getType(parentRef).equals(ContentModel.TYPE_SYSTEM_FOLDER)) {
        return;
      }

      AlfrescoTransactionSupport.bindListener(this);

      RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED);
      event.setNodeId(childRef.getId());
      event.setNodePath(notificationUtils.getNodePath(childRef));
      event.setEventPerpetrator(authenticationComponent.getCurrentUserName());
      event.setEventTimestamp(System.currentTimeMillis());
      addEvent(event);

//      getCreatedNodes().add(childRef);

      RepositoryEvent parentEvent = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED);
      parentEvent.setNodeId(parentRef.getId());
      parentEvent.setNodePath(notificationUtils.getNodePath(parentRef));
      //fake the property change event as the modified date changed. Alfresco doesn't seem to be calling
      //onUpdateProperties anymore like it used to for properties like modified date.
      parentEvent.setPropertyName(ContentModel.PROP_NODE_UUID.toString());
      parentEvent.setPropertyValue(nodeService.getProperty(parentRef, ContentModel.PROP_NODE_UUID).toString());
      parentEvent.setEventPerpetrator(authenticationComponent.getCurrentUserName());
      parentEvent.setEventTimestamp(System.currentTimeMillis());
      addEvent(parentEvent);

//      getPropertyUpdatedNodes().add(parentRef);
    }
    long end = System.currentTimeMillis();

    logger.debug("onCreateNode time = " + (end - start));
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
