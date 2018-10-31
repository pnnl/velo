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
package gov.pnnl.cat.ui.rcp.views.users;

import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.logging.CatLogger;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.ImageData;

/**
 */
public class GetProfileImageJob extends Job {

  private IProfilable profilable;
  private ImageData imgData;
  private static Logger logger = CatLogger.getLogger(GetProfileImageJob.class);

  /**
   * Constructor for GetProfileImageJob.
   * @param name String
   * @param profilable IProfilable
   */
  public GetProfileImageJob(String name, IProfilable profilable) {
    super(name);
    this.profilable = profilable;
  }

  /**
   * Method run.
   * @param monitor IProgressMonitor
   * @return IStatus
   */
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }

    if (monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }

    try {
      monitor.beginTask("Downloading Image", IProgressMonitor.UNKNOWN);
      File profileImage = profilable.getPicture();

      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }

      monitor.subTask("Creating Image");

      long begin = System.currentTimeMillis();
      imgData = new ImageData(new FileInputStream(profileImage));
      long end = System.currentTimeMillis();
      logger.debug("ImageData created in " + (end - begin) + " ms");

      monitor.subTask("Done!");
    } catch (Throwable e) {
      logger.warn("Unable to retrieve picture file for " + profilable.toString(),e);
    }

    return Status.OK_STATUS;
  }

  /**
   * Method getImageData.
   * @return ImageData
   */
  public ImageData getImageData() {
    return imgData;
  }
}
