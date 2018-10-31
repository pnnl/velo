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

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public abstract class FileChooserPage extends WizardPage {

  protected static Logger logger = CatLogger.getLogger(FileChooserPage.class);
  
  protected Text destinationText;
  protected IWorkbenchWindow workbenchWindow;
  protected Text selectedFilesText;
  protected String[] selectedFiles;
  protected IStructuredSelection selection;
  protected IResource containerFolder;
  protected boolean validFolder;
  protected boolean selectMultipleFiles = false;
  protected String fileExtension = null;
  protected Object catTreeRoot = null;
  protected boolean showFiles = true;
  
  /**
  
   * @return the showFiles */
  public boolean isShowFiles() {
    return showFiles;
  }

  /**
   * @param showFiles the showFiles to set
   */
  public void setShowFiles(boolean showFiles) {
    this.showFiles = showFiles;
  }

  /**
  
   * @return the catTreeRoot */
  public Object getCatTreeRoot() {
    return catTreeRoot;
  }

  /**
   * @param catTreeRoot the catTreeRoot to set
   */
  public void setCatTreeRoot(Object catTreeRoot) {
    this.catTreeRoot = catTreeRoot;
  }

  /**
   * Method getFileExtension.
   * @return String
   */
  public String getFileExtension() {
    return fileExtension;
  }

  /**
   * Method setFileExtension.
   * @param fileExtension String
   */
  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }

  /**
  
   * @return the selectMultipleFiles */
  public boolean isSelectMultipleFiles() {
    return selectMultipleFiles;
  }

  /**
   * @param selectMultipleFiles the selectMultipleFiles to set
   */
  public void setSelectMultipleFiles(boolean selectMultipleFiles) {
    this.selectMultipleFiles = selectMultipleFiles;
  }

  /**
   * Constructor for FileChooserPage.
   * @param pageName String
   * @param description String
   * @param workbenchWindow IWorkbenchWindow
   * @param selection IStructuredSelection
   */
  public FileChooserPage(String pageName, String description, IWorkbenchWindow workbenchWindow, IStructuredSelection selection) {
    super(pageName);
    this.workbenchWindow = workbenchWindow;

    this.selection = selection;
    setTitle("File Chooser");
    setDescription(description);
  }

  /**
   * Method createControl.
   * @param parent Composite
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    container.setEnabled(true);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    layout.numColumns = 3;
    layout.verticalSpacing = 9;
    GridData gd;
    
    Label foldernameLabel = new Label(container, SWT.NULL);
    if(selectMultipleFiles) {
      foldernameLabel.setText("Files:");
    } else {
      foldernameLabel.setText("File:");
    }
    selectedFilesText = new Text(container, SWT.BORDER | SWT.SINGLE);
    gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
    selectedFilesText.setLayoutData(gd);
    selectedFilesText.setEditable(false);
    selectedFilesText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    });

    FileDialog fileDialog = null;
    
    if(selectMultipleFiles) {
      // Let user select multiple files
      fileDialog = new FileDialog(this.workbenchWindow.getShell(), SWT.MULTI | SWT.OPEN);
      
    } else {
      fileDialog  = new FileDialog(this.workbenchWindow.getShell(), SWT.OPEN);      
    }
    
    if(fileExtension != null) {
      fileDialog.setFilterExtensions(new String[]{fileExtension});
    }
    
    final FileDialog finalFileDialog = fileDialog;    
    final Button button = new Button(container, SWT.NONE);
    button.setLayoutData(new GridData(76, SWT.DEFAULT));
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {

        String fileString = finalFileDialog.open();
        if(fileString != null){//will be null if user cancels

          // Append all the selected files. Since getFileNames() returns only 
          // the names, and not the path, prepend the path
          selectedFiles = finalFileDialog.getFileNames();
          for (int i = 0; i < selectedFiles.length; i++) {
            StringBuffer buf = new StringBuffer();
            buf.append(finalFileDialog.getFilterPath());
            if (buf.charAt(buf.length() - 1) != File.separatorChar) {
              buf.append(File.separatorChar);
            }
            buf.append(selectedFiles[i]);
            selectedFiles[i] = buf.toString();
          }
        }


        if (selectedFiles != null && selectedFiles.length > 0) {
          String nameString = "";
          for(int i = 0; i < selectedFiles.length; i++) {
            if(i > 0) {
              nameString += ";";
            }
            nameString += selectedFiles[i];
          }
          selectedFilesText.setText(nameString);
        }
      }

    });
    button.setText("B&rowse...");
    
    final Label destinationLabel = new Label(container, SWT.NONE);
    destinationLabel.setText("Destination:");

    destinationText = new Text(container, SWT.BORDER);
    destinationText.setEditable(false);
    destinationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    final ResourceTreeDialog catResourceDialog = new ResourceTreeDialog(this.workbenchWindow.getShell(), catTreeRoot, showFiles, false);
    
    final Button browseButton = new Button(container, SWT.NONE);
    browseButton.setLayoutData(new GridData(76, SWT.DEFAULT));
    browseButton.setText("Br&owse...");
    browseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        try {
          IResourceManager mgr = ResourcesPlugin.getResourceManager();
          IResource selResource = null;
          //if a folder is already selected, use it to start the browse tree
          if (validFolder) {
            selResource = mgr.getResource(getDestinationFolder());
          } else {
            //else init the tree to just the users home folder.
            selResource = mgr.getHomeFolder();
          }

          if (selResource != null) {
            catResourceDialog.setInitialSelection(selResource);
          }
        } catch (Exception ex) {
          //EZLogger.logError(ex, "Could not open resource chooser dialog.");
          logger.error("Could not open resource chooser dialog",ex);
        }
        if (catResourceDialog.open() == Dialog.OK && catResourceDialog.getSelectedResource() != null) {
          containerFolder = catResourceDialog.getSelectedResource();
          destinationText.setText(containerFolder.getPath().toDisplayString());
          dialogChanged();
        }
      }
    });

    initialize();
    dialogChanged();
    setControl(container);
    // don't show an error message right away.
    setErrorMessage(null);
  }
  
  private void initialize() {
    if (selection != null && selection.isEmpty() == false
        && selection instanceof IStructuredSelection) {
      IStructuredSelection ssel = (IStructuredSelection) selection;
      if (ssel.size() > 1)
        return;
      IResource resource = RCPUtil.getResource(ssel.getFirstElement());
      IFolder container = null;
      try {
        if(resource instanceof ILinkedResource){
          resource = ((ILinkedResource)resource).getTarget();
        }
        
        if (resource instanceof IFile) {
          container = (IFolder) resource.getParent();
        }else if (resource instanceof IFolder) {
          container = (IFolder) resource;
        }
      } catch (ResourceException e) {
        // TODO Auto-generated catch block
        logger.error(e);
      }
     
      if(container != null){
        this.destinationText.setText(container.getPath().toDisplayString());
        this.validFolder = true;
        this.containerFolder = container;
      }
    }
  }
  
  /**
   * Child classes need to validate if the selected file(s) and destination folder(s) are
   * ok.
  
   * @return false if selection is not valid */
  protected abstract boolean validateSelectedFiles();
  
  private void dialogChanged() {
    if(selectedFilesText.getText().length() == 0){
      updateStatus("File must be specified");
      return;
    }
    
    if(this.getDestination().length() == 0){
      updateStatus("Destination must be specified");
      return;
    } 
    
    try {
      if(validateSelectedFiles()) {
        updateStatus(null);        
      }
      
    } catch (ResourceException e) {
      logger.error(e);
      updateStatus("An unexpected error occurred! See the client log for details.");
    }
  }
  
  /**
   * Method updateStatus.
   * @param message String
   */
  public void updateStatus(String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  /**
   * Method getDestination.
   * @return String
   */
  public String getDestination() {
    return destinationText.getText();
  }
  
  /**
   * Method getDestinationFolder.
   * @return CmsPath
   */
  public CmsPath getDestinationFolder() {
    return new CmsPath(getDestination());
  }

  /**
  
   * @return the selectedFiles */
  public String[] getSelectedFiles() {
    return selectedFiles;
  }
  
}
