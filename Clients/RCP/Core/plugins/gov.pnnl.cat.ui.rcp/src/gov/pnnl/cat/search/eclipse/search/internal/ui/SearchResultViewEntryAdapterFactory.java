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

import org.eclipse.core.runtime.IAdapterFactory;

/**
 * Implements basic UI support for Java elements.
 * Implements handle to persistent support for Java elements.
 * @version $Revision: 1.0 $
 */
public class SearchResultViewEntryAdapterFactory implements IAdapterFactory {
	
	private static Class[] PROPERTIES= new Class[] {
		IResource.class
	};
	

	/**
	 * Method getAdapterList.
	 * @return Class[]
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return PROPERTIES;
	}
	
	/**
	 * Method getAdapter.
	 * @param element Object
	 * @param key Class
	 * @return Object
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(Object, Class)
	 */
	public Object getAdapter(Object element, Class key) {
		
		ISearchResultViewEntry entry= (ISearchResultViewEntry) element;
		
		if (IResource.class.equals(key)) {
			IResource resource= entry.getResource();
      return resource;
		}
		return null; 
	}
}
