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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.utils.CatUIUtil;

/**
 */
public abstract class AbstractCatApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

  // Application title used on window title bar
  protected String appTitle;
  private static boolean firstWindow = true;

  /**
   * @param configurer
   * @param appTitle
   *          String
   */
  public AbstractCatApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, String appTitle) {
    super(configurer);
    this.appTitle = appTitle;
  }
  
  public abstract String[] getPerspectiveIds();

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
   */
  @Override
  public void preWindowOpen() {
    final IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
    configurer.setInitialSize(new Point(1024, 835));
    configurer.setShowCoolBar(false);
    configurer.setShowStatusLine(true);
    configurer.setShowMenuBar(true);
    configurer.setShowPerspectiveBar(true);
    configurer.setShowProgressIndicator(true);
    configurer.setShowFastViewBars(true);
    configurer.setShowStatusLine(true);

    configurer.getWindow().addPerspectiveListener(new IPerspectiveListener() {

      @Override
      public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        configurer.setTitle(getTitle(perspective));
      }

      @Override
      public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
        configurer.setTitle(getTitle(perspective));
      }

    });
  }
  
  @Override
  public void postWindowCreate() {
    // uncomment this if we want to maximize the window
//      IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
//      IWorkbenchWindow window = configurer.getWindow();
//      window.getShell().setMaximized(true);
  }

  /**
   * Method getTitle.
   * 
   * @param perspective
   *          IPerspectiveDescriptor
   * @return String
   */
  protected String getTitle(IPerspectiveDescriptor perspective) {
    return getTitle();
  }

  public static String getTitle() {
    return Platform.getProduct().getName() + " [version: " + getVersion() + "] ";
  }

  public static String getVersion() {
    // use the rcp plugin bundle version as the version
    return Platform.getProduct().getDefiningBundle().getVersion().toString();
  }

  public void postWindowOpen() {

    String[] args = Platform.getCommandLineArgs();
    CatLogger.getLogger(CatUIUtil.class).info("Command Line: " + Arrays.asList(args));

    // This should be more robust
    if (args.length == 0)
      return;

    String last = args[args.length - 1];
    if (last.endsWith(".catfile")) {
      File file = new File(last);
      if (!file.exists())
        return;
      try {
        CatUIUtil.startWith(file);
      } catch (IOException e) {
        CatLogger.getLogger(CatUIUtil.class).error("File startup failed.", e);
      }
    }

    // ZCG: STOLE FROM INTERWEB: http://stackoverflow.com/questions/11774138/remove-file-edit-etc-menus-from-eclipse-rcp-application
    // remove unwanted UI contributions that eclipse makes by default
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();

    for (int i = 0; i < windows.length; ++i) {
      IWorkbenchPage page = windows[i].getActivePage();
      if (page != null) {
        hideUnwantedUiItems(page);
      }
    }
    
    PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new IPerspectiveListener() {
      @Override
      public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
      }

      @Override
      public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
        hideUnwantedUiItems(page);
      }
    });
    
    // this is a hack to work around bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=382625
    initializePerspectives();

  }
  
  private void initializePerspectives() {
    if(firstWindow) {
      String[] perspectiveIds = getPerspectiveIds();
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      
      int index = 0;
      for (String id : perspectiveIds) {
        try {
          activatePerspective(window, id, index);
          index++;
          
        } catch (Throwable e) {
          e.printStackTrace();
        } 
      }

      firstWindow = false;
    }
  }
  
  @SuppressWarnings("restriction")
  private void activatePerspective(IWorkbenchWindow window, String perspectiveId, int index) {
    
    EModelService modelService = (EModelService) PlatformUI.getWorkbench().getService(EModelService.class);
    MWindow model = ((WorkbenchWindow) window).getModel();
    List<MPerspectiveStack> theStack = modelService.findElements(model, null,
            MPerspectiveStack.class, null);
    MPerspectiveStack _perspectiveStack=null;
    if (theStack.size() > 0) {
      _perspectiveStack = theStack.get(0);
    }
        
    IPerspectiveDescriptor perspect = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
        
        
    MPerspective pers = MAdvancedFactory.INSTANCE.createPerspective();
        
    pers.setLabel(perspect.getLabel());
    pers.setElementId(perspect.getId());

    EPartService partService = (EPartService) window.getService(EPartService.class);
        
    IPerspectiveFactory factory = ((PerspectiveDescriptor) perspect).createFactory();
    ModeledPageLayout modelLayout = new ModeledPageLayout(model, modelService,
            partService, pers, perspect, (WorkbenchPage) window.getActivePage(), true);
        factory.createInitialLayout(modelLayout);

    if(_perspectiveStack!=null) {
      boolean match = false;
      for (MPerspective persp : _perspectiveStack.getChildren()) {
        if(persp.getElementId().equals(perspectiveId)) {
          match = true; 
          break;
        }
      }
      if(!match) {
        _perspectiveStack.getChildren().add(index, pers);
      }
    }

  }
  
  private void hideUnwantedUiItems(IWorkbenchPage page) {
    // hide generic 'File' commands
    page.hideActionSet("org.eclipse.ui.actionSet.openFiles");

    // hide 'Convert Line Delimiters To...'
    page.hideActionSet("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");

    // hide 'Search' commands
    page.hideActionSet("org.eclipse.search.searchActionSet");
    page.hideActionSet("org.eclipse.rse.core.search.searchActionSet");

    // hide 'Annotation' commands
    page.hideActionSet("org.eclipse.ui.edit.text.actionSet.annotationNavigation");

    // hide 'Forward/Back' type navigation commands
    page.hideActionSet("org.eclipse.ui.edit.text.actionSet.navigation");
  }

}
