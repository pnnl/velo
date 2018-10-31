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

import java.util.ArrayList;
import java.util.List;

/**
 */
public class GroupImpl implements IGroup {
  protected String name;
  protected CmsPath path;
  protected CmsPath parentPath;
  protected List<CmsPath> subgroups = new ArrayList<CmsPath>();
  protected List<String> members = new ArrayList<String>();
  
  /**
   * Method clone.
   * @return GroupImpl
   * @see gov.pnnl.cat.core.resources.security.IGroup#clone()
   */
  public GroupImpl clone() {
    try {
      GroupImpl newGroup = (GroupImpl)super.clone();
      newGroup.setPath(new CmsPath(this.path));
      if(this.parentPath != null)
      {
        newGroup.setParent(new CmsPath(this.parentPath));
      }
      newGroup.setMembers(new ArrayList<String>(this.members));
      newGroup.setSubgroups(new ArrayList<CmsPath>(this.subgroups) );
      return newGroup;
      
    } catch(CloneNotSupportedException e) {
      return null;
    }
  }
  
  /**
   * Method hashCode.
   * @return int
   */
  public int hashCode() {
    return this.getPath().toString().hashCode();
  }
  
  /**
   * Method equals.
   * @param o Object
   * @return boolean
   */
  public boolean equals(Object o) {
    if (o.getClass().equals(getClass())) {
      GroupImpl g = (GroupImpl) o;
      return g.getPath().toString().equals(this.getPath().toString());
    }
    return false;
  }

  /**
   * The server will throw an error if you try
   * to commit members that do not exist.
   * @param userID String
   * @see gov.pnnl.cat.core.resources.security.IGroup#addMember(String)
   */
  public void addMember(String userID) {
   if(!members.contains(userID)){
     members.add(userID);
   }
  }

  /**
   * Method deleteMember.
   * @param userID String
   * @see gov.pnnl.cat.core.resources.security.IGroup#deleteMember(String)
   */
  public void deleteMember(String userID) {
   members.remove(userID);
  }

  /**
   * Method getMembers.
   * @return List<String>
   * @see gov.pnnl.cat.core.resources.security.IGroup#getMembers()
   */
  public List<String> getMembers() {
    return this.members;
  }
  
  /**
   * Method setMembers.
   * @param members List<String>
   * @see gov.pnnl.cat.core.resources.security.IGroup#setMembers(List<String>)
   */
  public void setMembers(List<String> members) {
    this.members = members;
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IGroup#getName()
   */
  public String getName() {
    return this.name;
  }

  /**
   * Method getParent.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#getParent()
   */
  public CmsPath getParent() {
    return this.parentPath;
  }

  /**
   * Method setParent.
   * @param parentPath CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#setParent(CmsPath)
   */
  public void setParent(CmsPath parentPath) {
    this.parentPath = parentPath;
  }
  
  /**
   * Method getPath.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#getPath()
   */
  public CmsPath getPath() {
    return this.path;
  }

  /**
   * Method setPath.
   * @param path CmsPath
   * @see gov.pnnl.cat.core.resources.security.IGroup#setPath(CmsPath)
   */
  public void setPath(CmsPath path) {
    this.path = path;
    this.name = path.last().getName();
  }
  
  /**
   * Method getSubgroups.
   * @return List<CmsPath>
   * @see gov.pnnl.cat.core.resources.security.IGroup#getSubgroups()
   */
  public List<CmsPath> getSubgroups() {
    return this.subgroups;
  }
  
  /**
   * Method setSubgroups.
   * @param subgroups List<CmsPath>
   * @see gov.pnnl.cat.core.resources.security.IGroup#setSubgroups(List<CmsPath>)
   */
  public void setSubgroups(List<CmsPath> subgroups) {
    this.subgroups = subgroups;
  }
  
  /**
   * Method isMember.
   * @param userID String
   * @return boolean
   * @see gov.pnnl.cat.core.resources.security.IGroup#isMember(String)
   */
  public boolean isMember(String userID) {
    return members.contains(userID);
  }
  
  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return this.path.toString();
  }

}
