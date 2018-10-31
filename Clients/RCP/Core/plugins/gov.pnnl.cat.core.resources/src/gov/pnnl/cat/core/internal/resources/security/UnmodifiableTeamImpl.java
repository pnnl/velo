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
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 */
public class UnmodifiableTeamImpl implements ITeam {

  protected ITeam team;
  
  /**
   * Constructor for UnmodifiableTeamImpl.
   * @param team ITeam
   */
  public UnmodifiableTeamImpl (ITeam team) {
    this.team = team;
  }

  /**
   * Method addMember.
   * @param userID String
   * @see gov.pnnl.cat.core.resources.security.IGroup#addMember(String)
   */
  public void addMember(String userID) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }
  
  /**
   * Method clone.
   * @return ITeam
   * @see gov.pnnl.cat.core.resources.security.ITeam#clone()
   */
  public ITeam clone() {
    return team.clone();
  }

  /**
   * Method deleteMember.
   * @param userID String
   * @see gov.pnnl.cat.core.resources.security.IGroup#deleteMember(String)
   */
  public void deleteMember(String userID) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method getCreated.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreated()
   */
  public String getCreated() {
    return team.getCreated();
  }

  /**
   * Method getCreator.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getCreator()
   */
  public String getCreator() {
    return team.getCreator();
  }

  /**
   * Method getDescription.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.ITeam#getDescription()
   */
  public String getDescription() {
    return team.getDescription();
  }

  /**
   * Method getEmail.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getEmail()
   */
  public String getEmail() {
    return team.getEmail();
  }

  /**
   * Method getHomeFolder.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getHomeFolder()
   */
  public CmsPath getHomeFolder() {
    return team.getHomeFolder();
  }

  /**
   * Method getID.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getID()
   */
  public String getID() {
    return team.getID();
  }

  /**
   * Method getMembers.
   * @return List<String>
   * @see gov.pnnl.cat.core.resources.security.IGroup#getMembers()
   */
  public List<String> getMembers() {
    return team.getMembers();
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IGroup#getName()
   */
  public String getName() {
    return team.getName();
  }

  /**
   * Method getNewPicture.
   * @return File
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getNewPicture()
   */
  public File getNewPicture() {
    return team.getNewPicture();
  }

  /**
   * Method getParent.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#getParent()
   */
  public CmsPath getParent() {
    return team.getParent();
  }

  /**
   * Method getPath.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#getPath()
   */
  public CmsPath getPath() {
    return team.getPath();
  }

  /**
   * Method getPicture.
   * @return File
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getPicture()
   */
  public File getPicture() throws ResourceException {
    return team.getPicture();
  }

  /**
   * Method getProperty.
   * @param key QualifiedName
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getProperty()
   */
  public String getProperty(String key) {
    return team.getProperty(key);
  }

  /**
   * Method getSubgroups.
   * @return List<CmsPath>
   * @see gov.pnnl.cat.core.resources.security.IGroup#getSubgroups()
   */
  public List<CmsPath> getSubgroups() {
    return team.getSubgroups();
  }

  /**
   * Method hasPicture.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#hasPicture()
   */
  public boolean hasPicture() {
    return team.hasPicture();
  }

  /**
   * Method isMember.
   * @param userID String
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IGroup#isMember(String)
   */
  public boolean isMember(String userID) {
    return team.isMember(userID);
  }

  /**
   * Method isPictureDeleted.
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IProfilable#isPictureDeleted()
   */
  public boolean isPictureDeleted() {
    return team.isPictureDeleted();
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
   * Method setDescription.
   * @param description String
   * @see gov.pnnl.cat.core.resources.security.ITeam#setDescription(String)
   */
  public void setDescription(String description) {
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
   * Method setMembers.
   * @param members List<String>
   * @see gov.pnnl.cat.core.resources.security.IGroup#setMembers(List<String>)
   */
  public void setMembers(List<String> members) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setParent.
   * @param parent CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#setParent(CmsPath)
   */
  public void setParent(CmsPath parent) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setPath.
   * @param path CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#setPath(CmsPath)
   */
  public void setPath(CmsPath path) {
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
   * @param key QualifiedName
   * @param value String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setProperty(String, String)
   */
  public void setProperty(String key, String value) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method setSubgroups.
   * @param subgroups List<CmsPath>
   * @see gov.pnnl.cat.core.resources.security.IGroup#setSubgroups(List<CmsPath>)
   */
  public void setSubgroups(List<CmsPath> subgroups) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return team.toString();
  }

  /**
   * Method getTeam.
   * @return ITeam
   */
  protected ITeam getTeam() {
    return team;
  }

  /**
   * Method equals.
   * @param o Object
   * @return boolean
   */
  public boolean equals(Object o) {
    if (o.getClass().equals(getClass())) {
      UnmodifiableTeamImpl team = (UnmodifiableTeamImpl) o;
      return team.getTeam().equals(this.team);
    }
    return false;
  }

  /**
   * Method hashCode.
   * @return int
   */
  public int hashCode() {
    return team.hashCode();
  }

  /**
   * Method getMultiValuedProperty.
   * @param key QualifiedName
   * @return String[]
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getMultiValuedProperty()
   */
  public String[] getMultiValuedProperty(String key) {
    return team.getMultiValuedProperty(key);
  }

  /**
   * Method setProperty.
   * @param key QualifiedName
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
    return team.getPictureMimetype();
  }

  /**
   * Method setPictureMimetype.
   * @param type String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setPictureMimetype(String)
   */
  public void setPictureMimetype(String type) {
    team.setPictureMimetype(type);
  }

  /**
   * Method setHomeFolderProperty.
   * @param folderPath String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setHomeFolderProperty(String)
   */
  @Override
  public void setHomeFolderProperty(String folderPath) {
    team.setHomeFolderProperty(folderPath);
  }

}
