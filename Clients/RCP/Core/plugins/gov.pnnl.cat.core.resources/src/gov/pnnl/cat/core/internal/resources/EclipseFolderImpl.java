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
package gov.pnnl.cat.core.internal.resources;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.velo.model.CmsPath;

import java.util.List;

import org.eclipse.core.runtime.QualifiedName;

/**
 */
public class EclipseFolderImpl extends EclipseResourceImpl implements IFolder {
  /**
   * Constructor for EclipseFolderImpl.
   * @param path CmsPath
   * @param type QualifiedName
   * @param mgr IResourceManager
   */
  public EclipseFolderImpl(CmsPath path, String type, IResourceManager mgr) {
    super(path, type, mgr);
  }

  /**
   * Method getChildren.
   * @return List<IResource>
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFolder#getChildren()
   */
  public List<IResource> getChildren() throws ResourceException {
    return mgr.getChildren(path);
  }

  /**
   * Method addFolder.
   * @param name String
   * @return IFolder
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFolder#addFolder(String)
   */
  public IFolder addFolder(String name) throws ResourceException {
    return getResourceManager().createFolder(getPath().append(name));
  }

  /**
   * Method getSize.
   * @return long
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFolder#getSize()
   */
  public long getSize() throws ResourceException {
    return getContents()[0];
  }

  /**
   * Method getContents.
   * @return long[]
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFolder#getContents()
   */
  public long[] getContents() throws ResourceException {
    List<IResource> children = getChildren();
    long size = 0;
    long totalFiles = 0;
    long totalFolders = 0;
    long childSize;

    for (IResource child : children) {

      childSize = 0;

      if (child instanceof IFile) {
        IFile file = (IFile) child;
        totalFiles++;

        // skip links.
        // we will treat them as a file with a size of 0
        if (!file.isLink()) {
          childSize = file.getSize();
          size += childSize;
        }

      } else if (child instanceof IFolder) {
//        IFolder folder = (IFolder) child;
        totalFolders++;

        // again, skip links
        // Don't do this because it loads folders recursively down the tree
//        if (!folder.isLink()) {
//          long[] childContents;
//          childContents = folder.getContents();
//          size += childContents[0];
//          totalFiles += childContents[1];
//          totalFolders += childContents[2];
//        }

      }
    }

    return new long[] {size, totalFiles, totalFolders};
  }

  /**
   * Method getType.
   * @return int
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IResource#getType()
   */
  public int getType() throws ResourceException {
    //a taxonomy folder is not a PHYSCIAL type, so don't include PHYSCIAL if this
    //eclipse folder has a taxonomy root or taxonomy foler aspect
    int aspects = getAspectsType();

    if(((IResource.TAXONOMY_FOLDER & aspects) == IResource.TAXONOMY_FOLDER) || 
       ((IResource.TAXONOMY_ROOT & aspects) == IResource.TAXONOMY_ROOT)){
      return aspects | IResource.FOLDER;
    }
    return aspects | IResource.FOLDER | IResource.PHYSICAL;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.EclipseResourceImpl#getMimetype()
   */
  @Override
  public String getMimetype() {
    String mimetype = super.getMimetype();
    if(mimetype == null) {
      mimetype = "Folder";
    }
    return mimetype;
  }
  
  
}
