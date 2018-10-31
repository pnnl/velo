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
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.events.IGroupEventListener;
import gov.pnnl.cat.core.resources.events.IUserEventListener;
import gov.pnnl.velo.model.ACE;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public interface ISecurityManager {

  
  /**
   * Method login.
   * @param username String
   * @param Password String
   * @throws ServerException
   * @throws CatSecurityException
   */
  public void login(String username, String Password) throws ServerException, CatSecurityException;
  public void logout();
  
  /**
   * Reset caches
   * @throws ServerException
   * @throws ResourceException
   */
  public void resetCaches() throws ServerException, ResourceException;
  /**
   * Method resetUserCache.
   * @throws ServerException
   * @throws ResourceException
   */
  public void resetUserCache() throws ServerException, ResourceException;
  /**
   * Method resetGroupCache.
   * @throws ServerException
   * @throws ResourceException
   */
  public void resetGroupCache()  throws ServerException, ResourceException;
  
  /*-------------------------------- User Operations ---------------------------------*/
  
  // Use WebServiceFactory.getAdministrationService()to perform the user methods.
  
  /**
   * Return all the registered users.
   * Do not show the special Guest and Administrator users unless you are a system admin.
   * This may require filtering these out by hand, as I'm not sure what comes back from
   * Alfresco. 
   * 
   * Curt says not to show the Guest user ever.
   * @return IUser[]
   **/
  public IUser[] getUsers();

  /**
   * Method getUsers.
   * @param members List<String>
   * @return List<IUser>
   */
  public List<IUser> getUsers(List<String> members);
  
  /**
   * Look up an existing user. 
  
  
   * @param username String
   * @return IUser
   * @throws ServerException  * @throws ResourceException  */
  public IUser getUser(String username);

  /**
   * Looks up the currently logged-in user.
   * @return the user currently logged in. 
   **/
  public IUser getActiveUser();
  
  /**
   * Create a new user on the server.  We will need to convert the IUser properties to
   * a NewUserDetails object for the Alfresco AdministrationService.
   * Once create is complete, put user in the local cache.
  
  
  
  
   * @param user IUser[]
   * @return the user object that is in the cache * @throws CatSecurityException  * @throws ServerException  * @throws ResourceException  */
  public IUser[] createUser(IUser... user) throws CatSecurityException, ServerException, ResourceException;
  
  /**
   * Modify the properties for the given user.  We will need to convert the IUser
   * properties to a UserDetails object for the Alfresco AdministrationService
  
  
  
   * @param user IUser[]
   * @return the user object that is in the cache * @throws ServerException  * @throws ResourceException  */
  public IUser[] updateUser(IUser... user) throws ServerException, ResourceException;

  
  /**
   * Method changePassword.
   * @param userName String
   * @param currentPassword String
   * @param newPassword String
   * @throws ServerException
   */
  public void changePassword(String userName, String currentPassword, String newPassword) throws ServerException;
  
  /**
   * Method deleteUser.
   * @param userIDs String[]
   * @throws CatSecurityException
   * @throws ServerException
   */
  public void deleteUser(String... userIDs) throws CatSecurityException, ServerException;
  
  /**
   * Method addUserEventListener.
   * @param listener IUserEventListener
   */
  public void addUserEventListener(IUserEventListener listener);
  /**
   * Method removeUserEventListener.
   * @param listener IUserEventListener
   */
  public void removeUserEventListener(IUserEventListener listener);

  /**
   * Method addLoginListener.
   * @param listener ILoginListener
   */
  public void addLoginListener(ILoginListener listener);
  
  /**
   * Method removeLoginListener.
   * @param listener ILoginListener
   */
  public void removeLoginListener(ILoginListener listener);
  
  /**
   * Method addGroupEventListener.
   * @param listener IGroupEventListener
   */
  public void addGroupEventListener(IGroupEventListener listener);
  /**
   * Method removeGroupEventListener.
   * @param listener IGroupEventListener
   */
  public void removeGroupEventListener(IGroupEventListener listener);
  
  /**
   * Method addPasswordChangeListener.
   * @param listener IPasswordChangeListener
   */
  public void addPasswordChangeListener(IPasswordChangeListener listener);
  /**
   * Method removePasswordChangeListener.
   * @param listener IPasswordChangeListener
   */
  public void removePasswordChangeListener(IPasswordChangeListener listener);
  
  /**
   * Method getGroup.
   * @param groupName String
   * @return IGroup
   */
  public IGroup getGroup(String groupName);
  
  /*-------------------------------- Team Operations ---------------------------------*/
  
  // TBD - use custom Action calls that I have to write. Use WebServiceFactory.getActionService()
  // to call these actions when we are ready.
  
  /**
   * Get all groups/teams.  Only get teams for which the current user is a member.  Get all teams if this is
   * system admin.  When a team is loaded, it loads all metadata at once, including
   * the member list.  When teams are loaded, all parent/child links are
   * set appropriately.
   * 
   * Returned collection is unmodifiable.
   * @return Collection<ITeam>
   * @throws ServerException
   * @throws ResourceException
   */
  public Collection<ITeam> getTeams() throws ServerException, ResourceException;

  /**
   * Method getTeam.
   * @param teamPath CmsPath
   * @return ITeam
   * @throws ServerException
   * @throws ResourceException
   */
  public ITeam getTeam(CmsPath teamPath) throws ServerException, ResourceException ;
  /**
   * Method getUserTeams.
   * @param userId String
   * @return ArrayList<ITeam>
   * @throws ServerException
   * @throws ResourceException
   */
  public ArrayList<ITeam> getUserTeams(String userId) throws ServerException, ResourceException; 
  /**
   * Create a new team on the server.
   * Once create is complete, put team in the local cache.
   * 
   * Once team is created, parent team child lists will be updated.
  
   * @param team ITeam
   * @return the team object that is in the cache * @throws CatSecurityException
   * @throws ServerException
   * @throws ResourceException
   */
  public ITeam createTeam(ITeam team) throws CatSecurityException, ServerException, ResourceException;
  
  /**
   * When team is deleted, parent team child lists will be updated.
   * @param teamPaths CmsPath[]
   * @throws CatSecurityException
   * @throws ServerException
   */
  public void deleteTeam(CmsPath... teamPaths) throws CatSecurityException, ServerException ;
  
  /**
   * Modify the properties for the given team (including new members)
  
   * @param team ITeam
   * @return the team object that is in the cache * @throws ServerException
   * @throws ResourceException
   */
  public ITeam updateTeam(ITeam team) throws ServerException, ResourceException;
  
  /**
   * Method getUsername.
   * @return String
   */
  public String getUsername();

  /**
   * Method getPassword.
   * @return String
   */
  public String getPassword();
   
  /**
   * Set permissions (access control list, or ACL) for one or more
   * nodes.
   * @param acls
   */
  public void setPermissions(ACL[] acls);
  
  /**
   * Method setPermissions.
   * @param acls ACL[]
   * @param recursive - if true, these permissions will be recursively applied to the whole subtree
   */
  public void setPermissions(ACL[] acls, boolean recursive);
  

  /**
   * Get the access control list for the given resource.  This list contains a 
   * set of access control entries (ACEs).  To get the ACEs, call ACL.getAces().
   * Every time you map a user/group to a permission (i.e., grant carina read),
   * this is an ACE.  An ACL contains a whole bunch of ACEs representing all
   * the permissions for all the users. 
   * 
   * Use this method to display the access controls on the Security tab
   * in the properties dialog.
   * 
   * An ACL will always exist for every resource.
   *
   * @param resource CmsPath
   * @return a read only copy of the ACL that is in the cache */
  public ACL getPermissions(CmsPath path);
  
  /**
   * Does the current user have the given permissions on the given folder.
   * This takes into account any teams the user may be in.
   * @param path
   * @param user
   * @param permission
   * @return
   */
  public boolean hasPermissions(CmsPath path, String... permissions);
 
}
