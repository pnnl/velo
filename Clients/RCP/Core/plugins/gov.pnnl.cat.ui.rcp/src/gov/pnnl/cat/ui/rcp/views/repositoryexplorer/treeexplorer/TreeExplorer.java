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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.part.IPageSite;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.search.advanced.query.AdvancedSearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.IResourceSelection;
import gov.pnnl.cat.ui.rcp.IResourceSelectionListener;
import gov.pnnl.cat.ui.rcp.ISystemUpdateListener;
import gov.pnnl.cat.ui.rcp.ITableSelectedFolderListener;
import gov.pnnl.cat.ui.rcp.ResourceStructuredSelection;
import gov.pnnl.cat.ui.rcp.SystemManager;
import gov.pnnl.cat.ui.rcp.actions.CollapseAllAction;
import gov.pnnl.cat.ui.rcp.actions.ExplorerActions;
import gov.pnnl.cat.ui.rcp.actions.OpenFileInSystemEditorAction;
import gov.pnnl.cat.ui.rcp.actions.RefreshFolderAction;
import gov.pnnl.cat.ui.rcp.contextmenus.VeloResourceContextMenu;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.viewers.ResourceTreeViewer;
import gov.pnnl.cat.ui.rcp.views.adapters.CatBaseWorkbenchContentProvider;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.ISmartFolder;
import gov.pnnl.cat.ui.rcp.views.dnd.DNDSupport;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.AbstractNodeEventGenerator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.CatViewerContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.TreeExplorerView;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class TreeExplorer extends AbstractNodeEventGenerator implements ISystemUpdateListener, ITableSelectedFolderListener,
  IResourceEventListener, CatViewerContainer {

  protected static final String TAXONOMIES_ROOT = "TAXONOMIES_ROOT";

  protected static final String PROJECTS_ROOT = "PROJECTS_ROOT";

  protected static final String MEMENTO_ROOT_PATH = "root_path";

  protected static final String MEMENTO_EXPANDED_NODE = "expanded_node";

  protected static final String MEMENTO_TREE_PATH = "tree_path";

  protected static final String MEMENTO_USER_ID = "user_id";

  protected static final String MEMENTO_SELECTION = "selection";

  protected static final String FAVORITES_ROOT = "FAVORITES_ROOT";

  public static final String PERSONAL_ROOT = "PERSONAL_ROOT";
  
  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.customDoubleClickBehavior";
  protected static final String ATTRIBUTE = "class";
  protected static List<CustomDoubleClickBehavior> customDoubleClickBehaviors;
  static {
    loadCustomBehaviors();
  }

  protected ResourceTreeViewer treeViewer;

  protected ICatExplorerView catParentView;

  protected Composite mainComp;

  protected IResourceManager mgr;

  protected Tree tree;

  protected MenuManager popupMenuManager;

  protected Object root;

  protected IMemento memento;

  protected boolean singleSelection = false;

  protected boolean changeTitle = false;

  protected EventListenerList evFileExplorerListenerList = new EventListenerList();

  protected Vector<ViewerFilter> filterVector = new Vector<ViewerFilter>();

  protected IViewSite viewSite;

  protected Logger logger = CatLogger.getLogger(TreeExplorer.class);

  protected IPageSite pageSite;

  protected CollapseAllAction collapseAllAction;

  protected ArrayList<CmsPath> rootChildrenPaths = new ArrayList<CmsPath>();

  protected boolean showRoot = true; // true by default
  
  protected boolean showFiles;
  
  /**
   * Constructor for TreeExplorer.
   * @param showFiles boolean
   * @param catParentView ICatView
   */
  public TreeExplorer(boolean showFiles, ICatExplorerView catParentView) {
    this(showFiles, catParentView, false);
  }

  /**
   * Constructor for TreeExplorer.
   * @param showFiles boolean
   * @param catParentView ICatView
   * @param singleSelection boolean
   */
  public TreeExplorer(boolean showFiles, ICatExplorerView catParentView, boolean singleSelection) {
    this.showFiles = showFiles;
    this.catParentView = catParentView;
    this.singleSelection = singleSelection;

    SystemManager.getInstance().addDropListener(this);
    try {
      this.mgr = ResourcesPlugin.getResourceManager();
    } catch (Exception e) {
      logger.error(e);
    }
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.events.CacheListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // refresh myself
    refresh();
  }
    
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(gov.pnnl.cat.core.resources.events.IBatchNotification)
   */
  @Override
  public void onEvent(IBatchNotification events) {
    // if a new team or user home folder was added, refresh the root
    final Boolean[] homeFolderEvent = new Boolean[]{false};
    Iterator<IResourceEvent> it = events.getNonRedundantEvents();
    IResourceEvent event;
    String userDocuments = ResourcesPlugin.getResourceManager().getUserDocumentsPath().getName();
    String teamDocuments = ResourcesPlugin.getResourceManager().getTeamDocumentsPath().getName();

    while(it.hasNext()) {
      event = it.next();
      String lastSegmentName = event.getPath().getSegments().getLast().getName();
      
      if (lastSegmentName.equals(userDocuments) || lastSegmentName.equals(teamDocuments)) {
        homeFolderEvent[0] = true;
        break;
      } 
    }
    // most of the refresh events come through SystemManager, so we only care about home folder events
    if(homeFolderEvent[0] == true) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
          reload();
        }
      });
    }
  }
  
  /**
   * Reload the tree from defaults
   */
  public void reload() {
    catParentView.setRoot(catParentView.getDefaultRoot());
    refresh(); 
  }

  /**
   * Method setVisible.
   * @param visible boolean
   */
  public void setVisible(boolean visible) {
    mainComp.setVisible(visible);
  }

  /**
   * Create the composite
   * 
   * @param parent
   */
  public void createPartControl(Composite parent) {
    createPartControl(parent, true, true);
  }
  
  /**
   * @param parent
   * @param showNewWizardInPopupMenu - Should this tree explorer show the new wizard in the popup menu?
   * Some users find the new wizard behavior confusing...
   * @param showFiles boolean
   */
  public void createPartControl(Composite parent, boolean showNewWizardInPopupMenu, boolean showFiles) {
    mainComp = new Composite(parent, 0);
    this.showFiles = showFiles;

    final GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    mainComp.setLayout(gridLayout);

    SashForm sash = new SashForm(mainComp, SWT.NONE);
    GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1);
    sash.setLayoutData(gd);

    int iSelectionMode;

    if (singleSelection) {
      iSelectionMode = SWT.SINGLE;
    } else {
      iSelectionMode = SWT.MULTI;
    }

    treeViewer = new ResourceTreeViewer(showFiles, sash, iSelectionMode);
//    treeViewer.setLabelProvider(new CatWorkbenchLabelProvider(treeViewer));
    
    // add a sorter that only applies to IResource objects
    treeViewer.setSorter(new FileFolderSorter());

    // add the appropriate filters
    if (!showFiles) {
      filterVector.add(new FolderFilter());
    }    
    for (Iterator<ViewerFilter> iter = filterVector.iterator(); iter.hasNext();) {
      ViewerFilter filter = (ViewerFilter) iter.next();
      treeViewer.addFilter(filter);
    }

    tree = treeViewer.getTree();

    hookToolbarActions();
    createContextMenu(showNewWizardInPopupMenu);
    hookDragAndDrop();
    hookDoubleClickListener();
    hookSingleClickListener();
    
    // JSD Added as a demonstration and placeholder for the context
    // senisitive help.
    IWorkbenchHelpSystem iwhs = PlatformUI.getWorkbench().getHelpSystem();
    iwhs.setHelp(tree, catParentView.getViewId());

    // listen for asynchronous change events
    ResourcesPlugin.getResourceManager().addResourceEventListener(this);

  }
  
  protected void hookSingleClickListener() {
    // publish events when resources in the tree are selected
    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent e) {
        IStructuredSelection selection = (IStructuredSelection) e.getSelection();
        
        // return if nothing is selected
        if(selection == null || selection.isEmpty()) {
          return;
          
        } else if(selection.getFirstElement() instanceof ISmartFolder) { // If this is a smart folder, we do a search
          try {
            executeSearch(((ISmartFolder)selection.getFirstElement()).getQuery());
          } catch (Exception e1) {
           ToolErrorHandler.handleError("Error executing search" , e1, true);
          }
          
        } else if (selection.getFirstElement() instanceof IResource) { // if this is a resource, fire a normal selection event (this includes favorites)
          // if this is a resource, fire a normal selection event (this includes favorites)
          IResourceSelection resourceSelection = new ResourceStructuredSelection(selection);
          fireSelectionChanged(resourceSelection);
          
          // if this is a category, we do a search
          if(((IResource)selection.getFirstElement()).getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
            executeCategorySearch((IResource)selection.getFirstElement());           
          
          } else {
            // Make sure the folders view comes to the forefront.
            // We need to run this in separate thread to make sure that the fireeSelectionChanged
            // event completes first.
            Display.getDefault().asyncExec(new Runnable() {

              @Override
              public void run() {
                try {
                  RCPUtil.activateFoldersView();
                } catch (Throwable e1) {
                  logger.error("Failed to activate folders view.", e1);
                }
              }
            });
          }
        }
      }
    });
     
  }
  
  /**
   * TODO: move to a CategoryService class
   * @param selectedCategory
   */
  protected void executeCategorySearch(final IResource selectedCategory) {
    AdvancedSearchQuery query = new AdvancedSearchQuery() {

      @Override
      public String getSearchString() {
       
        // first we must parse off the category root from the path (i.e., cm:categoryRoot)
        CmsPath path = selectedCategory.getPath();
        path = path.subPath(1, path.size()-1);
        
        // We have to iso9075 encode every segment
        String query = "PATH:\"" + path.toPrefixString(true) + "/member\"";
        
        return query;
      }
      
    };
    NewSearchUI.activateSearchResultView();
    NewSearchUI.runQueryInBackground(query); 
  }
  
  protected void executeSearch(final String queryString) {
    AdvancedSearchQuery query = new AdvancedSearchQuery() {

      @Override
      public String getSearchString() {
        return queryString;
      }      
    };
    NewSearchUI.activateSearchResultView();
    NewSearchUI.runQueryInBackground(query); 
  }
  
  protected void hookDoubleClickListener() {
    treeViewer.addDoubleClickListener(new IDoubleClickListener() {
      
      OpenFileInSystemEditorAction fopenFileAction = ExplorerActions.openFileInSystemEditorAction(treeViewer, false);

      // TODO: this needs to be global so all trees have access to it
      public void doubleClick(DoubleClickEvent e) {
        StructuredSelection selectedFile = (StructuredSelection) treeViewer.getSelection();
        
        if (selectedFile != null && !selectedFile.isEmpty() && selectedFile.getFirstElement() instanceof IResource) {
          IResource selectedResource = (IResource) selectedFile.getFirstElement();
          
          // Check for custom Behavior
          boolean doubleClicked = false;
          
          for(CustomDoubleClickBehavior behavior : customDoubleClickBehaviors) {
            doubleClicked = behavior.doubleClick(selectedResource);
            if(doubleClicked) {
              break;
            } 
          }
          
          if(!doubleClicked) {
            if (selectedResource instanceof IFile) { // Double click on file
              // Run application on file.
              fopenFileAction.run();
            } else if (selectedResource instanceof IFolder) { // Double click on folder
              
              if(selectedResource.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
                // Call the browse remote resource action
                try {
                  RSEUtils.openInRemoteSystemsExplorer(selectedResource);
                } catch (Exception ex) {
                  ToolErrorHandler.handleError("Failed to open Remote Systems Explorer", ex, true);
                } 
              } else {
                // Expand file in tree.
                if (treeViewer.getExpandedState(selectedFile.getFirstElement())) {
                  treeViewer.collapseToLevel(selectedFile.getFirstElement(), 1);
                } else {
                  treeViewer.expandToLevel(selectedFile.getFirstElement(), 1);
                }
              }
         
            }
          }
        } else if (selectedFile != null && !selectedFile.isEmpty() && selectedFile.getFirstElement() instanceof GenericContainer) {
          // Expand file in tree.
          if (treeViewer.getExpandedState(selectedFile.getFirstElement())) {
            treeViewer.collapseToLevel(selectedFile.getFirstElement(), 1);
          } else {
            treeViewer.expandToLevel(selectedFile.getFirstElement(), 1);
          }
        }
      }
    });
  }
  
  /**
   * Method setShowRoot.
   * @param showRoot boolean
   */
  // TODO: remove this option since it only works if your root object is a resource, not a resource container root
  public void setShowRoot(boolean showRoot) {
    this.showRoot = showRoot;
  }

  /**
   * sets the root of the tree for starting the display
   * 
   * @param newRoot
   */
  public void setRoot(Object newRoot) {
    setRoot(newRoot, true);
  }
  
  /**
   * Method setRoot.
   * @param newRoot Object
   * @param refreshRootChildrenPaths boolean
   */
  public void setRoot(Object newRoot, boolean refreshRootChildrenPaths) {
    Object selRoot = newRoot;
    this.root = newRoot;
    treeViewer.setInput(newRoot);
    treeViewer.getInput();

    if (newRoot instanceof IResource && showRoot) {
      RepositoryContainer superRoot = new RepositoryContainer(null, (IResource) newRoot);
      newRoot = superRoot;   
    } 

    if(newRoot instanceof GenericContainer && refreshRootChildrenPaths) {
      GenericContainer container = (GenericContainer)newRoot;
      IResource[] children;
      
      if(newRoot instanceof RepositoryContainer) {
        children = (IResource[])container.getChildren();
        
      } else {
        RepositoryContainer repo = RCPUtil.findRepositoryContainer((GenericContainer)newRoot);   
        children = (IResource[])repo.getChildren();
      }
      for (IResource child : children) {
          rootChildrenPaths.add((CmsPath) ((IResource) child).getPath());
      }
      Collections.sort(rootChildrenPaths);
    }

    // If we are showing the root and the root is an IResource, then select it in the tree
    if (showRoot && selRoot instanceof IResource) {
      IResource selResource = (IResource) selRoot;
      treeViewer.setSelection(new StructuredSelection(selResource));
    }

  }

  public void setFocus() {
    treeViewer.getControl().setFocus();
  }
  
  /**
   * Method getRoot.
   * @return Object
   */
  public Object getRoot() {
    return root;
  }

  /**
   * Add actions to the toolbar
   */
  private void hookToolbarActions() {
    IActionBars bars = catParentView.getActionBars();
    IToolBarManager toolBarManager = bars.getToolBarManager();

    // Refresh
    // We only want to add this if the parent view is an instance of TreeExplorerView
    if(catParentView instanceof TreeExplorerView) {
      RefreshFolderAction frefreshViewAction = ExplorerActions.refreshFolder(treeViewer);
      toolBarManager.add(frefreshViewAction);
    }

    // Collapse All
    collapseAllAction = ExplorerActions.collapseAll(treeViewer);
    toolBarManager.add(collapseAllAction);
  }

  private void createContextMenu(boolean showNewWizardInPopupMenu) {
    VeloResourceContextMenu fileFolderContextMenu = new VeloResourceContextMenu(catParentView, treeViewer, showNewWizardInPopupMenu);
    fileFolderContextMenu.listenToViewer(treeViewer);

    // context menu
    this.popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$

    this.popupMenuManager.addMenuListener(fileFolderContextMenu);
    this.popupMenuManager.setRemoveAllWhenShown(true);
    
    // notice that we cannot register the context menu here
    // because we are using a non-standard selection provider.
    // catParentView.getViewSite().registerContextMenu(popupMenuManager,
    // catParentView.getViewSite().getSelectionProvider());
    Menu menu = this.popupMenuManager.createContextMenu(tree);
    tree.setMenu(menu);

  }
  
  /**
   * This code was originally in RefreshFolderAction, which would clear the cache then refresh
   * one view only.  We need to refresh all tree only views if the cache is cleared, so i changed this
   * to be driven by cache cleared event.  So now RefreshFolderAction only clears the cache, and then
   * the viewer refresh is triggered by event.
   */
  public void refresh() {

    try {
      final ContentViewer view = treeViewer;
      final ISelection select = view.getSelection();
      IResource lastSelectedResource = null;

      // Get the resource that was currently selected
      if(select != null && select instanceof TreeSelection) {
        if( ((TreeSelection)select).getFirstElement() instanceof IResource ) {
          lastSelectedResource = (IResource)((TreeSelection)select).getFirstElement();
        }
      }

      view.refresh();
      final CatBaseWorkbenchContentProvider contentProvider = (CatBaseWorkbenchContentProvider)view.getContentProvider();
      
      // Now re-load the items into the cache
      if(lastSelectedResource != null) {
        final Stack<IResource>stack = new Stack<IResource>();          
        
        //final CmsPath listenToParentPath = lastSelectedResource.getParent().getPath();
        
        // We want to be alerted when tree is loaded so we can set the selection
        try {
          // expand all the way down the tree
          IResource resource = lastSelectedResource;
          while(resource != null) {
            stack.push(resource);
            resource = resource.getParent();
          }

          while(!stack.isEmpty()) {
            resource = stack.pop();
            resource = ResourcesPlugin.getResourceManager().getResource(resource.getPath());
            contentProvider.getChildren(resource);
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
        
      } else {
          view.setSelection(select, true);
      }
      
    } catch (Exception ex) {
      logger.error(ex);
    }
  
  }

  /**
   * Method getContextMenuManager.
   * @return MenuManager
   */
  public MenuManager getContextMenuManager() {
    return this.popupMenuManager;
  }

  // private Action openWithAppAction(final String strExt, String label) {
  // Action action = new Action(label) { //$NON-NLS-1$
  // public void run() {
  // try {
  //          
  // // find out what was selected to put in new view part
  // StructuredSelection selectedFile = (StructuredSelection)
  // treeViewer.getSelection();
  // IFile theFile = (IFile) selectedFile.getFirstElement();
  // System.out.println("File: "+theFile.getURL().toExternalForm());
  // // String strExt = ".doc";
  // launchProgram(theFile, strExt);
  // } catch (Exception ex) {
  // ex.printStackTrace();
  // }
  // }
  //
  // private void launchProgram(IFile theFile, String strExt) throws
  // ResourceException {
  // Program prog = Program.findProgram(strExt);
  // if (prog != null) {
  // System.out.println("Program Found: "+prog.getName());
  // prog.execute(theFile.getURL().toExternalForm());
  // } else {
  // System.out.println("program not found.");
  // }
  // }
  // };
  // //get the image of the program
  // Program prog = Program.findProgram(strExt);
  // if(prog.getImageData() != null){
  // action.setImageDescriptor(ImageDescriptor.createFromImageData(prog.getImageData()));
  // }else{
  // action.setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_WINDOW_16));
  // }
  // action.setToolTipText("Open File in " + label);
  // return action;
  // }

  private void hookDragAndDrop() {
    DNDSupport.addDragSupport(treeViewer);
    DNDSupport.addDropSupport(this);
  }

  /**
   * Based on the Viewpart this explorer is a part of
   * 
   * @param site
   * @param memento
  
   * @throws PartInitException */
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    this.memento = memento;
    this.changeTitle = false;
    this.viewSite = site;

    // if there is a secondary id.. the user has opened a tab then when we want
    // to check for the secondary ID.
    if (this.viewSite.getId().equalsIgnoreCase(CatViewIDs.PERSONAL_LIBRARY_VIEW) || 
        this.viewSite.getId().equalsIgnoreCase(CatViewIDs.FAVORITES_VIEW) ||
        this.viewSite.getSecondaryId() != null) {
      this.changeTitle = true;      
    }

    catParentView.setViewId(this.viewSite.getId());

  }

  /**
   * Save the current state of the perspective.
   * 
   * @param memento
   */
  public void saveState(IMemento memento) {
    
    // save path to last selected resource
    IMemento userNode = memento.createChild(MEMENTO_USER_ID, ResourcesPlugin.getSecurityManager().getUsername());

    StructuredSelection selection = (StructuredSelection) this.treeViewer.getSelection();
    if (!selection.isEmpty()) {
      // Only save selected nodes if they are an IResource
      if(selection.getFirstElement() instanceof IResource) {
        userNode.createChild(MEMENTO_SELECTION, ((IResource) selection.getFirstElement()).getPath().toString());
      }
    }

    Object[] expandedNodes = treeViewer.getVisibleExpandedElements();
    for (int i = 0; i < expandedNodes.length; i++) {
      Object element = expandedNodes[i];
      if(element instanceof IResource) {
        userNode.createChild(MEMENTO_EXPANDED_NODE, ((IResource) element).getPath().toFullyQualifiedString());
      }
    }

  }

  /**
   * Called from the createPartControl
   */
  public void restoreState() {
    IMemento userNode = null;
    if (memento != null) {
      IMemento[] userIDs = memento.getChildren(MEMENTO_USER_ID);
      String username = ResourcesPlugin.getSecurityManager().getUsername();
      for (int i = 0; i < userIDs.length; i++) {
        if (userIDs[i].getID().equals(username)) {
          userNode = userIDs[i];
          break;
        }
      }
    }

    if (userNode != null) {
      // now expand all the previously expanded elements
      IMemento[] expandedNodes = userNode.getChildren(MEMENTO_EXPANDED_NODE);
      CmsPath expandedPaths[] = new CmsPath[expandedNodes.length];
      for (int i = 0; i < expandedNodes.length; i++) {
        String nodeIResourcePathString = expandedNodes[i].getID();
        expandedPaths[i] = new CmsPath(nodeIResourcePathString);
      }

      IMemento selectionNode = userNode.getChild(MEMENTO_SELECTION);
      CmsPath selectedPath = null;

      if (selectionNode != null) {
        String pathToSelect = selectionNode.getID();

        if (pathToSelect != null) {
          selectedPath = new CmsPath(pathToSelect);
        }
      }

      for(CmsPath path : expandedPaths) {
        IResource resource = mgr.getResource(path);
        // Do not try to restore category paths for now, since we don't have them wrapped in a decorator
        // that can point to the parent GenericContainer
        if(resource != null && !resource.getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
          treeViewer.expandToPath(path);
        }
      }
      
      if(selectedPath != null) {
        CmsPath path = new CmsPath(selectedPath);
        IResource resource = mgr.getResource(path);
        // Do not try to restore category paths for now, since we don't have them wrapped in a decorator
        // that can point to the parent GenericContainer
        if(resource != null && !resource.getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
          StructuredSelection sel = new StructuredSelection(resource);
          treeViewer.expandToPath(path);
          treeViewer.setSelection(sel);
        }
      }      
    }

  }

  public void dispose() {
    SystemManager.getInstance().removeDropListener(this);
    mainComp.dispose();
    ResourcesPlugin.getResourceManager().removeResourceEventListener(this);
  }

  /**
   * Add Listener<br>
   * Add a listener to the the event list.
   * 
   * @param listener
   *          the listener to add to the list
   */
  public void addFileExplorerChangeListener(IResourceSelectionListener listener) {
    synchronized (evFileExplorerListenerList) {
      evFileExplorerListenerList.add(IResourceSelectionListener.class, listener);
    }
  }

  /**
   * Remove Listener<br>
   * Remove a listener from the the event list.
   * 
   * @param listener
   *          the listener to remove from the list
   */
  public void removeRolesChangeListener(IResourceSelectionListener listener) {
    synchronized (evFileExplorerListenerList) {
      evFileExplorerListenerList.remove(IResourceSelectionListener.class, listener);
    }
  }

  /**
   * @param currentSelection
   */
  protected void fireSelectionChanged(IResourceSelection currentSelection) {
    Object[] listeners = evFileExplorerListenerList.getListenerList();
    // Each listener occupies two elements - the first is the listener class
    // and the second is the listener instance
    for (int i = 0; i < listeners.length; i += 2) {
      if (listeners[i] == IResourceSelectionListener.class) {
        ((IResourceSelectionListener) listeners[i + 1]).resourceSelectionChanged(currentSelection);
      }
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.ui.rcp.ISystemUpdateListener#folderUpdated(gov.pnnl.cat.core.resources.IFolder)
   */
  /**
   * Method refreshResource.
   * @param path CmsPath
   * @param selectResource boolean
   * @see gov.pnnl.cat.ui.rcp.ISystemUpdateListener#refreshResource(CmsPath, boolean)
   */
  public void refreshResource(final CmsPath path, final boolean selectResource) {
    logger.debug("Folder Updated: " + path);

    // Very weird - sometimes this is getting called and the input is null - possible after widget
    // has been disposed?
    // Until we figure out what's going on, just return if there is no input to refresh.
    if(treeViewer.getInput() == null) {
      return;
    }   
    
    IResource theFolder = null;
    try {
      if (!mgr.resourceCached(path)) {
        // This path no longer exists, so the folder cannot be updated.
        return;
      }
      IResource resource = mgr.getResource(path);
      if(resource instanceof IFolder) {
        theFolder = resource;
      } else {
        theFolder = resource.getParent();
      }
      
      // refresh and expand the folder
      treeViewer.expandToPath(theFolder.getPath());
      treeViewer.update(theFolder, null);
      treeViewer.refresh(theFolder, true);
      
    } catch (ResourceException e) {
      logger.error("Unable to get resource at " + path, e);
    }    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.ui.rcp.ITableSelectedFolderListener#selectedFolderUpdated(gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.CatItemNode)
   */
  /**
   * Method selectedFolderUpdated.
   * @param folder IResource
   * @see gov.pnnl.cat.ui.rcp.ITableSelectedFolderListener#selectedFolderUpdated(IResource)
   */
  @Override
  public void selectedFolderUpdated(IResource folder) {

    // expand to the folder's parent
    treeViewer.expandToPath(folder.getPath());
    
    // now select it
    StructuredSelection sel = new StructuredSelection(folder);
    treeViewer.setSelection(sel);
  }

  /**
  
   * @return the customDoubleClickBehaviors */
  public static List<CustomDoubleClickBehavior> getCustomDoubleClickBehaviors() {
    return customDoubleClickBehaviors;
  }

  /**
   * Used to get the control for the treeViewer.
   * 
  
   * @return Control * @see gov.pnnl.cat.ui.rcp.views.ICatExplorerInput#getCatControl()
   */
  public Control getCatControl() {
    return this.treeViewer.getControl();
  }

  /**
   * Method getTreeViewer.
   * @return ResourceTreeViewer
   */
  public ResourceTreeViewer getTreeViewer() {
    return treeViewer;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.CatViewerContainer#getViewer()
   */
  @Override
  public StructuredViewer getViewer() {
    return treeViewer;
  }

  /**
   * Method getCollapseAllAction.
   * @return CollapseAllAction
   */
  public CollapseAllAction getCollapseAllAction() {
    return this.collapseAllAction;
  }

  private static void loadCustomBehaviors() {
    customDoubleClickBehaviors = new ArrayList<CustomDoubleClickBehavior>();
    
    try {
      // look up all the extensions for the LaunchSimulation extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof CustomDoubleClickBehavior) {
          CustomDoubleClickBehavior behavior = (CustomDoubleClickBehavior)obj;
          customDoubleClickBehaviors.add(behavior);
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom double click behavior extension points.", e);
    }
  }

}
