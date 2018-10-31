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

import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.repository.RepositoryWebService;
import org.alfresco.repo.webservice.repository.UpdateResult;
import org.alfresco.repo.webservice.types.CML;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Execute a CML statement.  For now this web script just forwards calls to RepositoryWebService to
 * avoid axis calls from client.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class ExecuteCml extends AbstractCatWebScript {
  
  protected RepositoryWebService repositoryWebService;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    
    ObjectMapper mapper = new ObjectMapper();
    CML cml = mapper.readValue(requestContent, CML.class);
    UpdateResult[] results = repositoryWebService.getCmlUtil().executeCML(cml);
    List<Resource> modifiedResources = new ArrayList<Resource>();
    
    // need to return a Resource list containing the resources that were modified by the CML so 
    // clients can update their cache
    for(UpdateResult result : results) {
      
      String cmd = result.getStatement();
      
      if(cmd.equals(VeloConstants.CML_DELETE)) {
        Resource deletedResource = new Resource(result.getSource().getUuid());
        deletedResource.setPath(result.getSource().getPath());
        deletedResource.addProperty(VeloConstants.PROP_CML_COMMAND, VeloConstants.CML_DELETE);
        modifiedResources.add(deletedResource);
        
      } else {
        NodeRef nodeRef = Utils.convertToNodeRef(result.getDestination(), nodeService, searchService, namespaceService);      
        Resource resource = WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService);
        resource.addProperty(VeloConstants.PROP_CML_COMMAND, cmd);
        modifiedResources.add(resource);
      }    
      
    }
    
    // write the results to the output stream
    // serialize children via json
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), modifiedResources);
    return null;

  }

  public void setRepositoryWebService(RepositoryWebService repositoryWebService) {
    this.repositoryWebService = repositoryWebService;
  }



}
