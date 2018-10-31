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
package gov.pnnl.cat.util;

import java.io.File;
import java.io.FileFilter;

/**
 */
public class DirFilter implements FileFilter {

  private final static DirFilter dirFilter = new DirFilter();
  
  /**
   * Method getInstance.
   * @return DirFilter
   */
  public static DirFilter getInstance() {
    return dirFilter;
  }
  
  /**
   * Method accept.
   * @param file File
   * @return boolean
   * @see java.io.FileFilter#accept(File)
   */
  public boolean accept(File file) {
    if (file.isDirectory()) {
      return true;
    }
    return false;
  }

}
