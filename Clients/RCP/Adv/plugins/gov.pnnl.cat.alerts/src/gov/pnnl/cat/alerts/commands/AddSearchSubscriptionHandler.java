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
package gov.pnnl.cat.alerts.commands;

import gov.pnnl.cat.alerts.wizards.NewSearchSubscriptionWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 */
public class AddSearchSubscriptionHandler extends AbstractHandler {

  /**
   * Method execute.
   * @param event ExecutionEvent
   * @return Object
   * @throws ExecutionException
   * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    // All we do here is execute the newWizard command, passing in
    // the New Search Subscription ID as a parameter.
    // The wizard takes care of the rest.
    try {
      ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
      IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
      Command newWizardCmd = cmdService.getCommand("org.eclipse.ui.newWizard");

      IParameter viewIdParm = newWizardCmd.getParameter("newWizardId");

      Parameterization parm = new Parameterization(viewIdParm, NewSearchSubscriptionWizard.ID);
      ParameterizedCommand parmCommand = new ParameterizedCommand(newWizardCmd, new Parameterization[] { parm });
      handlerService.executeCommand(parmCommand, null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Method isEnabled.
   * @return boolean
   * @see org.eclipse.core.commands.IHandler#isEnabled()
   */
  @Override
  public boolean isEnabled() {
    // TODO: enable only when subscription-supporting search results are active
    // Update: Do we really want to do that? That might be nice, but
    //         displaying an error message in the wizard does a good job
    //         of explaining why the subscription cannot be created.
    //         I suppose we could display the same message in a tooltip,
    //         but the link to open the search perspective is pretty convenient.
    return true;
  }

}
