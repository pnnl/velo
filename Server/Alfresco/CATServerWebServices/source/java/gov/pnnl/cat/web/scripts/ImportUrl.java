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

import gov.pnnl.cat.server.webservice.imprt.ImportWebService;
import gov.pnnl.velo.model.ImportUrlRequest;

import java.io.File;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Execute an action.  For now this web script just forwards calls to ActionWebService to
 * avoid axis calls from client.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class ImportUrl extends AbstractCatWebScript {
  
  protected ImportWebService importWebService;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    
    ObjectMapper mapper = new ObjectMapper();
    ImportUrlRequest actionInfo = mapper.readValue(requestContent, ImportUrlRequest.class);
    
    // TODO:  this is horrifically ugly because axis creates separate classes for client vs. server side.
    // I don't want to include all the server-side classes in the client, so we have to convert over here.
    // When we remove the axis class definitions, we can clean this up.
//    org.alfresco.webservice.types.Reference ref1 = actionInfo.getTarget();
//    Store store1 = ref1.getStore();
//    org.alfresco.repo.webservice.types.Store store2 = null;
//    if(store1 != null) {
//      store2 = new org.alfresco.repo.webservice.types.Store(store1.getScheme(), store1.getAddress());
//    }
//    Reference ref2 = new Reference(store2, ref1.getUuid(), ref1.getPath());
//    importWebService.urlImportAction(actionInfo.getXml(), ref2);
    
    return null;

  }


  public void setImportWebService(ImportWebService importWebService) {
    this.importWebService = importWebService;
  }



}
