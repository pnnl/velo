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

import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Support any {@link IResource} with the given {@link #getAspect()}.
 * <p>
 * Retrieve {@link Image} and {@link ImageDescriptor} from the provided {@link #getImageRegistry()}, using the {@link #getImageKey()}.
 * </p>
 * @version $Revision: 1.0 $
 */
public abstract class AspectResourceImageFactory implements ResourceImageFactory {

  /**
   * Get the {@link String} representing the aspect the sub-class handles.
   * 
  
   * @return {@link String} */
  protected abstract String getAspect();

  /**
   * {@inheritDoc}
   * 
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImage(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public Image getImage(IResource resource, int size) {
    if(isSupported(resource)) {
      return getImageRegistry().get(getImageKey(resource, size));
    }
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImageDescriptor(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public ImageDescriptor getImageDescriptor(IResource resource, int size) {
    if(isSupported(resource)) {
      return getImageRegistry().getDescriptor(getImageKey(resource, size));
    }
    return null;
  }

  /**
   * Get the String key for the image found in the {@link org.eclipse.ui.internal.UIPlugin#getImageRegistry()}.
   * <p>
   * Images are preloaded by {@link org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(ImageRegistry)}
   * </p>
   * 
   * @param resource
   *          {@link IResource} to get image key for
   * @param size
   *          int image size to get image key for
  
   * @return String */
  protected abstract String getImageKey(IResource resource, int size);

  /**
   * Get the {@link ImageRegistry} to use when retrieving {@link Image} and {@link ImageDescriptor}.
   * <p>
   * This likely returns the current plug-in's {@link org.eclipse.ui.internal.UIPlugin#getImageRegistry()}.
   * </p>
   * 
  
   * @return {@link ImageRegistry} */
  protected abstract ImageRegistry getImageRegistry();

  public boolean isSupported(IResource resource) {
    boolean supported = false;
    IResource target = unlink(resource);

    if (target != null) {
      supported = target.hasAspect(getAspect());
    }

    return supported;
  }

  /**
   * If the given {@link IResource} is an instance of {@link ILinkedResource}, recursively look up its target and return that value.
   * <p>
   * Otherwise, just return the provided {@link IResource}.
   * </p>
   * 
   * @param resource
   *          {@link IResource} to unlink
  
   * @return unlinked {@link IResource} */
  protected IResource unlink(IResource resource) {
    IResource target = resource;

    while (target instanceof ILinkedResource) {
      target = ((ILinkedResource) target).getTarget();
    }

    return target;
  }
}
