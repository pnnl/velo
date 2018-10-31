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

import gov.pnnl.cat.pipeline.impl.ThumbnailProcessor;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Get a thumbnail for a single resource. Creates the thumbnail if it does not
 * exist.
 * 
 * @author d3k339
 * 
 * @version $Revision: 1.0 $
 */
public class GetThumbnail extends AbstractCatWebScript {
  public static final String PARAM_PATH = "path";

  public static final String PARAM_THUMBNAIL_NAME = "thumbnailName";

  private ThumbnailService thumbnailService;

  private ThumbnailProcessor thumbnailProcessor;

  /**
   * Method setThumbnailService.
   * 
   * @param thumbnailService
   *          ThumbnailService
   */
  public void setThumbnailService(ThumbnailService thumbnailService) {
    this.thumbnailService = thumbnailService;
  }

  /**
   * Method executeImpl.
   * 
   * @param req
   *          WebScriptRequest
   * @param res
   *          WebScriptResponse
   * @param requestContent
   *          File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // Get the request parameters:
    String path = req.getParameter(PARAM_PATH);
    NodeRef nodeRef = NodeUtils.getNodeByName(path, nodeService);
    String thumbnailName = req.getParameter(PARAM_THUMBNAIL_NAME);
  
    String encoded = getEncodedThumbnailString(nodeRef, thumbnailName);
    if(encoded != null) {
      writeMessage(res, encoded);
      
    } else {
      writeMessage(res, "");
    }
    
    return null;

  }
  
  public String getEncodedThumbnailString(NodeRef nodeRef, String thumbnailName) {
    
    if (thumbnailName == null) {
      thumbnailName = VeloConstants.THUMBNAIL_PREVIEW_PANE;
    }

    NodeRef thumbnail = NodeUtils.getThumbnail(thumbnailService, thumbnailProcessor, nodeService, nodeRef, thumbnailName);
    String encoded = null;
    if (thumbnail != null) {

      FileContentReader reader = (FileContentReader) contentService.getReader(thumbnail, ContentModel.PROP_CONTENT);
      encoded = NodeUtils.base64EncodeFile(reader.getFile());
    } 
    
    return encoded;
  }

  public void setThumbnailProcessor(ThumbnailProcessor thumbnailProcessor) {
    this.thumbnailProcessor = thumbnailProcessor;
  }

}
