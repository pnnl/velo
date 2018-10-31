package gov.pnnl.cat.ui.rcp.views.databrowser.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Placeholder for search folder API
 *
 */
public interface ISmartFolder {

  public String getName();
  public Image getImage();
  public ImageDescriptor getImageDescriptor();
  public String getQuery() throws Exception;
  
}
