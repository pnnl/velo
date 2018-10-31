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

//import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 */
class RemoveMatchAction extends Action {

//	private ISelectionProvider fSelectionProvider;

	/**
	 * Constructor for RemoveMatchAction.
	 * @param provider ISelectionProvider
	 */
	public RemoveMatchAction(ISelectionProvider provider) {
		super(SearchMessages.SearchResultView_removeMatch_text); 
		setToolTipText(SearchMessages.SearchResultView_removeMatch_tooltip); 
//		fSelectionProvider= provider;
	}
	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
    /*
		IMarker[] markers= getMarkers(fSelectionProvider.getSelection());
		if (markers != null)
			try {
				SearchPlugin.getWorkspace().deleteMarkers(markers);
			} catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_deleteMarkers_title, SearchMessages.Search_Error_deleteMarkers_message); 
			}
      */
	}
	/*
	private IMarker[] getMarkers(ISelection s) {
		if (! (s instanceof IStructuredSelection) || s.isEmpty())
			return null;
		
		IStructuredSelection selection= (IStructuredSelection)s;
		int size= selection.size();
		if (size != 1)
			return null;
		if (selection.getFirstElement() instanceof ISearchResultViewEntry) {
			IMarker marker= ((ISearchResultViewEntry)selection.getFirstElement()).getSelectedMarker();
			if (marker != null)
				return new IMarker[] {marker};
		}
		return null;
	}
  */
}
