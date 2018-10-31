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
package gov.pnnl.cat.patches;

import gov.pnnl.cat.actions.crawler.TreeCrawlerActionExecutor;
import gov.pnnl.cat.util.NodeUtils;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This patch propagates the owner property down the team and user
 * home folder hierarchy (by default, owner is not inherited).
 *
 * @version $Revision: 1.0 $
 */
public class HashPatch extends AbstractPatch {

  // Logger
  private static final Log logger = LogFactory.getLog(HashPatch.class);
 
  protected NodeUtils nodeUtils;
  protected ActionService actionService;
 
  /**
   * @param actionService the actionService to set
   */
  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Ensure that required properties have been set
   * @throws Exception
   */
  protected void checkRequiredProperties() throws Exception
  {
      checkPropertyNotNull(nodeService, "nodeService");
      checkPropertyNotNull(nodeUtils, "nodeUtils");
  }
  
  
  /**
   * Method applyInternal.
   * @return String
   * @throws Exception
   */
  @Override
  protected String applyInternal() throws Exception {
    String actionName = "tree-crawler";
    String visitorName = "hashNodeVisitor";
    Action action = actionService.createAction(actionName);
    action.setParameterValue(TreeCrawlerActionExecutor.VISITOR_ID, visitorName);
    action.setParameterValue(TreeCrawlerActionExecutor.RUN_EACH_NODE_IN_TRANSACTION, "true");

    // only run on user and team documents
    NodeRef companyHome = nodeUtils.getCompanyHome();
    actionService.executeAction(action, companyHome, false, false);

    return "OK";
  }
 
}
