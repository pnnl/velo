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
package gov.pnnl.cat.search.eclipse.search.internal.ui;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ExceptionHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Manage search results
 * @version $Revision: 1.0 $
 */
public class SearchManager implements IResourceEventListener {

	static final SearchManager fgDefault= new SearchManager();
  protected static Logger logger = CatLogger.getLogger(SearchManager.class);
  
	Search fCurrentSearch= null;
	
	private SearchManager() {
    try {
      ResourcesPlugin.getResourceManager().addResourceEventListener(this);
    } catch (Exception e) {
      //EZLogger.logError(e, "Error registering resource listener on SearchManager");
      logger.error("Error registering resource listener on SearchManger", e);
    }
	}
	
	private HashSet fListeners= new HashSet();
	private LinkedList fPreviousSearches= new LinkedList();
//	private boolean fIsRemoveAll= false;
	
	/**
	 * Method getDefault.
	 * @return SearchManager
	 */
	public static SearchManager getDefault() {
		return fgDefault;
	}
	
	/**
	 * Returns the list with previous searches (ISearch).
	 * @return LinkedList
	 */
	LinkedList getPreviousSearches() {
		return fPreviousSearches;
	}
	/**
	 * Returns the list with current (last) results
	 * @return ArrayList
	 */
	ArrayList getCurrentResults() {
		if (fCurrentSearch == null)
			return new ArrayList(0);
		else
			return (ArrayList)fCurrentSearch.getResults();
	}

	/**
	 * Method getCurrentSearch.
	 * @return Search
	 */
	public Search getCurrentSearch() {
		return fCurrentSearch;
	}

	void removeAllSearches() {

		// clear searches
		fPreviousSearches= new LinkedList();
		fCurrentSearch= null;

		// update viewers
		Iterator iter= fListeners.iterator();
		while (iter.hasNext()) {
			SearchResultViewer viewer= (SearchResultViewer)iter.next();
			handleAllSearchesRemoved(viewer);
		}
	}

	/**
	 * Method handleAllSearchesRemoved.
	 * @param viewer SearchResultViewer
	 */
	private void handleAllSearchesRemoved(SearchResultViewer viewer) {
		viewer.handleAllSearchesRemoved();
	}

	/**
	 * Method setCurrentSearch.
	 * @param search Search
	 */
	void setCurrentSearch(final Search search) {
		if (fCurrentSearch == search)
			return;
			
		IRunnableWithProgress op= new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				internalSetCurrentSearch(search, monitor);
			}
		};
		
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(true, true, op);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_switchSearch_title, SearchMessages.Search_Error_switchSearch_message); 
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		} 
		
		getPreviousSearches().remove(search);
		getPreviousSearches().addFirst(search);
	}

	/**
	 * Method internalSetCurrentSearch.
	 * @param search Search
	 * @param monitor IProgressMonitor
	 */
	void internalSetCurrentSearch(final Search search, IProgressMonitor monitor) {
		if (fCurrentSearch != null)
			fCurrentSearch.backupMarkers();
				
		final Search previousSearch= fCurrentSearch;
		fCurrentSearch= search;
		monitor.beginTask(SearchMessages.SearchManager_updating, getCurrentResults().size() + 20); 
		
		// remove current search markers (I deleted this block of code)
		monitor.worked(10);

		// add search markers
		Iterator iter= getCurrentResults().iterator();
		ArrayList emptyEntries= new ArrayList(10);
		boolean filesChanged= false;
		boolean filesDeleted= false;
//		IGroupByKeyComputer groupByKeyComputer= getCurrentSearch().getGroupByKeyComputer();
		while (iter.hasNext()) {
			monitor.worked(1);
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
//			Iterator attrPerMarkerIter= entry.getAttributesPerMarker().iterator();
			entry.clearMarkerList();
			if (entry.getResource() == null) {
				emptyEntries.add(entry);
				filesDeleted= true;
				continue;
			}
      /*  TODO: PROBABLY DELETE THIS SECTION AS WE ARE NOT USING MARKERS ANYMORE
			while (attrPerMarkerIter.hasNext()) {
				IMarker newMarker= null;
				try {
					newMarker= entry.getResource().createMarker(entry.getMarkerType());
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchMessages.Search_Error_createMarker_title, SearchMessages.Search_Error_createMarker_message); 
					continue;
				}
				try {
					newMarker.setAttributes((Map)attrPerMarkerIter.next());
					if (groupByKeyComputer !=null && groupByKeyComputer.computeGroupByKey(newMarker) == null) {
						filesDeleted= true;						
						newMarker.delete();
						continue;
					}
				} catch (CoreException ex) {
					ExceptionHandler.handle(ex, SearchMessages.Search_Error_markerAttributeAccess_title, SearchMessages.Search_Error_markerAttributeAccess_message); 
				}
				entry.add(newMarker);
			}
      */
			if (entry.getMatchCount() == 0)
				emptyEntries.add(entry);
      /* TODO: Fix this after we add a modification stamp method to our IResource
			else if (!filesChanged && entry.getResource().getModificationStamp() != entry.getModificationStamp())
				filesChanged= true;
      */
		}
		getCurrentResults().removeAll(emptyEntries);
		monitor.worked(10);
		
		String warningMessage= null;
		Display display= getDisplay();
		
		if (filesChanged)
			warningMessage= SearchMessages.SearchManager_resourceChanged; 
		if (filesDeleted) {
			if (warningMessage == null)
				warningMessage= ""; //$NON-NLS-1$
			else
				warningMessage += "\n";			 //$NON-NLS-1$
			warningMessage += SearchMessages.SearchManager_resourceDeleted; 
		}
		if (warningMessage != null) {
			if (display != null && !display.isDisposed()) {
				final String warningTitle= SearchMessages.SearchManager_resourceChangedWarning; 
				final String warningMsg= warningMessage;
				display.syncExec(new Runnable() {
					public void run() {
						MessageDialog.openWarning(getShell(), warningTitle, warningMsg);
					}
				});
			}
		}
			
		// update viewers
		iter= fListeners.iterator();
		if (display != null && !display.isDisposed()) {
			final Viewer visibleViewer= ((SearchResultView)CatSearchPlugin.getSearchResultView()).getViewer();
			while (iter.hasNext()) {
				final SearchResultViewer viewer= (SearchResultViewer)iter.next();
				display.syncExec(new Runnable() {
					public void run() {
						if (previousSearch != null && viewer == visibleViewer)
							previousSearch.setSelection(viewer.getSelection());
						viewer.setInput(null);
						viewer.setPageId(search.getPageId());
						viewer.setGotoMarkerAction(search.getGotoMarkerAction());
						viewer.setContextMenuTarget(search.getContextMenuContributor());
						viewer.setActionGroupFactory(null);
						viewer.setInput(getCurrentResults());
						viewer.setActionGroupFactory(search.getActionGroupFactory());
						viewer.setSelection(fCurrentSearch.getSelection(), true);
					}
				});
			}
		}
		monitor.done();
	}

	/**
	 * Returns the number of matches
	 * @return int
	 */
	int getCurrentItemCount() {
		if (fCurrentSearch != null)
			return fCurrentSearch.getItemCount();
		else
			return 0;
	}

  // TODO: probably delete this method as we are removing this option from the search gui
	void removeAllResults() {
//		fIsRemoveAll= true;
    /*
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message); 
			fIsRemoveAll= false;
		}
    */
	}

	/**
	 * Method addNewSearch.
	 * @param newSearch Search
	 */
	void addNewSearch(final Search newSearch) {
		
    try {
      ResourcesPlugin.getResourceManager().removeResourceEventListener(this);
    } catch(Exception e) {
      //EZLogger.logError(e, "Error unregistering resource listener");
      logger.error("Error unregistering resource listener", e);
    }
      
		// Clear the viewers
		Iterator iter= fListeners.iterator();
		Display display= getDisplay();
		if (display != null && !display.isDisposed()) {
			final Viewer visibleViewer= ((SearchResultView)CatSearchPlugin.getSearchResultView()).getViewer();
			while (iter.hasNext()) {
				final SearchResultViewer viewer= (SearchResultViewer)iter.next();
				display.syncExec(new Runnable() {
					public void run() {
						if (fCurrentSearch != null && viewer == visibleViewer)
							fCurrentSearch.setSelection(viewer.getSelection());
						setNewSearch(viewer, newSearch);
					}
				});
			}
		}
		
		if (fCurrentSearch != null) {
			if (fCurrentSearch.isSameSearch(newSearch))
				getPreviousSearches().remove(fCurrentSearch);
			else
				fCurrentSearch.backupMarkers();
		}
		fCurrentSearch= newSearch;
		getPreviousSearches().addFirst(fCurrentSearch);
		
		// Remove the markers
    /*
		try {
			SearchPlugin.getWorkspace().getRoot().deleteMarkers(SearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message); 
		}
    */
	}

	/**
	 * Method searchFinished.
	 * @param results ArrayList
	 */
	void searchFinished(ArrayList results) {
		Assert.isNotNull(results);
		getCurrentSearch().setResults(results);

		Display display= getDisplay();
		if (display == null || display.isDisposed())
			return;
		
		if (Thread.currentThread() == display.getThread())
			handleNewSearchResult();
		else {
			display.syncExec(new Runnable() {
				public void run() {
					handleNewSearchResult();
				}
			});
		}
		try {
		  ResourcesPlugin.getResourceManager().addResourceEventListener(this);
    } catch(Exception e){
      //EZLogger.logError(e, "error registering resource listener.");
      logger.error("error registering resource listener",e);
    }
	}
	
	//--- Change event handling -------------------------------------------------

	/**
	 * Method addSearchChangeListener.
	 * @param viewer SearchResultViewer
	 */
	void addSearchChangeListener(SearchResultViewer viewer) {
		fListeners.add(viewer);
	}

	/**
	 * Method removeSearchChangeListener.
	 * @param viewer SearchResultViewer
	 */
	void removeSearchChangeListener(SearchResultViewer viewer) {
		Assert.isNotNull(viewer);
		fListeners.remove(viewer);
	}

  /*
	private final void handleSearchMarkersChanged(IMarkerDelta[] markerDeltas) {
		if (fIsRemoveAll) {
			handleRemoveAll();
			fIsRemoveAll= false;
			return;
		}

		Iterator iter= fListeners.iterator();
		while (iter.hasNext())
			((SearchResultViewer)iter.next()).getControl().setRedraw(false);
	
		for (int i=0; i < markerDeltas.length; i++) {
			handleSearchMarkerChanged(markerDeltas[i]);
		}

		iter= fListeners.iterator();
		while (iter.hasNext())
			((SearchResultViewer)iter.next()).getControl().setRedraw(true);

	}

	private void handleSearchMarkerChanged(IMarkerDelta markerDelta) {
		int kind= markerDelta.getKind();
		// don't listen for adds will be done by ISearchResultView.addMatch(...)
		if (((kind & IResourceDelta.REMOVED) != 0))
			handleRemoveMatch(markerDelta.getMarker());
		else if ((kind & IResourceDelta.CHANGED) != 0)
			handleUpdateMatch(markerDelta.getMarker());
	}
*/
  
//	private void handleRemoveAll() {
//		if (fCurrentSearch != null)
//			fCurrentSearch.removeResults();
//		Iterator iter= fListeners.iterator();
//		while (iter.hasNext())
//			((SearchResultViewer)iter.next()).handleRemoveAll();
//	}
	
	private void handleNewSearchResult() {
		Iterator iter= fListeners.iterator();
		while (iter.hasNext()) {
			SearchResultViewer viewer= (SearchResultViewer)iter.next();
			viewer.setInput(getCurrentResults());
		}
	}
	
	/**
	 * Method setNewSearch.
	 * @param viewer SearchResultViewer
	 * @param search Search
	 */
	private void setNewSearch(SearchResultViewer viewer, Search search) {
		viewer.setInput(null);
		viewer.clearTitle();
		viewer.setPageId(search.getPageId());
		viewer.setGotoMarkerAction(search.getGotoMarkerAction());
		viewer.setContextMenuTarget(search.getContextMenuContributor());
		viewer.setActionGroupFactory(search.getActionGroupFactory());
	}
  
	/* TODO: I think we may be able to delete this since we won't be allowing the option to remove a match 
	private void handleRemoveMatch(IMarker marker) {
		SearchResultViewEntry entry= findEntry(marker);
		if (entry != null) {
			entry.remove(marker);
			if (entry.getMatchCount() == 0) {
				getCurrentResults().remove(entry);
				Iterator iter= fListeners.iterator();
				while (iter.hasNext())
					((SearchResultViewer)iter.next()).handleRemoveMatch(entry);
			}
			else {
				Iterator iter= fListeners.iterator();
				while (iter.hasNext())
					((SearchResultViewer)iter.next()).handleUpdateMatch(entry, true);
			}
		}
	}
  
	private void handleUpdateMatch(IMarker marker) {
		SearchResultViewEntry entry= findEntry(marker);
		if (entry != null) {
			Iterator iter= fListeners.iterator();
			while (iter.hasNext())
				((SearchResultViewer)iter.next()).handleUpdateMatch(entry, false);
		}
	}

	private SearchResultViewEntry findEntry(IMarker marker) {
		Iterator entries= getCurrentResults().iterator();
		while (entries.hasNext()) {
			SearchResultViewEntry entry= (SearchResultViewEntry)entries.next();
			if (entry.contains(marker))
				return entry;
		}
		return null;
	}
*/
	/**
	 * Received a resource event. Since the delta could be created in a 
	 * separate thread this methods post the event into the viewer's 
	 * display thread.
	 * @param event IBatchNotification
	 * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
	 */
	public final void onEvent(final IBatchNotification event) {
		if (event == null)
			return;
/*  TODO: need to revise this logic
		final IMarkerDelta[] markerDeltas= event.findMarkerDeltas(SearchUI.SEARCH_MARKER, true);
		if (markerDeltas == null || markerDeltas.length < 1)
			return;

		Display display= getDisplay();
		if (display == null || display.isDisposed())
			return;

		Runnable runnable= new Runnable() {
			public void run() {
				if (getCurrentSearch() != null) {
					handleSearchMarkersChanged(markerDeltas);
					// update title and actions
					Iterator iter= fListeners.iterator();
					while (iter.hasNext()) {
						SearchResultViewer viewer= (SearchResultViewer)iter.next();
						viewer.enableActions();
						viewer.updateTitle();
					}
				}
			}
		};
		display.syncExec(runnable);	
    */
	}
	/**
	 * Find and return a valid display
	 * @return Display
	 */
	private Display getDisplay() {
		Iterator iter= fListeners.iterator();
		while (iter.hasNext()) {
			Control control= ((Viewer)iter.next()).getControl();
			if (control != null && !control.isDisposed()) {
				Display display= control.getDisplay();
				if (display != null && !display.isDisposed())
					return display;
			}
		}
		return null;
	}
	/**
	 * Find and return a valid shell
	 * @return Shell
	 */
	private Shell getShell() {
		return CatSearchPlugin.getActiveWorkbenchShell();
	}

  /**
   * Method cacheCleared.
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }
}

