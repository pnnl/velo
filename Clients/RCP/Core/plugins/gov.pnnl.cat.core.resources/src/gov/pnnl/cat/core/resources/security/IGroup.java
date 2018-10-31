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

import java.util.List;

/**
 * A group represents an Alfresco security group (i.e.,
 * authority).  All group members have the same role.
 * Subgroups are allowed.
 * @version $Revision: 1.0 $
 */
public interface IGroup extends Cloneable {

  
  /*-------------- Read Operations --------------------*/
  
  /**
  
   * @return the members of the group */
  public List<String> getMembers();
  
  /**
   * 
   * @param userID
  
   * @return true if the given user belongs to this group */
  public boolean isMember(String userID);

  /**
   * Use this to display the group's hierarchy.
   * E.g., /BKC/CAT/CAT Developers.
   * Team name will have slashes in it.
   * This is actually the unique team ID used
   * in Alfresco, since all teams must have unique
   * IDs.
   * 
  
   * @return An CmsPath representing the group's hierarchy.
   * This is NOT a real path to a node. */
  public CmsPath getPath();
  
  /**
   * This is the last segment in the group path, which is
   * the display name for the group (i.e., CAT Developers)
  
   * @return String
   */
  public String getName();
  
  /**
   * Gets the path for parent group
   * (This can be determined from the path.)
  
   * @return null if this is a top level group */
  public CmsPath getParent();
  
  /**
   * Gets the path for any child groups.
   * @return List<CmsPath>
   */
  public List<CmsPath> getSubgroups();
  
  
  /*-------------- Write Operations --------------------*/
  /**
   *  Use clone() or SecurityManager.getNewTeam() before calling
   *  these operations.
   * @return IGroup
   */  
  
  /**
   * Clone this group so you can make changes to it.  The clone is not a handle
   * and contains a copy of all its parameters.
   */
  public IGroup clone();
  
  /**
   * Method addMember.
   * @param userID String
   */
  public void addMember(String userID);
  
  /**
   * Method deleteMember.
   * @param userID String
   */
  public void deleteMember(String userID);  
    
  /**
   * Method setPath.
   * @param path CmsPath
   */
  public void setPath(CmsPath path);
  
  /**
   * Method setMembers.
   * @param members List<String>
   */
  public void setMembers(List<String> members);
  
  /**
   * Method setParent.
   * @param parent CmsPath
   */
  public void setParent(CmsPath parent);
  
  /**
   * Method setSubgroups.
   * @param subgroups List<CmsPath>
   */
  public void setSubgroups(List<CmsPath>subgroups);
}
