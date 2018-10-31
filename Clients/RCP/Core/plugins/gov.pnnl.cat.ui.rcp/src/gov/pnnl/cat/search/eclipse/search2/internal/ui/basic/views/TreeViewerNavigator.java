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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 */
public class TreeViewerNavigator implements INavigate {
	private TreeViewer fViewer;
	private AbstractTextSearchViewPage fPage;
	
	/**
	 * Constructor for TreeViewerNavigator.
	 * @param page AbstractTextSearchViewPage
	 * @param viewer TreeViewer
	 */
	public TreeViewerNavigator(AbstractTextSearchViewPage page, TreeViewer viewer) {
		fViewer= viewer;
		fPage= page;
	}
	
	/**
	 * Method navigateNext.
	 * @param forward boolean
	 * @see gov.pnnl.cat.search.eclipse.search2.internal.ui.basic.views.INavigate#navigateNext(boolean)
	 */
	public void navigateNext(boolean forward) {
		TreeItem currentItem= getCurrentItem(forward);
		if (currentItem == null)
			return;
		TreeItem nextItem= null;
		if (forward) {
			nextItem= getNextItemForward(currentItem);
			if (nextItem == null)
				nextItem= getFirstItem();
		} else {
			nextItem= getNextItemBackward(currentItem);
			if (nextItem == null)
				nextItem= getLastItem();
		}
		if (nextItem != null) {
			internalSetSelection(nextItem);
		}
	}
	
	/**
	 * Method getFirstItem.
	 * @return TreeItem
	 */
	private TreeItem getFirstItem() {
		TreeItem[] roots= fViewer.getTree().getItems();
		if (roots.length == 0)
			return null;
		for (int i = 0; i < roots.length; i++) {
			if (hasMatches(roots[i]))
				return roots[i];
			TreeItem firstChild= getFirstChildWithMatches(roots[0]);
			if (firstChild != null)
				return firstChild;
		}
		return null;
	}
	
	/**
	 * Method getLastItem.
	 * @return TreeItem
	 */
	private TreeItem getLastItem() {
		TreeItem[] roots= fViewer.getTree().getItems();
		if (roots.length == 0)
			return null;
		return getLastChildWithMatches(roots[roots.length-1]);
	}


	/**
	 * Method getNextItemBackward.
	 * @param currentItem TreeItem
	 * @return TreeItem
	 */
	private TreeItem getNextItemBackward(TreeItem currentItem) {
		TreeItem previousSibling= getNextSibling(currentItem, false);
		if (previousSibling != null) {
			TreeItem lastChild= getLastChildWithMatches(previousSibling);
			if (lastChild != null)
				return lastChild;
			if (hasMatches(previousSibling))
				return previousSibling;
			return null;
		}
		TreeItem parent= currentItem.getParentItem();
		if (parent != null) {
			if (hasMatches(parent))
				return parent;
			return getNextItemBackward(parent);
		}
		return null;
	}

	/**
	 * Method getLastChildWithMatches.
	 * @param currentItem TreeItem
	 * @return TreeItem
	 */
	private TreeItem getLastChildWithMatches(TreeItem currentItem) {
		TreeItem[] children= getChildren(currentItem);
		if (children.length == 0)
			return null;
		TreeItem recursiveChild= getLastChildWithMatches(children[children.length-1]);
		if (recursiveChild == null)
			return children[children.length-1];
		return recursiveChild;
	}

	/**
	 * Method getNextItemForward.
	 * @param currentItem TreeItem
	 * @return TreeItem
	 */
	private TreeItem getNextItemForward(TreeItem currentItem) {
		TreeItem child= getFirstChildWithMatches(currentItem);
		if (child != null)
			return child;
		TreeItem nextSibling= getNextSibling(currentItem, true);
		if (nextSibling != null) {
			if (hasMatches(nextSibling))
				return nextSibling;
			return getFirstChildWithMatches(nextSibling);
		}
		TreeItem parent= currentItem.getParentItem();
		while (parent != null) {
			nextSibling= getNextSibling(parent, true);
			if (nextSibling != null) {
				if (hasMatches(nextSibling))
					return nextSibling;
				return getFirstChildWithMatches(nextSibling);
			}
			parent= parent.getParentItem();
		}
		return null;
	}

	/**
	 * Method getFirstChildWithMatches.
	 * @param item TreeItem
	 * @return TreeItem
	 */
	private TreeItem getFirstChildWithMatches(TreeItem item) {
		TreeItem[] children= getChildren(item);
		if (children.length == 0)
			return null;
		TreeItem child= children[0];

		if (hasMatches(child))
			return child;
		return getFirstChildWithMatches(child);
	}
	
	/**
	 * Method getChildren.
	 * @param item TreeItem
	 * @return TreeItem[]
	 */
	private TreeItem[] getChildren(TreeItem item) {
		fViewer.setExpandedState(item.getData(), true);
		return item.getItems();
	}

	/**
	 * Method getNextSibling.
	 * @param currentItem TreeItem
	 * @param forward boolean
	 * @return TreeItem
	 */
	private TreeItem getNextSibling(TreeItem currentItem, boolean forward) {
		TreeItem[] siblings= getSiblings(currentItem);
		if (siblings.length < 2)
			return null;
		int index= -1;
		for (int i= 0; i <siblings.length; i++) {
			if (siblings[i] == currentItem) {
				index= i;
				break;
			}
		}
		if (forward && index == siblings.length-1) {
			return null;
		} else if (!forward && index == 0) {
			return null;
		}
		return forward?siblings[index+1]:siblings[index-1];
	}

	/**
	 * Method getSiblings.
	 * @param currentItem TreeItem
	 * @return TreeItem[]
	 */
	private TreeItem[] getSiblings(TreeItem currentItem) {
		Tree tree= fViewer.getTree();
		TreeItem parentItem= currentItem.getParentItem();
		if (parentItem != null)
			return parentItem.getItems();
		return tree.getItems();
	}

	/**
	 * Method hasMatches.
	 * @param item TreeItem
	 * @return boolean
	 */
	private boolean hasMatches(TreeItem item) {
		Object element= item.getData();
		if (element == null)
			return false;
		return fPage.getDisplayedMatchCount(element) > 0;
	}

	/**
	 * Method getCurrentItem.
	 * @param forward boolean
	 * @return TreeItem
	 */
	private TreeItem getCurrentItem(boolean forward) {
		Tree tree= fViewer.getTree();
		TreeItem[] selection= tree.getSelection();
		if (selection.length == 0) {
			selection= tree.getItems();
		}

		TreeItem nextItem= null;
		if (selection.length > 0) {
			nextItem= forward?selection[0]:selection[selection.length-1];
		}
		return nextItem;
	}


	/**
	 * Method internalSetSelection.
	 * @param ti TreeItem
	 */
	private void internalSetSelection(TreeItem ti) {
		if (ti != null) {
			Object data= ti.getData();
			if (data != null) {
				ISelection selection= new StructuredSelection(data);
				fViewer.setSelection(selection, true);
			}
		}
	}
}
