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

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.CommentUtils;
import gov.pnnl.velo.model.Comment;

import java.io.File;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Get all comments for the given node.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetComments extends AbstractCatWebScript {

  public static final String PARAM_UUID = "uuid";  // uuid to the node you which to get comments for
  

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
    String uuid = req.getParameter(PARAM_UUID);
    NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);
        
    // Get the comments
    Comment[] comments = CommentUtils.getComments(nodeRef);
    
    // write the results to the output stream
    // serialize via json
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), comments);
    
    return null;
    
  }




}
