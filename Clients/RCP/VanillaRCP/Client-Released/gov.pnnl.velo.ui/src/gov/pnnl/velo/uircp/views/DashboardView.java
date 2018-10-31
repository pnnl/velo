/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package gov.pnnl.velo.uircp.views;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import com.centerkey.utils.BareBonesBrowserLaunch;

import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.perspectives.DataBrowser;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.rse.ImportFilesWizard;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.uircp.VeloUIPlugin;

public class DashboardView extends ViewPart {

  public static final String ID = DashboardView.class.getName();
  private static final String ACTION_SEARCH = "$$_search";
  private static final String ACTION_WORKSPACE = "$$_workspace";
  private static final String ACTION_WEBSITE = "$$_website";
  private static final String ACTION_ERROR = "$$_error";
  private static final String ACTION_TEAM = "$$_team";
  private static final String ACTION_IMPORT = "$$_import";
  private static final String ACTION_HELP = "$$_help";

  private FormToolkit toolkit;

  /**
   * The constructor.
   */
  public DashboardView() {
  }
  

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    final Browser browser;

    browser = new Browser(parent, SWT.NONE);
    browser.setUrl(VeloUIPlugin.getDefault().getUrl("web-sbrsfa/welcome.html").toString());


    browser.addLocationListener(new LocationAdapter() {
      public void changing(LocationEvent event) {
        String location = event.location;

        if(location.contains(ACTION_SEARCH)) {
          // switch to search perspective
          try{
            PlatformUI.getWorkbench().showPerspective(CatPerspectiveIDs.SEARCH, getViewSite().getWorkbenchWindow());

          } catch(WorkbenchException e){
            ToolErrorHandler.handleError("Failed to open perspective.", e, true);
          }
          event.doit = false;          

        } else if (location.contains(ACTION_WORKSPACE)) {
          // switch to data browser perspective
          try{
            PlatformUI.getWorkbench().showPerspective(DataBrowser.ID, getViewSite().getWorkbenchWindow());

          } catch(Throwable e){
            ToolErrorHandler.handleError("Failed to open perspective.", e, true);
          }
          event.doit = false;             
           

        } else if (location.contains(ACTION_WEBSITE)) {
          BareBonesBrowserLaunch.openURL("http://sbrsfaalliance.org/#sthash.0AcL8AqY.OqWrlPlP.dpuf");
          event.doit = false;

        } else if (location.contains(ACTION_TEAM)) {
          // switch to My Workspace perspective
          try{
            PlatformUI.getWorkbench().showPerspective(CatPerspectiveIDs.TEAM_PERSPECTIVE, getViewSite().getWorkbenchWindow());

          } catch(Throwable e){
            ToolErrorHandler.handleError("Failed to open perspective.", e, true);
          }
          event.doit = false;

        } else if (location.contains(ACTION_ERROR)) {
          ToolErrorHandler.openEmailDialog(null);
          event.doit = false;

        } else if (location.contains(ACTION_IMPORT)) {
          try {
// TODO: hook me up
//            ImportFilesWizard wizard = new ImportFilesWizard("Import Files", RCPUtil.getTreeRoot());
//            wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
//
//            // Instantiates the wizard container with the wizard and opens it
//            WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);      
//            dialog.create();
//            dialog.open();

          } catch (Throwable e) {
            ToolErrorHandler.handleError("An unexpected error occurred! See the client log for details.", e, true);
          }
          event.doit = false;
          
        } else if (location.contains(ACTION_HELP)) {
          //IWorkbenchCommandConstants.HELP_HELP_CONTENTS
          try {
            RCPUtil.invokeCommand(null, null, IWorkbenchCommandConstants.HELP_HELP_CONTENTS);
          } catch(Exception e) {
            ToolErrorHandler.handleError("Failed to invoke Help command.", e, true);
          }

          event.doit = false;
        }
      }

    }); 

    toolkit = new FormToolkit(parent.getDisplay());


  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {

  }

  /**
   * Disposes the toolkit
   */
  public void dispose() {
    toolkit.dispose();
    super.dispose();
  }


}
