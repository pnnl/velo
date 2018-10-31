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

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.net.VeloNetworkPlugin;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.dnd.ResourceList;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 */
public class OpenWithSystemEditor extends OpenWithAction {

  //the currect selection
  private IStructuredSelection selection;

  private static Logger logger = CatLogger.getLogger(OpenWithSystemEditor.class);

  public OpenWithSystemEditor() {
    super();

    this.setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_WINDOW, SharedImages.CAT_IMG_SIZE_16));
    this.setText("System Editor");
    this.setToolTipText("Open File(s) in native editor.");
  }

  /**
   * Method getEnabledStatus.
   * @param structSelection IStructuredSelection
   * @return boolean
   */
  @Override
  public boolean getEnabledStatus(IStructuredSelection structSelection) {

    this.selection = structSelection;

    if (structSelection.isEmpty()) {
      setEnabled(false);
      
    } else {
      List selectedItems = structSelection.toList();
      ResourceList selectedResources = new ResourceList();

      try {
        for (Object item : selectedItems) {
          selectedResources.add(RCPUtil.getResource(item));
        }

        // action is enable for a selection that contain only text files
        // TODO: we need to call Alfresco service to determine if files are text based on the mimetypes
        // registered on the server
        // for now action is enabled for a selection that contain only files
        setEnabled(selectedResources.isHomogeneous(IResource.FILE));

      } catch (ResourceException e) {
        logger.error("Error processing resources", e);
      }
    }

    return this.isEnabled();
  }

  /**
   * Method openFilesInSystemEditor.
   * @param allFiles Vector<IResource>
   * @param monitor IProgressMonitor
   */
  public static void openFilesInSystemEditor(Vector<IResource> allFiles, IProgressMonitor monitor) {
    
    try {

      IResourceManager mgr = ResourcesPlugin.getResourceManager();

      for (IResource resource : allFiles) {
        IFile fileResource = (IFile) resource;
        File localFile = null;
        
        if(VeloNetworkPlugin.getVeloFileSystemManager().isLocalDriveEnabled()) {
        	// use mapped file path so it will automatically save back to the server
        	localFile = new File(VeloNetworkPlugin.getVeloFileSystemManager().getLocalFilePath(fileResource.getPath().toDisplayString()));
        	
        } else {
        	localFile = mgr.getContentPropertyAsFile(fileResource.getPath(), VeloConstants.PROP_CONTENT);
        }
        
        boolean success = Program.launch(localFile.getAbsolutePath());
        if (!success) {
          String errMsg = "Could Not Launch Application.  No application was found that is associated with this file type.";
          ToolErrorHandler.handleError(errMsg, null, true);
        }


//        IFileStore fileStore= EFS.getStore(localFile.toURI());
//        IPath eclipsePath = new Path(localFile.getAbsolutePath());
//        org.eclipse.core.resources.IFile eclipseFile = SystemBasePlugin.getWorkspaceRoot().getFileForLocation(eclipsePath);
//        String editorId = IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID;
//        IDE.setDefaultEditor(eclipseFile, editorId);
//
//        FileEditorInput fileInput = new FileEditorInput(eclipseFile);
//        newWindow.getActivePage().openEditor(fileInput, editorId);

        if(monitor != null) {
          monitor.worked(1);
        }
      }

    } catch (Throwable e) {
      logger.error(e);
      StatusUtil.handleStatus("Failed to open system editor in new window.", e, StatusManager.SHOW);
    }    
  }
  
  /**
   * Method runInternal.
   * @param monitor IProgressMonitor
   */
  private void runInternal(IProgressMonitor monitor) {
    // find out what was selected to pass to the utility method and get just all the files
    StructuredSelection selectedFile = (StructuredSelection) this.selection;
    Vector<IResource> allFiles = this.getAllFiles(selectedFile);
    monitor.beginTask("Opening files", allFiles.size());
    openFilesInSystemEditor(allFiles, monitor);

  }

  /**
   * Method runJob.
   * @param monitor IProgressMonitor
   * @return IStatus
   */
  @Override
  public IStatus runJob(final IProgressMonitor monitor) {
    // have to run this in the UI thread because we use editor compponents
    Display.getDefault().asyncExec(new Runnable() {

      @Override
      public void run() {
        runInternal(monitor);
      }

    });

    return Status.OK_STATUS;
  }
  

  /**
   * Method getSelection.
   * @return IStructuredSelection
   */
  public IStructuredSelection getSelection() {
    return selection;
  }

  /**
   * Method setSelection.
   * @param selection IStructuredSelection
   */
  public void setSelection(IStructuredSelection selection) {
    this.selection = selection;
  }

}
