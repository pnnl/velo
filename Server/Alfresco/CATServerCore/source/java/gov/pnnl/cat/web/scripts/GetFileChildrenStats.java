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

import java.io.File;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

/**
 * Search for up the given resources, including all properties and aspects for each 
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetFileChildrenStats extends AbstractCatWebScript {
  
  public static final String PARAM_UUID = "uuid";
  
   /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    // <url>/cat/getFileChildren?uuid={uuid}&pageNumber={?pageNumber}&maxItems={?maxItems}&sortByProp={?sortByProp}&order={?order}</url>
    String uuid = req.getParameter(PARAM_UUID);

    // Compute the path string
    NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);
    ObjectMapper mapper = new ObjectMapper();
    long[] stats = NodeUtils.getChildrenStatsRecursive(nodeService, contentService, nodeRef);
    mapper.writeValue(res.getOutputStream(), stats);
    return null;
  }
  
  
}
