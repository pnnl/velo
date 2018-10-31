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
package gov.pnnl.cat.core.resources.security;

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 */
public abstract class Profile implements IProfilable {

  protected CmsPath homeFolder;
  protected File newPicture;
  protected String pictureMimetype;
  protected boolean deletePicture;
 
  protected Map<String, Object> properties = new HashMap<String, Object>();
  
  private static Logger logger = CatLogger.getLogger(Profile.class);
  
  public Profile() {
 
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public Profile clone() {
    try {
      Profile newProfile = (Profile)super.clone();
      if (homeFolder != null) {
        newProfile.homeFolder = new CmsPath(this.homeFolder);
      }
      newProfile.properties = new HashMap<String, Object>(this.properties);
      return newProfile;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o.getClass().equals(getClass())) {
      Profile user = (Profile) o;
      return user.getHomeFolder().equals(this.homeFolder);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreated()
   */
  @Override
  public String getCreated() {
    return (String)properties.get(VeloConstants.PROP_CREATED);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreator()
   */
  @Override
  public String getCreator() {
    return (String)properties.get(VeloConstants.PROP_CREATOR);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getEmail()
   */
  public abstract String getEmail(); 

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getHomeFolder()
   */
  public abstract CmsPath getHomeFolder();

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getID()
   */
  @Override
  public String getID() {
    return (String)properties.get(VeloConstants.PROP_UUID);
  }
  
  @Override
  public String getName() {
    return (String)properties.get(VeloConstants.PROP_NAME);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getNewPicture()
   */
  @Override
  public File getNewPicture() {
    return this.newPicture;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPicture()
   */
  @Override
  public File getPicture() throws ResourceException {
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    return mgr.getContentPropertyAsFile(getID(), VeloConstants.PROP_PICTURE);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getProperty(java.lang.String)
   */
  @Override
  public String getProperty(String key) {
    return (String)properties.get(key);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return this.homeFolder.hashCode();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#hasPicture()
   */
  @Override
  public boolean hasPicture() {
   return properties.get(VeloConstants.PROP_PICTURE) != null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#isPictureDeleted()
   */
  @Override
  public boolean isPictureDeleted() {
    return this.deletePicture;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setDeletePicture(boolean)
   */
  @Override
  public void setDeletePicture(boolean delete) {
    this.deletePicture = delete;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setEmail(java.lang.String)
   */
  public abstract void setEmail(String email); 

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setHomeFolderProperty(java.lang.String)
   */
  public abstract void setHomeFolderProperty(String folderPath);
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPicture(java.io.File)
   */
  @Override
  public void setPicture(File picture) {
    this.newPicture = picture;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty(String key, String value) {
    properties.put(key, value);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.homeFolder.toString();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getMultiValuedProperty(java.lang.String)
   */
  @Override
  public String[] getMultiValuedProperty(String key) {
    return (String[])properties.get(key);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(java.lang.String, java.lang.String[])
   */
  @Override
  public void setProperty(String key, String[] value) {
    properties.put(key, value);   
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPictureMimetype()
   */
  @Override
  public String getPictureMimetype() {
    return pictureMimetype;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPictureMimetype(java.lang.String)
   */
  @Override
  public void setPictureMimetype(String type) {
    pictureMimetype = type;
  }
}
