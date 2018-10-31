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
package gov.pnnl.cat.search.eclipse.search.ui;

/**
 * An extension interface to <code>ISearchPage</code>. If clients implement
 * <code>IReplacePage</code> in addition to <code>ISearchPage</code>, a 
 * "Replace" button will be shown in the search dialog.
 *
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public interface IReplacePage {

	/**
	 * Performs the replace action for this page.
	 * The search dialog calls this method when the Replace
	 * button is pressed.
	 *
	
	 * @return <code>true</code> if the dialog can be closed after execution */
	public boolean performReplace();
	
}
