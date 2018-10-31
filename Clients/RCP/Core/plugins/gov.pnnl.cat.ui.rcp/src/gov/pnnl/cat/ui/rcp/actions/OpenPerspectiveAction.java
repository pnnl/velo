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

import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 */
public class OpenPerspectiveAction extends Action {
  
  private final IWorkbenchWindow window;
  private final String id;
  private IPerspectiveDescriptor desc;
  
  /**
   * Constructor for OpenPerspectiveAction.
   * @param window IWorkbenchWindow
   * @param id String
   */
  public OpenPerspectiveAction(IWorkbenchWindow window, String id){
    this.window = window;
    this.id = id;
    
    desc = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id);
    if(desc != null){
      setText(desc.getLabel());
      setImageDescriptor(desc.getImageDescriptor());
    }
  }
  
  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run(){
    try{
      PlatformUI.getWorkbench().showPerspective(id, window);
    }catch(WorkbenchException e){
      ToolErrorHandler.handleError("An error occurred trying to open the perspective.", e, true);
    }
  }

}
