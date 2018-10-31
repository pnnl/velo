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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public interface HarvesterEngine {

	/**
	 * Method getSupportedHarvestTemplates.
	 * @return List<QName>
	 */
	public List<QName> getSupportedHarvestTemplates();
	
	/**
	 * Given a HarvestRequest object, we assume a node has been created in the repository.
	 * This is where we set the properties and content of the node based on the HarvestRequest
	 * @param request
	
	 * @throws Exception */
	public void configureHarvestRequestForEngine(HarvestRequest request) throws Exception;
	
	/**
	 * Given a HarvestRequest object, create a bare-bones NodeRef.  Based on different engine
	 * implementations, we might create a cm:content node, or a cm:folder node, or some other type
	 * @param parentFolder
	 * @param request
	
	
	 * @return NodeRef
	 * @throws Exception */
	public NodeRef createHarvestRequestNodeRef(NodeRef parentFolder, HarvestRequest request) throws Exception;
}
