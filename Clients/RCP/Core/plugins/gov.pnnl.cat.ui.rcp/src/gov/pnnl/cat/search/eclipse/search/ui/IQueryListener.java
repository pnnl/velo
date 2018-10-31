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
 * <p>A listener for changes to the set of search queries. Queries are added by running
 * them via {@link gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI#runQueryInBackground(ISearchQuery) NewSearchUI#runQueryInBackground(ISearchQuery)} or 
 * {@link gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI#runQueryInForeground(org.eclipse.jface.operation.IRunnableContext,ISearchQuery) NewSearchUI#runQueryInForeground(IRunnableContext,ISearchQuery)}</p>
 * <p>The search UI determines when queries are rerun, stopped or deleted (and will notify
 * interested parties via this interface). Listeners can be added and removed in the {@link gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI NewSearchUI} class.
 * </p>
 * <p>Clients may implement this interface.</p>
 * 
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public interface IQueryListener {
	/**
	 * Called when an query has been added to the system.
	 * 
	 * @param query the query that has been added
	 */
	
	void queryAdded(ISearchQuery query);
	/**
	 * Called when a query has been removed.
	 * 
	 * @param query the query that has been removed
	 */
	void queryRemoved(ISearchQuery query);
	
	/**
	 * Called before an <code>ISearchQuery</code> is starting.
	 * @param query the query about to start
	 */
	void queryStarting(ISearchQuery query);
	
	/**
	 * Called after an <code>ISearchQuery</code> has finished.
	 * @param query the query that has finished
	 */
	void queryFinished(ISearchQuery query);
}
