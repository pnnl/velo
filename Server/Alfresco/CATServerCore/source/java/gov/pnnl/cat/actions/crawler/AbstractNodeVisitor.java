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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 */
public abstract class AbstractNodeVisitor implements INodeVisitor {
  
  // the public node service w/ security filtering
	protected NodeService nodeService;
  
  // the unprotected node service w/ no interceptors
  // Use this one to look up children, because it's way faster
  protected NodeService unprotectedNodeService;

  // So you can check permissions in lieu of using the protected services (for better performance)
  protected PermissionService permissionService;

  
	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
   * @param unprotectedNodeService the unprotectedNodeService to set
   */
  public void setUnprotectedNodeService(NodeService unprotectedNodeService) {
    this.unprotectedNodeService = unprotectedNodeService;
  }

  /**
   * @param permissionService the permissionService to set
   */
  public final void setPermissionService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  /**
	 * Default implementation: return all children.  Use the
	 * unprotectedNodeService since it's way faster.  Override with specific behavior
	 * @param nodeRef NodeRef
   * @return List<ChildAssociationRef>
   * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getNodeChildren(NodeRef)
   */
	public List<ChildAssociationRef> getNodeChildren(NodeRef nodeRef) {
	  // Node may not exist by the time the children the next down are fetched
	  if(!unprotectedNodeService.exists(nodeRef)) {
	    return new ArrayList<ChildAssociationRef>();
	  }
		return unprotectedNodeService.getChildAssocs(nodeRef);
	}

	/**
	 * Method setup.
	 * @param parameters Map<String,Serializable>
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#setup(Map<String,Serializable>)
	 */
	public void setup(Map<String, Serializable> parameters) {
		// nothing to do
	}

	/**
	 * Method teardown.
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#teardown()
	 */
	public void teardown() {
		// nothing to do
	}

	/**
	 * Implement specific behavior here.
	 * @param nodeRef NodeRef
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#visitNode(NodeRef)
	 */
	public abstract void visitNode(NodeRef nodeRef);
	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
	 */
	public abstract String getName();
}
