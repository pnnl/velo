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

import gov.pnnl.cat.core.resources.security.IGroup;
import gov.pnnl.velo.model.CmsPath;

import java.util.List;

/**
 */
public class UnmodifiableGroup implements IGroup {

  protected IGroup group;
  
  /**
   * Constructor for UnmodifiableGroup.
   * @param group IGroup
   */
  public UnmodifiableGroup (IGroup group) {
    this.group = group;
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
   * @return IGroup
   * @see gov.pnnl.cat.core.resources.security.IGroup#clone()
   */
  public IGroup clone() {
    return group.clone();
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
   * Method getMembers.
   * @return List<String>
   * @see gov.pnnl.cat.core.resources.security.IGroup#getMembers()
   */
  public List<String> getMembers() {
    return group.getMembers();
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IGroup#getName()
   */
  public String getName() {
    return group.getName();
  }

  /**
   * Method getParent.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#getParent()
   */
  public CmsPath getParent() {
    return group.getParent();
  }

  /**
   * Method getPath.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#getPath()
   */
  public CmsPath getPath() {
    return group.getPath();
  }

  /**
   * Method getSubgroups.
   * @return List<CmsPath>
   * @see gov.pnnl.cat.core.resources.security.IGroup#getSubgroups()
   */
  public List<CmsPath> getSubgroups() {
    return group.getSubgroups();
  }

  /**
   * Method isMember.
   * @param userID String
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IGroup#isMember(String)
   */
  public boolean isMember(String userID) {
    return group.isMember(userID);
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
   * Method setSubgroups.
   * @param subgroups List<CmsPath>
   * @see gov.pnnl.cat.core.resources.security.IGroup#setSubgroups(List<CmsPath>)
   */
  public void setSubgroups(List<CmsPath> subgroups) {
    throw new UnsupportedOperationException("This object cannot be modified.  Create a clone first.");
  }

}
