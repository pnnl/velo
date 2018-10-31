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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Create or update a bunch of resources in one call.  Return the resources that were
 * just created/updated.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class CreateUpdateResources extends  AbstractCatWebScript {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
	    ArrayList<Resource> resourcesToCreate;
	    ObjectMapper mapper = new ObjectMapper();
	    
	    // TODO: Until we can figure out how to get jQuery.ajax to submit json in request body, we have to be able to accept 
	    // json as a parameter OR in the request body
	    if(requestContent.length() == 0) {
	      String jsonString = req.getParameter("jsonString");
	      resourcesToCreate = mapper.readValue(jsonString, new TypeReference<ArrayList<Resource>>() {});
	      
	    } else {
	      resourcesToCreate = mapper.readValue(requestContent, new TypeReference<ArrayList<Resource>>() {});
	    }

	    ArrayList<Resource> newResources = createUpdateResources(resourcesToCreate);

	    // write the results to the output stream
	    // serialize children via json
	    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
	    mapper = new ObjectMapper();
	    mapper.writeValue(res.getOutputStream(), newResources);

	    return null;

	  }

  /**
   * Create nodes, setting the given path and 
   * @param resourcesToCreate
   * @return
   * @throws Exception
   */
  public ArrayList<Resource> createUpdateResources(List<? extends Resource> resourcesToCreate) {
    ArrayList<Resource> newResources = new ArrayList<Resource>();

    for(Resource resourceToCreate : resourcesToCreate) {
      NodeRef nodeRef = WebScriptUtils.getNodeRef(resourceToCreate, nodeService);
          
      // only create resource if it doesn't already exist
      if(nodeRef == null) {
        // this will automatically set the properties
        nodeRef = WebScriptUtils.createNode(resourceToCreate, nodeService, dictionaryService);
     
      } else { // update any properties present
        WebScriptUtils.setProperties(nodeRef, resourceToCreate, nodeService, dictionaryService);
      }
      
      // update any aspects present
      WebScriptUtils.setAspects(nodeRef, resourceToCreate, nodeService);

      newResources.add(WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService));
    }

    return newResources;
  }

}
