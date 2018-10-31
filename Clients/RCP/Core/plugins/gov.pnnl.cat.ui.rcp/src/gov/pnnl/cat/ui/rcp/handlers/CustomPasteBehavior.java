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
package gov.pnnl.cat.ui.rcp.handlers;

import gov.pnnl.cat.core.resources.IResource;

/**
 */
public interface CustomPasteBehavior {
  
  /**
   * Perform some custom paste behavior on the resource.
   * @param source - the resource that will be copied or moved
   * @param destinationParent - the parent folder where the source will be moved or copied to
   * @param isMove - true if the resource is being moved, false if copied
   * @return true if the resource was pasted and the default paste no longer applies.
   *  If the resource still needs to be pasted after running this custom behavior,
   *  return false.
   * @throws RuntimeException - throw an exception if there was a problem
   */
  public boolean paste(IResource source, IResource destinationParent, boolean isMove) throws RuntimeException;


}
