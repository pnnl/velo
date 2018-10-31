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
package gov.pnnl.velo.policy;

import gov.pnnl.cat.pipeline.FileProcessingPipelineUtil;
import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.pipeline.WikiPhase2Processor;
import gov.pnnl.velo.util.WikiUtils;
import gov.pnnl.velo.wiki.content.WikiContentExtractorRegistry;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This policy is is used to sync the lifecycle of Alfresco resources with 
 * resource metadata pages on the wiki.
 * We queue up the nodes being touched and sync them after the transaction
 * successfully commits.
 * @version $Revision: 1.0 $
 */
public class WikiPolicy extends ExtensiblePolicyAdapter implements TransactionListener
{  
  protected static final Log logger = LogFactory.getLog(WikiPolicy.class);

  protected WikiContentExtractorRegistry wikiContentExtractorRegistry;
  protected WikiPhase2Processor wikiPhase2Processor;

  public static final String UPDATED_FILES = "gov.pnnl.velo.wiki.policy.WikiPolicy.UpdatedFiles";
  public static final String NEW_FOLDERS = "gov.pnnl.velo.wiki.policy.WikiPolicy.NewFolders";
  public static final String MOVED_NODES = "gov.pnnl.velo.wiki.policy.WikiPolicy.MovedNodes";
  //public static final String RENAMED_NODES = "gov.pnnl.velo.wiki.policy.WikiPolicy.RenamedNodes";
  public static final String DELETED_NODES = "gov.pnnl.velo.wiki.policy.WikiPolicy.DeletedNodes"; 
  public static final String UPDATED_LINKS = "gov.pnnl.velo.wiki.policy.WikiPolicy.UpdatedLinks";  


  // So we can disable from other policy
  protected JavaBehaviour onUpdatePropertiesBehaviour;

  @Override
  public void init() {

    // First check to see if we are running on a machine with a wiki installed

    if(WikiUtils.getWikiHome() == null) {
      logger.info("wiki.home not specified.  Not implementing wiki integration policy.");

    } else {
      onUpdatePropertiesBehaviour = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);

      // bind wiki policy
      policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
          this, // binds to all types
          new JavaBehaviour(this, "onCreateNode",  NotificationFrequency.FIRST_EVENT));  

      policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
          this, // binds to all types
          onUpdatePropertiesBehaviour); 

      this.policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
          this, // binds to all types
          new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT));

      this.policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"),
          this, // binds to all types
          new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));

      this.policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
          this, // binds to all types
          new JavaBehaviour(this, "onCopyComplete", NotificationFrequency.FIRST_EVENT));

    }
  }

  /**
   * Method getOnUpdatePropertiesBehaviour.
   * @return JavaBehaviour
   */
  public JavaBehaviour getOnUpdatePropertiesBehaviour() {
    return onUpdatePropertiesBehaviour;
  }

  /**
   * Method setWikiContentExtractorRegistry.
   * @param wikiContentExtractorRegistry WikiContentExtractorRegistry
   */
  public void setWikiContentExtractorRegistry(WikiContentExtractorRegistry wikiContentExtractorRegistry) {
    this.wikiContentExtractorRegistry = wikiContentExtractorRegistry;
  }

  /**
   * Method setWikiPhase2Processor.
   * @param wikiPhase2Processor WikiPhase2Processor
   */
  public void setWikiPhase2Processor(WikiPhase2Processor wikiPhase2Processor) {
    this.wikiPhase2Processor = wikiPhase2Processor;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onCopyComplete(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean, java.util.Map)
   */
  @Override
  public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
    // Alfresco policy is not throwing a content update with null "before" content any more when doing a copy, so 
    // we explicitly have to listen for copy events to record the new nodes in the wiki
    
    // make sure the node still exists
    if(!nodeService.exists(destinationRef)) {
      return;
    }
    
    // Now make sure this node is under wiki  control
    if(!WikiUtils.isWikiNode(destinationRef, nodeService, namespaceService)) {
      return;
    }

    AlfrescoTransactionSupport.bindListener(this);
    
    // make sure this is a folder
    QName nodeType = nodeService.getType(destinationRef);
    if(nodeService.hasAspect(destinationRef, CatConstants.ASPECT_REMOTE_LINK)){
    	String name = (String)nodeService.getProperty(destinationRef, ContentModel.PROP_NAME);
        getObjectList(UPDATED_LINKS).add(destinationRef);
    	
    }else{
	    if(nodeType.equals(ContentModel.TYPE_FOLDER)) {
	      String name = (String)nodeService.getProperty(destinationRef, ContentModel.PROP_NAME);
	      logger.debug("WikiPolicy detected copied folder: " + name);
	      getObjectList(NEW_FOLDERS).add(destinationRef);
	      
	    } else if (nodeType.equals(ContentModel.TYPE_CONTENT)) {
	      String name = (String)nodeService.getProperty(destinationRef, ContentModel.PROP_NAME);
	      logger.debug("WikiPolicy detected copied file: " + name);
	      getObjectList(UPDATED_FILES).add(destinationRef);
    }
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    NodeRef nodeRef = newChildAssocRef.getChildRef();
    NodeRef oldParent = oldChildAssocRef.getParentRef();
    NodeRef newParent = newChildAssocRef.getParentRef();

    // make sure the node still exists
    if(!nodeService.exists(nodeRef)) {
      return;
    }

    // Make sure this node is under wiki  control
    if(!WikiUtils.isWikiNode(nodeRef, nodeService, namespaceService)) {
      return;
    }

    // bind to tx for after-commit processing
    AlfrescoTransactionSupport.bindListener(this);


    // we have been moved
    // (note that if oldParent equals newParent, this is a rename, which still triggers the onMoveNode policy,
    // however, we have no idea what the old name is from this method, so we have to get the oldName from
    // onUpdateProperties)

    // First see if the node has already been cached by onUpdateProperties
    MovedNodeInfo info = getMovedNodes().get(nodeRef);
    if(info == null) {
      info = new MovedNodeInfo();
      getMovedNodes().put(nodeRef, info);
    }
    info.newParentPath = WikiUtils.getWikiPath(newParent, nodeService);
    info.oldParentPath = WikiUtils.getWikiPath(oldParent, nodeService);


  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {
    // make sure the node still exists
    if(!nodeService.exists(nodeRef)) {
      return;
    }

    // Make sure this node is under wiki  control
    if(!WikiUtils.isWikiNode(nodeRef, nodeService, namespaceService)) {
      return;
    }

    // Put the wiki path of the deleted node in our list, since after the tx commits, we wont
    // be able to access this info
    String wikiPath = WikiUtils.getWikiPath(nodeRef, nodeService);
    AlfrescoTransactionSupport.bindListener(this);
    getObjectList(DELETED_NODES).add(wikiPath);

  }

  /**
   * Right now, this policy is used to detect new folders. 
   * New files are handled via the onUpdateProperties policy.
   * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef newNode = childAssocRef.getChildRef();

    // make sure the node still exists
    if(!nodeService.exists(newNode)) {
      return;
    }

    // make sure this is a folder
    QName nodeType = nodeService.getType(newNode);
    if(!nodeType.equals(ContentModel.TYPE_FOLDER)) {
      return;
    }

    // Now make sure this node is under wiki  control
    if(!WikiUtils.isWikiNode(newNode, nodeService, namespaceService)) {
      return;
    }

    // All good, go ahead and queue up node for wiki synchronization
    AlfrescoTransactionSupport.bindListener(this);
    getObjectList(NEW_FOLDERS).add(newNode);
    
  }

  /**
   * Get cached object list bound to current tx
   * @param listName
  
   * @return Set<Object>
   */
  private Set<Object> getObjectList(String listName) {
    @SuppressWarnings("unchecked")
    Set<Object> nodes = (Set<Object>) AlfrescoTransactionSupport.getResource(listName);

    if (nodes == null) {
      nodes = new HashSet<Object>();
      AlfrescoTransactionSupport.bindResource(listName, nodes);
    }

    return nodes;
  }

  /**
   * Get moved nodes   map bound to current tx.
  
   * @return Map<NodeRef,MovedNodeInfo>
   */
  private Map<NodeRef, MovedNodeInfo> getMovedNodes() {
    @SuppressWarnings("unchecked")
    Map<NodeRef, MovedNodeInfo> movedNodes = (Map<NodeRef, MovedNodeInfo>)AlfrescoTransactionSupport.getResource(MOVED_NODES);

    if(movedNodes == null) {
      movedNodes = new HashMap<NodeRef, MovedNodeInfo>();
      AlfrescoTransactionSupport.bindResource(MOVED_NODES, movedNodes);
    }
    return movedNodes;
  }

  /**
   * Used for new or modified files.
   * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
   */
  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    logger.debug("triggered onUpdateProperties");
    
    // make sure the node still exists
    if(!nodeService.exists(nodeRef)) {
      return;
    }

    // Make sure this node is under wiki  control
    if(!WikiUtils.isWikiNode(nodeRef, nodeService, namespaceService)) {
      logger.debug("not a wiki node - exiting wiki policy");
      return;
    }

    // First check to see if content property changed
    if(FileProcessingPipelineUtil.userCreatedContentChanged(nodeRef, before, after)) {
      logger.debug("content change detected");
      AlfrescoTransactionSupport.bindListener(this);
      String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      if(nodeService.hasAspect(nodeRef, CatConstants.ASPECT_REMOTE_LINK)){
    	  getObjectList(UPDATED_LINKS).add(nodeRef);
      }else{
	      logger.debug("WikiPolicy detected content change for file: " + name);
	      getObjectList(UPDATED_FILES).add(nodeRef);
      }

    } else {
      logger.debug("no content change detected");
      // See if the name property changed, meaning a rename
      String oldName = (String) before.get(ContentModel.PROP_NAME);
      String newName = (String) after.get(ContentModel.PROP_NAME);

      if(oldName != null && newName != null && !oldName.equals(newName)) {
        logger.debug("move detected");
        // If we are renaming a node from its UUID, then this only happens by Alfresco
        // during a copy, so we ignore it
        if(!oldName.equals(nodeRef.getId())) {
          // we were renamed (meaning a move should have happened to change child association)
          // First see if the node has already been cached by onMoveNode
          AlfrescoTransactionSupport.bindListener(this);
          MovedNodeInfo info = getMovedNodes().get(nodeRef);
          if(info == null) {
            info = new MovedNodeInfo();
            getMovedNodes().put(nodeRef, info);
          }
          info.oldName = oldName;
          info.newName = newName;
        }
      }
    }

  }

  /**
   * Create a wiki metadata page for the given new file in alfresco.
   * This will only be an empty page until the CAT file processing
   * pipeline has run the appropriate extractors.
   * @param file
  
   * @param commandFile File
   * @throws Exception */
  private void createWikiFilePage(NodeRef file, File commandFile) throws Exception {

    // get the revision 
    String revision = (String) nodeService.getProperty(file, ContentModel.PROP_VERSION_LABEL);

    if(revision == null) {
      // For new files, the revision will be 1.0
      revision = "1.0";

    } else {
      // Predict the next version
      double version = Double.valueOf(revision);
      version = version + 1.0;
      revision = String.valueOf(version);
    }

    // Get the userid
    String userId =  WikiUtils.getCurrentUserId(authenticationComponent);

    // Get the mimetype
    String mimetype = WikiUtils.getMimetype(file, nodeService, contentService);
    
    // DO NOT add the wiki page content - this will be done from the CAT file processing pipeline

    // add the title (which is everything after WFS: in the wiki URL) 
    String title = WikiUtils.getWikiPath(file, nodeService);
    logger.debug("Performing Phase 1 call for: " + title + ", " + revision + ", " + mimetype + ", " + userId);

    // Get the alfresco file path
    String filePath = WikiUtils.getAlfrescoFilePath(file);

    WikiUtils.appendToFile(commandFile, title + ", " + revision + ", " + mimetype + ", " + userId + ", " + filePath);
    
  }  
  
  /**
   * Create a wiki metadata page for the given folder in alfresco
   * @param folder
  
   * @param commandFile File
   * @throws Exception */
  private void createWikiFolderPage(NodeRef folder, File commandFile) throws Exception {

    // get the revision 
    // For new folders, the revision will be 1.0
    // TODO: try taking out the revision param and see what happens
    // (since alfresco doesn't do versioning on folders)
    String revision ="1.0";

    // get the userid
    String userId =  WikiUtils.getCurrentUserId(authenticationComponent);

    // get the mimetype
    String folderMimetype = WikiUtils.getFolderMimetype(folder, nodeService);

    // add the title (which is everything after WFS: in the wiki URL) 
    String title = WikiUtils.getWikiPath(folder, nodeService);

    // First call alfRecordFile to put the folder into the system
    logger.debug("Recording new folder " + title + ", " + revision + ", " + folderMimetype + ", " + userId);
    WikiUtils.appendToFile(commandFile, title + ", " + revision + ", " + folderMimetype + ", " + userId);

  }
  

  /**
   * Method createWikiRemoteLinkPage.
   * @param file NodeRef
   * @param commandFile File
   * @throws Exception
   */
  private void createWikiRemoteLinkPage(NodeRef file, File commandFile) throws Exception {

	    // get the revision 
	    String revision = (String) nodeService.getProperty(file, ContentModel.PROP_VERSION_LABEL);

	    if(revision == null) {
	      // For new files, the revision will be 1.0
	      revision = "1.0";

	    } else {
	      // Predict the next version
	      double version = Double.valueOf(revision);
	      version = version + 1.0;
	      revision = String.valueOf(version);
	    }

	    // Get the userid
	    String userId =  WikiUtils.getCurrentUserId(authenticationComponent);

	    // Get the mimetype
	    //String mimetype = WikiUtils.getMimetype(file, nodeService, contentService);
	    //Chandrika - above statement always resolves to text/plain we currently 
	    //don't have any to find/get the original mimetype of the remotely linked file
	    //so setting it to a unique mimetype so with major=cmsfile so that wiki would
	    //use the GS3Page.php instead of DefaultFilePage.php for page view
	    //TODO - needs to change once we have resolution on how to capture and use the orginal
	    //file type. Is it enough if we capture the difference between a remotelink to a file and 
	    //a remotelink to a folder? Isn't it all we need to scp/ftp/wget the file from the remote server
	    //Once we download the file we can figure out the file's mimetype locally	 
	    String mimetype = "cmsfile/remotefile";
	    // DO NOT add the wiki page content - this will be done from the CAT file processing pipeline

	    // add the title (which is everything after WFS: in the wiki URL) 
	    String title = WikiUtils.getWikiPath(file, nodeService);
	    logger.debug("Performing Phase 1 call for: " + title + ", " + revision + ", " + mimetype + ", " + userId);
	    
	    logger.debug("Appending remote link file for " + title );
	    WikiUtils.appendToFile(commandFile, title + ", " + revision + ", " + mimetype + ", " + userId );
	    
  }

  /**
   * Delete the corresponding wiki page (and all child pages) for the given resource
   * @param wikiPath
  
   * @throws Exception */
  private void deleteWikiPage(String wikiPath) throws Exception {    
    String path = wikiPath;

    String[] cmdArray = {"php", WikiUtils.getDeletePath(), "--p", path};
    logger.debug("Deleting page: "  + wikiPath);
    WikiUtils.execCommand(cmdArray);
  }

  /**
   * Method moveWikiPage.
   * @param nodeRef NodeRef
   * @param info MovedNodeInfo
   * @throws Exception
   */
  private void moveWikiPage(NodeRef nodeRef, MovedNodeInfo info) throws Exception {

    // There could be a case where the node name property was changed without a move
    // This is unlikely, but could happen because of bad programming.  If we detect this case
    // Use the current path as the old and new path.
    if(info.oldParentPath == null) {
      logger.warn("File: " + info.oldName + " was renamed to " + info.newName + " without a move.  Alfresco is in an inconsistent state.");
      NodeRef currentParent = nodeService.getPrimaryParent(nodeRef).getParentRef();
      String currentParentPath = WikiUtils.getWikiPath(currentParent, nodeService);
      info.oldParentPath = currentParentPath;
      info.newParentPath = currentParentPath;
    }

    // There could be a case where the node was moved but the name was not changed.  In this case,
    // use the current name for both the old and new name
    if(info.oldName == null) {
      String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      info.oldName = name;
      info.newName = name;
    }

    String oldPath = info.oldParentPath + "/" + info.oldName;
    String newPath = info.newParentPath + "/" + info.newName;

    String[] cmdArray = {"php", WikiUtils.getMovePath(), "-r", "1.0", oldPath, newPath};
    // If we just copied a file and renamed it, then we could get a move where old path and new path are the same.
    // In this case, we don't want to do the move.
    if(!oldPath.equals(newPath)) {
      logger.debug("moving wiki page: " + oldPath + ", " + newPath);
      WikiUtils.execCommand(cmdArray);
    }
  }

  /** 
   * We need to run wiki synchronization code after the transaction has already committed
   * to be sure that the tx has committed successfully so we don't sync stale data.
   * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
   */
  @Override
  public void afterCommit() {
    
    logger.debug("Starting WikiPolicy.afterCommit()");
    long start = System.currentTimeMillis();

    final File commandFile = TempFileProvider.createTempFile("velo-phase1", ".alfRecordFile");

    // Get modified nodes and sync them with the wiki
    // For now, do everything in one tx (could be faster to batch if we have a huge tx)
    // Wrap in a retrying transaction handler in case of db deadlock
    RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
    {
      public Object execute() throws Exception
      {
        Set<Object> deletedNodes = getObjectList(DELETED_NODES);
        if(deletedNodes != null) {
          for(Object obj : deletedNodes) {
        	  String path = (String)obj;
            deleteWikiPage(path);
          }
        }
        
        // phase 1 for folders
        Set<Object> folders = getObjectList(NEW_FOLDERS);
        if(folders != null) {
          for (Object obj : folders) {
        	  NodeRef folder = (NodeRef) obj;
            createWikiFolderPage(folder, commandFile);
          }
        }

        // phase 1 for files
        Set<Object> updatedFiles = getObjectList(UPDATED_FILES);
        if(updatedFiles != null) {
          for (Object obj : updatedFiles) {
        	  NodeRef file = (NodeRef)obj;
            createWikiFilePage(file, commandFile);
          }
        }
        
        //phase 1 for remote links
        Set<Object> updatedlinks = getObjectList(UPDATED_LINKS);
        if(updatedlinks != null && !updatedlinks.isEmpty()) {
          logger.debug("creating phase 1 pages for remote links");
          for (Object obj : updatedlinks) {
        	  NodeRef file = (NodeRef)obj;
            createWikiRemoteLinkPage(file, commandFile);
          }
        }

        // Execute batch phase 1 command for files and folders
        try {
          String[] cmdArray = {"php", WikiUtils.getRecordFilePath(), "--input", commandFile.getAbsolutePath()};
          WikiUtils.execCommand(cmdArray);       
        } catch (Throwable e) {
          logger.error("Failed to execute batch command.", e);
        } finally {
          commandFile.delete();
        }
        
        // Execute batch phase 2 command for folders (files will happen in processing pipeline
        boolean executeBatch=false;
        if(folders != null && folders.size() > 0) {
          for (Object obj : folders) {
            NodeRef folder = (NodeRef) obj;
            wikiPhase2Processor.logPhase2Command(folder);            
          }
          executeBatch = true;
        }
        if(updatedlinks != null && updatedlinks.size() > 0) {
            logger.debug("creating phase 2 pages for remote links");
            for (Object obj : updatedlinks) {
          	  NodeRef file = (NodeRef)obj;
              wikiPhase2Processor.logPhase2Command(file);
            }
            executeBatch=true;
       }
       if(executeBatch){
    	// Force batch import to run now
        wikiPhase2Processor.executeBatchImportTextFile();
       }
        
        Map<NodeRef, MovedNodeInfo> movedNodes = getMovedNodes();
        if(movedNodes != null) {
          for (NodeRef nodeRef : movedNodes.keySet()) {
            MovedNodeInfo info = movedNodes.get(nodeRef);
            moveWikiPage(nodeRef, info);
          }
        }

        return null;
      }
    };
    transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);
    long end = System.currentTimeMillis();
    long time = (end - start) / 1000;
    logger.debug("Phase 1 time to sync with wiki: " + time + " seconds ");
    logger.debug("Finished WikiPolicy.afterCommit()");
  }

  /**
   * Method afterRollback.
   * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
   */
  @Override
  public void afterRollback() {}

  /**
   * Method beforeCommit.
   * @param readOnly boolean
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
   */
  @Override
  public void beforeCommit(boolean readOnly) {}

  /**
   * Method beforeCompletion.
   * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
   */
  @Override
  public void beforeCompletion() {}

  /**
   * Method flush.
   * @see org.alfresco.repo.transaction.TransactionListener#flush()
   */
  @Override
  public void flush() {}


  /**
   * Cache info about the move so wiki can be called with correct params.
   * @version $Revision: 1.0 $
   */
  private class MovedNodeInfo {
    String oldParentPath;
    String oldName;
    String newParentPath;
    String newName;
  }
}
