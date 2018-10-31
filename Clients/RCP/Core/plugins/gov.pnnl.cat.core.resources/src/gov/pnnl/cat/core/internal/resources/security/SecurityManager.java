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

import gov.pnnl.cat.core.internal.resources.ResourceManager;
import gov.pnnl.cat.core.internal.resources.events.NotificationManagerJMS;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IGroupEventListener;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.core.resources.events.IUserEventListener;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.IGroup;
import gov.pnnl.cat.core.resources.security.ILoginListener;
import gov.pnnl.cat.core.resources.security.IPasswordChangeListener;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;


/**
 * This class represents the object request broker (ORB)
 * for the remote security objects.  It is responsible for
 * caching these objects to minimize hits to the server.
 * It decorates the SecurityService with a cache.
 * @version $Revision: 1.0 $
 */
public class SecurityManager implements ISecurityManager, IResourceEventListener {

  public static final String USERS_PATH = "/sys:system/sys:people";
  public static final String TEAMS_PATH = "/sys:system/sys:teams";
  public static final String AUTHORITIES_PATH = "/sys:system/sys:authorities";
  
  private String username;
  private String password;
  
  private boolean successfulLogin;

  protected SecurityService securityService;
  protected IResourceManager resourceManager;

  // cache and locks
  protected List<IGroup> topLevelGroups;
  protected Map<CmsPath, IGroup> groups;
  protected Map<String, IUser> users;
  protected ReentrantReadWriteLock userLock;
  protected ReentrantReadWriteLock groupLock;

  // notification
  protected NotificationManagerJMS notificationManager;
  protected ArrayList<IUserEventListener> userListeners;
  protected ArrayList<IGroupEventListener> groupListeners;
  protected ArrayList<IPasswordChangeListener> passwordListeners;
  protected ArrayList<ILoginListener> loginListeners;
  
  // logger
  protected Logger logger = CatLogger.getLogger(this.getClass());

  public SecurityManager(){
    userLock = new ReentrantReadWriteLock();
    groupLock = new ReentrantReadWriteLock();   
    userListeners = new ArrayList<IUserEventListener>();
    groupListeners = new ArrayList<IGroupEventListener>();
    passwordListeners = new ArrayList<IPasswordChangeListener>();
    loginListeners = new ArrayList<ILoginListener>();
  }

  /**
   * Spring init method - called after all dependencies have
   * been injected.
   */
  public void init() {
	  notificationManager.addResourceEventListener(SecurityManager.this);
	  ((ResourceManager)resourceManager).afterLogin(); 
  }
  
  /**
   * @param resourceManager the resourceManager to set
   */
  public void setResourceManager(IResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  /**
   * Method changePassword.
   * @param userName String
   * @param currentPassword String
   * @param newPassword String
   * @throws ServerException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#changePassword(String, String, String)
   */
  public void changePassword(String userName, String currentPassword, String newPassword) throws ServerException {
    
    this.securityService.changePassword(userName, currentPassword, newPassword);
    if(this.username.equals(userName)) {
      this.password = newPassword;
      
      // also need to change the password for any listeners
      ArrayList<IPasswordChangeListener> listenersCopy;
      synchronized(this.passwordListeners){
        listenersCopy = new ArrayList<IPasswordChangeListener>(this.passwordListeners);
      }
      for(IPasswordChangeListener listener : listenersCopy) {
        listener.passwordChanged(currentPassword, newPassword);
      }
    }
  }

  /**
   * 
  
  
   * @throws ServerException  * @throws ResourceException  * @see gov.pnnl.cat.core.resources.security.ISecurityManager#resetCaches()
   */
  public void resetCaches() throws ServerException, ResourceException {
    resetUserCache();
    resetGroupCache();
  }

  /**
   * Method resetUserCache.
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#resetUserCache()
   */
  public void resetUserCache() throws ServerException, ResourceException {
    userLock.writeLock().lock();
    try {
      this.users = null;
      loadUsers();
    } finally {
      userLock.writeLock().unlock();
    }
  }

  /**
   * Method resetGroupCache.
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#resetGroupCache()
   */
  public void resetGroupCache() throws ServerException, ResourceException {
    groupLock.writeLock().lock();
    try {
      this.groups = null;  
      loadGroups();
    } finally {
      groupLock.writeLock().unlock();
    }
  }

  /**
   * 
  
  
   * @throws ServerException * @throws ResourceException */
  protected void checkGroupCache() throws ServerException, ResourceException {
    groupLock.readLock().lock();
    
    if (groups == null) {
      groupLock.readLock().unlock(); // must unlock first to obtain writelock
      groupLock.writeLock().lock();
      try {
        if (groups == null) { // recheck
          loadGroups();
        }
      } finally {
        groupLock.writeLock().unlock();
      }
    } else {
      groupLock.readLock().unlock();
    }
  }
  
  /**
   * 
  
  
   * @throws ServerException * @throws ResourceException */
  protected void checkUserCache() {

    userLock.readLock().lock();
    
    if (users == null) {
      userLock.readLock().unlock(); // must unlock first to obtain writelock
      userLock.writeLock().lock();
      try {
        if (users == null) { // recheck
          loadUsers();
        }
      } finally {
        userLock.writeLock().unlock();
      }
    } else {
      userLock.readLock().unlock();
    }
  }
  
  /**
   * Method createTeam.
   * @param team ITeam
   * @return ITeam
   * @throws CatSecurityException
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#createTeam(ITeam)
   */
  public ITeam createTeam(ITeam team) throws CatSecurityException, ServerException, ResourceException {
    checkGroupCache();

    ITeam result = this.securityService.createTeam(team);
//    groupLock.writeLock().lock();
//    try {
//      this.groups.put(result.getPath(), result);
//    } finally {
//      groupLock.writeLock().unlock();
//    }
    return new UnmodifiableTeam(result);
  }

  /**
   * Method createUser.
   * @param user IUser[]
   * @return IUser[]
   * @throws CatSecurityException
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#createUser(IUser[])
   */
  public IUser[] createUser(IUser... user) throws CatSecurityException, ServerException, ResourceException {
    checkUserCache();

    IUser[] results =  this.securityService.createUsers(user);
    IUser[] unmodifiableResults = new IUser[results.length];
//    userLock.writeLock().lock();
//    try {
//      for(int i = 0; i < results.length; i++) {
//        this.users.put(results[i].getUsername(), results[i]);
//        unmodifiableResults[i] = new UnmodifiableUser(results[i]);
//      }
//    } finally {
//      userLock.writeLock().unlock();
//    }
    return unmodifiableResults;
  }

  /**
   * Method deleteTeam.
   * @param teamPaths CmsPath[]
   * @throws CatSecurityException
   * @throws ServerException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#deleteTeam(CmsPath[])
   */
  public void deleteTeam(CmsPath... teamPaths) throws CatSecurityException, ServerException {
    this.securityService.deleteTeam(teamPaths);
    groupLock.writeLock().lock();
    try {
      for(CmsPath path : teamPaths) {
        this.groups.remove(path);
      }
    } finally {
      groupLock.writeLock().unlock();
    }
  }

  /**
   * Method deleteUser.
   * @param userIDs String[]
   * @throws CatSecurityException
   * @throws ServerException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#deleteUser(String[])
   */
  public void deleteUser(String... userIDs) throws CatSecurityException, ServerException {
    this.securityService.deleteUser(userIDs);
    userLock.writeLock().lock();
    try {
      for(String userID : userIDs) {
        this.users.remove(userID);
      }
    } finally {
      userLock.writeLock().unlock();
    }
  }

  /**
   * Method getActiveUser.
   * @return IUser
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getActiveUser()
   */
  public IUser getActiveUser() {
    return getUser(username);
  }

  /**
   * Method getTeams.
   * @return Collection<ITeam>
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getTeams()
   */
  public Collection<ITeam> getTeams() throws ServerException, ResourceException {
    checkGroupCache();

    ArrayList<ITeam> unmodifiableTeams = new ArrayList<ITeam>();
    groupLock.readLock().lock();
    try {
      for(IGroup group : topLevelGroups) {
        if (group instanceof ITeam) {
          unmodifiableTeams.add(new UnmodifiableTeam((ITeam)group));
        }
//        else {
//          unmodifiableGroups.add(new UnmodifiableGroup(group));
//        }
      }
    } finally {
      groupLock.readLock().unlock();
    }
    return unmodifiableTeams;
  }

  /**
   * Method getPassword.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getPassword()
   */
  public String getPassword(){
    return this.password;
  }

  //groupName could be /CAT/Developers
  // or if its an alfresco only group, could be Administrators, or Gang_Curator
  /**
   * Method getGroup.
   * @param groupName String
   * @return IGroup
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getGroup(String)
   */
  public IGroup getGroup(String groupName){
    if ( groups == null ) {
      try {
        resetGroupCache();
      } catch (ServerException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ResourceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    IGroup group = groups.get(new CmsPath(groupName));
    if(group != null){
      return new UnmodifiableGroup(group);
    }else{
      return null;
    }
  }
  
  
  /**
   * Method getTeam.
   * @param teamPath CmsPath
   * @return ITeam
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getTeam(CmsPath)
   */
  public ITeam getTeam(CmsPath teamPath) throws ServerException, ResourceException {
    checkGroupCache();
    groupLock.readLock().lock();
    IGroup group = null;
    try {
      group = groups.get(teamPath);
    } finally {
      groupLock.readLock().unlock();
    }
    ITeam ret = null;
    if (group != null) {
      if (!(group instanceof ITeam)) {
        throw new ResourceException("Group: " + teamPath + " has no team profile!");
      }
      ret = new UnmodifiableTeam((ITeam)group);
    }
    return ret;
  }
  
  /**
   * Method getUserTeams.
   * @param userId String
   * @return ArrayList<ITeam>
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getUserTeams(String)
   */
  public ArrayList<ITeam> getUserTeams(String userId) throws ServerException, ResourceException {
    checkGroupCache();
    ArrayList<ITeam> ret = new ArrayList<ITeam>();
    groupLock.readLock().lock();
    try {
      for(IGroup group : groups.values()) {
        if(group.isMember(userId)){
          if ((group instanceof ITeam)) {
            ret.add(new UnmodifiableTeam((ITeam)group));
          }
        }
      }
    } finally {
      groupLock.readLock().unlock();
    }
    
    return ret;
  }

//public Collection<IUser> getUsers() throws ServerException, ResourceException {

//if(this.users == null) {
//loadUsers();
//}    
//return Collections.unmodifiableCollection(users.values());    
//}

  /**
   * Method getUser.
   * @param username String
   * @return IUser
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getUser(String)
   */
  public IUser getUser(String username) {
    checkUserCache();

    userLock.readLock().lock();
    IUser ret = null;
    try {
      ret = users.get(username.toLowerCase());
    } finally {
      userLock.readLock().unlock();
    }
    if(ret != null) {
      ret = new UnmodifiableUser(ret);
    }
    return ret;
  }

  /**
   * Method getUsers.
   * @param members List<String>
   * @return List<IUser>
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getUsers(List<String>)
   */
  public List<IUser> getUsers(List<String> members)
  {
    List<IUser> users = new ArrayList<IUser>();
    
    for(String member:members)
    {
        users.add(getUser(member));
    }
    
    return users;
  }
  

  /**
   * Method getUsername.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getUsername()
   */
  public String getUsername(){
    return this.username;
  }

  /**
   * Method getUsers.
   * @return IUser[]
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#getUsers()
   */
  public IUser[] getUsers() {
    checkUserCache();

    userLock.readLock().lock();
    IUser[] results = null;
    try {
      Collection<IUser> values = users.values();
      results = new IUser[values.size()];
      int i = 0;
      for(IUser user : values) {
        results[i] = new UnmodifiableUser(user);
        i++;
      }
    } finally {
      userLock.readLock().unlock();
    }
    return results;
  }

  /**
   * Method loadGroups.
   * @throws ServerException
   * @throws ResourceException
   */
  protected void loadGroups() throws ServerException, ResourceException {
    groupLock.writeLock().lock();
    try {
      this.groups = new HashMap<CmsPath, IGroup>();
      IGroup[] loadedGroups = this.securityService.getGroups();
      List<CmsPath> subGroups = new ArrayList<CmsPath>();
      
      for (int i = 0; i < loadedGroups.length; i++) {
        groups.put(loadedGroups[i].getPath(), loadedGroups[i]);
        for(CmsPath path : loadedGroups[i].getSubgroups()) {
          subGroups.add(path);
        }
      }
      
      // Only show top-level teams
      topLevelGroups = new ArrayList<IGroup>();
      for(CmsPath path : groups.keySet()) {
        if(!subGroups.contains(path)) {
          topLevelGroups.add(groups.get(path));
        }
      }

    } catch (Throwable e) {
      logger.error("Failed to load groups.", e);
      throw e;
      
    } finally {
      groupLock.writeLock().unlock();
    }
    
  }

  /**
   * Method loadUsers.
   * @throws ServerException
   * @throws ResourceException
   */
  protected void loadUsers() {
    userLock.writeLock().lock();
    try {
      this.users = new HashMap<String, IUser>();
      IUser[] loadedUsers = this.securityService.getUsers();
      for (int i = 0; i < loadedUsers.length; i++) {
        users.put(loadedUsers[i].getUsername().toLowerCase(), loadedUsers[i]);
      }
    } finally {
      userLock.writeLock().unlock();
    }
  }

  /**
   * Method login.
   * @param username String
   * @param password String
   * @throws CatSecurityException
   * @throws ServerException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#login(String, String)
   */
  public void login(String username, String password) throws CatSecurityException, ServerException {
    this.username = username;
    this.password = password;  //TODO: we shouldn't (should we??) store this...
    this.successfulLogin = this.securityService.login(username, password);
    
    if(successfulLogin){
      // let listeners know
      ArrayList<ILoginListener> listenersCopy;
      synchronized(this.loginListeners){
        listenersCopy = new ArrayList<ILoginListener>(this.loginListeners);
      }
      for(ILoginListener listener : listenersCopy) {
        listener.userLoggedIn(username);
      }
    }
  }

  /**
   * Method logout.
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#logout()
   */
  public void logout() {
    // Shouldn't try logout if user canceled the login
    if (successfulLogin) {
      this.securityService.logout();
    }
  }

  /**
   * Method setSecurityService.
   * @param mgr SecurityService
   */
  public void setSecurityService(SecurityService mgr) {
    this.securityService = mgr;
  }

  /**
   * Method setNotificationManager.
   * @param notificationMgr NotificationManagerJMS
   */
  public void setNotificationManager(NotificationManagerJMS notificationMgr) {
    this.notificationManager = notificationMgr;
  }

  /**
   * Method updateTeam.
   * @param team ITeam
   * @return ITeam
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#updateTeam(ITeam)
   */
  public ITeam updateTeam(ITeam team) throws ServerException, ResourceException {
    checkGroupCache();
    ITeam result = this.securityService.updateTeam(team);

//    groupLock.writeLock().lock();
//    try {
//      this.groups.put(result.getPath(), result);
//    } finally {
//      groupLock.writeLock().unlock();
//    }
    return new UnmodifiableTeam(result);
  }

  /**
   * Method updateUser.
   * @param user IUser[]
   * @return IUser[]
   * @throws ServerException
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#updateUser(IUser[])
   */
  public IUser[] updateUser(IUser... user) throws ServerException, ResourceException {
    checkUserCache();
    IUser[] updatedUsers = this.securityService.updateUser(user);
    IUser[] unmodifiableUsers = new IUser[updatedUsers.length];

//    userLock.writeLock().lock();
//    try {
//      for(int i = 0; i < updatedUsers.length; i++) {
//        this.users.put(updatedUsers[i].getUsername(), updatedUsers[i]);
//        unmodifiableUsers[i] = new UnmodifiableUser(updatedUsers[i]);
//      }
//    } finally {
//      userLock.writeLock().unlock();
//    }
    return unmodifiableUsers;
  }

  /**
   * Method addUserEventListener.
   * @param listener IUserEventListener
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#addUserEventListener(IUserEventListener)
   */
  public void addUserEventListener(IUserEventListener listener) {
    synchronized(this.userListeners) {
      // replace if it already exists
      this.userListeners.remove(listener);
      this.userListeners.add(listener);
    } 
  }

  /**
   * Method addGroupEventListener.
   * @param listener IGroupEventListener
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#addGroupEventListener(IGroupEventListener)
   */
  public void addGroupEventListener(IGroupEventListener listener) {
    synchronized(this.groupListeners) {
      // replace if it already exists
      this.groupListeners.remove(listener);
      this.groupListeners.add(listener);
    } 
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#addPasswordChangeListener(gov.pnnl.cat.core.resources.security.IPasswordChangeListener)
   */
  @Override
  public void addPasswordChangeListener(IPasswordChangeListener listener) {
    synchronized(this.passwordListeners) {
      // replace if it already exists
      this.passwordListeners.remove(listener);
      this.passwordListeners.add(listener);
    }  
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#addLoginListener(gov.pnnl.cat.core.resources.security.ILoginListener)
   */
  @Override
  public void addLoginListener(ILoginListener listener) {
    synchronized(this.loginListeners) {
      // replace if it already exists
      this.loginListeners.remove(listener);
      this.loginListeners.add(listener);
    }  
  }

  /**
   * Method removeUserEventListener.
   * @param listener IUserEventListener
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#removeUserEventListener(IUserEventListener)
   */
  public void removeUserEventListener(IUserEventListener listener) {
    synchronized (this.userListeners) {
      this.userListeners.remove(listener);
    }   
  }
  
  /**
   * Method removeGroupEventListener.
   * @param listener IGroupEventListener
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#removeGroupEventListener(IGroupEventListener)
   */
  public void removeGroupEventListener(IGroupEventListener listener) {
    synchronized (this.groupListeners) {
      this.groupListeners.remove(listener);
    }   
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#removePasswordChangeListener(gov.pnnl.cat.core.resources.security.IPasswordChangeListener)
   */
  @Override
  public void removePasswordChangeListener(IPasswordChangeListener listener) {
    synchronized (this.passwordListeners) {
      this.passwordListeners.remove(listener);
    }   
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.security.ISecurityManager#removeLoginListener(gov.pnnl.cat.core.resources.security.ILoginListener)
   */
  @Override
  public void removeLoginListener(ILoginListener listener) {
    synchronized (this.loginListeners) {
      this.loginListeners.remove(listener);
    }   
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }

  /**
   * If the events are in the /system/people or /system/teams
   * area, then reset the cache and pass the events on to the UI
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  public void onEvent(IBatchNotification events) {
     
    // iterate through the events, seeing if a user or team has
    // been changed
    List<IResourceEvent> userEvents = new ArrayList<IResourceEvent>();
    List<IResourceEvent> teamEvents = new ArrayList<IResourceEvent>();
    Iterator<IResourceEvent> it = events.getNonRedundantEvents();
    IResourceEvent event;

    while(it.hasNext()) {
      event = it.next();
      String prefixPath = event.getPath().toPrefixString();

      if (prefixPath.startsWith(SecurityManager.USERS_PATH)) {
        userEvents.add(event);

      } else if (prefixPath.startsWith(SecurityManager.TEAMS_PATH)) {
        teamEvents.add(event);
      
      } else if(prefixPath.startsWith(SecurityManager.AUTHORITIES_PATH)) {
        teamEvents.add(event);
      }
    }

    if(userEvents.size() > 0) {
      try {
        resetUserCache();
      } catch (Exception e) {
        logger.error("Failed to reload user cache!", e);
      }
      notifyUserListeners(userEvents);
    }
    
    if(teamEvents.size() > 0) {
      try {
        resetGroupCache();
      } catch (Exception e) {
        logger.error("Failed to reload group cache!", e);
      }
      notifyGroupListeners(teamEvents);
    }

  }

  /**
   * Method notifyUserListeners.
   * @param userEvents List<IResourceEvent>
   */
  protected void notifyUserListeners(List<IResourceEvent> userEvents) {

    ArrayList<IUserEventListener> listenersCopy;
    synchronized(this.userListeners){
      listenersCopy = new ArrayList<IUserEventListener>(this.userListeners);
    }
    for(IUserEventListener listener : listenersCopy) {
      listener.onEvent(userEvents);
    }
  }   
  
  /**
   * Method notifyGroupListeners.
   * @param groupEvents List<IResourceEvent>
   */
  protected void notifyGroupListeners(List<IResourceEvent> groupEvents) {

    ArrayList<IGroupEventListener> listenersCopy;
    synchronized(this.groupListeners){
      listenersCopy = new ArrayList<IGroupEventListener>(this.groupListeners);
    }
    for(IGroupEventListener listener : listenersCopy) {
      listener.onEvent(groupEvents);
    }
  }

  @Override
  public void setPermissions(ACL[] acls) {
    securityService.setPermissions(acls);    
  }

  @Override
  public void setPermissions(ACL[] acls, boolean recursive) {
    securityService.setPermissions(acls, recursive);    
  }

  @Override
  public ACL getPermissions(CmsPath path) {
    return securityService.getPermissions(path);
  }

  @Override
  public boolean hasPermissions(CmsPath path, String... permissions) {
    return securityService.hasPermissions(path, permissions);
  }

}

