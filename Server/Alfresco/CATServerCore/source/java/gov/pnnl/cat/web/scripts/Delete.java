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
/**
 * 
 */
package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.exception.ProvenanceException;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Delete a bunch of resources in one call.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class Delete extends AbstractCatWebScript {

  public static final String PARAM_OPTION = "option";  
  
  // prevent deletes if provenance assocs are present on the node
  public static final String OPTION_PREVENT = "prevent";
  
  // delete node and all proveance assocs to it
  public static final String OPTION_FORCE = "force";
  
  // delete node and allow alfresco to reroute associations to the archived node
  // (this is the alfresco default behavior)
  public static final String OPTION_ARCHIVE = "archive";

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String option = req.getParameter(PARAM_OPTION);

    ArrayList<Resource> nodesToDelete = null;
    ObjectMapper mapper = new ObjectMapper();

    // TODO: Until we can figure out how to get jQuery.ajax to submit json in request body, we have to be able to accept 
    // json as a parameter OR in the request body
    if(requestContent.length() == 0) {
      String jsonString = req.getParameter("jsonString");
      nodesToDelete = mapper.readValue(jsonString, new TypeReference<ArrayList<Resource>>() {});
      
    } else {
      nodesToDelete = mapper.readValue(requestContent, new TypeReference<ArrayList<Resource>>() {});
    }
    
    delete(nodesToDelete, option);
    
    return null;
  }
  
  public void delete(List<Resource> nodesToDelete, String option) {
    if (option == null) {
      option = OPTION_ARCHIVE;
    }

    for(Resource resource : nodesToDelete) {
      delete(resource, option);
    }    
  }
  
  private void delete(Resource resource, String option) {
    NodeRef nodeRef = WebScriptUtils.getNodeRef(resource, nodeService);

    // only delete if the node exists
    if(nodeRef != null && nodeService.exists(nodeRef)) {
      
      if(option.equals(OPTION_PREVENT)) {
        // note that this has performance issues because we have to recurse through the entire
        // subtree to check for associations on a node-by-node basis
        checkProvenanceAssociations(nodeRef);
        deleteNodeWithArchivedAssociations(nodeRef);
        
      } else if (option.equals(OPTION_FORCE)) {
        deleteProvenanceAssociations(nodeRef);
        deleteNodeWithArchivedAssociations(nodeRef);
      
      } else {
        deleteNodeWithArchivedAssociations(nodeRef);
        
      }
    }

  }

  
  /**
   * TODO: we may need to run this as admin if we don't have permissions to remove the associations,
   * if some other user created the association to a document that this user doesn't have access to.
   * I'm not sure if this case will throw an access exception or not.
   * @param nodeRef
   */
  private void deleteProvenanceAssociations(NodeRef nodeRef) {
    
    for(ChildAssociationRef child : nodeService.getChildAssocs(nodeRef)) {
      // only recurse on folders
      if(dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_FOLDER)) {
        checkProvenanceAssociations(child.getChildRef());
      }
    }

    // remove all associations where I am the source (i.e, me->node)
    for(AssociationRef targetAssoc : nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL)) {
      NodeRef target = targetAssoc.getTargetRef();
      String namespace = targetAssoc.getTypeQName().getNamespaceURI();
      targetAssoc.getTypeQName().getNamespaceURI();
      if(!target.getStoreRef().equals(CatConstants.SPACES_STORE)) {
        continue;
      }
      // Only remove associations that have the velo namespace
      if(namespace.equals(VeloConstants.NAMESPACE_VELO)) {
        nodeService.removeAssociation(nodeRef, target, targetAssoc.getTypeQName());
      }
    }   
    
    // remove all associations where I am the target (i.e., node->me)
    for(AssociationRef sourceAssoc : nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL)) {
      NodeRef source = sourceAssoc.getSourceRef();
      String namespace = sourceAssoc.getTypeQName().getNamespaceURI();
      sourceAssoc.getTypeQName().getNamespaceURI();
      if(!source.getStoreRef().equals(CatConstants.SPACES_STORE)) {
        continue;
      }
      // Only remove associations that have the velo namespace
      if(namespace.equals(VeloConstants.NAMESPACE_VELO)) {
        nodeService.removeAssociation(source, nodeRef, sourceAssoc.getTypeQName());
      }
    }
     
  }
  
  /**
   *
   * @param nodeRef
   */
  private void checkProvenanceAssociations(NodeRef nodeRef) {
    // throw exception if any resource under the tree has a provenance assoc

    // Get all associations where I am the source (i.e, me->node)
    for(AssociationRef targetAssoc : nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL)) {
      NodeRef target = targetAssoc.getTargetRef();
      String namespace = targetAssoc.getTypeQName().getNamespaceURI();
      targetAssoc.getTypeQName().getNamespaceURI();
      if(!target.getStoreRef().equals(CatConstants.SPACES_STORE)) {
        continue;
      }
      if(namespace.equals(VeloConstants.NAMESPACE_VELO)) {
        String path = NodeUtils.getNamePath(nodeRef, nodeService);
        String targetPath = NodeUtils.getNamePath(target, nodeService);
        throw new ProvenanceException("Unable to delete " + path + " because it has relationship " + targetAssoc.getTypeQName() + " to " + targetPath);
      }
    }
    
    
    // Get all associations where I am the target (i.e., node->me)
    for(AssociationRef sourceAssoc : nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL)) {
      NodeRef source = sourceAssoc.getSourceRef();
      String namespace = sourceAssoc.getTypeQName().getNamespaceURI();
      sourceAssoc.getTypeQName().getNamespaceURI();
      if(!source.getStoreRef().equals(CatConstants.SPACES_STORE)) {
        continue;
      }
      if(namespace.equals(VeloConstants.NAMESPACE_VELO)) {
        String path = NodeUtils.getNamePath(nodeRef, nodeService);
        String sourcePath = NodeUtils.getNamePath(source, nodeService);
        throw new ProvenanceException("Unable to delete " + path + " because it has relationship " + sourceAssoc.getTypeQName() + " to " + sourcePath);
      }
    }
    
    for(ChildAssociationRef child : nodeService.getChildAssocs(nodeRef)) {
      // only recurse on folders
      if(dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_FOLDER)) {
        checkProvenanceAssociations(child.getChildRef());
      }
    }
  }
  
  
  /**
   * Method deleteNodeWithArchivedAssociations.
   * @param nodeRef NodeRef
   * @throws Exception
   */
  private void deleteNodeWithArchivedAssociations(NodeRef nodeRef) {    
    // Delete the node
    nodeService.deleteNode(nodeRef);
  }
}
