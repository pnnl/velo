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

import java.util.EventObject;

/**
 * The common superclass of all events sent from <code>ISearchResults</code>.
 * This class is supposed to be subclassed to provide more specific
 * notification.
 * 
 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultListener#searchResultChanged(SearchResultEvent)
 * 
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public abstract class SearchResultEvent extends EventObject {
	/**
	 * Creates a new search result event for the given search result.
	 * 
	 * @param searchResult the source of the event
	 */
	protected SearchResultEvent(ISearchResult searchResult) {
		super(searchResult);
	}
	/**
	 * Gets the <code>ISearchResult</code> for this event.
	 * 
	
	 * @return the source of this event */
	public ISearchResult getSearchResult() {
		return (ISearchResult) getSource();
	}
}
