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
import gov.pnnl.cat.util.NodeUtils;

import java.io.File;
import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to check if a resource's content has changed
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class IsIdentical extends AbstractCatWebScript {
  public static final String PARAM_PATH = "path";  
  public static final String PARAM_UUID = "uuid";  
  public static final String PARAM_HASH = "hash";  
  public static final String PARAM_PROPERTY = "property";  
  
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
    String path = req.getParameter(PARAM_PATH);
    String uuid = req.getParameter(PARAM_UUID);
    String hash = req.getParameter(PARAM_HASH);
    String propStr = req.getParameter(PARAM_PROPERTY);

    QName propQname;
    if(propStr == null) {
      propQname = ContentModel.PROP_CONTENT;
    } else {
      propQname = QName.createQName(propStr);
    }
    
    NodeRef nodeRef = WebScriptUtils.getNodeRef(path, uuid, nodeService);
    if (nodeRef == null) {
      throw new RuntimeException(path + ":" + uuid + " does not exist!");
    }

    boolean isIdentical = isIdentical(nodeRef, hash, propQname);
    // write the response to the output stream
    writeMessage(res, String.valueOf(isIdentical));

    return null;
  }

  /**
   * Method isIdentical.
   * @param path String
   * @param hash String
   * @param propQname QName
   * @return boolean
   * @throws Exception
   */
  public boolean isIdentical(NodeRef nodeRef, String hash, QName propQname) throws Exception{
    
    boolean isIdentical = false;

    String md5Hash = (String)nodeService.getProperty(nodeRef, CatConstants.PROP_HASH);

    // md5Hash hasn't been computed yet
    if(md5Hash == null) {
      ContentReader reader = contentService.getReader(nodeRef, propQname);
      
      if(reader != null) {
        InputStream input = reader.getContentInputStream();
        if(input != null) {
          md5Hash = NodeUtils.createMd5Hash(input);
        }
      }
    }
    
    if(md5Hash != null && md5Hash.equals(hash)) {
      isIdentical = true;
    }
   return isIdentical;
  }
  
}
