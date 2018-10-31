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
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/***
 * This is a lightweight handle to a resource in the ResourceManager, who 
 * maintains the cache and does all the real work.
 * @version $Revision: 1.0 $
 */
public class EclipseFileImpl extends EclipseResourceImpl implements IFile {
  private static Logger logger = CatLogger.getLogger(EclipseFileImpl.class);
  
  /**
   * Constructor for EclipseFileImpl.
   * @param path CmsPath
   * @param type QualifiedName
   * @param mgr IResourceManager
   */
  public EclipseFileImpl(CmsPath path, String type, IResourceManager mgr) {
    super(path, type, mgr);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#setContent(java.lang.String)
   */
  @Override
  public void setContent(String content) {
    try {
      File file = File.createTempFile(getName(), ".txt");
      file.deleteOnExit();
      FileUtils.writeStringToFile(file, content);
      mgr.createFile(path, file);
    } catch (ResourceException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("Failed to set content.", e);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getContent()
   */
  @Override
  public InputStream getContent() {
    return getResourceManager().getContentProperty(getPath(), VeloConstants.PROP_CONTENT);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getContentAsText()
   */
  @Override
  public InputStream getContentAsText() {
    TransformData textTransform = getTextTransform();
    InputStream ret = null;

    // only try to return the text content if no error occurred on the transform
    if(textTransform != null && textTransform.getErrorMessage() == null) {
      ret = getResourceManager().getContentProperty(path, textTransform.getContentProperty());
    }
    return ret;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getWebdavUrl()
   */
  @Override
  public URL getWebdavUrl() {
    return getResourceManager().getWebdavUrl(getPath());
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getHttpUrl()
   */
  @Override
  public URL getHttpUrl() {
    return getHttpUrl(IFile.DIRECT);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getHttpUrl(java.lang.String)
   */
  @Override
  public URL getHttpUrl(String attachmentMode) {
    return getResourceManager().getHttpUrl(getPath(), attachmentMode);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getSize()
   */
  @Override
  public long getSize() {
    String size = getResourceManager().getProperty(getPath(), VeloConstants.PROP_SIZE, false);
    long sizeValue = 0;
    if(size != null){
      sizeValue = Long.parseLong(size);
    } else{
      logger.warn(getPath()+" has null value for size property");
    }
    return sizeValue;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getFileExtension()
   */
  @Override
  public String getFileExtension() {
    CmsPath path = getPath();
    return path.last().getFileExtension();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getType()
   */
  @Override
  public int getType() {
    return getAspectsType() | IResource.FILE | IResource.PHYSICAL;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getTransforms()
   */
  @Override
  public Map<String, TransformData> getTransforms() {    
    return getResourceManager().getTransforms(getPath());
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getTextTransform()
   */
  @Override
  public TransformData getTextTransform() {    
    Map<String, TransformData> transforms = getResourceManager().getTransforms(getPath());
    return transforms.get(TransformData.TEXT);    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IFile#getMimetype()
   */
  @Override
  public String getMimetype() {
    String mimetype = getPropertyAsString(VeloConstants.PROP_MIMETYPE);
    
    if(mimetype == null) {
      // get it from the content property
      String contentUrl = getPropertyAsString(VeloConstants.PROP_CONTENT);
      ContentData cd = new ContentData(contentUrl);
      mimetype = cd.getMimetype();      
    }
    return mimetype; 
  }

}
