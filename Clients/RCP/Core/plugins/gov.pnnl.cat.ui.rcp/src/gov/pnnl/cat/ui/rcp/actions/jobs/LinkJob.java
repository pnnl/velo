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
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.actions.ActionUtil;
import gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class LinkJob extends AbstractTransferJob {

  private ICatCML linkCML = null;
  private ICatCML deleteCML = null;
  IResourceManager mgr = ResourcesPlugin.getResourceManager();
  
  /**
   * Constructor for LinkJob.
   * @param sourcePaths String[]
   * @param destination CmsPath
   * @param shell Shell
   */
  public LinkJob(String[] sourcePaths, CmsPath destination, Shell shell) {
    super(sourcePaths, destination, shell);
    linkCML = getManager().getCML();
    deleteCML = getManager().getCML();
  }

  /**
   * Method getJobType.
   * @return String
   */
  public String getJobType() {
    return "Link";
  }

  /**
   * Method getJobDescription.
   * @return String
   */
  public String getJobDescription() {
    return "Creating Link";
  }

  //no longer need this since it doesn't matter how many files we process since we're using a
  //CML statement hence we can't monitor progress - see getTotalWork() below
//  protected long countSourceFileBytes() {
//    return getSourceFiles().length;
//  }
  
  //any jobs that use CML statement we can't really monitor the progress
  /**
   * Method getTotalWork.
   * @return int
   */
  public int getTotalWork() {
    return IProgressMonitor.UNKNOWN;
  }
    
  /**
   * Method canTreatAsFile.
   * @param currentFile ITransferObjectAdapter
   * @return boolean
   */
  protected boolean canTreatAsFile(ITransferObjectAdapter currentFile) {
    return true;
  }

  /**
   * Method doFileTransfer.
   * @param transferFile ITransferObjectAdapter
   * @param resolvedDestination CmsPath
   * @param monitor IProgressMonitor
   * @throws ResourceException
   */
  public void doFileTransfer(ITransferObjectAdapter transferFile, CmsPath resolvedDestination, IProgressMonitor monitor) throws ResourceException {
//    CmsPath destinationFolderPath = resolvedDestination.removeLastSegments(1);
//    IFolder destinationFolder = (IFolder) getManager().getResource(destinationFolderPath);

    //this needs to happen sooner so that we get the correct new name before seeing if it already exists and ask to replace it
    // do rename logic and just do a straight transfer on the mgr.
//    CmsPath proposedDestination = getUniqueName(destinationFolderPath, (IResource)transferFile.getObject(), ActionUtil.LINK);

    IResource resource = (IResource) transferFile.getObject();
    if (resource instanceof ILinkedResource){
      resource = ((ILinkedResource) resource).getTarget();
    }
    
    String targetUuid = resource.getPropertyAsString(VeloConstants.PROP_UUID);
    linkCML.addLink(resolvedDestination, targetUuid);
  }
  
//  removing this implementation because when pasting a link inside a folder where the link's filename
//  already existed caused CAT to overwrite the file with the link without asking.
//  protected boolean checkFileOperation(CmsPath filePath, Map dialogAnswers) {
//    return true;
//  }
  
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
      linkCML.addFolder(newDestination);
    }
    return false;
  }
  
  
  /**
   * Method jobTraversalComplete.
   * @param monitor IProgressMonitor
   */
  public void jobTraversalComplete(IProgressMonitor monitor) {
    try {
      monitor.subTask("Server Executing Request");
      mgr.executeCml(deleteCML);
      mgr.executeCml(linkCML);
      monitor.subTask("Link Job Completed");
    } catch (ResourceException e) {
      String errMsg = "Failed to link files.";
      ToolErrorHandler.handleError(errMsg, e, true);
    }
  }

  
  /**
   * Method doPreFileTransfer.
   * @param resolvedDestination CmsPath
   * @throws ResourceException
   */
  public void doPreFileTransfer(CmsPath resolvedDestination) throws ResourceException {
    //have to have another cml to execute before the link one because it appeared that
    //if the delete and addlink were both in the same cml statement, the delete was not
    //happening first
    deleteCML.deleteResource(resolvedDestination);
  }

  
  /**
   * Method getAction.
   * @return int
   */
  public int getAction() {
    return ActionUtil.LINK;
  }
}
