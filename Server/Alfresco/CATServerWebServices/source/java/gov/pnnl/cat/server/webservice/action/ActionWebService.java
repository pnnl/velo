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
package gov.pnnl.cat.server.webservice.action;

import gov.pnnl.cat.server.webservice.util.ExceptionUtils;

import java.rmi.RemoteException;

import org.alfresco.repo.webservice.action.Action;
import org.alfresco.repo.webservice.action.ActionExecutionResult;
import org.alfresco.repo.webservice.action.ActionFault;
import org.alfresco.repo.webservice.types.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Overload this class so we can get rid of overarching transactions.  If the
 * actions are executed over a large number of nodes, the tx are too big.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class ActionWebService extends org.alfresco.repo.webservice.action.ActionWebService {

  /** Log */
  private static Log logger = LogFactory.getLog(ActionWebService.class);

  /**
   * TODO: instead of overloading this method, create a new method that doesn't wrap in a
   * tx, so users will have both options available.
   * @param predicate Predicate
   * @param webServiceActions Action[]
   * @return ActionExecutionResult[]
   * @throws RemoteException
   * @throws ActionFault
   * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#executeActions(Predicate, Action[])
   */
  @Override
  public ActionExecutionResult[] executeActions(Predicate predicate, Action[] webServiceActions) throws RemoteException, ActionFault {
    logger.debug("trying to execute actions");
    
    try {
      return executeActionsImpl(predicate, webServiceActions);

    } catch (Throwable exception) {
      logger.error("Unexpected error occurred", exception);
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new ActionFault(0, rootCause.toString());
    }
  }

}
