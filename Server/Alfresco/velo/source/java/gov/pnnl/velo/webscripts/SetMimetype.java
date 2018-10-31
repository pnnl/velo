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

import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to delete a resource.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class SetMimetype extends AbstractVeloWebScript {
  public static final String PARAM_WIKI_PATH = "wikiPath";  
  public static final String PARAM_MIMETYPE = "mimetype";  
  
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
    String mimetype = req.getParameter(PARAM_MIMETYPE);
    
    setMimetype(wikiPath, mimetype);
    return null;
  }

  /**
   * Method setMimetype.
   * @param wikiPath String
   * @param mimetype String
   * @throws FileNotFoundException
   */
  public void setMimetype(String wikiPath, String mimetype) throws FileNotFoundException {
    // Convert the path to alfresco format
    String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
    
    // Find the node from the path (will throw exception if node does not exist)
    NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
    
    // Set Mimetype
    FileInfo info = fileFolderService.getFileInfo(nodeRef);
    if(info.isFolder()) {
      nodeService.setProperty(nodeRef, VeloServerConstants.PROP_MIMEYPE, mimetype);
    } else {
      ContentData oldContent = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
      ContentData content = ContentData.setMimetype(oldContent, mimetype);
      nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, content); 
    }    
  }

}
