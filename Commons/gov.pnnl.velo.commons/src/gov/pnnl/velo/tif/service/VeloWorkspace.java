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
package gov.pnnl.velo.tif.service;

import java.io.File;

/**
 * This class provides information regarding the local Velo Workspace, where
 * configuration data should be stored (such as logs and local machine config files).
 * 
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public interface VeloWorkspace {
  

  /**
   * Folder where all velo runtime information (such as logs) should be written.
   * Default is $USER_HOME/.velo 
   * @return File
   */
  public File getVeloFolder();

  
}
