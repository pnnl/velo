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
package gov.pnnl.cat.search.eclipse.search2.internal.ui;

import gov.pnnl.cat.search.eclipse.search.ui.IQueryListener;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 */
class QueryManager {
	private List fQueries;
	private List fLRU;
	private List fListeners;
	public QueryManager() {
		super();
		// an ArrayList should be plenty fast enough (few searches).
		fQueries= new ArrayList();
		fListeners= new ArrayList();
		fLRU= new ArrayList();
	}
	/**
	 * Method getQueries.
	 * @return ISearchQuery[]
	 */
	synchronized ISearchQuery[] getQueries() {
		ISearchQuery[] result= new ISearchQuery[fQueries.size()];
		return (ISearchQuery[]) fQueries.toArray(result);
	}

	/**
	 * Method removeQuery.
	 * @param query ISearchQuery
	 */
	void removeQuery(ISearchQuery query) {
		synchronized (fQueries) {
			fQueries.remove(query);
			fLRU.remove(query);
		}
		fireRemoved(query);
	}

	/**
	 * Method addQuery.
	 * @param query ISearchQuery
	 */
	void addQuery(ISearchQuery query) {
		synchronized (fQueries) {
			if (fQueries.contains(query))
				return;
			fQueries.add(0, query);
			fLRU.add(0, query);
		}
		fireAdded(query);
	}
	
	/**
	 * Method addQueryListener.
	 * @param l IQueryListener
	 */
	void addQueryListener(IQueryListener l) {
		synchronized (fListeners) {
			fListeners.add(l);
		}
	}

	/**
	 * Method removeQueryListener.
	 * @param l IQueryListener
	 */
	void removeQueryListener(IQueryListener l) {
		synchronized (fListeners) {
			fListeners.remove(l);
		}
	}
	
	/**
	 * Method fireAdded.
	 * @param query ISearchQuery
	 */
	void fireAdded(ISearchQuery query) {
		Set copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= (IQueryListener) listeners.next();
			l.queryAdded(query);
		}
	}

	/**
	 * Method fireRemoved.
	 * @param query ISearchQuery
	 */
	void fireRemoved(ISearchQuery query) {
		Set copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= (IQueryListener) listeners.next();
			l.queryRemoved(query);
		}
	}
	
	/**
	 * Method fireStarting.
	 * @param query ISearchQuery
	 */
	void fireStarting(ISearchQuery query) {
		Set copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= (IQueryListener) listeners.next();
			l.queryStarting(query);
		}
	}

	/**
	 * Method fireFinished.
	 * @param query ISearchQuery
	 */
	void fireFinished(ISearchQuery query) {
		Set copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= (IQueryListener) listeners.next();
			l.queryFinished(query);
		}
	}

	void removeAll() {
		Set copiedSearches= new HashSet();
		synchronized (fQueries) {
			copiedSearches.addAll(fQueries);
			fQueries.clear();
			fLRU.clear();
			Iterator iter= copiedSearches.iterator();
			while (iter.hasNext()) {
				ISearchQuery element= (ISearchQuery) iter.next();
				fireRemoved(element);
			}
		}
	}

	/**
	 * Method queryFinished.
	 * @param query ISearchQuery
	 */
	void queryFinished(ISearchQuery query) {
		fireFinished(query);
	}

	/**
	 * Method queryStarting.
	 * @param query ISearchQuery
	 */
	void queryStarting(ISearchQuery query) {
		fireStarting(query);
	}
	
	/**
	 * Method touch.
	 * @param query ISearchQuery
	 */
	void touch(ISearchQuery query) {
		if (fLRU.contains(query)) {
			fLRU.remove(query);
			fLRU.add(0, query);
		}
	}
	
	/**
	 * Method getOldestQuery.
	 * @return ISearchQuery
	 */
	ISearchQuery getOldestQuery() {
		if (fLRU.size() > 0)
			return (ISearchQuery) fLRU.get(fLRU.size()-1);
		return null;
	}

}
