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
package gov.pnnl.cat.web.scripts;

import gov.pnnl.velo.model.Relationship;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 */
public abstract class AbstractRelationshipWebScript extends AbstractCatWebScript {
  
  public enum Operation {CREATE, DELETE};
  
  protected Object executeRelationshipWebScript(WebScriptRequest req, WebScriptResponse res, 
      File requestContent, Operation op) throws Exception {
    
    // Run as admin so anyone can attach relationships to a node
    // TODO: add whether to force the relationship creation as a web script parameter
    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    
    ArrayList<Relationship> relationships = null;
    ObjectMapper mapper = new ObjectMapper();

    // TODO: use a multipart form so it works better with jquery.ajax calls
    if(requestContent.length() == 0) {
      String jsonString = req.getParameter("jsonString");
      relationships = mapper.readValue(jsonString, new TypeReference<ArrayList<Relationship>>() {});
      
    } else {
      relationships = mapper.readValue(requestContent, new TypeReference<ArrayList<Relationship>>() {});
    }
    
    for(Relationship relationship : relationships) {
      executeRelationshipAction(relationship, op);
    }
    
    return null;
  }
  
  /**
   * Method parseLine.
   * @param line String
   * @param op Operation
   * @throws Exception
   */
  protected void executeRelationshipAction(Relationship relationship, Operation op) throws Exception {

    // Find the nodes from the path or uuid (will throw exception if node does not exist)
    NodeRef node1 = WebScriptUtils.getNodeRef(relationship.getSourcePath(), relationship.getSourceResourceUuid(), nodeService);
    NodeRef node2 = WebScriptUtils.getNodeRef(relationship.getDestinationPath(), relationship.getDestinationResourceUuid(), nodeService);
    
    // If for some reason, one of the endpoints is null it doesn't exist anymore, so just return 
    // TODO: do we need to return a status in the response?
    if(node1 == null || node2 == null) {
      if(node1 == null) {
        logger.error("Unable to " + op + " relationship from node " + relationship.getSourcePath() + " because it no longer exists");
      }
      if(node2 == null) {
        logger.error("Unable to " + op + " relationship to node " + relationship.getDestinationPath() + " because it no longer exists");
      }
      
      return;
    }
    
    QName assocTypeQName = QName.createQName(relationship.getRelationshipType());
    
    // Check if the association exists
    // Unfortunately, Alfresco does not have an API that lets us check if an assoc exists,
    // so we have to loop through all the assocs, which isn't very efficient when you could
    // just query the DB once.  I'll post a JIRA about it.
    List<AssociationRef> assocs = nodeService.getTargetAssocs(node1, assocTypeQName);
    boolean found = false;
    for(AssociationRef assoc : assocs) {
      if(assoc.getSourceRef().equals(node1) && assoc.getTargetRef().equals(node2)) {
        found = true;
        break;
      }
    }
    if(op == Operation.CREATE && !found) {
      nodeService.createAssociation(node1, node2, assocTypeQName);
    
    } else if (op == Operation.DELETE && found) {
      nodeService.removeAssociation(node1, node2, assocTypeQName);
    }
  
  }

}
