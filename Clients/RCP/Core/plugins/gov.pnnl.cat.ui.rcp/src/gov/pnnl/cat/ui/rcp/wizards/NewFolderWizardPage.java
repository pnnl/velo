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
import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatRcpMessages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.model.CmsPath;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 * @version $Revision: 1.0 $
 */

public class NewFolderWizardPage extends WizardPage {
	private Text destinationText;
	private Text folderText;

	private ISelection selection;

//  private IFolder container;
  private int folderType;
  private IWorkbenchWindow window;
  
  private IResource containerFolder;
  
  private IResourceManager mgr = ResourcesPlugin.getResourceManager();
  protected static Logger logger = CatLogger.getLogger(NewFolderWizardPage.class);
  
	/**
	 * Constructor for SampleNewWizardPage.
	 * @param window TODO
	
	
	 * @param selection ISelection
	 * @param folderType int
	 */
	public NewFolderWizardPage(ISelection selection, int folderType, IWorkbenchWindow window) {
		super("wizardPage");
    this.folderType = folderType;
    if(folderType == IResource.TAXONOMY_ROOT){
      setTitle(CatRcpMessages.NewTaxonomy_window_title);
      setDescription(CatRcpMessages.NewTaxonomy_description);
    }else if(folderType == IResource.PROJECT){
      setTitle("Project");
      setDescription("Create a new project.");
    } else {
  		setTitle("Folder");
  		setDescription("Create a new folder.");
    }
		this.selection = selection;
    this.window = window;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setEnabled(true);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		GridData gd;

		final Label foldernameLabel_1 = new Label(composite, SWT.NONE);
		foldernameLabel_1.setText("Destination:");

		destinationText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		destinationText.setEditable(true);
		final GridData gd_1 = new GridData(GridData.FILL_HORIZONTAL);
		destinationText.setLayoutData(gd_1);
    destinationText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    });

		final Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setLayoutData(new GridData(76, SWT.DEFAULT));
    browseButton.setText("B&rowse...");
  

    browseButton.addSelectionListener(new SelectionAdapter() {
      

      public void widgetSelected(final SelectionEvent e) {
        ResourceTreeDialog taxonomyDialog = new ResourceTreeDialog(window.getShell());
        taxonomyDialog.create();
        taxonomyDialog.setTitle("Select Parent Folder");
        taxonomyDialog.setMessage("Select the destination folder for this new folder.");
        
        try {
          IResource selResource = null;
          //if a folder is already selected, use it to start the browse tree
          if (isValid()) {
            try{
              selResource = mgr.getResource(getParentFolder());
            }catch(ResourceException exception){
              selResource = mgr.getHomeFolder();
            }
          }
          else {
            //else init the tree to just the users home folder.
            selResource = mgr.getHomeFolder();
          }

          if (selResource != null) {
            taxonomyDialog.setInitialSelection(selResource);
          }
        } catch (Exception ex) {
          //EZLogger.logError(ex, "Could not open resource chooser dialog.");
          logger.error("Could not open resource chosser dialog",ex);
        }
        if (taxonomyDialog.open() == Dialog.OK && taxonomyDialog.getSelectedResource() != null) {
          containerFolder = taxonomyDialog.getSelectedResource();
          destinationText.setText(taxonomyDialog.getSelectedResource().getPath().toDisplayString());
          dialogChanged();
        }
      }
    });
    
		Label foldernameLabel = new Label(composite, SWT.NULL);
		foldernameLabel.setText("&Name:");

		folderText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		folderText.setLayoutData(gd);
    folderText.setEditable(true);
		folderText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(composite);
		new Label(composite, SWT.NONE);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 * @return boolean
	 */
  private boolean isValid(){
    if(this.destinationText.getText().length() > 0){
      return true;
    }
    else return false;
  }
  
	private void initialize() {
	  if (selection != null && selection.isEmpty() == false
	      && selection instanceof IStructuredSelection) {
	    IStructuredSelection ssel = (IStructuredSelection) selection;
	    if (ssel.size() > 1)
	      return;
      
      IFolder container = null;
      Object selectedItem = ssel.getFirstElement();

      //if it is a folder or a file
      if(selectedItem instanceof IResource)
      {
        IResource resource = RCPUtil.getResource(selectedItem);

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
      }
      //for Team or User
      else if(selectedItem instanceof IProfilable)
      {
        IProfilable theItem = (IProfilable)selectedItem;
        try {
          container = (IFolder)mgr.getResource(theItem.getHomeFolder());
        } catch (ResourceException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else //can this ever happen?  
      {
        logger.error("Unfamiliar selected Item: " + selectedItem.getClass());
      }

	    if(this.folderType == IResource.TAXONOMY_ROOT){
	      folderText.setText(CatRcpMessages.NewTaxonomy_folder_text);
	    }else if (this.folderType == IResource.PROJECT){
	      folderText.setText("New Project");
	    } else {
	      folderText.setText("New Folder");
	    }
	    folderText.forceFocus();
	    folderText.selectAll();
	    if(container != null){
	      this.destinationText.setText(container.getPath().toDisplayString());
	      this.containerFolder = container;
	    }

	  }

	}


	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
    try {
//    if(container == null){
//      updateStatus("Folder container must be selected");
//      return;
//    }
      String foldername = getFolderName();
      if(foldername.length() == 0){
        updateStatus("Name must be specified");
        return;
      }
      if(getDestination().length() == 0 || this.containerFolder == null) {
        updateStatus("Parent folder must be specified");
        return;
      }
      
      if(foldername.matches(IResource.invalidCharactersRegex))
      {
        String errMsg = "A folder name " + IResource.invalidCharactersMsg;
        updateStatus(errMsg);
        return;
      }
      
      //need to do more now that taxonomys cannot be created inside a taxonomy, taxonomy folder, or project
      //but you can create a plain jane folder anywhere (at the moment)
      if(this.containerFolder != null && this.folderType == IResource.TAXONOMY_ROOT 
          && (this.containerFolder.isType(IResource.TAXONOMY_FOLDER) || this.containerFolder.isType(IResource.TAXONOMY_ROOT))){
        updateStatus(CatRcpMessages.CreateTaxonomy_parent_cannot_be_taxonomy);
        return;
      }
    //...weird, no longer seems to be the case, so allowing it again
//    if(this.containerFolder != null && this.folderType == IResource.TAXONOMY_ROOT 
//        && this.containerFolder.isType(IResource.PROJECT)){
//      updateStatus("Parent folder of a Taxonomy cannot be a Project.");
//      return;
//    }
      //also - you can't create a project inside a taxonomy 
      if(this.containerFolder != null && this.folderType == IResource.PROJECT 
          && (this.containerFolder.isType(IResource.TAXONOMY_FOLDER) 
              || this.containerFolder.isType(IResource.TAXONOMY_ROOT))){
        updateStatus(CatRcpMessages.CreateProject_parent_cannot_be_taxonomy);
        return;
      }
//    try{
//      if(this.containerFolder != null && this.folderType == IResource.PROJECT && this.containerFolder.isTypeInPath(IResource.PROJECT)){
//        updateStatus("Parent folder of a Project cannot be another Project or Project Folder.");
//        return;
//      }
//    }catch (ResourceException re){
//      //assuming if we catch an exception something is wrong and we can't create the project anyways
//      updateStatus("Parent folder of a Project cannot be another Project or Project Folder.");
//      return;
//    }
      updateStatus(null);
    } catch (ResourceException e) {
      logger.error(e);
      updateStatus("An unexpected error occurred! See the client log for details.");
    }
  }

	/**
	 * Method updateStatus.
	 * @param message String
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}


  /**
   * Method getFolderName.
   * @return String
   */
  public String getFolderName() {
    return folderText.getText().trim();
  }
  
  /**
   * Method setFolderName.
   * @param newText String
   */
  public void setFolderName(String newText) {
    folderText.setText(newText);
  }
  
  /**
   * Method getDestination.
   * @return String
   */
  public String getDestination() {
    return destinationText.getText();
  }
  
  /**
   * Method getParentFolder.
   * @return CmsPath
   */
  public CmsPath getParentFolder() {
    return new CmsPath(getDestination());
  }
  

  
}
