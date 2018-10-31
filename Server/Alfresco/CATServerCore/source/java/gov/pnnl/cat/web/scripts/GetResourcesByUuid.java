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
import gov.pnnl.velo.model.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Look up the given resources, including all properties and aspects for each 
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetResourcesByUuid extends AbstractCatWebScript {
  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Read the request body to get the uuids to look up
    // Body is a file of link params, one remote link per line
    
    BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);

      String line;
      logger.debug("Trying to read request body");
      ArrayList<Resource> nodes = new ArrayList<Resource>();
      
      while ( (line = reader.readLine()) != null) {
        String uuid = line;
        try {
          Resource resource = getNode(uuid);
          if(resource != null) {
            nodes.add(resource);  
          }
        } catch (Throwable e) {
          // ignore
        }
      }

      // write the results to the output stream
      // serialize children via json
      res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(res.getOutputStream(), nodes);

      
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }
  
  /**
   * Method getNode.
   * @param uuid String
   * @return Resource
   * @throws Exception
   */
  protected Resource getNode(String uuid) throws Exception {
    NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);
    return WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService);
  }
  
}
