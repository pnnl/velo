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
package gov.pnnl.cat.pipeline;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for common content type checks.
 * @version $Revision: 1.0 $
 */
public class FileProcessingPipelineUtil {
  private static final Log logger = LogFactory.getLog(FileProcessingPipelineUtil.class);
  
  /* A set of the content nodes to text transform */
  protected static final String CONTENT_NODES = "gov.pnnl.cat.policy.transforms.ContentType.ContentNodes";
  
  // Injected beans
  protected static FileProcessingPipeline processingPipeline;
  
  private static MimetypeService mimetypeService;
  private static NodeService nodeService;
  
  protected static TransactionListener txListener = new PipelineTxListener();
  
  
  private static Map<String, String> zipFileExt = new HashMap<String, String>();
  private static String[] zipExt = {
    "zip",
    "zipx",
    "gz",
    "bz2",
    "gtar",
    "tar",
    "tgz",
    "z",
    "tbz2",
    "lzma",
    "tlz",
    "s7z",
    "7z",
    "jar",
    "war",
    "ear",
    "xpi",
    "sxc",
    "stc",
    "sxd",
    "std",
    "sxi",
    "sti",
    "sxm",
    "stm",
    "sxw",
    "stw"
  };
  static {
    for(String ext : zipExt) {
      zipFileExt.put(ext, ext);
    }
  }
      
  /**
   * We want to check if the cm:content property was modified, since only  this
   * property reprsents content changes by the user.  We only want to check this
   * for cm:content nodes - not any other subtype of content, since only these
   * files are user created.  We also don't want to include any nodes in the version
   * store.  This is to prevent recursive policy when one policy (like thumbnails)
   * produces a content property.
   * 
   * All policy triggered by content changes should generally be using this method.
   * Since the logic is fairly lengthy, we only want to define it in one place.
   * 
   * @param nodeRef
   * @param propsBefore
   * @param propsAfter
  
  
   * @return boolean
   */
  public static boolean userCreatedContentChanged(NodeRef nodeRef, Map<QName, Serializable> propsBefore, Map<QName, Serializable> propsAfter) {
    logger.debug("checking if we should trigger pipeline");
    ContentData contentBefore = (ContentData) propsBefore.get(ContentModel.PROP_CONTENT);
    ContentData contentAfter = (ContentData) propsAfter.get(ContentModel.PROP_CONTENT);
    boolean isUserContent;
    
    // make sure the node exists (it may have been deleted if this is a working copy)
    if(!nodeService.exists(nodeRef)) {
      isUserContent = false;
      logger.debug("node no longer exists");
      
    // Do not even try to process any nodes coming from the version store
    } else if(NodeUtils.isVersionNode(nodeRef)) {
      isUserContent = false;
      logger.debug("node is a version");
    
    // only process exact types of cm:content(not subclasses)
    } else if(!nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
      isUserContent = false;
      logger.debug("node is not a content node");
      
    // do not process nodes if they are a "rendition" of another node
    } else if (nodeService.hasAspect(nodeRef, RenditionModel.ASPECT_RENDITION)) {
      isUserContent = false;
      logger.debug("node is a rendition");

    } else {
      // Do not process files under the alfresco dictionary
      String path = nodeService.getPath(nodeRef).toString();
      
      if(path.startsWith("/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}dictionary")) {
        isUserContent = false;
        logger.debug("node is in the dictionary");

      // Only process if the cm:content property changes (not any other content properties)
      } else {
        logger.debug("contentBefore: " + contentBefore); 
        logger.debug("contentAfter: " + contentAfter);
        if(contentAfter != null) {
          logger.debug("contentAfter URL: " + contentAfter.getContentUrl());
        }
        if ((contentBefore == null || !contentBefore.equals(contentAfter)) && 
            (contentAfter != null && contentAfter.getContentUrl() != null)) {
          isUserContent = true;
          logger.debug("change detected in content property");

        } else {
          logger.debug("no changed detected in content property");
          isUserContent = false;
        }
      }
    }
    return isUserContent;
  }

  /**
   * Collect information about the file to be processed so it is easy to access.
   * @param nodeRef
  
  
   * @param fileName String
   * @param contentData ContentData
   * @return FileProcessingInfo or Null if file is not valid for the pipeline
   */
  public static FileProcessingInfo createFileProcessingInfo(NodeRef nodeRef, String fileName, ContentData contentData) {
    
    // Do not even try to process any nodes coming from the version store
    if(NodeUtils.isVersionNode(nodeRef)) {
      logger.debug("node is a version");
      return null;
    
    // only process exact types of cm:content(not subclasses)
    } else if(!nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {      
      logger.debug("node is not a content node");
      return null;
      
    // do not process nodes if they are a "rendition" of another node
    } else if (nodeService.hasAspect(nodeRef, RenditionModel.ASPECT_RENDITION)) {
      logger.debug("node is a rendition");
      return null;

    } else {
      // Do not process files under the alfresco dictionary
      String path = nodeService.getPath(nodeRef).toString();
      
      if(path.startsWith("/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}dictionary")) {
        logger.debug("node is in the dictionary");
        return null;
      } 
    }
    
    // If the size is 0, we have no content, so just return null - we can't process the file
    // if we have no content
    long fileSize = contentData.getSize();
    if(fileSize == 0) {
      return null;
    }
    
    NodeRef nodeToExtract = nodeRef;
    String mimetype = contentData.getMimetype();
    
    Map<QName, Serializable> properties = new HashMap<QName, Serializable> ();

    // Set the mimetype property on the node so it's easy to access
    properties.put(CatConstants.PROP_MIMEYPE, mimetype);
    
    String username = AuthenticationUtil.getRunAsUser();
    
    boolean textExtractionRequired = false;
    
    // if the content is already text in any format except UTF-16, lets just share the content property,
    // so we don't have to waste time making a copy
    String encoding = contentData.getEncoding();
    if(encoding == null) {
      encoding = "UTF-8";
      contentData = ContentData.setEncoding(contentData, encoding);
    }
    if (mimetypeService.isText(mimetype) && !contentData.getEncoding().startsWith("UTF-16") ) {

      properties.put(CatConstants.PROP_TEXT_TRANSFORMED_CONTENT, contentData);

    // DO NOT auto-text extract any zip files!
    } else if(!isZip(fileName)) {
      // flag the indexer that the transform has not completed yet
      properties.put(CatConstants.PROP_TEXT_NEEDS_TRANSFORM, true);
      
      textExtractionRequired = true;
    }

    return new FileProcessingInfo(fileSize, mimetype, nodeToExtract, textExtractionRequired, username, fileName, properties);
  }
  
  /**
   * Method isZip.
   * @param fileName String
   * @return boolean
   */
  private static boolean isZip(String fileName) {
    int pos = fileName.lastIndexOf('.');
    if(pos >= 0) {
      String ext = fileName.substring(pos + 1);
      return (zipFileExt.get(ext) != null);
    }
    
    return false;
    
  }
  
  /**
   * Method setMimetypeService.
   * @param mimetypeService MimetypeService
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    FileProcessingPipelineUtil.mimetypeService = mimetypeService;
  }
  
  /**
   * Method getMimetypeService.
   * @return MimetypeService
   */
  public static MimetypeService getMimetypeService() {
    return mimetypeService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    FileProcessingPipelineUtil.nodeService = nodeService;
  }

  /**
   * Method addFileToPipeline.
   * @param fileInfo FileProcessingInfo
   */
  public static void addFileToPipeline(FileProcessingInfo fileInfo) {
    if(fileInfo != null){
      logger.debug("Adding node to pipeline.");
      AlfrescoTransactionSupport.bindListener(txListener);
      getNodeList(FileProcessingPipelineUtil.CONTENT_NODES).add(fileInfo);
    } else {
      logger.debug("FileProcessingInfo is null.");
    }
    
  }
  

  /**
   * Method setProcessingPipeline.
   * @param processingPipeline FileProcessingPipeline
   */
  public void setProcessingPipeline(FileProcessingPipeline processingPipeline) {
    FileProcessingPipelineUtil.processingPipeline = processingPipeline;
  }

  /**
   * Method getNodeList.
   * @param listName String
   * @return Set<FileProcessingInfo>
   */
  public static Set<FileProcessingInfo> getNodeList(String listName) {
    @SuppressWarnings("unchecked")
    Set<FileProcessingInfo> contentNodes = (Set<FileProcessingInfo>) AlfrescoTransactionSupport.getResource(listName);

    if (contentNodes == null) {
      contentNodes = new HashSet<FileProcessingInfo>();
      AlfrescoTransactionSupport.bindResource(listName, contentNodes);
    }

    return contentNodes;
  }
  
  public static void disablePiplineForCurrentTransaction() {
    AlfrescoTransactionSupport.bindResource(FileProcessingPipeline.PIPELINE_DISABLED, Boolean.TRUE);
  }
  
  public static void enablePipelineForCurrentTransaction() {
    AlfrescoTransactionSupport.unbindResource(FileProcessingPipeline.PIPELINE_DISABLED);

  }

  /**
   */
  public static class PipelineTxListener implements TransactionListener {
    
    /**
     * Need to run this on after commit to make sure the original transaction has completed before file processing is attempted.
     * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
     */
    public void afterCommit() {

      if (logger.isDebugEnabled()) {
        logger.debug("Calling afterCommit");
      }
      
      // do nothing if user has disabled pipeline for this transaction
      Boolean disabledFlag = (Boolean) AlfrescoTransactionSupport.getResource(FileProcessingPipeline.PIPELINE_DISABLED);
      if(disabledFlag != null && disabledFlag.equals(Boolean.TRUE)) {
        return;
      }
      
      Set<FileProcessingInfo> contentNodes = getNodeList(CONTENT_NODES);

      // Unlikely, but could be null none of the documents has any content
      if (contentNodes.size() > 0) {
        processingPipeline.submitProcessingJob(contentNodes);
      }
      
     }

    /**
     * Method afterRollback.
     * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
     */
    public void afterRollback() {
      // TODO Auto-generated method stub

    }

    /**
     * Method beforeCommit.
     * @param readOnly boolean
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
     */
    public void beforeCommit(boolean readOnly) {
      // TODO Auto-generated method stub

    }

    /**
     * Method beforeCompletion.
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
     */
    public void beforeCompletion() {
      // TODO Auto-generated method stub

    }

    /**
     * Method flush.
     * @see org.alfresco.repo.transaction.TransactionListener#flush()
     */
    public void flush() {
      // TODO Auto-generated method stub

    }
    
  }
  
}
