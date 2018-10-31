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
package gov.pnnl.cat.ui.rcp.decorators;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.TransformData;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.velo.util.VeloConstants;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 */
public class LinkDecorator implements ILabelDecorator {


  public LinkDecorator() {
  }

  private Logger logger = CatLogger.getLogger(this.getClass());

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
   */
  @Override
  public Image decorateImage(Image baseImage, Object element) {
    // only decorate cat item nodes.
    if (!(element instanceof IResource)) {
      return baseImage;
    }

    Image thisImage = baseImage;
    IResource resource = (IResource) element;
    if (resource == null || thisImage == null) {
      return null;
    }

    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    try {    
      if(resource instanceof ILinkedResource) {
        // get the destination has been cached
        resource = mgr.getPropertyAsResource(resource.getPath(), VeloConstants.PROP_LINK_DESTINATION);
      }

      if(resource != null) { // the target resource (if this is a link) may have been deleted
        
        if(resource.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
          // add decorator for remote link
          Image remoteLinkDecorator = SWTResourceManager.getImage(CatRcpPlugin.getDefault().getAbsolutePath("icons/8x8/remote_link.gif"));
          thisImage = SWTResourceManager.decorateImage(thisImage, remoteLinkDecorator, SWTResourceManager.BOTTOM_RIGHT);               

        } else if (resource instanceof IFile) {

          IFile theFile = (IFile) resource;

          if(theFile != null) {
            TransformData rawtext = theFile.getTextTransform();

            if (rawtext != null) {
              String transformStatus = (String) rawtext.getErrorMessage();
              if(transformStatus != null && transformStatus.length() > 0) {
                if(transformStatus.contains("No text transformer found for: ")){
                  // CSL - I am commenting this out because it is confusing to users - the question mark is showing up for every image
                  // It would be better if we added help documentation to search to list all the file types that were available for full-text search
                  //                thisImage = SWTResourceManager.decorateImage(thisImage, SharedImages.getInstance().getImage(SharedImages.CAT_IMG_NO_TRANSFORMER_DECORATOR,
                  //                  SharedImages.CAT_IMG_SIZE_8), SWTResourceManager.BOTTOM_RIGHT);
                }else if(transformStatus.contains("Transform failed.")){
                  thisImage = SWTResourceManager.decorateImage(thisImage, SharedImages.getInstance().getImage(SharedImages.CAT_IMG_TRANSFORMER_FAILED_DECORATOR,
                      SharedImages.CAT_IMG_SIZE_8), SWTResourceManager.BOTTOM_RIGHT);
                }
              }
            }  
          } 
        }
      }
    }
    catch (ResourceException e) {}

    return thisImage;
  }

  // not doing label (text) decoration for now
  /**
   * Method decorateText.
   * @param text String
   * @param element Object
   * @return String
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(String, Object)
   */
  public String decorateText(String text, Object element) {
    return text;
  }

  /**
   * Method decorate.
   * @param element Object
   * @param decoration IDecoration
   */
  public void decorate(Object element, IDecoration decoration) {
    logger.debug("Within the decorate");

  }

  /**
   * Method addListener.
   * @param listener ILabelProviderListener
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener) {

  }

  /**
   * Method dispose.
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose() {

  }

  /**
   * Method isLabelProperty.
   * @param element Object
   * @param property String
   * @return boolean
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
   */
  public boolean isLabelProperty(Object element, String property) {
    return true;
  }

  /**
   * Method removeListener.
   * @param listener ILabelProviderListener
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener) {

  }

}
