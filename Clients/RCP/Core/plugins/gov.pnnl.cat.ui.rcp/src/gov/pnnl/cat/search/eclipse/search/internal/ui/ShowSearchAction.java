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

/**
 */
class ShowSearchAction extends Action {
	private Search fSearch;
	
	/**
	 *	Create a new instance of this class
	 * @param search Search
	 */
	public ShowSearchAction(Search search) {
		fSearch= search;
		String desc= search.getShortDescription();
		setText(desc);
		setToolTipText(desc);
		setImageDescriptor(search.getImageDescriptor());
	}
	/**
	 *	Invoke the resource wizard selection wizard
	 *
	 *	
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (fSearch != SearchManager.getDefault().getCurrentSearch())
			SearchManager.getDefault().setCurrentSearch(fSearch);
	}
}
