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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

/**
 */
class RemovePotentialMatchesAction extends Action {

	private IWorkbenchSite fSite;

	/**
	 * Constructor for RemovePotentialMatchesAction.
	 * @param site IWorkbenchSite
	 */
	public RemovePotentialMatchesAction(IWorkbenchSite site) {
		fSite= site;

		if (usePluralLabel()) {
			setText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatches_text); 
			setToolTipText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatches_tooltip); 
		}
		else {
			setText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatch_text); 
			setToolTipText(SearchMessages.RemovePotentialMatchesAction_removePotentialMatch_tooltip); 
		}
	}
	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
    /*
		IMarker[] markers= getMarkers();
		if (markers != null)
			try {
				SearchPlugin.getWorkspace().deleteMarkers(markers);
			} catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message); 
			}
		else {
			String title= SearchMessages.RemovePotentialMatchesAction_dialog_title; 
			String message= SearchMessages.RemovePotentialMatchesAction_dialog_message; 
			MessageDialog.openInformation(fSite.getShell(), title, message);
		}

		// action only makes sense once
		setEnabled(false);
    */
	}
	/*
	private IMarker[] getMarkers() {

		ISelection s= fSite.getSelectionProvider().getSelection();
		if (! (s instanceof IStructuredSelection))
			return null;
		IStructuredSelection selection= (IStructuredSelection)s;

		int size= selection.size();
		if (size <= 0)
			return null;

		ArrayList markers= new ArrayList(size * 3);
		Iterator iter= selection.iterator();
		for(int i= 0; iter.hasNext(); i++) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			Iterator entryIter= entry.getMarkers().iterator();
			while (entryIter.hasNext()) {
				IMarker marker= (IMarker)entryIter.next();
				if (marker.getAttribute(SearchUI.POTENTIAL_MATCH, false))
					markers.add(marker);
			}
		}
		return (IMarker[])markers.toArray(new IMarker[markers.size()]);
	}
*/
	/**
	 * Method usePluralLabel.
	 * @return boolean
	 */
	private boolean usePluralLabel() {
		ISelection s= fSite.getSelectionProvider().getSelection();

		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return false;
	
		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size <= 0)
			return false;

    /*
		int markerCount= 0;
		Iterator iter= selection.iterator();
		for(int i= 0; iter.hasNext(); i++) {
			SearchResultViewEntry entry= (SearchResultViewEntry)iter.next();
			Iterator entryIter= entry.getMarkers().iterator();
			while (entryIter.hasNext()) {
				IMarker marker= (IMarker)entryIter.next();
				if (marker.getAttribute(SearchUI.POTENTIAL_MATCH, false)) {
					markerCount++;
				}
				if (markerCount > 1)
					return true;
			}
		}
    */
		return false;
	}
  
}
