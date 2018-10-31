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
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.TransformData;
import gov.pnnl.velo.model.CmsPath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 */
public class EclipseLinkedFile extends EclipseLinkedResource implements IFile {

  /**
   * Constructor for EclipseLinkedFile.
   * @param path CmsPath
   * @param type QualifiedName
   * @param mgr IResourceManager
   */
  public EclipseLinkedFile(CmsPath path, String type, IResourceManager mgr) {
    super(path, type, mgr);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#setContent(java.lang.String)
   */
  @Override
  public void setContent(String content) {
    ((IFile) getTarget()).setContent(content);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getMimetype()
   */
  @Override
  public String getMimetype() {
    // first check if this link target still exists
    IResource target = getTarget();
    
    if(target == null) {
      return "Link";
      
    } else {
      return ((IFile)target).getMimetype();
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getContent()
   */
  @Override
  public InputStream getContent() {
    // first check if this link target still exists
    IResource target = getTarget();
    
    if(target == null) {
      String text = "This is an invalid link as the target resource has been removed.";
      return new ByteArrayInputStream(text.getBytes());
      
    } else {
      return ((IFile)target).getContent();
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getContentAsText()
   */
  @Override
  public InputStream getContentAsText() {
    // first check if this link target still exists
    IResource target = getTarget();
    
    if(target == null) {
      String text = "This is an invalid link as the target resource has been removed.";
      return new ByteArrayInputStream(text.getBytes());
      
    } else {
      return ((IFile)target).getContentAsText();
    }

  }

//  /**
//   * Method getCifsPath.
//   * @return IPath
//   * @throws ResourceException
//   * @see gov.pnnl.cat.core.resources.IResource#getCifsPath()
//   */
//  @Override
//  public IPath getCifsPath() throws ResourceException {
//    // first check if this link target still exists
//    IResource target = getTarget();
//    if(target == null) {
//      return null;
//      
//    } else {
//      return ((IFile)target).getCifsPath();
//    }
//  }
  
  /**
   * Method getWebdavUrl.
   * @return URL
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFile#getWebdavUrl()
   */
  public URL getWebdavUrl() throws ResourceException {
    return ((IFile)this.getTarget()).getWebdavUrl();
  }
  
  /**
   * Method getHttpUrl.
   * @return URL
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFile#getHttpUrl()
   */
  public URL getHttpUrl() throws ResourceException {
    return ((IFile)this.getTarget()).getHttpUrl();
  }
  
  /**
   * Method getHttpUrl.
   * @param attachmentMode String
   * @return URL
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFile#getHttpUrl(String)
   */
  public URL getHttpUrl(String attachmentMode) throws ResourceException {
    return ((IFile)this.getTarget()).getHttpUrl(attachmentMode);
  }

  /**
   * Method getSize.
   * @return long
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFile#getSize()
   */
  public long getSize() throws ResourceException {
    IFile target = ((IFile)this.getTarget());
    if(target == null) {
      return 0;
    } else {
      return ((IFile)this.getTarget()).getSize();
    }
  }

  /**
   * Method getFileExtension.
   * @return String
   * @see gov.pnnl.cat.core.resources.IFile#getFileExtension()
   */
  public String getFileExtension() {
    return getPath().last().getFileExtension();
  }

  /**
   * Method getType.
   * @return int
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IResource#getType()
   */
  public int getType() throws ResourceException {
    return getAspectsType() | IResource.LINK | IResource.FILE;
  }

  /**
   * Method getTransforms.
   * @return Map<String,TransformData>
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFile#getTransforms()
   */
  public Map<String, TransformData> getTransforms() throws ResourceException {
    return ((IFile)this.getTarget()).getTransforms();
  }

  /**
   * Method getTextTransform.
   * @return TransformData
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.IFile#getTextTransform()
   */
  public TransformData getTextTransform() throws ResourceException {
    if(this.getTarget() != null) {
      return ((IFile)this.getTarget()).getTextTransform();
    } else {
      return null;
    }
  }
}
