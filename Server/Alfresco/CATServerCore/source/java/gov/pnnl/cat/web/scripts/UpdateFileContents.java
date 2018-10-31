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

import gov.pnnl.velo.model.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Update content using a path or uuid and a content property
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class UpdateFileContents extends AbstractCatWebScript {
  public static final String PARAM_PATH= "path";
  public static final String PARAM_UUID= "uuid";
  public static final String PARAM_PROPERTY= "property";
  public static final String PARAM_MIMETYPE= "mimetype";
  public static final String PARAM_OFFSET= "offset";
  
  /**
   * Only call this web script if you cannot call UploadServlet
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
	  // Get the parameters
    String pathStr = req.getParameter(PARAM_PATH);
    String uuidStr = req.getParameter(PARAM_UUID);
    String propStr = req.getParameter(PARAM_PROPERTY);
    // We need a mimetype property as the mimetype could have changed
    String mimetype = req.getParameter(PARAM_MIMETYPE);
    
    //ONLY USED BY CALLBACK FILE SYSTEM.  Zoe noticed that when winrar 'edits' a zip file (via d&d a new file into the opened zip) it
    //will not start writing at position 0.
    String offsetStr = req.getParameter(PARAM_OFFSET);
    
    NodeRef nodeRef = WebScriptUtils.getNodeRef(pathStr, uuidStr, nodeService);
    QName propQname;
    
    if(propStr == null) {
      propQname = ContentModel.PROP_CONTENT; // default content property
    } else {
      propQname = QName.createQName(propStr);
    }
    
    ContentWriter writer = contentService.getWriter(nodeRef, propQname, true);
    if(mimetype != null) {
      writer.setMimetype(mimetype);
    }else{
      writer.setMimetype(mimetypeService.guessMimetype((String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)));
    }
    //if offset is passed in and is not zero, get a file channel to write to instead of overwritting the entire contents
    //copy from old FileSystem code how to write to a file at a given offset (and will need to truncate remaining bytes
    // after the offset's byte in case the file shrinks)
    if(offsetStr != null && !offsetStr.isEmpty()){
      System.out.println("writing beginning at offset: " + offsetStr);
      long offset = Long.parseLong(offsetStr);
      FileChannel fc = writer.getFileChannel(false);
      fc = fc.truncate(offset);//remove everything from the offset on in case the file has shrunk
      fc.position(offset);
      FileChannel appendFc = new FileInputStream(requestContent).getChannel(); 
      appendFc.transferTo(0, requestContent.length(), fc);  
    }else{
      writer.putContent(requestContent);
    }
    
    
    // Return modified resource
    // serialize via json
    Resource resource = WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService);
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), resource);
    
    return null;
  }

}
  
