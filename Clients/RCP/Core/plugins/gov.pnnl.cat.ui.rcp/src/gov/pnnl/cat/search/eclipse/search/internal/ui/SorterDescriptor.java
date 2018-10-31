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

import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ExceptionHandler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerSorter;
import org.osgi.framework.Bundle;

/**
 * Proxy that represents a sorter.
 * @version $Revision: 1.0 $
 */
public class SorterDescriptor {

	public final static String SORTER_TAG= "sorter"; //$NON-NLS-1$
	private final static String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private final static String PAGE_ID_ATTRIBUTE= "pageId"; //$NON-NLS-1$
	private final static String ICON_ATTRIBUTE= "icon"; //$NON-NLS-1$
	private final static String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	private final static String LABEL_ATTRIBUTE= "label"; //$NON-NLS-1$
	private final static String TOOLTIP_ATTRIBUTE= "tooltip"; //$NON-NLS-1$
	
	private IConfigurationElement fElement;
	
	/**
	 * Creates a new sorter node with the given configuration element.
	 * @param element IConfigurationElement
	 */
	public SorterDescriptor(IConfigurationElement element) {
		fElement= element;
	}

	/**
	 * Creates a new sorter from this node.
	 * @return ViewerSorter
	 */
	public ViewerSorter createObject() {
		try {
			return (ViewerSorter)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_createSorter_title, SearchMessages.Search_Error_createSorter_message); 
			return null;
		} catch (ClassCastException ex) {
			ExceptionHandler.displayMessageDialog(ex, SearchMessages.Search_Error_createSorter_title, SearchMessages.Search_Error_createSorter_message); 
			return null;
		}
	}
	
	//---- XML Attribute accessors ---------------------------------------------
	
	/**
	 * Returns the sorter's id.
	 * @return String
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}
	 
	/**
	 * Returns the sorter's image
	 * @return ImageDescriptor
	 */
	public ImageDescriptor getImage() {
		String imageName= fElement.getAttribute(ICON_ATTRIBUTE);
		if (imageName == null)
			return null;
		Bundle bundle = Platform.getBundle(fElement.getNamespace());
		return SearchPluginImages.createImageDescriptor(bundle, new Path(imageName), true);
	}

	/**
	 * Returns the sorter's label.
	 * @return String
	 */
	public String getLabel() {
		return fElement.getAttribute(LABEL_ATTRIBUTE);
	}
	
	/**
	 * Returns the sorter's preferred size
	 * @return String
	 */
	public String getToolTipText() {
		return fElement.getAttribute(TOOLTIP_ATTRIBUTE);
	}

	/**
	 * Returns the sorter's preferred size
	 * @return String
	 */
	public String getPageId() {
		return fElement.getAttribute(PAGE_ID_ATTRIBUTE);
	}
}
