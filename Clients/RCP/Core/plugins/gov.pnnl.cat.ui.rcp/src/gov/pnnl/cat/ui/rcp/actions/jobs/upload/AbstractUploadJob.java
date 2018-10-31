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

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 */
public abstract class AbstractUploadJob extends Job {

  private File file;
  private CmsPath destination;
  protected IResourceManager mgr;
  protected IProgressMonitor parentMonitor;

  /**
   * Constructor for AbstractUploadJob.
   * @param file File
   * @param destination CmsPath
   * @param mgr IResourceManager
   * @param monitor IProgressMonitor
   */
  public AbstractUploadJob(File file, CmsPath destination, IResourceManager mgr, IProgressMonitor monitor) {
    super("Uploading " + destination.last().getName());

    assert(file != null);
    assert(destination != null);
    assert(mgr != null);
    assert(monitor != null);

    this.file = file;
    this.destination = destination;
    this.mgr = mgr;
    this.parentMonitor = monitor;
  }

  /**
   * Returns the <tt>File</tt> that this job is responsible for upload.
  
   * @return File
   */
  public synchronized File getFile() {
    return this.file;
  }

  /**
   * Returns the destination for this upload.
  
   * @return File
   */
  public synchronized File getDestination() {
    return this.file;
  }

  /**
   * Method run.
   * @param monitor IProgressMonitor
   * @return IStatus
   */
  @Override
  protected final IStatus run(IProgressMonitor monitor) {
    try {
      return upload(file, destination, monitor);
    } catch (Exception e) {
      return new Status(IStatus.ERROR, CatRcpPlugin.PLUGIN_ID, 0, e.getMessage(), e);
    }
  }

  /**
   * Method upload.
   * @param file File
   * @param destination CmsPath
   * @param monitor IProgressMonitor
   * @return IStatus
   * @throws Exception
   */
  protected abstract IStatus upload(File file, CmsPath destination, IProgressMonitor monitor) throws Exception;

  /**
   * Method isFolderUpload.
   * @return boolean
   */
  public abstract boolean isFolderUpload();
}
