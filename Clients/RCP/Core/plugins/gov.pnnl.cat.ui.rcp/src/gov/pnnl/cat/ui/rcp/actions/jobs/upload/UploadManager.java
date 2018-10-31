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
package gov.pnnl.cat.ui.rcp.actions.jobs.upload;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.util.IErrorMonitor;
import gov.pnnl.cat.logging.CatLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 */
public class UploadManager extends JobChangeAdapter implements IErrorMonitor {
  private static final int MAX_FILE_UPLOAD_JOBS = 10;
  private static final int MAX_FOLDER_UPLOAD_JOBS = 2;

  private Map<File, CmsPath> fileUploads =  Collections.synchronizedMap(new LinkedHashMap<File, CmsPath>());
  private Map<File, CmsPath> folderUploads = Collections.synchronizedMap(new LinkedHashMap<File, CmsPath>());
  private int totalActiveFileUploads = 0;
  private int totalActiveFolderUploads = 0;
  private int totalCompleteFileUploads = 0;
  private List<AbstractUploadJob> jobs = Collections.synchronizedList(new ArrayList<AbstractUploadJob>());

  private IProgressMonitor monitor;
  private IResourceManager mgr;
  private Logger logger = CatLogger.getLogger(getClass());

  public UploadManager() {
    this.mgr = ResourcesPlugin.getResourceManager();
  }

  /**
   * Adds a file to be uploaded.
   * <p/>If this job is already running, it will continue to run and eventually process the new file.
   * If this job is not running, it will schedule itself to process the new file.
  
   * @param destination
   * @param file File
   */
  public void scheduleFileUpload(File file, CmsPath destination) {
    fileUploads.put(file, destination);
//    evaluateFiles();
  }

  /**
   * Method scheduleFolderUpload.
   * @param file File
   * @param destination CmsPath
   */
  public void scheduleFolderUpload(File file, CmsPath destination) {
    logger.debug("scheduleFolderUpload, about to put " + file.getName() + " on folderUploads");
    folderUploads.put(file, destination);
    logger.debug("scheduleFolderUpload, completed putting " + file.getName() + " on folderUploads, calling evaluateFolders");
    evaluateFolders();
    logger.debug("scheduleFolderUpload " + file.getName() + " returning");
  }

  public synchronized void evaluateWork() {
    logger.debug("evaluateWork");

    if (!monitor.isCanceled()) {
      evaluateFolders();
//      evaluateFiles();
    }
  }

//  private void evaluateFiles() {
//    boolean keepGoing = true;
//
//    // keep evaluating until a loop does not result in a new upload job getting scheduled.
//    while (keepGoing) {
//      keepGoing = false;
//
//      synchronized (fileUploads) {
//        if (!fileUploads.isEmpty()) {
//          System.out.println("Active Jobs: "+totalActiveFileUploads);
//          if (totalActiveFileUploads < MAX_FILE_UPLOAD_JOBS) {
//
//            // schedule a new file upload if:
//            // 1. it is the first file upload requested, or
//            // 2. at least one file upload has completed
//            if (totalActiveFileUploads == 0 || totalCompleteFileUploads > 0) {
//              File file = fileUploads.keySet().iterator().next();
//              CmsPath destination = fileUploads.remove(file);
//              System.out.println("Upload processed: "+fileUploads.size());
//              
//              totalActiveFileUploads++;
//
//              logger.debug("Scheduling a file upload");
//              UploadFileJob job = new UploadFileJob(file, destination, mgr, monitor);
//              job.addJobChangeListener(this);
//              job.setRule(new PathSchedulingRule(destination));
//              job.setPriority(Job.LONG);
//              jobs.add(job);
//              job.schedule();
//
//              if (monitor.isCanceled()) {
//                keepGoing = false;
//              } else {
//                keepGoing = true;
//              }
//            }
//          }
//        }
//      }
//    }
//    System.out.println("Left Loop");
//  }

  private void evaluateFolders() {
    logger.debug("evaluateFolders");
    boolean keepGoing = true;

    // keep evaluating until a loop does not result in a new upload job getting scheduled.
    while (keepGoing) {
      keepGoing = false;

      logger.debug("above synchronized block");
      synchronized (folderUploads) { //in ordert o synchronize on this list, it MUST be created using Collections.synchronizedMap
        logger.debug("inside synchronized block");
        while (!folderUploads.isEmpty()) {
          if (totalActiveFolderUploads < MAX_FOLDER_UPLOAD_JOBS) {
            File file = folderUploads.keySet().iterator().next();
            CmsPath destination = folderUploads.remove(file);

            totalActiveFolderUploads++;

            UploadFolderJob job = new UploadFolderJob(file, destination, mgr, monitor);
            job.addJobChangeListener(this);
            job.setRule(new PathSchedulingRule(destination));
            jobs.add(job);

            logger.debug("evaluateFolders, job.schedule()");
            job.schedule();
            if (monitor.isCanceled()) {
              keepGoing = false;
              break;
            } else {
              keepGoing = true;
            }
          }
        }
        logger.debug("exiting synchronized block");
      }
      logger.debug("out of synchronized block");
    }
  }

  /**
   * Method setProgressMonitor.
   * @param monitor IProgressMonitor
   */
  public void setProgressMonitor(IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  /**
   * Method done.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(IJobChangeEvent)
   */
  public void done(IJobChangeEvent event) {
    logger.debug("done");
    AbstractUploadJob job = (AbstractUploadJob) event.getJob();
    if (job.isFolderUpload()) {
      totalActiveFolderUploads--;
      logger.debug("Folder upload has completed: " + job.getName());
    } else {
      totalActiveFileUploads--;
      totalCompleteFileUploads++;
      monitor.worked((int)job.getFile().length());
      logger.debug("File upload has completed: " + job.getName());
    }
    jobs.remove(job);

    evaluateWork();
  }


  public void joinUploadThreads() {

    logger.debug("joinUploadThreads");
    // Wait for all folder jobs to complete
    while (!jobs.isEmpty() && !monitor.isCanceled()) {
      try {
        jobs.get(0).join();
      } catch (InterruptedException e) {
        logger.warn(e);
      }
    }
    
    // Start bulk upload
    try {
      if(monitor != null && !monitor.isCanceled() && fileUploads != null && fileUploads.size() > 0)
      monitor.beginTask("Uploading Files to Server", this.fileUploads.size());
      mgr.bulkUpload(this.fileUploads, monitor);
    } catch (ResourceException e) {
      StatusUtil.handleStatus("Error uploading files", e, StatusManager.SHOW);
    }
  }

  /**
   * Method logError.
   * @param msg String
   * @param th Throwable
   * @see gov.pnnl.cat.core.resources.util.IErrorMonitor#logError(String, Throwable)
   */
  @Override
  public void logError(String msg, Throwable th) {
    StatusUtil.handleStatus("Error uploading files", th, StatusManager.SHOW);
  }
}
