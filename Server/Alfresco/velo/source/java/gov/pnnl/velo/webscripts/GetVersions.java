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
package gov.pnnl.velo.webscripts;

import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to get all versions of a resource.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class GetVersions extends AbstractVeloWebScript {

  public static final String PARAM_WIKI_PATH = "wikiPath";

  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // Get the request parameters:
    String wikiPath = req.getParameter(PARAM_WIKI_PATH);
    
 // Write the versions to the response
    PrintStream printStream = null;
    
    try {
      printStream = new PrintStream(res.getOutputStream());
      getVersions(wikiPath, printStream);
    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }

    return null;
  }

  /**
   * Method getVersions.
   * @param wikiPath String
   * @param printStream PrintStream
   * @throws Exception
   */
  public void getVersions(String wikiPath, PrintStream printStream) throws Exception{
 // Convert the path to alfresco format
    String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);

    // Find the node from the path (will throw exception if node does not exist)
    NodeRef nodeRef = 
      WikiUtils.getNodeByName(alfrescoPath, nodeService);

    // get the list of versions (in descending order - ie. most recent first)
    VersionHistory history = versionService.getVersionHistory(nodeRef);
    List<Version> orderedVersions = new ArrayList<Version>();
    if(history != null) {
      orderedVersions = new ArrayList<Version>(history.getAllVersions());
    }
    
    
      
      for(Version version : orderedVersions) {

        String versionLabel = version.getVersionLabel();
        String creator = version.getFrozenModifier();
        Date createdDate = version.getFrozenModifiedDate();

        printStream.println(versionLabel + "\t" + creator + "\t" + createdDate.getTime());
      }

      logger.debug("Done reading request body");
      printStream.flush();

    
  }

}
