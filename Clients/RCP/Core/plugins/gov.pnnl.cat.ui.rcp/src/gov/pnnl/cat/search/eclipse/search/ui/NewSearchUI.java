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

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.OpenSearchDialogAction;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPreferencePage;
import gov.pnnl.cat.search.eclipse.search2.internal.ui.InternalSearchUI;
import gov.pnnl.cat.search.eclipse.search2.internal.ui.SearchMessages;
import gov.pnnl.cat.ui.rcp.CatViewIDs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.IWorkbenchWindow;
/**
 * A facade for access to the new search UI facilities.
 * 
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public class NewSearchUI {
	/**
	 * Activates a search result view in the current workbench window page. If a
	 * search view is already open in the current workbench window page, it is
	 * activated. Otherwise a new search view is opened and activated.
	 * 
	
	 * @return the activate search result view or <code>null</code> if the
	 *         search result view couldn't be activated */
	public static ISearchResultViewPart activateSearchResultView() {
		return InternalSearchUI.getInstance().activateSearchResultsView();
	}
	/**
	 * Gets the search result view shown in the current workbench window.
	 * 
	
	 * @return the search result view or <code>null</code>, if none is open
	 *         in the current workbench window page */
	public static ISearchResultViewPart getSearchResultView() {
		return InternalSearchUI.getInstance().getSearchResultsView();
	}
	/**
	 * Runs the given search query. This method may run the given query in a
	 * separate thread if <code>ISearchQuery#canRunInBackground()</code>
	 * returns <code>true</code>. Running a query adds it to the set of known
	 * queries and notifies any registered <code>IQueryListener</code>s about
	 * the addition.
	 * 
	 * @param query
	 *            the query to execute
	 * @deprecated deprecated in 3.1.
	 * Use {@link #runQueryInBackground(ISearchQuery)} to run a query in background
	 * or {@link #runQueryInForeground(IRunnableContext, ISearchQuery)} to run it in foreground
	 */
	public static void runQuery(ISearchQuery query) {
		if (query.canRunInBackground())
			runQueryInBackground(query);
		else {
			IStatus status= runQueryInForeground(null, query);
			if (status != null) {
				if (!status.isOK())
					CatSearchPlugin.log(status);
				if (status.getSeverity() == IStatus.ERROR) {
					ErrorDialog.openError(CatSearchPlugin.getActiveWorkbenchShell(), SearchMessages.NewSearchUI_error_title, SearchMessages.NewSearchUI_error_label, status); 
				}
			}
		}
	}
	/**
	 * Runs the given search query. This method will execute the query in a
	 * background thread and not block until the search is finished. 
	 * Running a query adds it to the set of known queries and notifies
	 * any registered <code>IQueryListener</code>s about the addition.
	 * 
	 * @param query
	 *            the query to execute. The query must be able to run in background, that means
	 *            {@link ISearchQuery#canRunInBackground()} must be <code>true</code>
	
	 * @since 3.1
	 * @throws IllegalArgumentException Thrown when the passed query is not able to run in background */
	public static void runQueryInBackground(ISearchQuery query) throws IllegalArgumentException {
		if (query.canRunInBackground())
			InternalSearchUI.getInstance().runSearchInBackground(query);
		else
			throw new IllegalArgumentException("Query can not be run in background"); //$NON-NLS-1$
	}
	
	/**
	 * Runs the given search query. This method will execute the query in the
	 * same thread as the caller. This method blocks until the query is
	 * finished. Running a query adds it to the set of known queries and notifies
	 * any registered <code>IQueryListener</code>s about the addition.
	 * 
	 * @param context
	 *            the runnable context to run the query in
	 * @param query
	 *            the query to execute
	
	 * @return a status indicating whether the query ran correctly, including {@link IStatus#CANCEL} to signal
	 *            that the query was canceled. */
	public static IStatus runQueryInForeground(IRunnableContext context, ISearchQuery query) {
		return InternalSearchUI.getInstance().runSearchInForeground(context, query);
	}
	/**
	 * Registers the given listener to receive notification of changes to
	 * queries. The listener will be notified whenever a query has been added,
	 * removed, is starting or has finished. Has no effect if an identical
	 * listener is already registered.
	 * 
	 * @param l
	 *            the listener to be added
	 */
	public static void addQueryListener(IQueryListener l) {
		InternalSearchUI.getInstance().addQueryListener(l);
	}
	/**
	 * Removes the given query listener. Does nothing if the listener is not
	 * present.
	 * 
	 * @param l
	 *            the listener to be removed.
	 */
	public static void removeQueryListener(IQueryListener l) {
		InternalSearchUI.getInstance().removeQueryListener(l);
	}
	/**
	 * Returns all search queries know to the search UI (i.e. registered via
	 * <code>runQuery()</code> or <code>runQueryInForeground())</code>.
	 * 
	
	 * @return all search results */
	public static ISearchQuery[] getQueries() {
		return InternalSearchUI.getInstance().getQueries();
	}
	
	/**
	 * Returns whether the given query is currently running. Queries may be run
	 * by client request or by actions in the search UI.
	 * 
	 * @param query
	 *            the query
	
	
	
	 * @return whether the given query is currently running * @see NewSearchUI#runQueryInBackground(ISearchQuery) * @see NewSearchUI#runQueryInForeground(IRunnableContext, ISearchQuery) */
	public static boolean isQueryRunning(ISearchQuery query) {
		return InternalSearchUI.getInstance().isQueryRunning(query);
	}
	
	/**
	 * Sends a 'cancel' command to the given query running in background.
	 * The call has no effect if the query is not running, not in background or is not cancelable. 
	 * 
	 * @param query
	 *            the query
	 * @since 3.1
	 */
	public static void cancelQuery(ISearchQuery query) {
		InternalSearchUI.getInstance().cancelSearch(query);
	}
	
	/**
	 * Search Plug-in Id (value <code>"org.eclipse.search"</code>).
	 */
	public static final String PLUGIN_ID= "gov.pnnl.cat.ui.rcp";

	public static final String SEARCH_MARKER=  PLUGIN_ID + ".searchmarker";

	public static final String SEARCH_VIEW_ID = CatViewIDs.SEARCH;
	
	public static final String SEARCH_RESULTS_VIEW_ID = CatViewIDs.SEARCH_RESULTS;
	
	/**
	 * Id of the Search action set
	 * (value <code>"org.eclipse.search.searchActionSet"</code>).
	 */
	public static final String ACTION_SET_ID = PLUGIN_ID + ".searchActionSet"; 

	/**
	 * Opens the search dialog.
	 * If <code>pageId</code> is specified and a corresponding page
	 * is found then it is brought to top.
	 * @param window 	the parent window
	 *
	 * @param pageId	the page to select or <code>null</code>
	 * 					if the best fitting page should be selected
	 */
	public static void openSearchDialog(IWorkbenchWindow window, String pageId) {
		new OpenSearchDialogAction(window, pageId).run();
	}		

	/**
	 * Returns the preference whether editors should be reused
	 * when showing search results.
	 * 
	 * The goto action can decide to use or ignore this preference.
	 *
	
	 * @return <code>true</code> if editors should be reused for showing search results */
	public static boolean reuseEditor() {
		return SearchPreferencePage.isEditorReused();
	}

	/**
	 * Returns the preference whether a search engine is
	 * allowed to report potential matches or not.
	 * <p>
	 * Search engines which can report inexact matches must
	 * respect this preference i.e. they should not report
	 * inexact matches if this method returns <code>true</code>
	 * </p>
	
	 * @return <code>true</code> if search engine must not report inexact matches */
	public static boolean arePotentialMatchesIgnored() {
		return SearchPreferencePage.arePotentialMatchesIgnored();
	}

	/**
	 * Returns the ID of the default perspective.
	 * <p>
	 * The perspective with this ID will be used to show the Search view.
	 * If no default perspective is set then the Search view will
	 * appear in the current perspective.
	 * </p>
	
	 * @return the ID of the default perspective <code>null</code> if no default perspective is set */
	public static String getDefaultPerspectiveId() {
		return SearchPreferencePage.getDefaultPerspectiveId();
	}

}
