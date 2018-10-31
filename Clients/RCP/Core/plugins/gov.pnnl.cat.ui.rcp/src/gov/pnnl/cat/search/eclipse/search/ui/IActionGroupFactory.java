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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Allows to specify an <code>ActionGroup</code> factory
 * which will be used by the Search view to create an
 * <code>ActionGroup</code> which is used to build the
 * actions bars and the context menu.
 * <p>
 * Note: Local tool bar contributions are not supported in 2.0.
 * </p>
 * 
 * Clients can implement this interface and pass an
 * instance to the search result view.
 * 
 * @see	org.eclipse.ui.actions.ActionGroup
 * @see	ISearchResultView#searchStarted(IActionGroupFactory, String, String, ImageDescriptor, String, ILabelProvider, IAction, IGroupByKeyComputer, IRunnableWithProgress)
 * @since 	2.0
 * @deprecated Part of the old ('classic') search result view. Since 3.0 clients can create their own search result view pages (see {@link ISearchResultPage}), leaving it up to the page 
 * how to create actions.
 * @version $Revision: 1.0 $
 */
public interface IActionGroupFactory {

	/**
	 * Creates an <code>ActionGroup</code> for a Search view.
	 *
	 * @param 	searchView the search result view for which the group is made
	
	
	 * @return an <code>ActionGroup</code> for a Search view * @see	org.eclipse.ui.actions.ActionGroup */
	ActionGroup createActionGroup(ISearchResultView searchView);
}
