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

import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 */
public class RepositoryEventMessageHandler extends AbstractMessageHandler {

	private IEventMessageListener listener;

  private static final Log logger = LogFactory.getLog(RepositoryEventMessageHandler.class);
  
	/**
	 * Method getListener.
	 * @return IEventMessageListener
	 */
	public IEventMessageListener getListener() {
		return listener;
	}

	/**
	 * Method setListener.
	 * @param listener IEventMessageListener
	 */
	public void setListener(IEventMessageListener listener) {
		this.listener = listener;
	}

	/**
	 * Method onMessage.
	 * @param payload MessagePayload
	 * @param rawMessage Message
	 */
	@Override
	public void onMessage(MessagePayload payload, Message rawMessage) {
		if ((payload instanceof RepositoryEventMessage) == false) {
			return;
		}
    if(logger.isDebugEnabled()) {
      try {
        long start = rawMessage.getLongProperty("sent");
        long end = System.currentTimeMillis();
        logger.debug("event message took " + (end - start) + " ms to get to filter");

      } catch (Exception e) {
        logger.warn("could not get long property 'sent'");
      }
    }
		RepositoryEventMessage repoEventMessage = (RepositoryEventMessage)payload;
		logger.debug("calling repository event handler: " + listener.getClass().getName());
		listener.handleEventMessage(repoEventMessage);
		logger.debug("repository event handler returned");

	}

  /**
   * Method onMessage.
   * @param message Message
   * @see javax.jms.MessageListener#onMessage(Message)
   */
  @Override
  public void onMessage(Message message) {
    onMessage(message, RepositoryEventMessage.class);  
  }

}
