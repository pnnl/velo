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
/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.service.VeloWorkspace;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

/**
 * Default implementation which requires setting the folders via the spring
 * configuration path.
 * 
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class VeloWorkspaceDefault implements VeloWorkspace {
  private static Logger logger = Logger.getLogger(VeloWorkspaceDefault.class);
  
  /*
   * NOTE: I had to rename the fields to NOT match the getters in the interface definition
   * or else Spring thinks my setter methods are not writable :(
   */
  private Resource veloDir;

  /**
   * @param veloDir the veloDir to set
   */
  public void setVeloDir(Resource veloDir) {
    this.veloDir = veloDir;
  }


  /**
   * @return the veloDir
   */
  public File getVeloFolder() {
    try {
      return veloDir.getFile();

    } catch (IOException e) {
      logger.error("Failed to load filesystem resource.", e);
      return null;
    }
  }



}
