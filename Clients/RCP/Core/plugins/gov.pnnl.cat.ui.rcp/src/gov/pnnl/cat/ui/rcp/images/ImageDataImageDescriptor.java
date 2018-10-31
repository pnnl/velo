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
package gov.pnnl.cat.ui.rcp.images;

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * Creates an image descriptor from an existing image.
 * 
 * Copied from Eclipse internal code, as they chose to make
 * it a protected package-only class :(
 * @version $Revision: 1.0 $
 */
public class ImageDataImageDescriptor extends ImageDescriptor {

    private ImageData data;
    
    /**
     * Original image being described, or null if this image is described
     * completely using its ImageData
     */
    private Image originalImage = null;
    
    /**
     * Creates an image descriptor, given an existing image.
     * 
     * @param originalImage
     */
    public ImageDataImageDescriptor(Image originalImage) {
        this(originalImage.getImageData());
        this.originalImage = originalImage;
    }
    
    /**
     * Creates an image descriptor, given some image data.
     * 
     * @param data describing the image
     */

    public ImageDataImageDescriptor(ImageData data) {
        this.data = data;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#create(org.eclipse.swt.graphics.Device)
     */
    /**
     * Method createResource.
     * @param device Device
     * @return Object
     * @throws DeviceResourceException
     */
    public Object createResource(Device device) throws DeviceResourceException {

        // If this descriptor is an existing font, then we can return the original font
        // if this is the same device.
        if (originalImage != null) {
            // If we're allocating on the same device as the original font, return the original.
            if (originalImage.getDevice() == device) {
                return originalImage;
            }
        }
        
        return super.createResource(device);
    }
  
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#destroy(java.lang.Object)
     */
    /**
     * Method destroyResource.
     * @param previouslyCreatedObject Object
     */
    public void destroyResource(Object previouslyCreatedObject) {
        if (previouslyCreatedObject == originalImage) {
            return;
        }
        
        super.destroyResource(previouslyCreatedObject);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
     */
    public ImageData getImageData() {
        return data;
    }
    
    /* (non-Javadoc)
     * @see Object#hashCode
     */
    /**
     * Method hashCode.
     * @return int
     */
    public int hashCode() {
       if (originalImage != null) {
             return System.identityHashCode(originalImage);
         }
         return data.hashCode();
    }

    /* (non-Javadoc)
     * @see Object#equals
     */
    /**
     * Method equals.
     * @param obj Object
     * @return boolean
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageDataImageDescriptor)) {
      return false;
    } 
        
        ImageDataImageDescriptor imgWrap = (ImageDataImageDescriptor) obj;
        
        //Intentionally using == instead of equals() as Image.hashCode() changes
        //when the image is disposed and so leaks may occur with equals()
       
        if (originalImage != null) {
            return imgWrap.originalImage == originalImage;
        }
        
        return (imgWrap.originalImage == null && data.equals(imgWrap.data));
    }
    
}
