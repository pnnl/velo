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
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to set properties on a resource.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class SetProperties extends AbstractVeloWebScript {
  public static final String PARAM_WIKI_PATH = "wikiPath";  
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // Get the request parameters:
    String wikiPath = req.getParameter(PARAM_WIKI_PATH);
    BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);
      setProperties(wikiPath, reader);
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }

  /**
   * Method setProperties.
   * @param wikiPath String
   * @param reader BufferedReader
   * @throws Exception
   */
  public void setProperties(String wikiPath, BufferedReader reader) throws Exception {
 // Convert the path to alfresco format
    String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
    
    // Find the node from the path (will throw exception if node does not exist)
    NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
    
    // Read the properties from the request body
    String line = "";
    try {
      logger.debug("Trying to read request body");
      
      while ( (line = reader.readLine()) != null) {
        logger.debug("Setting property: " + line);
        String[] parts = line.split("\t");
        String propName = parts[0];
        String valueStr = parts[1];
        // Default to type string if parameter type not sent
        String valueClass = String.class.getName();
        if(parts.length > 2) {
          valueClass = parts[2];
        }
        Class valueType = Class.forName(valueClass);
        Serializable value = null;

        QName qname = QName.createQName(propName);

        // null value of property will delete it
        if(!valueStr.equals("null")) {
          if(valueType.equals(String.class)) {
            value = valueStr;
            
          } else if (valueType.equals(Boolean.class)) {
            value = Boolean.valueOf(valueStr);
          
          } else if (valueType.equals(Integer.class)) {
            value = Integer.valueOf(valueStr);
            
          } else if (valueType.equals(Double.class)) { 
            value = Double.valueOf(valueStr);
            
          } else if (valueType.equals(Date.class)) {
            long epochTime = Long.valueOf(valueStr);
            value = new Date(epochTime);
            
          } else {
            throw new RuntimeException("Invalid property type specified: " + valueClass);
          }
        
          nodeService.setProperty(nodeRef, qname, value);
          
        } else {
          nodeService.removeProperty(nodeRef, qname);
        }
      }
      logger.debug("Done reading request body");

    } catch (Throwable e) {
      logger.error("Failed to set properties for: " + wikiPath + "\n line = " + line, e);
      throw new RuntimeException(e);
    }      
  }

}
