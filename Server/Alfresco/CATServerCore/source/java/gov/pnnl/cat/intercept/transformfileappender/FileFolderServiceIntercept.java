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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AfterReturningAdvice;

/**
 */
public class FileFolderServiceIntercept implements AfterReturningAdvice, MethodInterceptor {

    private static Log logger = LogFactory.getLog(FileFolderServiceIntercept.class);
	
	private NodeService nodeService;
	private DictionaryService dictionaryService;
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
	 * This is where we can do simple things after execution, like adjust return values
	 * @param returnValue Object
	 * @param method Method
	 * @param args Object[]
	 * @param target Object
	 * @throws Throwable
	 * @see org.springframework.aop.AfterReturningAdvice#afterReturning(Object, Method, Object[], Object)
	 */
	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		String methodName = method.getName();
		if(showPhantomTextTransformNodes && (methodName.equals("list") || methodName.equals("listFiles")) || methodName.equals("search")) {
			afterReturningAddTransformedFiles(returnValue, method, args, target);
		}
		return;
	}


	/**
	 * This is where we can wrap an entire call to the FileFolderService, 
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
		
		Method method = methodInvocation.getMethod();
		try {

		if (method.getName().equals("resolveNamePath")) {
			return invokeResolveNamePath(methodInvocation);
		}  
		else if (method.getName().equals("getWriter")) {
			return invokeGetWriter(methodInvocation);
		} 
		else if (method.getName().equals("delete")) {
			return invokeMoveRenameDelete(methodInvocation);
		} 
		else if (method.getName().equals("rename")) {
			return invokeMoveRenameDelete(methodInvocation);
		} 
		else if (method.getName().equals("move")) {
			return invokeMoveRenameDelete(methodInvocation);
		} 
		else if (method.getName().equals("getFileInfo")) {
			return invokeGetFileInfo(methodInvocation);
		} 
		else if (method.getName().equals("searchSimple")) {
			return invokeSearchSimple(methodInvocation);
		}
		
		return methodInvocation.proceed();
		
		}
		catch (Throwable ex) {
			//ex.printStackTrace();
			throw ex;
		}
	}
	

	
	/**
	 * Method invokeGetFileInfo.
	 * @param methodInvocation MethodInvocation
	 * @return FileInfo
	 * @throws Throwable
	 */
	private FileInfo invokeGetFileInfo(MethodInvocation methodInvocation) throws Throwable {
		Object[] args = methodInvocation.getArguments();	
		
		NodeRef nodeRef = (NodeRef)args[0];
		
		if (TransformUtils.isPhantomTextNode(nodeRef)) {
			
			FileInfo fileInfo = toFileInfo(TransformUtils.convertPhantomTextNode(nodeRef), nodeRef);			
			
			return fileInfo;
		}
		
		return (FileInfo)methodInvocation.proceed();
	}	
	
	/**
	 * Method invokeGetWriter.
	 * @param methodInvocation MethodInvocation
	 * @return ContentWriter
	 * @throws Throwable
	 */
	private ContentWriter invokeGetWriter(MethodInvocation methodInvocation) throws Throwable {
         
        Object[] args = methodInvocation.getArguments();	
		
		NodeRef nodeRef = (NodeRef)args[0];
		
		if (TransformUtils.isPhantomTextNode(nodeRef)) {			
			throw new InvalidTypeException("Transformed nodes are read-only");
		}	
		
		return (ContentWriter)methodInvocation.proceed();
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
	 * Method invokeResolveNamePath.
	 * @param methodInvocation MethodInvocation
	 * @return FileInfo
	 * @throws Throwable
	 */
	private FileInfo invokeResolveNamePath(MethodInvocation methodInvocation) throws Throwable {
		
		Object[] args = methodInvocation.getArguments();
		Object target = methodInvocation.getThis();
		NodeRef rootNodeRef = (NodeRef)args[0];
		
		if (!TransformUtils.isPhantomTextNode(rootNodeRef)) {
			return (FileInfo)methodInvocation.proceed();			
		}
		else {
		
			List<String> pathElements = (List<String>)args[1];

			String originalFilename = pathElements.remove(pathElements.size() - 1);

			// try removing the last file extension, in case it was one we added to represent Transformed content
			String[] tokens = originalFilename.split("\\.");
			StringBuffer newLastElement = new StringBuffer();
			for (int i=0; i<tokens.length - 1; i++) {
				if (i > 0) {
					newLastElement.append(".");
				}
				newLastElement.append(tokens[i]);
			}
			pathElements.add(newLastElement.toString());

			FileFolderService fileFolderService = (FileFolderService)target;

			// this method will either succeed, and continue forward
			// or it will throw another FileNotFoundException
			FileInfo fileInfo = fileFolderService.resolveNamePath(rootNodeRef, pathElements);
			
			NodeRef originalTransformNode = fileInfo.getNodeRef();			
			
			// If it doesn't have a text transform aspect then throw exception 
			if ( (originalTransformNode == null) || 
				 (!nodeService.hasAspect(originalTransformNode, CatConstants.ASPECT_TEXT_TRANSFORM)) ) {
				throw new FileNotFoundException("Transformation Not Found");
			}
		
	        NodeRef phantomTransformNode = TransformUtils.createPhantomTextNode(originalTransformNode); 
		    	
			FileInfo transformFileInfo = toFileInfo(originalTransformNode, phantomTransformNode);
			
			return transformFileInfo;		
		}
	}
	

	/**
	 * Method invokeSearchSimple.
	 * @param methodInvocation MethodInvocation
	 * @return NodeRef
	 * @throws Throwable
	 */
	private NodeRef invokeSearchSimple(MethodInvocation methodInvocation) throws Throwable {
		Object[] args = methodInvocation.getArguments();
		Object target = methodInvocation.getThis();
		
		NodeRef returnValue = (NodeRef)methodInvocation.proceed();
		if (returnValue != null) {
			return returnValue;
		}

		NodeRef contextNodeRef = (NodeRef)args[0];
		String originalFilename = (String)args[1];

		// try removing the last file extension, in case it was one we added to represent Transformed content
		String[] tokens = originalFilename.split("\\.");
		StringBuffer newLastElement = new StringBuffer();
		for (int i=0; i<tokens.length - 1; i++) {
			if (i > 0) {
				newLastElement.append(".");
			}
			newLastElement.append(tokens[i]);
		}
		
		// take the new file name and perform the search for it
		String newFilename = newLastElement.toString();
		FileFolderService fileFolderService = (FileFolderService)target;
		NodeRef originalNode = fileFolderService.searchSimple(contextNodeRef, newFilename);
		
		// If it doesn't have a text transform aspect then throw exception 
		if (originalNode == null) {
			logger.warn("Could not find node: " + originalFilename);
			return null;
		}
		
		return TransformUtils.createPhantomTextNode(originalNode);	
	}

	
	/**
	 * Method afterReturningAddTransformedFiles.
	 * @param returnValue Object
	 * @param method Method
	 * @param args Object[]
	 * @param target Object
	 * @throws Throwable
	 */
	private void afterReturningAddTransformedFiles(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		// check the return value to see if there was even a list of FileInfo objects returned
		List<FileInfo> fileList = (List<FileInfo>)returnValue;
		if (fileList.size() == 0) {
			return;
		}

		List<FileInfo>newFileList = new ArrayList<FileInfo>();

		// iterate through the returned FileInfo
		for (FileInfo fileInfo : fileList) {
			
			NodeRef nodeRef = fileInfo.getNodeRef();

			// does it have our aspect?
			if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TEXT_TRANSFORM)) {
				
				NodeRef transformedNodeRef = TransformUtils.createPhantomTextNode(nodeRef); 
					
				Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
				
				
				// Should be done in the nodeservice get properties interceptor
				// assume this node has the properties we want
				properties.put(ContentModel.PROP_CONTENT, properties.get(CatConstants.PROP_TEXT_TRANSFORMED_CONTENT));
				properties.put(ContentModel.PROP_NAME, 
						       TransformUtils.makeTransformFileName(nodeRef, TransformUtils.TEXT_TRANSFORM_EXTENSION, nodeService));
				
				// phantom content node
				FileInfo transFileInfo = new TransformedFileInfoImpl(transformedNodeRef, false, properties);
				
				newFileList.add(transFileInfo);					
			}
		}
		fileList.addAll(newFileList);
	}

	
	/**
	 * Helper method to convert a transform node reference instance to a FileInfo 
	 * We know only phantom nodes will be called for this method.
	 * @param originalNodeRef NodeRef
	 * @param phantomNodeRef NodeRef
	 * @return FileInfo
	 * @throws InvalidTypeException
	 */
	private FileInfo toFileInfo(NodeRef originalNodeRef, NodeRef phantomNodeRef) throws InvalidTypeException
	{
		// get the file attributes
		// Note this version of the nodeService DOES NOT have the CIFS interceptor
		Map<QName, Serializable> properties = nodeService.getProperties(originalNodeRef);
		
		// Replace content and name properties
		properties.put(ContentModel.PROP_CONTENT, properties.get(CatConstants.PROP_TEXT_TRANSFORMED_CONTENT));
		properties.put(ContentModel.PROP_NAME, 
				       TransformUtils.makeTransformFileName(originalNodeRef, TransformUtils.TEXT_TRANSFORM_EXTENSION, nodeService));

		
		// construct the file info and add to the results
		FileInfo fileInfo = new TransformedFileInfoImpl(phantomNodeRef, false, properties);
		
		return fileInfo;
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
