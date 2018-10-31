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
package gov.pnnl.cat.harvester.engine.uluka;

import gov.pnnl.cat.harvester.HarvestConstants;
import gov.pnnl.cat.harvester.HarvestRequest;
import gov.pnnl.cat.harvester.engine.HarvestEngineUtil;
import gov.pnnl.cat.harvester.engine.PropertyValueFormatter;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 */
public class UlukaValueFormatter implements PropertyValueFormatter{

	/**
	 * Method formatPropertyValue.
	 * @param request HarvestRequest
	 * @param propertyKey QName
	 * @return String
	 * @see gov.pnnl.cat.harvester.engine.PropertyValueFormatter#formatPropertyValue(HarvestRequest, QName)
	 */
	public String formatPropertyValue(HarvestRequest request,
			QName propertyKey) {

		Serializable value = request.getParameters().get(propertyKey);

		if (propertyKey.equals(HarvestConstants.PROP_FILTER_URL_HOSTS) || 
				propertyKey.equals(HarvestConstants.PROP_FILTER_URL_WORDS)) {
			// these fields want comma separated
			return HarvestEngineUtil.collapseMultiValue(value, ",");
		} 

		return HarvestEngineUtil.collapseMultiValue(value, "");
	}
}
