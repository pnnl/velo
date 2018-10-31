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
package gov.pnnl.cat.actions;


import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Remove an aspect from nodes in a subtree.
 *
 * @version $Revision: 1.0 $
 */
public class RecursiveRemoveAspectAction extends ActionExecuterAbstractBase
{

  /**
   * The logger
   */
  private static Log logger = LogFactory.getLog(RecursiveRemoveAspectAction.class); 

  /**
   * Action constants
   */
  public static final String PARAM_ASPECT_NAME = "aspect-name";
  public static final String DISPLAY_NAME_ASPECT = "Aspect";
  
  private TransactionService transactionService;
  private NodeService nodeService;

  /**
   * @param transactionService the transactionService to set
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * @param nodeService the nodeService to set
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Add parameter definitions
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
  {
    ParameterDefinitionImpl param = new ParameterDefinitionImpl(
        PARAM_ASPECT_NAME, 
        DataTypeDefinition.QNAME,
        true,
        DISPLAY_NAME_ASPECT);
    paramList.add(param);

  }

  /**
   * Actioned upon node ref is our .category file
  
   * @param action Action
   * @param actionedUponNodeRef NodeRef
   * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef) */
  @Override
  protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
    if(logger.isDebugEnabled()) {
      logger.debug("calling executeImpl");
    }

    QName aspect = (QName)action.getParameterValue(PARAM_ASPECT_NAME);

    recursiveRemoveAspect(actionedUponNodeRef, aspect);
  }

  /**
   * Method recursiveRemoveAspect.
   * @param nodeRef NodeRef
   * @param aspect QName
   */
  private void recursiveRemoveAspect(final NodeRef nodeRef, final QName aspect) {

    TransactionUtil.executeInNonPropagatingUserTransaction(
        transactionService,
        new TransactionUtil.TransactionWork<Object>()
        {
          public Object doWork()
          {
            if (nodeService.hasAspect(nodeRef, aspect)) {
              nodeService.removeAspect(nodeRef, aspect);
            }            
            return null;
          }
        });


    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);

    for (ChildAssociationRef childAssoc : children) {
      recursiveRemoveAspect(childAssoc.getChildRef(), aspect);
    }
    
  }
  
}
