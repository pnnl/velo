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
package gov.pnnl.cat.ui.rcp.actions.jobs;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoRepoWebserviceUtils;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoUtils;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatRcpMessages;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Predicate;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Sends a bunch of files to a taxonomy, using a simple search on the 
 * server to decide in which taxonomy folders to place the files.
 *
 * @version $Revision: 1.0 $
 */
public class SendToTaxonomyJob extends Job {

  private IResourceManager mgr = ResourcesPlugin.getResourceManager();
  private IFolder taxonomyFolder;
  private List<IResource>selection;
  private List<IResource> selectedResources;
  private static int BATCH_SIZE = 3000;
  private static Logger logger = CatLogger.getLogger(SendToTaxonomyJob.class);

  /**
   * Constructor
   * @param taxonomyFolder - Folder where we are uploading
  
   * @param selection List<IResource>
   */
  public SendToTaxonomyJob(IFolder taxonomyFolder, List<IResource> selection) {
    super("Sending to Taxonomy");
    this.taxonomyFolder = taxonomyFolder;
    this.selection = selection;
    this.selectedResources = new ArrayList<IResource>();
  }
  
  /**
   * Method recursiveGetFiles.
   * @param resource IResource
   * @param fileList List<IResource>
   * @throws Exception
   */
  protected void recursiveGetFiles(IResource resource, List<IResource>fileList) throws Exception {
    
    if(resource.isType(IResource.FOLDER)) {
      List<IResource> children = mgr.getChildren(resource.getPath());
      for(IResource child : children) {
        recursiveGetFiles(child, fileList);
      }
      
    } else {
      fileList.add(resource);
    }
    
  }

  /**
   * Method run.
   * @param monitor IProgressMonitor
   * @return IStatus
   */
  @Override
  protected IStatus run(IProgressMonitor monitor) {

    if(monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }

    try {
      sendToTaxonomy(monitor);
    } catch (Exception e) {

      ToolErrorHandler.handleError("Could not send files to taxonomy.", e, true);
      return Status.CANCEL_STATUS;
    }

    return Status.OK_STATUS;

  }
  
  /**
   * Sends the documents to the taxonomy in batches, so we can cancel.
   * @param monitor
  
   * @return IStatus
   * @throws Exception */
  public IStatus sendToTaxonomy(IProgressMonitor monitor) throws Exception {
    long start = System.currentTimeMillis();
    monitor.beginTask(CatRcpMessages.SendToTaxonomy_job_title, IProgressMonitor.UNKNOWN);
    
    try {
      for(IResource resource : selection) {
        // This is REALLY slow, so let's not do that here!
        //recursiveGetFiles(resource, this.selectedResources);
        selectedResources.add(resource);
      }
    } catch (Throwable e) {
      throw new RuntimeException("Failed to compose file list from selection.", e);
    }

    // Create the common parameters
    Predicate actionedUponNode = AlfrescoRepoWebserviceUtils.getPredicate(this.taxonomyFolder.getPath());
    Action sendToTaxonomyAction = new Action();
    sendToTaxonomyAction.setActionName("send-to-taxonomy");
    sendToTaxonomyAction.setTitle("Send to Taxonomy");
    sendToTaxonomyAction.setDescription("Send to taxonomy.");
    String taxUuid = taxonomyFolder.getPropertyAsString(VeloConstants.PROP_UUID);
    String taxReference = AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, taxUuid);
    NamedValue taxParam = new NamedValue("taxonomy-ref",false, taxReference, null);
    String referenceList = "";

    Exception timedOut = null;
    
    
    for(int i = 0; i < this.selectedResources.size(); i++) {
      IResource resource = selectedResources.get(i);

      // quit if user has canceled the job
      if(monitor.isCanceled())
      {
        logger.info("Send To Taxonomy Canceled");
        return Status.CANCEL_STATUS;
      }

      // append to the reference list
      if (referenceList.length() > 0) {
        referenceList += ";";
      }
      
      String uuid = resource.getPropertyAsString(VeloConstants.PROP_UUID);
      referenceList += AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, uuid);

      // send a batch to the server
      if( (i+1) % SendToTaxonomyJob.BATCH_SIZE == 0) {
        logger.debug("Sending next batch to taxonomy: " + referenceList);
        NamedValue referenceListParam = new NamedValue("reference-list", false, referenceList, null);
        sendToTaxonomyAction.setParameters(new NamedValue[]{taxParam, referenceListParam});
        try {
//          AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{sendToTaxonomyAction});
          ResourcesPlugin.getDefault().getResourceManager().executeActions(actionedUponNode, new Action[]{sendToTaxonomyAction});
        } catch (Exception exception) {
          // Need to keep on going if the connection timed out
          if((exception.getCause() != null && exception.getCause().getClass().equals(SocketTimeoutException.class)) 
              || (exception.getCause() != null && exception.getCause().getCause() != null && exception.getCause().getCause().getClass().equals(SocketTimeoutException.class))){
            timedOut = exception;
            
          } else {
            throw exception;
          }
        }
          
        referenceList = ""; // reset the reference list
      }

    }

    // Finish up the last batch if any left
    if(referenceList.length() > 0) {
      logger.debug("Sending final batch to taxonomy: " + referenceList);
      NamedValue referenceListParam = new NamedValue("reference-list", false, referenceList, null);
      sendToTaxonomyAction.setParameters(new NamedValue[]{taxParam, referenceListParam});
      try {
        //AlfrescoWebServiceFactory.getActionService().executeActions(actionedUponNode, new Action[]{sendToTaxonomyAction});
        ResourcesPlugin.getDefault().getResourceManager().executeActions(actionedUponNode, new Action[]{sendToTaxonomyAction});
      } catch (Exception exception) {
        // Need to keep on going if the connection timed out
        if((exception.getCause() != null && exception.getCause().getClass().equals(SocketTimeoutException.class)) 
            || (exception.getCause() != null && exception.getCause().getCause() != null && exception.getCause().getCause().getClass().equals(SocketTimeoutException.class))){
          timedOut = exception;
          
        } else {
          throw exception;
        }
      } 
    }

    long end = System.currentTimeMillis();
    logger.debug("time to send to taxonomy = " + (end - start));
    
    // Now show the user that something timed out, so the are aware
    if(timedOut != null) {
      ToolErrorHandler.handleError("Connection Timed Out", null, true);
    }
    
    return Status.OK_STATUS;
  }

}
