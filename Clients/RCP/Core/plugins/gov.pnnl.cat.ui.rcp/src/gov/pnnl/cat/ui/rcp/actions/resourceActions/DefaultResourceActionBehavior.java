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
package gov.pnnl.cat.ui.rcp.actions.resourceActions;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.ui.rcp.actions.jobs.CopyJob;
import gov.pnnl.cat.ui.rcp.actions.jobs.LinkJob;
import gov.pnnl.cat.ui.rcp.actions.jobs.MoveJob;
import gov.pnnl.cat.ui.rcp.actions.jobs.UploadJob;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides the default behavior for all extensible resource actions.
 * @version $Revision: 1.0 $
 */
public class DefaultResourceActionBehavior extends AbstractResourceActionBehavior {

  /**
   * Method run.
   * @param shell Shell
   * @param sourcePaths String[]
   * @param destination IFolder
   * @param operation String
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.actions.resourceActions.IResourceActionBehavior#run(Shell, String[], IFolder, String)
   */
  public boolean run(Shell shell, String[] sourcePaths, IFolder destination, String operation) {
    Job job = createJob(shell, sourcePaths, destination, operation);
    long delay = 0;

    // TODO: do we really need this code?
//    if (operation.equals(IResourceActionBehavior.ACTION_TYPE_UPLOAD)) {
//      // delay the start of an upload job to give the file a chance
//      // to download if it is being dragged from a webpage.
//      delay = 250;
//    }

    scheduleJob(job, delay);

    return true;
  }

  /**
   * Method createJob.
   * @param shell Shell
   * @param sourcePaths String[]
   * @param destination IFolder
   * @param operation String
   * @return Job
   */
  public static Job createJob(Shell shell, String[] sourcePaths, IFolder destination, String operation) {
    Job job = null;

    if (operation.equals(IResourceActionBehavior.ACTION_TYPE_COPY)) {
      job = new CopyJob(sourcePaths, destination.getPath(), shell);
    } else if (operation.equals(IResourceActionBehavior.ACTION_TYPE_LINK)) {
      job = new LinkJob(sourcePaths, destination.getPath(), shell);
    } else if (operation.equals(IResourceActionBehavior.ACTION_TYPE_MOVE)) {
      job = new MoveJob(sourcePaths, destination.getPath(), shell);
    } else if (operation.equals(IResourceActionBehavior.ACTION_TYPE_UPLOAD)) {
      job = new UploadJob(shell, sourcePaths, destination);
    }

    return job;
  }
}
