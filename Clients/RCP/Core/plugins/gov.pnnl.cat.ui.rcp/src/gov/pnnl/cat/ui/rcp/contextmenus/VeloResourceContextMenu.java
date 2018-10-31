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
package gov.pnnl.cat.ui.rcp.contextmenus;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.wb.swt.SWTResourceManager;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.ChooseProgramAction;
import gov.pnnl.cat.ui.rcp.actions.ExplorerActions;
import gov.pnnl.cat.ui.rcp.actions.OpenFileInSystemEditorAction;
import gov.pnnl.cat.ui.rcp.actions.OpenWithAction;
import gov.pnnl.cat.ui.rcp.editors.ResourceTextEditor;
import gov.pnnl.cat.ui.rcp.editors.VeloEditorUtil;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.IFavoriteFolder;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.ISmartFolder;
import gov.pnnl.cat.ui.rcp.views.databrowser.service.FavoritesService;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class VeloResourceContextMenu implements IMenuListener, ISelectionChangedListener, FocusListener {

  private Logger logger = CatLogger.getLogger(VeloResourceContextMenu.class);

  // todo: convert to commands/handlers
  private OpenFileInSystemEditorAction fopenFileAction;
  protected OpenFileInSystemEditorAction fopenWithDefaultEditorAction;
  
  private IAction removeFavoritesAction;

  private ChooseProgramAction chooseProgramAction;
  private IResourceManager mgr;
  protected ContentViewer viewer;
  private IConfigurationElement[] elementExtensions;
  private Vector openWithExtensions;
  private ISelection selection;
  protected IContributionItem mWizardMenu;
  private GroupMarker foreignActionMarker;
  ICatExplorerView parentView;

  private static String OPEN_WITH_EXTENSION_POINT_ID = "gov.pnnl.cat.ui.rcp.openWithMenu";

  private IEditorRegistry registry;

  private SelectionChangedEvent event;

  /**
   * Constructor for VeloResourceContextMenu.
   * 
   * @param catParentView
   *          ICatView
   * @param inViewer
   *          ContentViewer
   * @param showNewWizardInPopupMenu
   *          boolean
   */
  public VeloResourceContextMenu(ICatExplorerView catParentView, ContentViewer inViewer, boolean showNewWizardInPopupMenu) {
    this(catParentView.getPage().getWorkbenchWindow(), catParentView, inViewer, showNewWizardInPopupMenu);
  }

  /**
   * Constructor for VeloResourceContextMenu.
   * 
   * @param workbenchWindow
   *          IWorkbenchWindow
   * @param catParentView
   *          ICatView
   * @param inViewer
   *          ContentViewer
   * @param showNewWizardInPopupMenu
   *          boolean
   */
  public VeloResourceContextMenu(IWorkbenchWindow workbenchWindow, ICatExplorerView catParentView, ContentViewer inViewer, boolean showNewWizardInPopupMenu) {
    this.viewer = inViewer;

    this.registry = PlatformUI.getWorkbench().getEditorRegistry();
    try {
      this.mgr = ResourcesPlugin.getResourceManager();
    } catch (Exception e) {
      logger.error(e);
    }

    this.parentView = catParentView;
    fopenFileAction = ExplorerActions.openFileInSystemEditorAction(this.viewer, false);
    fopenWithDefaultEditorAction = ExplorerActions.openFileInSystemEditorAction(this.viewer, true);
    chooseProgramAction = new ChooseProgramAction();
    
    removeFavoritesAction = new RemoveFavoritesAction();

    if (workbenchWindow != null && showNewWizardInPopupMenu) {
      mWizardMenu = ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(workbenchWindow);
    }

    foreignActionMarker = new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS);

    // init extensions
    loadExtensions();
  }

  /**
   * Method listenToViewer.
   * 
   * @param viewer
   *          ContentViewer
   */
  public void listenToViewer(ContentViewer viewer) {
    viewer.addSelectionChangedListener(this);
    viewer.getControl().addFocusListener(this);
  }

  /**
   * Method menuAboutToShow.
   * 
   * @param manager
   *          IMenuManager
   * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(IMenuManager)
   */
  public void menuAboutToShow(IMenuManager manager) {
    Object selected = ((StructuredSelection) selection).getFirstElement();
    
    if(selected instanceof ISmartFolder) {
    
    } else if (selected instanceof IFavoriteFolder) {
      // change the delete function to remove
      displayResourceContextMenu(manager, true);
    
    } else if (selected instanceof IResource) {
      if(!((IResource)selected).getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
        displayResourceContextMenu(manager, false);              
      }
      
    } 
  }
  
  protected void displayResourceContextMenu(IMenuManager manager, boolean isFavorite) {
    if (mWizardMenu != null) {
      MenuManager wizardMenu = new MenuManager("&New", "new");
      wizardMenu.add(mWizardMenu);
      manager.add(wizardMenu);
    }
    manager.add(new Separator("new"));
    manager.add(new Separator("openIn"));
    manager.add(new Separator("app.ext"));
    manager.add(new Separator());
    manager.add(fopenFileAction);
    manager.add(createOpenWithMenu());
    manager.add(new Separator("open"));
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(new Separator(IWorkbenchActionConstants.CUT_EXT));
    manager.add(new Separator("selectDelete"));      
    manager.add(new Separator("properties"));
    if(isFavorite) {
      manager.add(removeFavoritesAction);
    }
  }

  /**
   * Creates the "Open With" Menu. Finds any extensions that for the selected resource.
   * 
   * @return MenuManager The "Open With" Menu Manager
   */
  private MenuManager createOpenWithMenu() {
    MenuManager openWithMenu = new MenuManager("Open With");

    if (this.fopenWithDefaultEditorAction.updateProgram()) {
      // If the program is the default browser then do not add this to the menu, it'll be in the bottom program listing already (because we have a browser extension)
      if (!(this.fopenWithDefaultEditorAction.getText().equalsIgnoreCase("Browser"))) {
        openWithMenu.add(this.fopenWithDefaultEditorAction);
      }
    }

    StructuredSelection structSelection = new StructuredSelection();
    if (selection != null) {
      structSelection = (StructuredSelection) selection;
    }

    boolean editorAdded = false;
    // only let editors show up for single FILE selections:
    if (!structSelection.isEmpty() && structSelection.size() == 1) {
      IResource resource = RCPUtil.getResource(structSelection.getFirstElement());
      if (resource.isType(IResource.FILE)) {

        String resourceFileName = resource.getName();
        List<IEditorDescriptor> veloEditors = VeloEditorUtil.getVeloEditors(resourceFileName);
        for (IEditorDescriptor editor : veloEditors) {
          createOpenEditorAction(openWithMenu, editor, null);
        }

        // always add the resource text editor last:
        IEditorDescriptor textEditor = registry.findEditor(ResourceTextEditor.RESOURCE_TEXT_EDITOR_ID);
        createOpenEditorAction(openWithMenu, textEditor, null);
        editorAdded = true;
      }
    }

    if (editorAdded) {
      openWithMenu.add(new Separator());
    }
    // load the "open with" extenstions that are NOT editors:
    // init extensions if this is first time for menu to load:
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
   * Determines the plug-ins have extended the extension point "gov.pnnl.cat.ui.rcp". These are used for populating in the "Open With" Menu.
   */
  private void loadExtensions() {
    if (elementExtensions != null) {
      return;
    }

    IExtensionRegistry registry = Platform.getExtensionRegistry();
    elementExtensions = registry.getConfigurationElementsFor(OPEN_WITH_EXTENSION_POINT_ID);
    this.openWithExtensions = new Vector(elementExtensions.length);
    for (int i = 0; i < elementExtensions.length; i++) {
      try {
        OpenWithAction newOpenWithAction = (OpenWithAction) elementExtensions[i].createExecutableExtension("class");
        this.openWithExtensions.addElement(newOpenWithAction);
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        logger.error("Failed", e);
      }
    }
  }

  /**
   * Method selectionChanged.
   * 
   * @param event
   *          SelectionChangedEvent
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
   */
  public void selectionChanged(SelectionChangedEvent event) {
    this.selection = null;
    this.event = null;
    if (event != null) {
      selection = event.getSelection();
      this.event = event;
    }
    enableDisableActions(selection);
  }

  // TODO Move to the action itself
  /**
   * Method enableDisableActions.
   * 
   * @param selection
   *          ISelection
   */
  private void enableDisableActions(ISelection selection) {
    this.fopenFileAction.updateEnabledStatus(selection);
    this.fopenWithDefaultEditorAction.updateEnabledStatus(selection);
  }

  /**
   * Method focusGained.
   * 
   * @param e
   *          FocusEvent
   * @see org.eclipse.swt.events.FocusListener#focusGained(FocusEvent)
   */
  public void focusGained(FocusEvent e) {
    enableDisableActions(viewer.getSelection());
  }

  /**
   * Method focusLost.
   * 
   * @param e
   *          FocusEvent
   * @see org.eclipse.swt.events.FocusListener#focusLost(FocusEvent)
   */
  public void focusLost(FocusEvent e) {
    enableDisableActions(null);
  }

  // These methods copied from org.eclipse.ui.actions.OpenWithMenu and had some
  // slight modifications to work with Velo's IResource instead of Eclipse's
  /**
   * Match both the input and id, so that different types of editor can be opened on the same input.
   */
  private static final int MATCH_BOTH = IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID;

  /**
   * Opens the given editor on the selected file.
   *
   * @param editorDescriptor
   *          the editor descriptor, or null for the system editor
   *
   * @since 3.5
   */
  protected void openEditor(IEditorDescriptor editorDescriptor) {
    IFile file = getFileResource();
    if (file == null) {
      return;
    }
    
    VeloEditorUtil.openFilesInEditor(true, null, editorDescriptor, file);
  }

  /**
   * Creates the menu item for the editor descriptor.
   *
   * @param menu
   *          the menu to add the item to
   * @param descriptor
   *          the editor descriptor, or null for the system editor
   * @param preferredEditor
   *          the descriptor of the preferred editor, or <code>null</code>
   */
  private void createOpenEditorAction(MenuManager menu, final IEditorDescriptor descriptor, final IEditorDescriptor preferredEditor) {
    final IAction openEditorAction = new org.eclipse.jface.action.Action() {

      @Override
      public ImageDescriptor getImageDescriptor() {
        return getEditorImageDescriptor(descriptor);
      }

      @Override
      public String getText() {
        return descriptor.getLabel();
      }

      @Override
      public void run() {
        openEditor(descriptor);
      }

    };

    menu.add(openEditorAction);
  }


  /**
   * Returns the image descriptor for the given editor descriptor, or null if it has no image.
   */
  private ImageDescriptor getEditorImageDescriptor(IEditorDescriptor editorDesc) {
    ImageDescriptor imageDesc = null;
    if (editorDesc == null) {
      imageDesc = registry.getImageDescriptor(getFileResource().getName());
      // TODO: is this case valid, and if so, what are the implications for content-type editor bindings?
    } else {
      imageDesc = editorDesc.getImageDescriptor();
    }
    if (imageDesc == null) {
      if (editorDesc.getId().equals(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)) {
        imageDesc = registry.getSystemExternalEditorImageDescriptor(getFileResource().getName());
      }
    }
    return imageDesc;
  }

  /**
   * Converts the IAdaptable file to IFile or null.
   */
  private IFile getFileResource() {
    StructuredSelection structSelection = new StructuredSelection();
    if (selection != null) {
      structSelection = (StructuredSelection) selection;
    }
    IResource resource = RCPUtil.getResource(structSelection.getFirstElement());
    if (resource.isType(IResource.FILE)) {
      return (IFile) resource;
    } else
      return null;
  }

  private class RemoveFavoritesAction extends Action {
   
    public RemoveFavoritesAction() {
      super("Remove From Favorites");
      setImageDescriptor(SWTResourceManager.getPluginImageDescriptor("gov.pnnl.cat.ui", "icons/16x16/delete2.gif"));
      setToolTipText("Remove from Favorites");
    }
    
    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      IFolder selected = (IFolder) (((StructuredSelection) selection).getFirstElement());
      FavoritesService.getInstance().removeFavorite(selected);
      parentView.getTreeExplorer().reload();
    }
  }


}
