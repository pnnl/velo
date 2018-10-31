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

import org.alfresco.service.namespace.QName;

/**
 */
public class HarvestConstants {

	// folders within the User Home folder where Harvest Requests are created
	public static final String FOLDER_HARVEST = "Harvests";
	public static final String FOLDER_HARVEST_REQUESTS = "Requests";
	
	// namespace for Harvest related properties, aspects
	public static final String NAMESPACE_HARVEST =  "http://www.pnl.gov/cat/model/harvest/1.0";
	public static final String NAMESPACE_HARVEST_GOOGLE =  "http://www.pnl.gov/cat/model/harvest/google/1.0";
	public static final String NAMESPACE_HARVEST_WEB =  "http://www.pnl.gov/cat/model/harvest/web/1.0";
	
	// aspect for Harvest Request nodes
	public static final QName ASPECT_HARVEST_REQUEST = QName.createQName(NAMESPACE_HARVEST, "harvestRequest");
	public static final QName ASPECT_HARVEST_REQUEST_GOOGLE = QName.createQName(NAMESPACE_HARVEST_GOOGLE, "harvestRequest_google");
	public static final QName ASPECT_HARVEST_REQUEST_WEB = QName.createQName(NAMESPACE_HARVEST_WEB, "harvestRequest_web");
	
	
	// properties for ASPECT_HARVEST_REQUEST
	public static final QName PROP_MAX_DOCUMENTS = QName.createQName(NAMESPACE_HARVEST, "maxDocuments");
	public static final QName PROP_CONCURRENT_CONNECTIONS = QName.createQName(NAMESPACE_HARVEST, "concurrentConnections");	
	public static final QName PROP_HARVEST_DEPTH = QName.createQName(NAMESPACE_HARVEST, "harvestDepth");	
	public static final QName PROP_FILTER_URL_HOSTS = QName.createQName(NAMESPACE_HARVEST, "filterUrlHosts");	
	public static final QName PROP_FILTER_URL_WORDS = QName.createQName(NAMESPACE_HARVEST, "filterUrlWords");	
	public static final QName PROP_HARVEST_TEMPLATE_ID = QName.createQName(NAMESPACE_HARVEST, "harvestTemplateId");	
	public static final QName PROP_TARGET_REPOSITORY_PATH = QName.createQName(NAMESPACE_HARVEST, "targetRepositoryPath");	
	public static final QName PROP_HARVEST_TITLE = QName.createQName(NAMESPACE_HARVEST, "harvestTitle");	

	// properties associated with ASPECT_HARVEST_REQUEST_GOOGLE
	public static final QName PROP_GOOGLE_EXACT_PHRASE = QName.createQName(NAMESPACE_HARVEST_GOOGLE, "exactPhrase");
	public static final QName PROP_GOOGLE_AT_LEAST_ONE_WORD = QName.createQName(NAMESPACE_HARVEST_GOOGLE, "atLeastOneWord");
	public static final QName PROP_GOOGLE_WITHOUT_WORDS = QName.createQName(NAMESPACE_HARVEST_GOOGLE, "withoutWords");
	public static final QName PROP_GOOGLE_ALL_WORDS = QName.createQName(NAMESPACE_HARVEST_GOOGLE, "allWords");
	

	// properties associated with ASPECT_HARVEST_REQUEST_WEB
	public static final QName PROP_WEB_URL_LIST = QName.createQName(NAMESPACE_HARVEST_WEB, "urlList");
	public static final QName PROP_WEB_LOCAL_ONLY = QName.createQName(NAMESPACE_HARVEST_WEB, "localOnly");
	public static final QName PROP_WEB_PROMPT_FOR_CREDENTIALS = QName.createQName(NAMESPACE_HARVEST_WEB, "promptForCredentials");

}
