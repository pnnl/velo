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
package gov.pnnl.cat.search.eclipse.search.internal.ui.util;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/** 
 * A specialized content provider to show a list of editor parts.
 * @version $Revision: 1.0 $
 */ 
public class ListContentProvider implements IStructuredContentProvider {
	List fContents;	

	public ListContentProvider() {
	}
	
	/**
	 * Method getElements.
	 * @param input Object
	 * @return Object[]
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {
		if (fContents != null && fContents == input)
			return fContents.toArray();
		return new Object[0];
	}

	/**
	 * Method inputChanged.
	 * @param viewer Viewer
	 * @param oldInput Object
	 * @param newInput Object
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof List) 
			fContents= (List)newInput;
		else
			fContents= null;
		// we use a fixed set.
	}

	/**
	 * Method dispose.
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * Method isDeleted.
	 * @param o Object
	 * @return boolean
	 */
	public boolean isDeleted(Object o) {
		return fContents != null && !fContents.contains(o);
	}
}
