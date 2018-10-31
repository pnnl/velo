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

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 */
class SearchDropDownAction extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private Menu fMenu;
	
	public SearchDropDownAction() {
		setText(SearchMessages.SearchResultView_previousSearches_text); 
		setToolTipText(SearchMessages.SearchResultView_previousSearches_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_HISTORY);
		setMenuCreator(this);
	}

	/**
	 * Method dispose.
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null)  {
			fMenu.dispose();
			fMenu= null;
		}
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
		if (fMenu != null)
			fMenu.dispose();
		
		fMenu= new Menu(parent);
		boolean checkedOne= false;
		Iterator iter= SearchManager.getDefault().getPreviousSearches().iterator();
		Search selected= SearchManager.getDefault().getCurrentSearch();
		int i= 0;
		while (iter.hasNext() && i++ < RESULTS_IN_DROP_DOWN) {
			Search search= (Search)iter.next();
			ShowSearchAction action= new ShowSearchAction(search);
			action.setChecked(search.equals(selected));
			if (search.equals(selected))
				checkedOne= true;
			addActionToMenu(fMenu, action);
		}
		new MenuItem(fMenu, SWT.SEPARATOR);
		if (iter.hasNext()) {
			Action others= new ShowSearchesAction();
			others.setChecked(!checkedOne);
			addActionToMenu(fMenu, others);
		}
		addActionToMenu(fMenu, new RemoveAllSearchesAction());
		return fMenu;
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
			new ShowSearchesAction().run(true);
	}

	/**
	 * Get's rid of the menu, because the menu hangs on to 
	 * the searches, etc.
	 */
	void clear() {
		dispose();
	}
}
