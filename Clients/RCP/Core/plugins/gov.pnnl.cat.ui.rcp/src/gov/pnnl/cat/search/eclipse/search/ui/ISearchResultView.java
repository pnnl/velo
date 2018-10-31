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

import gov.pnnl.cat.core.resources.IResource;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

/**
 * Provides client access to the search result view.
 * Each element in the view is a <code>ISearchResultViewEntry</code>,
 * which groups markers based on the <code>groupByKey</code> provided
 * by the client each time when adding a match. If every match should
 * show up in the search result view then the match itself can be used
 * as key.
 * <p>
 * The search result view has id <code>"org.eclipse.search.SearchResultView"</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @deprecated Part of the old ('classic') search result view. Since 3.0 clients can create their own search result view pages. 
 * To access the parent view, {@link gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart} is used instead.
 * @version $Revision: 1.0 $
 */
public interface ISearchResultView extends IViewPart {

	/**
	 * Informs the view that a search has started.
	 * Provides all necessary information to create an entry in the search result 
	 * view.
	 * If every match should show up in the search result view then the match
	 * itself can be used as key.	 
	 *
 	 * @param	groupFactory			the action group factory
  	 *								  		or <code>null</code> if no factory is provided.
 	 * @param	singularLabel			the label to be used for this search occurrence
 	 * 									  if there is one match
	 *									  or <code>null</code> if the pluralLabelPattern should be used
 	 * @param	pluralLabelPattern		the label pattern to be used for this search occurrence
 	 * 									  if there are more than one matches or none.
 	 * 									  This string may contain {0} which will be replace by the match count
 	 * @param	imageDescriptor			the image descriptor to be used for this search occurrence,
	 *									  or <code>null</code> if this search should not have an image
	 * @param	pageId					the id of the search page which started the search
	 * @param	labelProvider			the label provider used by this search result view
  	 *									  or <code>null</code> if the default provider should be used.
	 *									  The default label provider shows the resource name and the corresponding image.
	 * @param	gotoAction				the action used by the view to go to a marker
	 * @param	groupByKeyComputer		the computer used by the view to compute the key for a marker
	 * @param	operation				the runnable used by the view to repeat the search
	 * 
	
 	 * @since 2.0
	 * @see IActionGroupFactory */
	public void searchStarted(
				IActionGroupFactory		groupFactory,
				String					singularLabel,
				String					pluralLabelPattern,
				ImageDescriptor			imageDescriptor,
				String					pageId,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation);

	/**
	 * Returns the current selection of the search result view
	 *
	
	
	 * @return	the current selection of the search result view * @see	org.eclipse.jface.viewers.ISelectionProvider#getSelection */
	public ISelection getSelection();

	/**
	 * Informs the view that a search has started.
	 * Provides all necessary information to create an entry in the search result 
	 * view.
	 * If every match should show up in the search result view then the match
	 * itself can be used as key.	 
	 *
	 * @param	pageId				the id of the search page which started the search
 	 * @param	label				the label to be used for this search occurrence
 	 * @param	imageDescriptor		the image descriptor to be used for this search occurrence,
 	 *								  or <code>null</code> if this search should not have an image
 	 * @param	contributor			the context menu contributor
  	 *								  or <code>null</code> if no context menu is contributed
	 * @param	labelProvider		the label provider used by this search result view
  	 *								  or <code>null</code> if the default provider should be used.
	 *								  The default label provider shows the resource name and the corresponding image.
	 * @param	gotoAction			the action used by the view to go to a marker
	 * @param	groupByKeyComputer	the computer used by the view to compute the key for a marker
	 * @param	operation			the runnable used by the view to repeat the search
	 * @deprecated	As of build  > 20011107, replaced by the new version with additional parameter
	 */
	public void searchStarted(
				String					pageId,
				String					label,
				ImageDescriptor			imageDescriptor,
				IContextMenuContributor contributor,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation);

	/**
	 * Informs the view that a search has started.
	 * Provides all necessary information to create an entry in the search result 
	 * view.
	 * If every match should show up in the search result view then the match
	 * itself can be used as key.	 
	 *
	 * @param	pageId					the id of the search page which started the search
 	 * @param	singularLabel			the label to be used for this search occurrence
 	 * 									  if there is one match
	 *									  or <code>null</code> if the pluralLabelPattern should be used
 	 * @param	pluralLabelPattern		the label pattern to be used for this search occurrence
 	 * 									  if there are more than one matches or none.
 	 * 									  This string may contain {0} which will be replace by the match count
 	 * @param	imageDescriptor			the image descriptor to be used for this search occurrence,
	 *									  or <code>null</code> if this search should not have an image
	 * @param	contributor				the context menu contributor
  	 *									  or <code>null</code> if no context menu is contributed
	 * @param	labelProvider			the label provider used by this search result view
  	 *									  or <code>null</code> if the default provider should be used.
	 *									  The default label provider shows the resource name and the corresponding image.
	 * @param	gotoAction				the action used by the view to go to a marker
	 * @param	groupByKeyComputer		the computer used by the view to compute the key for a marker
	 * @param	operation				the runnable used by the view to repeat the search
 	 * @since 2.0
	 * @deprecated	As of build  > 20020514, replaced by the new version which provides an action group factory
	 */
	public void searchStarted(
				String					pageId,
				String					singularLabel,
				String					pluralLabelPattern,
				ImageDescriptor			imageDescriptor,
				IContextMenuContributor contributor,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation);

	/**
	 * Informs the view that the search has finished.
	 * This method must also be called in case of the search
	 * fails or has been canceled.
	 */
	public void searchFinished();

	/**
	 * Informs the view that a match has been found.
	 * Provides all necessary information to create a search result entry in this
	 * view.
	 * <p>
	 * Note: It is the clients responsibility to create the marker for this match.
	 * </p>
	 *
	 * @param	description		the text description of the match
	 * @param	groupByKey		the <code>Object</code> by which this match is grouped
 	
	 * @param	resource		the marker's resource passed for optimization
	 */
	public void addMatch(String description, Object groupByKey, IResource resource);

	/**
	 * Returns the label provider of a search result view.
	 *
	
	 * @since 2.0
	 * @return	the label provider of a search result view or <code>null</code> */
	public ILabelProvider getLabelProvider();
}
