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
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.preferences.PreferenceUtils;
import gov.pnnl.cat.ui.rcp.dialogs.ResourceSelectionValidator;
import gov.pnnl.cat.ui.rcp.dialogs.ResourceSelectionValidatorAdapter;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 */
public class FileExportPage extends WizardPage implements Listener {
  //Error Messages
  private static final String INVALID_DESTINATION = "Destination directory is not valid or has not been specified.";
  private static final String INVALID_FILE = "Export folder/file is not valid or has not been specified";

  private org.eclipse.swt.widgets.List resourcesToExportList;

  private Text destinationFileField;

  private Button browseCatButton;

  private Button browseDestinationButton;
  
  private List<IResource> resourcesToExport;
  private String newNameForSingleExportedResource = null;

  private Button exportCommentsChk;

  private String currentMessage;
  private IResourceManager mgr;

  private Object catTreeRoot = null;
  boolean showExportComments = true;

  private static final Logger logger = CatLogger.getLogger(FileExportPage.class);

  /**
   * Constructor for FileExportPage.
   * @param pageName String
   * @param initialSelection List<IResource>
   */
  public FileExportPage(String pageName, List<IResource> initialSelection) {
    super(pageName);
    setTitle("File Export Wizard");
    setDescription("Export resources to the local file system.");
    setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_WORKSET, SharedImages.CAT_IMG_SIZE_64));
    mgr = ResourcesPlugin.getResourceManager();
    this.resourcesToExport = initialSelection;
  }
  
  /**
   * Method setShowExportComments.
   * @param showExportComments boolean
   */
  public void setShowExportComments(boolean showExportComments) {
    this.showExportComments = showExportComments;
  }

  /**
   * @param catTreeRoot the catTreeRoot to set
   */
  public void setCatTreeRoot(Object catTreeRoot) {
    this.catTreeRoot = catTreeRoot;
  }

  /**
   * Method createControl.
   * @param parent Composite
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  @Override
  public void createControl(Composite parent) {
    Composite workArea = new Composite(parent, SWT.NONE);
    setControl(workArea);
    workArea.setLayout(new GridLayout());
    workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    createBrowseSection(workArea);
  }

  /**
   * Create the export section
   * @param workArea Composite
   */
  private void createBrowseSection(Composite workArea) {

    // project specification group
    Composite importGroup = new Composite(workArea, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    layout.makeColumnsEqualWidth = false;
    layout.marginWidth = 0;
    importGroup.setLayout(layout);
    importGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    //Export File Section
    Label sourceLabel = new Label(importGroup, SWT.NONE);
    sourceLabel.setText("Export resource(s):");
    sourceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
        
    resourcesToExportList = new org.eclipse.swt.widgets.List(importGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
   // resourcesToExportList.setEnabled(false);
    GridData catPathData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    resourcesToExportList.setLayoutData(catPathData);
    browseCatButton = new Button(importGroup, SWT.PUSH);
    browseCatButton.setText("Browse...");
    GridData buttonGD = setButtonLayoutData(browseCatButton);
    buttonGD.verticalAlignment = SWT.TOP;

    //Destination Folder Section
    Label destinationLabel = new Label(importGroup, SWT.RIGHT);
    destinationLabel.setText("To destination:");
    destinationLabel.setAlignment(SWT.RIGHT);
    destinationFileField = new Text(importGroup, SWT.BORDER);
    GridData desktopPathData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    destinationFileField.setLayoutData(desktopPathData); // browse button
    
    String lastOpenedFolder = PreferenceUtils.getLastBrowsedLocalDirectoryPath();
    if(lastOpenedFolder != null) {
      destinationFileField.setText(lastOpenedFolder);
    }
    browseDestinationButton = new Button(importGroup, SWT.PUSH);
    browseDestinationButton.setText("Browse...");
    setButtonLayoutData(browseDestinationButton);


    //Export Comments Section
    if(showExportComments) {
      Composite chkGroup = new Composite(workArea, SWT.NONE);
      chkGroup.setLayout(new RowLayout());
      exportCommentsChk = new Button(chkGroup, SWT.CHECK);
      exportCommentsChk.setText("Export file comments (if available)");
    }

    //Add modify listeners to the components and 
    //check for page completion (will initialize error messages)
    resourcesToExportList.addListener(SWT.Modify, this);
    updateExportFileField();
    destinationFileField.addListener(SWT.Modify, this);
    browseDestinationButton.addListener(SWT.Selection, this);
    browseCatButton.addListener(SWT.Selection, this);

    //Check for page completion once the controls are created
    updatePageCompletion();
  }

  /**
   * Handle the event when the "Browse" button
   * for the export file/folder is clicked
   */
  protected void handleCatButtonPressed() {
    
    ResourceSelectionValidator validator = new ResourceSelectionValidatorAdapter() {

      @Override
      public String validateSelection(List<IResource> selectedResources) {
        String errorMessage = null;
        if(selectedResources.size() == 0) {
          errorMessage = "You must select at least one resource.";
        }
        return errorMessage;
      }
      
    };

    ResourceTreeDialog dialog = new ResourceTreeDialog(getContainer().getShell(), catTreeRoot, validator, true, true);
  
    if(resourcesToExport != null && resourcesToExport.size() > 0) {
      dialog.setInitialSelection(resourcesToExport);
    }

    dialog.create();
    dialog.setTitle("Export Resources");
    dialog.setMessage("Select the resources you would like to export.");

    if (dialog.open() == Dialog.OK && dialog.getSelectedResource() != null) {
      resourcesToExport = dialog.getSelectedResources();
      updateExportFileField();
    }
  }
  
  private void updateExportFileField() {
    String[] items = new String[resourcesToExport.size()];
    
    for(int i = 0; i < resourcesToExport.size(); i++) {
      items[i] = resourcesToExport.get(i).getPath().toDisplayString();
    }

    resourcesToExportList.setItems(items);
  }

  /**
   * Handle the event when the "Browse" button
   * for the destination source is clicked
   */
  protected void handleBrowseDestinationButtonPressed() {
    DirectoryDialog dialog = new DirectoryDialog(destinationFileField.getShell(), SWT.SAVE | SWT.SHEET);
    String lastOpenedFolder = PreferenceUtils.getLastBrowsedLocalDirectoryPath();
    if(lastOpenedFolder != null) {
      dialog.setFilterPath(lastOpenedFolder);
    }
    dialog.setText("Export To Directory");
    dialog.setFilterPath(getDestinationFileValue());
    
    

    String selectedFolder = dialog.open();
    if (selectedFolder != null) {
      setDestinationFileValue(selectedFolder);
      PreferenceUtils.setLastBrowsedLocalFile(new File(selectedFolder));
    }
  }

  /**Handles all of the events for the widgets
   * on the page.
   * 
   * This includes modification of text in a text field
   * and when a button is pressed
   * @param e Event
   * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
   */
  public void handleEvent(Event e){
    Widget source = e.widget;

    if(source == browseDestinationButton){
      handleBrowseDestinationButtonPressed();
    }else if(source == browseCatButton){
      handleCatButtonPressed();
    }
    updatePageCompletion();
  }

  /**
   * Update the page completion 
   * by validating the export and destination
   * file/folder paths.
   * 
   * Sets the dialog error message and page completion
   * for the component
   */
  private void updatePageCompletion(){
    boolean complete = validateExportFile() && validateDestinationPath();

    if(complete){
      setErrorMessage(null);
      //Setting the page complete to true will enable the "Finish" button
      setPageComplete(true);
    }else{
      setErrorMessage(currentMessage);
      //Setting the page complete to false will disable the "Finish" button
      setPageComplete(false);
    }
  }

  /**
   * Validates the export file/folders path to
   * make sure it exists.
   * 
   * Sets the error message if the file/folder can't
   * be found
  
   * @return a boolean value describing if the resource was found or not */
  private boolean validateExportFile(){

    if(resourcesToExport != null && resourcesToExport.size() > 0) {
      return true;
    } 
    return false;
  }

  /**
   * Check that a valid destination path was selected
  
   * @return a boolean value describing if the destination
   * path was found or not */
  protected boolean validateDestinationPath() {
    newNameForSingleExportedResource = null;
    boolean valid = false;
    File dir = new File(getDestinationFileValue());
    if(dir.exists() && dir.isDirectory()) {
      valid = true;
      
    } else if(resourcesToExport.size() == 1) {
      File parent = dir.getParentFile();
      if(parent != null && parent.exists() && parent.isDirectory()) {
        newNameForSingleExportedResource = dir.getName();
        valid = true;
      }
    }

    if (!valid) {
      setCurrentMessage(INVALID_DESTINATION);
    }

    return valid;
  }

  /**
   * Method dispose.
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  @Override
  public void dispose(){
    browseCatButton.removeListener(SWT.Selection, this);
    browseDestinationButton.removeListener(SWT.Selection, this);
    resourcesToExportList.removeListener(SWT.Modify, this);
    destinationFileField.removeListener(SWT.Modify, this);
    super.dispose();
  }

  /**
   * Get the current error message for the page
  
   * @return a string describing the current error
   * message on the page */
  public String getCurrentMessage() {
    return currentMessage;
  }

  /**
   * Set the current error message for the page
   * @param currentMessage String
   */
  public void setCurrentMessage(String currentMessage) {
    this.currentMessage = currentMessage;
  }

  /**
   * Get the destination path
  
   * @return a String describing the destination 
   * path */
  public String getDestinationFileValue() {
    return destinationFileField.getText().trim();
  }

  /**
   * Set the destination path
   * @param value String
   */
  public void setDestinationFileValue(String value) {
    this.destinationFileField.setText(value);
  }

  /**
   * Check to see if the export comments checkbox
   * was selected.
  
   * @return a boolean value describing the 
   * selection state of the export comments checkbox */
  public boolean getExportComments(){
    if(exportCommentsChk != null) {
      return exportCommentsChk.getSelection();
    } else {
      return false;
    }
  }

  /**
  
   * @return the resourcesToExport */
  public List<IResource> getResourcesToExport() {
    return resourcesToExport;
  }

  /**
  
   * @return the newName */
  public String getNewNameForSingleExportedResource() {
    return newNameForSingleExportedResource;
  }
  
}
