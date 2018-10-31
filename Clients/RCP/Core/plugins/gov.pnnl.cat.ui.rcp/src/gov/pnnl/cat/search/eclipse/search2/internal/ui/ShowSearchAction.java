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

import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;


/**
 */
class ShowSearchAction extends Action {
	private ISearchResult fSearch;
	private SearchResultsView fView;
	
	/*
	 *	Create a new instance of this class
	 */
	/**
	 * Constructor for ShowSearchAction.
	 * @param view SearchResultsView
	 * @param search ISearchResult
	 * @param text String
	 * @param image ImageDescriptor
	 * @param tooltip String
	 */
	public ShowSearchAction(SearchResultsView view, ISearchResult search, String text, ImageDescriptor image, String tooltip) {
		fSearch= search;
		fView= view;
		// fix for bug 38049
		if (text.indexOf('@') >= 0)
			text+= '@';
		setText(text);
		setImageDescriptor(image);
		setToolTipText(tooltip);
	}
	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fView.showSearchResult(fSearch);
	}
}
