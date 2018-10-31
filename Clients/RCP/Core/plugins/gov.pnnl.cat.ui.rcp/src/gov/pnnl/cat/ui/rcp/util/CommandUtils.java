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
package gov.pnnl.cat.ui.rcp.util;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.ResourceStructuredSelection;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 */
public class CommandUtils {

  /**
   * Method executeEclipseCommand.
   * @param resource IResource
   * @param handler IHandler
   * @param cmdId String
   */
  public static void executeEclipseCommand(final IResource resource, final IHandler handler, final String cmdId) {

    try {
      ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
      IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);

      // Force the handler to be active because it deactivates when we are in a swing window :(
      IHandlerActivation activation = null;
      if(handler != null) {
        activation = handlerService.activateHandler(cmdId, handler);
      }
      Command cmd = cmdService.getCommand(cmdId);

      Map<String, String> params = new HashMap<String, String>();

      // get the application context
      IEvaluationContext appContext = new EvaluationContext(handlerService.getCurrentState(), Collections.EMPTY_LIST);

      // set up the appContext as we would want it.
      if(resource != null) {
        IStructuredSelection selection = new ResourceStructuredSelection(resource);
        appContext.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
      }

      ExecutionEvent event = new ExecutionEvent(cmd, params, null, appContext);   
      cmd.executeWithChecks(event);

      // Now we have to force the handler to deactivate or it will always be enabled in the Eclipse window
      if(activation != null) {
        handlerService.deactivateHandler(activation);
      }
    } catch(Exception e) {
      ToolErrorHandler.handleError("Failed to execute " + cmdId , e, true);
    }

  }
  
  public static void executeEclipseCommand(final String cmdId) throws Exception {

      IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
      handlerService.executeCommand(cmdId, null);
  }
}
