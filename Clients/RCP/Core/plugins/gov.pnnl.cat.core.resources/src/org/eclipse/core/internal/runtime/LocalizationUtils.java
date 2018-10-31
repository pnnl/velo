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
package org.eclipse.core.internal.runtime;

import java.lang.reflect.Field;

/**
 * Helper methods related to string localization.
 * 
 * @since org.eclipse.equinox.common 3.3
 * @version $Revision: 1.0 $
 */
public class LocalizationUtils {
	/**
	 * This method can be used in the absence of NLS class. The method tries to 
	 * use the NLS-based translation routine. If it falls, the method returns the original
	 * non-translated key.
	 * 
	 * @param key case-sensitive name of the filed in the translation file representing 
	 * the string to be translated
	
	 * @return The localized message or the non-translated key */
	static public String safeLocalize(String key) {
		try {
			Class messageClass = Class.forName("org.eclipse.core.internal.runtime.CommonMessages"); //$NON-NLS-1$
			if (messageClass == null)
				return key;
			Field field = messageClass.getDeclaredField(key);
			if (field == null)
				return key;
			Object value = field.get(null);
			if (value instanceof String)
				return (String) value;
		} catch (ClassNotFoundException e) {
			// eat exception and fall through
		} catch (NoClassDefFoundError e) {
			// eat exception and fall through
		} catch (SecurityException e) {
			// eat exception and fall through
		} catch (NoSuchFieldException e) {
			// eat exception and fall through
		} catch (IllegalArgumentException e) {
			// eat exception and fall through
		} catch (IllegalAccessException e) {
			// eat exception and fall through
		}
		return key;
	}
}
