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
import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchViewPage;
import gov.pnnl.cat.search.eclipse.search2.internal.ui.SearchMessages;

import org.eclipse.jface.action.Action;

/**
 */
public class ShowPreviousResultAction extends Action {
	
	private AbstractTextSearchViewPage fPage;

	/**
	 * Constructor for ShowPreviousResultAction.
	 * @param page AbstractTextSearchViewPage
	 */
	public ShowPreviousResultAction(AbstractTextSearchViewPage page) {
		super(SearchMessages.ShowPreviousResultAction_label); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_PREV);
		setToolTipText(SearchMessages.ShowPreviousResultAction_tooltip); 
		fPage= page;
	}
	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fPage.gotoPreviousMatch();
	}
}
