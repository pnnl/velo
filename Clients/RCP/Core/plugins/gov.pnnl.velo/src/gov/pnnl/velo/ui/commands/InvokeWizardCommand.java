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

import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 */
public abstract class InvokeWizardCommand extends AbstractHandler {

	protected IWorkbenchWizard wizard;

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
		String id = getWizardId(selection);

		// First see if this is a "new wizard".
		IWizardDescriptor descriptor = PlatformUI.getWorkbench()
				.getNewWizardRegistry().findWizard(id);
		// If not check if it is an "import wizard".
		if (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getImportWizardRegistry()
					.findWizard(id);
		}

		// Or maybe an export wizard
		if (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getExportWizardRegistry()
					.findWizard(id);
		}
		
		try {
			// Then if we have a wizard, open it.
			if (descriptor != null) {
				this.wizard = descriptor.createWizard();
				initWizard();
				wizard.init(PlatformUI.getWorkbench(), selection);
				WizardDialog wd = new WizardDialog(Display.getDefault()
						.getActiveShell(), wizard);
				wd.setTitle(wizard.getWindowTitle());
				wd.open();
			}
		} catch (CoreException e) {
      ToolErrorHandler.handleError("Failed to launch wizard.", e, true);
		}
		return null;
	}

	/**
	 * Method getWizardId.
	 * @param selection IStructuredSelection
	 * @return String
	 */
	protected abstract String getWizardId(IStructuredSelection selection);
	protected void initWizard(){}

}
