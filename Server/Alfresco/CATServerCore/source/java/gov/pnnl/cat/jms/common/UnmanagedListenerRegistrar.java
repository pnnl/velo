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


import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Use this class to manage subscriptions to different
 * messaging sources.  This is Unmanaged because it is
 * being managed via this class, as opposed to an external
 * management system, like Jencks for JMS subscriptions
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class UnmanagedListenerRegistrar {
	private ConnectionFactory connectionFactory;
	private Connection connection;
	private ExceptionListener exceptionListener;
	private Map<MessageConsumer, Session> sessionMap = new HashMap<MessageConsumer, Session>();

  private static final Log logger = LogFactory.getLog(UnmanagedListenerRegistrar.class);


	/**
	 * Required to be wired via Spring
	 * @param factory
	 */
	public void setConnectionFactory(ConnectionFactory factory) {
		this.connectionFactory = factory;
	}

	/**
	 * Can be wired via Spring, but most likely to be wired in code
	 * @param exceptionListener
	 */
	public void setExceptionListener(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}

	public void resetConnection() {
		try {
			this.connection.close();
		} catch (Exception e) {
			logger.warn("Exception resetting connection, ignoring", e);
		}
		this.connection = null;
	}


	/**
	 * Method getConnection.
	 * @return Connection
	 * @throws JMSException
	 */
	private Connection getConnection() throws JMSException {
		if (connection == null) {
			connection = connectionFactory.createConnection("","");
			if (exceptionListener != null) {
				connection.setExceptionListener(exceptionListener);
			}
		}
		connection.start();
		return connection;
	}
	
	/**
	 * Method createSession.
	 * @return Session
	 * @throws JMSException
	 */
	private Session createSession() throws JMSException {
		Session s = null;
		try {
			s = getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
			
		} catch (JMSException e1) {
		  logger.error("Failed to create JMS connection:", e1);
		  
			// create session failed.  try ditching the connection and trying again
			resetConnection();
			s = getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
		}
		return s;
	}

	/**
	 * Use this to register a listener to a specific source.  In
	 * other words, create a new subscription
	 * @param listener
	 * @param source
	
	 * @return MessageConsumer
	 */
	public MessageConsumer newMessageListener(AbstractMessageHandler listener, Destination source) {
		try {
			Session s = createSession();
			MessageConsumer consumer = s.createConsumer(source);
			consumer.setMessageListener(listener);
			sessionMap.put(consumer, s);
			return consumer;

		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Use this to register a listener to a specific source.  In
	 * other words, create a new subscription.  Specify a selection criteria
	 * for this subscription
	 * @param listener
	 * @param source
	 * @param selectionCriteria
	
	 * @return MessageConsumer
	 */
	public MessageConsumer newMessageListener(AbstractMessageHandler listener, Destination source, String selectionCriteria) {
		if (selectionCriteria == null) {
			return newMessageListener(listener, source);
		}
		try {
			Session s = createSession();
			MessageConsumer consumer = s.createConsumer(source, selectionCriteria);
			consumer.setMessageListener(listener);
			sessionMap.put(consumer, s);
			return consumer;
			
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use this to remove a listener from receiving messages anymore
	 * 
	 * @param consumer
	 */
	public void removeMessageListener(MessageConsumer consumer) {
		try {
			consumer.setMessageListener(null);
			consumer.close();
			Session s = sessionMap.get(consumer);
			if (s != null) {
				s.close();
			}
		} catch (JMSException e) {
			logger.warn("Exception closing MessageConsumer.  Usually safe to ignore", e);
		}
	}
	
	/**
	 * A utility method for creating a temporary JMS queue
	
	 * @return Topic
	 */
	public Topic createTemporaryTopic() {
		try {
			Session s = createSession();
			Topic t = s.createTemporaryTopic();
			return t;
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Method removeTemporaryTopic.
	 * @param d Destination
	 */
	public void removeTemporaryTopic(Destination d) {
		if ((d instanceof TemporaryTopic) == false) {
			return;
		}
		try {
			TemporaryTopic tt = (TemporaryTopic)d;
			tt.delete();
		} catch (JMSException e) {
			logger.warn("Exception removing TemporaryTopic.  Usually safe to ignore", e);
		}
	}
}
