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
package gov.pnnl.cat.alerts.handlers;

import gov.pnnl.cat.alerts.wizards.AddSubscriptionToResourceWizard;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 */
public class AddSubscriptionToResourceHandler extends AbstractHandler {

	/**
	 * Method execute.
	 * @param event ExecutionEvent
	 * @return Object
	 * @throws ExecutionException
	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
		  ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager(); 
			IUser user = securityMgr.getActiveUser();
			
			IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
			
			AddSubscriptionToResourceWizard wizard = new AddSubscriptionToResourceWizard();
			wizard.init(PlatformUI.getWorkbench(), selection);
			wizard.setUser(user);
			
			WizardDialog wizardDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
			wizardDialog.setPageSize(200, 250);
			wizardDialog.setMinimumPageSize(200, 250);
			
			wizardDialog.open();
		} catch (Throwable e) {
		  ToolErrorHandler.handleError("An error occurred while trying to retrieve user information.", e, true);
		}
		
		return null;
	}

}
