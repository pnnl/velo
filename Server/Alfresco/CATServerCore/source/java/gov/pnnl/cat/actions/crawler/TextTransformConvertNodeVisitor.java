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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 */
public class TextTransformConvertNodeVisitor extends AbstractNodeVisitor {

  private TransactionService transactionService;

  private static final String TEXT_TRANSFORM_TYPE = "Raw Text";
  private static final String NAMESPACE_CAT = "http://www.pnl.gov/cat/model/content/1.0";
  private static final QName ASPECT_TRANSFORMABLE = QName.createQName(NAMESPACE_CAT, "transformable");
  private static final QName PROP_TRANSFORMATIONS = QName.createQName(NAMESPACE_CAT, "transformations");
  private static final QName PROP_TRANSFORMED_CONTENT = QName.createQName(NAMESPACE_CAT, "transformedContent");    
  private static final QName PROP_TRANSFORMER = QName.createQName(NAMESPACE_CAT, "transformer");
  private static final QName PROP_TRANSFORM_LABEL = QName.createQName(NAMESPACE_CAT, "transformLabel");  
  private static final QName PROP_TRANSFORM_ERROR = QName.createQName(NAMESPACE_CAT, "transformError"); 
  private static final QName ASSOC_TRANSFORM_CONTAINER = QName.createQName(NAMESPACE_CAT, "transformContainer");
  private static final QName NAME_TRANSFORMS_FOLDER = QName.createQName(NAMESPACE_CAT, "Transforms");


  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
   */
  @Override
  public String getName() {
    return "textTransformConvertTreeCrawler";
  }

  /**
   * Method visitNode.
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

    // only process nodes that have the old TRANSFORMABLE aspect on them
    NodeRef transformNode = getTransformNode(nodeRef, TEXT_TRANSFORM_TYPE, unprotectedNodeService); 

    if (transformNode != null) {

      // Add the new aspect
      unprotectedNodeService.addAspect(nodeRef, CatConstants.ASPECT_TEXT_TRANSFORM, null);

      // Copy properties from old text transform node
      Map<QName, Serializable> transformNodeProperties = unprotectedNodeService.getProperties(transformNode);
      Map<QName, Serializable> fNodeProperties = unprotectedNodeService.getProperties(nodeRef);
      fNodeProperties.put(CatConstants.PROP_TEXT_TRANSFORMED_CONTENT, transformNodeProperties.get(PROP_TRANSFORMED_CONTENT));
      fNodeProperties.put(CatConstants.PROP_TEXT_TRANSFORMER, transformNodeProperties.get(PROP_TRANSFORMER));
      fNodeProperties.put(CatConstants.PROP_TEXT_TRANSFORM_LABEL, transformNodeProperties.get(PROP_TRANSFORM_LABEL));
      fNodeProperties.put(CatConstants.PROP_TEXT_TRANSFORM_ERROR, transformNodeProperties.get(PROP_TRANSFORM_ERROR));
      unprotectedNodeService.setProperties(nodeRef, fNodeProperties);


      // remove transform child folder
      NodeRef transformFolder = 
        unprotectedNodeService.getChildByName(nodeRef, 
            ASSOC_TRANSFORM_CONTAINER, 
            NAME_TRANSFORMS_FOLDER.getLocalName());
      if (transformFolder != null) {
        unprotectedNodeService.deleteNode(transformFolder);
      }

      // remove old aspect
      unprotectedNodeService.removeAspect(nodeRef, ASPECT_TRANSFORMABLE);

    }


//  Previous transform action           
//  // only process nodes that still have the old TEXT_TRANSFORMED aspect on them
//  if (unprotectedNodeService.hasAspect(fNodeRef, CatConstants.ASPECT_TEXT_TRANSFORMED)) {

//  // check and see if there is a txt node in the transforms area of this node
//  NodeRef textnode = TransformUtils.getTransformReader(fNodeRef, unprotectedNodeService, null);
//  if (textnode == null || (unprotectedNodeService.exists(textnode) == false)) {
//  // none found, create one

//  textExtractor.execute(null, fNodeRef);
//  unprotectedNodeService.removeAspect(fNodeRef, CatConstants.ASPECT_TEXT_TRANSFORMED);

//  }
//  } 
//  else {
//  // force the transform to be re-generated anyway, since the format may have changed
//  textExtractor.execute(null, fNodeRef);
//  }



  }


  /**
   * Method getTransformNode.
   * @param contentNode NodeRef
   * @param transformType String
   * @param nodeService NodeService
   * @return NodeRef
   */
  private NodeRef getTransformNode(NodeRef contentNode, String transformType, NodeService nodeService) {

    if (nodeService.hasAspect(contentNode, ASPECT_TRANSFORMABLE) == false) {
      return null;
    }

    Serializable value = nodeService.getProperty(contentNode, PROP_TRANSFORMATIONS);
    Collection<String> transformations;

    if(value != null) {
      transformations = DefaultTypeConverter.INSTANCE.getCollection(String.class, value);
      //logger.debug("transformations size = " + transformations.size());

      for(String transform : transformations ) {
        //logger.debug("transformation string: " + transform);
        StringTokenizer tokenizer = new StringTokenizer(transform, ";");

        String thisUuid = tokenizer.nextToken().trim();
        String thisTransformType = tokenizer.nextToken().trim();

        // if transform type matches, return the noderef
        if (thisTransformType.equals(transformType)) {
          StoreRef catStore = CatConstants.SPACES_STORE;
          return new NodeRef(catStore, thisUuid);
        }
      }
    }

    return null;
  }

}
