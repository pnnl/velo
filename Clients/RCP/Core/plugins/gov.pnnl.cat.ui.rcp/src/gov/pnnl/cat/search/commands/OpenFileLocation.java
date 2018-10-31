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
package gov.pnnl.cat.search.commands;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 */
public class OpenFileLocation extends AbstractHandler {
  
  /**
   * Method execute.
   * @param event ExecutionEvent
   * @return Object
   * @throws ExecutionException
   * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
      return null;
    }

    try {
      IResource resource = RCPUtil.getResource(selection.getFirstElement());
      
      // first make sure document library perspective is active
      String perspectiveId = RCPUtil.getDocLibPerspectiveProvider().getPerspectiveID(resource);
      PlatformUI.getWorkbench().showPerspective(perspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
      RCPUtil.selectResourceInTree(resource);
      
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Failed to open folder.", e, true);
    }
    return null;
  }
 
  
  
}

