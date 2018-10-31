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
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 */
public interface HarvestRequest {

	/**
	 * Get the id of the HarvestTemplate used to create this Harvest
	
	 * @return QName
	 */
	public QName getHarvestTemplateId();
	
	/**
	 * Get the runtime parameters specified by the user for this harvest
	
	 * @return Map<QName,Serializable>
	 */
	public Map<QName, Serializable> getParameters();
	
	/**
	 * Get the display name for this harvest request
	
	 * @return String
	 */
	public String getName();
	
	/**
	 * Get the path to this request within the repository
	 * This will allow an external application to be launched
	 * using this request as a parameter.  CAT Client can
	 * use this path to resolve into a CIFS path
	
	 * @return String
	 */
	public String getRepositoryPath();
	
	/**
	 * Get the UUID to this request within the repository
	 * This will allow an external application to be launched
	 * using this request as a parameter.  This has more
	 * applicability to a server-side harvester, since UUIDs
	 * are harder to use on the client
	
	 * @return String
	 */
	public String getUUID();
}
