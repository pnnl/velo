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

import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 */
class SearchResultLabelProvider extends LabelProvider {
	
	private static final String MATCHES_POSTFIX= " " + SearchMessages.SearchResultView_matches + ")";  //$NON-NLS-1$ //$NON-NLS-2$

	private ILabelProvider fLabelProvider;

	
	/**
	 * Constructor for SearchResultLabelProvider.
	 * @param provider ILabelProvider
	 */
	SearchResultLabelProvider(ILabelProvider provider) {
		fLabelProvider= provider;
	}

	/**
	 * Method getText.
	 * @param element Object
	 * @return String
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		StringBuffer buf= new StringBuffer(getLabelProvider().getText(element));
		int count= ((ISearchResultViewEntry)element).getMatchCount();
		if (count > 1) {
			buf.append(" ("); //$NON-NLS-1$
			buf.append(count);
			buf.append(MATCHES_POSTFIX);
		}
		return buf.toString();			
	}
	
	/**
	 * Method getImage.
	 * @param element Object
	 * @return Image
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		return fLabelProvider.getImage(element);
	}
	
	// Don't dispose since label providers are reused.
	/**
	 * Method dispose.
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Method getLabelProvider.
	 * @return ILabelProvider
	 */
	ILabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	/**
	 * Method addListener.
	 * @param listener ILabelProviderListener
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		super.addListener(listener);
		fLabelProvider.addListener(listener);
		PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator().addListener(listener);
	}

	/**
	 * Method isLabelProperty.
	 * @param element Object
	 * @param property String
	 * @return boolean
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return fLabelProvider.isLabelProperty(element, property);
	}

	/**
	 * Method removeListener.
	 * @param listener ILabelProviderListener
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		super.removeListener(listener);
		fLabelProvider.removeListener(listener);
		PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator().removeListener(listener);
	}
}
