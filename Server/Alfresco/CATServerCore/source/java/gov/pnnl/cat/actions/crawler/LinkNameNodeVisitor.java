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
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class LinkNameNodeVisitor extends AbstractNodeVisitor {
  private static Log logger = LogFactory.getLog(LinkNameNodeVisitor.class);

	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
	 */
	@Override
	public String getName() {
		return "linkNameNodeVisitor";
	}
	
	
	/**
	 * Only get children that are links or folders
   * @see gov.pnnl.cat.actions.crawler.AbstractNodeVisitor#getNodeChildren(org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  public List<ChildAssociationRef> getNodeChildren(NodeRef nodeRef) {
    // Node may not exist by the time the children the next down are fetched
    if(!unprotectedNodeService.exists(nodeRef)) {
      return new ArrayList<ChildAssociationRef>();
    }
    
    List<ChildAssociationRef> allChildren = unprotectedNodeService.getChildAssocs(nodeRef);
    List<ChildAssociationRef> children = new ArrayList<ChildAssociationRef>();
    for(ChildAssociationRef child : allChildren) {
      QName type = unprotectedNodeService.getType(child.getChildRef());
      if(type.equals(ContentModel.TYPE_FOLDER) || type.equals(ContentModel.TYPE_LINK)) {
        children.add(child);
      }
    }
    return children;
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
    
	  // only run on link nodes
	  if (!unprotectedNodeService.getType(nodeRef).equals(ContentModel.TYPE_LINK)) {
	    return;
	  }

    String name = (String)unprotectedNodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    if(!name.endsWith(".url")) {
      name = name.concat(".url");
      unprotectedNodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
    }   
	
	}
	
}
