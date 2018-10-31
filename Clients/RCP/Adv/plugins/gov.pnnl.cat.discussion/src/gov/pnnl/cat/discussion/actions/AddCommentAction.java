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
package gov.pnnl.cat.discussion.actions;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.discussion.views.DiscussionDialog;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 */
public class AddCommentAction implements IViewActionDelegate {
  private IViewPart view;
  private IResource currentSelection;

  /**
   * Method init.
   * @param view IViewPart
   * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
   */
  public void init(IViewPart view) {
    this.view = view;
  }

  /**
   * Method run.
   * @param action IAction
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    DiscussionDialog newMessageDialog = new DiscussionDialog(
        view.getSite().getShell(),
        currentSelection);
    newMessageDialog.open();
  }

  /**
   * Method selectionChanged.
   * @param action IAction
   * @param selection ISelection
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    currentSelection = null;

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structSel = (IStructuredSelection) selection;
      currentSelection = RCPUtil.getResource(structSel.getFirstElement());
    }
  }

}
