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


import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class OwnableNodeVisitor extends AbstractNodeVisitor {
  private static Log logger = LogFactory.getLog(OwnableNodeVisitor.class); 
	private OwnableService ownableService;

	/**
   * @param ownableService the ownableService to set
   */
  public void setOwnableService(OwnableService ownableService) {
    this.ownableService = ownableService;
  }

	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
	 */
	@Override
	public String getName() {
		return "ownableNodeVisitor";
	}

	/**
	 * Method getNodeChildren.
	 * @param nodeRef NodeRef
	 * @return List<ChildAssociationRef>
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getNodeChildren(NodeRef)
	 */
	@Override
  public List<ChildAssociationRef> getNodeChildren(NodeRef nodeRef) {
	   // Node may not exist by the time the children the next down are fetched
    if(!unprotectedNodeService.exists(nodeRef)) {
      return new ArrayList<ChildAssociationRef>();
    }
	  
	  // Don't visit files' transforms  commenting this logic out as it prevented thumbnails to get the correct owner set on them (thumbnails are children of content type nodes).
//	  if(unprotectedNodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
//	    return new ArrayList<ChildAssociationRef>();
//	  }
    
	  return unprotectedNodeService.getChildAssocs(nodeRef);
  }
	
	/**
	 * don't create a transaction here, because the crawler does it for us
	 * @param nodeRef NodeRef
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#visitNode(NodeRef)
	 */
	@Override
	public void visitNode(NodeRef nodeRef) {
    // Node may have been deleted between the time the children were fetched and the child
    // was visited.
    if(!unprotectedNodeService.exists(nodeRef)) {
      return;
    }
	  
		NodeRef parent = unprotectedNodeService.getPrimaryParent(nodeRef).getParentRef();

		if(unprotectedNodeService.hasAspect(parent, ContentModel.ASPECT_OWNABLE))
		{ 
		  if(logger.isDebugEnabled())
		  {
		    logger.debug("setting owner for " + (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
		  }
		  // Get the owner
		  ownableService.setOwner(nodeRef, ownableService.getOwner(parent));
		}
          
	}
	
}
