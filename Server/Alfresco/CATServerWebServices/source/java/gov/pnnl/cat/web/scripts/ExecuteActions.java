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
/**
 * 
 */
package gov.pnnl.cat.web.scripts;

import java.io.File;

import org.alfresco.service.cmr.action.ActionService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Execute an action.  For now this web script just forwards calls to ActionWebService to
 * avoid axis calls from client.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class ExecuteActions extends AbstractCatWebScript {
  private ActionService actionService;
  

  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

//    ObjectMapper mapper = new ObjectMapper();
//    ExecuteActionsRequest actionInfo = mapper.readValue(requestContent, ExecuteActionsRequest.class);
//    ActionExecutionResult[] result = executeActionsImpl(actionInfo.getPredicate(), actionInfo.getWebServiceActions());

    return null;

  }

//  public ActionExecutionResult[] executeActionsImpl(Predicate predicate, Action[] webServiceActions) throws RemoteException, ActionFault
//  {
//    List<ActionExecutionResult> results = new ArrayList<ActionExecutionResult>(10);
//
//    // Resolve the predicate to a list of nodes
//    List<NodeRef> nodeRefs = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
//    for (NodeRef nodeRef : nodeRefs)
//    {
//      // Create the execution result object and set the action reference
//      ActionExecutionResult executionResult = new ActionExecutionResult();
//      executionResult.setReference(Utils.convertToReference(this.nodeService, this.namespaceService, nodeRef));
//
//      // Tyr and execute the actions
//      List<Action> executedActions = new ArrayList<Action>(10);
//      for (Action webServiceAction : webServiceActions)
//      {
//        // Get the repository action object
//        org.alfresco.service.cmr.action.Action action = convertToAction(webServiceAction);
//
//        // TODO what about condition inversion
//        if (this.actionService.evaluateAction(action, nodeRef) == true)
//        {
//          // Execute the action (now that we know the conditions have been met)
//          this.actionService.executeAction(action, nodeRef, false);   
//
//          // Add the result value to the executed action for return to the client
//          Serializable result = action.getParameterValue(ActionExecuter.PARAM_RESULT);
//          if (result != null)
//          {
//            // Convert the result to a string value
//            String convertedValue = DefaultTypeConverter.INSTANCE.convert(String.class, result);
//            NamedValue convertedNameValue = new NamedValue(ActionExecuter.PARAM_RESULT, false, convertedValue, null);
//
//            // Append the new value to the current parameter array
//            NamedValue[] currentValues = webServiceAction.getParameters();
//            NamedValue[] updatedValues = new NamedValue[currentValues.length+1];                      
//            int index = 0;
//            for (NamedValue value : currentValues) 
//            {
//              updatedValues[index] = value;
//              index ++;
//            }
//            updatedValues[index] = convertedNameValue;
//
//            // Set the updated parameter values
//            webServiceAction.setParameters(updatedValues);
//
//          }
//
//          // Add the executed action to the result list
//          executedActions.add(webServiceAction);
//        }
//      }
//
//      // Set the executed actions on the execution result object
//      org.alfresco.repo.webservice.action.Action[] executedWebServiceActions = (org.alfresco.repo.webservice.action.Action[])executedActions.toArray(new org.alfresco.repo.webservice.action.Action[executedActions.size()]);
//      executionResult.setActions(executedWebServiceActions);
//
//      // Add the execution object to the result list
//      results.add(executionResult);
//    }
//    return (ActionExecutionResult[])results.toArray(new ActionExecutionResult[results.size()]);
//  }  


}
