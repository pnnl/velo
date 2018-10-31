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
package gov.pnnl.cat.events.tests;

import gov.pnnl.cat.jms.common.AbstractMessageHandler;
import gov.pnnl.cat.jms.common.MessagePayload;

import javax.jms.Message;


public class TestMessageListener extends AbstractMessageHandler {

	private int messageCount;
	
	public int getMessageCount() {
		return messageCount;
	}
	
	public void onMessage(MessagePayload payload, Message rawMessage) {
		messageCount++;
	}

  @Override
  public void onMessage(Message message) {
    onMessage(message, MessagePayload.class);
    
  }
}
