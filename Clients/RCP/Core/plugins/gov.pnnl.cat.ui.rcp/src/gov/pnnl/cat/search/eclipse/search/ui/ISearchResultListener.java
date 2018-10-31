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
/**
 * Listener interface for changes to an <code>ISearchResult</code>.
 * Implementers of <code>ISearchResult</code> should define subclasses of 
 * <code>SearchResultEvent</code> and send those to registered listeners. Implementers of
 * <code>ISearchResultListener</code> will in general know the concrete class of search 
 * result they are listening to, and therefore the kind of events they
 * have to handle. 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public interface ISearchResultListener {
	/**
	 * Called to notify listeners of changes in a <code>ISearchResult</code>.
	 * The event object <code>e</code> is only guaranteed to be valid for
	 * the duration of the call.
	 * 
	 * @param e the event object describing the change. Note that implementers 
	 *  of <code>ISearchResult</code> will be sending subclasses of 
	 *  <code>SearchResultEvent</code>
	 */
	void searchResultChanged(SearchResultEvent e);
}
