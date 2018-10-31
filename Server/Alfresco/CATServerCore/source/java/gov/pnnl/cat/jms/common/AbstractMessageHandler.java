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



import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * AbstractMessageHandler implements the generic JMS MessageListener
 * interface.  Any class that wants to receive a message should
 * extend this class, and implement onMessage to convert the MessagePayload
 * to a specific subclass of MessagePayload and handle it
 * accordingly.
 * 
 * For example, a specific handler
 * might convert the payload to a specific object, then
 * call a registered listener that knew how to process
 * this message.  Another example might be whether this
 * incoming message is part of a synchronous message exchange,
 * or an asynchronous message.  The subclass would implement
 * this behavior.
 *
 * @version $Revision: 1.0 $
 */
public abstract class AbstractMessageHandler implements MessageListener {
  private static final Log logger = LogFactory.getLog(AbstractMessageHandler.class);


	/**
	 * Method onMessage.
	 * @param rawMessage Message
	 * @param classs Class
	 */
	public final void onMessage(Message rawMessage, Class classs) {
		logger.debug("Message received " + rawMessage);
		if ((rawMessage instanceof TextMessage) == false) {
			logger.debug("Unrecognized message, returning");
			return;
		}
		try {
			TextMessage textMessage = (TextMessage)rawMessage;
			String xml = textMessage.getText();
			MessagePayload payload = MessagePayload.fromString(xml, classs);
			// sometimes jackson will return null instead of throwing an error if
			// the class being deserialized doesn't match the json
			if(payload == null) {
			  throw new RuntimeException("Jackson json deserialization failed for class " + classs + " and json " + xml);
			}
			
			Enumeration propNames = rawMessage.getPropertyNames();
			while (propNames.hasMoreElements()) {
				String propName = (String)propNames.nextElement();
				String propValue = rawMessage.getStringProperty(propName);
				if (payload.getProperties() == null) {
					payload.setProperties(new HashMap<String,String>());
				}
				payload.getProperties().put(propName, propValue);
			}
			
			logger.debug("calling handler in " + this.getClass().getName());
			onMessage(payload, rawMessage);
			logger.debug("handler returned");
			
		} catch (JMSException e) {
			throw new RuntimeException("Handler threw exception.", e);
		}
	}

	/**
	 * Implement this method to create a specific message handler
	 * for a given message.  
	 * @param payload
	 * @param rawMessage
	 */
	public abstract void onMessage(MessagePayload payload, Message rawMessage);

}
