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
package gov.pnnl.velo.ui.views;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.actions.ExplorerActions;
import gov.pnnl.cat.ui.rcp.actions.OpenFileInSystemEditorAction;
import gov.pnnl.cat.ui.rcp.contextmenus.VeloResourceContextMenu;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.dnd.CatResourceTransfer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.ui.adapters.CommentsDragAdapter;
import gov.pnnl.velo.ui.adapters.ScratchPadDropAdapter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class ScratchPadView extends ViewPart {
  public static final String ID = ScratchPadView.class.getName();

  private HashSet<Object> scratchPadSet = new HashSet<Object>();

  private WritableSet scratchPad;

  private TableViewer viewer;
  protected OpenFileInSystemEditorAction openFileAction;

  private IAction removeAction;

  private Table scratchPadTable;

  public ScratchPadView() {
  }

  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    this.scratchPadTable = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    viewer = new TableViewer(scratchPadTable);
    viewer.setLabelProvider(new WorkbenchLabelProvider());
    viewer.setContentProvider(new ObservableSetContentProvider());
    this.scratchPad = new WritableSet(scratchPadSet, Object.class);
    viewer.setInput(scratchPad);
    
    this.getViewSite().setSelectionProvider(viewer);

    createActions();
    createContextMenu();
    hookDragAndDrop();
    hookDoubleClickListener();
  }
  
  private void hookDoubleClickListener() {
    viewer.addDoubleClickListener(new IDoubleClickListener() {
      
      OpenFileInSystemEditorAction fopenFileAction = ExplorerActions.openFileInSystemEditorAction(viewer, false);

      public void doubleClick(DoubleClickEvent e) {
        StructuredSelection selectedFile = (StructuredSelection) viewer.getSelection();
        if (selectedFile != null && !selectedFile.isEmpty()) {
          IResource selectedResource = (IResource) selectedFile.getFirstElement();
          
          // Check for custom Behavior
          boolean doubleClicked = false;
          
          for(CustomDoubleClickBehavior behavior : TreeExplorer.getCustomDoubleClickBehaviors()) {           
            doubleClicked = behavior.doubleClick(selectedResource);
            if(doubleClicked) {
              break;
            }
          }
          if(!doubleClicked) {
            if (selectedResource instanceof IFile) { // Double click on file
              // Run application on file.
              fopenFileAction.run();
              
            } else if (selectedResource instanceof IFolder) { // Double click on
              RCPUtil.selectResourceInTree(selectedResource);
            }
          }
        }
      }
    });

  }
  
  private void createActions() {
    removeAction = new RemoveAction();
    IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
    toolbarManager.add(removeAction);
    
  }
  

  /**
   * Method getScratchPadContents.
   * @return Set<Object>
   */
  @SuppressWarnings("unchecked")
  public Set<Object> getScratchPadContents() {
    return this.scratchPad;
  }

  private void createContextMenu() {
   
    VeloResourceContextMenu fileFolderContextMenu = new VeloResourceContextMenu(getSite().getWorkbenchWindow(), null, viewer, false);

    fileFolderContextMenu.listenToViewer(viewer);

    openFileAction = new OpenFileInSystemEditorAction();
    openFileAction.setViewer(viewer);

    // Add the context menu for this explorer.
     MenuManager popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$

    popupMenuManager.addMenuListener(fileFolderContextMenu);
    popupMenuManager.setRemoveAllWhenShown(true);
    Menu menu = popupMenuManager.createContextMenu(viewer.getControl());
    viewer.getControl().setMenu(menu);
    fileFolderContextMenu.listenToViewer(viewer);
    
    // Create menu manager
    popupMenuManager.setRemoveAllWhenShown(true);
    popupMenuManager.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager mgr) {
        mgr.add(removeAction);
      }
    });

    // Register menu for extension
    getSite().registerContextMenu(popupMenuManager, viewer);
    
  }

  protected void hookDragAndDrop() {
    // hook drag
    Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer(), CatResourceTransfer.getInstance() };
    int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
    DragSourceListener listener = new CommentsDragAdapter(viewer);
    viewer.addDragSupport(operations, transferTypes, listener);

    // hook drop
    int ops = DND.DROP_COPY | DND.DROP_MOVE;
    viewer.addDropSupport(ops, transferTypes, new ScratchPadDropAdapter(viewer));
  }

  /**
   * Method addToScratchPad.
   * @param object Object
   */
  public void addToScratchPad(Object object) {
    if (scratchPad.add(object)) {
      viewer.setInput(scratchPad);
      viewer.refresh();
    }
  }

  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
  }

  /**
   */
  private class RemoveAction extends Action {
    public RemoveAction() {
      super("Remove from Scratch Pad");
      setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_DEL, SharedImages.CAT_IMG_SIZE_16));
      setToolTipText("Remove from Scratch Pad");
    }

    
    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      TableItem[] items = scratchPadTable.getSelection();
      for (TableItem item : items) {
        scratchPad.remove(item.getData());
      }
      viewer.refresh();
    }
  }

}
