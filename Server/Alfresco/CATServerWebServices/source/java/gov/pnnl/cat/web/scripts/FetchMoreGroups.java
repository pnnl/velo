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

import gov.pnnl.cat.server.webservice.group.GroupQueryResults;
import gov.pnnl.cat.server.webservice.group.GroupWebService;

import java.io.File;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fetch more groups from query session.  For now this web script just forwards calls to GroupWebService to
 * avoid axis calls from client.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class FetchMoreGroups extends AbstractCatWebScript {
  public static final String PARAM_QUERY_SESSION_ID = "querySessionId";  

  protected GroupWebService groupWebService;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    String querySessionId = req.getParameter(PARAM_QUERY_SESSION_ID);
    ObjectMapper mapper = new ObjectMapper();

    GroupQueryResults queryResults = groupWebService.fetchMoreGroupsImpl(querySessionId);
    mapper.writeValue(res.getOutputStream(), queryResults);
    return null;

  }

  public void setGroupWebService(GroupWebService groupWebService) {
    this.groupWebService = groupWebService;
  }
  

}
