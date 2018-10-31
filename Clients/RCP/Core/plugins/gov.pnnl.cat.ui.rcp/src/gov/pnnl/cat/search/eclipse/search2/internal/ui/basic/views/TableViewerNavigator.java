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
package gov.pnnl.cat.search.eclipse.search2.internal.ui.basic.views;
import org.eclipse.jface.viewers.TableViewer;

/**
 */
public class TableViewerNavigator implements INavigate {
	private TableViewer fViewer;
	/**
	 * Constructor for TableViewerNavigator.
	 * @param viewer TableViewer
	 */
	public TableViewerNavigator(TableViewer viewer) {
		fViewer = viewer;
	}
	/**
	 * Method navigateNext.
	 * @param forward boolean
	 * @see gov.pnnl.cat.search.eclipse.search2.internal.ui.basic.views.INavigate#navigateNext(boolean)
	 */
	public void navigateNext(boolean forward) {
		int itemCount = fViewer.getTable().getItemCount();
		if (itemCount == 0)
			return;
		int[] selection = fViewer.getTable().getSelectionIndices();
		int nextIndex = 0;
		if (selection.length > 0) {
			if (forward) {
				nextIndex = selection[selection.length - 1] + 1;
				if (nextIndex >= itemCount)
					nextIndex = 0;
			} else {
				nextIndex = selection[0] - 1;
				if (nextIndex < 0)
					nextIndex = itemCount - 1;
			}
		}
		fViewer.getTable().setSelection(nextIndex);
		fViewer.getTable().showSelection();
	}
}
