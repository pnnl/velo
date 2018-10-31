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
package gov.pnnl.velo.bootstrap;

import gov.pnnl.cat.pipeline.FileProcessingInfo;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.util.VeloTifConstants;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 */
public class DisconnectedJobsChecker extends AbstractLifecycleBean {
  protected TransactionService transactionService;
  protected NodeService nodeService;
  protected SearchService searchService;
  
  private static final Log logger = LogFactory.getLog(DisconnectedJobsChecker.class);
  
  /**
   * Method onBootstrap.
   * @param event ApplicationEvent
   */
  @Override
  protected void onBootstrap(ApplicationEvent event) {

    RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
      
      public Object execute() throws Throwable {
        ResultSet results = null;
        try {
          // Query to find any jobs that are running  
          String query = "@velo\\:status:(\"In Queue\" OR \"Running\" OR \" Job Complete. Post-processing results\")";
          //String query = "@velo\\:status:\"Running\"";
          
          results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
          List<FileProcessingInfo> infos = new ArrayList<FileProcessingInfo>();
          List<NodeRef> nodeRefs = results.getNodeRefs();
          
          for (NodeRef nodeRef : nodeRefs) {      
            if(nodeService.exists(nodeRef)){
              nodeService.setProperty(nodeRef, QName.createQName(VeloTifConstants.JOB_STATUS), VeloTifConstants.STATUS_DISCONNECTED);
            }
          }
          return null;
        
        } finally {
          if (results != null) {
            // MUST close the results or Alfresco will keep the index file
            // handle open
            results.close();
          }
        }
      }
    };

    try {
      transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

    } catch (Throwable e) {
      logger.error("Failed to execute query", e);
      e.printStackTrace();
    }


  }
  
  /**
   * Method onShutdown.
   * @param event ApplicationEvent
   */
  @Override
  protected void onShutdown(ApplicationEvent event) {
    // TODO Auto-generated method stub

  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

}
