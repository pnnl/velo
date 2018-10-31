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
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * INodeVisitors are to be used with the TreeCrawlerActionExecutor Alfresco action.
 *
 * @version $Revision: 1.0 $
 */
public interface INodeVisitor {

	/**
	 * Method setup.
	 * @param parameters Map<String,Serializable>
	 */
	public void setup(Map<String, Serializable> parameters);
	public void teardown();
	/**
	 * Method visitNode.
	 * @param nodeRef NodeRef
	 */
	public void visitNode(NodeRef nodeRef);
	/**
	 * Method getNodeChildren.
	 * @param nodeRef NodeRef
	 * @return List<ChildAssociationRef>
	 */
	public List<ChildAssociationRef> getNodeChildren(NodeRef nodeRef);
	/**
	 * Method getName.
	 * @return String
	 */
	public String getName();
}
