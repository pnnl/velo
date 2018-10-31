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

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;

/**
 * Container for the repository.
 */
public class RepositoryContainer extends GenericContainer {
  public static final String NAME = "Repository";
  public static final String IMAGE_PLUGIN_ID = "gov.pnnl.cat.ui";
  public static final String IMAGE_PATH = "icons/16x16/books.png";

  public RepositoryContainer(Object parent, Object... children) {
    super(NAME, IMAGE_PLUGIN_ID, IMAGE_PATH, parent, children);
  }

  /**
   * Override so we can wrapper resource so tree viewer knows the parent is this container
   * instead of another resource
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer#setChildren(java.lang.Object[])
   */
  @Override
  protected void setChildren(Object[] children) {
    IResource[] wrappedChildren = new IResource[children.length];
    
    for (int i = 0; i < children.length; i++) {
      IResource resource = (IResource)children[i];
      if(resource instanceof IFile) {
        wrappedChildren[i] = new IFileTreeWrapper((IFile)resource, this);
 
      } else if (resource instanceof IFolder){     
        wrappedChildren[i] = new IFolderTreeWrapper((IFolder)resource, this);
        
      } else {
        throw new RuntimeException("Trying to add a non-file or non-folder to RepositoryContainer");
      }
    }
    
    this.children = wrappedChildren;
    
  }

  
}
