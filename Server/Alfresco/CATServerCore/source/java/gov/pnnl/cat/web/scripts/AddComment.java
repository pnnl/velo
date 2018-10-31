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
import org.apache.commons.io.FileUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 */
public class AddComment extends AbstractCatWebScript {

  public static final String PARAM_UUID = "uuid";  // uuid to the node you which to add a comment to
  public static final String PARAM_COMMENT = "comment";// uuid to the node you which to add a comment to
  

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
        
    // Comment text is in the request body
    String commentText = null;
    if(requestContent.length() > 0) {
        commentText = FileUtils.readFileToString(requestContent);
    } else {
        commentText = req.getParameter(PARAM_COMMENT);
    }
    
    Comment comment = CommentUtils.addComment(nodeRef, commentText);
    
    // return the new comment in json format
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), comment);
    
    return null;
  }




}
