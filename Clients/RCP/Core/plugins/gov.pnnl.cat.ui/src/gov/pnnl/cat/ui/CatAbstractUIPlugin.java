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
package gov.pnnl.cat.ui;

import gov.pnnl.cat.core.util.PluginUtils;
import gov.pnnl.cat.logging.CatLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Support common features:
 * <ul>
 * <li>Load {@link ImageDescriptor} in the plug-in {@link #getImageRegistry()}</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public abstract class CatAbstractUIPlugin extends AbstractUIPlugin {
  protected Logger logger = CatLogger.getLogger(this.getClass());
  
  
  public File getAbsoluteFile(String relativePath) {
    return PluginUtils.getPluginFile(getBundle(), relativePath);
  }
  
  /**
   * Look up any resource in the plugin bundle based on its 
   * relative path from the bundle root.
   * @param relativePath
  
   * @return String
   */
  public String getAbsolutePath(String relativePath) {
    return PluginUtils.getAbsolutePath(getBundle(), relativePath);
  }
  
  /**
   * Method getUrl.
   * @param relativePath String
   * @return URL
   */
  public URL getUrl(String relativePath) {
    String absolutePath = getAbsolutePath(relativePath);
    File file = new File(absolutePath);
    URL url = null;
    
    try {
      url = file.toURI().toURL();
   
    } catch (MalformedURLException e) {
      logger.error("Failed to get url for file.", e);
    }
    return url;
  }
  
  /**
   * This assumes icons of different sizes MUST have the same name, but be in different
   * folders named exactly for their size.  For example icons/16x16/chart.png vs. 
   * icons/32x32/chart.png
   * @param imageName
   * @param size
   * @return
   */
  protected String getImagePath(String imageName, int size) {
    String sizeStr = String.valueOf(size);
    String path = "icons/" + sizeStr + "x" + sizeStr + "/" + imageName;
    return getAbsolutePath(path);
  }
  
  public Image getImage(String imageName, int size) {
    return SWTResourceManager.getImage(getImagePath(imageName, size));
  }

  public Image getImageByFullPath(String imagePath) {
    return SWTResourceManager.getImage(getAbsolutePath(imagePath));
  }

  public ImageDescriptor getImageDescriptor(String imageName, int size) {
    return SWTResourceManager.getImageDescriptor(getImagePath(imageName, size));
  } 
  
  public ImageDescriptor getImageDescriptorByFullPath(String imagePath) {
    return SWTResourceManager.getImageDescriptor(getAbsolutePath(imagePath));
  }
  
 
}
