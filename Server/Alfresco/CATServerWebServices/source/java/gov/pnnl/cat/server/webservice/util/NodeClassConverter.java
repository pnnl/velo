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
package gov.pnnl.cat.server.webservice.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Node;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 */
public class NodeClassConverter {

	/**
	 * Method convertNodeRefToNode.
	 * @param nodeRef NodeRef
	 * @param nodeService NodeService
	 * @param namespaceService NamespaceService
	 * @param dictionaryService DictionaryService
	 * @return Node
	 */
	public static Node convertNodeRefToNode(NodeRef nodeRef, NodeService nodeService, NamespaceService namespaceService, DictionaryService dictionaryService) {
		// Get the node's reference
		Reference reference = Utils.convertToReference(nodeService, namespaceService, nodeRef);

		// Get the node's type
		String type = nodeService.getType(nodeRef).toString();

		// Get the node's aspects
		Set<QName> aspectQNames = nodeService.getAspects(nodeRef);
		String[] aspects = new String[aspectQNames.size()];
		int aspectIndex = 0;
		for (QName aspectQName : aspectQNames)
		{
			aspects[aspectIndex] = aspectQName.toString();
			aspectIndex++;
		}

		// Get the nodes properties, plus an extra for the clusters
		Map<QName, Serializable> propertyMap = nodeService.getProperties(nodeRef);
		NamedValue[] properties = convertPropertyMapToNamedValues(propertyMap, dictionaryService);

		// Create the node and add to the right cluster set
		Node node = new Node(reference, type, aspects, properties);
		return node;
	}

	/**
	 * Method convertPropertyMapToNamedValues.
	 * @param propertyMap Map<QName,Serializable>
	 * @param dictionaryService DictionaryService
	 * @return NamedValue[]
	 */
	public static NamedValue[] convertPropertyMapToNamedValues(Map<QName, Serializable> propertyMap, DictionaryService dictionaryService) {
		NamedValue[] properties = new NamedValue[propertyMap.size()];
		int propertyIndex = 0;
		for (Map.Entry<QName, Serializable> entry : propertyMap.entrySet())
		{ 
			properties[propertyIndex] = Utils.createNamedValue(dictionaryService, entry.getKey(), entry.getValue());
			propertyIndex++;
		}
		return properties;
	}

	/**
	 * Method convertNodeToNodeRef.
	 * @param node Node
	 * @param nodeService NodeService
	 * @param searchService SearchService
	 * @param namespaceService NamespaceService
	 * @return NodeRef
	 */
	public static NodeRef convertNodeToNodeRef(Node node, NodeService nodeService, SearchService searchService, NamespaceService namespaceService) {
		Reference reference = node.getReference();
		return Utils.convertToNodeRef(reference, nodeService, searchService, namespaceService);
	}

	/**
	 * Get a property map from the named value array that can be used when setting properties
	 * Copied from CMLUtil because its method is private :(
	 * 
	 * @param namedValues   a array of named value properties
	
	 * @param dictionaryService DictionaryService
	 * @return              a property map of values */ 
	public static PropertyMap getPropertyMap(NamedValue[] namedValues, DictionaryService dictionaryService)
	{
		PropertyMap properties = new PropertyMap();
		if (namedValues != null)
		{
			for (NamedValue value : namedValues)
			{
				QName qname = QName.createQName(value.getName());
				Serializable propValue = Utils.getValueFromNamedValue(dictionaryService, qname, value);
				properties.put(qname, propValue);
			}
		}
		return properties;

	}

}
