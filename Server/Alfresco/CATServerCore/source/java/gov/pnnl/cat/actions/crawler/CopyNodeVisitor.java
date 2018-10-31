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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copies the current node to the destination node.  This is NOT 
 * a singleton bean, so all instances will be separate.
 * @version $Revision: 1.0 $
 */
public  class CopyNodeVisitor extends AbstractNodeVisitor {

  private static Log logger = LogFactory.getLog(CopyNodeVisitor.class);
  protected final static String COPY_TO = "copy-to";
  protected final static String COPY_FROM = "copy-from";
  protected final static String ASSOC_NAME = "assocName";

  // For each folder to copy, keep track of the corresponding destination folder
  // Make sure this is NOT a singleton bean, so this will be refreshed with each copy
  // Otherwise we need to make this a thread local variable
  protected Map<NodeRef, NodeRef> destinationNodes = new HashMap<NodeRef, NodeRef>();
  protected QName assocName;
  protected DictionaryService dictionaryService;
  protected CopyService copyService;
  protected NodeRef copyFromRef;
  
  /**
   * @param dictionaryService the dictionaryService to set
   */
  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * @param copyService the copyService to set
   */
  public void setCopyService(CopyService copyService) {
    this.copyService = copyService;
  }

  /* (non-Javadoc)
   * @see gov.pnl.dmi.actions.crawler.AbstractNodeVisitor#setup(java.util.Map)
   */
  @Override
  public void setup(Map<String, Serializable> parameters) {
    String copyFrom = (String)parameters.get(CopyNodeVisitor.COPY_FROM);
    String copyTo = (String)parameters.get(CopyNodeVisitor.COPY_TO);
    assocName = (QName)parameters.get(CopyNodeVisitor.ASSOC_NAME);
    
    if(copyFrom == null || copyTo == null) {
      throw new RuntimeException("CopyNodeVisitor called with null param copy-from or copy-to");
    }
    copyFromRef = new NodeRef(copyFrom);
    NodeRef copyToRef = new NodeRef(copyTo);
    NodeRef copyFromParentFolder = nodeService.getPrimaryParent(copyFromRef).getParentRef();
    destinationNodes.put(copyFromParentFolder, copyToRef);
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
   */
  @Override
  public String getName() {
    return "CopyNodeVisitor";
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
   
    // First check read permissions on the node - if the user doesn't have read permissions, 
    // don't copy it.  It's faster to do it here in batches rather then in getChildren for large
    // folders.
    // We can't catch access denied exceptions on the public node service because the transaction
    // interceptor will automatically roll back the tx if an exception is thrown
    AccessStatus access = permissionService.hasPermission(nodeRef, PermissionService.READ);
    if(access.equals(AccessStatus.DENIED)) {
      // ignore these - we just won't copy a child if the user doesn't have access
      logger.error("Acess denied trying to copy child: " + nodeRef + "  Ingoring.");
      return;
    }
    
    ChildAssociationRef childAssoc = unprotectedNodeService.getPrimaryParent(nodeRef);
    QName nodeType = unprotectedNodeService.getType(nodeRef);
    QName assocType = childAssoc.getTypeQName();
    QName assocName = childAssoc.getQName();
    NodeRef parent = childAssoc.getParentRef();
    NodeRef destinationNode = destinationNodes.get(parent);
    NodeRef newNode = null;
    
    if(dictionaryService.isSubClass(nodeType, ContentModel.TYPE_FOLDER)) {
      // If we are copying the first folder in the hierarchy, then use the passed in
      // assocName instead (in case we pass in a different assoc name because we want to
      // change it
      if(nodeRef.equals(copyFromRef)) {
        assocName = this.assocName;
      }
      
      // create new folder and copy over the top
      // Looks like Alfresco no longer tries to do a recursive copy and
      // copy all the children when you call this "copy over the top" method
      // so we can use the standard method implementation now.  CSL - 12/28/09
      newNode = nodeService.createNode(destinationNode, assocType, assocName, nodeType).getChildRef();
      copyService.copy(nodeRef, newNode);

      // reset the name property, since the copy service removes it
      String sourceName;
      
      // If we are copying the first folder in the hierarchy, then use the passed in
      // assocName instead (in case we want to change the name)
      if(nodeRef.equals(copyFromRef)) {
        sourceName = this.assocName.getLocalName();
      } else {
        sourceName = (String)unprotectedNodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      }
      
      unprotectedNodeService.setProperty(newNode, ContentModel.PROP_NAME, sourceName);

    } else {

      // copy the node
      //Long start = new Long(System.currentTimeMillis());
      //AlfrescoTransactionSupport.bindResource("copyServiceCallStart", start);
      newNode = copyService.copyAndRename(nodeRef, destinationNode, assocType, assocName, true); 
    }

    // assign new node to map
    destinationNodes.put(nodeRef, newNode);
  }
  
}
