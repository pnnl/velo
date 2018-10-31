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

import java.util.Map;


/**
 * A generic interface for sending messages.  Handles each
 * of the common message exchanges.
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public interface IMessageSender {
	/**
	 * Method sendSynchronous.
	 * @param payload Object
	 * @param properties Map
	 * @return Object
	 */
	public Object sendSynchronous(Object payload, Map properties);
	/**
	 * Method sendSynchronous.
	 * @param payload Object
	 * @param properties Map
	 * @param replyTo Object
	 * @return Object
	 */
	public Object sendSynchronous(Object payload, Map properties, Object replyTo);
	/**
	 * Method send.
	 * @param payload Object
	 * @param properties Map
	 * @return boolean
	 */
	public boolean send(Object payload, Map properties);
	/**
	 * Method send.
	 * @param payload Object
	 * @param properties Map
	 * @param replyTo Object
	 * @param correlationId Object
	 * @return boolean
	 */
	public boolean send(Object payload, Map properties, Object replyTo, Object correlationId);
	
	/**
	 * Method clone.
	 * @return IMessageSender
	 */
	public IMessageSender clone();
}
