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
package gov.pnnl.cat.search.eclipse.search2.internal.ui;

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;


/**
 */
public class SearchPageRegistry {
	private Map fClassToInstance;
	private Map fTargetClassNameToExtension;
	private Map fExtensionToInstance;
	private String fIdAttribute;
	
	/**
	 * Constructor for SearchPageRegistry.
	 * @param extensionPoint String
	 * @param targetClassAttribute String
	 * @param idAttribute String
	 */
	public SearchPageRegistry(String extensionPoint, String targetClassAttribute, String idAttribute) {
		super();
		fExtensionToInstance= new HashMap();
		fClassToInstance= new HashMap();
		initializeExtensionCache(extensionPoint, targetClassAttribute);
		fIdAttribute= idAttribute;
	}

	/**
	 * Method initializeExtensionCache.
	 * @param extensionPoint String
	 * @param targetClassAttribute String
	 */
	private void initializeExtensionCache(String extensionPoint, String targetClassAttribute) {
		fTargetClassNameToExtension= new HashMap();
		IConfigurationElement[] extensions=
			Platform.getExtensionRegistry().getConfigurationElementsFor(
				extensionPoint);
		for (int i= 0; i < extensions.length; i++) {
			fTargetClassNameToExtension.put(extensions[i].getAttribute(targetClassAttribute), extensions[i]);
		}

	}

	/**
	 * Method getExtensionObject.
	 * @param element Object
	 * @param expectedType Class
	 * @return ISearchResultPage
	 */
	public ISearchResultPage getExtensionObject(Object element, Class expectedType) {
		ISearchResultPage page= (ISearchResultPage) fClassToInstance.get(element.getClass());
		if (page != null)
			return page;
		if (fClassToInstance.containsKey(element.getClass()))
			return null;
		page= internalGetExtensionObject(element, expectedType);
		if (page != null)
		fClassToInstance.put(element.getClass(), page);
		return page;
	}
	
	/**
	 * Method internalGetExtensionObject.
	 * @param element Object
	 * @param expectedType Class
	 * @return ISearchResultPage
	 */
	private ISearchResultPage internalGetExtensionObject(Object element, Class expectedType) {
		IConfigurationElement configElement= (IConfigurationElement) fTargetClassNameToExtension.get(element.getClass().getName());
		if (configElement == null) {
			if (fTargetClassNameToExtension.containsKey(element.getClass().getName()))
				return null;
			configElement= getConfigElement(element.getClass());
			if (configElement != null)
			fTargetClassNameToExtension.put(element.getClass().getName(), configElement);
		}
		
		if (configElement != null) {
			ISearchResultPage lp= (ISearchResultPage) fExtensionToInstance.get(configElement);
			if (lp == null) {
				if (fExtensionToInstance.containsKey(configElement))
					return null;
				ISearchResultPage instance;
				try {
					instance= (ISearchResultPage) configElement.createExecutableExtension("class"); //$NON-NLS-1$
					String id= configElement.getAttribute(fIdAttribute);
					instance.setID(id);
					if (expectedType.isAssignableFrom(instance.getClass())) {
						fExtensionToInstance.put(configElement, instance);
						return instance;
					}
				} catch (CoreException e) {
					// programming error. Log it.
					CatSearchPlugin.log(e.getStatus());
				}
			} else {
				return lp;
			}
		}
		return null;
	}

	/**
	 * Method getConfigElement.
	 * @param clazz Class
	 * @return IConfigurationElement
	 */
	private IConfigurationElement getConfigElement(Class clazz) {
		return searchInSupertypes(clazz);
	}

	/**
	 * Method searchInSupertypes.
	 * @param clazz Class
	 * @return IConfigurationElement
	 */
	private IConfigurationElement searchInSupertypes(Class clazz) {
		IConfigurationElement foundExtension= null;
		Class superclass= clazz.getSuperclass();
		if (superclass != null)
			foundExtension= (IConfigurationElement) fTargetClassNameToExtension.get(superclass.getName());
		if (foundExtension != null)
			return foundExtension;
		Class[] interfaces= clazz.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			foundExtension= (IConfigurationElement) fTargetClassNameToExtension.get(interfaces[i].getName());
			if (foundExtension != null)
				return foundExtension;
		}
		if (superclass != null)
			foundExtension= searchInSupertypes(superclass);
		if (foundExtension != null)
			return foundExtension;
		for (int i= 0; i < interfaces.length; i++) {
			foundExtension= searchInSupertypes(interfaces[i]);
			if (foundExtension != null)
				return foundExtension;
		}
		return null;
	}

}
