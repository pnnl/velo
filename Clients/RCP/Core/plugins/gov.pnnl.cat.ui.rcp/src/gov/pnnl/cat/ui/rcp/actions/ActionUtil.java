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
package gov.pnnl.cat.ui.rcp.actions;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.SystemManager;
import gov.pnnl.cat.ui.rcp.actions.jobs.AbstractRepositoryJob;
import gov.pnnl.cat.ui.rcp.actions.resourceActions.IResourceActionBehavior;
import gov.pnnl.cat.ui.rcp.actions.resourceActions.ResourceActionManager;
import gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Shell;

/**
 * Utility functions used by the CAT RCP actions.
 *
 * @version $Revision: 1.0 $
 */
public class ActionUtil {
  
  // Constants
  public static final int COPY = DND.DROP_COPY;
  public static final int LINK = DND.DROP_LINK;
  public static final int MOVE = DND.DROP_MOVE;
  public static final int UPLOAD = 200;
  
  protected static Logger logger = CatLogger.getLogger(ActionUtil.class);

  /**
   * Method allVirtualFolders.
   * @param transObj ITransferObjectAdapter[]
   * @return boolean
   */
  public static boolean allVirtualFolders(ITransferObjectAdapter[] transObj) {
    if (transObj != null) {
      for (int i = 0; i < transObj.length; i++) {
        if (transObj[i].isVirtualFolder() == false) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
    
  /**
   * Converts a job type into it's string representation.
  
  
   * @param job AbstractRepositoryJob
   */
//  private static String getJobName(int iJobType) {
//    if (iJobType == COPY) {
//      return "Copy"; 
//    } else if (iJobType == LINK) {
//      return "Link"; 
//    } else if (iJobType == MOVE) {  
//      return "Move"; 
//    } else if (iJobType == UPLOAD) {
//      return "Upload"; 
//    }  
//    
//    return "";
//  }  

  public static void startJob(AbstractRepositoryJob job) {
    startJob(job, 0);
  }

  /**
   * Method startJob.
   * @param job Job
   * @param delay long
   */
  public static void startJob(Job job, long delay) {
    job.addJobChangeListener(SystemManager.getInstance());

//  IProgressConstants.PROGRESS_VIEW_ID = CatViewIDs.PROGRESS_MONITOR_VIEW;
//  transferJob.setProperty(IProgressConstants.PROGRESS_VIEW_ID, CatViewIDs.PROGRESS_MONITOR_VIEW);
    job.setUser(true);
    job.setPriority(Job.LONG);
    job.schedule(delay);
  }

  /**
   * Method createDndJob.
   * @param shell Shell
   * @param sourcePaths String[]
   * @param destination IFolder
   * @param operation int
   */
  public static void createDndJob(Shell shell, String[] sourcePaths, IFolder destination, int operation) {
    ResourceActionManager resourceActionMgr = ResourceActionManager.getInstance();
    String operationStr;

    switch (operation) {
      case (ActionUtil.COPY):
        operationStr = IResourceActionBehavior.ACTION_TYPE_COPY;
        break;
      case (ActionUtil.LINK):
        operationStr = IResourceActionBehavior.ACTION_TYPE_LINK;
        break;
      case (ActionUtil.MOVE):
        operationStr = IResourceActionBehavior.ACTION_TYPE_MOVE;
        break;
      case (ActionUtil.UPLOAD):
        operationStr = IResourceActionBehavior.ACTION_TYPE_UPLOAD;
        break;
      default:
        logger.error("Unexpected operation: " + operation);
        return;
    }

    resourceActionMgr.executeResourceAction(shell, sourcePaths, destination, operationStr);
  }
  
  
//  /**
//   * Moves the source path resources to the desination folder.
//   * @param sourcePaths paths to the orginal source resources.
//   * @param destination parent folder.
//   * @param operation type of ActionUtil action to do. ActionUtil.COPY, ActionUtil.UPLOAD, etc.
//   * @param isCATResource are the original source paths cat paths (verses system paths). 
//   */
//  public static void moveCopyJob(Shell shell, String[] sourcePaths, IFolder destination, int operation, boolean isCATResource) {
//  
//    System.out.println(Display.getCurrent().toString());
//    System.out.println("active shell's display = " + shell.getDisplay());
//    Job transferJob = new AbstractRepositoryJob(sourcePaths, destination, operation, isCATResource,
//          shell); //before this was "Uploading Files"    
//    transferJob.addJobChangeListener(SystemManager.getInstance());
//    
////    IProgressConstants.PROGRESS_VIEW_ID = CatViewIDs.PROGRESS_MONITOR_VIEW;
////    transferJob.setProperty(IProgressConstants.PROGRESS_VIEW_ID, CatViewIDs.PROGRESS_MONITOR_VIEW);
//    transferJob.setUser(true);
//    transferJob.setPriority(Job.LONG);
//    transferJob.schedule();    
//  }
//  
 
}
