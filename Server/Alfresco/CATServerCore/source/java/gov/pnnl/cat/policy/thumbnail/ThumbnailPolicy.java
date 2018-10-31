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
package gov.pnnl.cat.policy.thumbnail;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.ImageUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;

/**
 * Add properties to thumbnails as they are created and updated.
 * @version $Revision: 1.0 $
 */
public class ThumbnailPolicy implements InitializingBean, NodeServicePolicies.OnCreateNodePolicy, ContentServicePolicies.OnContentPropertyUpdatePolicy {

  /* So we can bind policies */
  protected PolicyComponent policyComponent;

  protected NodeService nodeService;

  protected ContentService contentService;

  /**
   * Method setPolicyComponent.
   * @param policyComponent PolicyComponent
   */
  public void setPolicyComponent(PolicyComponent policyComponent) {
    this.policyComponent = policyComponent;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Bind this policy to onCreateNode and onContentPropertyUpdate
   * 
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    // Bind policy to ourself as a service, so we don't have to worry about collisions in the future if
    // somebody else tries to bind to the thumbnail type
    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), this, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onContentPropertyUpdate"), 
        this, 
        new JavaBehaviour(this, "onContentPropertyUpdate", NotificationFrequency.TRANSACTION_COMMIT));
 }

  /**
   * Add parentMimeType and ignore aspect.
   * 
   * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef nodeRef = childAssocRef.getChildRef();

    if (!nodeService.exists(nodeRef)) {
      return;
    }

    if (nodeService.getType(nodeRef).equals(CatConstants.TYPE_THUMBNAIL)) {
      // check to see if the parent node is an image type - if it is, then flag this thumbnail as a thumbnail for an image file
      NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
      ContentData content = (ContentData) nodeService.getProperty(parent, ContentModel.PROP_CONTENT);
      if (content != null && content.getMimetype().startsWith("image/")) {
        nodeService.setProperty(nodeRef, CatConstants.PROP_PARENT_MIMETYPE, content.getMimetype());
      }

      // add the cat:ignore aspect so thumbnails don't come back in search results
      nodeService.addAspect(nodeRef, CatConstants.ASPECT_IGNORE, null);
    }
  }

  /**
   * Add height/width properties to the thumbnails.
   * 
   * @see org.alfresco.repo.content.ContentServicePolicies.OnContentPropertyUpdatePolicy#onContentPropertyUpdate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.ContentData, org.alfresco.service.cmr.repository.ContentData)
   */
  @Override
  public void onContentPropertyUpdate(NodeRef nodeRef, QName propertyQName, ContentData beforeValue, ContentData afterValue) {
    if (nodeService.exists(nodeRef) && nodeService.getType(nodeRef).equals(CatConstants.TYPE_THUMBNAIL) && ContentModel.PROP_CONTENT.equals(propertyQName)) {
      ImageUtils.setDimensions(nodeRef, contentService, nodeService);
    }
  }

}
