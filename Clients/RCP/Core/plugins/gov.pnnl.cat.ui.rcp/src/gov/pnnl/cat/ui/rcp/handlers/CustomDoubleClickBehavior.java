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
 * So deployments can add custom functionality for double clicking 
 * items in explorer trees.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public interface CustomDoubleClickBehavior {
    
  /**
   * Perform some custom double click behavior on the resource.
   * @param source
   * @return true if the double click is finished and the default behavior no longer applies.
   *  If the resource still needs default double click after running this custom behavior,
   *  return false.
   * @throws RuntimeException - throw an exception if there was a problem
   */
  public boolean doubleClick(IResource source) throws RuntimeException;


}
