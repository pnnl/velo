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
package gov.pnnl.cat.tagtimer;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.TagTimerConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * TODO: this class needs to be moved out of core CAT since it is not a core component
 */
public class JustificationFileCreator extends AbstractLifecycleBean{
	// implements ApplicationContextAware{

	// Logger
	private static final Log logger = LogFactory
			.getLog(JustificationFileCreator.class);
	private DictionaryService dictionaryService;
	private NodeService nodeService;
	private ContentService contentService;
	private TransactionService transactionService;
	
	/**
	 * Method onShutdown.
	 * @param event ApplicationEvent
	 */
	protected void onShutdown(ApplicationEvent event) {

	}


	/**
	 * Method onBootstrap.
	 * @param event ApplicationEvent
	 */
	protected void onBootstrap(ApplicationEvent event) {
	  RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>() {
	    public Object execute() throws Throwable {

	      AuthenticationUtil.setRunAsUserSystem();

	      NodeRef confNodeRef = NodeUtils.getConfFolder(nodeService);
	      NodeRef justificationReasonFile = NodeUtils.getChildByName(
	          confNodeRef,
	          TagTimerConstants.JUSTIFICATION_REASON_FILENAME,
	          nodeService);

	      // add the node if it has not been added before
	      if (justificationReasonFile == null) {
	        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	        properties.put(ContentModel.PROP_NAME,
	            TagTimerConstants.JUSTIFICATION_REASON_FILENAME);

	        justificationReasonFile = nodeService
	            .createNode(
	                NodeUtils.getConfFolder(nodeService),
	                ContentModel.ASSOC_CONTAINS,
	                QName
	                .createQName(
	                    TagTimerConstants.NAMESPACE_TAGTIMER,
	                    TagTimerConstants.JUSTIFICATION_REASON_FILENAME),
	                    ContentModel.TYPE_CONTENT, properties)
	                    .getChildRef();


	        nodeService.addAspect(justificationReasonFile, CatConstants.ASPECT_IGNORE, null);

	        PropertyDefinition property = dictionaryService
	            .getProperty(TagTimerConstants.PROP_JUSTIFICATION_REASON);
	        List<ConstraintDefinition> constraints = property
	            .getConstraints();
	        // only expecting one:
	        ConstraintDefinition constraintDefinition = constraints.get(0);
	        ListOfValuesConstraint constraint = (ListOfValuesConstraint) constraintDefinition
	            .getConstraint();
	        List<String> values = constraint.getAllowedValues();
	        StringBuffer constraintString = new StringBuffer();

	        for (String value : values) {
	          constraintString.append(value + ",");
	        }
	        ContentWriter writer = contentService.getWriter(
	            justificationReasonFile, ContentModel.PROP_CONTENT,
	            true);
	        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
	        writer.setEncoding("UTF-8");
	        writer.putContent(constraintString.toString());
	      }
	      return null;
	    }
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(cb,
				false, true);
	}

	/**
	 * Method setDictionaryService.
	 * @param dictionaryService DictionaryService
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
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
	 * Method setTransactionService.
	 * @param transactionService TransactionService
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
}
