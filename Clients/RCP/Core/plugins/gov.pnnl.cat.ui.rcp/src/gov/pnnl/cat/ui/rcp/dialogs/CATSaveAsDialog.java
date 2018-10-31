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
package gov.pnnl.cat.ui.rcp.dialogs;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.UiPlugin;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.NewFolderDialog;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.awt.Toolkit;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * A standard "Save As" dialog which solicits a path from the user. 
 * Copied from Eclipse, but modified to use CAT resources and
 * CAT resource tree browser
 * @version $Revision: 1.0 $
 */
public class CATSaveAsDialog extends TitleAreaDialog {

  private static final String DIALOG_SETTINGS_SECTION = "SaveAsDialogSettings"; //$NON-NLS-1$
  public final static int NEW_FOLDER_ID = IDialogConstants.CLIENT_ID + 1;

  private IResource originalFile = null;
  private String originalName = null;
  private CmsPath result;
  private boolean showFiles;
  private boolean allowExistingResources = false;

  // widgets
  private ResourceAndContainerGroup resourceGroup;

  private Button okButton;
  private Object root;
  private ResourceSelectionValidator parentFolderValidator;

  /**
   * Image for title area
   */
  private Image dlgTitleImage = null;

  /**
   * Creates a new Save As dialog for no specific file.
   *
   * @param parentShell the parent shell
   * @param showFiles boolean
   * @param allowExistingResources boolean
   * @param root Object
   * @param parentFolderValidator ParentFolderValidator
   */
  public CATSaveAsDialog(Shell parentShell, boolean showFiles, boolean allowExistingResources, Object root, ResourceSelectionValidator parentFolderValidator) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
    this.showFiles = showFiles;
    this.root = root;
    this.allowExistingResources = allowExistingResources;
    this.parentFolderValidator = parentFolderValidator;
  }
  
  /**
   * Constructor for CATSaveAsDialog.
   * @param parentShell Shell
   * @param showFiles boolean
   * @param root Object
   * @param parentFolderValidator ParentFolderValidator
   */
  public CATSaveAsDialog(Shell parentShell, boolean showFiles, Object root, ResourceSelectionValidator parentFolderValidator) {
    this(parentShell, showFiles, false, root, parentFolderValidator);
  }
  
  /* (non-Javadoc)
   * Method declared in Window.
   */
  /**
   * Method configureShell.
   * @param shell Shell
   */
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText("Save As");
  }

  /* (non-Javadoc)
   * Method declared in Window.
   */
  /**
   * Method createContents.
   * @param parent Composite
   * @return Control
   */
  protected Control createContents(Composite parent) {

    Control contents = super.createContents(parent);

    initializeControls();
    validatePage();
    resourceGroup.setFocus();
    setTitle("Save As");
    setMessage("Save to another location.");

    return contents;
  }

  /** 
   * The <code>SaveAsDialog</code> implementation of this <code>Window</code>
   * method disposes of the banner image when the dialog is closed.
   * @return boolean
   */
  public boolean close() {
    if (dlgTitleImage != null) {
      dlgTitleImage.dispose();
    }
    return super.close();
  }


  /* (non-Javadoc)
   * Method declared on Dialog.
   */
  /**
   * Method createDialogArea.
   * @param parent Composite
   * @return Control
   */
  protected Control createDialogArea(Composite parent) {
    // top level composite
    Composite parentComposite = (Composite) super.createDialogArea(parent);

    // create a composite with standard margins and spacing
    Composite composite = new Composite(parentComposite, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setFont(parentComposite.getFont());

    Listener listener = new Listener() {
      public void handleEvent(Event event) {
        setDialogComplete(validatePage());
      }
    };

    resourceGroup = new ResourceAndContainerGroup(
        composite,
        listener,
        "File name:", "file", showFiles, root,
        parentFolderValidator);
    
    resourceGroup.setAllowExistingResources(allowExistingResources);

    return parentComposite;
  }

  /**
   * Returns the full path entered by the user.
   * <p>
   * Note that the file and container might not exist and would need to be created.
   * See the <code>IFile.create</code> method and the 
   * <code>ContainerGenerator</code> class.
   * </p>
   *
  
   * @return the path, or <code>null</code> if Cancel was pressed */
  public CmsPath getResult() {
    return result;
  }

  /**
   * Initializes the controls of this dialog.
   */
  private void initializeControls() {
    if (originalFile != null) {
      resourceGroup.setContainerFullPath(originalFile.getParent().getPath());
      resourceGroup.setResource(originalFile.getName());
    } else if (originalName != null) {
      resourceGroup.setResource(originalName);
    }
    setDialogComplete(validatePage());
  }

  /* (non-Javadoc)
   * Method declared on Dialog.
   */
  protected void okPressed() {
    // Get new path.
    CmsPath path = resourceGroup.getContainerFullPath().append(resourceGroup.getResourceName());

    //If the user does not supply a file extension and if the save 
    //as dialog was provided a default file name append the extension 
    //of the default filename to the new name
    // TODO: maybe convert this later
//    if (path.getFileExtension() == null) {
//      if (originalFile != null && originalFile.getFileExtension() != null) {
//        path = path.addFileExtension(originalFile.getFileExtension());
//      } else if (originalName != null) {
//        int pos = originalName.lastIndexOf('.');
//        if (++pos > 0 && pos < originalName.length()) {
//          path = path.addFileExtension(originalName.substring(pos));
//        }
//      }
//    }

    // If the path already exists then confirm overwrite.
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    IResource file = null;
    try {
      file = mgr.forceGetResource(path);
    } catch (Throwable e) {
      // exception means file doesnt exist 
    }
    
    if (file != null) {
      String[] buttons = new String[] { IDialogConstants.YES_LABEL,
          IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
      String question = "The file " + path.toDisplayString() + " already exists. Do you want to replace the existing file?";
      MessageDialog d = new MessageDialog(getShell(),
          "Question",
          null, question, MessageDialog.QUESTION, buttons, 0) {
        protected int getShellStyle() {
          return super.getShellStyle() | SWT.SHEET;
        }
      };
      int overwrite = d.open();
      switch (overwrite) {
        case 0: // Yes
        break;
        case 1: // No
          return;
        case 2: // Cancel
        default:
          cancelPressed();
          return;
      }
    }

    // Store path and close.
    result = path;
    close();
  }

  /**
   * Sets the completion state of this dialog and adjusts the enable state of
   * the Ok button accordingly.
   *
   * @param value <code>true</code> if this dialog is compelete, and
   *  <code>false</code> otherwise
   */
  protected void setDialogComplete(boolean value) {
    okButton.setEnabled(value);
  }

  /**
   * Sets the original file to use.
   *
   * @param originalFile the original file
   */
  public void setOriginalFile(IResource originalFile) {
    this.originalFile = originalFile;
  }

  /**
   * Set the original file name to use.
   * Used instead of <code>setOriginalFile</code>
   * when the original resource is not an IFile.
   * Must be called before <code>create</code>.
   * @param originalName default file name
   */
  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  /**
   * Returns whether this page's visual components all contain valid values.
   *
  
   * @return <code>true</code> if valid, and <code>false</code> otherwise */
  private boolean validatePage() {
    if (!resourceGroup.areAllValuesValid()) {
      if (!resourceGroup.getResourceName().equals("")) { //$NON-NLS-1$
        setErrorMessage(resourceGroup.getProblemMessage());
      } else {
        setErrorMessage(null);
      }
      return false;
    }

    setErrorMessage(null);
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
   * 
   * @since 3.2
   */
  protected IDialogSettings getDialogBoundsSettings() {
    IDialogSettings settings = UiPlugin.getDefault().getDialogSettings();
    IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
    if (section == null) {
      section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
    } 
    return section;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  protected boolean isResizable() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    okButton = createButton(parent, IDialogConstants.OK_ID,
        IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);
    createButton(parent, NEW_FOLDER_ID, "&New Folder", false);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
   */
  @Override
  protected void buttonPressed(int buttonId) {
    if (buttonId == NEW_FOLDER_ID) {
      try {
        createNewFolder();
      } catch (ResourceException e) {
        ToolErrorHandler.handleError("Error creating folder.", null, true);
      }
    } else {
      super.buttonPressed(buttonId);
    }
  }

  /**
   * Method createNewFolder.
   * @throws ResourceException
   */
  protected void createNewFolder() throws ResourceException {
    IStructuredSelection selection = (IStructuredSelection) resourceGroup.getContainerGroup().getTreeViewer().getSelection();
    IResource resource = RCPUtil.getResource(selection.getFirstElement());

    if (!(resource instanceof IFolder)) {
      // the tree should always have a folder selected, so
      // this should never even happen
      Toolkit.getDefaultToolkit().beep();
    } else {
      IFolder folder = (IFolder) resource;
      List<IResource> children = folder.getChildren();

      String[] childNames = new String[children.size()];

      for (int i = 0; i < children.size(); i++) {
        childNames[i] = children.get(i).getName();
      }

      NewFolderDialog newFolderDialog = new NewFolderDialog(getShell(), childNames);
      int result = newFolderDialog.open();
      if (result == IDialogConstants.OK_ID) {
        IResourceManager mgr = ResourcesPlugin.getResourceManager();
        CmsPath newPath = resource.getPath().append(newFolderDialog.getName());
        mgr.createFolder(newPath);
        //IResource newFolder = mgr.getResource(newPath);
        resourceGroup.getContainerGroup().getTreeViewer().refresh(selection.getFirstElement());
        // TODO: automatically select the new folder
      }
    }
  }

}

