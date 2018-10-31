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


/**
 * In the GUI we need to use read only pointers to the teams that
 * are in the cache.  If we need to make changes to a team, we need
 * to perform a clone() operation so we can get a team object that is
 * not read only.  Then we can update the object and pass it to the 
 * security manager update method.
 * 
 * Teams are just groups with a team profile.
 * @version $Revision: 1.0 $
 */
public interface ITeam extends IGroup, IProfilable {
  
  public static final String TEAM_NAME_ALL_USERS = "All Users";

  /*-------------- Read Operations --------------------*/  
  
  /**
  
   * @return description of the team */
  public String getDescription();
  
  /**
   * Clone this team so you can make changes to it.  The clone is not a handle
   * and contains a copy of all its parameters.
   * @return ITeam
   * @see gov.pnnl.cat.core.resources.security.IGroup#clone()
   */
  public ITeam clone();
  
  /*-------------- Write Operations --------------------*/
  /**
   *  Use clone() or SecurityManager.getNewTeam() before calling
   *  these operations.
   * @param description String
   */  
  
  public void setDescription(String description);
  
}
