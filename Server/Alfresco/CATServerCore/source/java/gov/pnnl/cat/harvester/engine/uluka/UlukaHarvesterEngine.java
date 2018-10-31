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

import gov.pnnl.cat.harvester.HarvestRequest;
import gov.pnnl.cat.harvester.HarvesterEngine;
import gov.pnnl.cat.harvester.engine.HarvestEngineUtil;
import gov.pnnl.cat.harvester.engine.PropertyValueFormatter;
import gov.pnnl.cat.harvester.internal.HarvestRequestImpl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;

/**
 */
public class UlukaHarvesterEngine implements HarvesterEngine, InitializingBean {

	private NodeService nodeService;
	private NamespacePrefixResolver namespacePrefixResolver;
	private ContentService contentService;
	private String harvestMapIniTemplate;
	private String harvestRequestIniTemplate;
	private String harvestTemplateClasspathDir;
	private List<QName> supportedHarvestTemplates;
	private List<String> supportedHarvestTemplateStrings; 
	private PropertyValueFormatter valueFormatter;


	/**
	 * Method setValueFormatter.
	 * @param valueFormatter PropertyValueFormatter
	 */
	public void setValueFormatter(PropertyValueFormatter valueFormatter) {
		this.valueFormatter = valueFormatter;
	}

	/**
	 * Method setContentService.
	 * @param contentService ContentService
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	// store the string values, convert to QNames in afterPropertiesSet()
	/**
	 * Method setSupportedHarvestTemplateStrings.
	 * @param templatesAsStrings List<String>
	 */
	public void setSupportedHarvestTemplateStrings(List<String> templatesAsStrings) {
		this.supportedHarvestTemplateStrings = templatesAsStrings;
	}
	
	/**
	 * Method setHarvestTemplateClasspathDir.
	 * @param harvestTemplateClasspathDir String
	 */
	public void setHarvestTemplateClasspathDir(String harvestTemplateClasspathDir) {
		if (harvestTemplateClasspathDir.endsWith("/") == false) {
			harvestTemplateClasspathDir += "/";
		}
		this.harvestTemplateClasspathDir = harvestTemplateClasspathDir.trim();

	}

	/**
	 * Method setNamespacePrefixResolver.
	 * @param namespacePrefixResolver NamespacePrefixResolver
	 */
	public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}
	
	/**
	 * Method afterPropertiesSet.
	 * @throws Exception
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		// we need to make sure namespacePrefixResolver and supportedHarvestTemplateStrings have both been set
		supportedHarvestTemplates = new ArrayList<QName>();
		for (String template : supportedHarvestTemplateStrings) {
			QName templateQName = QName.createQName(template, namespacePrefixResolver);
			supportedHarvestTemplates.add(templateQName);
		}

		// this depends on harvestTemplateClasspathDir being set
		// look for the harvester template files in the harvestTemplateClasspathDir
		// first, load the HarvestRequest.ini template
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(harvestTemplateClasspathDir + "HarvestRequest.ini");
		BufferedReader reader = new BufferedReader(new InputStreamReader( is));             
		String line = "";
		StringBuffer buffer = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		reader.close();
		harvestRequestIniTemplate = buffer.toString();

		// now, load the HarvestMap.ini template
		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(harvestTemplateClasspathDir + "HarvestMap.ini");
		reader = new BufferedReader(new InputStreamReader( is));             
		line = "";
		buffer = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffer.append(line).append("\n");
		}
		reader.close();
		harvestMapIniTemplate = buffer.toString();

	}


	/** from HarvestEngine interface.  We assume a NodeRef has already been created * @param request HarvestRequest
	 * @throws Exception
	 * @see gov.pnnl.cat.harvester.HarvesterEngine#configureHarvestRequestForEngine(HarvestRequest)
	 */
	public void configureHarvestRequestForEngine(HarvestRequest request)
	throws Exception {

		// hack!  we need a way to get to the NodeRef :)
		HarvestRequestImpl harvestRequestImpl = (HarvestRequestImpl)request;
		NodeRef rootNode = harvestRequestImpl.getNodeRef();

		// nodeRef refers to the folder
		// now, create a HarvestRequestMap.ini file
		NodeRef harvestRequestIni = nodeService.createNode(
				rootNode,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "HarvestRequest.ini"),
				ContentModel.TYPE_CONTENT,
				null).getChildRef();

		nodeService.setProperty(harvestRequestIni, ContentModel.PROP_NAME, "HarvestRequest.ini");
		
		ContentWriter contentWriter = contentService.getWriter(harvestRequestIni, ContentModel.PROP_CONTENT, true); 

		String myHarvestRequestIni = HarvestEngineUtil.substituteFieldValues(harvestRequestIniTemplate, request, namespacePrefixResolver, valueFormatter);
		// set up the writer
		contentWriter.setMimetype("text/plain");
		contentWriter.setEncoding("UTF-8");
		contentWriter.putContent(myHarvestRequestIni);

		// and create a HarvestMap.ini file
		NodeRef harvestMapIni = nodeService.createNode(
				rootNode,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "HarvestMap.ini"),
				ContentModel.TYPE_CONTENT,
				null).getChildRef();

		nodeService.setProperty(harvestMapIni, ContentModel.PROP_NAME, "HarvestMap.ini");

		contentWriter = contentService.getWriter(harvestMapIni, ContentModel.PROP_CONTENT, true); 

		String myHarvestMapIni = HarvestEngineUtil.substituteFieldValues(harvestMapIniTemplate, request, namespacePrefixResolver, valueFormatter);

		// set up the writer
		contentWriter.setMimetype("text/plain");
		contentWriter.setEncoding("UTF-8");
		contentWriter.putContent(myHarvestMapIni);
	}

	/**
	 * For Uluka, we create an entire folder structure to store our Harvest Request
	 * This is because multiple filesd are required to drive the Uluka engine
	 * @param parentFolder NodeRef
	 * @param harvestRequest HarvestRequest
	 * @return NodeRef
	 * @throws Exception
	 * @see gov.pnnl.cat.harvester.HarvesterEngine#createHarvestRequestNodeRef(NodeRef, HarvestRequest)
	 */
	public NodeRef createHarvestRequestNodeRef(NodeRef parentFolder, HarvestRequest harvestRequest)
	throws Exception {
		NodeRef harvestNode = nodeService.createNode(
				parentFolder,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, harvestRequest.getName()),
				ContentModel.TYPE_FOLDER,
				harvestRequest.getParameters()).getChildRef();

		return harvestNode;

	}

	/**
	 * Method getSupportedHarvestTemplates.
	 * @return List<QName>
	 * @see gov.pnnl.cat.harvester.HarvesterEngine#getSupportedHarvestTemplates()
	 */
	public List<QName> getSupportedHarvestTemplates() {
		return supportedHarvestTemplates;
	}



}
