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
package gov.pnnl.cat.ui.rcp.views.profile;


import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IGroupEventListener;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.util.DateFormatUtility;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.users.GetProfileImageJob;
import gov.pnnl.velo.util.VeloConstants;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 */
public abstract class ProfileDetailsView extends ViewPart implements ISelectionListener, IGroupEventListener {
  public static final int MAX_IMG_WIDTH = 300;
  public static final int MAX_IMG_HEIGHT = 300;

  private IProfilable currentSelection;
  private GetProfileImageJob getImageJob;
  private UpdateImageJobListener jobListener;
  private Image image;
  private Date selectionLastModified;
  private String selectionProfileImage;
  private static Logger logger = CatLogger.getLogger(ProfileDetailsView.class);
  private ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager();
  
  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    this.securityMgr.addGroupEventListener(this);
  }
  
  /**
   * Indicates that the selection has changed to the specified profilable.
   * @param profilable the IProfilable that has been selected, or <code>null</code> if nothing is selected.
   */
  protected abstract void selectionChanged(IProfilable profilable);

  /**
   * Lays out the view again.
   *
   */
  protected abstract void layout();

  /**
   * Returns the default image to display if the IProfilable does not have an image.
  
   * @return the default image descriptor, or <code>null</code> if no image should be displayed. */
  protected abstract ImageDescriptor getDefaultImageDescriptor();

  /**
   * Method getErrorImageDescriptor.
   * @return ImageDescriptor
   */
  protected abstract ImageDescriptor getErrorImageDescriptor();

  /**
   * Sets the image to be displayed.
   * @param img the image to display.
   */
  protected abstract void setImage(Image img);

  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    // do nothing
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    this.securityMgr.removeGroupEventListener(this);
    if (this.image != null) {
      this.image.dispose();
    }
    super.dispose();
  }

  /**
   * Method selectionChanged.
   * @param part IWorkbenchPart
   * @param selection ISelection
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
   */
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    // get modified time, compare

    // get profileImage

//    // if they are just re-selecting the current selection, ignore it
//    if (currentSelection != null &&
//        !selection.isEmpty() &&
//        selection instanceof IStructuredSelection &&
//        ((IStructuredSelection) selection).getFirstElement().equals(currentSelection)) {
//      return;
//    }

//    // if we have a new selection, we don't want to waste any time processing
//    // data for an old selection.
//    if (getImageJob != null) {
//      logger.debug("Get Image Job is already running. Canceling...");
//
//      getImageJob.cancel();
//      getImageJob.removeJobChangeListener(jobListener);
//      getImageJob = null;
//    }

    if (selection.isEmpty()) {
      logger.debug("Selection is empty. Clearing the view.");

      selectionChangedInternal(null);
      setImageData(null);

      currentSelection = null;
      selectionLastModified = null;
      selectionProfileImage = null;
    } else if (selection instanceof IStructuredSelection) {
      IStructuredSelection structevent = (IStructuredSelection) selection;

      if (structevent.getFirstElement() instanceof IProfilable) {
        IProfilable profilable = (IProfilable) structevent.getFirstElement();
        String lastModified = profilable.getProperty(VeloConstants.PROP_MODIFIED);
        String profileImage = profilable.getProperty(VeloConstants.PROP_PICTURE);
        boolean selectionChanged = true;
        boolean refreshSelection = true;
        logger.debug("Selection: " + profilable + ", last modified: " + lastModified + ", profileImage: " + profileImage);

        // if they selected the same profilable, then we only want to update
        // if the object has been updated
        if (currentSelection != null && currentSelection.equals(profilable)) {
          logger.debug("Detected repeat selection");
          selectionChanged = false;
        }

        if (lastModified != null) {
          Date lastModifiedDate = DateFormatUtility.parseJcrDate(lastModified);

          // don't bother updating the selection unless it has been modified
          // since the last selection
          if (!selectionChanged && !lastModifiedDate.after(selectionLastModified)) {
            logger.debug("Will not refresh selection because selection has not been modified");
            refreshSelection = false;
          }

          // save the last modified time
          selectionLastModified = lastModifiedDate;
        } else {
          selectionLastModified = null;
        }

        if (refreshSelection) {
          logger.debug("Refreshing selection");
          selectionChangedInternal(profilable);
        }

        // update the picture if the picture was changed
        if (selectionProfileImage == null || !selectionProfileImage.equals(profileImage)) {
          logger.debug("Refreshing image");

          // clear out the picture
          setImageData(null);

          // if the profilable has a picture, schedule a job to show it.
          if (profilable.hasPicture()) {
            scheduleImageUpdate(profilable);
          }

          // save the new picture string
          selectionProfileImage = profileImage;
        }
      }
    }
  }

  /**
   * Team events get filtered by the security manager, so we know that 
   * if this method is called, at least one team has been changed.
   * @param events
   * @see gov.pnnl.cat.core.resources.events.IGroupEventListener#onEvent(List<IResourceEvent>)
   */
  public void onEvent(List<IResourceEvent> events) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        logger.debug("Refreshing selection");
        //if we get a team event, refresh the view because the team event might have just been generated by us
        selectionChangedInternal(currentSelection); 
        
      }
    });
  }
  
  /**
   * Method selectionChangedInternal.
   * @param profilable IProfilable
   */
  private void selectionChangedInternal(IProfilable profilable) {
    currentSelection = profilable;
    selectionChanged(profilable);
  }


  /**
   * Method scheduleImageUpdate.
   * @param profilable IProfilable
   */
  private void scheduleImageUpdate(final IProfilable profilable) {
    if (!profilable.hasPicture()) {
      logger.debug("No picture available for " + profilable + ". Clearing the image.");
      setImageData(null);
    } else {
      logger.debug("A picture is available for " + profilable + ". Scheduling job to download image.");
      getImageJob = new GetProfileImageJob("Downloading Image", profilable);
      getImageJob.setPriority(Job.DECORATE);
      jobListener = new UpdateImageJobListener(profilable);
      getImageJob.addJobChangeListener(jobListener);
      getImageJob.schedule(50);
    }
  }

  /**
   * A simple utility method to work around null Strings.
   * @param s
  
   * @return String
   */
  public String getNonNullString(String s) {
    if (s == null) {
      return "";
    }
    return s;
  }

  /**
   * Method setImageOnUiThread.
   * @param imgData ImageData
   */
  private void setImageOnUiThread(final ImageData imgData) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        setImageData(imgData);
      }
    });
  }

  /**
   * Method setImageData.
   * @param imgData ImageData
   */
  private void setImageData(ImageData imgData) {

    if (imgData == null) {
      ImageDescriptor imageDesc = getDefaultImageDescriptor();
      imgData = imageDesc.getImageData();
    }

    imgData = scaleImage(imgData, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);

    try {
      setImage(new Image(getViewSite().getShell().getDisplay(), imgData));
    } catch (Exception e) {
      logger.error("Could not display image", e);
      // if something goes wrong setting the image, display the error
      ImageDescriptor imageDesc = getErrorImageDescriptor();
      if (imageDesc != null) {
        imgData = imageDesc.getImageData();
        setImage(new Image(getViewSite().getShell().getDisplay(), imgData));
      }
    }
    layout();
  }


  /**
   * Method scaleImage.
   * @param sourceImageData ImageData
   * @param maxWidth int
   * @param maxHeight int
   * @return ImageData
   */
  public static ImageData scaleImage(ImageData sourceImageData, int maxWidth, int maxHeight) {
    ImageData imgData = sourceImageData;

    if (imgData.width > maxWidth) {
      double scale = (double) imgData.width / maxWidth;

      int newWidth = maxWidth;
      int newHeight = (int) (imgData.height / scale);

      imgData = imgData.scaledTo(newWidth, newHeight);
    }

    if (imgData.height > maxHeight) {
      double scale = (double) imgData.height / maxHeight;

      int newHeight = maxHeight;
      int newWidth = (int) (imgData.width / scale);

      imgData = imgData.scaledTo(newWidth, newHeight);      
    }

    return imgData;
  }


  /**
   */
  private class UpdateImageJobListener extends JobChangeAdapter {
    private IProfilable profilable;

    /**
     * Constructor for UpdateImageJobListener.
     * @param profilable IProfilable
     */
    public UpdateImageJobListener(IProfilable profilable) {
      this.profilable = profilable;
    }

    /**
     * Method isOutOfDate.
     * @return boolean
     */
    private boolean isOutOfDate() {
      return currentSelection == null || !currentSelection.equals(profilable);
    }

    /**
     * Method done.
     * @param event IJobChangeEvent
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(IJobChangeEvent)
     */
    public void done(IJobChangeEvent event) {
      logger.debug("Get Image Job has completed");

      // it may have taken a considerable amount of time to get the image,
      // so we must check that the selection has not changed before we update the image.
      if (isOutOfDate()) {
        return;
      }

      final ImageData imgData = getImageJob.getImageData();

      if (imgData == null) {

        logger.debug("No ImageData available. Clearing image.");
        setImageOnUiThread(null);

      } else {

        if (isOutOfDate()) {
          logger.debug("Selection is out of date. Not showing image for " + profilable);
          return;
        }

        logger.debug("Updating Image...");
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            try {
              setImageData(imgData);
            } catch (Exception e) {
              //EZLogger.logWarning("Unable to create ImageData for " + profilable, e);
              logger.warn("Unable to create ImageData for " + profilable,e);
              setImageData(null);
            }
          }
        });

      }
    }
  }
}
