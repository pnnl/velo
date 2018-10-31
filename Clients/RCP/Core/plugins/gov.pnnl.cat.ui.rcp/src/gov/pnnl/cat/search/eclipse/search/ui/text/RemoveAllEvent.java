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
package gov.pnnl.cat.search.eclipse.search.ui.text;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.SearchResultEvent;
/**
 * An event indicating that all matches have been removed from a <code>AbstractTextSearchResult</code>.
 * 
 * <p>
 * Clients may instantiate or subclass this class.
 * </p>
 * 
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public class RemoveAllEvent extends SearchResultEvent {
	private static final long serialVersionUID = 6009335074727417445L;
	/**
	 * A constructor
	 * @param searchResult the search result this event is about
	 */
	public RemoveAllEvent(ISearchResult searchResult) {
		super(searchResult);
	}
}
