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
package gov.pnnl.cat.harvester;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 */
public interface HarvestTemplate {

  /**
   * Get the name of this harvest template, as displayed to the user
  
   * @return String
   */
	public String getName();
	
	/**
	 * Get the internal ID of this harvest template
	 * represented as a QName
	
	 * @return QName
	 */
	public QName getHarvestTemplateId();
	
	/**
	 * Get the list of user-specified runtime parameters
	
	 * @return Map<QName,PropertyDefinition>
	 */
	public Map<QName, PropertyDefinition> getParameterDefinitions();
	
	/**
	 * Get a list of Parameter Definition QNames so a GUI can display the parameters in a logical order
	
	 * @return List<QName>
	 */
	public List<QName> getParameterOrderedList();
	
}
