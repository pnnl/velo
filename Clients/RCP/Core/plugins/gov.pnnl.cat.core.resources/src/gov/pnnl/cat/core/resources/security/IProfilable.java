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

import gov.pnnl.velo.model.CmsPath;

import java.io.File;


/**
 * Base interface for objects that have profiles (i.e.,
 * users and teams)
 * @version $Revision: 1.0 $
 */
public interface IProfilable extends Cloneable {
  
  /**
   * Returns the uuid for the profile node
  
   * @return String
   */
  public String getID();
 
  /**
   * Indicates if this profile has a picture.
  
  
   * @return <code>true</code> if the profile has a picture, and <code>false</code> otherwise. * @see #getPicture() */
  public boolean hasPicture();

  /**
   * Returns the InputStream from the server of the picture for this profile.
   * If no picture has been set for this picture, a <code>ResourceException</code> will be thrown.
   * For this reason, {@link #hasPicture()} should be used to guard uses of this method. 
   * @return an File for the content of the picture. * @throws ResourceException if an error occurs retrieving the content from the server, or if no picture has been set. * @see #hasPicture() */
  public File getPicture();
 
  /**
   * Method getProperty.
   * @param key QualifiedName
   * @return String
   */
  public String getProperty(String key);
  
  /**
   * Method getMultiValuedProperty.
   * @param key QualifiedName
   * @return String[]
   */
  public String[] getMultiValuedProperty(String key);
  
  /**
   * Method setProperty.
   * @param key QualifiedName
   * @param value String
   */
  public void setProperty(String key, String value); 
  
  /**
   * Method setProperty.
   * @param key QualifiedName
   * @param value String[]
   */
  public void setProperty(String key, String[] value);
  
  /**
   *
   * Returns the home folder for the profile.
   * @return CmsPath
   */
  public CmsPath getHomeFolder();

  /**
   * Method getEmail.
   * @return String
   */
  public String getEmail();
  /**
   * Method setEmail.
   * @param email String
   */
  public void setEmail(String email);
  
  /**
   * Returns the File for the new image for this profile.
   * If the profile is not currently being updated with a new image, this
   * will return null.
  
   * @return File
   */
  public File getNewPicture();

  /**
   * Returns true if the profile's current picture should be deleted.
   * If the profile is not currently being updated, this 
   * will return false.
  
   * @return boolean
   */
  public boolean isPictureDeleted();
  
  /**
   * Sets the new image for this profile.
   * @param picture
   */
  public void setPicture(File picture);

  /**
   * Method setPictureMimetype.
   * @param type String
   */
  public void setPictureMimetype(String type);
  /**
   * Method getPictureMimetype.
   * @return String
   */
  public String getPictureMimetype();
  
  /**
   * Method setDeletePicture.
   * @param delete boolean
   */
  public void setDeletePicture(boolean delete);

  /**
   * Method getCreated.
   * @return String
   */
  public String getCreated();

  /**
   * Method getCreator.
   * @return String
   */
  public String getCreator();


  /**
   * Method setHomeFolderProperty.
   * @param folderPath String
   */
  public void setHomeFolderProperty(String folderPath);

  public String getName();

  
}
