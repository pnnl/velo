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

import java.util.Iterator;

/**
 * Hopefully, this class will let you quickly 
 * find the events you want from a potentially
 * large batch notification sent by the back end
 * repository.
 * 
 * Lets you get an iterator over the events in a batch
 * notification.  The iterator can be filtered based upon
 * an event type mask.  Or you can just look up the
 * event for a single resource.
 *
 * TODO: this interface is generic enough, that we may want
 * to create more than one implementation: one for 
 * resource events, one for user events, one for team events,
 * etc.
 * @version $Revision: 1.0 $
 */
public interface IBatchNotification extends Iterable<IResourceEvent> {

  /**
   * Gets all events
   * @return Iterator<IResourceEvent>
   */
  public Iterator<IResourceEvent> getAllEvents();

  /**
   * Gets all events
   * @return Iterator<IResourceEvent>
   */
  public Iterator<IResourceEvent> getNonRedundantEvents();
  
//  /**
//   * Filter on the event type
//   * @see IResourceEvent for types
//   * @param eventTypeMask
//   * @return
//   * @param path CmsPath
//   * @return IResourceEvent
//   */
//  public Iterator<IResourceEvent> getNonRedundantEvents(int eventTypeMask);
   
  /**
   * Only get the event for a single resource
   * @param path
   * @return null if this resource did not change
   */
  public IResourceEvent findEvent(CmsPath path);
  
}
