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
package gov.pnnl.velo.webscripts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.infoscoop.util.TextTrimListner;
import org.infoscoop.util.Xml2Json;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.web.scripts.AbstractCatWebScript;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloServerConstants;

/**
 * List all children, including all properties and aspects for each child
 * 
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetDatasetMetadata extends AbstractCatWebScript {
  public static final String PARAM_PATH = "path";
  public static final String PARAM_UUID = "uuid";
  public static final String METADATA_FOLDER = "Metadata";
  public static final String METADATA_FILE = "dataset.xml";

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String path = req.getParameter(PARAM_PATH);
    String uuidStr = req.getParameter(PARAM_UUID);

    // find the dataset noderef
    NodeRef nodeRef = null;
    if (uuidStr != null) {
      // make sure that there isn't an ending slash
      if (uuidStr.endsWith("/")) {
        uuidStr = uuidStr.substring(0, uuidStr.length() - 1);
      }
      nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuidStr);
    } else {
      // look up node (will throw exception if it doesn't exist)
      nodeRef = NodeUtils.getNodeByName(path, nodeService);
    }

    if (nodeRef == null) {
      if (uuidStr != null) {
        throw new RuntimeException("node with UUID " + uuidStr + " does not exist!");
      } else {
        throw new RuntimeException(path + " does not exist!");
      }
    }

    // Create json object
    String datasetName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    String json = null;

    NodeRef metadataFolder = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, METADATA_FOLDER);
    if (metadataFolder == null) {
      throw new RuntimeException(datasetName + " does not contain folder " + METADATA_FOLDER);
    }
    NodeRef metadataFile = nodeService.getChildByName(metadataFolder, ContentModel.ASSOC_CONTAINS, METADATA_FILE);
    if (metadataFile == null) {
      throw new RuntimeException(datasetName + " does not contain " + METADATA_FILE + " under them" + METADATA_FOLDER + " subfolder");
    }
    
    json = datasetXmlToJson(metadataFile);
    
    String doi = (String)nodeService.getProperty(nodeRef, VeloServerConstants.PROP_DOI);
    
    //publishDate could be null if there is no DOI yet, or if its DRAFT
    Date publishDate = (Date)nodeService.getProperty(nodeRef, VeloServerConstants.PROP_DATASET_PUBLISH_DATE);
    
    //now (i know this seems silly but Zoe is much faster at writing Java than javascript) parse the json for
    //elements used in the MLA citation format and add that as a string to the json before we send to landing page
    
    // Replace viewer file paths with UUID's so as not to throw permissions errors when looking up path by name
    JSONObject jsonObject = new JSONObject(json);
    try {
      JSONArray viewersArray = jsonObject.getJSONArray("viewers");

      for(int j = 0; j < viewersArray.length(); j++) {
        JSONObject viewer = viewersArray.getJSONObject(j);
        if(viewer.getString("viewerType").equalsIgnoreCase("ImageGallery") || viewer.getString("viewerType").equalsIgnoreCase("ImageVideoGallery")){
          JSONArray imgFiles = viewer.getJSONArray("imageFiles");
          for(int i = 0; i < imgFiles.length(); i++){
            JSONObject imgFile = imgFiles.getJSONObject(i);
            String filePath = imgFile.getString("filePath");
            // get UUID from file path
            String uuid = getUuid(filePath);
            if(uuid != null) {
              imgFile.remove("filePath");
              imgFile.put("fileUuid", uuid);
            }
            String mimetype = getMimetype(filePath);
            if(mimetype != null) {
              imgFile.put("mimetype", mimetype);
            }
          }
        } else {
          String filePath = viewer.getString("filePath");
          // get UUID from file path
          String uuid = getUuid(filePath);
          if(uuid != null) {
            viewer.remove("filePath");
            viewer.put("fileUuid", uuid);
          }
          String mimetype = getMimetype(filePath);
          if(mimetype != null) {
            viewer.put("mimetype", mimetype);
          }
        }
      }
    } catch (JSONException e) {
      // if there is no viewers element yet, just ignore
      e.printStackTrace();
    }
    
    // TODO: also replace the paths for methods and data quality documents
//    JSONObject dataQualityObject = jsonObject.getJSONObject("data");
//    for(int j = 0; j < viewersArray.length(); j++){
    
    
    //UNCOMMENT THIS LINE WHEN ABOVE IS IMPLEMENTED SO REMAINING HACK WILL WORK
    json = jsonObject.toString();
    
    //FOR NOW leave this code in
    ObjectMapper mapper = new ObjectMapper();
    JsonParser jp = mapper.getFactory().createJsonParser(json);
    JsonNode rootNode = mapper.readTree(jp);
    
    JsonNode citationNode = rootNode.findValue("citationInformation");
    JsonNode authorsNode = citationNode.get("authors");
   
    //gather information for the MLA citation string:
    StringBuilder authorsSB = new StringBuilder();
    if(authorsNode != null){
      for (int i = 0; i < authorsNode.size(); i++){
        JsonNode author = authorsNode.get(i);
        authorsSB.append(author.get("lastName").asText() + " ");
        authorsSB.append(author.get("firstName").asText().substring(0, 1));
        if(author.has("middleName")){
          authorsSB.append(author.get("middleName").asText().substring(0, 1));
        }
        if(i+1 != authorsNode.size()){
          authorsSB.append(", ");
        }
      }
    }
    
    String doiYear = "(NA)";
    String doiString = "NO DOI";
    String doiState = (String)nodeService.getProperty(nodeRef, VeloServerConstants.PROP_DATASET_STATE);
    String title = citationNode.get("title").asText() + ".";
    String publisher = "OSTI.";
    
    if(doi!=null){
      if(VeloServerConstants.DATASET_STATE_DRAFT.equals(doiState)){
        doiYear = "(DRAFT)";
        doiString = "DRAFT doi:" + doi;
      }else{
        if(publishDate != null){//should never be the case
          Calendar cal = new GregorianCalendar();
          cal.setTime(publishDate);
          doiYear = "("+cal.get(Calendar.YEAR)+")";
        }
        doiString = "doi:" + doi;
      }
    }
      
    
      
      String addedJson = "\"mlaCitationString\": \""
          + authorsSB.toString()
          + " " + doiYear
          + " " + title
          + " " + publisher
          + " " + doiString
          + "\",\n ";
      
      if(doi != null){
        addedJson += "\"doi\": \"" + doi + "\",\n ";
      }
      
      if(publishDate != null){
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String strDate = df.format(publishDate);
        addedJson += "\"listingDate\": \""
            + strDate + "\",\n ";
      }
      
      if(doiState != null){
        addedJson += "\"state\": \""
          + doiState + "\",\n ";
      }
          
    
      // add to the json the DOI info and MLS citation string
      //remove from original JSON the closing curly bracket and add a comma, 
      //then remove the last comma from the added json and add back in the closing curly bracket 
      json = StringUtils.substringBeforeLast(json, "}")  
          + ",\n " 
          + StringUtils.substringBeforeLast(addedJson, ",")
          +"}"; 
    

    // write the json to the response
    writeMessage(res, json, MimetypeMap.MIMETYPE_JSON);

    return null;

  }

  private String datasetXmlToJson(NodeRef metadataFile) {

    ContentReader contentReader = NodeUtils.getReader(metadataFile, null, ContentModel.PROP_CONTENT, contentService, versionService);
    String xml = contentReader.getContentString();

    Xml2Json x2j = new Xml2Json();
    x2j.setListner(new TextTrimListner());
    try {
      addArrayPaths(x2j);
      String json = x2j.xml2json(xml);
      return json;
      
    } catch (Exception e) {
      throw new RuntimeException("Error parsing " + NodeUtils.getNamePath(metadataFile, nodeService) + e.toString());
    }

  }

  private void addArrayPaths(Xml2Json x2j) throws IOException {

    x2j.addArrayPath("/datasetMetadata/citationInformation/authors");
    x2j.addArrayPath("/datasetMetadata/dataAccess/requiredSoftwares");
    x2j.addArrayPath("/datasetMetadata/publications");
    x2j.addArrayPath("/datasetMetadata/contacts");
    x2j.addArrayPath("/datasetMetadata/methods");
    x2j.addArrayPath("/datasetMetadata/dataQuality/dataQualityFiles");

    // OSTI
    x2j.addArrayPath("/datasetMetadata/DOEContractNumbers");
    x2j.addArrayPath("/datasetMetadata/nonDOEContractNumbers");
    x2j.addArrayPath("/datasetMetadata/researchOrganizations");
    x2j.addArrayPath("/datasetMetadata/sponsorOrganizations");

    // landing page config
    x2j.addArrayPath("/datasetMetadata/viewers");
    x2j.addArrayPath("/datasetMetadata/viewers/imageGalleryViewer/imageFiles");
    x2j.addArrayPath("/datasetMetadata/relatedWebsites");

  }
  
  /**
   * We have to run as system user so we don't get access denied when trying to look up
   * the path.
   * @param path
   * @return
   */
  private String getUuid(String path) {
    String uuid = null;
    final String currentUser = this.authenticationComponent.getCurrentUserName();
    
    AuthenticationUtil.setRunAsUserSystem();
    CmsPath cmsPath = new CmsPath(path);    
    NodeRef nodeRef = NodeUtils.getNodeByName(cmsPath.toAssociationNamePath(), nodeService);
    if(nodeRef != null) {
      uuid = nodeRef.getId();
    }
    
    AuthenticationUtil.setRunAsUser(currentUser);
    
    return uuid;
  }
  
  private String getMimetype(String path) {
    String mimetype = null;
    final String currentUser = this.authenticationComponent.getCurrentUserName();
    
    AuthenticationUtil.setRunAsUserSystem();
    CmsPath cmsPath = new CmsPath(path);    
    NodeRef nodeRef = NodeUtils.getNodeByName(cmsPath.toAssociationNamePath(), nodeService);
    if(nodeRef != null) {
      ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
      if (reader != null) {
        mimetype = reader.getMimetype();
      }
    }
    
    AuthenticationUtil.setRunAsUser(currentUser);
    
    return mimetype;
  }

}
