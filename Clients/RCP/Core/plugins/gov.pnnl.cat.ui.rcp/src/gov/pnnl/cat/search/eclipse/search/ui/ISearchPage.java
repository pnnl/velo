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

import org.eclipse.jface.dialogs.IDialogPage;

/**
 * Interface to be implemented by contributors to the extension point <code>org.eclipse.search.searchPages</code>.
 * Represents a page in the search dialog. Implementod typically subclass <code>DialogPage</code>.
 * <p>
 * The search dialog calls the <code>performAction</code> method when the Search
 * button is pressed.
 * <p>
 *
 * @see org.eclipse.jface.dialogs.IDialogPage
 * @see org.eclipse.jface.dialogs.DialogPage
 * @version $Revision: 1.0 $
 */
public interface ISearchPage extends IDialogPage {

	/**
	 * Performs the action for this page.
	 * The search dialog calls this method when the Search
	 * button is pressed.
	 *
	
	 * @return <code>true</code> if the dialog can be closed after execution */
	public boolean performAction();

	/**
	 * Sets the container of this page.
	 * The search dialog calls this method to initialize this page.
	 * Implementations may store the reference to the container.
	 *
	 * @param	container	the container for this page
	 */
	public void setContainer(ISearchPageContainer container);
}
