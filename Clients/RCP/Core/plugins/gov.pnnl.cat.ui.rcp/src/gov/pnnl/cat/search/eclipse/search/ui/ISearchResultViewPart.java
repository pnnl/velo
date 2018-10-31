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
package gov.pnnl.cat.search.eclipse.search.ui;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IViewPart;
/**
 * <p>
 * Interface for the search result view. The search result view is responsible
 * for managing the set of search result and delegates display of search results
 * to the appropriate <code>ISearchResultPage</code>. Clients may access the
 * search result view via the <code>NewSearchUI</code> facade class.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients
 * </p>
 * 
 * @see NewSearchUI#activateSearchResultView()
 * @see NewSearchUI#getSearchResultView()
 * 
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public interface ISearchResultViewPart extends IViewPart {
	/**
	 * Search result pages should call this method to have the search results
	 * view contribute to their context menus.
	 * 
	 * @param menuManager
	 *            the menu manager the search result view should contribute to
	 */
	void fillContextMenu(IMenuManager menuManager);
	/**
	 * Returns the <code>ISearchResultPage</code> currently shown in this
	 * search view. Returns <code>null</code> if no page is currently shown.
	 * 
	
	 * @return the active <code>ISearchResultPage</code> or <code>null</code> */
	ISearchResultPage getActivePage();
	
	/**
	 * Requests that the search view updates the label it is showing for search result
	 * pages. Typically, a search result page will call this method when the search result
	 * it's displaying is updated.
	
	 * @see ISearchResultPage#getLabel() */
	void updateLabel();

	/**
	 * Returns the <code>ISearchResult</code> currently displayed.
	 * Added to interface July 2008 by Eric Marshall.
	 * 
	
	 * @return the current search result */
	public ISearchResult getCurrentSearchResult();
}
