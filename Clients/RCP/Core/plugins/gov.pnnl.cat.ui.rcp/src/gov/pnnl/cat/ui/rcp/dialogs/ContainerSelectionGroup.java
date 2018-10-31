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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.IResourceSelection;
import gov.pnnl.cat.ui.rcp.ResourceStructuredSelection;
import gov.pnnl.cat.ui.rcp.viewers.ResourceTreeViewer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FileFolderSorter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FolderFilter;
import gov.pnnl.velo.model.CmsPath;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.DrillDownComposite;

/**
 * Workbench-level composite for choosing a container.
 * @version $Revision: 1.0 $
 */
public class ContainerSelectionGroup extends Composite {
  // The listener to notify of events
  private Listener listener;

  // Last selection made by user
  private IResource selectedContainer;

  // handle on parts
  private Text containerNameField;

  private ResourceTreeViewer treeViewer;
  private boolean showFiles;
  private Object root; // so we can set root to something other than company home

  
  // the message to display at the top of this dialog
  private static final String DEFAULT_MSG_SELECT_ONLY = "Select the parent folder:";

  // sizing constants
  private static final int SIZING_SELECTION_PANE_WIDTH = 320;

  private static final int SIZING_SELECTION_PANE_HEIGHT = 300;

  /**
   * Creates a new instance of the widget.
   * 
   * @param parent
   *            The parent widget of the group.
   * @param listener
   *            A listener to forward events to. Can be null if no listener is
   *            required.
   * @param allowNewContainerName
   *            Enable the user to type in a new container name instead of
   *            just selecting from the existing ones.
   * @param showFiles boolean
   * @param root Object
   */
  public ContainerSelectionGroup(Composite parent, Listener listener,
      boolean allowNewContainerName, boolean showFiles, Object root) {
    this(parent, listener, null, showFiles, root);
  }

  /**
   * Creates a new instance of the widget.
   * 
   * @param parent
   *            The parent widget of the group.
   * @param listener
   *            A listener to forward events to. Can be null if no listener is
   *            required.
  
   * @param message
   *            The text to present to the user.
  
   * @param showFiles boolean
   * @param root Object
   */
  public ContainerSelectionGroup(Composite parent, Listener listener, String message, boolean showFiles, Object root) {
    this(parent, listener, message, SIZING_SELECTION_PANE_HEIGHT,
        SIZING_SELECTION_PANE_WIDTH, showFiles, root);
  }

  /**
   * Creates a new instance of the widget.
   * 
   * @param parent
   *            The parent widget of the group.
   * @param listener
   *            A listener to forward events to. Can be null if no listener is
   *            required.
  
   * @param message
   *            The text to present to the user.
  
   * @param heightHint
   *            height hint for the drill down composite
   * @param widthHint
   *            width hint for the drill down composite
   * @param showFiles boolean
   * @param root Object
   */
  public ContainerSelectionGroup(Composite parent, Listener listener, String message,
      int heightHint, int widthHint, boolean showFiles, Object root) {
    super(parent, SWT.NONE);
    this.listener = listener;
    this.root = root;
    if (message != null) {
      createContents(message, heightHint, widthHint);
 
    } else {
      createContents(DEFAULT_MSG_SELECT_ONLY, heightHint, widthHint);
    }
    this.showFiles = showFiles;
  }

  /**
   * The container selection has changed in the tree view. Update the
   * container name field value and notify all listeners.
   * 
   * @param container
   *            The container that changed
   */
  public void containerSelectionChanged(IResource container) {
    selectedContainer = container;

    // fire an event so the parent can update its controls
    if (listener != null) {
      Event changeEvent = new Event();
      changeEvent.type = SWT.Selection;
      changeEvent.widget = this;
      listener.handleEvent(changeEvent);
    }
  }

  /**
   * Creates the contents of the composite.
   * 
   * @param message
   */
  public void createContents(String message) {
    createContents(message, SIZING_SELECTION_PANE_HEIGHT,
        SIZING_SELECTION_PANE_WIDTH);
  }

  /**
   * Creates the contents of the composite.
   * 
   * @param message
   * @param heightHint
   * @param widthHint
   */
  public void createContents(String message, int heightHint, int widthHint) {
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    setLayout(layout);
    setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Label label = new Label(this, SWT.WRAP);
    label.setText(message);
    label.setFont(this.getFont());

    // filler...
    new Label(this, SWT.NONE);

    createTreeViewer(heightHint);
    Dialog.applyDialogFont(this);
  }

  /**
   * Returns a new drill down viewer for this dialog.
   * 
   * @param heightHint
   *            height hint for the drill down composite
   */
  protected void createTreeViewer(int heightHint) {
    // Create drill down.
    DrillDownComposite drillDown = new DrillDownComposite(this, SWT.BORDER);
    GridData spec = new GridData(SWT.FILL, SWT.FILL, true, true);
    spec.widthHint = SIZING_SELECTION_PANE_WIDTH;
    spec.heightHint = heightHint;
    drillDown.setLayoutData(spec);

    // Create tree viewer inside drill down.
    treeViewer = new ResourceTreeViewer(true, drillDown, SWT.SINGLE | SWT.BORDER);
    Tree tree = treeViewer.getTree();
    drillDown.setChildTree(treeViewer);

    GridData grid_data = createLayoutData();

    tree.setLayoutData(grid_data);

    treeViewer.setSorter(new FileFolderSorter());
    if (!showFiles) {
      treeViewer.addFilter(new FolderFilter());
    }
    
    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IResourceSelection resourceSelection = new ResourceStructuredSelection((IStructuredSelection) event.getSelection());
        containerSelectionChanged(resourceSelection.getIResource());
      }
    });
    treeViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection) {
          Object item = ((IStructuredSelection) selection)
              .getFirstElement();
          if (item == null) {
            return;
          }
          if (treeViewer.getExpandedState(item)) {
            treeViewer.collapseToLevel(item, 1);
          } else {
            treeViewer.expandToLevel(item, 1);
          }
        }
      }
    });
    
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    if(root == null) {
      treeViewer.setInput(mgr);
    } else {
      treeViewer.setInput(root);
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
   * Returns the currently entered container name. Null if the field is empty.
   * Note that the container may not exist yet if the user entered a new
   * container name in the field.
   * 
  
   * @return CmsPath */
  public CmsPath getDestinationFolderPath() {
//    if (allowNewContainerName) {
//      String pathName = containerNameField.getText();
//      if (pathName == null || pathName.length() < 1) {
//        return null;
//      }
//      // The user may not have made this absolute so do it for them
//      return new CmsPath(pathName);
//
//    }
    if (selectedContainer == null)
      return null;
    return selectedContainer.getPath();

  }

  /**
   * Gives focus to one of the widgets in the group, as determined by the
   * group.
   */
  public void setInitialFocus() {
    treeViewer.getTree().setFocus();
  }

  /**
   * Sets the selected existing container.
   * 
   * @param container
   */
  public void setSelectedContainer(IResource container) {
    selectedContainer = container;    
    treeViewer.expandToPath(this.selectedContainer.getPath());  
    treeViewer.selectResource(container);

  }

  /**
  
   * @return the treeViewer */
  public ResourceTreeViewer getTreeViewer() {
    return treeViewer;
  }
  
}
