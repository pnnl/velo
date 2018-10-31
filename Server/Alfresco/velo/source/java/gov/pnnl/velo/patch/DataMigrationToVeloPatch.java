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
package gov.pnnl.velo.patch;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.pipeline.WikiPhase2Processor;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO9075;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class DataMigrationToVeloPatch extends AbstractPatch {

  private static Log logger = LogFactory.getLog(DataMigrationToVeloPatch.class);

  private ActionService actionService;

  protected NodeUtils nodeUtils;

  private AuthenticationComponent authenticationComponent;

  private ContentService contentService;

  protected WikiPhase2Processor wikiPhase2Processor;

  
  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
   */
  @Override
  protected String applyInternal() throws Exception {
    // Do not run this patch if there is no wiki running on the server
    if(WikiUtils.getWikiHome() == null) {
      return "Skipped data migration to velo since wiki is not installed."; 
    }
    
    //taking out logic to add mimetype properties to alfresco as this isn't needed for velo's patch
//    String actionName = "tree-crawler";
//    String visitorName = "addVeloMimetypesNodeVisitor";
//    Action action = actionService.createAction(actionName);
//    action.setParameterValue(TreeCrawlerActionExecutor.VISITOR_ID, visitorName);
//    action.setParameterValue(TreeCrawlerActionExecutor.RUN_EACH_NODE_IN_TRANSACTION, "true");
//
//    // run tree crawler action to set the mimetype property for projects and folders
//    logger.debug("WikiUtils.getWikiMountPoint() " + WikiUtils.getWikiMountPoint() );

//    String actionName = "tree-crawler";
//    String visitorName = "renameVeloNodeVisitor";
//    Action action = actionService.createAction(actionName);
//    action.setParameterValue(TreeCrawlerActionExecutor.VISITOR_ID, visitorName);
//    action.setParameterValue(TreeCrawlerActionExecutor.RUN_EACH_NODE_IN_TRANSACTION, "true");
//    
    
    NodeRef wikiMountNode = nodeUtils.getNodeByXPath("/app:company_home/cm:" + ISO9075.encode(WikiUtils.getWikiMountPoint()));
    logger.debug("wikiMountNode: " + wikiMountNode);
//    actionService.executeAction(action, wikiMountNode, false, false);
//    
    // then do user home dirs
    NodeRef userDocsNode = nodeUtils.getNodeByXPath(CatConstants.XPATH_USER_DOCUMENTS);
//    actionService.executeAction(action, userDocsNode, false, false);

    
    
    // now sync every file and folder under the wikiMountPoint & user docs with Velo:
    try {
      if(wikiMountNode != null && nodeService.exists(wikiMountNode)){
        final File commandFile = TempFileProvider.createTempFile("velo-phase1", ".alfRecordFile");
        logger.debug("wikiMountNode commandFile: " + commandFile);
        copyFiles(wikiMountNode, commandFile);
        String[] cmdArray = { "php", WikiUtils.getRecordFilePath(), "--input", commandFile.getAbsolutePath() };
        WikiUtils.execCommand(cmdArray);
      }else{
        logger.error("WikiMountNode not found, how can this be?");
      }
      if(userDocsNode != null && nodeService.exists(userDocsNode)){
        final File commandFile = TempFileProvider.createTempFile("velo-phase1", ".alfRecordFile");
        logger.debug("userDocsNode commandFile: " + commandFile);
        //sync the user docs folder as well:
        recursivelyVisitNode(userDocsNode, commandFile);
        copyFiles(userDocsNode, commandFile);
        String[] cmdArray = { "php", WikiUtils.getRecordFilePath(), "--input", commandFile.getAbsolutePath() };
        WikiUtils.execCommand(cmdArray);
      }else{
        logger.error("userDocumentsNode not found, how can this be?");
      }
    } catch (Throwable e) {
      logger.error("Failed to execute batch command.", e);
    }
    
    // Execute batch phase 1 command for files and folders
    
    // // Force batch import to run now (phase 2)
    // wikiPhase2Processor.executeBatchImportTextFile();

    return "OK";
  }

  /**
   * Method copyFiles.
   * @param nodeRef NodeRef
   * @param commandFile File
   * @throws Exception
   */
  private void copyFiles(NodeRef nodeRef, File commandFile) throws Exception {
    if(logger.isDebugEnabled()){
      logger.debug("copyFiles for node: " + nodeService.getProperty(nodeRef,ContentModel.PROP_NAME ));
    }
    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    // visit the children to add the properties and copy to the wiki
    for (ChildAssociationRef childNode : children) {
      if(!nodeService.hasAspect(childNode.getChildRef(), CatConstants.ASPECT_IGNORE) 
         && !nodeService.hasAspect(childNode.getChildRef(), RenditionModel.ASPECT_HIDDEN_RENDITION)){
        if(logger.isDebugEnabled()){
          logger.debug("calling recursivelyVisitNode for node: " + nodeService.getProperty(childNode.getChildRef(),ContentModel.PROP_NAME ));
        }
        recursivelyVisitNode(childNode.getChildRef(), commandFile);
      }
    }
  }

  /**
   * Method recursivelyVisitNode.
   * @param nodeRef NodeRef
   * @param commandFile File
   * @throws Exception
   */
  private void recursivelyVisitNode(NodeRef nodeRef, File commandFile) throws Exception {
    
    
    // phase 1 for folders
    if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
      if(logger.isDebugEnabled()){
        logger.debug("createWikiFolderPage node: " + nodeService.getProperty(nodeRef,ContentModel.PROP_NAME ));
      }
      createWikiFolderPage(nodeRef, commandFile);
    }

    // phase 1 for files
    if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
      if(logger.isDebugEnabled()){
        logger.debug("createWikiFilePage node: " + nodeService.getProperty(nodeRef,ContentModel.PROP_NAME ));
      }
      createWikiFilePage(nodeRef, commandFile);
    }

    // Execute batch phase 2 command for folders & files
    wikiPhase2Processor.logPhase2Command(nodeRef);

    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    for (ChildAssociationRef childAssoc : children) {
      recursivelyVisitNode(childAssoc.getChildRef(), commandFile);
    }

  }

  // copied from velo WikiPolicy:
  /**
   * Create a wiki metadata page for the given folder in alfresco
   * 
   * @param folder
  
   * @param commandFile File
   * @throws Exception */
  private void createWikiFolderPage(NodeRef folder, File commandFile) throws Exception {

    // get the revision
    // For new folders, the revision will be 1.0
    // TODO: try taking out the revision param and see what happens
    // (since alfresco doesn't do versioning on folders)
    String revision = "1.0";

    // get the userid
    String userId = WikiUtils.getCurrentUserId(authenticationComponent);

    // get the mimetype
    String folderMimetype = WikiUtils.getFolderMimetype(folder, nodeService);

    // add the title (which is everything after WFS: in the wiki URL)
    String title = WikiUtils.getWikiPath(folder, nodeService);

    // First call alfRecordFile to put the folder into the system
    WikiUtils.appendToFile(commandFile, title + ", " + revision + ", " + folderMimetype + ", " + userId);

  }

  /**
   * Create a wiki metadata page for the given new file in alfresco. This will only be an empty page until the CAT file processing pipeline has run the appropriate extractors.
   * 
   * @param file
  
   * @param commandFile File
   * @throws Exception */
  private void createWikiFilePage(NodeRef file, File commandFile) throws Exception {

    // get the revision
    String revision = (String) nodeService.getProperty(file, ContentModel.PROP_VERSION_LABEL);

    if (revision == null) {
      // For new files, the revision will be 1.0
      revision = "1.0";

    } else {
      // Predict the next version
      double version = Double.valueOf(revision);
      version = version + 1.0;
      revision = String.valueOf(version);
    }

    // Get the userid
    String userId = WikiUtils.getCurrentUserId(authenticationComponent);

    // Get the mimetype
    String mimetype = WikiUtils.getMimetype(file, nodeService, contentService);

    // DO NOT add the wiki page content - this will be done from the CAT file processing pipeline

    // add the title (which is everything after WFS: in the wiki URL)
    String title = WikiUtils.getWikiPath(file, nodeService);
    try{
    // Get the alfresco file path
    String filePath = WikiUtils.getAlfrescoFilePath(file);

    WikiUtils.appendToFile(commandFile, title + ", " + revision + ", " + mimetype + ", " + userId + ", " + filePath);
    }catch (Throwable e){
      logger.error("failed to get file for content node: " + file.getId(), e);
    }
  }

  /**
   * Method setAuthenticationComponent.
   * @param authenticationComponent AuthenticationComponent
   */
  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }

  /**
   * Method setActionService.
   * @param actionService ActionService
   */
  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Method setWikiPhase2Processor.
   * @param wikiPhase2Processor WikiPhase2Processor
   */
  public void setWikiPhase2Processor(WikiPhase2Processor wikiPhase2Processor) {
    this.wikiPhase2Processor = wikiPhase2Processor;
  }

 

}
