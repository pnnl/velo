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
package gov.pnnl.cat.ui.rcp.actions.jobs;

import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.ActionUtil;
import gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.io.FileNotFoundException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class MoveJob extends AbstractTransferJob {

  private Logger logger = CatLogger.getLogger(this.getClass());
  private ICatCML moveCML = null;
  private ICatCML deleteCML = null;
  IResourceManager mgr = ResourcesPlugin.getResourceManager();
  
  /**
   * Constructor for MoveJob.
   * @param sourcePaths String[]
   * @param destination CmsPath
   * @param shell Shell
   */
  public MoveJob(String[] sourcePaths, CmsPath destination, Shell shell) {
    super(sourcePaths, destination, shell);
    moveCML = getManager().getCML();
    deleteCML = getManager().getCML();
  }

  /**
   * Method getJobType.
   * @return String
   */
  public String getJobType() {
    return "Move";
  }

  /**
   * Method getJobDescription.
   * @return String
   */
  public String getJobDescription() {
    return "Moving File";
  }

  /**
   * Method canStartOperation.
   * @param currentFile ITransferObjectAdapter
   * @param destination CmsPath
   * @return boolean
   */
  public boolean canStartOperation(ITransferObjectAdapter currentFile, CmsPath destination) {
    CmsPath transObjPath = new CmsPath(currentFile.getPath());
    if (isSourceEqualToDestination(currentFile, destination)) {
      String errMsg = "Error Moving File or Folder.  The source and destination file names are the same.";
      ToolErrorHandler.handleError(errMsg, null, true);
      return false;
      
    } else if (transObjPath.isPrefixOf(destination)) {
      // This case should just be ignored as it was probably done by mistake - nobody tries to drag a folder into itself or its child
      // unless they have a hand spaz :)
//      String errMsg = "Error Moving File or Folder.  The destination folder cannot be a subfolder of what you are trying to move.";
//      ToolErrorHandler.handleError(errMsg, null, true);
      return false;
    }
    return true;
  }

  /**
   * Method currentOperationComplete.
   * @param currentFile ITransferObjectAdapter
   * @param destination IFolder
   * @param dialogAnswers HashMap<?,?>
   */
  public void currentOperationComplete(ITransferObjectAdapter currentFile, IFolder destination, HashMap<?, ?> dialogAnswers) {
    if (!canTreatAsFile(currentFile)) {
      //TODO: see if there are any links to this folder, 
      //if so point the links to the new folder before we delete it
//            ((ILinkedResource)transObj[i]).
//            getManager().
      try {
        CmsPath folderPath = new CmsPath(currentFile.getPath());
        IFolder resource = (IFolder) getManager().getResource(folderPath);
        if (resource.getChildren().size() == 0) {
          getManager().deleteResource(folderPath);
        }
      } catch (ResourceException e) {
        //EZLogger.logError(e, "Cannot delete folder");
        logger.error("Cannot delete folder",e);
      }
    }
  }

  /**
   * Method doFileTransfer.
   * @param transferFile ITransferObjectAdapter
   * @param resolvedDestination CmsPath
   * @param monitor IProgressMonitor
   * @throws ResourceException
   */
  public void doFileTransfer(ITransferObjectAdapter transferFile, CmsPath resolvedDestination, IProgressMonitor monitor) throws ResourceException {
//    getManager().move( ((IResource)transferFile.getObject()).getPath(), resolvedDestination, monitor );
    moveCML.move(((IResource)transferFile.getObject()).getPath(), resolvedDestination);
    monitor.worked((int)transferFile.getSize());
  }

  /**
   * Method doFolderTransfer.
   * @param transferFolder ITransferObjectAdapter
   * @param originalDestination CmsPath
   * @param newDestination CmsPath
   * @param monitor IProgressMonitor
   * @return boolean
   * @throws ResourceException
   * @throws FileNotFoundException
   */
  public boolean doFolderTransfer(ITransferObjectAdapter transferFolder, CmsPath originalDestination, CmsPath newDestination, IProgressMonitor monitor) throws ResourceException, FileNotFoundException {
    if (!getManager().resourceExists(newDestination)) {
      moveCML.move(((IResource)transferFolder.getObject()).getPath(), newDestination);
      logger.debug("Doing Complete Move on: "+((IResource)transferFolder.getObject()).getPath());
//      moveCML.addFolder(newDestination);
      return true;
    }  
    return false;
  }
  //any jobs that use CML statement we can't really monitor the progress
  /**
   * Method getTotalWork.
   * @return int
   */
  public int getTotalWork() {
    return IProgressMonitor.UNKNOWN;
  }
  
  /**
   * Method jobTraversalComplete.
   * @param monitor IProgressMonitor
   */
  public void jobTraversalComplete(IProgressMonitor monitor) {
    try {
      monitor.subTask("Server Executing Request");
      logger.debug("Move Job is moving resources using "+ moveCML.getCMLSize() +" CML statements");
      mgr.executeCml(moveCML);
      logger.debug("Delete Job is deleting resources using "+ deleteCML.getCMLSize() +" CML statements");
      mgr.executeCml(deleteCML);
      monitor.subTask("Move Job Completed");
    } catch (ResourceException e) {
      String errMsg = "Failed to copy files.";
      ToolErrorHandler.handleError(errMsg, e, true);
    }
  }

  
  /**
   * Method doPreFileTransfer.
   * @param resolvedDestination CmsPath
   * @throws ResourceException
   */
  public void doPreFileTransfer(CmsPath resolvedDestination) throws ResourceException {
    moveCML.deleteResource(resolvedDestination);
  }

  /**
   * Method getAction.
   * @return int
   */
  public int getAction() {
    return ActionUtil.MOVE;
  }

  /**
   * Method doRemoveFolder.
   * @param folder ITransferObjectAdapter
   * @return boolean
   */
  @Override
  public boolean doRemoveFolder(ITransferObjectAdapter folder) {
    IResource target = (IResource)folder.getObject();
    try {
      if (getManager().resourceExists(target.getPath())) 
      {
        deleteCML.deleteResource(target.getPath());
        return true;
      }
    } catch (ResourceException e) {
      logger.error("Could not delete folder", e);
    }
    return false;
  }
}
