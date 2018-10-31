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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 */
public class NodeServiceIntercept implements MethodInterceptor {

  private boolean showPhantomTextTransformNodes;

	/**
	 * This is where we can wrap an entire call to the NodeService, 
	 * trap any exceptions, change return values, etc...
	 * @param methodInvocation MethodInvocation
	 * @return Object
	 * @throws Throwable
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
	 */
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
	  //if showPhantomTextTransformNodes is false, this is a no-op:
    if(!showPhantomTextTransformNodes){
      return methodInvocation.proceed();
    }
    
		Object[] args = methodInvocation.getArguments();	
		//NodeRef nodeRef = (NodeRef)args[0];		
		
		//if (TransformUtils.isPhantomTextNode(nodeRef)) {
		
			Method method = methodInvocation.getMethod();

			if (method.getName().equals("getProperty")) {
				return invokeGetProperty(methodInvocation);
			} else if (method.getName().equals("deleteNode")) {
				return invokeMoveRenameDelete(methodInvocation);
			} else if (method.getName().equals("moveNode")) {
				return invokeMoveRenameDelete(methodInvocation);
			} else if (method.getName().equals("getProperties")) {
				return invokeGetProperties(methodInvocation);
			}
			else {
				NodeService targetInstance = (NodeService) methodInvocation.getThis();
				if (args.length > 0 && args[0] instanceof NodeRef ) {
					NodeRef noderef = (NodeRef) args[0];
					if (TransformUtils.isPhantomTextNode(noderef)) {
						NodeRef originalNodeRef = TransformUtils.convertPhantomTextNode(noderef);
						args[0] = originalNodeRef;
					}
					return method.invoke(targetInstance, args);
				}
				return methodInvocation.proceed();
			}
		//}
		//return methodInvocation.proceed();
	}
	
	/**
	 * Method invokeGetProperties.
	 * @param methodInvocation MethodInvocation
	 * @return Object
	 * @throws Throwable
	 */
	private Object invokeGetProperties(MethodInvocation methodInvocation) throws Throwable {
		

		Map<QName, Serializable> properties = null;
		
		Object target = (NodeService)methodInvocation.getThis();
		Object[] args = methodInvocation.getArguments();
		NodeService thisNodeService = (NodeService)target;

		NodeRef nodeRef = (NodeRef)args[0];
		
		// Need to handle differently for a text transform NodeRef
		if (TransformUtils.isPhantomTextNode(nodeRef)) {
			NodeRef transformNode = TransformUtils.convertPhantomTextNode(nodeRef);
			
			
			properties = thisNodeService.getProperties(transformNode);
			
			// assume this node has the properties we want
			properties.put(ContentModel.PROP_CONTENT, properties.get(CatConstants.PROP_TEXT_TRANSFORMED_CONTENT));
			properties.put(ContentModel.PROP_NAME, 
					       TransformUtils.makeTransformFileName(transformNode, TransformUtils.TEXT_TRANSFORM_EXTENSION, thisNodeService));
		}
		else {
			
			properties = (Map<QName, Serializable>)methodInvocation.proceed();
		}



		return properties;
	}


	/**
	 * Method invokeGetProperty.
	 * @param methodInvocation MethodInvocation
	 * @return Object
	 * @throws Throwable
	 */
	private Object invokeGetProperty(MethodInvocation methodInvocation) throws Throwable {
		NodeService thisNodeService = (NodeService)methodInvocation.getThis();
		Object[] args = methodInvocation.getArguments();	
		NodeRef nodeRef = (NodeRef)args[0];
		QName qname = (QName)args[1];
		Object returnValue = null;
		
		if (TransformUtils.isPhantomTextNode(nodeRef)) {
			
			NodeRef transformNode = TransformUtils.convertPhantomTextNode(nodeRef);
			
			if (qname.equals(ContentModel.PROP_CONTENT)) {
				returnValue = thisNodeService.getProperty(transformNode, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT);
			
			} else if (qname.equals(ContentModel.PROP_NAME)) {
				returnValue = TransformUtils.makeTransformFileName(transformNode, TransformUtils.TEXT_TRANSFORM_EXTENSION, thisNodeService);
			
			} else {
				returnValue = thisNodeService.getProperty(transformNode, qname);
			}
			
		} else {
			returnValue = methodInvocation.proceed();
		}
		
		return returnValue;		
	}
	
	
	/**
	 * Method invokeMoveRenameDelete.
	 * @param methodInvocation MethodInvocation
	 * @return Object
	 * @throws Throwable
	 */
	private Object invokeMoveRenameDelete(MethodInvocation methodInvocation) throws Throwable {
		Object[] args = methodInvocation.getArguments();	
		
		NodeRef nodeRef = (NodeRef)args[0];			
		if (TransformUtils.isPhantomTextNode(nodeRef)) {			
			throw new InvalidTypeException("Operation not supported on transformed nodes");
		}
		return methodInvocation.proceed();
	}
	
	
	/**
	 * Exception when the type is not a valid File or Folder type
	 * 
	 * @see ContentModel#TYPE_CONTENT
	 * @see ContentModel#TYPE_FOLDER
	 * 
	 * @author Derek Hulley
	 * @version $Revision: 1.0 $
	 */
	private static class InvalidTypeException extends RuntimeException
	{
		private static final long serialVersionUID = -310101369475434280L;

		/**
		 * Constructor for InvalidTypeException.
		 * @param msg String
		 */
		public InvalidTypeException(String msg)
		{
			super(msg);
		}
	}


  /**
   * Method setShowPhantomTextTransformNodes.
   * @param showPhantomTextTransformNodes boolean
   */
  public void setShowPhantomTextTransformNodes(boolean showPhantomTextTransformNodes) {
    this.showPhantomTextTransformNodes = showPhantomTextTransformNodes;
  }
}
