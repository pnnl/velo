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

import gov.pnnl.cat.server.webservice.group.GroupFilter;
import gov.pnnl.cat.server.webservice.group.GroupQueryResults;
import gov.pnnl.cat.server.webservice.group.GroupWebService;

import java.io.File;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Query groups.  For now this web script just forwards calls to GroupWebService to
 * avoid axis calls from client.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class QueryGroups extends AbstractCatWebScript {
  
  protected GroupWebService groupWebService;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    
    ObjectMapper mapper = new ObjectMapper();
    GroupFilter filter = null;
    if(requestContent.length() > 0) {
      filter = mapper.readValue(requestContent, GroupFilter.class);
    }
    GroupQueryResults queryResults = groupWebService.queryGroupsImpl(filter);

    // TODO: figure out how to get polymorphism to work with jackson using their crappy
    // mixin annotations, or else find a different json parser
    // For now, we will use XStream...
    mapper.writeValue(res.getOutputStream(), queryResults);
    //XmlUtility.serializeToOutputStream(queryResults, res.getOutputStream());
    return null;

  }

  public void setGroupWebService(GroupWebService groupWebService) {
    this.groupWebService = groupWebService;
  }

}
