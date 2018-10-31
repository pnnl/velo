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
package gov.pnnl.cat.search.taxonomy.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

/**
 */
public class TaxonomyCheckboxTreeViewer extends ContainerCheckedTreeViewer {

	/**
	 * Constructor for TaxonomyCheckboxTreeViewer.
	 * @param parent Composite
	 * @param style int
	 */
	public TaxonomyCheckboxTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/*
   * Copied from super implementation.
   * This was necessary in order to customize the behavior of the checkboxes.
   * See updateParentItems for more information.
   * 
   * (non-Javadoc)
   * @see org.eclipse.ui.dialogs.ContainerCheckedTreeViewer#doCheckStateChanged(java.lang.Object)
	 */
  protected void doCheckStateChanged(Object element) {
    Widget item = findItem(element);
    if (item instanceof TreeItem) {
      TreeItem treeItem = (TreeItem) item;
      treeItem.setGrayed(false);
      updateChildrenItems(treeItem);
      updateParentItems(treeItem.getParentItem());
    }
  }


  /*
   * Copied from super implementation.
   * This was necessary in order to customize the behavior of the checkboxes.
   * See updateParentItems for more information.
   */
  /**
   * Method updateChildrenItems.
   * @param parent TreeItem
   */
  private void updateChildrenItems(TreeItem parent) {
    Item[] children = getChildren(parent);
    boolean state = parent.getChecked();
    for (int i = 0; i < children.length; i++) {
      TreeItem curr = (TreeItem) children[i];
      if (curr.getData() != null && ((curr.getChecked() != state) || curr.getGrayed())) {
        curr.setChecked(state);
        curr.setGrayed(false);
        updateChildrenItems(curr);
      }
    }
  }

	/**
	 * Updates the check / gray state of all parent items
	 * 
	 * This method was copied from ContainerCheckedTreeViewer.updateParentItems and modified slightly.
	 * The logical difference between the two is that this method leaves the parent checkbox gray
	 * even if all of its children become checked.
	 * @param item TreeItem
	 */
	protected void updateParentItems(TreeItem item) {
		if (item != null) {
			Item[] children = getChildren(item);
			boolean containsChecked = false;
			boolean containsUnchecked = false;
			for (int i = 0; i < children.length; i++) {
				TreeItem curr = (TreeItem) children[i];
				containsChecked |= curr.getChecked();
				containsUnchecked |= (!curr.getChecked() || curr.getGrayed());
			}
			item.setChecked(containsChecked);
			item.setGrayed(containsChecked);
			updateParentItems(item.getParentItem());
		}
	}

  /*
   *  (non-Javadoc)
   * This method was copied from CheckboxTreeViewer.
   * This class will function correctly without this method (using the super 
   * class's implementation), but performance will be much slower.
   * 
   * @see org.eclipse.jface.viewers.CheckboxTreeViewer#getCheckedElements()
   */
  public Object[] getCheckedElements() {
    ArrayList v = new ArrayList();
    Control tree = getControl();
    internalCollectChecked(v, tree);
    return v.toArray();
  }


  /*
   *  (non-Javadoc)
   * This method was copied from CheckboxTreeViewer.
   * See getCheckedElements for more information.
   */
  /**
   * Method internalCollectChecked.
   * @param result List
   * @param widget Widget
   */
  private void internalCollectChecked(List result, Widget widget) {
      Item[] items = getChildren(widget);
      for (int i = 0; i < items.length; i++) {
          Item item = items[i];
          if (item instanceof TreeItem && ((TreeItem) item).getChecked()) {
              Object data = item.getData();
              if (data != null)
                  result.add(data);
          }
          internalCollectChecked(result, item);
      }
  }

}
