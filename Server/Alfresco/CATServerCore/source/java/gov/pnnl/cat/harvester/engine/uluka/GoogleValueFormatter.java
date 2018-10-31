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

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 */
public class GoogleValueFormatter extends UlukaValueFormatter {

	/**
	 * Method formatPropertyValue.
	 * @param request HarvestRequest
	 * @param propertyKey QName
	 * @return String
	 * @see gov.pnnl.cat.harvester.engine.PropertyValueFormatter#formatPropertyValue(HarvestRequest, QName)
	 */
	public String formatPropertyValue(HarvestRequest request, QName propertyKey) {
		
		// some of the fields for a Google harvest are multi-value fields in the model
		// but need to be collapsed to single value strings for the HarvestRequest.ini file
		
		// todo: urlencode these values, so characters get escaped
		// change multi-values fields back to single valued?  fixes the whole phrasing problem

		Serializable value = request.getParameters().get(propertyKey);
		
		if (propertyKey.equals(HarvestConstants.PROP_GOOGLE_EXACT_PHRASE) || 
				propertyKey.equals(HarvestConstants.PROP_GOOGLE_WITHOUT_WORDS) ||
				propertyKey.equals(HarvestConstants.PROP_GOOGLE_ALL_WORDS)) {
			// use a simple URLEncoding 
			return HarvestEngineUtil.urlEncode(value.toString());
		} else if (propertyKey.equals(HarvestConstants.PROP_GOOGLE_AT_LEAST_ONE_WORD)) {
			// this fields want + separated
			return HarvestEngineUtil.collapseMultiValueWithUrlEncoding(value, " ", true);
		}
		
		return super.formatPropertyValue(request, propertyKey);
	}

}
