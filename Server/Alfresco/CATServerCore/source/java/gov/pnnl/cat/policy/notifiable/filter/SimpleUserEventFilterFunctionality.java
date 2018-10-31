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

import gov.pnnl.cat.policy.notifiable.NotifiableAspectBehavior;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class SimpleUserEventFilterFunctionality extends AbstractFilterFunctionality {

	private static final Log logger = LogFactory.getLog(NotifiableAspectBehavior.class);

	/**
	 * Method filterMessage.
	 * @param rawMessage RepositoryEventMessage
	 * @return RepositoryEventMessage
	 * @see gov.pnnl.cat.policy.notifiable.filter.IFilterFunctionality#filterMessage(RepositoryEventMessage)
	 */
	public RepositoryEventMessage filterMessage(RepositoryEventMessage rawMessage) {
		// simple filter: return original message
		logger.debug("filtered message");
		return rawMessage;
	}
	
}
