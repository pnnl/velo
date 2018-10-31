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
package gov.pnnl.cat.actions.crawler;


import gov.pnnl.cat.util.CatConstants;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class UpgradeThumbnailsNodeVisitor extends AbstractNodeVisitor {
  private static Log logger = LogFactory.getLog(UpgradeThumbnailsNodeVisitor.class); 
  private long maxFileSize = 10000000;
  
  private ContentService contentService;
  private ActionService actionService;
  
  
	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
	 */
	@Override
	public String getName() {
		return "upgradeThumbnailNodeVisitor";
	}

  /**
   * don't create a transaction here, because the crawler does it for us
   * @param node NodeRef
   * @see gov.pnnl.cat.actions.crawler.INodeVisitor#visitNode(NodeRef)
   */
  @Override
  public void visitNode(NodeRef node) {
    // Node may have been deleted between the time the children were fetched and the child
    // was visited.
    if(!unprotectedNodeService.exists(node)) {
      return;
    }
    
    // Only apply this change if the node has the old aspect cm:thumbnailed
    QName thumbnailedAspect = QName.createQName("{http://www.alfresco.org/model/content/1.0}thumbnailed");
    if(!unprotectedNodeService.hasAspect(node, thumbnailedAspect)) {
      return;
    }
    
    String name = (String)nodeService.getProperty(node, ContentModel.PROP_NAME);
    logger.debug("Removing thumbnails on node: " + name);
    
    // Delete all thumbnails
    List<ChildAssociationRef> children = nodeService.getChildAssocs(node);
    for (ChildAssociationRef childRef : children) {
      NodeRef child = childRef.getChildRef();
      QName type = nodeService.getType(child);
      if(type.equals(ContentModel.TYPE_THUMBNAIL)) {
        nodeService.deleteNode(child);
      }
    }
    
    // Remove the thumbnailed aspect
    nodeService.removeAspect(node, thumbnailedAspect);
    
    // Now re-run the thumbnails action on that node
    //don't generate thumbnails for files with the cat:ignore aspect
    // don't generate thumbnails for files > maxFileSize
    long fileSize = contentService.getReader(node, ContentModel.PROP_CONTENT).getSize();
    if(fileSize <= maxFileSize && !nodeService.hasAspect(node, CatConstants.ASPECT_IGNORE)){
      Action action = actionService.createAction("create-thumbnail");   
      action.setParameterValue("thumbnail-name", "imgpreview");
      actionService.executeAction(action, node, false, false);
      
      action.setParameterValue("thumbnail-name", "medium");
      actionService.executeAction(action, node, false, false);
    }
  
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Method setActionService.
   * @param actionService ActionService
   */
  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }	
          
	
	
}
