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
package gov.pnnl.cat.actions.tagtimer;

import gov.pnnl.cat.jobs.alerts.TagTimerAlertWork;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * When we clean up links for deleted content nodes, we have to perform a search, and this takes too long to embed inside a transaction. Therefore, we call this action so we can do the cleanup asynchronously.
 * @version $Revision: 1.0 $
 */
public class ForceTagTimerAlertsAction extends ActionExecuterAbstractBase {

  private static final Log logger = LogFactory.getLog(ForceTagTimerAlertsAction.class);

  private TagTimerAlertWork tagTimerAlertWork;

  /**
   * Define the parameters that can be passed into this action
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
//    if (logger.isDebugEnabled())
//      logger.debug("setting parameter list");
//    ParameterDefinitionImpl param = new ParameterDefinitionImpl("action", DataTypeDefinition.TEXT, true, "action");
//    paramList.add(param);
  }

  /**
   * Method executeImpl.
   * @param ruleAction Action
   * @param nodeActedUpon NodeRef
   */
  @Override
  protected void executeImpl(final Action ruleAction, final NodeRef nodeActedUpon) {
    new Thread(new Runnable() {
      public void run() {
        try {
          AuthenticationUtil.setRunAsUserSystem();
          tagTimerAlertWork.run();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  /**
   * Method setTagTimerAlertWork.
   * @param tagTimerAlertWork TagTimerAlertWork
   */
  public void setTagTimerAlertWork(TagTimerAlertWork tagTimerAlertWork) {
    this.tagTimerAlertWork = tagTimerAlertWork;
  }


}
