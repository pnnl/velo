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

import gov.pnnl.cat.util.XmlUtility;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 */
public class GetMimetypesWebScript extends AbstractWebScript {
  protected MimetypeService mimetypeService;

  /**
   * Method setMimetypeService.
   * @param mimetypeService MimetypeService
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }

  /**
   * Method execute.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @throws IOException
   * @see org.springframework.extensions.webscripts.WebScript#execute(WebScriptRequest, WebScriptResponse)
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
    String mapType = req.getExtensionPath();
    Map<String, String> map;
    if(mapType.equalsIgnoreCase("extensionsByMimetype")){
      map = this.mimetypeService.getExtensionsByMimetype();
    }
    else if(mapType.equalsIgnoreCase("MimetypesByExtension")){
      map = this.mimetypeService.getMimetypesByExtension();
    }
    else if(mapType.equalsIgnoreCase("DisplaysByMimetype")){
      map = this.mimetypeService.getDisplaysByMimetype();
    }
    else {
//      if(mapType.equalsIgnoreCase("extensionsByMimetype")){
      map = this.mimetypeService.getDisplaysByExtension();
    }
    
    res.setContentType(MimetypeMap.MIMETYPE_XML);
    res.setContentEncoding("UTF-8");
    XmlUtility.serializeToOutputStream(map, res.getOutputStream());
  }

}
