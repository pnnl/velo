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
package gov.pnnl.cat.ui.images;

import gov.pnnl.cat.core.resources.IResource;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Supply images for an {@link IResource}, overriding the default CAT images.
 * <p>
 * Implementations should cache images to prevent heavy memory use.
 * </p>
 * @version $Revision: 1.0 $
 */
public interface ResourceImageFactory {

  /**
   * Return the {@link Image} for the given {@link IResource} and image size.
   * 
   * @param resource
   *          {@link IResource} to retrieve {@link Image} for (may be null)
   * @param size
   *          int image size
  
   * @return {@link Image} */
  Image getImage(IResource resource, int size);

  /**
   * Return the {@link ImageDescriptor} for the given {@link IResource} and image size.
   * 
   * @param resource
   *          {@link IResource} to retrieve {@link ImageDescriptor} for (may be null)
   * @param size
   *          int image size
  
   * @return {@link ImageDescriptor} */
  ImageDescriptor getImageDescriptor(IResource resource, int size);

}
