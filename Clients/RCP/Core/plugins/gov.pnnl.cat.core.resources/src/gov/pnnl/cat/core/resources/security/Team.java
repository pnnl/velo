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

import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;


/**
 */
/**
 * @author D3K339
 *
 */
public class Team extends Group implements ITeam, IProfilable {
  protected Profile profile;
  
  /**
   * 
   */
  public Team() {
    super();
    this.profile = new TeamProfile();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.GroupImpl#clone()
   */
  @Override
  public Team clone() {
      Team newTeam = (Team)super.clone();
      if(newTeam != null) {
        newTeam.profile = (Profile)this.profile.clone();
      } 
      return newTeam;
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.ITeam#getDescription()
   */
  @Override
  public String getDescription() {
    return profile.getProperty(VeloConstants.PROP_DESCRIPTION);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.ITeam#setDescription(java.lang.String)
   */
  public void setDescription(String desc) {
    profile.setProperty(VeloConstants.PROP_DESCRIPTION, desc);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreated()
   */
  @Override
  public String getCreated() {
    return this.profile.getCreated();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreator()
   */
  @Override
  public String getCreator() {
   return profile.getCreator();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getEmail()
   */
  @Override
  public String getEmail() {
    return profile.getEmail();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getHomeFolder()
   */
  @Override
  public CmsPath getHomeFolder() {
   return profile.getHomeFolder();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getID()
   */
  @Override
  public String getID() {
    return profile.getID();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getNewPicture()
   */
  @Override
  public File getNewPicture() {
    return profile.getNewPicture();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPicture()
   */
  @Override
  public File getPicture() throws ResourceException {
    return profile.getPicture();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#hasPicture()
   */
  @Override
  public boolean hasPicture() {
    return profile.hasPicture();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#isPictureDeleted()
   */
  public boolean isPictureDeleted() {
   return profile.isPictureDeleted();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setDeletePicture(boolean)
   */
  @Override
  public void setDeletePicture(boolean delete) {
    profile.setDeletePicture(delete); 
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setEmail(java.lang.String)
   */
  public void setEmail(String email) {
   profile.setEmail(email);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPicture(java.io.File)
   */
  @Override
  public void setPicture(File picture) {
    profile.setPicture(picture);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getProperty(java.lang.String)
   */
  @Override
  public String getProperty(String key) {
   return profile.getProperty(key);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty(String key, String value) {
    profile.setProperty(key, value);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getMultiValuedProperty(java.lang.String)
   */
  @Override
  public String[] getMultiValuedProperty(String key) {
    return profile.getMultiValuedProperty(key);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(java.lang.String, java.lang.String[])
   */
  @Override
  public void setProperty(String key, String[] value) {
    profile.setProperty(key, value);
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPictureMimetype()
   */
  @Override
  public String getPictureMimetype() {
    return profile.getPictureMimetype();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPictureMimetype(java.lang.String)
   */
  @Override
  public void setPictureMimetype(String type) {
    profile.setPictureMimetype(type);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setHomeFolderProperty(java.lang.String)
   */
  @Override
  public void setHomeFolderProperty(String folderPath) {
    profile.setHomeFolderProperty(folderPath);
  }

}

