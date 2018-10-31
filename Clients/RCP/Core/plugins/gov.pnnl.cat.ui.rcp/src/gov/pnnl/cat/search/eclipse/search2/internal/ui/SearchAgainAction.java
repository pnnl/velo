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
package gov.pnnl.cat.search.eclipse.search2.internal.ui;

import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPluginImages;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 */
class SearchAgainAction extends Action {
	private SearchResultsView fView;
	
	/**
	 * Constructor for SearchAgainAction.
	 * @param view SearchResultsView
	 */
	public SearchAgainAction(SearchResultsView view) {
		setText(SearchMessages.SearchAgainAction_label); 
		setToolTipText(SearchMessages.SearchAgainAction_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_TOOL, SearchPluginImages.IMG_TOOL_SEARCH);
		fView= view;	
	}

	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		final ISearchResult search= fView.getCurrentSearchResult();
		if (search != null) {
			ISearchQuery query= search.getQuery();
			NewSearchUI.cancelQuery(query);
			if (query.canRerun()) {
				if (query.canRunInBackground())
					NewSearchUI.runQueryInBackground(query);
				else {
					Shell shell= fView.getSite().getShell();
					ProgressMonitorDialog pmd= new ProgressMonitorDialog(shell);
					IStatus status= NewSearchUI.runQueryInForeground(pmd, query);
					if (!status.isOK() && status.getSeverity() != IStatus.CANCEL) {
						ErrorDialog.openError(shell, SearchMessages.SearchAgainAction_Error_title, SearchMessages.SearchAgainAction_Error_message, status); 
					}
				}
			}
		}
	}
}
