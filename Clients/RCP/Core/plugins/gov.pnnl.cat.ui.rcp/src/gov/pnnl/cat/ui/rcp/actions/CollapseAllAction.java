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

import gov.pnnl.cat.ui.images.SharedImages;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 */
public class CollapseAllAction extends ViewerAction {

  public CollapseAllAction() {
    super("Collapse All");
    setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_COLLAPSE, SharedImages.CAT_IMG_SIZE_16));
    setToolTipText("Collapse all nodes on the tree.");
  }
  
  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run() {
    if (getViewer() instanceof TreeViewer) {
      ((TreeViewer)getViewer()).collapseAll();
    }
  }
  
  /**
   * Method updateEnabledStatus.
   * @param selection ISelection
   */
  public void updateEnabledStatus(ISelection selection) {
    if (getViewer() instanceof TreeViewer) {
      setEnabled(true);
    } else {
      setEnabled(false);
    }
  }
  
  /**
   * Method getPolicy.
   * @return int
   */
  public int getPolicy() {
    return 0;
  }

}
