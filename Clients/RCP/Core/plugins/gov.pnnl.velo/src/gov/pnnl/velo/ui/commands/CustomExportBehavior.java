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
package gov.pnnl.velo.ui.commands;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 */
public interface CustomExportBehavior {
  
  /**
   * Perform some custom export behavior on the resource.
   * @param source
   * @return true if the resource was exported and the default export no longer applies.
   *  If the resource still needs to be exported after running this custom behavior,
   *  return false.
   * @throws RuntimeException - throw an exception if there was a problem
   */
  public boolean export(IStructuredSelection selection) throws RuntimeException;

}
