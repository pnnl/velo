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
package gov.pnnl.cat.web.scripts;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.web.scripts.Search.SearchResultNodeRefs;
import gov.pnnl.velo.model.QueryRequest;
import gov.pnnl.velo.model.Resource;

/**
 * Search for up the given resources, including all properties and aspects for each 
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetFileChildren extends AbstractCatWebScript {
  
  public static final String PARAM_UUID = "uuid";
  
  //params passed in by backbone's table
  public static final String PARAM_CURRENT_PAGE = "current_page";
  public static final String PARAM_PAGE = "page";
  public static final String PARAM_LIMIT = "limit";
  public static final String PARAM_OFFSET = "offset";
  public static final String PARAM_SORT_KEY = "sortKey";
  public static final String PARAM_ORDER = "order";
  
  private Search searchWebScript;
  
   /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    // <url>/cat/getFileChildren?uuid={uuid}&pageNumber={?pageNumber}&maxItems={?maxItems}&sortByProp={?sortByProp}&order={?order}</url>
    String uuid = req.getParameter(PARAM_UUID);
    Integer pageNumber = getIntParameter(req, PARAM_CURRENT_PAGE);
    Integer page = getIntParameter(req, PARAM_PAGE);
    if(page != null){
      pageNumber = page;
    }
    
    
    Integer maxItems = getIntParameter(req, PARAM_LIMIT);
    String sortByProp = req.getParameter(PARAM_SORT_KEY); //sortKey: Name, Size, and Date
    String order = req.getParameter(PARAM_ORDER); //order: -1, 0, and 1 where -1 for ascending order or 1 for descending order. If 0, no client side sorting will be done and the order query parameter will not be sent to the server during a fetch
    

    // Convert web service order param to solr syntax
    if(order != null) {
      //asc or desc
      if(order.equals("asc")) {
        order = "ascending";
      } else {
        order = "descending";
      }
    }
    
    
    //change sort param to what service expects:
    if(sortByProp != null){
      if(sortByProp.equalsIgnoreCase("name")){
        sortByProp = ContentModel.PROP_NAME.toString();
      //}else if(sortByProp.equalsIgnoreCase("Size")){
        //sortByProp = ContentModel.PROP_Con.toString();  Can't currently sort by filesize according to alfresco's wiki: https://wiki.alfresco.com/wiki/Search "This will be extended to include the size of the content in the future." 
      } else if(sortByProp.equalsIgnoreCase("Date")){
        sortByProp = ContentModel.PROP_MODIFIED.toString();
      }
    }
    


    // Compute the path string
    NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);
    String pathStr = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
    
    // Compose the query
    //PATH:"/app:company_home/cm:User_x0020_Documents/cm:admin/*" AND TYPE:"cm:content" AND -ASPECT:"rn:hiddenRendition"
    String query = "PATH:\"" + pathStr + "/*\" AND TYPE:\"cm:content\" AND -ASPECT:\"rn:hiddenRendition\"";
    QueryRequest queryRequest = new QueryRequest(query, false, sortByProp, order, maxItems, pageNumber, null);
    
    SearchResultNodeRefs searchResult = searchWebScript.performSearchForNodes(queryRequest.getQuery(), 
        queryRequest.isIncludeThumbnails(), queryRequest.getSortByProp(), 
        queryRequest.getOrder(), queryRequest.getMaxItems(), queryRequest.getPageNumber(), queryRequest.getPropertyFacets());
    
    
    // write the results to the output stream
    // serialize children via json
    long begin = System.currentTimeMillis();
    
    res.setContentType(MimetypeMap.MIMETYPE_JSON);
    ObjectMapper mapper = new ObjectMapper();
    
    DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
    //List<DataTablesResource> dataTableResources = new ArrayList<DataTablesResource>();
    
    //write the json directly to the response
    JsonGenerator generator = mapper.getFactory().createJsonGenerator(res.getOutputStream());
    generator.writeStartObject();
    generator.writeFieldName("total_entries");
    generator.writeNumber((int)((long)searchResult.getNumHits()));

    generator.writeArrayFieldStart("items");
    List<NodeRef> nodes = searchResult.nodeRefs;
    for (NodeRef node : nodes) {
      Date modifiedDate = (Date)nodeService.getProperty(node, ContentModel.PROP_MODIFIED);
      long modified = modifiedDate.getTime();
      ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);

      // couldn't get datatables to work with it's 'data' JSON element being a list of objects (data:[{name:zoe, mood:blah}] NOTE CURLEY BRACES
      // so instead I'm having to build the json manually and sending just a list of strings  (data:[[zoe, blah]] NOTE SQUARE BRACES 
      // once we have more time to debug, this can probably be reverted to send objects instead and figure out
      // how to properly configure datatables to accept the list of objects instead 
//      DataTablesResource dtr = new DataTablesResource(
//          (String)nodeService.getProperty(node, ContentModel.PROP_NAME),
//          String.valueOf(reader.getSize()),
//          reader.getMimetype(),
//          reader.getContentUrl(),
//          df.format(modifiedDate)
//          );
//      
//      dataTableResources.add(dtr);
      String name = (String)nodeService.getProperty(node, ContentModel.PROP_NAME);
      generator.writeStartArray();
      generator.writeString(name);
      generator.writeString(String.valueOf(reader.getSize()));
      generator.writeString(reader.getMimetype());
      // uuid will be used in the URL: 
      ///service/landingPage/getFileContents/"+filename+"?uuid=" + uuid ; 
      generator.writeString(node.getId()); 
      generator.writeString(String.valueOf(modified));
      generator.writeEndArray();
    }
    generator.writeEndArray();
    generator.writeEndObject();
    generator.flush();
//      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); 
//      DataTablesWrapper wrapper = new DataTablesWrapper(draw, (int)((long)searchResult.getNumHits()), dataTableResources);
//      mapper.writeValue(res.getOutputStream(), wrapper);
    return null;
  }
  
  
  protected Integer getIntParameter(WebScriptRequest req, String paramName) {
    Integer param = null;
    String paramString = req.getParameter(paramName);
    if(paramString != null) {
      param = Integer.valueOf(paramString);
    }
    return param;
  }

  public void setSearchWebScript(Search searchWebScript) {
    this.searchWebScript = searchWebScript;
  }
  
  
}
