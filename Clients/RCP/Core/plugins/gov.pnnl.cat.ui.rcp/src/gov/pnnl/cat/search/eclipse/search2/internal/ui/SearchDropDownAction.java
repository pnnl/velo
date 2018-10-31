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

import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 */
class SearchDropDownAction extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private Menu fMenu;
	private SearchResultsView fSearchView;
	
	/**
	 * Constructor for SearchDropDownAction.
	 * @param searchView SearchResultsView
	 */
	public SearchDropDownAction(SearchResultsView searchView) {
		setText(SearchMessages.SearchDropDownAction_label); 
		setToolTipText(SearchMessages.SearchDropDownAction_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HISTORY);
		fSearchView= searchView;
		setMenuCreator(this);
	}

	/**
	 * Method dispose.
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		disposeMenu();
	}

	void disposeMenu() {
		if (fMenu != null)
			fMenu.dispose();
	}

	/**
	 * Method getMenu.
	 * @param parent Menu
	 * @return Menu
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	/**
	 * Method getMenu.
	 * @param parent Control
	 * @return Menu
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		ISearchQuery currentQuery= null;
		ISearchResult currentSearch= fSearchView.getCurrentSearchResult();
		if (currentSearch != null)
			currentQuery= currentSearch.getQuery();
		disposeMenu();
		
		fMenu= new Menu(parent);
		ISearchQuery[] searches= InternalSearchUI.getInstance().getSearchManager().getQueries();
		for (int i= 0; i < searches.length; i++) {
			ISearchResult search= searches[i].getSearchResult();
			String label= escapeAmp(search.getLabel());
			String tooltip= search.getTooltip();
			ImageDescriptor image= search.getImageDescriptor();
			if (InternalSearchUI.getInstance().isQueryRunning(search.getQuery()))
				label= MessageFormat.format(SearchMessages.SearchDropDownAction_running_message, new String[] { label }); 
			ShowSearchAction action= new ShowSearchAction(fSearchView, search, label, image, tooltip );
			if (searches[i].equals(currentQuery))
				action.setChecked(true);
			addActionToMenu(fMenu, action);
		}
		if (searches.length > 0) {
			new MenuItem(fMenu, SWT.SEPARATOR);
			addActionToMenu(fMenu, new RemoveAllSearchesAction());
		}
		return fMenu;
	}

	/**
	 * Method escapeAmp.
	 * @param label String
	 * @return String
	 */
	private String escapeAmp(String label) {
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < label.length(); i++) {
			char ch= label.charAt(i);
			buf.append(ch);
			if (ch == '&') {
				buf.append('&');
			}
		}
		return buf.toString();
	}

	/**
	 * Method addActionToMenu.
	 * @param parent Menu
	 * @param action Action
	 */
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		new ShowSearchesAction(fSearchView).run();
	}
}
