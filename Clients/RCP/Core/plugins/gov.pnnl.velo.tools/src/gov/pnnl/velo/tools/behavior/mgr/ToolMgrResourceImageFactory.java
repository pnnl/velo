/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.images.ResourceImageFactory;

/**
 * @author d3k339
 *
 */
public class ToolMgrResourceImageFactory extends ToolMgrBehavior implements ResourceImageFactory {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImage(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public Image getImage(IResource resource, int size) {
    return toolManager.getImage(resource, size);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImageDescriptor(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public ImageDescriptor getImageDescriptor(IResource resource, int size) {
    return toolManager.getImageDescriptor(resource, size);
  }

}
