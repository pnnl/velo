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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 */
public class PropertiesActionGroup extends ActionGroup {

  private IViewPart viewPart;
//  private ISelectionProvider selectionProvider;

  /**
   * Constructor for PropertiesActionGroup.
   * @param viewPart IViewPart
   * @param selectionProvider ISelectionProvider
   */
  public PropertiesActionGroup(IViewPart viewPart, ISelectionProvider selectionProvider){
    this.viewPart = viewPart;
//    this.selectionProvider = selectionProvider;
    createSiteActions(viewPart.getSite(), selectionProvider);
  }
  
  
  /**
   * Method createSiteActions.
   * @param site IWorkbenchSite
   * @param selectionProvider ISelectionProvider
   */
  private void createSiteActions(IWorkbenchSite site, ISelectionProvider selectionProvider) {
    //TODO: do more actions here
    setAction(ActionFactory.PROPERTIES.getId(), new PropertyDialogAction(site, selectionProvider)); 
  }
  
  /**
   * Method setAction.
   * @param actionID String
   * @param action IAction
   */
  public void setAction(String actionID, IAction action) {
//    if (action == null) {
//      Object removedAction= fActionMap.remove(actionID);
//      fUpdateables.remove(removedAction);
//    } else {
//      fActionMap.put(actionID, action);
//      if (action instanceof IUpdate) {
//        fUpdateables.add(action);
//      }
//    }
//    if (fgGlobalActionIds.contains(actionID)) {
      IActionBars actionBars = viewPart.getViewSite().getActionBars(); 
      actionBars.setGlobalActionHandler(actionID, action);
      actionBars.updateActionBars();
//    }
  } 
}
