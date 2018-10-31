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
package gov.pnnl.velo.pipeline;

import gov.pnnl.cat.pipeline.AbstractFileProcessor;
import gov.pnnl.cat.pipeline.FileProcessingInfo;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;
import gov.pnnl.velo.wiki.content.WikiContentExtractorRegistry;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * Based on extracting metadata from file, create a wiki page content and mimetype and
 * sync with wiki.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class WikiPhase2Processor extends AbstractFileProcessor {
  // line terminator regex
  private static String lineTerminatorRegex = "\\r|\\n";
  @SuppressWarnings("unused")
  private static Pattern lineTerminatorPattern = Pattern.compile(lineTerminatorRegex);
  
  private File commandFile;
  private Set<NodeRef> copyrightedFiles;
  private Set<NodeRef> linkContentFiles;
  private String dirRoot;
  private WikiContentExtractorRegistry registry;
  
  protected BehaviourFilter policyBehaviourFilter;
  
  // background timer thread
  public static final Timer IMPORT_TEXT_FILE_DAEMON = new Timer("ImportTextFile Scheduler", true);

  
  /**
   * Method setRegistry.
   * @param registry WikiContentExtractorRegistry
   */
  public void setRegistry(WikiContentExtractorRegistry registry) {
    this.registry = registry;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.pipeline.AbstractFileProcessor#init()
   */
  @Override
  public void init() {
    try {
      copyrightedFiles = new HashSet<NodeRef>();
      linkContentFiles = new HashSet<NodeRef>();
      
      String commandFilePath = dirRoot + "/importTextFile.txt";
      commandFile = new File(commandFilePath);
      if(!commandFile.exists()) {
        commandFile.createNewFile();
      }

      // TODO: check for backup file (which would exist if server went down during importTextFile
      // call) and append to commandFile

      // Start up background timer thread to process additions to commandFile
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          executeBatchImportTextFile();          
        }
      };
      // schedule after 30 second delay to run every 30 seconds
      IMPORT_TEXT_FILE_DAEMON.schedule(timerTask, 30000, 30000);
     
    } catch(Throwable e) {
      throw new RuntimeException(e);
    }

  }
  
  /**
   * Method setPolicyBehaviourFilter.
   * @param policyBehaviourFilter BehaviourFilter
   */
  public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
    this.policyBehaviourFilter = policyBehaviourFilter;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.pipeline.FileProcessor#getName()
   */
  @Override
  public String getName() {
    return "Wiki Page Generation";
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.pipeline.FileProcessor#processFile(gov.pnnl.cat.pipeline.FileProcessingInfo)
   */
  @Override
  public void processFile(FileProcessingInfo fileInfo) throws Exception { 
    NodeRef nodeRef = fileInfo.getNodeToExtract();
    if(WikiUtils.isWikiNode(nodeRef, nodeService, namespaceService) && 
        WikiUtils.getWikiHome() != null) {
      logPhase2Command(nodeRef);
    }

  }
  
  /**
   * Method logPhase2Command.
   * @param nodeRef NodeRef
   */
  public void logPhase2Command(NodeRef nodeRef) {
    // get the userid
    String userId =  WikiUtils.getCurrentUserId(authenticationComponent);
  
    // get the mimetype
    String mimetype = WikiUtils.getMimetype(nodeRef, nodeService, contentService);
        
    // get the wiki page content
    File wikiMetadataTempFile = new File("/tmp");
    try {
      wikiMetadataTempFile = WikiUtils.getWikiContent(nodeRef, mimetype, contentService, nodeService, registry);
    } catch (Throwable e) {
      logger.error(e);
    }
    // get the title (which is everything after WFS: in the wiki URL) 
    String title = WikiUtils.getWikiPath(nodeRef, nodeService);
    System.out.println("Logging phase 2 command for: " + title);
    
    logger.debug("Calling logPhase2Command for: " + title);
    logger.debug("Extracted metadata file = " + wikiMetadataTempFile);
    
    // If we have metadata content to log
    if(wikiMetadataTempFile != null) {
      String importTextFileCommand = title + ", " + userId;
      
      if(wikiMetadataTempFile != null) {
        importTextFileCommand = importTextFileCommand + ", " + wikiMetadataTempFile.getAbsolutePath(); 
      }
      
      logger.debug("Recording Phase2 command: " + importTextFileCommand);
      
      Boolean copyrightProp = (Boolean)nodeService.getProperty(nodeRef, VeloServerConstants.PROP_HAS_COPYRIGHT);
      boolean isCopyrighted = copyrightProp != null && copyrightProp.equals(true);
      Boolean hasContentProp = (Boolean)nodeService.getProperty(nodeRef, VeloServerConstants.PROP_HAS_LINK_CONTENT);
      boolean hasContent = hasContentProp != null && hasContentProp.equals(true);
      
      synchronized (commandFile) {
        if(isCopyrighted) {
          copyrightedFiles.add(nodeRef);
        }
        if(hasContent) {
          linkContentFiles.add(nodeRef);
        }
        WikiUtils.appendToFile(commandFile, importTextFileCommand);
      }
    }    
  }

  /**
   * Phase 2 in a 2-part wiki page creation process.
   * If called outside timer thread, force the batch import to happen
   * immediately.
  
   */
  public void executeBatchImportTextFile() {
    synchronized(commandFile) {
      if(commandFile.length() == 0) {
         return;
      }
    }
    System.out.println("Executing Phase 2 batch import.");
    // TODO: don't use a temp file, use a special backup file so we can recover if 
    // server goes down during this step
    File tempFile = TempFileProvider.createTempFile("velo-phase2-", ".importTextFile");
    final Set<NodeRef> copyrightedBatch = new HashSet<NodeRef>();
    final Set<NodeRef> linkContentBatch = new HashSet<NodeRef>();
    try {
      
      synchronized(commandFile) {
        // copy commandFile to temp file
        FileUtils.copyFile(commandFile, tempFile);
        
        // clean up commandFile so it can be reused
        commandFile.delete(); 
        commandFile.createNewFile();
        
        // clean up copyrightedFiles so it can be reused
        for(NodeRef nodeRef : copyrightedFiles) {
          copyrightedBatch.add(nodeRef);
        }
        copyrightedFiles.clear();
        
        // clean up linkContentFiles so it can be reused
        for(NodeRef nodeRef : linkContentFiles) {
          linkContentBatch.add(nodeRef);
        }
        linkContentFiles.clear();
      }
      
      // now process any link files with content
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
      {
        public Object execute() throws Throwable {                
          AuthenticationUtil.setRunAsUserSystem();
          processLinkContentFiles(copyrightedBatch, linkContentBatch);
          return null;
        }
      };
      transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

      // now add the phase2 pages to the wiki
      if(tempFile.length() > 0) {
        logger.debug("Performing phase2 batch update from file: " + tempFile.getAbsolutePath());
        String[] cmdArray = {"php", WikiUtils.getImportFilePath(), "--input", tempFile.getAbsolutePath()};
        long start = System.currentTimeMillis();
        WikiUtils.execCommand(cmdArray);      
        long end = System.currentTimeMillis();
        logger.debug("Time to perform batch update: " + (end-start)/1000 + " seconds");
      }
      
    } catch (Throwable e) {
      logger.error("Failed to execute batch importTextFile command.", e);

    } finally {
      
      // delete all the metadata temp files listed in tempFile
      if(tempFile.exists() && tempFile.length() > 0) {
        try {
          LineIterator it = FileUtils.lineIterator(tempFile);
          while(it.hasNext()) {
            String line = it.nextLine();
            int pos = line.lastIndexOf(',');
            if(pos != -1) {
              String metadataFilePath = line.substring(pos + 2).trim();
              File metadataFile = new File(metadataFilePath);
              if(metadataFile.exists()) {
                metadataFile.delete();
              }
            }
          }
        } catch (Throwable e) {
          logger.error(e);
        }
      }
      // Clean up temp file
      tempFile.delete();
      
    }
    
  }
  
  /**
   * Method processLinkContentFiles.
   * @param copyrightedBatch Set<NodeRef>
   * @param linkContentBatch Set<NodeRef>
   */
  protected void processLinkContentFiles(Set<NodeRef>copyrightedBatch, Set<NodeRef>linkContentBatch) {
    policyBehaviourFilter.disableBehaviour();
    
    try {
      for(NodeRef nodeRef : linkContentBatch) {
        String linkUrl = (String)nodeService.getProperty(nodeRef, VeloServerConstants.PROP_TEMPORARY_REMOTE_URL);
        String linkDescription = (String)nodeService.getProperty(nodeRef, VeloServerConstants.PROP_TEMPORARY_LINK_DESCRIPTION);

        if(copyrightedBatch.contains(nodeRef)) {
          // remove content property
          ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
          StringBuilder content = new StringBuilder("Name:");
          content.append("Remote Link:");
          content.append(linkUrl);
          content.append("\n");
          if(linkDescription != null) {
            content.append("Description:");
            content.append(linkDescription);
          }
          writer.putContent(content.toString());
        }
        // TODOs: remove custom permissions
        //permissionService.clearPermission(nodeRef, authority)
        // change permissions so it inherits parent permissions
        permissionService.setInheritParentPermissions(nodeRef, true);
        
        // add the link property
        nodeService.setProperty(nodeRef, CatConstants.PROP_LINK_URL, linkUrl);
        
        // clean up temp properties
        nodeService.removeProperty(nodeRef, VeloServerConstants.PROP_TEMPORARY_REMOTE_URL);
        nodeService.removeProperty(nodeRef, VeloServerConstants.PROP_TEMPORARY_LINK_DESCRIPTION);
        nodeService.removeProperty(nodeRef, VeloServerConstants.PROP_TEMPORARY_LINK_TITLE);
        nodeService.removeProperty(nodeRef, VeloServerConstants.PROP_HAS_LINK_CONTENT);
        nodeService.removeProperty(nodeRef, VeloServerConstants.PROP_HAS_COPYRIGHT);        
      }

    } finally {
      // re-enable policy
      policyBehaviourFilter.enableBehaviour();
    }
  }
  
  /**
   * Method setDirRoot.
   * @param dirRoot String
   */
  public void setDirRoot(String dirRoot) {
      this.dirRoot = dirRoot;
  }
}
