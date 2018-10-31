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
package gov.pnnl.cat.ui.rcp.wizards;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.rcp.actions.ActionUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class ImportFilesWizard extends Wizard implements IImportWizard {
  /***
   * the page where to specify the files to import and the destination CAT folder
   */
  protected FileChooserPage importPage = null;

  private static Logger logger = CatLogger.getLogger(ImportFilesWizard.class);

  private IWorkbenchWindow workbenchWindow;

  private IStructuredSelection selection;
  
  private Object catTreeRoot = null;

  /***
  
  
  
   */
  public ImportFilesWizard() {
    this("Import Files", null);
  }

  /**
   * Constructor for ImportFilesWizard.
   * @param title String
   * @param catTreeRoot Object
   */
  public ImportFilesWizard(String title, Object catTreeRoot) {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle(title);
    this.catTreeRoot = catTreeRoot;
  }

  /**
   * Method init.
   * @param workbench IWorkbench
   * @param selection IStructuredSelection
   * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.workbenchWindow = workbench.getActiveWorkbenchWindow();
    this.selection = selection;
  }

  /***
   * Add the 'Detail' and 'Import Model' pages
   * 
  
   * @see Wizard@addpages */
  public void addPages() {
    setDefaultPageImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_WORKSET, SharedImages.CAT_IMG_SIZE_64));
    importPage = new FileChooserPage("importPageId", "Select the files to import.", workbenchWindow, this.selection) {
      
      @Override
      protected boolean validateSelectedFiles() {
        // this is a generic import, so we don't care which files were selected
        return true;
      }
    };
    
    importPage.setCatTreeRoot(catTreeRoot);
    importPage.setSelectMultipleFiles(true);
    addPage(importPage);
  }

  /***
   * Do the work after everything is specified.
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish() {
    boolean success = false;
    
    String[] selectedFiles = importPage.getSelectedFiles();
    CmsPath destPath = importPage.getDestinationFolder();
    try {
      IFolder destinationFolder = (IFolder) ResourcesPlugin.getResourceManager().getResource(destPath);

      // Import Files in a user job
      if(selectedFiles != null && selectedFiles.length > 0) {
        ActionUtil.createDndJob(getShell(), selectedFiles, destinationFolder, ActionUtil.UPLOAD);
      }

      success = true;

    } catch (final Exception e) {

      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          ToolErrorHandler.handleError("Error Importing Files", e, true);
        }
      });
    }
    return success;
  }

  
}
