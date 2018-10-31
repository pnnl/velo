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

import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 */
class RemoveResultAction extends Action {

	private ISelectionProvider fSelectionProvider;

	/**
	 * Constructor for RemoveResultAction.
	 * @param provider ISelectionProvider
	 * @param stringsDependOnMatchCount boolean
	 */
	public RemoveResultAction(ISelectionProvider provider, boolean stringsDependOnMatchCount) {
		fSelectionProvider= provider;
		if (!stringsDependOnMatchCount || usePluralLabel()) {
			setText(SearchMessages.SearchResultView_removeEntries_text); 
			setToolTipText(SearchMessages.SearchResultView_removeEntries_tooltip); 
		}
		else {
			setText(SearchMessages.SearchResultView_removeEntry_text); 
			setToolTipText(SearchMessages.SearchResultView_removeEntry_tooltip); 
		}
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM);
	}
	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
    /*
		final IMarker[] markers= getMarkers(fSelectionProvider.getSelection());
		if (markers != null) {
			BusyIndicator.showWhile(CatSearchPlugin.getActiveWorkbenchShell().getDisplay(), new Runnable() {
				public void run() {
					try {					
						SearchPlugin.getWorkspace().deleteMarkers(markers);
					} catch (CoreException ex) {
						ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message); 
					}
				}
			});
		}
    */
	}
	/*
	private IMarker[] getMarkers(ISelection s) {
		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return null;
		
		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size <= 0)
			return null;
		ArrayList markers= new ArrayList(size * 3);
		int markerCount= 0;
		Iterator iter= selection.iterator();
		for(int i= 0; iter.hasNext(); i++) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			markerCount += entry.getMatchCount();
			markers.addAll(entry.getMarkers());
		}
		return (IMarker[])markers.toArray(new IMarker[markerCount]);
	}
*/
	/**
	 * Method usePluralLabel.
	 * @return boolean
	 */
	private boolean usePluralLabel() {
		ISelection s= fSelectionProvider.getSelection();
		if (s == null || s.isEmpty() || !(s instanceof IStructuredSelection))
			return false;
		IStructuredSelection selection= (IStructuredSelection)s;

		if (selection.size() != 1)
			return true;

		Object firstElement= selection.getFirstElement();
		if (firstElement instanceof ISearchResultViewEntry)
			return ((ISearchResultViewEntry)firstElement).getMatchCount() > 1;
		else
			return false;
	}
}
