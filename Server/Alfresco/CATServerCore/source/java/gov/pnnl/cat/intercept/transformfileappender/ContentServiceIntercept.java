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
package gov.pnnl.cat.intercept.transformfileappender;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.TransformUtils;

import java.lang.reflect.Method;

import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 */
public class ContentServiceIntercept implements MethodInterceptor {

	private NodeService nodeService;
	private ContentService contentService;
	private boolean showPhantomTextTransformNodes;


	/**
	 * Method setContentService.
	 * @param contentService ContentService
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}



	/**
	 * This is where we can wrap an entire call to the ContentService, 
	 * trap any exceptions, change return values, etc...
	 * @param methodInvocation MethodInvocation
	 * @return Object
	 * @throws Throwable
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
	 */
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		if(showPhantomTextTransformNodes){
  		Method method = methodInvocation.getMethod();
  		if (method.getName().equals("getReader")) {
  			return invokeGetReader(methodInvocation);
  		} else if (method.getName().equals("getWriter")) {
  			return invokeGetWriter(methodInvocation);
  		}
		}
		//if showPhantomTextTransformNodes is false, this is a no-op:
		return methodInvocation.proceed();
	}
	
	
	/**
	 * Method invokeGetWriter.
	 * @param methodInvocation MethodInvocation
	 * @return ContentWriter
	 * @throws Throwable
	 */
	private ContentWriter invokeGetWriter(MethodInvocation methodInvocation) throws Throwable {
		/* This has been commented out, since enabling this interceptor prevents the node
		 * from ever being written to, even by our policy that creates the node 
		 * Dave 2007-Apr-02
		 */
		
/*		Object[] args = methodInvocation.getArguments();	
		
		NodeRef nodeRef = (NodeRef)args[0];
		if (nodeService.getType(nodeRef).equals(CatConstants.TYPE_TRANSFORM)) {
			throw new InvalidTypeException("Transformed nodes are read-only");
		}*/
		
		//return (ContentWriter)methodInvocation.proceed();
		
		Object[] args = methodInvocation.getArguments();	
		NodeRef nodeRef = (NodeRef)args[0];

		// Alfresco passes in null for the nodeRef in BaseContentNode.getContentAsText(), so we have to check for this case
		if (nodeRef != null && TransformUtils.isPhantomTextNode(nodeRef)) {
		  throw new InvalidTypeException("Transformed nodes are read-only", CatConstants.PROP_TEXT_TRANSFORMED_CONTENT);
		}
		
		// else, proceed with the method call
		return (ContentWriter)methodInvocation.proceed();
	}		
	
			
	/**
	 * Method invokeGetReader.
	 * @param methodInvocation MethodInvocation
	 * @return ContentReader
	 * @throws Throwable
	 */
	private ContentReader invokeGetReader(MethodInvocation methodInvocation) throws Throwable {
		Object[] args = methodInvocation.getArguments();	
		
		// for our special Transform nodes, get the reader from the 
		//   CatConstants.PROP_TEXT_TRANSFORMED_CONTENT property
		NodeRef nodeRef = (NodeRef)args[0];
		
		if (TransformUtils.isPhantomTextNode(nodeRef)) {
			ContentService thisContentService = (ContentService)methodInvocation.getThis();
			NodeRef originalNode = TransformUtils.convertPhantomTextNode(nodeRef);
			return thisContentService.getReader(originalNode, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT);
		}
		
		// else, proceed with the method call
		return (ContentReader)methodInvocation.proceed();
	}


	/**
	 * Method setShowPhantomTextTransformNodes.
	 * @param showPhantomTextTransformNodes boolean
	 */
	public void setShowPhantomTextTransformNodes(boolean showPhantomTextTransformNodes) {
		this.showPhantomTextTransformNodes = showPhantomTextTransformNodes;
	}
}
