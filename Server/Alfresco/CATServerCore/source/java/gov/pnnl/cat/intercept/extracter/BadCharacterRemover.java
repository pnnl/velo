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
package gov.pnnl.cat.intercept.extracter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class BadCharacterRemover implements MetadataModifier {

	private List<String> propertyNames;
  private static final Log logger = LogFactory.getLog(BadCharacterRemover.class);

	/**
	 * Method setPropertyNames.
	 * @param propertyNames List<String>
	 */
	public void setPropertyNames(List<String> propertyNames) {
		this.propertyNames = propertyNames;
	}

  /**
   * Iterate through the property names specified
   * Replace values in the metadata map with new values
   * that have control characters removed
   * @param metadata Map<QName,Serializable>
   * @return Map<QName,Serializable>
   * @see gov.pnnl.cat.intercept.extracter.MetadataModifier#modifyMetadata(Map<QName,Serializable>)
   */
	public Map<QName, Serializable> modifyMetadata(Map<QName, Serializable> metadata) {
	  //loop thru all properties and if the property's namespace is alfresco's then see if its
	  // in the list of property names to check.  otherwise, for all other namespaces always check
		for (String propName : propertyNames) {
			QName propertyQName = QName.createQName(propName);
			Serializable s = metadata.get(propertyQName);
			
			if ((s != null) && (s instanceof String)) {
				String propValue = (String)s;
				propValue = removeBadCharacters(propValue);
				metadata.put(propertyQName, propValue);
			}
		}
    for (QName propertyName : metadata.keySet()) {
      if(!propertyName.getNamespaceURI().startsWith(NamespaceService.ALFRESCO_URI)){
        Serializable s = metadata.get(propertyName);
        
        if ((s != null) && (s instanceof String)) {
          String propValue = (String)s;
          propValue = removeBadCharacters(propValue);
          metadata.put(propertyName, propValue);
        }
      }
    }
		
		return metadata;
	}

	/**
	 * Thanks Carina for this code :)
	 * Only include characters with ASCII code >= 32 and <= 127
	 * All others replaced with a blank space
	 * @param value
	
	 * @return String
	 */
  private String removeBadCharacters(String value) {
    if(value == null) {
      return value;
    }
    char[] characters = value.toCharArray();
    StringBuilder out = new StringBuilder();
    char character;
    int charInt;
    
    for (int i = 0; i < characters.length; i++) {
      character = characters[i];
      charInt = (int)character;
      if(charInt >= 32 && charInt <= 127) {
        out.append(character);
        
      } else {
        logger.warn("Invalid XML character [" + character + "] being removed from property value.");
        out.append(' ');
      }
      
    }
    return out.toString();
  }

}
