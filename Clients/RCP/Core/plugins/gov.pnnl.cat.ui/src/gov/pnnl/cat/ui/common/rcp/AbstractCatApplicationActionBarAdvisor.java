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
package gov.pnnl.cat.ui.common.rcp;

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.ui.UiPlugin;
import gov.pnnl.cat.ui.utils.StatusLineCLabelContributionItem;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 * @version $Revision: 1.0 $
 */
public abstract class AbstractCatApplicationActionBarAdvisor extends ActionBarAdvisor {

  // Actions - important to allocate these only in makeActions, and then use
  // them in the fill methods. This ensures that the actions aren't recreated
  // when fillActionBars is called with FILL_PROXY.
  private IWorkbenchAction exitAction;
  // private IWorkbenchAction propertiesAction;
  private IWorkbenchAction importAction;
  private IWorkbenchAction exportAction;
  private IWorkbenchAction aboutAction;
  private IWorkbenchAction closeAllPerspectivesAction;
  private IWorkbenchAction closePerspectiveAction;
  private IWorkbenchAction resetPerspectiveAction;
  private IWorkbenchAction preferencesAction;
  
  private IWorkbenchAction introAction; // this shows the intro (Welcome) window
  private IWorkbenchAction helpContentsAction; // This is the help contents menu action item.
  private Action reportErrorAction;

  // generic retarget actions
  private IWorkbenchAction undoAction;
  private IWorkbenchAction redoAction;
  private IWorkbenchAction cutAction;
  private IWorkbenchAction copyAction;
  private IWorkbenchAction pasteAction;
  private IWorkbenchAction deleteAction;
  private IWorkbenchAction selectAllAction;
  private IWorkbenchAction findAction;
  private IWorkbenchAction printAction;
  private IWorkbenchAction saveAction;
  private IWorkbenchAction saveAsAction;

  /**
   * contribution items
  
   */
  private IWorkbenchAction mOpenNewWindowAction;
  private IContributionItem mPerspectiveMenu;
  private IContributionItem mViewMenu;
  private IContributionItem mWizardMenu;

  
  // So child classes can turn off certain Eclipse menus
  protected boolean showNewMenu = true;
  protected boolean showImportMenu = true;
  protected boolean showExportMenu = true;
  protected boolean showJobsMenu = true;
  protected boolean showHelpContents = true;
  protected boolean showProperties = true;

  /**
   * @param configurer
   */
  public AbstractCatApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
    super(configurer);
  }

  /**
   * Creates the actions and registers them.
   * Registering is needed to ensure that key bindings work.
   * The corresponding commands keybindings are defined in the plugin.xml file.
   * Registering also provides automatic disposal of the actions when
   * the window is closed.
   * @param window IWorkbenchWindow
   */
  protected void makeActions(final IWorkbenchWindow window) {
    
    exitAction = ActionFactory.QUIT.create(window);
    register(exitAction);

    undoAction = ActionFactory.UNDO.create(window);
    register(undoAction);

    redoAction = ActionFactory.REDO.create(window);
    register(redoAction);

    cutAction = ActionFactory.CUT.create(window);
    register(cutAction);

    copyAction = ActionFactory.COPY.create(window);
    register(copyAction);

    pasteAction = ActionFactory.PASTE.create(window);
    register(pasteAction);

    selectAllAction = ActionFactory.SELECT_ALL.create(window);
    register(selectAllAction);
    
    //import action in File menu
    importAction = ActionFactory.IMPORT.create(window);
    register(importAction);
    
    deleteAction = ActionFactory.DELETE.create(window);
    register(deleteAction);
    
    
    // JSD This is for the intro (Welcome) page
    if (window.getWorkbench().getIntroManager().hasIntro()) {
     	introAction= ActionFactory.INTRO.create(window);
     	register(introAction);
     }
    
    reportErrorAction = new Action() {

      @Override
      public String getText() {
        return "Report Error";
      }
      
      @Override
      public ImageDescriptor getImageDescriptor() {        
        String imagePath = UiPlugin.getDefault().getAbsolutePath("icons/16x16/exclamation.png");
        return SWTResourceManager.getImageDescriptor(imagePath);
      }

      @Override
      public String getToolTipText() {
        return "Send an email to report an error.";
      }

      @Override
      public void run() {
        ToolErrorHandler.openEmailDialog(null);
      }
      
    };

    
    // JSD This is for the Help contents menu item
    helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
    register(helpContentsAction);

    //new window action in Window Menu
    mOpenNewWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
    register(mOpenNewWindowAction);
    
//    //properties action in window menu
//    propertiesAction = ActionFactory.PROPERTIES.create(window);
//    register(propertiesAction);
       
    
    //preferences action in window menu 
    preferencesAction = ActionFactory.PREFERENCES.create(window);
    register(preferencesAction);

        
    //Abut action in help menu
    aboutAction = ActionFactory.ABOUT.create(window);
    register(aboutAction);
    
    //close all perspectives action in window menu
    closeAllPerspectivesAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create(window);
    register(closeAllPerspectivesAction);
    
    //close perspective action in window menu
    closePerspectiveAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
    register(closePerspectiveAction);
    
    //reset perspective in window menu
    resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
    register(resetPerspectiveAction);
    
    //"other" in open perspective shortlist in window menu
    mPerspectiveMenu = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
    
    // We are not using Eclipse's ShowViewMenu because we want to get rid of the Other... item at the bottom
    mViewMenu = new ShowViewMenu(window, "viewsShortlist");
    //ContributionItemFactory.VIEWS_SHORTLIST.create(window);
    
    
//    mSearchAction = new SearchContributionItem(window);

//    //could we get this to work? ie: have eclipse's retargetable 'copy' action
      //available but it not show up unless specified by a perspective

    saveAction = ActionFactory.SAVE.create(window);
    register(saveAction);
    
    mWizardMenu = ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(window);
    
    exportAction = ActionFactory.EXPORT.create(window);
    register(exportAction);
        
    saveAsAction = ActionFactory.SAVE_AS.create(window);
    register(saveAsAction);
    
    printAction = ActionFactory.PRINT.create(window);
    register(printAction);

    findAction = ActionFactory.FIND.create(window);
    register(findAction);
  }
    
  /* (non-Javadoc)
   * @see org.eclipse.ui.application.ActionBarAdvisor#fillStatusLine(org.eclipse.jface.action.IStatusLineManager)
   */
  @Override
  protected void fillStatusLine(IStatusLineManager statusLineManager) {
    StatusLineCLabelContributionItem loginStatus;
    StatusLineCLabelContributionItem messagingStatus;
    StatusLineCLabelContributionItem poweredByVeloStatus;
    
    // powered by velo
    poweredByVeloStatus = new StatusLineCLabelContributionItem("poweredByVeloStatus", 19);
    String imagePath = UiPlugin.getDefault().getAbsolutePath("icons/RCPFooter.png");
    Image image = SWTResourceManager.getImage(imagePath);
    poweredByVeloStatus.setImage(image);
    statusLineManager.insertAfter(StatusLineManager.END_GROUP, poweredByVeloStatus);

    // login status
    loginStatus = new StatusLineCLabelContributionItem("veloLoginStatus", 26);
    statusLineManager.appendToGroup(StatusLineManager.BEGIN_GROUP, loginStatus);
    ISecurityManager smgr = ResourcesPlugin.getSecurityManager();
    try {
      String currentUser = smgr.getActiveUser().getUsername();
      if(currentUser != null) {
        loginStatus.setText("Logged in as " + currentUser);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    
    // messaging status
    image = SWTResourceManager.getImage(UiPlugin.getDefault().getAbsolutePath("icons/16x16/information.gif"));
    messagingStatus = new StatusLineCLabelContributionItem("veloMessagingStatus", 25);
    messagingStatus.setText("messaging enabled");
    messagingStatus.setImage(image);
    statusLineManager.appendToGroup(StatusLineManager.MIDDLE_GROUP, messagingStatus);
     
  }

  /**
   * Method fillMenuBar.
   * @param menuBar IMenuManager
   */
  protected void fillMenuBar(IMenuManager menuBar) {
    // File Menu
    menuBar.add(createFileMenu());
    
    // Edit Menu
    menuBar.add(createEditMenu());
    
    // Jobs Menu hook
    if(showJobsMenu) {
      menuBar.add(new GroupMarker("jobs"));
    }
    
    // Additions will be contributed after the Jobs menu
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    
    // Window Menu
    menuBar.add(createWindowMenu());
    
    // Help Menu
    menuBar.add(createHelpMenu());

  }
  
  /**
   * Method createWindowMenu.
   * @return MenuManager
   */
  protected MenuManager createWindowMenu() {
    MenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
    windowMenu.add(mOpenNewWindowAction);
    
    MenuManager perspectiveMenu = new MenuManager("Open Perspective", "perspective");
    perspectiveMenu.add(mPerspectiveMenu);
    windowMenu.add(perspectiveMenu);
    
    MenuManager viewMenu = new MenuManager("Open View", "view");
    viewMenu.add(mViewMenu);
    windowMenu.add(viewMenu);
    windowMenu.add(new Separator());
    windowMenu.add(closePerspectiveAction);
    windowMenu.add(closeAllPerspectivesAction);
    windowMenu.add(resetPerspectiveAction);
    windowMenu.add(new Separator());
//    windowMenu.add(new Separator());
//    windowMenu.add(mPropertiesAction);
    windowMenu.add(new Separator());
    windowMenu.add(preferencesAction);

    return windowMenu;
  }

  /**
   * Method createHelpMenu.
   * @return MenuManager
   */
  protected MenuManager createHelpMenu() {
    MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
    
    // Add the intro (Welcome) page.
    if(introAction != null){
      helpMenu.add(introAction);
    }
    
    helpMenu.add(new Separator());
    
    // Add report error action
    helpMenu.add(reportErrorAction);
    
    helpMenu.add(new Separator());
  
    // TODO: maybe we can determine this programmatically if certain help 
    // extension points have been contributed
    if(showHelpContents) {
      // Add the help contents menu item to the Help menu.
      helpMenu.add(helpContentsAction);
    }
    helpMenu.add(new Separator());
    helpMenu.add(aboutAction); 
    
    return helpMenu;
  }
  
  /**
   * Creates and returns the Edit menu, shared by all windows
   * @return MenuManager
   */
  protected MenuManager createEditMenu() {
    MenuManager menu = new MenuManager("Edit", IWorkbenchActionConstants.M_EDIT);
    menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
    menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
    menu.add(new Separator());
    menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
    menu.add(new Separator());
    menu.add(new GroupMarker("selectDelete"));
    menu.add(new Separator());
    menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
    menu.add(new Separator());
    menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));
    menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
    menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    return menu;
  }

  
  /**
   * Creates and returns the File menu.
   * @return MenuManager
   */
  protected MenuManager createFileMenu() {
    MenuManager menu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
    menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));

    // create the New submenu, using the same id for it as the New action
    if(showNewMenu) {
      String newId = ActionFactory.NEW.getId();
      MenuManager wizardMenu = new MenuManager("&New", "new");
      wizardMenu.add(mWizardMenu);

      wizardMenu.add(new Separator(newId));
      wizardMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
      menu.add(wizardMenu);
    }
    MenuManager openMenu = new MenuManager("&Open", "open");
    openMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    menu.add(openMenu);

    menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
    menu.add(new Separator());

    menu.add(saveAction);
    menu.add(saveAsAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
    menu.add(new Separator());
    if(showImportMenu) {
      menu.add(importAction);
    }
    if(showExportMenu) {
      menu.add(exportAction);
    }
    menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
    menu.add(new Separator());

    menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

    if(showProperties) {
      menu.add(new Separator("properties"));
    }
    
//    menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
    menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
    menu.add(new Separator());
    menu.add(exitAction);
    menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
    return menu;
  }

  /**
   * Method fillCoolBar.
   * @param coolBar ICoolBarManager
   */
  protected void fillCoolBar(ICoolBarManager coolBar) {
  }

  
}
