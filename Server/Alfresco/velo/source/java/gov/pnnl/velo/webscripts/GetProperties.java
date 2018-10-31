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

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to get selected properties for a resource.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class GetProperties extends AbstractVeloWebScript {

  public static final String PARAM_WIKI_PATH = "wikiPath";
  public static final String PARAM_PROPS = "props";

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
    String wikiPath = req.getParameter(PARAM_WIKI_PATH);
    String propsString = req.getParameter(PARAM_PROPS);
    
    // Write the properties to the response
    PrintStream printStream = null;

    try {
      printStream = new PrintStream(res.getOutputStream());
      getProperties(wikiPath, propsString, printStream);
    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }  
    return null;
  }

  /**
   * Method getProperties.
   * @param wikiPath String
   * @param propsString String
   * @return Map<String,Object>
   * @throws Exception
   */
  public Map<String, Object> getProperties(String wikiPath, String propsString) throws Exception {
    // Convert the path to alfresco format
    String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);

    // Find the node from the path (will throw exception if node does not exist)
    NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);

    Map<String, Object> propValues = new HashMap<String, Object>();
    
      String[] props = propsString.split(",");
      for(int i = 0; i < props.length; i++) {

        QName qname = QName.createQName(props[i]);
        Serializable value = nodeService.getProperty(nodeRef, qname);
        propValues.put(props[i], value);
      }
      return propValues;
  }
  
  /**
   * Method getProperties.
   * @param wikiPath String
   * @param propsString String
   * @param printStream PrintStream
   * @throws Exception
   */
  public void getProperties(String wikiPath, String propsString, PrintStream printStream) throws Exception {
    Map<String, Object> propValues = getProperties(wikiPath, propsString);
    for (String prop : propValues.keySet()) {
      QName qname = QName.createQName(prop);
      Object value = propValues.get(prop);
        if(value == null) {
          value = "null";
        }          
        String valueStr = value.toString();

        if(value instanceof String) {
          valueStr = (String)value;

        } else if (value instanceof Boolean) {
          valueStr = String.valueOf((Boolean)value);

        } else if (value instanceof Integer) {
          valueStr = String.valueOf((Integer)value);

        } else if (value instanceof Double) { 
          valueStr = String.valueOf((Double)value);

        } else if (value instanceof Date) {
          long epochTime = ((Date)value).getTime();
          valueStr = String.valueOf(epochTime);

        } else {
          logger.warn("Unrecognizable property type: " + value.getClass().getName() + " value not interpreted.");
        }

        printStream.println(qname.toString() + "\t" + valueStr + "\t" + value.getClass().getName());
      }

      logger.debug("Done reading request body");
      printStream.flush();
  }


}
