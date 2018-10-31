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
package gov.pnnl.cat.ui.rcp.handlers;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.ui.rcp.actions.jobs.AbstractRepositoryJob;
import gov.pnnl.cat.ui.rcp.actions.jobs.LinkJob;

import org.eclipse.swt.widgets.Shell;

/**
 */
public class PasteShortcutHandler extends PasteHandler {

  /**
   * Method createJob.
   * @param sourcePaths String[]
   * @param destinationFolder IFolder
   * @param shell Shell
   * @return AbstractRepositoryJob
   */
  protected AbstractRepositoryJob createJob(String[] sourcePaths, IFolder destinationFolder, Shell shell) {
    return new LinkJob(sourcePaths, destinationFolder.getPath(), shell);
  }
  
}
