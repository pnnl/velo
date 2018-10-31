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
package gov.pnnl.cat.core.internal.resources.events;

import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.jms.common.AbstractMessageHandler;
import gov.pnnl.cat.jms.common.MessagePayload;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventIterator;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.log4j.Logger;

/**
 * Static access to an observation manager, so clients
 * can listen to changes to the remote repository.
 *
 * @version $Revision: 1.0 $
 */
public class NotificationManagerJMS {
  protected static Logger logger = CatLogger.getLogger(NotificationManagerJMS.class);

  // jms connection params
  private Destination destination;
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Session session;
  private JmsExceptionListener jmsExceptionListener = new JmsExceptionListener();
  private VeloJmsMessageHandler veloJmsMessageHandler = new VeloJmsMessageHandler();
  private JmsConnectionListener jmsConnectionListener;

  private ArrayList<IResourceEventListener> listeners;  
  private boolean subscribed = false;

  //private final static int MAX_NOTIFICATION_THREADS = 3;
  private static boolean JMS_ENABLED = false;

  private boolean runEventLoop;
  private LinkedBlockingQueue<RepositoryEventMessage> eventQueue = new LinkedBlockingQueue<RepositoryEventMessage>();


  /**
   * Constructor for NotificationManagerJMS.
   * @throws ResourceException
   */
  public NotificationManagerJMS() throws ResourceException {
    this.listeners = new ArrayList<IResourceEventListener>();
    //notificationExecutorService = Executors.newFixedThreadPool(MAX_NOTIFICATION_THREADS);
  }

  /**
   * @param destination the destination to set
   */
  public void setDestination(Destination destination) {
    this.destination = destination;
  }

  /**
   * @param connectionFactory the connectionFactory to set
   */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  public void shutdown() {
    if(JMS_ENABLED) {
      closeJmsConnection();
    }
    // stop the Event aggregation thread
    runEventLoop = false;
  }

  /**

   * @return the jMS_ENABLED */
  public static boolean isJMS_ENABLED() {
    return JMS_ENABLED;
  }

  /**
   * Create initial topic subscription for this session
   */
  public void connectToServer(JmsConnectionListener listener) {
    this.jmsConnectionListener = listener;
    
    if (subscribed == false) {

      // Do not throw an exception if you can't connect to JMS, so CAT
      // will still start up
      try {
        establishJmsConnection();
        subscribed = true;
        JMS_ENABLED = true;
        jmsConnectionListener.connectionStatusChanged(JmsConnectionListener.CONNECTED, null);

      } catch (Throwable e) {
        JMS_ENABLED = false;
        jmsConnectionListener.connectionStatusChanged(JmsConnectionListener.DISCONNECTED, e);
      }

      // start the Event aggregation thread
      if(subscribed) {
        runEventLoop = true;
        Runnable r = new Runnable() {
          public void run() {
            runEventLoop();
          }
        };
        new Thread(r, "Notification Event Loop").start();
      }
    }

  }

  private void establishJmsConnection() throws JMSException {
    try {

      connection = connectionFactory.createConnection("","");
      connection.setExceptionListener(jmsExceptionListener);
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer consumer = session.createConsumer(destination);
      consumer.setMessageListener(veloJmsMessageHandler);

    } catch (JMSException e) {
      logger.error("Failed to create JMS connection:", e);
      throw e;
    }
  }

  private void closeJmsConnection() {
    try {
      this.connection.close();
    } catch (Exception e) {
      logger.warn("Exception closing connection, ignoring", e);
    }
    this.connection = null;
  }
  
  /**
   * For now, let's let the batch notification do the filtering
   * @param listener
   */
  public void addResourceEventListener(IResourceEventListener listener) {
    synchronized(this.listeners) {
      // remove it first
      this.listeners.remove(listener);
      this.listeners.add(listener);
    }
  }



  /**
   * Method removeResourceEventListener.
   * @param listener IResourceEventListener
   */
  public void removeResourceEventListener(IResourceEventListener listener) {
    synchronized (this.listeners) {
      this.listeners.remove(listener);
    }
  }

  /**
   * Method notifyListeners.
   * @param notification IBatchNotification
   */
  private void notifyListeners(IBatchNotification notification) {
    // Send the batch notification to all the listeners
    ArrayList<IResourceEventListener> listenersCopy;
    synchronized(this.listeners){
      listenersCopy = new ArrayList<IResourceEventListener>(this.listeners);
    }
    for (int i = 0; i < listenersCopy.size(); i++) {
      listenersCopy.get(i).onEvent(notification);
    }    
  }

  /**
   * Method simpleLogEvents.
   * @param it RepositoryEventIterator
   */
  private void simpleLogEvents(RepositoryEventIterator it) {

    RepositoryEvent event;    
    while(it.hasNext()) {
      event = it.nextEvent();
      try {
        //EZLogger.logMessage(event.getNodeId() + " " + event.getNodePath());
        logger.debug(event.getNodeId() + " " + event.getNodePath());
      } catch (Exception e) {
        //EZLogger.logError(e, "problem with event iterator");
        logger.error("problem with event iterator", e);
      }
    }
  }

  private void runEventLoop() {
    while(runEventLoop) {
      RepositoryEventMessage newEventMessage = new RepositoryEventMessage();
      RepositoryEventList eventList = new RepositoryEventList();

      try {
        // after every 3 seconds, check back to see if we should keep polling
        RepositoryEventMessage headMessage = eventQueue.poll(3, TimeUnit.SECONDS);
        if (headMessage != null) {
          // add all of the events from the head event
          eventList.addAll(headMessage.getEvents());

          // now, drain the queue, and add all of the events from those messages
          List<RepositoryEventMessage> queuedMessages = new ArrayList<RepositoryEventMessage>();
          eventQueue.drainTo(queuedMessages);
          logger.debug("drain grabbed " + queuedMessages.size() + " messages");

          for (RepositoryEventMessage queuedMessage : queuedMessages) {
            eventList.addAll(queuedMessage.getEvents());
          }

          newEventMessage.setEvents(eventList);
          // now we have one RepositoryEventMessage with all of the events we've received
          // create a BatchNotification and continue processing

          RepositoryEventIterator eventIt = newEventMessage.getEvents().iterator();
          try {
            long begin = System.currentTimeMillis();
            // Consolidate the JMS events and turn them into a much smaller
            // set of IResourceEvents
            IBatchNotification notification = new BatchNotification(eventIt);

            notifyListeners(notification);

            long end = System.currentTimeMillis();
            logger.debug("Took " + (end - begin) + " ms to process the notification");

          } catch (ResourceException e) {
            //EZLogger.logError(e, "Error parsing JMS events.");
            logger.error("Error parsing JMS events", e);
          }
          // for testing
          simpleLogEvents((RepositoryEventIterator)eventIt);

        }
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        // thread was interrupted
      } catch (Exception e) {
        logger.error("Exception in Event Loop", e);
      }
    }
    // while loop ended, so end this thread
  }

  public class JmsExceptionListener implements ExceptionListener {

    public void onException(JMSException jmse) {
      // the JMS transport layer threw an exception.  Disconnect and reconnect
      logger.warn("Exception from JMS system.  Establishing a new connection", jmse);
      closeJmsConnection();
      
      // mark UI as JMS disabled
      JMS_ENABLED = false;
      subscribed = false;
      jmsConnectionListener.connectionStatusChanged(JmsConnectionListener.DISCONNECTED, jmse);

      // we should now be disconnected.  keep trying to reconnect
      boolean reconnected = false;
      while (!reconnected) {
        try {
          establishJmsConnection();
          logger.info("Reconnection successful");          
          reconnected = true;
          subscribed = true;
          JMS_ENABLED = true;
          jmsConnectionListener.connectionStatusChanged(JmsConnectionListener.RECONNECTED, null);

        } catch (Exception e) {
          logger.warn("Reconnection failed.  Waiting 5 seconds and trying again");
          // wait 5 seconds and try reconnecting again
          try {
            Thread.sleep(5000);
          } catch (InterruptedException e1) {
            ;
          }
        }
      }

    }
  }

  /**
   * JMS message callback
   */
  public class VeloJmsMessageHandler extends AbstractMessageHandler {

    @Override
    public void onMessage(Message message) {
      onMessage(message, RepositoryEventMessage.class);      
    }

    @Override
    public void onMessage(MessagePayload payload, Message rawMessage) {
      if ((payload instanceof RepositoryEventMessage) == false) {
        return;
      }

      try {
        RepositoryEventMessage repoEventMessage = (RepositoryEventMessage)payload;
        logger.debug("New notification: " + repoEventMessage.toString());
        eventQueue.put(repoEventMessage);
      } catch (Throwable e) {
        logger.error("Failed to parse repository event message", e);
      }

    }

  }

}


