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

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.model.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * List all children, including all properties and aspects for each child
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetChildren extends AbstractCatWebScript {
  public static final String PARAM_PATH = "path";  
  public static final String PARAM_UUID= "uuid";
  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String path = req.getParameter(PARAM_PATH);
    String uuidStr = req.getParameter(PARAM_UUID);
    
    res.setContentType(MimetypeMap.MIMETYPE_JSON);

    // write the results to the output stream
    ArrayList<Resource> children = getChildren(path, uuidStr);

    // serialize children via json
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), children);

    return null;

  }

  public ArrayList<Resource> getChildren(String path) {
    return getChildren(path, null);
  }
  
  /**
   * For the given nodeRef, get the applicable children.  Subclasses can override
   * this method to provide any custom filter they require.  Default is to 
   * return all children.
   * 
   * @param nodeRef
   * @return
   */
  protected  List<ChildAssociationRef> getChildAssociations(NodeRef nodeRef) {
    return WebScriptUtils.getChildAssociations(nodeRef, nodeService);
  }

  /**
   * Method getChildren.
   * @param path String
   * @return ResourceList
   * @throws Exception
   */
  public ArrayList<Resource> getChildren(String path, String uuidStr) {
    
    NodeRef nodeRef = null;
    if(uuidStr != null){
      // make sure that there isn't an ending slash
      if(uuidStr.endsWith("/")) {
        uuidStr = uuidStr.substring(0, uuidStr.length() - 1);
      }
      nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuidStr);
    }else{
      // look up node (will throw exception if it doesn't exist)
      nodeRef = NodeUtils.getNodeByName(path, nodeService);
    }
    
    if (nodeRef == null) {
      if(uuidStr != null){
        throw new RuntimeException("node with UUID " + uuidStr + " does not exist!");
      }else{
        throw new RuntimeException(path + " does not exist!");
      }      
    }  

    List<ChildAssociationRef> assocRefs = getChildAssociations(nodeRef);
    ArrayList<Resource> children = new ArrayList<Resource>();

    for (ChildAssociationRef assocRef : assocRefs) {
      NodeRef childNodeRef = assocRef.getChildRef();
      
      // filter out any resources that have an association name that isn't under the {http://www.alfresco.org/model/content/1.0}
      // namespace, or {http://www.alfresco.org/model/site/1.0} namespace, 
      // as this means it is a special alfresco folder where the assocation name and the name property don't match,
      // which causes us lots of problems
      // TODO: add association type regex and assoc name regex as parameters
      if(assocRef.getQName().getNamespaceURI().equals(NamespaceService.CONTENT_MODEL_1_0_URI) ||
          assocRef.getQName().getNamespaceURI().equals(SiteModel.SITE_MODEL_URL)) { 
        // filter out nodes that have the hiddenRendition aspect
        if(!nodeService.hasAspect(childNodeRef, RenditionModel.ASPECT_HIDDEN_RENDITION)) {
          Resource resource = WebScriptUtils.getResource(childNodeRef, nodeService, namespaceService, dictionaryService, contentService);
          if(resource != null) {  
            // add the resource to the overall results
            children.add(resource);
          }
        }
      }
    }
    return children;
  }
  
}
