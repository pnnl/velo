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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * Specify how clients can add menu items
 * to the context menu of the search result view.
 * A class that contributes context menu items
 * must implement this interface and pass an
 * instance of itself to the search result view.
 * 
 * @see	ISearchResultView#searchStarted(IActionGroupFactory, String, String, ImageDescriptor, String, ILabelProvider, IAction, IGroupByKeyComputer, IRunnableWithProgress)
 * @deprecated Part of the old ('classic') search result view. Since 3.0 clients can create their own search result view pages (see {@link ISearchResultPage}), leaving it up to the page 
 * how to create actions in context menus.
 * @version $Revision: 1.0 $
 */
public interface IContextMenuContributor {

	/**
	 * Contributes menu items to the given context menu appropriate for the
	 * given selection.
	 *
	 * @param menu		the menu to which the items are added
	 * @param inputProvider	the selection and input provider
	 */
	public void fill(IMenuManager menu, IInputSelectionProvider inputProvider);
}
