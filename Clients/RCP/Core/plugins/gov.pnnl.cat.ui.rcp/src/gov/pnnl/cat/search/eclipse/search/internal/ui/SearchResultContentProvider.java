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

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 */
class SearchResultContentProvider implements IStructuredContentProvider {
	
	private static final Object[] fgEmptyArray= new Object[0];
	
	/**
	 * Method inputChanged.
	 * @param viewer Viewer
	 * @param oldInput Object
	 * @param newInput Object
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing since the viewer listens to resource deltas
	}
	
	/**
	 * Method dispose.
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
	
	/**
	 * Method isDeleted.
	 * @param element Object
	 * @return boolean
	 */
	public boolean isDeleted(Object element) {
		return false;
	}
	
	/**
	 * Method getElements.
	 * @param element Object
	 * @return Object[]
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object element) {
		if (element instanceof ArrayList)
			return ((ArrayList)element).toArray();
		else
			return fgEmptyArray;
	}
}
