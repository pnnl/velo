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
package gov.pnnl.cat.events.common;

import gov.pnnl.cat.jms.common.MessagePayload;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * This utility class can be used to keep track of responses
 * that are associated to a particular outgoing message.  As
 * response messages are received, they can be added to this queue
 * with the addResponse() method.  Meanwhile, another thread
 * can call waitForResponseToCorrelationId() to wait for
 * a particular response to be received.  getResponseForCorrelationId()
 * returns immediately, even if no response has been received
 * for the given id.
 * 
 * Behind the scenes, this class implements a synchronized HashMap
 * so concurrent access to the methods of this class are permitted.
 *
 * @version $Revision: 1.0 $
 */
public class CorrelationIdResponseQueue {
	private Map responseMap = Collections.synchronizedMap(new HashMap());
	private static final int MILLISECONDS_BETWEEN_WAIT = 500;

	/**
	 * Add a given response and correlationId to the queue
	 * <P>
	 * 
	 * @param correlationId The correlationId of the response message
	 * @param payload The payload of the response message
	 * 
	 */
	public void addResponse(Object correlationId, MessagePayload payload) {
		responseMap.put(correlationId, payload);
	}

	/**
	 * This method returns immediately, regardless of whether a response
	 * exists for the given correlationId or not.
	 * @param correlationId
	
	 * @return the MessagePayload object associated with the id, or null if none exists */
	public MessagePayload getResponseForCorrelationId(Object correlationId) {
		return (MessagePayload)responseMap.get(correlationId);
	}

	/**
	 * waits a given number of seconds for the response to be received for the
	 * provided correlation id.  if no response is received after the number of
	 * seconds elapses, null is returned.  Otherwise, the message payload is returned
	 * @param correlationId
	 * @param seconds
	
	 * @return MessagePayload
	 */
	public MessagePayload waitForResponseToCorrelationId(Object correlationId, int seconds) {
		MessagePayload response = getResponseForCorrelationId(correlationId);

		long now = System.currentTimeMillis();
		long waitTime = now + (seconds * 1000);

		while ((now < waitTime) && (response == null)) {
			try {
				Thread.sleep(MILLISECONDS_BETWEEN_WAIT);
			} catch (InterruptedException ie) {
				break;
			}
			response = getResponseForCorrelationId(correlationId);
			now = System.currentTimeMillis();
		}
		return response;
	}
}
