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
package gov.pnnl.cat.search.eclipse.search.internal.ui;

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Opens the Search Dialog.
 * @version $Revision: 1.0 $
 */
public class OpenFileSearchPageAction implements IWorkbenchWindowActionDelegate {

	private static final String TEXT_SEARCH_PAGE_ID= "org.eclipse.search.internal.ui.text.TextSearchPage"; //$NON-NLS-1$

	private IWorkbenchWindow fWindow;

	public OpenFileSearchPageAction() {
	}

	/**
	 * Method init.
	 * @param window IWorkbenchWindow
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	/**
	 * Method run.
	 * @param action IAction
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (fWindow == null || fWindow.getActivePage() == null) {
			CatSearchPlugin.beep();
			logErrorMessage("Could not open the search dialog - for some reason the window handle was null"); //$NON-NLS-1$
			return;
		}
		NewSearchUI.openSearchDialog(fWindow, TEXT_SEARCH_PAGE_ID); //$NON-NLS-1$
	}

	/**
	 * Method selectionChanged.
	 * @param action IAction
	 * @param selection ISelection
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	/**
	 * Method dispose.
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow= null;
	}

	/**
	 * Method logErrorMessage.
	 * @param message String
	 */
	public static void logErrorMessage(String message) {
		IStatus status= new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, null);
		CatSearchPlugin.log(status);
	}
}
