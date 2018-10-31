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
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventIterator;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This object represents a batch of server notifications.
 * It is built up by merging the various server events, 
 * consolidating them, and transforming them into a smaller
 * set of IResourceEvents.
 * 
 * TODO: BatchNotificaitons include events from all notifiable
 * nodes, even if they are not an IResource.  We probably need
 * to make the IResourceEvent more generic so we don't falsely
 * imply that notifications are only for files and folders.
 * 
 * TODO: if somebody creates a company_home/system folder, we
 * are in trouble
 * @version $Revision: 1.0 $
 */
public class BatchNotification implements IBatchNotification {

  protected static Logger logger = CatLogger.getLogger(BatchNotification.class);
  
  private IResourceEvent root;

  private BatchNotification() {
    this.root = new ResourceEvent(new CmsPath());
  }

  /**
   * Constructor for BatchNotification.
   * @param events RepositoryEventIterator
   * @throws ResourceException
   */
  public BatchNotification(RepositoryEventIterator events) throws ResourceException  {
    this();
    Hashtable<String, ResourceEvent> eventTable = parseJmsEvents(events);
    organize(eventTable);
  }
/*
  public BatchNotification(CmsPath path, int changeFlags, IResourceService manager) throws ResourceException {
    this(new CmsPath[] {path}, changeFlags, manager);
  }

  public BatchNotification(CmsPath[] paths, int changeFlags, IResourceService manager) throws ResourceException {
    this();
    Hashtable<String, ResourceEvent> eventTable = new Hashtable<String, ResourceEvent>();
    ResourceEvent resourceEvent;

    for (int i = 0; i < paths.length; i++) {
      CmsPath path = paths[i];

//      path = getResponsibleNodePath(path);

      resourceEvent = new ResourceEvent(path);
      resourceEvent.addChange(changeFlags);
      eventTable.put(path.toString(), resourceEvent);

      if (resourceEvent.hasChange(IResourceEvent.ADDED) ||
          resourceEvent.hasChange(IResourceEvent.REMOVED)) {
        createParentModifiedEvent(resourceEvent, eventTable);
      }
    }

//  eventTable = addEventsForLinkedResources(eventTable);
    organize(eventTable);
  }*/

  /**
   * Should this return an iterator or a collection?
   * @return Iterator<IResourceEvent>
 * @see gov.pnnl.cat.core.resources.events.IBatchNotification#getNonRedundantEvents()
 */
  public Iterator<IResourceEvent> getNonRedundantEvents() {
    return new ResourceEventIterator(true);
  }
  
  public void printEvents() {
    Iterator<IResourceEvent> it = getAllEvents();
    IResourceEvent event;    
    while(it.hasNext()) {
      event = it.next();
      try {
        logger.debug("Event:  " + event.getPath().toDisplayString());
        if(event.hasChange(IResourceEvent.ADDED)) {
          logger.debug("Added");
        }
        if(event.hasChange(IResourceEvent.ASPECTS_CHANGED)) {
          logger.debug("Aspects changed");
        }
        if(event.hasChange(IResourceEvent.CONTENT_CHANGED)) {
          logger.debug("Content changed");
        } 
        if(event.hasChange(IResourceEvent.PROPERTY_CHANGED)) {
          logger.debug("Property changed");
        }   
        if(event.hasChange(IResourceEvent.REMOVED)) {
          logger.debug("Removed");
        } 
        if(event.hasChange(IResourceEvent.TARGET_CHANGED)) {
          logger.debug("Target changed");
        } 
        logger.debug("");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

//  /**
//   * Should this return an iterator or a Collection?
//   */
//  public Iterator<IResourceEvent> getNonRedundantEvents(int eventTypeMask) {
//    return new FooIterator(eventTypeMask, true);
//  }

  /**
   * Method getAllEvents.
   * @return Iterator<IResourceEvent>
   * @see gov.pnnl.cat.core.resources.events.IBatchNotification#getAllEvents()
   */
  public Iterator<IResourceEvent> getAllEvents() {
    return new ResourceEventIterator(false);
  }

  /**
   * Method findEvent.
   * @param path CmsPath
   * @return IResourceEvent
   * @see gov.pnnl.cat.core.resources.events.IBatchNotification#findEvent(CmsPath)
   */
  public IResourceEvent findEvent(CmsPath path) {
    // assume we have already been organized to have nodes hanging from the root node
    if (path.size() == 0) {
      return this.root;
    }

    return findEvent(this.root, path);
  }

  /**
   * Method findEvent.
   * @param parent IResourceEvent
   * @param path CmsPath
   * @return IResourceEvent
   */
  public IResourceEvent findEvent(IResourceEvent parent, CmsPath path) {
    for (Iterator iter = parent.getChildren().iterator(); iter.hasNext();) {
      IResourceEvent event = (IResourceEvent) iter.next();

      if (event.getPath().equals(path)) {
        return event;
      } else if (event.getPath().isPrefixOf(path)) {
        return findEvent(event, path);
      }
    }

    return null;
  }

//  private CmsPath getResponsibleNodePath(CmsPath path) {
//    if (path.lastSegment().equals(JcrConstants.JCR_CONTENT)) {
//      return path.removeLastSegments(1);
//    }
//    return path;
//  }

  /**
   * Method parseJmsEvents.
   * @param events RepositoryEventIterator
   * @return Hashtable<String,ResourceEvent>
   * @throws ResourceException
   */
  private Hashtable<String, ResourceEvent> parseJmsEvents(RepositoryEventIterator events) throws ResourceException  {
    RepositoryEvent repositoryEvent;
    ResourceEvent catEvent;
    String propName;
    CmsPath catPath;
    String resourcePath;
    int resourceChange;
    // this Hashtable will keep a mapping of a path to a ResourceEvent.
    // every event we generate will be put into this hashtable.
    Hashtable<String, ResourceEvent> eventTable = new Hashtable<String, ResourceEvent>();
    boolean parentModified;
    Set<CmsPath> parentsToBeRefreshed = new HashSet<CmsPath>();

    while (events.hasNext()) {
      parentModified = false;
      repositoryEvent = events.nextEvent();
 
      //TODO: need to check the hashtable for moves
      catPath = new CmsPath(repositoryEvent.getNodePath());
      resourcePath = catPath.toString();

      // look in our table to see if we have already processed anything for
      // this resource
      if (eventTable.containsKey(resourcePath)) {
        catEvent = eventTable.get(resourcePath);
      } else {
        catEvent = new ResourceEvent(catPath, repositoryEvent.getNodeId());
        eventTable.put(resourcePath, catEvent);
      }

      resourceChange = 0;

      // Figure out what the change flag should be
      String eventType = repositoryEvent.getEventType();
      if (eventType.equals(RepositoryEvent.TYPE_NODE_ADDED)) {
        resourceChange = IResourceEvent.ADDED;
        parentModified = true;
        logger.debug("Event received, new node added: " + resourcePath);
        
      } else if (eventType.equals(RepositoryEvent.TYPE_NODE_REMOVED) ) {
        resourceChange = IResourceEvent.REMOVED;
        parentModified = true;
        logger.debug("Event received, node removed: " + resourcePath);
        
      } else if (eventType.equals(RepositoryEvent.TYPE_PROPERTY_ADDED) ) {
        resourceChange = IResourceEvent.PROPERTY_CHANGED;
        logger.debug("Event received, property changed: " + resourcePath);
        
      } else if (eventType.equals(RepositoryEvent.TYPE_PROPERTY_CHANGED) ) {
        
        propName = repositoryEvent.getPropertyName();
        if (propName.equals("{http://www.alfresco.org/model/content/1.0}content")) {
          resourceChange = IResourceEvent.CONTENT_CHANGED;
          logger.debug("Event received, content changed: " + resourcePath);
        } else if (propName.equals("{http://www.alfresco.org/model/content/1.0}modified")) {
          // TODO: make sure that the apsects really did change before adding
          //       the ASPECTS_CHANGED flag.
          resourceChange = IResourceEvent.CONTENT_CHANGED | IResourceEvent.PROPERTY_CHANGED | IResourceEvent.ASPECTS_CHANGED;
          logger.debug("Event received, content changed: " + resourcePath);
        } else {
          resourceChange = IResourceEvent.PROPERTY_CHANGED;
          logger.debug("Event received, property changed: " + resourcePath);

        } 
        
      } else if (eventType.equals(RepositoryEvent.TYPE_PROPERTY_REMOVED) ) {
        resourceChange = IResourceEvent.PROPERTY_CHANGED;
        logger.debug("Event received, property changed: " + resourcePath);
        
      } else if (eventType.equals(RepositoryEvent.TYPE_TARGET_NODE_MOVED) ) {
    	  String newTargetPath = repositoryEvent.getPropertyValue(); // anything we can do with this?
        catEvent.addProperty(IResourceEvent.TARGET_CHANGED, IResourceEvent.PROP_TARGET_PATH, newTargetPath);
    	  resourceChange = IResourceEvent.TARGET_CHANGED;
      }

      catEvent.addChange(resourceChange);
      catEvent.setPerpetrator(repositoryEvent.getEventPerpetrator());

      // if the event at this level is deemed to have modified its parent
      // (e.g. adding or removing a new file modifies the parent folder)
      // then we need to "artificially" create another change event
      if (parentModified) {
    	    CmsPath parentPath = catEvent.getPath().removeLastSegments(1);
    	    parentsToBeRefreshed.add(parentPath);
      }
    }

    for (CmsPath parentPath : parentsToBeRefreshed) {
    	createParentModifiedEvent(parentPath, eventTable);
    }
    return eventTable;
  }


  /**
   * Method createParentModifiedEvent.
   * @param parentPath CmsPath
   * @param eventTable Hashtable<String,ResourceEvent>
   */
  private void createParentModifiedEvent(CmsPath parentPath, Hashtable<String, ResourceEvent> eventTable) {
    ResourceEvent parentEvent;
 //   CmsPath parentPath = catEvent.getPath().removeLastSegments(1);

    // look in our table to see if we have already processed anything for
    // this resource
    if (!eventTable.containsKey(parentPath.toString())) {
      parentEvent = new ResourceEvent(parentPath);
      eventTable.put(parentPath.toString(), parentEvent);
    }

    parentEvent = eventTable.get(parentPath.toString());
    parentEvent.addChange(IResourceEvent.CONTENT_CHANGED);    
  }

  /**
   * This method is responsible for organizing the tree structure of the notification.
   * Before this method is called, all we have is a big Hashtable of all the events
   * that occurred. This method does not actually modify the Hashtable, but instead
   * organizes the IResourceEvents themselves by utilizing ResourceEvent.insertChild().
   * @param eventTable
   */
  private void organize(Hashtable eventTable) {
    IResourceEvent event;

    for (Enumeration eventsEnum = eventTable.keys(); eventsEnum.hasMoreElements();) {
      event = (IResourceEvent) eventTable.get(eventsEnum.nextElement());

      this.root.insertChild(event);
    }
  }

  /**
   */
  private class ResourceEventIterator implements Iterator<IResourceEvent> {
//    private int mask = 255;// ^ IResourceEvent.CONTENT_CHANGED;
    private boolean ignoreRedundant;
    private List<IResourceEvent> descendants = new ArrayList<IResourceEvent>();
    private int position = 0;

//    /**
//     * The mask causes the iterator to include any events of any of the types specified in the mask.
//     * For example, a mask of CONTENT_CHANGED & PROPERTY_CHANGED will return events with any combination of
//     * events, as long as either CONTENT_CHANGED or PROPERTY_CHANGED (or both) is included.
//     * 
//     * @param eventTypeMask
//     * @param ignoreRedundant
//     */
//    public FooIterator(int eventTypeMask, boolean ignoreRedundant) {
//      this(ignoreRedundant);
//      this.mask = eventTypeMask;
//    }

    /**
     * Constructor for ResourceEventIterator.
     * @param ignoreRedundant boolean
     */
    public ResourceEventIterator(boolean ignoreRedundant) {
      this.ignoreRedundant = ignoreRedundant;

      if (root.getChangeFlags() != 0) {
        descendants.add(root);
      }

      addChildren(root);
    }

    /**
     * Method addChildren.
     * @param event IResourceEvent
     */
    private void addChildren(IResourceEvent event) {
      for (IResourceEvent child : event.getChildren()) {
        descendants.add(child);

        if (!ignoreRedundant || !isEventRedundant(child)) {
          addChildren(child); 
        }
      }
    }

    /**
     * Method isEventRedundant.
     * @param event IResourceEvent
     * @return boolean
     */
    private boolean isEventRedundant(IResourceEvent event) {
      return event.hasChange(IResourceEvent.CONTENT_CHANGED);
    }

    /**
     * Method remove.
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }

    /**
     * Method hasNext.
     * @return boolean
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return position < descendants.size();
    }

    /**
     * Method next.
     * @return IResourceEvent
     * @see java.util.Iterator#next()
     */
    public IResourceEvent next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      IResourceEvent nextEvent = descendants.get(position);
      position++;
      return nextEvent;
    }
  }

  /**
   * Provided for use in a foreach statement.
   * Returns the same iterator as getAllEvents.
  
   * @return Iterator<IResourceEvent>
   * @see #getAllEvents() */
  public Iterator<IResourceEvent> iterator() {
    return getAllEvents();
  }
}
