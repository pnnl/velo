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

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Implementors of this interface represent the result of a search. How the
 * results of a search are structured is up to the implementor of this
 * interface. The abstract base implementation provided with
 * {@link gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult AbstractTextSearchResult}
 * uses a flat list of matches to represent the result of a search. Subclasses
 * of <code>SearchResultEvent</code> can be used in order to notify listeners
 * of search result changes.
 * <p>
 * To present search results to the user implementors of this interface must
 * also provide an extension for the extension point
 * <code>org.eclipse.search.searchResultViewPage</code>.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public interface ISearchResult {
	/**
	 * Adds a <code>ISearchResultListener</code>. Has no effect when the
	 * listener has already been added.
	 * 
	 * @param l the listener to be added
	 */
	void addListener(ISearchResultListener l);
	/**
	 * Removes a <code>ISearchResultChangedListener</code>. Has no effect
	 * when the listener hasn't previously been added.
	 * 
	 * @param l the listener to be removed
	 */
	void removeListener(ISearchResultListener l);
	/**
	 * Returns a user readable label for this search result. The label is typically used in the result
	 * view and should contain the search query string and number of matches.
	 * 
	
	 * @return the label for this search result */
	String getLabel();
	/**
	 * Returns a tooltip to be used when this search result is shown in the UI.
	 * 
	
	 * @return a user readable String */
	String getTooltip();
	/**
	 * Returns an image descriptor for the given ISearchResult.
	 * 
	
	 * @return an image representing this search result or <code>null</code> */
	ImageDescriptor getImageDescriptor();
	/**
	 * Returns the query that produced this search result.
	 * 
	
	 * @return the query producing this result */
	ISearchQuery getQuery();
}
