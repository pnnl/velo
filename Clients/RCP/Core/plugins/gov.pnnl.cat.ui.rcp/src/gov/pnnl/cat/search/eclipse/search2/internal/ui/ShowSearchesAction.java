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

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;



/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 * @version $Revision: 1.0 $
 */
class ShowSearchesAction extends Action {
	private SearchResultsView fSearchView;


	/*
	 *	Create a new instance of this class
	 */
	/**
	 * Constructor for ShowSearchesAction.
	 * @param searchView SearchResultsView
	 */
	public ShowSearchesAction(SearchResultsView searchView) {
		super(SearchMessages.ShowSearchesAction_label); 
		setToolTipText(SearchMessages.ShowSearchesAction_tooltip); 
		fSearchView= searchView;
	}
	 
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		QueryManager sm= InternalSearchUI.getInstance().getSearchManager();
		ISearchQuery[] queries= sm.getQueries();

		ArrayList input= new ArrayList();
		for (int j= 0; j < queries.length; j++) {
			ISearchResult search= queries[j].getSearchResult();
			input.add(search);
		}
		
		SearchesDialog dlg= new SearchesDialog(CatSearchPlugin.getActiveWorkbenchShell(),input);
		
		ISearchResult current= fSearchView.getCurrentSearchResult();
		if (current != null) {
			Object[] selected= new Object[1];
			selected[0]= current;
			dlg.setInitialSelections(selected);
		}
		if (dlg.open() == Window.OK) {
			List result= Arrays.asList(dlg.getResult());
			if (result != null && result.size() == 1) {
				fSearchView.showSearchResult((ISearchResult) result.get(0));
			}
		}

	}
}
