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
package gov.pnnl.cat.pipeline.impl;

import gov.pnnl.cat.pipeline.FileProcessingInfo;
import gov.pnnl.cat.pipeline.FileProcessingJob;
import gov.pnnl.cat.pipeline.FileProcessingPipeline;
import gov.pnnl.cat.pipeline.FileProcessor;
import gov.pnnl.cat.pipeline.ProcessorSkippedException;
import gov.pnnl.cat.pipeline.ProcessorTimeoutException;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.NotificationUtils;
import gov.pnnl.cat.util.PrioritizedThreadFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class FileProcessingPipelineImpl implements FileProcessingPipeline, 
ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware,
InitializingBean {

  // Max limits for different file types to decide which thread pool to put them on
  private long maxRegularFileSize = 10000000;
  private long maxPdfFileSize = 1000000;// in cat-context.xml = 1000000;// = 1 MEG
  private long maxXlsxFileSize = 2000000;// 3 MEG still took forever, trying 2 meg
  
  // Logger
  private static final Log logger = LogFactory.getLog(FileProcessingPipelineImpl.class);

  // File processors
  private List<FileProcessor>fileProcessors = new ArrayList<FileProcessor>();
  private Map<Integer, List<FileProcessor>> processorsByPriority = new HashMap<Integer, List<FileProcessor>>();
  private List<Integer> sortedPriorities = new ArrayList<Integer>();

  // injected beans
  private NotificationUtils notificationUtils;
  private ThreadPoolExecutor bigFileThreadPool; 
  private String bigFileThreadPoolName;
  private ThreadPoolExecutor mediumPriorityThreadPool;
  private NodeService nodeService;
  private TransactionService transactionService;
  private ApplicationContext appContext;
  protected BehaviourFilter policyBehaviourFilter;
  

  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    bigFileThreadPoolName = ((PrioritizedThreadFactory)bigFileThreadPool.getThreadFactory()).getLabel();
  }

  /**
   * Method setNotificationUtils.
   * @param notificationUtils NotificationUtils
   */
  public void setNotificationUtils(NotificationUtils notificationUtils) {
    this.notificationUtils = notificationUtils;
  }

  /**
   * Method setBigFileThreadPool.
   * @param bigFileThreadPool ThreadPoolExecutor
   */
  public void setBigFileThreadPool(ThreadPoolExecutor bigFileThreadPool) {
    this.bigFileThreadPool = bigFileThreadPool;
  }

  /**
   * Method setMediumPriorityThreadPool.
   * @param mediumPriorityThreadPool ThreadPoolExecutor
   */
  public void setMediumPriorityThreadPool(ThreadPoolExecutor mediumPriorityThreadPool) {
    this.mediumPriorityThreadPool = mediumPriorityThreadPool;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method getMaxRegularFileSize.
   * @return long
   * @see gov.pnnl.cat.pipeline.FileProcessingPipeline#getMaxRegularFileSize()
   */
  public long getMaxRegularFileSize() {
    return maxRegularFileSize;
  }

  /**
   * Method setMaxRegularFileSize.
   * @param maxRegularFileSize long
   */
  public void setMaxRegularFileSize(long maxRegularFileSize) {
    this.maxRegularFileSize = maxRegularFileSize;
  }

  /**
   * Method getMaxPdfFileSize.
   * @return long
   * @see gov.pnnl.cat.pipeline.FileProcessingPipeline#getMaxPdfFileSize()
   */
  public long getMaxPdfFileSize() {
    return maxPdfFileSize;
  }

  /**
   * Method setMaxPdfFileSize.
   * @param maxPdfFileSize long
   */
  public void setMaxPdfFileSize(long maxPdfFileSize) {
    this.maxPdfFileSize = maxPdfFileSize;
  }

  /**
   * Method getMaxXlsxFileSize.
   * @return long
   * @see gov.pnnl.cat.pipeline.FileProcessingPipeline#getMaxXlsxFileSize()
   */
  public long getMaxXlsxFileSize() {
    return maxXlsxFileSize;
  }

  /**
   * Method setMaxXlsxFileSize.
   * @param maxXlsxFileSize long
   */
  public void setMaxXlsxFileSize(long maxXlsxFileSize) {
    this.maxXlsxFileSize = maxXlsxFileSize;
  }
  
  /**
   * Method setPolicyBehaviourFilter.
   * @param policyBehaviourFilter BehaviourFilter
   */
  public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
    this.policyBehaviourFilter = policyBehaviourFilter;
  }

  private void sortFileProcessors() {
    for (FileProcessor processor : fileProcessors) {
      Integer priority = processor.getPriority();

      // If two file processors have the same priority, the first one registered gets called first
      List<FileProcessor> priorityProcessors = processorsByPriority.get(priority);
      if(priorityProcessors == null) {
        priorityProcessors = new ArrayList<FileProcessor>();
        processorsByPriority.put(priority, priorityProcessors);
      }
      priorityProcessors.add(processor);
      if(!sortedPriorities.contains(priority)) {
        sortedPriorities.add(priority);
      }
    }

    // Make sure the keys are sorted
    if(sortedPriorities.size() > 1) {
      // sort throws NPE if you try to sort list of length 1
      Collections.sort(sortedPriorities);
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessingPipeline#submitProcessingJob(java.util.Collection)
   */
  @Override
  public void submitProcessingJob(Collection<FileProcessingInfo> files) {
    logger.debug("Adding: " + files.size() + " files to file processing pipeline");

    // Decide which thread pool to put the files in
    List<FileProcessingInfo> regularFiles = new ArrayList<FileProcessingInfo>();
    List<FileProcessingInfo> bigFiles = new ArrayList<FileProcessingInfo>();

    for (FileProcessingInfo fileInfo : files) {

      String mimetype = fileInfo.getMimetype();
      long fileSize = fileInfo.getFileSize();

      if (mimetype.equals(CatConstants.MIMETYPE_XLSX) && fileSize > maxXlsxFileSize) {
        bigFiles.add(fileInfo);

      } else if (mimetype.equals(MimetypeMap.MIMETYPE_PDF) && fileSize > maxPdfFileSize) {
        bigFiles.add(fileInfo);

      } else if(fileSize > maxRegularFileSize){
        bigFiles.add(fileInfo);

      } else {
        regularFiles.add(fileInfo);
      }

    }

    // Launch the appropriate file processing job
    if(regularFiles.size() > 0) {
      FileProcessingJob extractionJob = new FileProcessingJob(regularFiles, this);
      mediumPriorityThreadPool.execute(extractionJob);

    }
    if(bigFiles.size() > 0) {
      FileProcessingJob extractionJob = new FileProcessingJob(bigFiles, this);
      bigFileThreadPool.execute(extractionJob);

    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessingPipeline#processFiles(java.util.Collection)
   */
  @Override
  public void processFiles(Collection<FileProcessingInfo> files) {
    logger.debug("Processing batch of " + files.size() + " files.");
    
    // for each file
    for (FileProcessingInfo file : files) {
      boolean timedOut = false;
      
      // run each processor in order
      for (Integer priority : sortedPriorities) {
        List<FileProcessor> processors = processorsByPriority.get(priority);
        for (FileProcessor fileProcessor : processors) {
          if(fileProcessor.isEnabled()) {
            
            // If a previous processor has not timed out or this is a short running processor
            if(!timedOut || fileProcessor.isMandatory() == true) {
              double start = System.currentTimeMillis();

              if(logger.isDebugEnabled()) {
                logger.debug("starting " + fileProcessor.getName() + " for: " + file.getFileName());
              }
              try{
            	  NodeRef node = file.getNodeToExtract();
                if(nodeService.exists(node)) {
                  runProcessor(fileProcessor, file);
                }
              }catch(Throwable re){
                Throwable cause = NodeUtils.getRootCause(re);
                if(cause instanceof ProcessorTimeoutException){
                  //if the cause was a ProcessorTimeoutException, set pipelineFailed properties/aspect
                  timedOut = true;
                }
                logProcessorError(fileProcessor, file, cause);
              }
              
              if (logger.isDebugEnabled()) {
                double end = System.currentTimeMillis();
                logger.debug(fileProcessor.getName() + " complete for node " + file.getFileName() + ": "+((end-start)/1000d));
              }
            } else {
              //tell this processor that it has been skipped
              logProcessorError(fileProcessor, file, new ProcessorSkippedException());
            }
          } else if(logger.isDebugEnabled()) {
            logger.debug("Skipping processor " + fileProcessor.getName() + " since it is disabled.");
          }
        }

      }
    }

    // Send out bulk notification after everything is done
    sendBatchNotification(files);

  }

  /**
   * Send out bulk notification for all files that were modified
   * @param files
   */
  private void sendBatchNotification(final Collection<FileProcessingInfo> files) {

    // NotificationUtils methods must run in a tx
    RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>(){

      @Override
      public Object execute() throws Throwable {
        String userName = AuthenticationUtil.getSystemUserName();
        setUser(userName);
        RepositoryEventList eventList = new RepositoryEventList();
        for(FileProcessingInfo file : files) {
          NodeRef node = file.getNodeToExtract();
          if(nodeService.exists(node)) {
            try {
              RepositoryEvent propChangedEvent = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED);
              propChangedEvent.setNodeId(node.getId());
              propChangedEvent.setNodePath(notificationUtils.getNodePath(node));
              propChangedEvent.setEventPerpetrator(userName);
              propChangedEvent.setPropertyName(ContentModel.PROP_MODIFIED.toString());
              propChangedEvent.setPropertyValue(nodeService.getProperty(node, ContentModel.PROP_MODIFIED).toString());
              propChangedEvent.setEventTimestamp(System.currentTimeMillis());
              eventList.add(propChangedEvent);
            } catch (Throwable e) {
              e.printStackTrace();
            }
          }
        }
        notificationUtils.sendRepositoryEventList(eventList);  

        return null;
      }
    };
    transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);
  }
  
  /**
   * Run each processor in its own transaction.  Run as the designated user.
   * Turn off notifications while the processor is running.
   * @param processor
  
   * @param fileInfo FileProcessingInfo
   */
  private void runProcessor(final FileProcessor processor, final FileProcessingInfo fileInfo) {
    final NodeRef nodeRef = fileInfo.getNodeToExtract();
    
    RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
    {
      public Object execute() throws Throwable {        
        try {
          NodeRef nf = nodeRef;
          // do not run this processor if node has been deleted
          if(!nodeService.exists(nf)) {
            return null;
          }
          
          // if this node is a hidden rendition (i.e., thumbnail), then don't try to process it
          if(nodeService.hasAspect(nf, RenditionModel.ASPECT_HIDDEN_RENDITION)) {
            return null;
          }
          policyBehaviourFilter.disableBehaviour();

          String user = fileInfo.getUsername();

          // user could be null or unknown if we are doing the bootstrap
          if(user == null || user.equalsIgnoreCase("unknown")) {
            // Only run the thumbnails processor during bootstrap
            // For all other processors, assume the result has been imported along with the
            // bootstrap so we don't need to run it
            if(processor instanceof ThumbnailProcessor) {
              user = AuthenticationUtil.getSystemUserName();
            } else {
              return null;
            }
          }
          logger.debug("Running as user: " + user);
          setUser(user);

          // This node may have been deleted - if so, don't process
          if(!nodeService.exists(nodeRef)){
            return null;
          }
          // disable notifications
          notificationUtils.setNotificationsEnabledForThisTransaction(false);

          // remove any previous pipeline error message:
          nodeService.removeProperty(nodeRef, CatConstants.PROP_PIPELINE_ERROR);

          // run the processor
          processor.processFile(fileInfo);
        } finally {
          policyBehaviourFilter.enableBehaviour();
        }
        return null;
      }
    };
    transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
  }

  /**
   * Method logProcessorError.
   * @param processor FileProcessor
   * @param fileInfo FileProcessingInfo
   * @param cause Throwable
   */
  private void logProcessorError(final FileProcessor processor, final FileProcessingInfo fileInfo, final Throwable cause) {
    RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
    {
      public Object execute() throws Throwable {  
        NodeRef nodeRef = fileInfo.getNodeToExtract();
        try{
          policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
          policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        
          // cancel the processor
          processor.logProcessorError(fileInfo, cause);
        }finally{
          policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
          policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        }
        return null;
      }
    };
    transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
  }


  //TODO inject the policyBehaviourBean and call this at the beginning of each transaction
  //policyBehaviourFilter.disableBehaviour(node, ContentModel.ASPECT_AUDITABLE);
  // and then turn back on at end of transaction (enable...) in finally clause
  // this will prevent the 'System' user to be set as the last person who modified, etc.
  /**
   * Method setUser.
   * @param userName String
   */
  private void setUser(String userName){

    if(AuthenticationUtil.getSystemUserName().equalsIgnoreCase(userName)) {
      AuthenticationUtil.setRunAsUserSystem();

    } else {
      AuthenticationUtil.setFullyAuthenticatedUser(userName);
    }
  }

  /** 
   * This is called after all beans are loaded in the container.
  
   * @param arg0 ContextRefreshedEvent
   * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent) */
  @Override
  public void onApplicationEvent(ContextRefreshedEvent arg0) {
    if(fileProcessors.size() == 0) {
      String[] names = appContext.getBeanNamesForType(FileProcessor.class);
      for(String name : names) {
        fileProcessors.add((FileProcessor)appContext.getBean(name));
      }
      sortFileProcessors();
    }   
  }

 
  /**
   * Method setApplicationContext.
   * @param appContext ApplicationContext
   * @throws BeansException
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext(ApplicationContext)
   */
  @Override
  public void setApplicationContext(ApplicationContext appContext) throws BeansException {
    this.appContext = appContext; 
  }

    
}
