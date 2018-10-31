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

import org.alfresco.repo.content.MimetypeMap;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Returns true if the wiki is enabled on the server, so
 * file renaming policy should be active.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class WikiEnabled extends AbstractVeloWebScript {
  
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
    PrintStream printStream = null;
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    try {
      // write the results to the output stream
      boolean wikiEnabled = wikiEnabled();
      printStream = new PrintStream(res.getOutputStream());
      printStream.println(String.valueOf(wikiEnabled));
      printStream.flush();
      
    } catch (Throwable t) {
      logger.error("Could not print message to response stream.", t);

    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }
    return null;
  }

  /**
   * Method wikiEnabled.
   * @return boolean
   */
  public boolean wikiEnabled() {
    return WikiUtils.getWikiHome() != null;
  }

}
