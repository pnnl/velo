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
package gov.pnnl.velo.wiki.content.impl;


import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;

/**
 */
public class CollectionContentExtractor extends AbstractWikiContentExtractor {
  
  private Map<String, File> mimetypeToTemplate = new HashMap<String, File>();
  private String templateFolderPath = WikiUtils.getWikiExtensions() + "/wikipages";
  
  @Override
  public void init() {
    if(WikiUtils.getWikiExtensions() == null){
      return; //nothing to do as there is no wiki
    }
    
    try {
      File templateFolder = new File(templateFolderPath);
      logger.debug("loading templates from folder: " + templateFolderPath);
      
      File[] templates = templateFolder.listFiles();
      for (File file : templates) {
        // Only load collection template files
        String suffix = "_template.wiki"; 
        if(file.getName().endsWith(suffix)) {
          // parse off the special page template suffix
          int endIndex = file.getName().length() - suffix.length();
          String mimetype = file.getName().substring(0, endIndex);

          // convert underscore to /
          mimetype = mimetype.replaceAll("_", "/");

          logger.debug("Loading collection template: " + mimetype + " : " + file);         
          mimetypeToTemplate.put(mimetype, file);
        }
      }
    } catch (Throwable e) {
      // Log this error, but don't crash spring startup
      logger.error("Failed initialization.", e);
    }

    super.init();
  }
  

  /**
   * DO NOT delete this return file, as it is not a temp file, it is a template.
   * @param nodeRef NodeRef
   * @return File
   * @throws Exception
   * @see gov.pnnl.velo.wiki.content.WikiContentExtractor#extractWikiContent(NodeRef)
   */
  @Override
  public File extractWikiContent(NodeRef nodeRef) throws Exception {
    // Get the mimetypye
    String mimetype = WikiUtils.getMimetype(nodeRef, nodeService, contentService);
    
    // Find the template
    File templateFile = mimetypeToTemplate.get(mimetype);
    
    File tempFile = null;
    
    if(templateFile != null) {
      // Copy to a temp file
      tempFile = TempFileProvider.createTempFile("velo-folder-", ".metadata");
      FileUtils.copyFile(templateFile, tempFile);
    }
    return tempFile; 
  }

  /**
   * Method getSupportedMimetypes.
   * @return List<String>
   * @see gov.pnnl.velo.wiki.content.WikiContentExtractor#getSupportedMimetypes()
   */
  @Override
  public List<String> getSupportedMimetypes() {
    Set<String> keySet = mimetypeToTemplate.keySet();
    List<String> mimetypes = new ArrayList<String>();
    for (String key : keySet) {
      mimetypes.add(key);
    }
    return mimetypes;
  }

}
