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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;

/**
 * Container for Tree Viewers that holds arbitrary sets of 
 * objects to be shown.
 */
public class GenericContainer extends PlatformObject {
  
  protected final static Object[] EMPTY_ARRAY = new Object[0];
  protected IResourceManager mgr = ResourcesPlugin.getResourceManager();

  protected Object[] children;
  protected Object parent = null;
  protected String name;
  protected String imagePluginId; // plugin containing the image for this container
  protected String imagePath; // path relative to that plugin where image is located


  /**
   * Constructor for ResourceContainerRoot.
   * @param name String
   * @param roots IResource[]
   */
  public GenericContainer(String name, Object parent, Object... children) {
    setChildren(children);
    this.name = name;
    this.parent = parent;
  }
  
  protected void setChildren(Object[] children) {
    this.children = children;
  }
  
  public GenericContainer(String name, String imagePluginId, String imagePath, Object parent, Object... children) {
    this(name, parent, children);
    this.imagePluginId = imagePluginId;
    this.imagePath = imagePath;
  }
  
  
  public GenericContainer(String name) {
    this.name = name;
  }

  public Object getParent() {
    return parent;
  }
  
  /**
   * Method getRoots.
   * @return IResource[]
   */
  public Object[] getChildren() {
    if (children != null) {
      return children;
    
    } else {
      return new Object[0];
    }
  }

  /**
   * Method getName.
   * @return String
   */
  public String getName(){
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  public ImageDescriptor getImageDescriptor() {
    if(imagePluginId != null && imagePath != null) {
      return SWTResourceManager.getPluginImageDescriptor(imagePluginId, imagePath);
    }
    
    return null;
  }
  
  public Image getImage() {
    if(imagePluginId != null && imagePath != null) {
      return SWTResourceManager.getPluginImage(imagePluginId, imagePath);
    }
    
    return null;
  }
}
