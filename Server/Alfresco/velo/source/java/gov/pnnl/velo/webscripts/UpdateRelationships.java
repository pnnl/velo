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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to create a set of relationship triples.
 * (node-association-node)
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class UpdateRelationships extends AbstractRelationshipWebScript {
  
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
    
    // Read the request body to get the relationships.
    // Body is tab delimited file, one relationships per line:
    // node1Path /t association /t node2Path
    // Assumes node1 and node2 already exist .
    BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);
      String line;
      logger.debug("Trying to read request body");
      Operation op = AbstractRelationshipWebScript.Operation.CREATE;
      
      while ( (line = reader.readLine()) != null) {
        if(line.trim().equalsIgnoreCase("CREATE")) {
          op = AbstractRelationshipWebScript.Operation.CREATE;
          
        } else if (line.trim().equalsIgnoreCase("DELETE")) {
          op = AbstractRelationshipWebScript.Operation.DELETE;
          
        } else {
          parseLine(line, op);
        }
      }
      
      logger.debug("Done reading request body");

    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }    
    return null;
  }

}
