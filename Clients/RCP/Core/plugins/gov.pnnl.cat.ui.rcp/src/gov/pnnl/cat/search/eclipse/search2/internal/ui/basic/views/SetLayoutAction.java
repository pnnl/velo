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

import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchViewPage;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

/**
 */
public class SetLayoutAction extends Action {

	private AbstractTextSearchViewPage fPage;
	private int fLayout;

	/**
	 * Constructor for SetLayoutAction.
	 * @param page AbstractTextSearchViewPage
	 * @param label String
	 * @param tooltip String
	 * @param layout int
	 */
	public SetLayoutAction(AbstractTextSearchViewPage page, String label, String tooltip, int layout) {
		super(label,  IAction.AS_RADIO_BUTTON);
		fPage= page;
		setToolTipText(tooltip); //$NON-NLS-1$
		fLayout= layout;
	}
	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fPage.setLayout(fLayout);
	}
	
	/**
	 * Method getLayout.
	 * @return int
	 */
	public int getLayout() {
		return fLayout;
	}
}
