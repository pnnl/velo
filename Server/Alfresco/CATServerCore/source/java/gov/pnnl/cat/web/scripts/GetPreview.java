package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.io.StringWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Get the preview for the given resource. If the resource is an image, it will
 * return a thumbnail or the image itself if the thumbnail could not be created.
 * If the resource has a text transform, it will return the text. Otherwise, it
 * will return "No preview available". Preview will be returned in json format:
 * {"preview":"No preview available.","image":false}
 * 
 * @author d3k339
 * 
 */
public class GetPreview extends AbstractCatWebScript {
  public static final String PARAM_UUID = "uuid";

  private GetThumbnail getThumbnailWebScript;

  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // Get the request parameters:
    String uuid = req.getParameter(PARAM_UUID);

    // Get the NodeRef
    NodeRef nodeRef = null;
    if (uuid == null || uuid.isEmpty()) {
      throw new RuntimeException("uuid parameter must be specified.");
    }

    // make sure that there isn't an ending slash
    if (uuid.endsWith("/")) {
      uuid = uuid.substring(0, uuid.length() - 1);
    }
    nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);

    // get the approriate preview
    String previewText = null;
    boolean isImage = false;

    if (isResourceAnImage(nodeRef)) {
      previewText = getThumbnailWebScript.getEncodedThumbnailString(nodeRef, null);
      isImage = true;

    } else {
      previewText = getTextTransform(nodeRef);
    }

    if (previewText == null) {
      previewText = Preview.NO_PREVIEW_TEXT;
      isImage = false;
    }

    // serialize preview via json
    Preview preview = new Preview(isImage, previewText);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), preview);

    return null;
  }

  private String getTextTransform(NodeRef nodeRef) {
    String textContent = null;
    
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TEXT_TRANSFORM)) {
      String errorMsg = (String) nodeService.getProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR);
      
      if (errorMsg != null) {
        String[] msgParts = errorMsg.split(":", 2);

        // Do not parse the error message if it's a no transformer found error
        if (msgParts.length > 1 && !msgParts[0].startsWith("No text transformer found for")) {
          textContent = msgParts[1];
        
        } else {
          textContent = errorMsg;
        }
      } else {
        // get text transform
        ContentReader reader = contentService.getReader(nodeRef, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT);
        if(reader != null) {
          textContent = reader.getContentString();
        }
      }
    }
    return textContent;
  }

  private boolean isResourceAnImage(NodeRef nodeRef) {
   // we assume if it has a rendition, then it's a thumbnail - could be on a folder too
   if(nodeService.hasAspect(nodeRef, RenditionModel.ASPECT_RENDITIONED)) {
     return true;
     
   } else {
     // see if it is an image file
     String mimetype = (String)nodeService.getProperty(nodeRef, CatConstants.PROP_MIMEYPE);
     if(mimetype != null && mimetype.toLowerCase().contains("image")) {
       return true;
     }     
   }

    return false;
  }
  

  public void setGetThumbnailWebScript(GetThumbnail getThumbnailWebScript) {
    this.getThumbnailWebScript = getThumbnailWebScript;
  }

  public static void main(String[] args) {
    Preview preview = new Preview(false, "No preview available.");
    ObjectMapper mapper = new ObjectMapper();
    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, preview);
      System.out.println(writer.toString());
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public static class Preview {
    public static String NO_PREVIEW_TEXT = "No preview available.";

    boolean isImage = false; // images are base64 encoded strings

    String preview;

    public Preview(boolean isImage, String preview) {
      super();
      this.isImage = isImage;
      this.preview = preview;
    }

    public boolean isImage() {
      return isImage;
    }

    public void setImage(boolean isImage) {
      this.isImage = isImage;
    }

    public String getPreview() {
      return preview;
    }

    public void setPreview(String preview) {
      this.preview = preview;
    }

  }

}
