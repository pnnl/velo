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

//import org.eclipse.core.resources.IMarker;

/**
 * Computes the key by which the markers in the search result view
 * are grouped.
 * 
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @deprecated Part of the old ('classic') search result view. Since 3.0 clients can create their own search result view pages (see {@link ISearchResultPage}), leaving it up to the page 
 * how to group search results.
 * @version $Revision: 1.0 $
 */
public interface IGroupByKeyComputer {
	
	/**
	 * Computes and returns key by which the given marker is grouped.
	 *
	 * @param	marker	the marker for which the key must be computed
	 * @return	an object that will be used as the key for that marker,
	 *			<code>null</code> if the marker seems to be invalid
	 */
	//public Object computeGroupByKey(IMarker marker);
}
