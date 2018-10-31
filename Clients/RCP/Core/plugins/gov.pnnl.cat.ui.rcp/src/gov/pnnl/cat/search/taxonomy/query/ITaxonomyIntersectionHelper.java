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
package gov.pnnl.cat.search.taxonomy.query;

import gov.pnnl.cat.core.resources.ResourceException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A helper class to assist the TaxonomyIntersectionQuery.
 * This class will typically perform interaction with the UI, if necessary, then
 * update the ITaxonomyIntersectionQuery with the taxonomies on which to operate
 * via ITaxonomyIntersectionQuery.setTaxonomies().
 * 
 * An example implementation of this is getting the input from a viewer, parsing
 * the input into the format required by the query, and then updating the query.
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public interface ITaxonomyIntersectionHelper {
	/**
	 * Method prepareSearch.
	 * @param monitor IProgressMonitor
	 * @param query ITaxonomyIntersectionQuery
	 * @throws ResourceException
	 */
	public void prepareSearch(IProgressMonitor monitor, ITaxonomyIntersectionQuery query) throws ResourceException;
}
