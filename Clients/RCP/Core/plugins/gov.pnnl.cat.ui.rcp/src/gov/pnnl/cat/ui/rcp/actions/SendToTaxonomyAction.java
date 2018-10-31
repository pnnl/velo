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

import gov.pnnl.cat.ui.rcp.wizards.SendToTaxonomyWizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class SendToTaxonomyAction implements IViewActionDelegate{

  private IWorkbenchWindow window;
  private IStructuredSelection selection;
  
  public SendToTaxonomyAction(){
    
  }
  
  /**
   * Method init.
   * @param view IViewPart
   * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
   */
  public void init(IViewPart view) {
    // TODO Auto-generated method stub
    this.window = view.getViewSite().getWorkbenchWindow();
  }

  /**
   * Method run.
   * @param action IAction
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    // TODO Auto-generated method stub
    SendToTaxonomyWizard sttWizard = new SendToTaxonomyWizard();
    sttWizard.init(window.getWorkbench(), selection);
    
    WizardDialog dialog = new WizardDialog(window.getShell(), sttWizard);
    dialog.setPageSize(100, 150);
    dialog.setMinimumPageSize(100, 150);
    
    if (dialog.open() != Dialog.OK) {
      return;
    }
  }

  public void dispose() {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Method selectionChanged.
   * @param action IAction
   * @param selection ISelection
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    // TODO Auto-generated method stub
    if (selection instanceof IStructuredSelection) {
      this.selection = (IStructuredSelection) selection;
      
    }
  }

}
