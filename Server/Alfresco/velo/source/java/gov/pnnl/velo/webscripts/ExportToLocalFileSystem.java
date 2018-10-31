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

import gov.pnnl.velo.util.LocalFileSystemExporter;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.io.FileOutputStream;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.TempFileProvider;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 */
public class ExportToLocalFileSystem extends AbstractVeloWebScript{

  //destination is where to export to on the local disc
  public static final String PARAM_DESTINATION = "destination"; 
  public static final String PARAM_WIKI_NODE_PATH = "wikiNodePath"; 
  protected ExporterService exporterService;
  
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
    String wikiPath = req.getParameter(PARAM_WIKI_NODE_PATH);
    if(!wikiPath.startsWith("/")) {
      wikiPath = "/" + wikiPath;
    }

    String destination = req.getParameter(PARAM_DESTINATION);
    
    logger.debug("wikiPath = " + wikiPath);
    logger.debug("destination = " + destination);

    // Get the optional version parameter
    
    // Convert the wiki path to alfresco format
    String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
    logger.debug("alfrescoPath = " + alfrescoPath);
    
    // Find the node from the path (will throw exception if node does not exist)
    NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
    
    ExporterCrawlerParameters params = new ExporterCrawlerParameters();
    params.setCrawlChildNodes(true);
    params.setCrawlSelf(true);
    params.setCrawlAssociations(true);
    params.setCrawlContent(true);
    params.setExportFrom(new Location(nodeRef));
   
    StringBuilder linksScript = new StringBuilder();
    File destinationFolder = new File(destination);
    destinationFolder.mkdirs();
    this.exporterService.exportView(new LocalFileSystemExporter(nodeService, contentService, destinationFolder, linksScript), params, null);
    
    String firstLine = "#!/bin/sh\n";
    
    // Execute the script if necessary to actually create all the symbolic links
    if(!linksScript.toString().isEmpty()) {
      linksScript.insert(0, firstLine);
      File tempScriptFile = TempFileProvider.createTempFile("linkScript", ".sh");
      tempScriptFile.deleteOnExit();
      FileOutputStream outputStream= null;
      try {
        outputStream = new FileOutputStream(tempScriptFile);
        org.apache.commons.io.IOUtils.write(linksScript.toString(), outputStream);
        org.apache.commons.io.IOUtils.closeQuietly(outputStream);//have to close the stream before trying to executing it
        String mkdirCmd[] = {"mkdir", destinationFolder.getAbsolutePath() };
        WikiUtils.execCommand(mkdirCmd, destinationFolder);
        String chmodLinkDirCmd[] = {"chmod", "-R", "g+rw", destinationFolder.getAbsolutePath() };
        WikiUtils.execCommand(chmodLinkDirCmd, destinationFolder);
        String chmodCmd[] = {"chmod", "777", tempScriptFile.getAbsolutePath() };
        WikiUtils.execCommand(chmodCmd, destinationFolder);
        String scriptCmd[] = {tempScriptFile.getAbsolutePath()};
        long startScript = System.currentTimeMillis();
        WikiUtils.execCommand(scriptCmd, destinationFolder);
        long endScript = System.currentTimeMillis();
        logger.info("Time to execute link script: " + (endScript - startScript)/1000 + " seconds");

      } finally {
        if(outputStream != null) {
          org.apache.commons.io.IOUtils.closeQuietly(outputStream);
        }      
      }
    }
    return null;
  }

  /**
   * Method setExporterService.
   * @param exporterService ExporterService
   */
  public void setExporterService(ExporterService exporterService) {
    this.exporterService = exporterService;
  }


}
