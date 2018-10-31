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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Opens the Search Dialog.
 * @version $Revision: 1.0 $
 */
public class OpenSearchDialogAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow fWindow;
	private String fPageId;

	public OpenSearchDialogAction() {
		super(SearchMessages.OpenSearchDialogAction_label); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_TOOL, SearchPluginImages.IMG_TOOL_SEARCH);
		setToolTipText(SearchMessages.OpenSearchDialogAction_tooltip); 
	}

	/**
	 * Constructor for OpenSearchDialogAction.
	 * @param window IWorkbenchWindow
	 * @param pageId String
	 */
	public OpenSearchDialogAction(IWorkbenchWindow window, String pageId) {
		this();
		fPageId= pageId;
		fWindow= window;
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
		run();
	}

	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (getWindow().getActivePage() == null) {
			CatSearchPlugin.beep();
			return;
		}
		SearchDialog dialog= new SearchDialog(
			getWindow().getShell(),
			getSelection(),
			getEditorPart(),
			fPageId);
		dialog.open();
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
	 * Method getSelection.
	 * @return ISelection
	 */
	private ISelection getSelection() {
		return getWindow().getSelectionService().getSelection();
	}
	
	/**
	 * Method getEditorPart.
	 * @return IEditorPart
	 */
	private IEditorPart getEditorPart() {
		return getWindow().getActivePage().getActiveEditor();
	}

	/**
	 * Method getWindow.
	 * @return IWorkbenchWindow
	 */
	private IWorkbenchWindow getWindow() {
		if (fWindow == null)
			fWindow= CatSearchPlugin.getActiveWorkbenchWindow();
		return fWindow;
	}

	/**
	 * Method dispose.
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow= null;
	}
}
