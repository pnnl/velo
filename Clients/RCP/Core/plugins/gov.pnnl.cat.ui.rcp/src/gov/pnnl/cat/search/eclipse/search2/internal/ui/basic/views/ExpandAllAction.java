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

import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPluginImages;
import gov.pnnl.cat.search.eclipse.search2.internal.ui.SearchMessages;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

/**
 */
public class ExpandAllAction extends Action {

	private TreeViewer fViewer;

	/**
	 * Constructor for ExpandAllAction.
	 * @param viewer TreeViewer
	 */
	public ExpandAllAction(TreeViewer viewer) {
		super(SearchMessages.ExpandAllAction_label); 
		fViewer= viewer;
		setToolTipText(SearchMessages.ExpandAllAction_tooltip); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_EXPAND_ALL);
	}
	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fViewer.expandAll();
	}
}
