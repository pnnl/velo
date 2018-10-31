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
package gov.pnnl.velo.ui.commands;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.rse.ImportFilesWizard;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Create a new model (could be replaced by external tool)
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class ImportFiles extends AbstractHandler {
  @SuppressWarnings("unused")
  private static Logger logger = CatLogger.getLogger(ImportFiles.class);

  /**
   * Method execute.
   * @param event ExecutionEvent
   * @return Object
   * @throws ExecutionException
   * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    // Get the current selection 
    IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
      return null;
    }

    try {

      ImportFilesWizard wizard = new ImportFilesWizard("Import Files", RCPUtil.getTreeRoot());
      wizard.init(PlatformUI.getWorkbench(), selection);

      // Instantiates the wizard container with the wizard and opens it
      WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);      
      dialog.create();
      dialog.open();

    } catch (Throwable e) {
      ToolErrorHandler.handleError("An unexpected error occurred! See the client log for details.", e, true);
    }
    return null;
  }

}
