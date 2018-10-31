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

import java.util.EventListener;

/**
 * A resource listener is notified of any underlying changes to the
 * back-end repository.
 * 
 * @see IResourceEvent
 * @see IResourceEventIterator
 * @version $Revision: 1.0 $
 */
public interface IResourceEventListener extends EventListener {

  /**
   * New resource change events have come from the server
   * @param events
   */
  public void onEvent(IBatchNotification events);
  
  /**
   * The cache has been cleared, so existing resource data could be stale
   */
  public void cacheCleared();
  
}
