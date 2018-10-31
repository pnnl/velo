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
/**
 * 
 */
package gov.pnnl.velo.ui.images;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.CatAbstractUIPlugin;
import gov.pnnl.cat.ui.images.ResourceImageFactory;
import gov.pnnl.velo.util.VeloConstants;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public abstract class AbstractMimetypeBasedImageFactory implements ResourceImageFactory {

  /**
   * @return String
   */
  protected abstract String getMimetype();
  protected abstract String getImageName(IResource resource);
  protected abstract CatAbstractUIPlugin getPlugin();
  
  protected String getImagePath(CatAbstractUIPlugin plugin, String imageName, int size) {
    String sizeStr = String.valueOf(size);
    String path = "icons/" + sizeStr + "x" + sizeStr + "/" + imageName;
    return plugin.getAbsolutePath(path);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImage(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public Image getImage(IResource resource, int size) {
    if(isSupported(resource)) {
      return SWTResourceManager.getImage(getImagePath(getPlugin(), getImageName(resource), size));
    }
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImageDescriptor(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public ImageDescriptor getImageDescriptor(IResource resource, int size) {
    if(isSupported(resource)) {
      return SWTResourceManager.getImageDescriptor(getImagePath(getPlugin(), getImageName(resource), size));
    }
    return null;
  }

  public boolean isSupported(IResource resource) {
    boolean supported = false;
    try {
      String mimetype = resource.getPropertyAsString(VeloConstants.PROP_MIMETYPE);
      if(mimetype != null) {
        supported = getMimetype().equals(mimetype);
      }
    } catch(Throwable e) {
      // ignore - this could happen when items are deleted and we don't have
      // a big notificatioin throttle on the server
    }
    return supported;
  }

}
