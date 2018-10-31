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

import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 */
public class ResourceEvent implements IResourceEvent {

  private ArrayList<IResourceEvent> children;

  /**
   * The UUID of the node that was changed
   */
  private String uuid;
  
  /**
   * the path of the resource which was changed
   */
  private CmsPath path;
  
  /**
   * the change flags
   */
  private int flags = 0;
  
  private String perpetrator;

  /**
   * A map of a map of properties.
   * The "outer" map links a change flag Integer to the corresponding map of
   * properties relating to that change flag.
   * For example, the value for the key "8" might contain the name and value of
   * each property that was updated (since "8" is the value of PROPERTY_CHANGED).
   * 
   * TODO: making the "inner" maps <String, String> for now, but we may want to make them more flexible.
   */
  protected Map<Integer, Map<String, String>> properties;

  private static Logger logger = CatLogger.getLogger(ResourceEvent.class);
  
  /**
   * Constructor for ResourceEvent.
   * @param path CmsPath
   * @param uuid String
   */
  public ResourceEvent(CmsPath path, String uuid) {
    this(path);
    this.uuid = uuid;
  }
  
  public String getPerpetrator() {
    return perpetrator;
  }



  public void setPerpetrator(String perpetrator) {
    this.perpetrator = perpetrator;
  }



  /**
   * Constructor for ResourceEvent.
   * @param path CmsPath
   */
  public ResourceEvent(CmsPath path) {
    this.path = path;
    this.children = new ArrayList<IResourceEvent>();
  }

  /**
   * Method getPath.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#getPath()
   */
  public CmsPath getPath() {
    return this.path;
  }

  /**
   * Method addChange.
   * @param changeFlag int
   */
  public void addChange(int changeFlag) {
    //TODO: if the flag is MOVED, then we need to erase the
    // ADDED and REMOVED flags
    this.flags = this.flags | changeFlag;
  }

  /**
   * Method hasChange.
   * @param changeMask int
   * @return boolean
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#hasChange(int)
   */
  public boolean hasChange(int changeMask) {    
    return (this.flags & changeMask) == changeMask;
  }

  /**
   * Method getChangeFlags.
   * @return int
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#getChangeFlags()
   */
  public int getChangeFlags() {
    return this.flags;
  }

  /**
   * Method getUUID.
   * @return String
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#getUUID()
   */
  public String getUUID() {
    return this.uuid;
  }


  /**
   * Method addProperty.
   * @param changeFlag int
   * @param key String
   * @param value String
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#addProperty(int, String, String)
   */
  public void addProperty(int changeFlag, String key, String value) {
    // make sure the changeFlag is a power of two
    if (!isValidChangeFlag(changeFlag)) {
      logger.warn("adding a property for an invalid change flag! key: " + key + ", value: " + value);
    }

    if (properties == null) {
      properties = new HashMap<Integer, Map<String,String>>();
    }

    Map<String, String> relevantProps = properties.get(changeFlag);

    if (relevantProps == null) {
      relevantProps = new HashMap<String, String>();
      properties.put(changeFlag, relevantProps);
    }

    relevantProps.put(key, value);
  }

  /**
   * Method getProperties.
   * @param changeFlag int
   * @return Map<String,String>
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#getProperties(int)
   */
  public Map<String, String> getProperties(int changeFlag) {
    if (!isValidChangeFlag(changeFlag)) {
      logger.warn("getting properties for an invalid change flag!");
    }

    if (properties == null) {
      return null;
    }

    return properties.get(changeFlag);
  }

  /**
   * Method isValidChangeFlag.
   * @param changeFlag int
   * @return boolean
   */
  public boolean isValidChangeFlag(int changeFlag) {
    return (changeFlag & (changeFlag -1)) == 0;
  }

  /**
   * Two ResourceEvents are considered equal if they have the
   * same path.
   * TODO: should be changed to use uuid for the comparator
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj) {
    boolean ret = false;

    if(obj instanceof ResourceEvent) {
      ResourceEvent e = (ResourceEvent)obj;
      if (this.getPath().equals(e.getPath()))
        ret = true;
    }
    return ret;    
  }

  /**
   * Method containsChild.
   * @param path CmsPath
   * @return boolean
   */
  public boolean containsChild(CmsPath path) {
    // TODO: is this method intended to be defined in the IResourceEvent interface?
    return getChild(path) != null;
  }

  /**
   * Method insertChild.
   * @param child IResourceEvent
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#insertChild(IResourceEvent)
   */
  public void insertChild(IResourceEvent child) {
    // TODO: is this method intended to be defined in the IResourceEvent interface? (it was recently added)
    CmsPath path = child.getPath();

    if (getPath().equals(path)) {
      addChange(child.getChangeFlags());
    }
    // only add this child if it belongs in this subtree
    else if ( this.getPath().isPrefixOf(path) ) {
      
      ResourceEvent curChild;
      boolean inserted = false;

      // look in my children to figure out where to put the new child
      // start iterating at the end so that when we add children we won't have
      // to worry about processing them.
      for(int i = children.size() -1; i >= 0; i--) {
        curChild = (ResourceEvent)this.children.get(i);

        if (curChild.equals(child)) {
          logger.debug("updating change flags. current: " + curChild.getChangeFlags() + ", adding: " + child.getChangeFlags());
          curChild.addChange(child.getChangeFlags());
          inserted = true;
        } else if (curChild.getPath().isPrefixOf(child.getPath())) {
          curChild.insertChild(child);
          inserted = true;
        } else if (child.getPath().isPrefixOf(curChild.getPath())) {
          this.children.remove(curChild);
          if (!children.contains(child)) {
            this.children.add(child);
          }
          child.insertChild(curChild);
          inserted = true;
        }
      }

      if (!inserted && !this.children.contains(child)) {
        this.children.add(child);
      }
    }
  }

  /**
   * Method getChild.
   * @param path CmsPath
   * @return ResourceEvent
   */
  public ResourceEvent getChild(CmsPath path) {
    // TODO: is this method intended to be defined in the IResourceEvent interface?
    ResourceEvent curChild;
    ResourceEvent childSought = null;

    // iterate over all children looking to see if any of them
    // have the path that we are looking for
    for (Iterator iter = this.children.iterator(); iter.hasNext() && childSought == null;) {
      curChild = (ResourceEvent) iter.next();

      if (curChild.getPath().equals(path)) {
        childSought = curChild;
      }
    }

    return childSought;
  }

  /**
   * Method removeChild.
   * @param child ResourceEvent
   */
  public void removeChild(ResourceEvent child) {
    // TODO: is this method intended to be defined in the IResourceEvent interface?
    // TODO: is this method intended to be recursive?
    this.children.remove(child);
  }

  /**
   * Method getChildren.
   * @return Collection<IResourceEvent>
   * @see gov.pnnl.cat.core.resources.events.IResourceEvent#getChildren()
   */
  public Collection<IResourceEvent> getChildren() {
    return this.children;
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return this.path.toDisplayString();
  }
}
