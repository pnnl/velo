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

import gov.pnnl.cat.harvester.HarvestConstants;
import gov.pnnl.cat.harvester.HarvestRequest;
import gov.pnnl.cat.harvester.HarvestService;
import gov.pnnl.cat.harvester.HarvestTemplate;
import gov.pnnl.cat.harvester.HarvesterEngine;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;

/**
 */
public class HarvestServiceImpl implements HarvestService, InitializingBean {



	private NodeService nodeService;
	private NamespacePrefixResolver namespacePrefixResolver;
	private DictionaryService dictionaryService;
	private PersonService personService;
	private AuthenticationComponent authenticationComponent;
	private Map<QName, HarvestTemplate> harvestTemplatesMap;
	private Map<QName, HarvesterEngine> harvesterEngineMap;

	/**
	 * Method setHarvesterEngineList.
	 * @param harvesterEngines List<HarvesterEngine>
	 */
	public void setHarvesterEngineList(List<HarvesterEngine> harvesterEngines) {
		// register all of the HarvestEngines via the map
		harvesterEngineMap = new HashMap<QName, HarvesterEngine>();
		for (HarvesterEngine harvesterEngine : harvesterEngines) {
			List<QName> supportedTemplates = harvesterEngine.getSupportedHarvestTemplates();
			for (QName supportedTemplate : supportedTemplates) {
				harvesterEngineMap.put(supportedTemplate, harvesterEngine);
			}
		}
	}

	// generic setter methods
	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Method setPersonService.
	 * @param personService PersonService
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * Method setAuthenticationComponent.
	 * @param authenticationComponent AuthenticationComponent
	 */
	public void setAuthenticationComponent(
			AuthenticationComponent authenticationComponent) {
		this.authenticationComponent = authenticationComponent;
	}
	/**
	 * Method setNamespacePrefixResolver.
	 * @param namespacePrefixResolver NamespacePrefixResolver
	 */
	public void setNamespacePrefixResolver(
			NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	/**
	 * Method setHarvestTemplates.
	 * @param harvestTemplates List<HarvestTemplate>
	 */
	public void setHarvestTemplates(List<HarvestTemplate> harvestTemplates) {
		harvestTemplatesMap = new HashMap<QName, HarvestTemplate>();
		for (HarvestTemplate template : harvestTemplates) {
			harvestTemplatesMap.put(template.getHarvestTemplateId(), template);
		}
	}

	/**
	 * Method setDictionaryService.
	 * @param dictionaryService DictionaryService
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * Method afterPropertiesSet.
	 * @throws Exception
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {

		// spring will load all HarvestRequestTemplates
		// use Dictionary Service to load parameter definitions from model
		// the HarvestTemplateId is the same as the aspect we want to lookup in the model

		for (HarvestTemplate harvestTemplate : harvestTemplatesMap.values()) {
			HarvestTemplateImpl template = (HarvestTemplateImpl)harvestTemplate;

			QName aspectName = template.getHarvestTemplateId();
			AspectDefinition aspect = dictionaryService.getAspect(aspectName);
			Map<QName, PropertyDefinition> propDefinitions = aspect.getProperties();

			template.setParameterDefinitions(propDefinitions);
		}

	}


	/**
	 * Method getHarvestTemplate.
	 * @param harvestTemplateId QName
	 * @return HarvestTemplate
	 * @see gov.pnnl.cat.harvester.HarvestService#getHarvestTemplate(QName)
	 */
	public HarvestTemplate getHarvestTemplate(QName harvestTemplateId) {
		return harvestTemplatesMap.get(harvestTemplateId);
	}

	/**
	 * Method createHarvestRequest.
	 * @param harvestTemplateId QName
	 * @param parameters Map<QName,Serializable>
	 * @return HarvestRequest
	 * @see gov.pnnl.cat.harvester.HarvestService#createHarvestRequest(QName, Map<QName,Serializable>)
	 */
	public HarvestRequest createHarvestRequest(QName harvestTemplateId,
			Map<QName, Serializable> parameters) {

		try {
			// TODO: verify required parameters are set.  The integrity checker should do this, but it's not
			
			// create the object, set properties in it
			HarvestRequestImpl harvestRequest = new HarvestRequestImpl(this);
			harvestRequest.setHarvestTemplateId(harvestTemplateId);
			harvestRequest.setParameters(parameters);

			// store to the repository
			NodeRef harvestRequestNodeRef = storeHarvestRequestToNodeRef(harvestRequest);

			// store the nodeRef reference in the object
			harvestRequest.setNodeRef(harvestRequestNodeRef);
			
			HarvesterEngine harvesterEngine = harvesterEngineMap.get(harvestTemplateId);
			harvesterEngine.configureHarvestRequestForEngine(harvestRequest);

			// return the new object
			return harvestRequest;
		} catch (Exception e) {
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Method getHarvestRequests.
	 * @return List<HarvestRequest>
	 * @see gov.pnnl.cat.harvester.HarvestService#getHarvestRequests()
	 */
	public List<HarvestRequest> getHarvestRequests() {
		// look in the user's home directory for /Harvest/Requests
		NodeRef harvestRequestsNodeRef = getUserHarvestRequestFolder();

		// get all HarvestRequest children
		List<ChildAssociationRef> children = nodeService.getChildAssocs(harvestRequestsNodeRef);
		List<HarvestRequest> harvestRequests = new ArrayList<HarvestRequest>();

		// load all nodes into HarvestRequest objects (via loadHarvestRequestFromNodeRef)
		for (ChildAssociationRef child : children) {
			NodeRef harvestNode = child.getChildRef();
			if (nodeService.hasAspect(harvestNode, HarvestConstants.ASPECT_HARVEST_REQUEST)) {
				HarvestRequest harvestRequest = loadHarvestRequestFromNodeRef(harvestNode);
				harvestRequests.add(harvestRequest);
			}
		}
		return harvestRequests;
	}

	/**
	 * Method getHarvestTemplates.
	 * @return List<HarvestTemplate>
	 * @see gov.pnnl.cat.harvester.HarvestService#getHarvestTemplates()
	 */
	public List<HarvestTemplate> getHarvestTemplates() {
		// simply get all of the values in the HarvestTemplates map
		return new ArrayList<HarvestTemplate>(harvestTemplatesMap.values());
	}

	// the HarvestService interface hides this method
	// This is used by HarvestRequestImpl to find the path to the stored HarvestRequest node
	/**
	 * Method getRepositoryPath.
	 * @param harvestRequest HarvestRequestImpl
	 * @return String
	 */
	public String getRepositoryPath(HarvestRequestImpl harvestRequest) {
		if (harvestRequest.getNodeRef() == null) {
			return null;
		}
		NodeRef nodeRef = harvestRequest.getNodeRef();
		return nodeService.getPath(nodeRef).toString();
	}

	/**
	 * Method loadHarvestRequestFromNodeRef.
	 * @param nodeRef NodeRef
	 * @return HarvestRequest
	 */
	private HarvestRequest loadHarvestRequestFromNodeRef(NodeRef nodeRef) {
		// get the properties of the node via the node service
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
		// instantiate a HarvestRequestImpl object
		HarvestRequestImpl harvestRequest = new HarvestRequestImpl(this);
		// set properties
		harvestRequest.setHarvestTemplateId((QName)properties.get(HarvestConstants.PROP_HARVEST_TEMPLATE_ID));
		harvestRequest.setNodeRef(nodeRef);
		harvestRequest.setParameters(properties);
		// return object
		return harvestRequest;
	}

	/** Create a NodeRef based on the HarvestRequest, then add appropriate aspects * @param harvestRequest HarvestRequest
	 * @return NodeRef
	 * @throws Exception
	 */
	private NodeRef storeHarvestRequestToNodeRef(HarvestRequest harvestRequest) throws Exception {
		// use the node service to create a new node
		NodeRef harvestRequestFolder = getUserHarvestRequestFolder();
		Map<QName, Serializable> properties = harvestRequest.getParameters();
		properties.put(HarvestConstants.PROP_HARVEST_TEMPLATE_ID, harvestRequest.getHarvestTemplateId());
		properties.put(ContentModel.PROP_NAME, harvestRequest.getName());
		properties.put(ContentModel.PROP_TITLE, harvestRequest.getName());

		// let the individual HarvesterEngine create the node ref
		HarvesterEngine harvesterEngine = harvesterEngineMap.get(harvestRequest.getHarvestTemplateId());
		NodeRef harvestNode = harvesterEngine.createHarvestRequestNodeRef(harvestRequestFolder, harvestRequest);

		// need to set the appropriate aspects
		nodeService.addAspect(harvestNode, HarvestConstants.ASPECT_HARVEST_REQUEST, null);
		// the template ID is also the QName of the aspect to add to define the template specific properties
		nodeService.addAspect(harvestNode, harvestRequest.getHarvestTemplateId(), null);

		// return the noderef just created
		return harvestNode;
	}

	// convenience method to get the folder for the current user
	/**
	 * Method getUserHarvestRequestFolder.
	 * @return NodeRef
	 */
	private NodeRef getUserHarvestRequestFolder() {
		return getUserHarvestRequestFolder(authenticationComponent.getCurrentUserName());
	}

	// given a specific user, find their /Harvest/Requests folder
	/**
	 * Method getUserHarvestRequestFolder.
	 * @param userName String
	 * @return NodeRef
	 */
	private NodeRef getUserHarvestRequestFolder(String userName) {
		NodeRef personProfile = personService.getPerson(userName);
		if (personProfile == null) {
			throw new AlfrescoRuntimeException("Unknown Username: " + userName);
		}

		// get the home folder for the user
		NodeRef homeFolder = (NodeRef)nodeService.getProperty(personProfile, ContentModel.PROP_HOMEFOLDER);

		// get the /Harvest folder for this user
		NodeRef harvestFolderNode = NodeUtils.getChildByName(homeFolder, HarvestConstants.FOLDER_HARVEST, nodeService);
		if (harvestFolderNode == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, HarvestConstants.FOLDER_HARVEST);

			harvestFolderNode = nodeService.createNode(
					homeFolder,
					ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, HarvestConstants.FOLDER_HARVEST),
					ContentModel.TYPE_FOLDER,
					properties).getChildRef();

		}

		// get the /harvest/Requests folder for this user
		NodeRef harvestRequestsFolderNode = NodeUtils.getChildByName(harvestFolderNode, HarvestConstants.FOLDER_HARVEST_REQUESTS, nodeService);
		if (harvestRequestsFolderNode == null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, HarvestConstants.FOLDER_HARVEST_REQUESTS);

			harvestRequestsFolderNode = nodeService.createNode(
					harvestFolderNode,
					ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, HarvestConstants.FOLDER_HARVEST_REQUESTS),
					ContentModel.TYPE_FOLDER,
					properties).getChildRef();

		}
		return harvestRequestsFolderNode;

	}





}
