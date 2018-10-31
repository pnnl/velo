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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class CopyJob extends AbstractTransferJob {

  private Logger logger = CatLogger.getLogger(this.getClass());
  private ICatCML copyCML = null;
  private IResourceManager mgr = ResourcesPlugin.getResourceManager();
  
  /**
   * Constructor for CopyJob.
   * @param sourcePaths String[]
   * @param destination CmsPath
   * @param shell Shell
   */
  public CopyJob(String[] sourcePaths, CmsPath destination, Shell shell) {
    super(sourcePaths, destination, shell);
    copyCML = getManager().getCML();
  }

  /**
   * Method getJobType.
   * @return String
   */
  public String getJobType() {
    return "Copy"; 
  }

  /**
   * Method getJobDescription.
   * @return String
   */
  public String getJobDescription() {
    return "Copying File";
  }

//  public void operate(ITransferObjectAdapter currentFile, CmsPath destination, HashMap dialogAnswers, IProgressMonitor monitor) throws ResourceException {
//    if (isSourceEqualToDestination(currentFile, destination)) {
//
////    this needs to happen sooner so that we get the correct new name before seeing if it already exists and ask to replace it
//      // do rename logic and just do a straight transfer on the mgr.
////      CmsPath proposedDestination = getUniqueName(destination, (IResource)currentFile.getObject(), ActionUtil.COPY);
//      
//      // DEBUG
//      logger.debug("proposedDestination: " + proposedDestination);
//      doFileTransfer(currentFile, proposedDestination, monitor);
//    } else {
//      super.operate(currentFile, destination, dialogAnswers, monitor);
//    }
//  }
  
  /**
   * Method doPreFileTransfer.
   * @param resolvedDestination CmsPath
   * @throws ResourceException
   */
  public void doPreFileTransfer(CmsPath resolvedDestination) throws ResourceException {
    copyCML.deleteResource(resolvedDestination);
  }

  /**
   * Method doFileTransfer.
   * @param transferFile ITransferObjectAdapter
   * @param resolvedDestination CmsPath
   * @param monitor IProgressMonitor
   * @throws ResourceException
   */
  public void doFileTransfer(ITransferObjectAdapter transferFile, CmsPath resolvedDestination, IProgressMonitor monitor) throws ResourceException {
    copyCML.copy(((IResource)transferFile.getObject()).getPath(), resolvedDestination, false);
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
      copyCML.copy(((IResource)transferFolder.getObject()).getPath(), newDestination, false);
      logger.debug("Doing Complete Copy on: "+((IResource)transferFolder.getObject()).getPath());
//      copyCML.addFolder(newDestination);
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
      logger.debug("Copy Job is copying resources using "+ copyCML.getCMLSize() +" CML statements");
      mgr.executeCml(copyCML);
      
      monitor.subTask("Copy Job Completed");
    } catch (ResourceException e) {
      String errMsg = "Failed to copy files.";
      ToolErrorHandler.handleError(errMsg, e, true);
    }
  }

  /**
   * Method getAction.
   * @return int
   */
  public int getAction() {
    return ActionUtil.COPY;
  }

 
}
