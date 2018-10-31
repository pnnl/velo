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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.PropertiesActionGroup;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.FavoritesFolderWrapper;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.model.CmsPath;

/**
 * Based on CAT's TreeTableExplorerView, contains a TableExplorer 
 * that is updated with the current selection
 * 
 * @see TableExplorer 
 * @version $Revision: 1.0 $
 */
public class TableExplorerView extends AbstractExplorerView implements ICatExplorerView, ISelectionListener, IResourceEventListener {
  public final static String ID = TableExplorerView.class.getName();
  
  protected final static int[] CHILD_TYPES = { IResource.FOLDER };

  // Local Variables
  protected TableExplorer m_tableComp;

  @SuppressWarnings("unused")
  private Logger logger = CatLogger.getLogger(TableExplorerView.class);
  private IResource parentFolder = null;  // the folder this view is showing the contents of

  private PropertiesActionGroup propertiesActionGroup;

  public TableExplorerView() {
    super();
    setViewId(ID);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent) {
    
    Composite wrapper = new Composite(parent, SWT.NONE);
    final GridLayout gridLayout = new GridLayout(1, true);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    wrapper.setLayout(gridLayout);
    wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    m_tableComp = new MyTableExplorer(this);
    m_tableComp.createPartControl(wrapper, false);

    // Listen to any perspective changes.
    //this.getViewSite().getWorkbenchWindow().addPerspectiveListener(this);

    this.getViewSite().setSelectionProvider(new SelectionProvider());

    getViewSite().registerContextMenu(VIEW_CONTEXT_MENU_ID, m_tableComp.getContextMenuManager(), m_tableComp.getTableViewer());

    createActions();
    fillActionBars(getViewSite().getActionBars());
    
    // add myself as selection changed listener
    getSite().getPage().addSelectionListener(this);
    
    ResourcesPlugin.getResourceManager().addResourceEventListener(this);
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.AbstractExplorerView#getRoot()
   */
  @Override
  public Object getDefaultRoot() {
    return parentFolder;
  }

  /**
   * Method setSelection.
   * @param selection ISelection
   */
  public void setSelection(ISelection selection) {
    if (selection.isEmpty())
      return;
    if (!(selection instanceof IStructuredSelection))
      return;
    
    IStructuredSelection s = (IStructuredSelection) selection;
    Object object = s.getFirstElement();
    
    CmsPath path = null;
    
    if (object instanceof String) {
      String string = (String) object;
      path = new CmsPath( string );      
    }
    
    expandToPath(path);
    
    // SDH: Should try to force the selection to fire the apprpriate event.
//      selectionProvider.setSelection(new StructuredSelection( ResourcesPlugin.getResourceManager().getResource(path)));
  }

  private void createActions() {
    this.propertiesActionGroup = new PropertiesActionGroup(this, m_tableComp.getTableViewer());
  }

  /**
   * Method fillActionBars.
   * @param actionBars IActionBars
   */
  protected void fillActionBars(IActionBars actionBars) {
    this.propertiesActionGroup.fillActionBars(actionBars);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    // give focus to the table
    this.m_tableComp.getTableViewer().getControl().setFocus();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
   */
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
   */
  public void saveState(IMemento memento) {
    super.saveState(memento);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    super.dispose();
    m_tableComp.dispose();
    //this.getViewSite().getWorkbenchWindow().removePerspectiveListener(this);
    ResourcesPlugin.getResourceManager().removeResourceEventListener(this);
    getSite().getPage().removeSelectionListener(this);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#getTableExplorer()
   */
  @Override
  public TableExplorer getTableExplorer() {
    return m_tableComp;
  }  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#getTreeExplorer()
   */
  @Override
  public TreeExplorer getTreeExplorer() {
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceTable#getTableExplorer()
   */
  /**
   * Method getTableViewer.
   * @return TableViewer
   */
  @Override
  public TableViewer getTableViewer() {
    return m_tableComp.getTableViewer();
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceContainer#getTreeViewr()
   */
  /**
   * Method getTreeViewer.
   * @return TreeViewer
   */
  @Override
  public TreeViewer getTreeViewer() {
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.IResourceTable#isTableActive()
   */
  @Override
  public boolean isTableActive() {
    return true;
  }

  /**
   * @param m_tableComp the m_tableComp to set
   */
  public void setM_tableComp(TableExplorer m_tableComp) {
    this.m_tableComp = m_tableComp;
  }

  /**
   * Method setRoot.
   * @param objNewRoot Object
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#setRoot(Object)
   */
  @Override
  public void setRoot(Object objNewRoot) {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Child classes should override if they want to hide the new wizard from the 
   * popup context menu.  Some users find this confusing.
  
   * @return boolean
   */
  public boolean showNewWizardInPopupMenu() {
    return false;
  }

  /**
   * Method selectionChanged.
   * @param part IWorkbenchPart
   * @param selection ISelection
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
   */
  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    if(part == this) {
      // ignore if I provided the selection
      return;
    }
    
    // See if we care about the selection:
    if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
    
    } else {
      IStructuredSelection structuredSelection = (IStructuredSelection)selection;
      IResource resource = RCPUtil.getResource(structuredSelection.getFirstElement());
      
      // TODO: if the IFolder is decorated by a favorites wrapper, then the tree is not loading itself
      // properly (not sure why), so this is a hack to use the wrapped resource instead
      if(resource instanceof FavoritesFolderWrapper) {
        resource = ((FavoritesFolderWrapper)resource).getWrappedResource();
      }
      
      // Only change our selection if the selection is a folder and
      // if we aren't already viewing that same folder
      if(resource instanceof IFolder) {
        if(!resource.equals(parentFolder)) {
          parentFolder = resource;
          //Control control = m_tableComp.getTableViewer().getControl();
          m_tableComp.getTableViewer().setInput(resource);
          updatePathText();
        }
      }
    }

  }
  
  private void updatePathText() {
    if(parentFolder == null) {
      setContentDescription(""); 
    } else {
      setContentDescription(parentFolder.getPath().toDisplayString());
    }
  }
  
  /**
   * Method onEvent.
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  @Override
  public void onEvent(IBatchNotification events) {
    if(parentFolder == null) {
      return;
    }

    boolean needsRefresh = false;
    
    for (IResourceEvent event : events) {
      if(event.getPath().equals(parentFolder.getPath())) {
        needsRefresh = true;
        break;
      }
    }

    if (needsRefresh) {
      // refresh the view
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          // make sure our parent folder is still in the cache - if it isn't, this means
          // that the folder was deleted, so we need to clear the input
          if(ResourcesPlugin.getResourceManager().resourceCached(parentFolder.getPath()) == false) {
            m_tableComp.getTableViewer().setInput(null);
            updatePathText();
          }
          m_tableComp.getTableViewer().refresh();
        }
      });
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // refresh the view
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        m_tableComp.getTableViewer().refresh();
      }
    });    
  }


  /**
   */
  public class SelectionProvider implements ISelectionProvider {

    private final ListenerList selectionListeners = new ListenerList();
    
    public SelectionProvider() {
      m_tableComp.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
        
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
          ISelection selected = SelectionProvider.this.getSelection();
          fireSelectionChanged(selected);
        }
      });      
    }
    
    /**
     * Method fireSelectionChanged.
     * @param selection ISelection
     */
    private void fireSelectionChanged(ISelection selection) {
      if (selection != null && !selectionListeners.isEmpty()) {
        SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
        Object[] listeners = selectionListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
          ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
          listener.selectionChanged(event);
        }
      }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
      selectionListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
      selectionListeners.remove(listener);
    }


    /**
     * Method getSelection.
     * @return ISelection
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    @Override
    public ISelection getSelection() {
      
      ISelection selected = m_tableComp.getTableViewer().getSelection();

      if (selected == null || selected.isEmpty()) {
        // get the parentFolder to set as the selection
        if(parentFolder == null) {
          selected = StructuredSelection.EMPTY;
        } else {
          IResource[] elements = {parentFolder};
          selected = new StructuredSelection(elements);
        }
      }
      return selected;
    }


    /**
     * Method setSelection.
     * @param selection ISelection
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(ISelection)
     */
    @Override
    public void setSelection(ISelection selection) {
      // TODO Auto-generated method stub
      
    }

    
  }
  
  /**
   */
  public class MyTableExplorer extends TableExplorer {
        
    /**
     * Constructor for MyTableExplorer.
     * @param catParentView ICatView
     */
    public MyTableExplorer(ICatExplorerView catParentView) {
      super(catParentView);
    }

    /**
     * Method defaultDoubleClickFolder.
     * @param folder IResource
     */
    @Override   
    protected void defaultDoubleClickFolder(IResource folder) {
      RCPUtil.selectResourceInTree(folder);
    }
  }
}


