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
package gov.pnnl.cat.policy.taxonomy;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class TaxonomyCopyServiceInterceptor implements MethodInterceptor {

  private static Log logger = LogFactory.getLog(TaxonomyCopyServiceInterceptor.class);
  private NodeService nodeService;
  
  public TaxonomyCopyServiceInterceptor() {
    
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * We intervene if we are copying a regular file inside
   * a taxonomy.  Instead of doing the full copy, just create
   * a link instead, which is more efficient.
   * 
   * For now we are only overloading the copy method (not the
   * copy-over-the-top flavor) because this is the only one we
   * are using in CAT.  But the copyAndRename method is used by
   * the Copy Action, so if somebody ever uses this action on a
   * taxonomy, then we better overload this method too.
   * @param mi MethodInvocation
   * @return Object
   * @throws Throwable
   * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
   */
  public Object invoke(MethodInvocation mi) throws Throwable {
      
    if(mi.getMethod().getName().startsWith("copy")) {     
      return checkCopyToTaxonomy(mi);     
    }
    
    return mi.proceed();

  }
  
  /**
   * Method checkCopyToTaxonomy.
   * @param mi MethodInvocation
   * @return Object
   * @throws Throwable
   */
  protected Object checkCopyToTaxonomy(MethodInvocation mi) throws Throwable {
    
    Object[] args = mi.getArguments();  
    
    // Get the source NodeRef
    NodeRef source = (NodeRef)args[0];

    // Get the destination parent NodeRef
    NodeRef destParent = (NodeRef)args[1];
    
    // See if we are copying a cm:content node into a taxonomy
    if(nodeService.getType(source).equals(ContentModel.TYPE_CONTENT)) {
      
      // See if we are copying to a taxonomy:
      if(nodeService.hasAspect(destParent, CatConstants.ASPECT_TAXONOMY_ROOT) ||
        nodeService.hasAspect(destParent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
        
        // create link to source file
        if(logger.isDebugEnabled())
          logger.debug("creating link instead of performing regular copy");
        return NodeUtils.createLinkedFile(source, destParent, nodeService);  
      }

      // See if we are copying a taxonomy link outside a taxonomy
    } else if (nodeService.hasAspect(source, CatConstants.ASPECT_TAXONOMY_LINK)) {
      
      if(!nodeService.hasAspect(destParent, CatConstants.ASPECT_TAXONOMY_ROOT) &&
        !nodeService.hasAspect(destParent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
        
        NodeRef originalFile = (NodeRef)nodeService.getProperty(source, ContentModel.PROP_LINK_DESTINATION);
        String name = (String)nodeService.getProperty(originalFile, ContentModel.PROP_NAME);
        QName childQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
        
        // Copy original file into destination parent folder
        // Only copy if original file doesn't already exist.  If it already exists in this
        // folder, then the copyService will throw an exception.
        CopyService copyService = (CopyService)mi.getThis();
        NodeRef copiedOriginal = copyService.copy(originalFile, destParent, ContentModel.ASSOC_CONTAINS, childQName, true);

        // rename the copied file because the copy method wipes this property
        nodeService.setProperty(copiedOriginal, ContentModel.PROP_NAME, name);
        return copiedOriginal;
      }
      
    }
    
    return mi.proceed();
  }
  

}
