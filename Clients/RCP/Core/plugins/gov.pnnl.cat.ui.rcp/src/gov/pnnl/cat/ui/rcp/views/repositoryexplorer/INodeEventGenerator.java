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

/**
 */
public interface INodeEventGenerator {
  /**
   *  Add Listener<br>
   *  Add a listener to the the event list.
  
   * @param listener  the listener to add to the list
   */
  public void addNodeChangeListener(INodeChangeListener listener);

  /**
   *  Remove Listener<br>
   *  Remove a listener from the the event list.
   *
   *  @param listener  the listener to remove from the list
   */
  public void removeNodeChangeListener(INodeChangeListener listener);

  /**
   * Method fireChildrenChangeEvent.
   * @param hasChildren boolean
   */
  public void fireChildrenChangeEvent(boolean hasChildren);
}
