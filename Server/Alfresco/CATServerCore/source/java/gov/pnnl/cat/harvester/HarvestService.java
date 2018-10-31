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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 */
public interface HarvestService {

	/**
	 * Returns a list of all defined HarvestTemplates.
	
	 * @return List<HarvestTemplate>
	 */
	public List<HarvestTemplate> getHarvestTemplates();
	
	/**
	 * Returns a Harvest Template given a QName template id
	
	
	 * @param harvestTemplateId QName
	 * @return HarvestTemplate
	 */
	public HarvestTemplate getHarvestTemplate(QName harvestTemplateId);
	
	/**
	 * Given a HarvestTemplate and a Map of HarvestParameters and their values, create a HarvestRequest
	
	 * @param parameters
	 * @param harvestTemplateId QName
	 * @return HarvestRequest
	 */
	public HarvestRequest createHarvestRequest(QName harvestTemplateId, Map<QName, Serializable> parameters);
	
	/**
	 * Returns a list of all HarvestRequests for the current user
	
	 * @return List<HarvestRequest>
	 */
	public List<HarvestRequest> getHarvestRequests();
}
