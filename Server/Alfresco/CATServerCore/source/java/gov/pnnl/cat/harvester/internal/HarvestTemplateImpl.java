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
package gov.pnnl.cat.harvester.internal;

import gov.pnnl.cat.harvester.HarvestTemplate;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 */
public class HarvestTemplateImpl implements HarvestTemplate {

	private QName harvestTemplateId;
	private String name;
	private Map<QName, PropertyDefinition> parameterDefinitions;
	private List<QName> parameterOrderedList;
	
	// this impl class is full of gets and sets.  
	// See the HarvestTemplate interface for more description
	/**
	 * Method getHarvestTemplateIdString.
	 * @return String
	 */
	public String getHarvestTemplateIdString() {
		return harvestTemplateId.toString();
	}
	/**
	 * Method getHarvestTemplateId.
	 * @return QName
	 * @see gov.pnnl.cat.harvester.HarvestTemplate#getHarvestTemplateId()
	 */
	public QName getHarvestTemplateId() {
		return harvestTemplateId;
	}
	/**
	 * Method setHarvestTemplateIdString.
	 * @param harvestTemplateIdString String
	 */
	public void setHarvestTemplateIdString(String harvestTemplateIdString) {
		this.harvestTemplateId = QName.createQName(harvestTemplateIdString);
	}
	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.harvester.HarvestTemplate#getName()
	 */
	public String getName() {
		return name;
	}
	/**
	 * Method setName.
	 * @param name String
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Method getParameterDefinitions.
	 * @return Map<QName,PropertyDefinition>
	 * @see gov.pnnl.cat.harvester.HarvestTemplate#getParameterDefinitions()
	 */
	public Map<QName, PropertyDefinition> getParameterDefinitions() {
		return parameterDefinitions;
	}
	/**
	 * Method setParameterDefinitions.
	 * @param parameterDefinitions Map<QName,PropertyDefinition>
	 */
	public void setParameterDefinitions(
			Map<QName, PropertyDefinition> parameterDefinitions) {
		this.parameterDefinitions = parameterDefinitions;
	}
	/**
	 * Method getParameterOrderedList.
	 * @return List<QName>
	 * @see gov.pnnl.cat.harvester.HarvestTemplate#getParameterOrderedList()
	 */
	public List<QName> getParameterOrderedList() {
		return parameterOrderedList;
	}
	/**
	 * Method setParameterOrderedList.
	 * @param parameterOrderedList List<QName>
	 */
	public void setParameterOrderedList(List<QName> parameterOrderedList) {
		this.parameterOrderedList = parameterOrderedList;
	}
	



}
