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

import gov.pnnl.velo.util.WikiUtils;

import java.io.BufferedReader;
import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public abstract class AbstractRelationshipWebScript extends AbstractVeloWebScript {
  /**
   */
  public enum Operation {CREATE, DELETE};
  
  /**
   * Method parseFile.
   * @param reader BufferedReader
   * @param op Operation
   * @throws Exception
   */
  protected void parseFile(BufferedReader reader, Operation op) throws Exception {
    
    // Read the request body to get the relationships.
    // Body is tab delimited file, one relationships per line:
    // node1Path /t association /t node2Path
    // Assumes node1 and node2 already exist .
      String line;
      logger.debug("Trying to read request body");
      
      while ( (line = reader.readLine()) != null) {
        parseLine(line, op);
      }
      
      logger.debug("Done reading request body");

   
  }
  
  /**
   * Method parseLine.
   * @param line String
   * @param op Operation
   * @throws Exception
   */
  protected void parseLine(String line, Operation op) throws Exception {

    String[] parts = line.split("\t");
    String node1Path = parts[0];
    String assocType = parts[1];
    String node2Path = parts[2];

    node1Path = WikiUtils.getAlfrescoNamePath(node1Path);
    node2Path = WikiUtils.getAlfrescoNamePath(node2Path);
    
    // Find the nodes from the path (will throw exception if node does not exist)
    NodeRef node1 = WikiUtils.getNodeByName(node1Path, nodeService);
    NodeRef node2 = WikiUtils.getNodeByName(node2Path, nodeService);
    
    QName assocTypeQName = QName.createQName(assocType);
    
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
      logger.debug("Creating association: " + node1Path + " " + assocType + " " + node2Path);
      nodeService.createAssociation(node1, node2, assocTypeQName);
    
    } else if (op == Operation.DELETE && found) {
      logger.debug("Deleting association: " + node1Path + " " + assocType + " " + node2Path);
      nodeService.removeAssociation(node1, node2, assocTypeQName);
    }
  
  }

}
