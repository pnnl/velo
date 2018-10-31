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
package gov.pnnl.cat.transformers;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 */
public class ExecTransform {
  
  private static Log logger = LogFactory.getLog(ExecTransform.class);

  private static String libFolder = "C:\\Program Files\\CAT\\CAT Client\\bin";

  
  /**
   * Method getJavaHome.
   * @return String
   */
  public static String getJavaHome() {
    String jHome = System.getProperty("java.home");
    logger.debug("Java Home Used for Transforms: "+jHome);
    return jHome;
  }

  /**
   * Method getLibFolder.
   * @return String
   */
  public static String getLibFolder() {
    return libFolder;
  }

  /**
   * Method setLibFolder.
   * @param libFolder String
   */
  public void setLibFolder(String libFolder) {
    this.libFolder = libFolder;
  }
    
  /**
   * Method getClassesFolder.
   * @return String
   */
  public static String getClassesFolder() {
    File lib = new File(getLibFolder());
    String webinfFolder = lib.getParentFile().getAbsolutePath();
    String classesFolder = webinfFolder + File.separator + "classes";
    return classesFolder;
  }
  
  /**
   * Method getJavaExex.
   * @return String
   */
  public static String getJavaExex() {
	  String os = System.getProperty("os.name");
	  if(os.toLowerCase().startsWith("windows")){
		  return getJavaHome() + "/bin/java.exe";
	  }else{
		  return getJavaHome() + "/bin/java";
	  }
  }
  
  /**
   * Method getClassPath.
   * @return String
   */
  public static String getClassPath() {
    StringBuffer cp = new StringBuffer();
    
    // add WEB-INF/classes
    cp.append(getClassesFolder());
    cp.append(File.pathSeparatorChar);
    
    // add all jar files in WEB-INF/lib
    File binFolder = new File(getLibFolder());
    File files[] = binFolder.listFiles();
    
    for (int i = 0; i < files.length; i++) {
      if (files[i].getName().endsWith(".jar")) {
        cp.append(files[i].getPath());
        cp.append(File.pathSeparatorChar);
      }
    }
    if (cp.length() > 0) {
      cp.deleteCharAt(cp.length()-1);
    }
    return cp.toString();
  }
  
  /**
   * Method getSmallClassPath.
   * @return String
   */
  public static String getSmallClassPath() {
    StringBuffer cp = new StringBuffer();
    
    // add WEB-INF/lib/execTransform.jar
    File libFolder = new File(getLibFolder());
    File jarFile = new File(libFolder, "cat-core-1.4.jar");
    cp.append(jarFile.getPath());
    cp.append(File.pathSeparatorChar);
    
    return cp.toString();
  }
 
}

