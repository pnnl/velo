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
package gov.pnnl.cat.core.internal.resources.security;

import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.io.InputStream;
import java.util.Map;


/**
 */
public class UnmodifiableUserImpl implements IUser {
  private IUser user;

  /**
   * Constructor for UnmodifiableUserImpl.
   * @param user IUser
   */
  public UnmodifiableUserImpl(IUser user) {
    this.user = user;
  }

  /**
   * Method clone.
   * @return IUser
   * @see gov.pnnl.cat.core.resources.security.IUser#clone()
   */
  public IUser clone() {
    return user.clone();
  }

  /**
   * Method getCreated.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreated()
   */
  public String getCreated() {
    return user.getCreated();
  }

  /**
   * Method getCreator.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreator()
   */
  public String getCreator() {
    return user.getCreator();
  }

  /**
   * Method getEmail.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getEmail()
   */
  public String getEmail() {
   return user.getEmail();
  }

  /**
   * Method getFirstName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getFirstName()
   */
  public String getFirstName() {
    return user.getFirstName();
  }

  /**
   * Method getFullName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getFullName()
   */
  public String getFullName() {
    return user.getFullName();
  }

  /**
   * Method getLastNameFirstFullName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getLastNameFirstFullName()
   */
  public String getLastNameFirstFullName() {
    return user.getLastNameFirstFullName();
  }

  /**
   * Method getHomeFolder.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getHomeFolder()
   */
  public CmsPath getHomeFolder() {
    return user.getHomeFolder();
  }

  /**
   * Method getID.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getID()
   */
  public String getID() {
    return user.getID();
  }

  @Override
  public String getName() {
    return user.getName();
  }
  
  
  /**
   * Method getLastName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getLastName()
   */
  public String getLastName() {
    return user.getLastName();
  }

  /**
   * Method getNewPicture.
   * @return File
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getNewPicture()
   */
  public File getNewPicture() {
    return user.getNewPicture();
  }

  /**
   * Method getPassword.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getPassword()
   */
  public String getPassword() {
    return user.getPassword();
  }

  /**
   * Method getPhoneNumber.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getPhoneNumber()
   */
  public String getPhoneNumber() {
    return user.getPhoneNumber();
  }

  /**
   * Method getPicture.
   * @return File
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPicture()
   */
  public File getPicture() throws ResourceException {
    return user.getPicture();
  }

  /**
   * Method getProperty.
   * @param key String
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getProperty(String)
   */
  public String getProperty(String key) {
    return user.getProperty(key);
  }


  /**
   * Method getUsername.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getUsername()
   */
  public String getUsername() {
    return user.getUsername();
  }

  /**
   * Method hasPicture.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#hasPicture()
   */
  public boolean hasPicture() {
    return user.hasPicture();
  }

  /**
   * Method isAdmin.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IUser#isAdmin()
   */
  public boolean isAdmin() {
    return user.isAdmin();
  }

  /**
   * Method isPictureDeleted.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#isPictureDeleted()
   */
  public boolean isPictureDeleted() {
    return user.isPictureDeleted();
  }

  /**
   * Method setDeletePicture.
   * @param delete boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setDeletePicture(boolean)
   */
  public void setDeletePicture(boolean delete) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setEmail.
   * @param email String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setEmail(String)
   */
  public void setEmail(String email) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setFirstName.
   * @param firstName String
   * @see gov.pnnl.cat.core.resources.security.IUser#setFirstName(String)
   */
  public void setFirstName(String firstName) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setLastName.
   * @param lastName String
   * @see gov.pnnl.cat.core.resources.security.IUser#setLastName(String)
   */
  public void setLastName(String lastName) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");  }

  /**
   * Method setPassword.
   * @param password String
   * @see gov.pnnl.cat.core.resources.security.IUser#setPassword(String)
   */
  public void setPassword(String password) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setPhoneNumber.
   * @param phoneNumber String
   * @see gov.pnnl.cat.core.resources.security.IUser#setPhoneNumber(String)
   */
  public void setPhoneNumber(String phoneNumber) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setPicture.
   * @param picture File
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPicture(InputStream)
   */
  public void setPicture(File picture) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setProperties.
   * @param props Map<String,String>
   */
  public void setProperties(Map<String, String> props) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");   
  }

  /**
   * Method setProperty.
   * @param key String
   * @param value String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(String, String)
   */
  public void setProperty(String key, String value) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return user.toString();
  }

  /**
   * Method getUser.
   * @return IUser
   */
  protected IUser getUser() {
    return user;
  }

  /**
   * Method equals.
   * @param o Object
   * @return boolean
   */
  public boolean equals(Object o) {
    if (o.getClass().equals(getClass())) {
      UnmodifiableUserImpl user = (UnmodifiableUserImpl) o;
      return user.getUser().equals(this.user);
    }
    return false;
  }

  /**
   * Method hashCode.
   * @return int
   */
  public int hashCode() {
    return user.hashCode();
  }

  /**
   * Method getMultiValuedProperty.
   * @param key String
   * @return String[]
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getMultiValuedProperty(String)
   */
  public String[] getMultiValuedProperty(String key) {
    return user.getMultiValuedProperty(key);
  }

  /**
   * Method setProperty.
   * @param key String
   * @param value String[]
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(String, String[])
   */
  public void setProperty(String key, String[] value) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");    
  }
  
  /**
   * Method getPictureMimetype.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPictureMimetype()
   */
  public String getPictureMimetype() {
    return user.getPictureMimetype();
  }

  /**
   * Method setPictureMimetype.
   * @param type String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPictureMimetype(String)
   */
  public void setPictureMimetype(String type) {
    user.setPictureMimetype(type);
  }

  /**
   * Method setHomeFolderProperty.
   * @param folderPath String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setHomeFolderProperty(String)
   */
  @Override
  public void setHomeFolderProperty(String folderPath) {
    user.setHomeFolderProperty(folderPath);
  }




}
