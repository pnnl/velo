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
package gov.pnnl.cat.jms.common;

import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * A specific MessageSender that knows how to send a RepositoryEventMessage
 * 
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class RepositoryEventMessageSender extends AbstractMessageSender {

	/**
	 * Method sendMessage.
	 * @param message RepositoryEventMessage
	 */
	public void sendMessage(RepositoryEventMessage message) {
		Map<String,String> properties = new HashMap<String,String>();
		RepositoryEventList events = message.getEvents();
		if (message.getProperties() != null) {
			properties.putAll(message.getProperties());
		}
		getSender().send(message, properties);
	}
}
