package gov.pnnl.cat.web.scripts;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateEmptyFile extends CreateUpdateResources {

  protected ContentStore fileContentStore;

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Resource nodeToCreate = mapper.readValue(requestContent, Resource.class);

    List<Resource> newResources = createNode(nodeToCreate);
    
    // write the results to the output stream
    // serialize children via json
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), newResources.get(0));

    return null;
  }

  /**
   * Method createFolders.
   * 
   * @param foldersToCreate
   *          ResourceList
   * @return ResourceList
   * @throws Exception
   */
  private List<Resource> createNode(Resource nodeToCreate) throws Exception {
    String name = new CmsPath(nodeToCreate.getPath()).getName();
    ContentWriter writer = fileContentStore.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
    File file = ((FileContentWriter) writer).getFile();
    String contentUrl = writer.getContentUrl();
    String mimetype = mimetypeService.guessMimetype(name);
    ContentData contentData = new ContentData(contentUrl, mimetype, 0,  "UTF-8");
    nodeToCreate.setProperty(VeloConstants.PROP_CONTENT, contentData.getInfoUrl());

    ArrayList<Resource> resources = new ArrayList<Resource>();
    resources.add(nodeToCreate);
    return createUpdateResources(resources);
    
    // Later we may need another web script to return local file path in content store if we are using
    // local mapped drive on linux server
    //return file.getAbsolutePath();
  }

  
  
  public void setFileContentStore(ContentStore fileContentStore) {
    this.fileContentStore = fileContentStore;
  }
}