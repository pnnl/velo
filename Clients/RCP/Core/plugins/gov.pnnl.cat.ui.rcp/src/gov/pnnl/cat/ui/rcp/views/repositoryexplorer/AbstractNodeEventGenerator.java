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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer;

import javax.swing.event.EventListenerList;

import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class AbstractNodeEventGenerator extends PlatformObject implements INodeEventGenerator {

  public EventListenerList evSystemListenerList = new EventListenerList();
  
  /**
   *  Add Listener<br>
   *  Add a listener to the the event list.
   * @param listener  the listener to add to the list
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.INodeEventGenerator#addNodeChangeListener(INodeChangeListener)
   */
  public void addNodeChangeListener(INodeChangeListener listener) {
    synchronized (evSystemListenerList) {
      evSystemListenerList.add(INodeChangeListener.class, listener);
    }
  }

  /**
   *  Remove Listener<br>
   *  Remove a listener from the the event list.
   *
   *  @param listener  the listener to remove from the list
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.INodeEventGenerator#removeNodeChangeListener(INodeChangeListener)
   */
  public void removeNodeChangeListener(INodeChangeListener listener) {
    synchronized (evSystemListenerList) {
      evSystemListenerList.remove(INodeChangeListener.class, listener);
    }
  } 
  
  /**
   * Method fireChildrenChangeEvent.
   * @param hasChildren boolean
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.INodeEventGenerator#fireChildrenChangeEvent(boolean)
   */
  public void fireChildrenChangeEvent(boolean hasChildren){
    Object[] listeners = evSystemListenerList.getListenerList();
    // Each listener occupies two elements - the first is the listener class
    // and the second is the listener instance
    for (int i = 0; i < listeners.length; i += 2) {
      if (listeners[i] == INodeChangeListener.class) {
        ( (INodeChangeListener) listeners[i + 1]).childrenStateChanged(hasChildren);
      }
    }  
  }

}
