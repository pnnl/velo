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
package gov.pnnl.cat.jobs;

import gov.pnnl.cat.util.FileExtensionFilter;

import java.io.File;

/**
 */
public class TempFileConstants {

  public final static String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/CAT";
  public final static String METADATA_DIR = TEMP_DIR + "/metadata";
  public final static String TRANSFORM_COMPLETE_DIR = TEMP_DIR + "/transforms";
  public final static String WEBDAV_DIR = TEMP_DIR + "/WebDAV";
  public final static String CTEMP_DIR = TEMP_DIR + "/tmp";
  public final static String INDEX_TEMP_DIR = TEMP_DIR + "/index";
  
  public final static String READY_FOR_PROCESSING_EXT = ".ready";
  public final static String IN_PROGRESS_EXT = ".inprogress";
  
  public final static FileExtensionFilter READY_FILTER = new FileExtensionFilter(READY_FOR_PROCESSING_EXT);
  

  /**
   * Method renameFile.
   * @param file File
   * @param newExt String
   * @return File
   */
  public static File renameFile(File file, String newExt) {
    String fileName = file.getName();
    int pos = fileName.lastIndexOf(".");
    String baseName = fileName.substring(0,pos);
    if (newExt == null) {
      newExt = "";
    }
    fileName = baseName + newExt;
    File newFile = new File(file.getParent(), fileName);
    newFile.delete();
    file.renameTo(newFile);
    return newFile;
  }
  
  /**
   * Method removeExtension.
   * @param file File
   * @return File
   */
  public static File removeExtension(File file) {
    return renameFile(file, "");
  }
  
  /**
   * Method getFolder.
   * @param constant String
   * @return File
   */
  public static File getFolder(String constant) {
    File folder = new File(constant);
    if (folder.exists() == false) {
      // Make sure only one thread tries to create the folder at a time.
      synchronized (TEMP_DIR) {
        folder.mkdirs();        
      }
    }
    return folder;
  }
}
