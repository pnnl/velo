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
package gov.pnnl.velo.webscripts;


import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.exception.ProvenanceException;
import gov.pnnl.velo.tif.util.RegistryConstants;
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Deletes a set of resources (provided in request body) based on the option specified.
 * Option tells us what to do with associations:
 * 1) don't delete if assocs exist
 * 2) delete all assocs too
 * 3) move assocs to deleted node which is now in the archive (alfresco default)
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class AdvancedDelete extends AbstractVeloWebScript {
  public static final String PARAM_OPTION = "option";  
  
  // prevent deletes if provenance assocs are present on the node
  public static final String OPTION_PREVENT = "prevent";
  
  // delete node and all proveance assocs to it
  public static final String OPTION_FORCE = "force";
  
  // delete node and allow alfresco to reroute associations to the archived node
  // (this is the alfresco default behavior)
  public static final String OPTION_ARCHIVE = "archive";
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // Get the request parameters:
    String option = req.getParameter(PARAM_OPTION);
    if (option == null) {
      option = OPTION_ARCHIVE;
    }
    BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);
      delete(option, reader);
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }
  
  /**
   * Method delete.
   * @param option String
   * @param reader BufferedReader
   * @throws Exception
   */
  public void delete(String option, BufferedReader reader) throws Exception{

    // Read the request body to get the files to delete
    // Body is a file of wiki paths, one path per line
    
      String line;
      logger.debug("Trying to read request body");
      
      while ( (line = reader.readLine()) != null) {
        String wikiPath = line;
        // Convert the path to alfresco format
        String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
        
        // Find the parent node from the path (will throw exception if node does not exist)
        NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
        
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
      
      logger.debug("Done reading request body");

    

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
      if(namespace.equals(VeloServerConstants.NAMESPACE_ASCEM) || namespace.equals(RegistryConstants.NAMESPACE_REGISTRY)
          || namespace.equals(VeloServerConstants.NAMESPACE_VELO_PROVENANCE)) {
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
      if(namespace.equals(VeloServerConstants.NAMESPACE_ASCEM) || namespace.equals(RegistryConstants.NAMESPACE_REGISTRY)
          || namespace.equals(VeloServerConstants.NAMESPACE_VELO_PROVENANCE)) {
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
      if(namespace.equals(VeloServerConstants.NAMESPACE_ASCEM) || namespace.equals(RegistryConstants.NAMESPACE_REGISTRY)
          || namespace.equals(VeloServerConstants.NAMESPACE_VELO_PROVENANCE)) {
        String wikiPath = WikiUtils.getWikiPath(nodeRef, nodeService);
        String targetPath = WikiUtils.getWikiPath(target, nodeService);
        throw new ProvenanceException("Unable to delete " + wikiPath + " because it has relationship " + targetAssoc.getTypeQName() + " to " + targetPath);
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
      if(namespace.equals(VeloServerConstants.NAMESPACE_ASCEM) || namespace.equals(RegistryConstants.NAMESPACE_REGISTRY)
          || namespace.equals(VeloServerConstants.NAMESPACE_VELO_PROVENANCE)) {
        String wikiPath = WikiUtils.getWikiPath(nodeRef, nodeService);
        String sourcePath = WikiUtils.getWikiPath(source, nodeService);
        throw new ProvenanceException("Unable to delete " + wikiPath + " because it has relationship " + sourceAssoc.getTypeQName() + " to " + sourcePath);
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
  private void deleteNodeWithArchivedAssociations(NodeRef nodeRef) throws Exception {    
    // Delete the node
    nodeService.deleteNode(nodeRef);
  }
}
