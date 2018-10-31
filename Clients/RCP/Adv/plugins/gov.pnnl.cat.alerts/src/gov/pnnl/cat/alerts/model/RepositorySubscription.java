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
package gov.pnnl.cat.alerts.model;

import gov.pnnl.cat.core.resources.IResource;

/**
 */
public class RepositorySubscription extends AbstractSubscription{

	private ChangeType[] changeType;
	private IResource resource;
	
	/**
	 * Method getType.
	 * @return Type
	 * @see gov.pnnl.cat.alerts.model.ISubscription#getType()
	 */
	@Override
	public Type getType() {
		return Type.REPOSITORY;
	}

	/**
	 * Method getChangeTypes.
	 * @return ChangeType[]
	 */
	public ChangeType[] getChangeTypes() {
		return changeType;
	}

	/**
	 * Method setChangeTypes.
	 * @param changeType ChangeType[]
	 */
	public void setChangeTypes(ChangeType[] changeType) {
		this.changeType = changeType;
	}

	/**
	 * Method getResource.
	 * @return IResource
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * Method setResource.
	 * @param resource IResource
	 */
	public void setResource(IResource resource) {
		this.resource = resource;
	}

}