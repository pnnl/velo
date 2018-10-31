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
import java.io.InputStream;


/**
 */
public class UserImpl implements IProfilable, IUser  {
 
  protected UserProfile profile;
  private String password;
  private boolean admin;

  public UserImpl() {
    super();
    this.profile = new UserProfile();
  }

  /**
   * Method getFirstName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getFirstName()
   */
  public String getFirstName() {
    return profile.getProperty(VeloConstants.PROP_USER_FIRSTNAME);
  }

  /**
   * Method getFullName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getFullName()
   */
  public String getFullName() {
    String firstName = getFirstName();
    String lastName = getLastName();
    StringBuffer fullName = new StringBuffer();
    if (firstName != null && firstName.length() > 0) {
      fullName.append(firstName);
    }
    if (lastName != null && lastName.length() > 0) {
      if (firstName != null && firstName.length() > 0) {
        fullName.append(" ");
      }
      fullName.append(lastName);
    }
    return fullName.toString();
  }

  /**
   * Method getLastNameFirstFullName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getLastNameFirstFullName()
   */
  public String getLastNameFirstFullName() {
    String firstName = getFirstName();
    String lastName = getLastName();
    StringBuffer fullName = new StringBuffer();
    if (lastName != null && lastName.length() > 0) {
      fullName.append(lastName);
    }
    if (firstName != null && firstName.length() > 0) {
      if (lastName != null && lastName.length() > 0) {
        fullName.append(", ");
      }
      fullName.append(firstName);
    }
    return fullName.toString();
  }

  /**
   * Method getLastName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getLastName()
   */
  public String getLastName() {
    return profile.getProperty(VeloConstants.PROP_USER_LASTNAME);
  }

  /**
   * Method getPhoneNumber.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getPhoneNumber()
   */
  public String getPhoneNumber() {
    return profile.getProperty(VeloConstants.PROP_PRIMARY_PHONE);
  }

  /**
   * Method getUsername.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getUsername()
   */
  public String getUsername() {
    return profile.getProperty(VeloConstants.PROP_USERNAME);
  }

  /**
   * Method isAdmin.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IUser#isAdmin()
   */
  public boolean isAdmin() {
    return this.admin;
  }

  /**
   * Method setAdmin.
   * @param admin boolean
   */
  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  /**
   * Method setFirstName.
   * @param firstName String
   * @see gov.pnnl.cat.core.resources.security.IUser#setFirstName(String)
   */
  public void setFirstName(String firstName) {
    profile.setProperty(VeloConstants.PROP_USER_FIRSTNAME, firstName);
  }

  /**
   * Method setLastName.
   * @param lastName String
   * @see gov.pnnl.cat.core.resources.security.IUser#setLastName(String)
   */
  public void setLastName(String lastName) {
    profile.setProperty(VeloConstants.PROP_USER_LASTNAME, lastName);
  }

  /**
   * Method setPassword.
   * @param password String
   * @see gov.pnnl.cat.core.resources.security.IUser#setPassword(String)
   */
  public void setPassword(String password) {
    // TODO: Do we really want to keep this around in memory?
    this.password = password;
  }

  /**
   * Method setPhoneNumber.
   * @param phoneNumber String
   * @see gov.pnnl.cat.core.resources.security.IUser#setPhoneNumber(String)
   */
  public void setPhoneNumber(String phoneNumber) {
    profile.setProperty(VeloConstants.PROP_PRIMARY_PHONE, phoneNumber);
  }

  /**
   * Method setUsername.
   * @param username String
   */
  public void setUsername(String username) {
    profile.setProperty(VeloConstants.PROP_USERNAME, username);
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return this.getUsername() + " (" + getFullName() + ")";
  }

  /**
   * Method equals.
   * @param o Object
   * @return boolean
   */
  public boolean equals(Object o) {
    if (o.getClass().equals(getClass())) {
      UserImpl user = (UserImpl) o;
      return user.getUsername().equals(this.getUsername());
    }
    return false;
  }

  /**
   * Method hashCode.
   * @return int
   */
  public int hashCode() {
    return this.getUsername().hashCode();
  }

  /**
   * Method getPassword.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IUser#getPassword()
   */
  public String getPassword() {
    return password;
  }

  /**
   * Method clone.
   * @return UserImpl
   * @see gov.pnnl.cat.core.resources.security.IUser#clone()
   */
  public UserImpl clone() {
    try {
      UserImpl newUser = (UserImpl)super.clone();
      if(newUser != null) {
        newUser.profile = (UserProfile)this.profile.clone();
      } 
      return newUser;

    } catch (CloneNotSupportedException e) {
      return null;
    }
  }


  /**
   * Method getCreated.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreated()
   */
  public String getCreated() {
    return this.profile.getCreated();
  }

  /**
   * Method getCreator.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreator()
   */
  public String getCreator() {
   return profile.getCreator();
  }

  /**
   * Method getEmail.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getEmail()
   */
  public String getEmail() {
    return profile.getEmail();
  }

  /**
   * Method getHomeFolder.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getHomeFolder()
   */
  public CmsPath getHomeFolder() {
   return profile.getHomeFolder();
  }


  /**
   * Method getID.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getID()
   */
  public String getID() {
    return profile.getID();
  }
  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getName()
   */
  public String getName() {
    return profile.getName();
  }

  /**
   * Method getNewPicture.
   * @return File
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getNewPicture()
   */
  public File getNewPicture() {
    return profile.getNewPicture();
  }

  /**
   * Method getPicture.
   * @return File
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPicture()
   */
  public File getPicture() throws ResourceException {
    return profile.getPicture();
  }

  /**
   * Method hasPicture.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#hasPicture()
   */
  public boolean hasPicture() {
    return profile.hasPicture();
  }

  /**
   * Method isPictureDeleted.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#isPictureDeleted()
   */
  public boolean isPictureDeleted() {
   return profile.isPictureDeleted();
  }

  /**
   * Method setDeletePicture.
   * @param delete boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setDeletePicture(boolean)
   */
  public void setDeletePicture(boolean delete) {
    profile.setDeletePicture(delete); 
  }

  /**
   * Method setEmail.
   * @param email String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setEmail(String)
   */
  public void setEmail(String email) {
   profile.setEmail(email);
  }

  /**
   * Method setPicture.
   * @param picture File
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPicture(InputStream)
   */
  public void setPicture(File picture) {
    profile.setPicture(picture);
  }

  /**
   * Method getProperty.
   * @param key 
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getProperty()
   */
  public String getProperty(String key) {
   return profile.getProperty(key);
  }

  /**
   * Method setProperty.
   * @param key 
   * @param value String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(, String)
   */
  public void setProperty(String key, String value) {
    profile.setProperty(key, value);
  }

  /**
   * Method getMultiValuedProperty.
   * @param key 
   * @return String[]
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getMultiValuedProperty()
   */
  public String[] getMultiValuedProperty(String key) {
    return profile.getMultiValuedProperty(key);
  }

  /**
   * Method setProperty.
   * @param key 
   * @param value String[]
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(, String[])
   */
  public void setProperty(String key, String[] value) {
    profile.setProperty(key, value);
  }

  /**
   * Method getPictureMimetype.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPictureMimetype()
   */
  public String getPictureMimetype() {
    return profile.getPictureMimetype();
  }

  /**
   * Method setPictureMimetype.
   * @param type String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPictureMimetype(String)
   */
  public void setPictureMimetype(String type) {
    profile.setPictureMimetype(type);
  }

  /**
   * Method setHomeFolderProperty.
   * @param folderPath String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setHomeFolderProperty(String)
   */
  @Override
  public void setHomeFolderProperty(String folderPath) {
    profile.setHomeFolderProperty(folderPath);
  }

  
}
