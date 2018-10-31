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
package gov.pnnl.cat.core.resources.events;

import gov.pnnl.velo.model.CmsPath;

import java.util.Collection;
import java.util.Map;

/**
 * High level Event that indicates all changes that were made to
 * a resource.  Change details are very high level (only a flag).
 * 
 * TODO: Later we can add more change detail in a Hashtable or something
 * @version $Revision: 1.0 $
 */
public interface IResourceEvent {

  // Do we care what user caused the event?  JCR passes this info along, so
  // we can add it later if we want
  
  /**
   * Change flag - new resource added.  This could mean a new
   * subtree was added, if this resource is a folder and has
   * children.
   */
  public static final int ADDED = 1;
  
  /**
   * Change flag - resource (and all its subtree) removed.
   */
  public static final int REMOVED = 2;
  
  /**
   * Change flag - existing resource had its content changed.
   * If resource is a folder, then this means its children changed.
   */
  public static final int CONTENT_CHANGED = 4;
  
  /**
   * Change flag - existing resource had its properties changed.
   * Later we probably should provide which properties changed.
   */
  public static final int PROPERTY_CHANGED = 8;

  /**
   * Change flag - existing link had its target changed.
   */
  public static final int TARGET_CHANGED = 16;

  /**
   * Change flag - existing resource had its aspects changed.
   */
  public static final int ASPECTS_CHANGED = 32;
  
  //TODO: add a MOVED flag

  public static final String PROP_TARGET_PATH = "target_path";

  /**
   * Gets the CmsPath of the affected resource.  We can change this
   * to return an IResource if that's easier.
   * @return CmsPath
   */
  public CmsPath getPath();
  
  /**
   * So you can find out what changes are associated with this
   * event.  Later we could return ResourceChange objects
   * if we need more detail.
   * 
  
   * @param changeMask int
   * @return true if this event represents any one of the types
   * in the filter mask. */
  public boolean hasChange(int changeMask);

  /**
   * Return the set of change flags for this resource.
   * @return int
   */
  public int getChangeFlags();

  /**
   * Returns the unique identifier for this resource.
   * @return String
   */
  public String getUUID();

  /**
   * Returns a the children for this resource event.
   * @return Collection<IResourceEvent>
   */
  public Collection<IResourceEvent> getChildren();

  /**
   * Method insertChild.
   * @param event IResourceEvent
   */
  public void insertChild(IResourceEvent event);

  /**
   * Adds a property to the resource event.
   * <p>
   * This method is not thread-safe.
   * @param changeFlag
   * @param key
   * @param value
   */
  public void addProperty(int changeFlag, String key, String value);

  /**
   * Method getProperties.
   * @param changeFlag int
   * @return Map<String,String>
   */
  public Map<String, String> getProperties(int changeFlag);
  
  /**
   * @return the id of the user who caused this event
   */
  public String getPerpetrator();
}
