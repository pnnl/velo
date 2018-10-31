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
package gov.pnnl.cat.server.webservice.harvest;

import gov.pnnl.cat.harvester.HarvestService;
import gov.pnnl.cat.server.webservice.util.NodeClassConverter;

import java.rmi.RemoteException;
import java.util.List;

import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;


/**
 */
public class HarvestWebService implements HarvestServiceSoapPort {

	private HarvestService harvestService;
	private DictionaryService dictionaryService;

	/**
	 * Method setDictionaryService.
	 * @param dictionaryService DictionaryService
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * Method setHarvestService.
	 * @param harvestService HarvestService
	 */
	public void setHarvestService(HarvestService harvestService) {
		this.harvestService = harvestService;
	}

	/**
	 * Method createHarvestRequest.
	 * @param harvestTemplateId String
	 * @param parameters NamedValue[]
	 * @return HarvestRequest
	 * @throws RemoteException
	 * @throws HarvestFault
	 * @see gov.pnnl.cat.server.webservice.harvest.HarvestServiceSoapPort#createHarvestRequest(String, NamedValue[])
	 */
	public HarvestRequest createHarvestRequest(String harvestTemplateId,
			NamedValue[] parameters) throws RemoteException, HarvestFault {

		// convert list of properties to a PropertyMap
		PropertyMap propertyMap = NodeClassConverter.getPropertyMap(parameters, dictionaryService);

		// call the HarvestService
		gov.pnnl.cat.harvester.HarvestRequest harvestRequest = harvestService.createHarvestRequest(
				QName.createQName(harvestTemplateId), propertyMap);

		// convert the response back to a web service object
		HarvestRequest wsHarvestRequest = convertToWebServiceHarvestRequest(harvestRequest);

		// return it
		return wsHarvestRequest;	
	}


	/**
	 * Method getHarvestRequests.
	 * @return HarvestRequest[]
	 * @throws RemoteException
	 * @throws HarvestFault
	 * @see gov.pnnl.cat.server.webservice.harvest.HarvestServiceSoapPort#getHarvestRequests()
	 */
	public HarvestRequest[] getHarvestRequests() throws RemoteException, HarvestFault {
		List<gov.pnnl.cat.harvester.HarvestRequest> harvestRequests = harvestService.getHarvestRequests();
		HarvestRequest[] wsHarvestRequests = new HarvestRequest[harvestRequests.size()];
		for (int i=0; i<harvestRequests.size(); i++) {
			wsHarvestRequests[i] = convertToWebServiceHarvestRequest(harvestRequests.get(i));
		}
		return wsHarvestRequests;
	}

	// convert a HarvestRequest object to a web-service HarvestRequest
	/**
	 * Method convertToWebServiceHarvestRequest.
	 * @param harvestRequest gov.pnnl.cat.harvester.HarvestRequest
	 * @return HarvestRequest
	 */
	private HarvestRequest convertToWebServiceHarvestRequest(gov.pnnl.cat.harvester.HarvestRequest harvestRequest) {
		HarvestRequest wsHarvestRequest = new HarvestRequest();
		wsHarvestRequest.setHarvestTemplateId(harvestRequest.getHarvestTemplateId().toString());
		wsHarvestRequest.setName(harvestRequest.getName());
		NamedValue[] properties = NodeClassConverter.convertPropertyMapToNamedValues(harvestRequest.getParameters(), dictionaryService);
		wsHarvestRequest.setParameters(properties);
		wsHarvestRequest.setUuid(harvestRequest.getUUID());
		// this needs to invoke a bean that requires a transaction
		wsHarvestRequest.setRepositoryPath(harvestRequest.getRepositoryPath());

		return wsHarvestRequest;
	}





}
