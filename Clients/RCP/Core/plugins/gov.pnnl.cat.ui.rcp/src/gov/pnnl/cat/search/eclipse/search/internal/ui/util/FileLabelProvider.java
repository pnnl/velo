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

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchMessages;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;


/**
 */
public class FileLabelProvider extends LabelProvider {
		
	public static final int SHOW_LABEL= 1;
	public static final int SHOW_LABEL_PATH= 2;
	public static final int SHOW_PATH_LABEL= 3;
	public static final int SHOW_PATH= 4;
	
	private static final String fgSeparatorFormat= SearchMessages.FileLabelProvider_dashSeparated; 
	
	private WorkbenchLabelProvider fLabelProvider;
	private ILabelDecorator fDecorator;
		
	private int fOrder;
	private String[] fArgs= new String[2];

	/**
	 * Constructor for FileLabelProvider.
	 * @param orderFlag int
	 */
	public FileLabelProvider(int orderFlag) {
		fDecorator= PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		fLabelProvider= new WorkbenchLabelProvider();
		fOrder= orderFlag;
	}

	/**
	 * Method setOrder.
	 * @param orderFlag int
	 */
	public void setOrder(int orderFlag) {
		fOrder= orderFlag;
	}
	
	/**
	 * Method getText.
	 * @param element Object
	 * @return String
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		if (!(element instanceof ISearchResultViewEntry))
			return ""; //$NON-NLS-1$

		IResource resource= ((ISearchResultViewEntry) element).getResource();
		String text= null;

		if (resource == null)
			text= SearchMessages.SearchResultView_removed_resource; 
		
		else {
			CmsPath path= resource.getPath().removeLastSegments(1);
			if (fOrder == SHOW_LABEL || fOrder == SHOW_LABEL_PATH) {
				text= fLabelProvider.getText(resource);
				if (path != null && fOrder == SHOW_LABEL_PATH) {
					fArgs[0]= text;
					fArgs[1]= path.toString();
					text= MessageFormat.format(fgSeparatorFormat, fArgs);
				}
			} else {
				if (path != null)
					text= path.toString();
				else
					text= ""; //$NON-NLS-1$
				if (fOrder == SHOW_PATH_LABEL) {
					fArgs[0]= text;
					fArgs[1]= fLabelProvider.getText(resource);
					text= MessageFormat.format(fgSeparatorFormat, fArgs);
				}
			}
		}
		
		// Do the decoration
		if (fDecorator != null) {
			String decoratedText= fDecorator.decorateText(text, resource);
		if (decoratedText != null)
			return decoratedText;
		}
		return text;
	}

	/**
	 * Method getImage.
	 * @param element Object
	 * @return Image
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		if (!(element instanceof ISearchResultViewEntry))
			return null; //$NON-NLS-1$

		IResource resource= ((ISearchResultViewEntry) element).getResource();
		Image image= fLabelProvider.getImage(resource);
		if (fDecorator != null) {
			Image decoratedImage= fDecorator.decorateImage(image, resource);
			if (decoratedImage != null)
				return decoratedImage;
		}
		return image;
	}

	/**
	 * Method dispose.
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		fLabelProvider.dispose();
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
	}

	/**
	 * Method addListener.
	 * @param listener ILabelProviderListener
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		super.addListener(listener);
		fLabelProvider.addListener(listener);
	}
}
