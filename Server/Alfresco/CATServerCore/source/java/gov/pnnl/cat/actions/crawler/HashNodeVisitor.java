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
import gov.pnnl.cat.util.NodeUtils;

import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class HashNodeVisitor extends AbstractNodeVisitor {
  private static Log logger = LogFactory.getLog(HashNodeVisitor.class);
  private ContentService contentService;
  private BehaviourFilter policyBehaviourFilter;
  
	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
	 */
	@Override
	public String getName() {
		return "hashNodeVisitor";
	}

	/**
   * @param policyBehaviourFilter the policyBehaviourFilter to set
   */
  public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
    this.policyBehaviourFilter = policyBehaviourFilter;
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
    
	  // ignore non-content nodes
	  if (!unprotectedNodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
	    return;
	  }
	  
	  // Turn off Content Policy so this patcher runs faster
    this.policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.TYPE_CONTENT);

		try {
		  if (!unprotectedNodeService.hasAspect(nodeRef, CatConstants.ASPECT_IDENTIFIABLE)) {
		    unprotectedNodeService.addAspect(nodeRef, CatConstants.ASPECT_IDENTIFIABLE, null);

		    // make sure the content actually exists before we try to create the hash
		    ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		    
		    if (reader != null && reader.exists()){

		      InputStream input = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentInputStream();
		      String md5Hash = NodeUtils.createMd5Hash(input);
		      unprotectedNodeService.setProperty(nodeRef, CatConstants.PROP_HASH, md5Hash);

		    } else{
		      logger.error("Unable to make hash - no content reader for: "+nodeRef.getId());
		    }

		  }
		} catch (Exception e) {
		  throw new RuntimeException("Unable to calculate hash for " + nodeService.getPath(nodeRef), e);
		}

	}

  /**
   * @param contentService the contentService to set
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }
	
}
