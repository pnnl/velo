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
package gov.pnnl.cat.ui.rcp.dialogs;

import java.util.List;

import gov.pnnl.cat.core.resources.IResource;

/**
 * Interface for providing validators to CAT resource
 * selection dialogs.  This enables callers to be able to validate the selection
 * before the user can hit OK.
 * @author D3K339
 *
 */
public interface ResourceSelectionValidator {

  /**
   * Validate that the selected resources are allowed.  
   * Could be one or more resources depending if the chooser was set
   * to select multiple or not.
   * @param selectedResources
   * @return String for the error message if not valid - if valid, return null
   */
  public String validateSelection(List<IResource> selectedResources);
  
  /**
   * Validate that the parent folder of the selection is valid (e.g., we are saving to
   * a valid folder)
   * @param parentFolder
   * @return String for the error message if not valid - if valid, return null
   */
  public String validateParentFolder(IResource parentFolder);
  
}
