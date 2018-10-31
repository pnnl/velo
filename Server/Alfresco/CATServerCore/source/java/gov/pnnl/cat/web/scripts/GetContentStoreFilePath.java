package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.File;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetContentStoreFilePath extends AbstractCatWebScript {
  public static final String PARAM_PATH= "path";
  public static final String PARAM_UUID= "uuid";
  
  protected ContentStore fileContentStore;
  
  /**
   * Only call this web script if you cannot call UploadServlet
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the parameters
    String pathStr = req.getParameter(PARAM_PATH);
    String uuidStr = req.getParameter(PARAM_UUID);
    
    NodeRef nodeRef;
    
    if(uuidStr != null) {
      nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuidStr);
    } else {
      nodeRef = NodeUtils.getNodeByName(pathStr, nodeService);
    }
    
    
    ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    if (reader != null && reader.exists()) {
      FileContentReader csReader = (FileContentReader)fileContentStore.getReader(reader.getContentUrl());
      File file = csReader.getFile();
      writeMessage(res, file.getAbsolutePath());
    }
    
    return null;
    
  }

  public void setFileContentStore(ContentStore fileContentStore) {
    this.fileContentStore = fileContentStore;
  }  
  
}
