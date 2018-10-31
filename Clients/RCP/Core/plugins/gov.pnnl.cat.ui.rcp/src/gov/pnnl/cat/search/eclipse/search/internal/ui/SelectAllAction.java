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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.PlatformUI;
 
/**
 * This action selects all entries currently showing in view.
 * @version $Revision: 1.0 $
 */
public class SelectAllAction extends Action {

	private TableViewer fViewer;
	
	/**
	 * Creates the action.
	 */
	public SelectAllAction() {
		super("selectAll"); //$NON-NLS-1$
		setText(SearchMessages.SelectAllAction_label); 
		setToolTipText(SearchMessages.SelectAllAction_tooltip); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ISearchHelpContextIds.SELECT_ALL_ACTION);
	}
	
	/**
	 * Method setViewer.
	 * @param viewer TableViewer
	 */
	public void setViewer(TableViewer viewer) {
		fViewer= viewer;
	}

	/**
	 * Selects all resources in the view.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (fViewer != null && !fViewer.getTable().isDisposed() && fViewer.getTable().isFocusControl()) {
			fViewer.getTable().selectAll();
			// force viewer selection change
			fViewer.setSelection(fViewer.getSelection());
		}
	}
}
