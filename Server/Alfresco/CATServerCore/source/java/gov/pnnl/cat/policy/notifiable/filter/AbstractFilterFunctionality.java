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
package gov.pnnl.cat.policy.notifiable.filter;

import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

/**
 */
public abstract class AbstractFilterFunctionality implements IFilterFunctionality {

	private String user;
	
	/**
	 * Method getUser.
	 * @return String
	 * @see gov.pnnl.cat.policy.notifiable.filter.IFilterFunctionality#getUser()
	 */
	@Override
	public String getUser() {
		return user;
	}

	/**
	 * Method setUser.
	 * @param user String
	 * @see gov.pnnl.cat.policy.notifiable.filter.IFilterFunctionality#setUser(String)
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	/**
	 * Method filterMessage.
	 * @param rawMessage RepositoryEventMessage
	 * @return RepositoryEventMessage
	 * @see gov.pnnl.cat.policy.notifiable.filter.IFilterFunctionality#filterMessage(RepositoryEventMessage)
	 */
	public abstract RepositoryEventMessage filterMessage(RepositoryEventMessage rawMessage);


}
