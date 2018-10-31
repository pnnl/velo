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
package gov.pnnl.cat.ui.rcp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.preferences.CatPreferenceIDs;
import gov.pnnl.cat.ui.preferences.PreferenceConstants;
import gov.pnnl.cat.ui.rcp.ResourceStructuredSelection;
import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.cat.ui.utils.PerspectiveOpener;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

/**
 */
public class RCPUtil {

  private static Logger logger = CatLogger.getLogger(RCPUtil.class);
  private static boolean showHiddenFiles;
  private static TreeRootProvider treeRootProvider;
  private static DocumentLibraryPerspectiveProvider docLibPerspectiveProvider;


  static {
    @SuppressWarnings("deprecation")
    ScopedPreferenceStore preferences =  new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    showHiddenFiles = preferences.getBoolean(PreferenceConstants.SHOW_HIDDEN_FILES);
    // register listener for when preference value is changed
    preferences.addPropertyChangeListener(new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty() == PreferenceConstants.SHOW_HIDDEN_FILES) {
          showHiddenFiles = Boolean.valueOf(event.getNewValue().toString());     
          // refresh the cache
          ResourcesPlugin.getResourceManager().clearCache();
        }
      }
    }); 
  }
  
  /**
   * Return the Eclipse workspace's data folder 
   * (e.g., $USER_HOME/.akuna)
   * @return
   */
  public static File getWorkspaceFolder() {
    // return the workspace data folder  
    try {
      return new File(Platform.getInstanceLocation().getURL().getFile());
    } catch (Exception e) {
      throw new RuntimeException("Failed to get workspace data folder.", e);
    }
  }

  public static void invokeCommand(final IResource resource, final IHandler handler, final String cmdId) throws Exception {
    
    ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
    IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);

    // Force the handler to be active because it deactivates when we are in a swing window :(
    IHandlerActivation activation = null;
    if(handler != null) {
      activation = handlerService.activateHandler(cmdId, handler);
    }
    Command cmd = cmdService.getCommand(cmdId);

    Map<String, String> params = new HashMap<String, String>();

    // get the application context
    IEvaluationContext appContext = new EvaluationContext(handlerService.getCurrentState(), Collections.EMPTY_LIST);

    // set up the appContext as we would want it.
    if(resource != null) {
      IStructuredSelection selection = new ResourceStructuredSelection(resource);
      appContext.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
    }

    ExecutionEvent event = new ExecutionEvent(cmd, params, null, appContext);   
    cmd.executeWithChecks(event);

    // Now we have to force the handler to deactivate or it will always be enabled in the Eclipse window
    if(activation != null) {
      handlerService.deactivateHandler(activation);
    }

  }
  
  // Used by all file dialogs in the RCP to get a consistent default browse root
  /**
  * Method getVeloTreeRoot.
  * @return GenericContainer
  */
  public static RepositoryContainer getTreeRoot() {
    return treeRootProvider.getVeloTreeRoot();
  }

  public static void setTreeRootProvider(TreeRootProvider treeRootProvider) {
    RCPUtil.treeRootProvider = treeRootProvider;
  }
  
  /**
   * Find the nested Repository element from an arbitrary container structure.
   * Only resources in the Repsitory element can be navigated to.
   * @param root
   * @return
   */
  public static RepositoryContainer findRepositoryContainer(GenericContainer root) {
    RepositoryContainer repo = null;
    
    if(root instanceof RepositoryContainer) {
      return (RepositoryContainer)root;
    }
    
    for(Object child : root.getChildren()) {
      if(child instanceof RepositoryContainer) {
        repo = (RepositoryContainer)child;
        break;
     
      } else if (child instanceof GenericContainer) {
        repo = findRepositoryContainer((GenericContainer)child);
        if(repo != null) {
          break;
        }
      }
    }
    return repo;
  }
  

  public static DocumentLibraryPerspectiveProvider getDocLibPerspectiveProvider() {
    return docLibPerspectiveProvider;
  }

  public static void setDocLibPerspectiveProvider(DocumentLibraryPerspectiveProvider docLibPerspectiveProvider) {
    RCPUtil.docLibPerspectiveProvider = docLibPerspectiveProvider;
  }
  

  /**
   * If it is possible to adapt the given object to the given type, this
   * returns the adapter. Performs the following checks:
   *
   * <ol>
   * <li>Returns <code>sourceObject</code> if it is an instance of the
   * adapter type.</li>
   * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
   * <li>If sourceObject is not an instance of PlatformObject (which would have
   * already done so), the adapter manager is queried for adapters</li>
   * </ol>
   *
   * Otherwise returns null.
   *
   * @param sourceObject
   *            object to adapt, or null
   * @param adapter
   *            type to adapt to
   * @param activatePlugins
   *            true if IAdapterManager.loadAdapter should be used (may trigger plugin activation)
   * @return a representation of sourceObject that is assignable to the
   *         adapter type, or null if no such representation exists
   */
  public static <T> T getAdapter(Object sourceObject, Class<T> adapter, boolean activatePlugins) {
    Assert.isNotNull(adapter);
    if (sourceObject == null) {
      return null;
    }
    if (adapter.isInstance(sourceObject)) {
      return adapter.cast(sourceObject);
    }

    if (sourceObject instanceof IAdaptable) {
      IAdaptable adaptable = (IAdaptable) sourceObject;

      T result = adaptable.getAdapter(adapter);
      if (result != null) {
        // Sanity-check
        Assert.isTrue(adapter.isInstance(result));
        return result;
      }
    }

    if (!(sourceObject instanceof PlatformObject)) {
      T result;
      if (activatePlugins) {
        result = adapter.cast(Platform.getAdapterManager().loadAdapter(sourceObject, adapter.getName()));
      } else {
        result = Platform.getAdapterManager().getAdapter(sourceObject, adapter);
      }
      if (result != null) {
        return result;
      }
    }

    return null;
  }
  

  /**
   * Method getCatAdapter.
   * @param element Object
   * @return ICatWorkbenchAdapter
   */
  public static ICatWorkbenchAdapter getCatAdapter(Object element) {
    return getAdapter(element, ICatWorkbenchAdapter.class, false);
  }


  /**
   * A utility method for getting an IResource from a selection.
   * @param object Object
   * @return IResource
   */
  public static IResource getResource(Object object) {
    if (object == null) {
      return null;
    }

    IResource resource;

    if (object instanceof IResource) {
      resource = (IResource) object;
    } else {
      // throw new RuntimeException("Cannot convert element \"" + object + "\" of type " + object.getClass().getName() + " to an IResource");
      resource = (IResource) getAdapter(object, IResource.class, true);
    }

    return resource;
  }

  /**
   * Method getFileExtension.
   * @param resource IResource
   * @return String
   */
  public static String getFileExtension(IResource resource) {
    String extension = "";

    try {
      if (resource instanceof ILinkedResource) {
        resource = ((ILinkedResource) resource).getTarget();
      }

      if (resource instanceof IFile) {
        int iExtPos = resource.getName().lastIndexOf('.');

        if (iExtPos != -1) {
          extension = resource.getName().substring(iExtPos);
        }
      }

    } catch (Throwable e) {
      String errMsg = "Unable to resolve link target.";
      ToolErrorHandler.handleError(errMsg, e, true);

    }
    return extension;
  }

  /**
   * Method handleEmptySelectionForTable.
   * @param selection ISelection
   * @param selectionProvider ISelectionProvider
   * @return ISelection
   */
  public static ISelection handleEmptySelectionForTable(ISelection selection, ISelectionProvider selectionProvider) {
    ISelection selected = selection;

    if (selected == null || selected.isEmpty()) {
      // if its the tableviewer, get the root to set as the selection
      if (selectionProvider instanceof TableViewer) {
        Object input = ((TableViewer) selectionProvider).getInput();
        if (input != null) {
          selected = new StructuredSelection(input);
        } // May need to do something with the selection if the input is null.
      }
    }

    return selected;
  }

  /**
   * Converts a structured selection into a set of IResource objects.
   * 
   * @param selection


   * @return List<IResource>
   * @throws ResourceException */
  public static List<IResource> getResources(IStructuredSelection selection) throws ResourceException {

    List<IResource> resVect = new ArrayList<IResource>();

    if (selection != null && !selection.isEmpty()) {

      IResource resource;
      Iterator<?> iter = selection.iterator();

      while (iter.hasNext()) {
        resource = RCPUtil.getResource(iter.next());
        if(resource != null) {
          resVect.add(resource);
        }
      }
    }
    return resVect;

  }

  /**
   * Method getCurrentStructuredSelection.
   * @param selection ISelection
   * @param part IWorkbenchPart
   * @return IStructuredSelection
   */
  public static IStructuredSelection getCurrentStructuredSelection(ISelection selection, IWorkbenchPart part) {
    ISelection currentSelection = selection;
    if (currentSelection != null) {
      if (currentSelection instanceof IStructuredSelection) {
        return (IStructuredSelection) currentSelection;
      }
    }
    return new StructuredSelection();
  }
  
  /**
   * Method isHiddenFile.
   * @param child Object
   * @return boolean
   */
  public static boolean isHiddenFile(Object child) {

    // If preference is set to hide hidden files, then omit any files that start with "."
    if(child != null && child instanceof IResource) {
      IResource resource = (IResource)child;
      if(showHiddenFiles == true) {
        return false;
      } else if(resource.getName().startsWith(".")) {
        logger.debug("skipping hidden file: " + resource.getName());
        return true;
      }
    }
    return false;
  }

  /**
   * Used by any content providers to filter hidden files (files that start with ".") if
   * the hide hidden files preference is set (true by default).
   * @param children

   * @return Object[]
   */
  public static Object[] filterHiddenFiles(Object[] children) {
    if(children == null) {
      return null;
    }

    List<Object> filteredChildren = new ArrayList<Object>();
    for(Object child : children) {
      if(isHiddenFile(child) == false) {
        filteredChildren.add(child);
      }
    }
    return filteredChildren.toArray();
  }

  /**
   * Method selectResourceInTree.
   * @param path CmsPath
   */
  public static void selectResourceInTree(String perspectiveID, CmsPath path) {
    IResource resource = ResourcesPlugin.getResourceManager().getResource(path);
    if(resource != null) {
      selectResourceInTree(perspectiveID, resource);
    }
  }
  
  public static void selectResourceInTree(CmsPath path) {
    selectResourceInTree(null, path);
  }

  /**
   * Method selectResourceInTree.
   * @param resource IResource
   */
  public static void selectResourceInTree(String perspectiveID, IResource resource) {
    if(resource instanceof IFolder){
      //select the resource itself if its a folder
      selectResourceInTree(perspectiveID, resource, resource);
    }else{
      //otherwise select the resource's parent (since its possible that files aren't shown in the tree)
      selectResourceInTree(perspectiveID, resource.getParent(), resource);
    }
  }
  
  public static void selectResourceInTree(IResource resource) {
    selectResourceInTree(null, resource);
  }
  
  /**
   * Method selectResourceInTree.
   * @param parent IResource
   * @param resource IResource
   */
  public static void selectResourceInTree(final String perspectiveID, final IResource parent, final IResource resource) {
    // make sure this runs in the UI thread
    Display.getDefault().asyncExec(new Runnable() {

      @Override
      public void run() {
        // TODO Auto-generated method stub
        selectResourceInTreeInternal(perspectiveID, parent, resource);
      }

    });

  }
  
  /**
   * Bring the folders view to the front on the active page, but only if the view is already present.
   */
  public static void activateFoldersView() throws Exception {

    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    IViewReference[] views = activePage.getViewReferences();
    ICatExplorerView foldersView = null;
    
    for (IViewReference viewReference : views) {
      IViewPart view = viewReference.getView(true);
      
      if(view instanceof ICatExplorerView) {
        ICatExplorerView explorerView = (ICatExplorerView)view;
        if(explorerView.getTreeExplorer() == null && explorerView.getTableExplorer() != null) {
          foldersView = explorerView;
          break;
        }
      }
    }
    if(foldersView != null) {
      activePage.showView(foldersView.getSite().getId());
    }
  }

  private static boolean selectResourceInCurrentPage(IResource parent, IResource resource, IWorkbenchPage activePage) throws Exception {
    boolean partFound = false;
    IViewReference[] views = activePage.getViewReferences();
    boolean treeOnlyViewFound = false;
    ICatExplorerView treeView = null;
    ICatExplorerView treeAndTableView = null;
    ICatExplorerView tableView = null;

    for (IViewReference viewReference : views) {
      IViewPart view = viewReference.getView(true);
      if(view instanceof ICatExplorerView) {
        ICatExplorerView explorerView = (ICatExplorerView)view;
        boolean treeOnlyView = explorerView.getTreeExplorer() != null && explorerView.getTableExplorer() == null;
        boolean treeTableView = explorerView.getTreeExplorer() != null && explorerView.getTableExplorer() != null;
        boolean tableOnlyView = explorerView.getTreeExplorer() == null && explorerView.getTableExplorer() != null;
        
        if(treeOnlyView && explorerView.isManagedPath(resource.getPath()) ) {
           treeView = explorerView;

        } else if (treeTableView && explorerView.isManagedPath(resource.getPath())) {
          treeAndTableView = explorerView;

        } else if (tableOnlyView) {
          tableView = explorerView;
        }
      }
    }
    
    if(treeView != null) {    
      activePage.showView(treeView.getSite().getId());
      treeView.getTreeExplorer().getTreeViewer().expandToPath(parent.getPath());
      treeView.getTreeExplorer().getTreeViewer().selectResource(parent);
  
    } 
    if (treeAndTableView != null) {
        activePage.showView(treeAndTableView.getSite().getId());
        treeAndTableView.getTreeExplorer().getTreeViewer().expandToPath(parent.getPath());
        treeAndTableView.getTreeExplorer().getTreeViewer().selectResource(parent);
        treeAndTableView.getTableExplorer().requestSelection(resource);

    }
    if (tableView != null) {
      activePage.showView(tableView.getSite().getId());
      TableExplorer tableExplorer = tableView.getTableExplorer();
      if(tableExplorer != null) {
        tableExplorer.requestSelection(resource);
      }
    }

    
    return treeView != null || treeAndTableView != null || tableView != null;
  }
  /**
   * Method selectResourceInTreeInternal.
   * @param parent IResource
   * @param resource IResource
   */
  private static void selectResourceInTreeInternal(String perspectiveID, final IResource parent, final IResource resource) {
    if(perspectiveID == null) {
      perspectiveID = getDocLibPerspectiveProvider().getPerspectiveID(resource);
    }

    try {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      IWorkbenchPage activePage = window.getActivePage();
      if(!selectResourceInCurrentPage(parent, resource, activePage)) {

        if (!activePage.getPerspective().getId().equalsIgnoreCase(perspectiveID)) {
          PerspectiveOpener perspectiveOpener = new PerspectiveOpener(perspectiveID, null, window);      
          int returnCode = perspectiveOpener.openPerspectiveWithPrompt();
          if (returnCode == IDialogConstants.CANCEL_ID) {
            return;
          }
          activePage = window.getActivePage();
          selectResourceInCurrentPage(parent, resource, activePage);
        }
      }

    } catch (Throwable e) {
      // do not crash the UI if this method fails for some reason, as it's not critical
      e.printStackTrace();
    }
  }
  
  public static IViewPart openView(String viewId, String secondaryId) {
    IViewPart part = null;
    
    try {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if(window == null) {
        // get the first one
        window = (PlatformUI.getWorkbench().getWorkbenchWindows())[0];
      }
      IWorkbenchPage activePage = window.getActivePage();
      if(activePage == null) {
        // get the first one
        activePage = (window.getPages())[0];
      }
      part = activePage.showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);

    } catch (Throwable e) {
      // do not crash the UI if this method fails for some reason, as it's not critical
      e.printStackTrace();
    }
    return part;
  }

  /**
   * Will attempt to change perspective, show a view, and select an item in that view.
   * 
   * @param perspectiveId
   * @param viewId
   * @param selectedItem
   */
  public static IViewPart openView(String perspectiveId, boolean newWindow, String viewId, Object selectedItem) {
    IViewPart part = null;
    
    try {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      // open in new window
      if(newWindow){
        window = PlatformUI.getWorkbench().openWorkbenchWindow(perspectiveId, null);
        window.getShell().forceActive();
      
      }

      IWorkbenchPage page = window.getActivePage();
      if (perspectiveId != null) {
        IPerspectiveDescriptor descriptor = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
        page.setPerspective(descriptor);
      }

      if (viewId != null) {
        part = page.showView(viewId);

        if (selectedItem != null) {
          if (part instanceof ISelectionProvider) {
            ISelectionProvider p = (ISelectionProvider) part;
            p.setSelection(new StructuredSelection(selectedItem));
          }
        }
        part.setFocus();
      }
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return part;
  }

  //  IStatusLineManager getStatusLineManager() {
  //    if (statusLineManager != null)
  //      return statusLineManager;
  //    IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
  //    if (activeWindow == null)
  //      return null;
  //    // YUCK! YUCK! YUCK!
  //    // IWorkbenchWindow does not define getStatusLineManager(), yet
  //    // WorkbenchWindow does
  //    try {
  //      Method method = activeWindow.getClass().getDeclaredMethod("getStatusLineManager", new Class[0]); //$NON-NLS-1$
  //      try {
  //        Object statusLine = method.invoke(activeWindow, new Object[0]);
  //        if (statusLine instanceof IStatusLineManager) {
  //          statusLineManager = (IStatusLineManager) statusLine;
  //          return statusLineManager;
  //        }
  //      } catch (InvocationTargetException e) {
  //        // oh well
  //      } catch (IllegalAccessException e) {
  //        // I tried
  //      }
  //    } catch (NoSuchMethodException e) {
  //      // can't blame us for trying.
  //    }
  //
  //    IWorkbenchPartSite site = activeWindow.getActivePage().getActivePart().getSite();
  //    if (site instanceof IViewSite) {
  //      statusLineManager = ((IViewSite) site).getActionBars().getStatusLineManager();
  //    } else if (site instanceof IEditorSite) {
  //      statusLineManager = ((IEditorSite) site).getActionBars().getStatusLineManager();
  //    }
  //    return statusLineManager;
  //  }
  //
  //  void updateStatusLine() {
  //    IStatusLineManager manager = getStatusLineManager();
  //    if (manager != null)
  //      manager.update(true);
  //  }
  
}
