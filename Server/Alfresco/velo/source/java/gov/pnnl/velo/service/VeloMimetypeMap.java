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
package gov.pnnl.velo.service;

import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: need to integrate other methods
 * 
 * TODO: this is messy since we have to have a separate mime.types file for Velo.
 
 * Alfresco mimetype-map.xml is a better way to hold this information
 * So either we copy all the mimetypes from mime.types to mimetype-map.xml,
 * or we have to overload this class's methods, which still requires modifying
 * two files.
 
 * @version $Revision: 1.0 $
 */
public class VeloMimetypeMap extends MimetypeMap {
  private static final Log logger = LogFactory.getLog(VeloMimetypeMap.class);
  private MimetypesFileTypeMap veloMimetypeMap;
  
  private static final String MIMETYPES_FILE_PATH = "/alfresco/module/velo/mime.types";
  
  // Any text mimetypes need to be added here or they will not be searchable
  // or previewable
  // TODO: this needs to be set in a config file
  private static List<String> veloTextMimetypes = new ArrayList<String>();
  static {
  	// TODO: text mimetypes should be registered via a config file
    veloTextMimetypes.add("application/x-las");
    veloTextMimetypes.add("application/x-perl");
    veloTextMimetypes.add("application/gviewer"); // *.inp files
    veloTextMimetypes.add("application/text/stomp/istomp");
  }
  
  @Override
  public void init() {
    super.init();
    logger.debug("Initializing VeloMimetypeMap");

    InputStream in = null;
    
    try {
      // Load the velo mimetypes map file
      if(WikiUtils.getWikiExtensions() != null){
        String mimetypeFilePath = WikiUtils.getWikiExtensions() + "/registry/mime.types";
        
        File file = new File(mimetypeFilePath);
        logger.debug("Found wiki mime.types file: " + file.getAbsolutePath());
        veloMimetypeMap = new MimetypesFileTypeMap(new FileInputStream(file));
        
      }else{
        in = this.getClass().getClassLoader().getResourceAsStream(MIMETYPES_FILE_PATH);
        if(in != null) {
          logger.debug("getting mime.types file out of Alfresco class path");
          veloMimetypeMap = new MimetypesFileTypeMap(in);
        } else {
          logger.error("Could not find " + MIMETYPES_FILE_PATH + " in classpath!");
        }
        
      }
    } catch (Throwable e) {
      logger.error("Failed to load mime.types file.", e);
    } finally {
      if(in != null) {
        try{in.close();} catch (Throwable e){}
      }
    }
  }
  /**
   * Method guessMimetype.
   * @param filename String
   * @return String
   * @see org.alfresco.service.cmr.repository.MimetypeService#guessMimetype(String)
   */
  @Override
  public String guessMimetype(String filename) {
    logger.debug("trying to guess mimetype");
    String mimetype =  super.guessMimetype(filename);
    
    if (mimetype.equals(MIMETYPE_BINARY)) {
      if(veloMimetypeMap != null) {
        logger.debug("Did not find mimetype in Alfresco, trying mime.types file");
        
        // Try to see if it's in velo's list
        // mimetypes map returns application/octet-stream if it can't find it
        mimetype = veloMimetypeMap.getContentType(filename);      
      } else {
        logger.debug("Did not find mimetype in Alfresco. No mime.types file either.");
        
      }
    }
    logger.debug("returning mimetype: " + mimetype);    
    return mimetype;
  }
  
  /**
   * Method getMimetype.
   * @param extension String
   * @return String
   * @see org.alfresco.service.cmr.repository.MimetypeService#getMimetype(String)
   */
  @Override
  public String getMimetype(String extension) {
    String mimetype = super.getMimetype(extension);
    if (mimetype.equals(MIMETYPE_BINARY) && veloMimetypeMap != null) {
      String fileName = "test." + extension;
      mimetype = veloMimetypeMap.getContentType(fileName);
    }
    logger.debug("returning mimetype: " + mimetype);
    return mimetype;
    
  }
  
  
  /**
   * Method isText.
   * @param mimetype String
   * @return boolean
   * @see org.alfresco.service.cmr.repository.MimetypeService#isText(String)
   */
  @Override
  public boolean isText(String mimetype) {   
    return super.isText(mimetype) || veloTextMimetypes.contains(mimetype) || mimetype.toLowerCase().contains("text");
  }
  
}
