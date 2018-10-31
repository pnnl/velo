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
package gov.pnnl.cat.ui.rcp.adapters;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IVirtualFolder;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.IFileTreeWrapper;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.IFolderTreeWrapper;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;

/**
 */
public class ResourceTransferObjectAdapter implements ITransferObjectAdapter {

  IResource resource = null;
  private Logger logger = CatLogger.getLogger(ResourceTransferObjectAdapter.class);
  
  /**
   * Constructor for ResourceAdapter.
   * @param adaptableObject Object
   */
  public ResourceTransferObjectAdapter(Object adaptableObject) {
    if(adaptableObject instanceof IFolderTreeWrapper) {
      resource = ((IFolderTreeWrapper)adaptableObject).getWrappedResource();
      
    } else if (adaptableObject instanceof IFileTreeWrapper) {
      resource =  ((IFileTreeWrapper)adaptableObject).getWrappedResource();
      
    } else {      
      resource = (IResource) adaptableObject;
    }
  }

  /**
   * Method getChildren.
   * @return ITransferObjectAdapter[]
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getChildren()
   */
  public ITransferObjectAdapter[] getChildren() {
    if (resource instanceof IFolder) {
      IFolder folder = (IFolder) resource;
      try {
        List<IResource> vecChildren = folder.getChildren();
        if(vecChildren == null) {
          vecChildren = new Vector<IResource>();
          logger.warn("Folder: " + folder.getPath().toDisplayString() + " has null children.");
        }
        ITransferObjectAdapter[] array = new ITransferObjectAdapter[vecChildren.size()];
        for (int i = 0; i < vecChildren.size(); i++) {
          array[i] = (ITransferObjectAdapter) ((IAdaptable)vecChildren.get(i)).getAdapter(ITransferObjectAdapter.class);
        }
        return array;
      } catch (ResourceException e) {
        // TODO Auto-generated catch block
        logger.error(e);
      }  
    }
    
    return new ITransferObjectAdapter[0];
  }

  /**
   * Method isFile.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isFile()
   */
  public boolean isFile() {
    if (resource instanceof IFile) {
      return true;
    }
    return false;
  }

  /**
   * Method isFolder.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isFolder()
   */
  public boolean isFolder() {
    if (resource instanceof IFolder) {
      return true;
    }    
    return false;
  }

  /**
   * Method getLabel.
   * @return String
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getLabel()
   */
  public String getLabel() {
    return resource.getName();
  }

  /**
   * Method canRead.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#canRead()
   */
  public boolean canRead() {
    return true;
  }

  /**
   * Method exists.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#exists()
   */
  public boolean exists() {
    return true;
  }

  /**
   * Method getType.
   * @return int
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getType()
   */
  public int getType() {
    return ITransferObjectAdapter.TYPE_RESOURCE_FILE;
  }

  /**
   * Method getObject.
   * @return Object
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getObject()
   */
  public Object getObject() {
    return resource;
  }

  /**
   * Method getPath.
   * @return String
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getPath()
   */
  public String getPath() {
    return resource.getPath().toString();
  }

  /**
   * Method isVirtualFolder.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isVirtualFolder()
   */
  public boolean isVirtualFolder() {
    if (resource instanceof IVirtualFolder) {
      return true;
    }
    return false;
  }

  /**
   * Method isLinked.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isLinked()
   */
  public boolean isLinked() {
    if (resource instanceof ILinkedResource) {
      logger.debug("resource instanceof ILinkedResource");
      return true;
    }
    return false;
  }

  /**
   * Method getSize.
   * @return long
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getSize()
   */
  public long getSize() {
//    if (isFile()) {
//      try {
//        return ((IFile)resource).getSize();
//      } catch (ResourceException e) {
//      }
//    }
    // Just used for file count right now
    return 1;
  }


}
