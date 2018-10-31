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
package gov.pnnl.cat.ui.rcp.actions;

import gov.pnnl.cat.core.resources.IResourceManager;

/**
 */
public abstract class RepositoryAction extends JobAction {
  private IResourceManager mgr;

//  public RepositoryAction() {
//    super();
//  }

//  public RepositoryAction(String text, ImageDescriptor image) {
//    super(text, image);
//  }
//
//  public RepositoryAction(String text, int style) {
//    super(text, style);
//  }
//
  /**
   * Constructor for RepositoryAction.
   * @param text String
   */
  public RepositoryAction(String text) {
    super(text);
  }

  /**
   * Method setResourceTreeManager.
   * @param mgr IResourceManager
   */
  public void setResourceTreeManager(IResourceManager mgr) {
    this.mgr = mgr;
  }

  /**
   * Method getResourceManager.
   * @return IResourceManager
   */
  public IResourceManager getResourceManager() {
    return this.mgr;
  }
}
