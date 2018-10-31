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
import gov.pnnl.cat.core.resources.ResourceException;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 */
public class UploadFolderJob extends AbstractUploadJob {

  /**
   * Constructor for UploadFolderJob.
   * @param file File
   * @param destination CmsPath
   * @param mgr IResourceManager
   * @param monitor IProgressMonitor
   */
  public UploadFolderJob(File file, CmsPath destination, IResourceManager mgr, IProgressMonitor monitor) {
    super(file, destination, mgr, monitor);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.actions.jobs.upload.AbstractUploadJob#upload(java.io.File, gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected IStatus upload(File file, CmsPath destination, IProgressMonitor monitor) throws FileNotFoundException, ResourceException {
    mgr.createFolder(destination);
    return Status.OK_STATUS; 
  }

  /**
   * Method isFolderUpload.
   * @return boolean
   */
  public boolean isFolderUpload() {
    return true;
  }

}
