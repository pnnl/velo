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

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;

/**
 */
public class ActionDelegateViewSetup {

  private static ActionDelegate copyActionDelegate = new ActionDelegate();;
  private static ActionDelegate pasteActionDelegate = new ActionDelegate();;
  private static ActionDelegate cutActionDelegate = new ActionDelegate();;
  private static ActionDelegate deleteActionDelegate = new ActionDelegate();;
  private static ActionDelegate selectAllActionDelegate = new ActionDelegate();;
  private static ActionDelegate renameActionDelegate = new ActionDelegate();;
  private static ActionDelegate saveAsActionDelegate = new ActionDelegate();
  
  /**
   * Constructor for ActionDelegateViewSetup.
   * @param bars IActionBars
   */
  public ActionDelegateViewSetup(IActionBars bars){
    
    bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyActionDelegate);
    bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteActionDelegate);
    bars.setGlobalActionHandler(ActionFactory.CUT.getId(), cutActionDelegate);
    bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteActionDelegate);
    bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllActionDelegate);
    bars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameActionDelegate);
    bars.setGlobalActionHandler(ActionFactory.SAVE_AS.getId(), saveAsActionDelegate);
    bars.updateActionBars();
  }
 
  /**
  
   * @return the saveAsActionDelegate */
  public static ActionDelegate getSaveAsActionDelegate() {
    return saveAsActionDelegate;
  }

  /**
   * Method getCopyActionDelegate.
   * @return ActionDelegate
   */
  public static ActionDelegate getCopyActionDelegate() {
    return copyActionDelegate;
  }

  /**
   * Method getCutActionDelegate.
   * @return ActionDelegate
   */
  public static ActionDelegate getCutActionDelegate() {
    return cutActionDelegate;
  }

  /**
   * Method getDeleteActionDelegate.
   * @return ActionDelegate
   */
  public static ActionDelegate getDeleteActionDelegate() {
    return deleteActionDelegate;
  }

  /**
   * Method getPasteActionDelegate.
   * @return ActionDelegate
   */
  public static ActionDelegate getPasteActionDelegate() {
    return pasteActionDelegate;
  }

  /**
   * Method getSelectAllActionDelegate.
   * @return ActionDelegate
   */
  public static ActionDelegate getSelectAllActionDelegate() {
    return selectAllActionDelegate;
  }
  
  /**
   * Method getRenameActionDelegate.
   * @return ActionDelegate
   */
  public static ActionDelegate getRenameActionDelegate() {
    return renameActionDelegate;
  }
//  public static void setCurrentActionListenerForAllActions(ContentViewer viewer) {
//    copyActionDelegate.setCurrentActionListener(viewer);
//    pasteActionDelegate.setCurrentActionListener(viewer);
//    cutActionDelegate.setCurrentActionListener(viewer);
//    deleteActionDelegate.setCurrentActionListener(viewer);
//
//  }
  
}
