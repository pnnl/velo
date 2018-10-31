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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatRcpMessages;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.rcp.actions.jobs.ImportTaxonomyJob;
import gov.pnnl.velo.model.CmsPath;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class ImportTaxonomyWizard extends Wizard implements IImportWizard {
  /***
   * the page where to specify the taxonomy file to import and the destination CAT folder
   */
  protected FileChooserPage importPage = null;

  private static Logger logger = CatLogger.getLogger(ImportTaxonomyWizard.class);

  private IWorkbenchWindow workbenchWindow;

  private IStructuredSelection selection;

  /***
  
  
  
   */
  public ImportTaxonomyWizard() {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle(CatRcpMessages.ImportTaxonomy_window_title);
  }

  /**
   * Constructor for ImportTaxonomyWizard.
   * @param title String
   */
  public ImportTaxonomyWizard(String title) {
    setWindowTitle(title);
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
    setDefaultPageImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_IMPORT_TAXONOMY, SharedImages.CAT_IMG_SIZE_64));

    importPage = new FileChooserPage("importPageId", "Choose the taxonomy file to import.", workbenchWindow, this.selection) {
      
      @Override
      protected boolean validateSelectedFiles() {
        if(this.containerFolder != null && 
            (this.containerFolder.isType(IResource.TAXONOMY_FOLDER) || this.containerFolder.isType(IResource.TAXONOMY_ROOT))){
          updateStatus(CatRcpMessages.CreateTaxonomy_parent_cannot_be_taxonomy);
          return false;
        }
        return true;
      }
    };
    
    importPage.setFileExtension("*.tax");
    addPage(importPage);
  }

  /***
   * Do the work after everything is specified.
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish() {
    final boolean[] success = { false };
    String[] selectedFiles = importPage.getSelectedFiles();
    
    // final IPath parentFolder = importPage.getParentFolderName();
    final String file = selectedFiles[0];
    logger.debug("file they selected: " + file);
    final CmsPath parentFolder = importPage.getDestinationFolder();

    try {
      if (preCheckTaxonomyFile(file, parentFolder)) {
        ImportTaxonomyJob importJob = new ImportTaxonomyJob(file, parentFolder);
        importJob.setPriority(Job.LONG);
        importJob.setUser(true);
        importJob.schedule();
        success[0] = true;
      } else {
        success[0] = false;
      }

    } catch (Exception e) {
      logger.error("Error importing taxonomy.", e);
      final IStatus status = new Status(IStatus.ERROR, CatRcpPlugin.PLUGIN_ID, 0, e.getClass().getSimpleName() + " was thrown", e);

      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          ErrorDialog.openError(getShell(), "Error Importing Taxonomy", "The taxonomy could not be imported because an error occurred.", status);
        }
      });
    }
    return success[0];
  }

  /**
   * checks the file for errors before performing the import
   * 
   * @param filename
   * @param parentPath
  
  
   * @return boolean
   * @throws Exception */
  private boolean preCheckTaxonomyFile(String filename, CmsPath parentPath) throws Exception {
    BufferedReader reader = null;
    boolean badLine = false;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
      String folder;
      String badNameRegex = IResource.invalidCharactersRegex;// moved to IResource".*[\"\\*\\\\>\\<\\?\\/\\:\\|\\%\\&\\+\\;\\xA3\\xAC]+.*";
      String endsWithTabRegex = ".*[\\t]+";// used to check for trailing tabs
      String blankLineRegex = "[\\s]+";// used to check for totally blank lines
      int lineNumber = 0;
      String line = reader.readLine();

      String errorMessage = "";
      while (line != null && !badLine) {
        lineNumber++;
        folder = line.trim();
        // check for bad characters
        badLine = false;
        errorMessage = "";
        if (folder.matches(badNameRegex)) {
          badLine = true;
          errorMessage = "A folder name " + IResource.invalidCharactersMsg;
          // }else if(line.matches(endsWithTabRegex)){
          // badLine = true;
          // errorMessage = "A line in the taxonomy import file cannot end with a tab.";
        }
        // else if(line.matches(blankLineRegex) || line.length() == 0 || folder.length() == 0){
        // badLine = true;
        // errorMessage = "Blank lines are not allowed in the taxonomy import file";
        // }

        if (badLine) {
          // TODO: what are these hex characters that are not allowed so that i can display to user in error message??? A3 AC
          final int badLineNumber = lineNumber;
          final String message = errorMessage;
          Display.getDefault().syncExec(new Runnable() {
            public void run() {
              final IStatus status = new Status(IStatus.ERROR, CatRcpPlugin.PLUGIN_ID, 0, message, null);
              ErrorDialog.openError(getShell(), "Error Importing Taxonomy", "The taxonomy could not be imported because the file has an invalid character(s) on line number " + badLineNumber + ".", status);
            }
          });
        }
        line = reader.readLine();
      }

    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return !badLine;
  }

  /**
   * Method that uses an Alfresco action to do the work
   */
  // private static void importTaxonomy(String filename, IPath parentPath) throws IOException, ResourceException {
  // IResourceManager mgr = ResourcesPlugin.getResourceManager();
  // mgr.importTaxonomy(parentPath, new File(filename));
  // }
}
