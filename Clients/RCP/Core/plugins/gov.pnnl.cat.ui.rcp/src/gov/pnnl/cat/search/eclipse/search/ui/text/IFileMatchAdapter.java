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
//import org.eclipse.core.resources.IFile;
/**
 * This interface serves to map matches to <code>IFile</code> instances. Changes to those
 * files are then tracked (via the platforms file buffer mechanism) and matches
 * updated when changes are saved. Clients who want their match positions
 * automatically updated should return an implementation of
 * <code>IFileMatchAdapter</code> from the <code>getFileMatchAdapter()</code>
 * method in their search result implementation. It is assumed that the match
 * adapters are stateless, and no lifecycle management is provided.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult
 * 
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public interface IFileMatchAdapter {
	/**
	 * Returns an array with all matches contained in the given file in the
	 * given search result. If the matches are not contained within an
	 * <code>IFile</code>, this method must return an empty array.
	 * 
	 * @param result the search result to find matches in
	 * @param file the file to find matches in
	 * 
	 * @return an array of matches (possibly empty)
	 */
	//public abstract Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file);
	/**
	 * Returns the file associated with the given element (usually the file the
	 * element is contained in). If the element is not associated with a file,
	 * this method should return <code>null</code>.
	 * 
	 * @param element an element associated with a match
	 * 
	 * @return the file associated with the element or <code>null</code>
	 */
	//public abstract IFile getFile(Object element);
}
