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

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPluginImages;
import gov.pnnl.cat.search.eclipse.search.ui.IQueryListener;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

//import org.eclipse.search2.internal.ui.text.PositionTracker;

/**
 */
public class InternalSearchUI {
	private static final int HISTORY_COUNT= 10;
	
	//The shared instance.
	private static InternalSearchUI fgInstance;
	
	// contains all running jobs
	private HashMap fSearchJobs;
	
	private QueryManager fSearchResultsManager;
	//private PositionTracker fPositionTracker;

	public static final Object FAMILY_SEARCH = new Object();
	

	/**
	 */
	private class SearchJobRecord {
		public ISearchQuery query;
		public Job job;
		public boolean background;
		public boolean isRunning;

		/**
		 * Constructor for SearchJobRecord.
		 * @param job ISearchQuery
		 * @param bg boolean
		 */
		public SearchJobRecord(ISearchQuery job, boolean bg) {
			this.query= job;
			this.background= bg;
			this.isRunning= false;
			this.job= null;
		}
	}
	

	/**
	 */
	private class InternalSearchJob extends Job {
		
		private SearchJobRecord fSearchJobRecord;
		
		/**
		 * Constructor for InternalSearchJob.
		 * @param sjr SearchJobRecord
		 */
		public InternalSearchJob(SearchJobRecord sjr) {
			super(sjr.query.getLabel());
			
			fSearchJobRecord= sjr;
		}
		
		/**
		 * Method run.
		 * @param monitor IProgressMonitor
		 * @return IStatus
		 */
		protected IStatus run(IProgressMonitor monitor) {
			ThrottlingProgressMonitor realMonitor= new ThrottlingProgressMonitor(monitor, 0.5f);
			fSearchJobRecord.job= this;
			searchJobStarted(fSearchJobRecord);
			IStatus status= null;
			try{
				status= fSearchJobRecord.query.run(realMonitor); 
			} finally {
				searchJobFinished(fSearchJobRecord);
			}
			fSearchJobRecord.job= null;
			return status;
		}
		/**
		 * Method belongsTo.
		 * @param family Object
		 * @return boolean
		 */
		public boolean belongsTo(Object family) {
			return family == InternalSearchUI.FAMILY_SEARCH;
		}

	}

	/**
	 * Method searchJobStarted.
	 * @param record SearchJobRecord
	 */
	private void searchJobStarted(SearchJobRecord record) {
		record.isRunning= true;
		getSearchManager().queryStarting(record.query);
	}
	
	/**
	 * Method searchJobFinished.
	 * @param record SearchJobRecord
	 */
	private void searchJobFinished(SearchJobRecord record) {
		record.isRunning= false;
		fSearchJobs.remove(record);
		getSearchManager().queryFinished(record.query);
	}
	
	/**
	 * The constructor.
	 */
	public InternalSearchUI() {
		fgInstance= this;
		fSearchJobs= new HashMap();
		fSearchResultsManager= new QueryManager();
		//fPositionTracker= new PositionTracker();
		PlatformUI.getWorkbench().getProgressService().registerIconForFamily(SearchPluginImages.DESC_VIEW_SEARCHRES, FAMILY_SEARCH);
	}

	/**
	
	 * @return returns the shared instance. */
	public static InternalSearchUI getInstance() {
		if (fgInstance ==null)
			fgInstance= new InternalSearchUI();
		return fgInstance;
	}

	/**
	 * Method getSearchResultsView.
	 * @return ISearchResultViewPart
	 */
	public ISearchResultViewPart getSearchResultsView() {
		return (ISearchResultViewPart) CatSearchPlugin.getActivePage().findView(NewSearchUI.SEARCH_RESULTS_VIEW_ID);
	}

	/**
	 * Method getProgressService.
	 * @return IWorkbenchSiteProgressService
	 */
	private IWorkbenchSiteProgressService getProgressService() {
		ISearchResultViewPart view= getSearchResultsView();
		if (view != null) {
			IWorkbenchPartSite site= view.getSite();
			if (site != null)
				return (IWorkbenchSiteProgressService)view.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		}
		return null;
	}
	
	/**
	 * Method runSearchInBackground.
	 * @param query ISearchQuery
	 * @return boolean
	 */
	public boolean runSearchInBackground(ISearchQuery query) {
		if (isQueryRunning(query))
			return false;
				
		addQuery(query);

		SearchJobRecord sjr= new SearchJobRecord(query, true);
		fSearchJobs.put(query, sjr);
				
		Job job= new InternalSearchJob(sjr);
		job.setPriority(Job.BUILD);	
		job.setUser(true);

		IWorkbenchSiteProgressService service= getProgressService();
		if (service != null) {
			service.schedule(job, 0, true);
		} else {
			job.schedule();
		}
		return true;
	}

	/**
	 * Method isQueryRunning.
	 * @param query ISearchQuery
	 * @return boolean
	 */
	public boolean isQueryRunning(ISearchQuery query) {
		SearchJobRecord sjr= (SearchJobRecord) fSearchJobs.get(query);
		return sjr != null && sjr.isRunning;
	}

	/**
	 * Method runSearchInForeground.
	 * @param context IRunnableContext
	 * @param query ISearchQuery
	 * @return IStatus
	 */
	public IStatus runSearchInForeground(IRunnableContext context, final ISearchQuery query) {
		if (isQueryRunning(query)) {
			return Status.CANCEL_STATUS;
		}

		addQuery(query);
		
		SearchJobRecord sjr= new SearchJobRecord(query, false);
		fSearchJobs.put(query, sjr);
		
		if (context == null)
			context= new ProgressMonitorDialog(null);
		
		return doRunSearchInForeground(sjr, context);
	}
	
	/**
	 * Method doRunSearchInForeground.
	 * @param rec SearchJobRecord
	 * @param context IRunnableContext
	 * @return IStatus
	 */
	private IStatus doRunSearchInForeground(final SearchJobRecord rec, IRunnableContext context) {
		try {
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					searchJobStarted(rec);
					try { 
						IStatus status= rec.query.run(monitor);
						if (status.matches(IStatus.CANCEL)) {
							throw new InterruptedException();
						}
						if (!status.isOK()) {
							throw new InvocationTargetException(new CoreException(status));
						}
					} catch (OperationCanceledException e) {
						throw new InterruptedException();
					} finally {
						searchJobFinished(rec);
					}
				}
			});
		} catch (InvocationTargetException e) {
			Throwable innerException= e.getTargetException();
			if (innerException instanceof CoreException) {
				return ((CoreException) innerException).getStatus();
			}
			return new Status(IStatus.ERROR, CatSearchPlugin.getID(), 0, SearchMessages.InternalSearchUI_error_unexpected, innerException);  
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	public static void shutdown() {
		InternalSearchUI instance= fgInstance;
		if (instance != null) 
			instance.doShutdown();
	}
	
	private void doShutdown() {
		Iterator jobRecs= fSearchJobs.values().iterator();
		while (jobRecs.hasNext()) {
			SearchJobRecord element= (SearchJobRecord) jobRecs.next();
			if (element.job != null)
				element.job.cancel();
		}
		//fPositionTracker.dispose();
	}

	/**
	 * Method cancelSearch.
	 * @param job ISearchQuery
	 */
	public void cancelSearch(ISearchQuery job) {
		SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(job);
		if (rec != null && rec.job != null)
			rec.job.cancel();
	}

	/**
	 * Method activateSearchResultsView.
	 * @return ISearchResultViewPart
	 */
	public ISearchResultViewPart activateSearchResultsView() {
		try {
			ISearchResultViewPart viewPart = (ISearchResultViewPart) CatSearchPlugin.getActivePage().showView(NewSearchUI.SEARCH_RESULTS_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			return viewPart;
		} catch (PartInitException ex) {
		  StatusUtil.handleStatus(
		      SearchMessages.Search_Error_openResultView_message,
          ex, StatusManager.SHOW);
		}	
		return null;
	}

	/**
	 * Method getSearchManager.
	 * @return QueryManager
	 */
	public QueryManager getSearchManager() {
		return fSearchResultsManager;
	}
/*
	public PositionTracker getPositionTracker() {
		return fPositionTracker;
	}
*/	
	/**
 * Method addQueryListener.
 * @param l IQueryListener
 */
public void addQueryListener(IQueryListener l) {
		getSearchManager().addQueryListener(l);
	}
	/**
	 * Method getQueries.
	 * @return ISearchQuery[]
	 */
	public ISearchQuery[] getQueries() {
		return getSearchManager().getQueries();
	}
	/**
	 * Method removeQueryListener.
	 * @param l IQueryListener
	 */
	public void removeQueryListener(IQueryListener l) {
		getSearchManager().removeQueryListener(l);
	}

	/**
	 * Method removeQuery.
	 * @param query ISearchQuery
	 */
	public void removeQuery(ISearchQuery query) {
		cancelSearch(query);
		getSearchManager().removeQuery(query);
		fSearchJobs.remove(query);
	}

	/**
	 * Method addQuery.
	 * @param query ISearchQuery
	 */
	public void addQuery(ISearchQuery query) {
		while (getSearchManager().getQueries().length >= HISTORY_COUNT) {
			removeQuery(getSearchManager().getOldestQuery());
		}
		getSearchManager().addQuery(query);
	}

	public void removeAllQueries() {
		for (Iterator queries= fSearchJobs.keySet().iterator(); queries.hasNext();) {
			ISearchQuery query= (ISearchQuery) queries.next();
			cancelSearch(query);
		}
		fSearchJobs.clear();
		getSearchManager().removeAll();
	}
}
