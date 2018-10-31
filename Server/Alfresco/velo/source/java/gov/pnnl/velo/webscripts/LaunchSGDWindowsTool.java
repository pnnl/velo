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

import java.io.File;
import java.io.FileOutputStream;

import org.alfresco.service.cmr.view.ExporterService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 */
public class LaunchSGDWindowsTool extends AbstractVeloWebScript{

  //destination is where to export to on the local disc
  public static final String PARAM_INPUT_FILES_PATH = "inputFilesPath"; 
  public static final String PARAM_TOOL_PATH = "toolPath"; 
  public static final String PARAM_TOOL_PARAMETERS = "toolParams";
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
    // need the name & path of the executable
    // need the path RELATIVE TO WIN to the input files (sym links)
    // create/overwrite the bat file so its contents use this input file and launch this executable
    // return a URL to the SGD tool
    String toolPath = req.getParameter(PARAM_TOOL_PATH);
    String inputFilesPath = req.getParameter(PARAM_INPUT_FILES_PATH);
    String params = req.getParameter(PARAM_TOOL_PARAMETERS);
    
    logger.debug("toolPath = " + toolPath);
    logger.debug("inputFilesPath = " + inputFilesPath);
    logger.debug("params = " + params);

      File tempScriptFile = new File("/pic/projects/cii/Tools/launchWinTool.bat");
      StringBuilder batFileContents = new StringBuilder();
//      batFileContents.append("set num=%random%");
//      
//          echo %num%
//          echo (todo - path to inputs go here) MSI_QuickView running > E:\SGDStatus\%num%.txt
//          start /wait E:/MSI_QuickView/distrib/MSI_QuickView.exe
//          echo (todo - path to inputs go here) MSI_QuickView complete > E:\SGDStatus\%num%.txt
//)
      tempScriptFile.deleteOnExit();
      FileOutputStream outputStream= null;
//      try {
//        outputStream = new FileOutputStream(tempScriptFile);
//        org.apache.commons.io.IOUtils.write(linksScript.toString(), outputStream);
//        String chmodCmd[] = {"chmod", "777", tempScriptFile.getAbsolutePath() };
//        WikiUtils.execCommand(chmodCmd, destinationFolder);
//        String scriptCmd[] = {tempScriptFile.getAbsolutePath()};
//        long startScript = System.currentTimeMillis();
//        WikiUtils.execCommand(scriptCmd, destinationFolder);
//        long endScript = System.currentTimeMillis();
//        logger.info("Time to execute link script: " + (endScript - startScript)/1000 + " seconds");
//
//      } finally {
//        if(outputStream != null) {
//          org.apache.commons.io.IOUtils.closeQuietly(outputStream);
//        }      
//      }
//    }
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
