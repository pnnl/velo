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
 * In the GUI we need to use read only pointers to the users that
 * are in the cache.  If we need to make changes to a user, we need
 * to perform a clone() operation so we can get a user object that is
 * not read only.  Then we can update the object and pass it to the 
 * security manager update method.
 * 
 * @version $Revision: 1.0 $
 */
public interface IUser extends Cloneable, IProfilable {
  
  /*-------------- Read Operations --------------------*/
  /**
   * Method getUsername.
   * @return String
   */
  public String getUsername();
  /**
   * Method getFullName.
   * @return String
   */
  public String getFullName();
  /**
   * Method getLastNameFirstFullName.
   * @return String
   */
  public String getLastNameFirstFullName();
  /**
   * Method getFirstName.
   * @return String
   */
  public String getFirstName();
  /**
   * Method getLastName.
   * @return String
   */
  public String getLastName();
  /**
   * Method getPhoneNumber.
   * @return String
   */
  public String getPhoneNumber();

   
  /**
   * returns true if this is an Alfresco system admin
   * 
   * @return boolean
   */
  public boolean isAdmin();  
  
  /*-------------- Write Operations --------------------*/
  /**
   *  Use clone() or SecurityManager.getNewUser() before calling
   *  these operations.
   * @param password String
   */
  
  /**
   * If this value is not null, then change the password when
   * saving the user.  Otherwise, just ignore it.
   */
  public void setPassword(String password);
  /**
   * Method getPassword.
   * @return String
   */
  public String getPassword();
  
  //TODO: check on Alfresco String validation (I know they have regex pattern matches)
  /**
   * Method setFirstName.
   * @param firstName String
   */
  public void setFirstName(String firstName);
  /**
   * Method setLastName.
   * @param lastName String
   */
  public void setLastName(String lastName);
  /**
   * Method setPhoneNumber.
   * @param phoneNumber String
   */
  public void setPhoneNumber(String phoneNumber);

  /**
   * It looks like the only way to add system admins in Alfresco is to
   * add an entry to the authority-services-context.xml Spring config file.
   * (Specifically the adminUsers property of the authorityService bean.)
   * 
   *  So, admins will have to be added by hand and the server restarted.
   * @return IUser
   */
  //public void setIsAdmin(boolean admin);

  /**
   * Clone this user so you can make changes to it.  The clone is not a handle
   * and contains a copy of all its parameters.
   */
  public IUser clone();

}
