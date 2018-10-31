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
package gov.pnnl.cat.actions;


//import gov.pnnl.cat.transformers.IRemoteTransformer;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.PrioritizedThreadPoolExecutor;
import gov.pnnl.cat.util.TransformUtils;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 */
public class TextExtractionAction extends ActionExecuterAbstractBase {

	private static final Log logger = LogFactory.getLog(TextExtractionAction.class);

	private NodeService nodeService;
	private ContentService contentService;
	private TransactionService transactionService;
	private PrioritizedThreadPoolExecutor mediumPriorityThreadPool;

	/**
   * @param transactionService the transactionService to set
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * @param mediumPriorityThreadPool
   */
  public void setMediumPriorityThreadPool(PrioritizedThreadPoolExecutor mediumPriorityThreadPool) {
    this.mediumPriorityThreadPool = mediumPriorityThreadPool;
  }

  /**
	 * Set the node service
	 * 
	 * @param nodeService  set the node service
	 */
	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}

	/**
	 * Set the content service
	 * @param contentService
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}




	/**
	 * Method addParameterDefinitions.
	 * @param paramList List<ParameterDefinition>
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// none needed
	}


	/**
	 * Method executeImpl.
	 * @param ruleAction Action
	 * @param nodeRef NodeRef
	 */
	@Override
	protected void executeImpl(Action ruleAction, NodeRef nodeRef) {
		QName nodeType = nodeService.getType(nodeRef);
		if (nodeType.equals(ContentModel.TYPE_FOLDER)) {
			recursiveTransformNodes(nodeRef);
		} else if (nodeType.equals(ContentModel.TYPE_CONTENT)) {
			textTransformContentNode(nodeRef);
		} else {
			logger.debug("This action is not appropriate for nodes of type: " + nodeType);
		}
		

	}
	
	/**
	 * Recursively text-transform all nodes in this folder and child folders
	
	 * @param currentNodeRef NodeRef
	 */
	private void recursiveTransformNodes(NodeRef currentNodeRef) {
		List<ChildAssociationRef> children = nodeService.getChildAssocs(currentNodeRef);
		for (ChildAssociationRef child : children) {
			NodeRef childNodeRef = child.getChildRef();
			QName nodeType = nodeService.getType(childNodeRef);
			if (nodeType.equals(ContentModel.TYPE_FOLDER)) {
				recursiveTransformNodes(childNodeRef);
			} else if (nodeType.equals(ContentModel.TYPE_CONTENT)) {
				textTransformContentNode(childNodeRef);
			} else {
				logger.debug("Skipping " + currentNodeRef + " of type " + nodeType);
			}
		}
	}
	
	/**
	 * Text-transform an individual content node
	 * @param nodeRef
	 */
	private void textTransformContentNode(NodeRef nodeRef) {

		// get a ContentReader, then find transformer based on the content mime type -> plain text
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		if (reader != null && reader.exists())
		{
			ContentTransformer transformer = contentService.getTransformer(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
			// is this transformer good enough?
			if (transformer == null)
			{
				// log it
				if (logger.isDebugEnabled())
				{
					logger.debug("Not indexed: No transformation: \n" + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN);
				}
				nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, "No text transformer found for:  " + reader.getMimetype());
			}

			else
			{
				// get a ContentWriter for the extracted text to be written to
				ContentWriter writer = TransformUtils.getTransformWriter(nodeRef, nodeService, contentService);

				writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
				writer.setEncoding("UTF-8");

				try {
				  //all transforms run in seperate processes now
					transformer.transform(reader, writer);
					if(nodeService.getProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR) != null) {
					  // update this node to indicate no error occured
					  // this only happens on a content update when the previous transform failed but now
					  // this one worked
					  nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, null);
					}
				} catch (Exception e) {
					// log the error and store it in the node's error property
//				I could add this test and modify message to say that the file was too large.  e.getCause() instanceof java.lang.OutOfMemoryError
				  if(e.getCause() instanceof java.lang.OutOfMemoryError){
				    logger.error("Transformer failed, OutOfMemoryError: \n   node: " + nodeRef + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN, e);
	          nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, e.getClass().getName() + ": Transform failed.  The file is too large to transform.");
				  }else{
  					logger.error("Transformer failed: \n   node: " + nodeRef + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN, e);
  					nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, e.getClass().getName() + ": Transform failed.  See server log");
				  }
				}

			}
		}
		// If the full text indexer is running at the same time that this job completes, but this file
		// was tried to be indexed first in the batch when this job hadn't complted yet, 
		// then the dirty flag won't get set again for a retry - we need a delayed trigger
		
		// problems when fti and transform threads run at same time
		
		// remove this flag so we know the transform completed, whether an error occurred or not
		nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_NEEDS_TRANSFORM, null);
				
//		String fileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
//		System.out.println("Finished text transform for file: " + fileName);
	}

}
