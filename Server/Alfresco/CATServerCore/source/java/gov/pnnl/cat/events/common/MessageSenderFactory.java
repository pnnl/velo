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

import gov.pnnl.cat.jms.common.IMessageSender;

/**
 * Factory is a loosely used term for this class.  This is really
 * a MessageSender cloner.  Given a sample MessageSender (set via
 * Spring), other classes can get a new MessageSender based on the
 * one provided by cloning it.
 * 
 * Cloning a MessageSender means calling the clone() method on the
 * provided MessageSender, so different implementations may share
 * resources across clones of the same MessageSender
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class MessageSenderFactory {
	
	private  IMessageSender sender;
		
	/**
	 * Method setSender.
	 * @param sender IMessageSender
	 */
	public void setSender(IMessageSender sender) {
		this.sender = sender;
	}

	/**
	 * Method newMessageSender.
	 * @return IMessageSender
	 */
	public  IMessageSender newMessageSender() {
		return sender.clone();
	}
}
