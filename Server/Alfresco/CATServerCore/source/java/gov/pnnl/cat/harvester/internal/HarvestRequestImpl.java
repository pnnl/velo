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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public class HarvestRequestImpl implements HarvestRequest {

	private QName harvestTemplateId;
	private Map<QName, Serializable> parameters;
	private NodeRef nodeRef;
	private HarvestServiceImpl harvestService;
	private String repositoryPath;

	/**
	 * Constructor for HarvestRequestImpl.
	 * @param harvestService HarvestServiceImpl
	 */
	public HarvestRequestImpl(HarvestServiceImpl harvestService) {
		this.harvestService = harvestService;
	}

	// lots of get and set methods
	/**
	 * Method setHarvestTemplateId.
	 * @param harvestTemplateId QName
	 */
	public void setHarvestTemplateId(QName harvestTemplateId) {
		this.harvestTemplateId = harvestTemplateId;
	}

	/**
	 * Method setParameters.
	 * @param parameters Map<QName,Serializable>
	 */
	public void setParameters(Map<QName, Serializable> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Method getNodeRef.
	 * @return NodeRef
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * Method setNodeRef.
	 * @param nodeRef NodeRef
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
		repositoryPath = harvestService.getRepositoryPath(this);
	}

	/**
	 * Method getHarvestTemplateId.
	 * @return QName
	 * @see gov.pnnl.cat.harvester.HarvestRequest#getHarvestTemplateId()
	 */
	public QName getHarvestTemplateId() {
		return harvestTemplateId;
	}

	/**
	 * Method getParameters.
	 * @return Map<QName,Serializable>
	 * @see gov.pnnl.cat.harvester.HarvestRequest#getParameters()
	 */
	public Map<QName, Serializable> getParameters() {
		return parameters;
	}

	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.harvester.HarvestRequest#getName()
	 */
	public String getName() {
		Serializable name = parameters.get(HarvestConstants.PROP_HARVEST_TITLE);
		if (name == null) {
			return null;
		}
		return name.toString();
	}

	/**
	 * Method getRepositoryPath.
	 * @return String
	 * @see gov.pnnl.cat.harvester.HarvestRequest#getRepositoryPath()
	 */
	public String getRepositoryPath() {
		return repositoryPath;
	}

	/**
	 * Method getUUID.
	 * @return String
	 * @see gov.pnnl.cat.harvester.HarvestRequest#getUUID()
	 */
	public String getUUID() {
		if (nodeRef == null) {
			return null;
		}
		return nodeRef.getId();
	}

}
