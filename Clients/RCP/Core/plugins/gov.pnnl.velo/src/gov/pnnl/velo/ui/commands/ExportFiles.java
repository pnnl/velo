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
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.FileExportWizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
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
public class ExportFiles extends AbstractHandler {
  @SuppressWarnings("unused")
  private static Logger logger = CatLogger.getLogger(ExportFiles.class);

  protected static final String EXTENSION_POINT = "gov.pnnl.velo.customExportBehavior";
  protected static final String ATTRIBUTE = "class";
  protected static List<CustomExportBehavior> customExportBehaviors;
  static {
    loadCustomBehaviors();
  }

  private static void loadCustomBehaviors() {
    customExportBehaviors = new ArrayList<CustomExportBehavior>();

    try {
      // look up all the extensions for the custom behaviors
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof CustomExportBehavior) {
          CustomExportBehavior behavior = (CustomExportBehavior)obj;
          customExportBehaviors.add(behavior);
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom delete behavior extension points.", e);
    }
  }

  /**
   * Method execute.
   * @param event ExecutionEvent
   * @return Object
   * @throws ExecutionException
   * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
    boolean exported = false;
    for(CustomExportBehavior behavior : customExportBehaviors) {
      exported = behavior.export(selection);
      if(exported) {
        break;
      }
    }
    if(!exported) {
      defaultExecute(event);      
    }

    return null;
  }

  private void defaultExecute(ExecutionEvent event) throws ExecutionException {

    // Get the current selection 
    IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);

    try {

      FileExportWizard wizard = new FileExportWizard(RCPUtil.getTreeRoot(), false);
      wizard.init(PlatformUI.getWorkbench(), selection);

      // Instantiates the wizard container with the wizard and opens it
      WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);      
      dialog.create();
      dialog.open();

    } catch (Throwable e) {
      StatusUtil.handleStatus(
          "An unexpected error occurred! See the client log for details.",
          e, StatusManager.SHOW);
    }
  }

}
