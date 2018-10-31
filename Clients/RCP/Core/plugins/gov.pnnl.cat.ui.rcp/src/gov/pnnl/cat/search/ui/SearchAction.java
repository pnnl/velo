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
package gov.pnnl.cat.search.ui;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 */
public class SearchAction implements IWorkbenchWindowActionDelegate {

  protected static Logger logger = CatLogger.getLogger(SearchAction.class);
  
  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {
    // TODO Auto-generated method stub

  }

  /**
   * Method init.
   * @param window IWorkbenchWindow
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    // TODO Auto-generated method stub

  }

  /**
   * Method run.
   * @param action IAction
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    // Need to switch to the search perspective
    try {
      IWorkbench workbench = PlatformUI.getWorkbench();
//      IWorkbenchPage visiblePage = 
        workbench.showPerspective(CatPerspectiveIDs.SEARCH, 
            workbench.getActiveWorkbenchWindow(), 
            null);    
    } catch (Exception e) {
      //EZLogger.logError(e, "Error launching Search Perspective.");
      logger.error("Error launching Search Perspective", e);
    }

  }

  /**
   * Method selectionChanged.
   * @param action IAction
   * @param selection ISelection
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    // TODO Auto-generated method stub

  }

}
