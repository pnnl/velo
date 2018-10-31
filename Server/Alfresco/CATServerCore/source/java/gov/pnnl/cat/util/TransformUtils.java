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
package gov.pnnl.cat.util;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Easy methods to interact with persistent transforms on a content node
 * 
 * TODO: make this be a bean to be injected instead of a static class
 *
 * @version $Revision: 1.0 $
 */
public class TransformUtils {
  /** Log */
  private static Log logger = LogFactory.getLog(TransformUtils.class); 
  
  public static final String TEXT_TRANSFORM_EXTENSION = "txt";
  public static final String TEXT_TRANSFORM_TYPE = "Raw Text";
  public static final String TEXT_TRANSFORM_NODE_REF_SUFFIX = "-T";

  /**
   * Construct the file name of the transform node, based on the original content node and extension to use.
   * Make a name of the form originalFilename.transformExtension
   * This is a separate method so if we need to change how the filename is constructed, we can change it in one place
   * @param contentNode - the original content node
   * @param transformExtension - the extension appended to the filename.  No dot prefix is needed
   * @param nodeService - the node service
  
   * @return String
   */
  public static String makeTransformFileName(NodeRef contentNode, String transformExtension, NodeService nodeService) {
	  String originalFileName = (String)nodeService.getProperty(contentNode, ContentModel.PROP_NAME);
	  return originalFileName + "." + transformExtension;
  }
  
  /**
   * Get a persistent transform node.  This method will create one if the transform does not exist.
   * @param contentNode
 * @param contentService TODO



  
   * @param nodeService NodeService
   * @return ContentWriter
   */
  public static ContentWriter getTransformWriter(NodeRef contentNode, NodeService nodeService, ContentService contentService) {
	  return getTransformWriter(contentNode, nodeService, contentService, true);
  }
  
  /**
 * @param contentNode
 * @param nodeService
 * @param contentService TODO

 * @return ContentReader
   */
public static ContentReader getTransformReader(NodeRef contentNode, NodeService nodeService, ContentService contentService) {
	
	ContentReader reader = null;
	
	if (contentService != null) {
		reader = contentService.getReader(contentNode, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT);
	}
	
	return reader;
  }

  
  /**
   * Get a persistent transform node.  This method will create one if the transform does not exist and createIfNotExist is true
   * @param contentNode
 * @param nodeService
 * @param contentService
 * @param createIfNotExist
  
   * @return ContentWriter
   */
  public static ContentWriter getTransformWriter(NodeRef contentNode, NodeService nodeService, ContentService contentService, 
      boolean createIfNotExist) {
    
	ContentWriter writer = null;
	boolean getWriter = true;
	  
    // add aspect it createIfNotExist is true
    if (!nodeService.hasAspect(contentNode, CatConstants.ASPECT_TEXT_TRANSFORM)) { 
    	if (createIfNotExist) {
          nodeService.addAspect(contentNode, CatConstants.ASPECT_TEXT_TRANSFORM, null);
    	}
        else {
          getWriter = false;
        }
    }
    
    if ((contentService != null) && (getWriter)) {
      writer = contentService.getWriter(contentNode, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT, true); 
    }
    
    return writer;
  }
  

  /**
   * Sets all the properties on the given node at the same time, thus reducing hits
   * to database.
   *
   * @param node NodeRef
   * @param newProps Map<QName,Serializable>
   * @param nodeService NodeService
   */
  public static void setProperties(NodeRef node, Map<QName, Serializable> newProps, NodeService nodeService) {
    
    // should overwrite any properties coming from newProps
    Map<QName, Serializable> allProps = nodeService.getProperties(node);
    allProps.putAll(newProps);
    nodeService.setProperties(node, allProps);
  }
  
  
  /**
   * Creates a text transform nodeRef based on the nodeRef containing the text
   * transformation.  These are transient nodeRef's used by the 
   * file folder service to represent these text transformations
   * as files.
   * 
   * @param node NodeRef of the node containing the text transformation.
   * 
  
   * @return returns the new text transform nodeRef. */
  public static NodeRef createPhantomTextNode(NodeRef node) {
	  return new NodeRef(node.toString() + TEXT_TRANSFORM_NODE_REF_SUFFIX);
  }

  
  /**
   * Method isPhantomTextNode.
   * @param node NodeRef
   * @return boolean
   */
  public static boolean isPhantomTextNode(NodeRef node) {
	  return node.toString().endsWith(TEXT_TRANSFORM_NODE_REF_SUFFIX);			
  }
  
  
  /**
   * If the passed in NodeRef is a phantom node, representing a text transorm node via the
   * CIFS view, this method cleans up the NodeRef so it points to the real file.
   * 
   * @param node NodeRef of the phantom node
   * 
  
   * @return returns the nodeRef of the real text transform node that
   *         the phantom node was pointing to.  If the passed in node wasn't a phantom node,
   *         the NodeRef remains unchanged. */
  public static NodeRef convertPhantomTextNode(NodeRef node) {
	  
	  NodeRef ret = node;
	  
	  if (isPhantomTextNode(node)) {
		  String nodeStr = node.toString();
		  ret = new NodeRef(nodeStr.substring(0, nodeStr.length() - TEXT_TRANSFORM_NODE_REF_SUFFIX.length())); 
	  }
	  
	  return ret;
  }
  
}


