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
package gov.pnnl.cat.search.eclipse.search.internal.ui;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

/**
 * Helper class for updating error markers and other decorators that work on resources.
 * Items are mapped to their element's underlying resource.
 * Method <code>resourceChanged</code> updates all items that are affected from the changed
 * elements.
 * @version $Revision: 1.0 $
 */
class ResourceToItemsMapper {

	private static final int NUMBER_LIST_REUSE= 10;

	// map from resource to item
	private HashMap fResourceToItem;
	private Stack fReuseLists;
	
	private ContentViewer fContentViewer;

	/**
	 * Constructor for ResourceToItemsMapper.
	 * @param viewer ContentViewer
	 */
	public ResourceToItemsMapper(ContentViewer viewer) {
		fResourceToItem= new HashMap();
		fReuseLists= new Stack();
		
		fContentViewer= viewer;
	}

	/**
	 * Must be called from the UI thread.
	 * @param changedResource IResource
	 */
	public void resourceChanged(IResource changedResource) {
		Object obj= fResourceToItem.get(changedResource);
		if (obj == null) {
			// not mapped
		} else if (obj instanceof Item) {
			updateItem((Item) obj);
		} else { // List of Items
			List list= (List) obj;
			for (int k= 0; k < list.size(); k++) {
				updateItem((Item) list.get(k));
			}
		}
	}
		
	/**
	 * Method updateItem.
	 * @param item Item
	 */
	private void updateItem(Item item) {
		if (!item.isDisposed()) { // defensive code
			ILabelProvider lprovider= (ILabelProvider) fContentViewer.getLabelProvider();
			
			Object data= item.getData();

			String oldText= item.getText();
			String text= lprovider.getText(data);
			if (text != null && !text.equals(oldText)) {
				item.setText(text);
			}

			Image oldImage= item.getImage();
			Image image= lprovider.getImage(data);
			if (image != null && !image.equals(oldImage)) {
				item.setImage(image);
			}
		}
	}

	/**
	 * Adds a new item to the map.
	 * @param element Element to map
	 * @param item The item used for the element
	 */
	public void addToMap(Object element, Item item) {
		IResource resource= ((ISearchResultViewEntry)element).getResource();
		if (resource != null) {
			Object existingMapping= fResourceToItem.get(resource);
			if (existingMapping == null) {
				fResourceToItem.put(resource, item);
			} else if (existingMapping instanceof Item) {
				if (existingMapping != item) {
					List list= getNewList();
					list.add(existingMapping);
					list.add(item);
					fResourceToItem.put(resource, list);
				}
			} else { // List			
				List list= (List) existingMapping;
				if (!list.contains(item)) {
					list.add(item);
				}
			}
		}
	}

	/**
	 * Removes an element from the map.
	 * @param element Object
	 * @param item Item
	 */	
	public void removeFromMap(Object element, Item item) {
		IResource resource= ((ISearchResultViewEntry)element).getResource();
		if (resource != null) {
			Object existingMapping= fResourceToItem.get(resource);
			if (existingMapping == null) {
				return;
			} else if (existingMapping instanceof Item) {
				fResourceToItem.remove(resource);
			} else { // List
				List list= (List) existingMapping;
				list.remove(item);
				if (list.isEmpty()) {
					fResourceToItem.remove(list);
					releaseList(list);
				}
			}
		}
	}
	
	/**
	 * Method getNewList.
	 * @return List
	 */
	private List getNewList() {
		if (!fReuseLists.isEmpty()) {
			return (List) fReuseLists.pop();
		}
		return new ArrayList(2);
	}
	
	/**
	 * Method releaseList.
	 * @param list List
	 */
	private void releaseList(List list) {
		if (fReuseLists.size() < NUMBER_LIST_REUSE) {
			fReuseLists.push(list);
		}
	}
	
	/**
	 * Clears the map.
	 */
	public void clearMap() {
		fResourceToItem.clear();
	}
	
	/**
	 * Clears the map.
	 * @return boolean
	 */
	public boolean isEmpty() {
		return fResourceToItem.isEmpty();
	}	
}
