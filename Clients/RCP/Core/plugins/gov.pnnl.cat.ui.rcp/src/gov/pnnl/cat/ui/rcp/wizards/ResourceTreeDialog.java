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

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.IResourceSelection;
import gov.pnnl.cat.ui.rcp.ResourceStructuredSelection;
import gov.pnnl.cat.ui.rcp.dialogs.ResourceSelectionValidator;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.viewers.ResourceTreeViewer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FileFolderSorter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FolderFilter;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

/**
 * Dialog that allows users to browse for resources in the Velo repository.
 * TODO: expose the filter on the resource tree
 */
public class ResourceTreeDialog extends TitleAreaDialog {
  public final static int NEW_FOLDER_ID = IDialogConstants.CLIENT_ID + 1;

  private static final Logger logger = CatLogger.getLogger(ResourceTreeDialog.class);
  private Object root; // so we can set root to something other than company home
  private List<IResource> selectedResources;
  private ResourceTreeViewer treeViewer;
  private boolean showFiles;
  private boolean selectMultiple;
  private ViewerFilter[] viewerFilters = null;
  private ResourceSelectionValidator validator;
  private Button okButton;

  /**
   * Constructor for ResourceTreeDialog.
   * @param parentShell Shell
   */
  public ResourceTreeDialog(Shell parentShell) {
    this(parentShell, false);
  }
  
  /**
   * Constructor for ResourceTreeDialog.
   * @param parentShell Shell
   * @param showFiles boolean
   */
  public ResourceTreeDialog(Shell parentShell, boolean showFiles) {
    this(parentShell, ResourcesPlugin.getResourceManager().getRoot(), showFiles, false);
  }  
  
  public ResourceTreeDialog(Shell parentShell, Object root, boolean showFiles, boolean selectMultiple) {
    this(parentShell, root, null, showFiles, selectMultiple);
  }

  /**
   * Constructor for ResourceTreeDialog.
   * @param parentShell Shell
   * @param root Object
   * @param showFiles boolean
   * @param selectMultiple boolean
   */
  public ResourceTreeDialog(Shell parentShell, Object root, ResourceSelectionValidator validator, boolean showFiles, boolean selectMultiple) {
    super(parentShell);
    setShellStyle(this.getShellStyle() | SWT.SHEET);
    this.showFiles = showFiles;
    this.root = root;
    this.selectMultiple = selectMultiple;
    this.validator = validator;
  }

  /**
   * Method setRoot.
   * @param root Object
   */
  public void setRoot(Object root) {
    this.root = root;
  }

  public void setViewerFilters(ViewerFilter... filters){
    this.viewerFilters = filters;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent) {

    if(selectMultiple) {
      treeViewer = new ResourceTreeViewer(true, parent, SWT.MULTI | SWT.BORDER);
      
    } else {
      treeViewer = new ResourceTreeViewer(true, parent, SWT.SINGLE | SWT.BORDER);
    }
    Tree tree = treeViewer.getTree();
    GridData grid_data = createLayoutData();

    parent.getShell().setText("Resource Tree");
    tree.setLayoutData(grid_data);

    treeViewer.setSorter(new FileFolderSorter());
    if (!showFiles) {
      treeViewer.addFilter(new FolderFilter());
    }
    
    if(this.viewerFilters != null){
      for (ViewerFilter filter : viewerFilters) {
        treeViewer.addFilter(filter);
      }
    }
    
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    if(root == null) {
      treeViewer.setInput(mgr);
    } else {
      treeViewer.setInput(root);
    }
    
    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent e) {
        List<IResource> selection = null;
        if(!e.getSelection().isEmpty()){
          IResourceSelection resourceSelection = new ResourceStructuredSelection((IStructuredSelection) e.getSelection());
          selection = resourceSelection.getIResources();
        } else {
          selection = new ArrayList<IResource>();
        }
        validateSelection(selection);
      }
    });
    
    treeViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent e) {
        List<IResource> selection = null;
        if(!e.getSelection().isEmpty()){
          IResourceSelection resourceSelection = new ResourceStructuredSelection((IStructuredSelection) e.getSelection());
          selection = resourceSelection.getIResources();
        } else {
          selection = new ArrayList<IResource>();
        }
        if(validateSelection(selection)) {
          okPressed();
        }
      }
    });

    return treeViewer.getControl();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContents(Composite parent) {

    Control contents = super.createContents(parent);
    
    // Initialize Selection
    // Have to do this here so it happens after the button bar has been created,
    // so we can enable the OK button correctly
    initializeSelection();
   
    return contents;
  }
  
  private boolean validateSelection(List<IResource> selection) {
    String errorMessage = null;
    selectedResources = selection;
    
    if(validator != null) {
      errorMessage = validator.validateSelection(selection);
    }
    boolean valid = errorMessage == null;
    
    
    setErrorMessage(errorMessage);
    okButton.setEnabled(valid);
    
    return valid;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, NEW_FOLDER_ID, "&New Folder", false);
    // create OK and Cancel buttons by default
    okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
        true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
   */
  @Override
  protected void buttonPressed(int buttonId) {
    if (buttonId == NEW_FOLDER_ID) {
      try {
        createNewFolder();
      } catch (Throwable e) {
        ToolErrorHandler.handleError("An error occurred creating the folder.", e, true);
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
    IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
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
        treeViewer.refresh(selection.getFirstElement());
        // TODO: automatically select the new folder
      }
    }
  }

  /**
   * Method createLayoutData.
   * @return GridData
   */
  private GridData createLayoutData() {
    GridData grid_data = new GridData();

    grid_data.grabExcessHorizontalSpace = true;
    grid_data.grabExcessVerticalSpace = true;
    grid_data.horizontalAlignment = SWT.FILL;
    grid_data.verticalAlignment = SWT.FILL;
    return grid_data;
  }

  /**
   * Method getInitialSize.
   * @return Point
   */
  protected Point getInitialSize() {
    return new Point(375, 480);
  }

  /**
   * Method getSelectedResources.
   * @return List<IResource>
   */
  public List<IResource> getSelectedResources() {
    return selectedResources;
  }
  
  /**
   * Method getSelectedResource.
   * @return IResource
   */
  public IResource getSelectedResource() {
    if(selectedResources == null || selectedResources.size() == 0) {
      return null;
    }
    return selectedResources.get(0);
  }

  /**
   * Method setInitialSelection.
   * @param selectedResource IResource
   */
  public void setInitialSelection(IResource selectedResource) {
    List<IResource> initialSelection = new ArrayList<IResource>();
    initialSelection.add(selectedResource);
    setInitialSelection(initialSelection);
  }
  
  /**
   * Method setInitialSelection.
   * @param initialSelection List<IResource>
   */
  public void setInitialSelection(List<IResource> initialSelection) {
    selectedResources = initialSelection;
    if(selectedResources == null) {
      selectedResources = new ArrayList<IResource>();
    }
  }
  
  protected void initializeSelection() {
    if(selectedResources != null){   
      // first expand the nodes in the tree
      List<CmsPath> selectedPaths = new ArrayList<CmsPath>();
      for(IResource resource : selectedResources) {
        selectedPaths.add(resource.getPath());
      }
      treeViewer.expandToPath(selectedPaths);  
      
      // now select them
      treeViewer.selectResources(selectedResources);
      
      validateSelection(selectedResources);
    }
    
  }
  

}
