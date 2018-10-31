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
package gov.pnnl.cat.search.basic.results.contextmenu;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.ui.rcp.actions.ChooseProgramAction;
import gov.pnnl.cat.ui.rcp.actions.ExplorerActions;
import gov.pnnl.cat.ui.rcp.actions.OpenFileInSystemEditorAction;
import gov.pnnl.cat.ui.rcp.actions.OpenWithAction;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class SearchResultsContextMenu implements IMenuListener, ISelectionChangedListener, FocusListener {

  private Logger logger = CatLogger.getLogger(SearchResultsContextMenu.class);

  // todo: convert to commands/handlers
  private OpenFileInSystemEditorAction fopenFileAction;
  protected OpenFileInSystemEditorAction fopenWithDefaultEditorAction;

  private ChooseProgramAction chooseProgramAction;
  private IResourceManager mgr;
  protected ContentViewer viewer;
  private IConfigurationElement[] elementExtensions;
  private Vector openWithExtensions;
  private ISelection selection;
  private GroupMarker foreignActionMarker;
  
  private static String OPEN_WITH_EXTENSION_POINT_ID = "gov.pnnl.cat.ui.rcp.openWithMenu";

  /**
   * Constructor for SearchResultsContextMenu.
   * @param workbenchWindow IWorkbenchWindow
   * @param inViewer ContentViewer
   */
  public SearchResultsContextMenu(IWorkbenchWindow workbenchWindow, ContentViewer inViewer) {
    this(workbenchWindow, null, inViewer);
  }
  
  /**
   * Constructor for SearchResultsContextMenu.
   * @param catParentView ICatView
   * @param inViewer ContentViewer
   */
  public SearchResultsContextMenu(ICatExplorerView catParentView, ContentViewer inViewer) {
    this(catParentView.getPage().getWorkbenchWindow(), catParentView, inViewer);
  }
  
  /**
   * Constructor for SearchResultsContextMenu.
   * @param workbenchWindow IWorkbenchWindow
   * @param catParentView ICatView
   * @param inViewer ContentViewer
   */
  public SearchResultsContextMenu(IWorkbenchWindow workbenchWindow, ICatExplorerView catParentView, ContentViewer inViewer) {
    this.viewer = inViewer;
   
    try {
      this.mgr = ResourcesPlugin.getResourceManager();
    } catch (Exception e) {
      logger.error(e);
    }

    fopenFileAction = ExplorerActions.openFileInSystemEditorAction(this.viewer, false);
    fopenWithDefaultEditorAction = ExplorerActions.openFileInSystemEditorAction(this.viewer, true);
    chooseProgramAction = new ChooseProgramAction();
        
    foreignActionMarker = new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS);

    //init extensions
    loadExtensions();
  }


  /**
   * Method listenToViewer.
   * @param viewer ContentViewer
   */
  public void listenToViewer(ContentViewer viewer) {
    viewer.addSelectionChangedListener(this);
    viewer.getControl().addFocusListener(this);
  }


  /**
   * Method menuAboutToShow.
   * @param manager IMenuManager
   * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(IMenuManager)
   */
  public void menuAboutToShow(IMenuManager manager) {
       
    manager.add(new Separator("app.ext"));
    manager.add(new Separator());
    manager.add(fopenFileAction);
    manager.add(createOpenWithMenu());
    manager.add(new Separator("open"));
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(new Separator(IWorkbenchActionConstants.CUT_EXT));
    manager.add(new Separator("selectDelete"));
    manager.add(new Separator("properties"));
  }
  
  /**
   * Creates the "Open With" Menu.  Finds any extensions that for the selected resource.
  
   * @return MenuManager The "Open With" Menu Manager */
  private MenuManager createOpenWithMenu() {
    MenuManager openWithMenu = new MenuManager("Open With");
    if(this.fopenWithDefaultEditorAction.updateProgram()){
      // If the program is the default browser then do not add this to the menu, it'll be in the bottom program listing already (because we have a browser extension)
      if (!(this.fopenWithDefaultEditorAction.getText().equalsIgnoreCase("Browser"))) {
        openWithMenu.add(this.fopenWithDefaultEditorAction);
      }
    }
    
    StructuredSelection structSelection = new StructuredSelection();
    if (selection != null) {
      structSelection = (StructuredSelection) selection;
    }

    //init extensions if this is first time for menu to load:
    loadExtensions();
    if (openWithExtensions.size() > 0) {
      openWithMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      // loop thru extensions to put in here other actions to open files with:
      // todo: update so that it will support handlers as well.
      for (Iterator iter = openWithExtensions.iterator(); iter.hasNext();) {
        OpenWithAction openWithAction = (OpenWithAction) iter.next();
        if (openWithAction.getEnabledStatus(structSelection)) {
          openWithMenu.add(openWithAction);
        }
      }
    }

    openWithMenu.add(new Separator());
    if (chooseProgramAction.getEnabledStatus(structSelection)) {
      openWithMenu.add(chooseProgramAction);
    }

    return openWithMenu;
  }
  
  

  /**
   * Determines the plug-ins have extended the extension point "gov.pnnl.cat.ui.rcp".  
   * These are used for populating in the "Open With" Menu.
   */
  private void loadExtensions(){
    if(elementExtensions != null){
      return;
    }
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    elementExtensions = registry.getConfigurationElementsFor( OPEN_WITH_EXTENSION_POINT_ID );
    this.openWithExtensions = new Vector(elementExtensions.length);
    for (int i = 0; i < elementExtensions.length; i++ ) {
      try {
        OpenWithAction newOpenWithAction = (OpenWithAction) elementExtensions[i].createExecutableExtension("class");
        this.openWithExtensions.addElement( newOpenWithAction );
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        logger.error("Failed", e);
      }
    }
  }
  

  /**
   * Method selectionChanged.
   * @param event SelectionChangedEvent
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {
    this.selection = null;
    if (event != null) {
      selection = event.getSelection();
    }
    enableDisableActions(selection);
  }
  
  
  //TODO Move to the action itself
  /**
   * Method enableDisableActions.
   * @param selection ISelection
   */
  private void enableDisableActions(ISelection selection){
    this.fopenFileAction.updateEnabledStatus(selection);
    this.fopenWithDefaultEditorAction.updateEnabledStatus(selection);
  }

  /**
   * Method focusGained.
   * @param e FocusEvent
   * @see org.eclipse.swt.events.FocusListener#focusGained(FocusEvent)
   */
  public void focusGained(FocusEvent e) {
    enableDisableActions(viewer.getSelection());
  }

  /**
   * Method focusLost.
   * @param e FocusEvent
   * @see org.eclipse.swt.events.FocusListener#focusLost(FocusEvent)
   */
  public void focusLost(FocusEvent e) {
    enableDisableActions(null);
  }
  
  public void doubleClick() {
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
          defaultDoubleClickFolder(selectedResource);
        }
      }
    }
  }
  
  /**
   * Method defaultDoubleClickFolder.
   * @param folder IResource
   */
  protected void defaultDoubleClickFolder(IResource folder) {
    if(folder.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
      // Call the browse remote resource action
      try {
        RSEUtils.openInRemoteSystemsExplorer(folder);
      } catch (Exception ex) {
        ToolErrorHandler.handleError("Failed to open Remote Systems Explorer", ex, true);
      } 
    } else {
      RCPUtil.selectResourceInTree(folder);
    }
  }

}
