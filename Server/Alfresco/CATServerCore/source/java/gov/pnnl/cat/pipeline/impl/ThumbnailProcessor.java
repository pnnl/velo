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
package gov.pnnl.cat.pipeline.impl;

import gov.pnnl.cat.pipeline.AbstractFileProcessor;
import gov.pnnl.cat.pipeline.FileProcessingInfo;
import gov.pnnl.cat.util.PrioritizedThreadPoolExecutor;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;

/**
 * @author D3K339
 * 
 * @version $Revision: 1.0 $
 */
public class ThumbnailProcessor extends AbstractFileProcessor {
  public static final String THUMBNAIL_PREVIEW_PANE = "previewPaneImage";

  /**
   * Deployments can set this property in alfresso-global.properties
   */
  private ThumbnailService thumbnailService;

  private BehaviourFilter policyBehaviourFilter;

  protected PrioritizedThreadPoolExecutor mediumPriorityThreadPool;

  public void setMediumPriorityThreadPool(PrioritizedThreadPoolExecutor mediumPriorityThreadPool) {
    this.mediumPriorityThreadPool = mediumPriorityThreadPool;
  }

  public ThumbnailProcessor() {
    super();
  }

  /**
   * Method setThumbnailService.
   * 
   * @param thumbnailService
   *          ThumbnailService
   */
  public void setThumbnailService(ThumbnailService thumbnailService) {
    this.thumbnailService = thumbnailService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#getName()
   */
  @Override
  public String getName() {
    return "Thumbnail Generation";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.pnnl.cat.policy.pipeline.FileProcessor#processFile(gov.pnnl.cat.policy
   * .pipeline.FileProcessingInfo)
   */
  @Override
  public void processFile(FileProcessingInfo fileInfo) throws Exception {
    NodeRef nodeRef = fileInfo.getNodeToExtract();

    // for now only generate thumbnails for images since the other file types
    // were causing performance issues
    // later we can try to resolve this
    ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    String mimetype = reader.getMimetype();
    if (mimetype.toLowerCase().contains("image")) {
      createThumbnail(nodeRef);
    }
  }

  public NodeRef createThumbnail(final NodeRef nodeRef) {
    return createThumbnail(nodeRef, THUMBNAIL_PREVIEW_PANE);
  }

  /**
   * Get existing thumbnail if it exists
   * 
   * @param nodeRef
   * @param thumbnailName
   * @return
   */
  public NodeRef getThumbnail(NodeRef nodeRef, String thumbnailName) {
    ThumbnailDefinition thumbnailDefinition = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(thumbnailName);
    return thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, thumbnailDefinition.getName());
  }

  /**
   * Method createThumbnail.
   * 
   * @param nodeRef
   *          NodeRef
   * @return NodeRef
   */
  public NodeRef createThumbnail(final NodeRef nodeRef, String thumbnailName) {
    try {
      policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
      policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);

      ThumbnailDefinition thumbnailDefinition = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(thumbnailName);

      NodeRef existingThumbnail = thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, thumbnailDefinition.getName());

      try {
        // Commenting out check for failed thumbnail property, so we will
        // refresh any previously failed thumbnails
        // && !nodeService.hasAspect(nodeRef,
        // ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE)
        if (existingThumbnail == null) {
          existingThumbnail = thumbnailService.createThumbnail(nodeRef, ContentModel.PROP_CONTENT, thumbnailDefinition.getMimetype(),
              thumbnailDefinition.getTransformationOptions(), thumbnailDefinition.getName());
        } else if (existingThumbnail != null) {
          // if there's an existing thumbnail then the image was changed and the
          // thumbnail needs to be updated. But,
          // if the thumbnail has 2 parents, updateThumbnail will fail (IDK why
          // having 2 parents is that big of a deal) so
          // first remove the non-primary parents before updating, then add them
          // back
          List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(existingThumbnail);
          List<NodeRef> nonPrimaryParents = new ArrayList<NodeRef>();
          QName assocTypeQName = null;
          QName assocName = null;
          if (parentAssocs.size() > 1) {
            for (ChildAssociationRef assocRef : parentAssocs) {
              if (!assocRef.isPrimary()) {
                nonPrimaryParents.add(assocRef.getParentRef());
                assocTypeQName = assocRef.getTypeQName();
                assocName = assocRef.getQName();
                // weird, the removeAssociation didn't work but removeChild
                // does.
                // nodeService.removeAssociation(assocRef.getParentRef(),
                // assocRef.getChildRef(), assocRef.getTypeQName());
                nodeService.removeChild(assocRef.getParentRef(), existingThumbnail);
              }
            }
          }

          thumbnailService.updateThumbnail(existingThumbnail, thumbnailDefinition.getTransformationOptions());

          for (NodeRef parent : nonPrimaryParents) {
            nodeService.addChild(parent, existingThumbnail, assocTypeQName, assocName);
          }
        }
      } catch (Throwable e) {
        logger.error("Failed to create thumbnail", e);
      }

      // if thumbnail fails, add an aspect so we don't keep trying for the same
      // file
      // if an exception was thrown and the thumbnail failed, add the failed
      // thumbnail aspect, but we have to do this in a seperate tx
      // otherwise the aspect won't stick as the thumbnail exception marks the
      // whole tx to be rolled back:
      // if(thumbnailException != null && existingThumbnail == null){
      // Runnable addAspectJob = new Runnable(){
      // @Override public void run() {
      // RetryingTransactionCallback callback = new
      // RetryingTransactionCallback<Object>() {
      // public Object execute() throws Exception {
      // AuthenticationUtil.setRunAsUserSystem();
      // try{
      // policyBehaviourFilter.disableBehaviour(nodeRef,
      // ContentModel.ASPECT_AUDITABLE);
      // nodeService.addAspect(nodeRef,
      // ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE, null);
      // }finally{
      // policyBehaviourFilter.enableBehaviour(nodeRef,
      // ContentModel.ASPECT_AUDITABLE);
      // }
      //
      // return null;
      // }
      //
      // };
      //
      // transactionService.getRetryingTransactionHelper().doInTransaction(callback,
      // false, true);
      // }};
      // mediumPriorityThreadPool.execute(addAspectJob);
      // throw new RuntimeException(thumbnailException);
      // }

      return existingThumbnail;
    } finally {
      policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
      policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
    }
  }

  public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
    this.policyBehaviourFilter = policyBehaviourFilter;
  }

}
