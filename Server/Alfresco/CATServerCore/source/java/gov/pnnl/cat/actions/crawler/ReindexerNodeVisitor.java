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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Forces a reindex of nodes.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class ReindexerNodeVisitor extends AbstractNodeVisitor {
  private static Log logger = LogFactory.getLog(ReindexerNodeVisitor.class);
  private NodeIndexer nodeIndexer;

	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
	 */
	@Override
	public String getName() {
		return "reindexerNodeVisitor";
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
    
    // reindex the node
    logger.error("rendexing node: " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
    nodeIndexer.indexUpdateNode(nodeRef);	
	}

  /**
   * Method setNodeIndexer.
   * @param nodeIndexer NodeIndexer
   */
  public void setNodeIndexer(NodeIndexer nodeIndexer) {
    this.nodeIndexer = nodeIndexer;
  }
	
}
