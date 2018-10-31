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

import gov.pnnl.cat.core.internal.resources.ResourceService;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.Group;
import gov.pnnl.cat.core.resources.security.IGroup;
import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.core.resources.security.Team;
import gov.pnnl.cat.core.resources.security.User;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.webservice.group.GroupDetails;
import gov.pnnl.cat.webservice.group.GroupQueryResults;
import gov.pnnl.cat.webservice.group.NewTeamDetails;
import gov.pnnl.cat.webservice.group.TeamDetails;
import gov.pnnl.cat.webservice.user.NewUserDetails;
import gov.pnnl.cat.webservice.user.UserDetails;
import gov.pnnl.cat.webservice.user.UserQueryResults;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.util.Constants;
import org.apache.log4j.Logger;

/**
 * This class consolidates all the remote security APIs into
 * a single client-side service.  This class controls
 * all remote security operations but does not do any caching.
 * @version $Revision: 1.0 $
 */
public class SecurityService {
  public final static int MAX_IMAGE_FILE_SIZE = 1024 * 1024 * 10;

  // logger
  protected Logger logger = CatLogger.getLogger(this.getClass());

  private ResourceService resourceService;

  /**
   * Constructor

   */
  public SecurityService() {  
  }

  /**
   * Method changePassword.
   * @param username String
   * @param currentPassword String
   * @param newPassword String
   * @throws ServerException
   */
  public void changePassword(String username, String currentPassword, String newPassword) {     
    resourceService.changePassword(username, currentPassword, newPassword);
  }

  /**
   * Create a CAT IGroup object from the GroupDetails returned from Alfresco
   * @param groupDetails
   * @return IGroup
   * @throws RemoteException * @throws ResourceException * @throws ParseException  */
  private IGroup createGroup(GroupDetails groupDetails)  {
    IGroup group;

    // we have a team
    if(groupDetails.getProperties() != null) {  
      Team team = new Team();

      // load the properties
      for (NamedValue property : groupDetails.getProperties()) {
        //home folder property could be null if someone deletes the home folder of a team, but the team is still there
        if(property.getValue() != null || property.getValues() != null){
          loadProperty(team, property);
        }
      }
      group = team;

    } else {
      group = new Group();      
    }
    // set the path
    CmsPath path = new CmsPath(groupDetails.getGroupPath());
    group.setPath(path);

    // add the members
    if(groupDetails.getMembers() != null) {
      for(String member : groupDetails.getMembers()) {
        group.addMember(member);
      }
    }

    // add the parent
    if(groupDetails.getParentPath() != null) {
      group.setParent(new CmsPath(groupDetails.getParentPath()));
    }

    // add the subgroups
    if(groupDetails.getSubGroupPaths() != null) {
      List<CmsPath> subgroups = new ArrayList<CmsPath>();
      for (String subgroup : groupDetails.getSubGroupPaths()) {
        subgroups.add(new CmsPath(subgroup));
      }
      group.setSubgroups(subgroups);
    }

    return group;
  }

  /**
   * Method createPersonProperties.
   * @param user IUser
   * @return NamedValue[]
   * @throws ResourceException
   */
  private NamedValue[] createPersonProperties(IUser user) throws ResourceException {
    // Create the new user objects
    return new NamedValue[] {
        new NamedValue(Constants.PROP_USER_FIRSTNAME, false, user.getFirstName(), null),
        new NamedValue(Constants.PROP_USER_LASTNAME, false, user.getLastName(), null),
        new NamedValue(Constants.PROP_USER_EMAIL, false, user.getEmail(), null),
        new NamedValue(VeloConstants.PROP_PRIMARY_PHONE, false, user.getPhoneNumber(), null)
    };
  }

  /**
   * Create a single team. We will only be creating one team at a time.
   * @param team
   */
  public ITeam createTeam(ITeam team) {

    NamedValue[] teamProps = createTeamProperties(team);
    List<String> members = team.getMembers();
    String[] membersStr = new String[members.size()];
    for(int i=0; i < members.size(); i++) {
      membersStr[i] = members.get(i);
    }
    NewTeamDetails newTeam = 
        new NewTeamDetails(team.getPath().toDisplayString(),membersStr, teamProps);
    NewTeamDetails[] newTeams = new NewTeamDetails[]{newTeam};

    // Create the new team

    try {
      GroupDetails[] groupDetails = resourceService.createGroups(newTeams);
      if (groupDetails == null) {
        throw new RuntimeException("New team could not be created.");
      }
      ITeam createdTeam = createTeam(groupDetails[0]);

      // All the new picture stuff is set on the team, not createdTeam, so we have to 
      // transfer some props to the team in order for create picture to work
      // (team must have name id set in order to save content to right node)
      ((Team)team).setProperty(VeloConstants.PROP_NAME,createdTeam.getName());
      ((Team)team).setProperty(VeloConstants.PROP_UUID,createdTeam.getProperty(VeloConstants.PROP_UUID));

      updatePictureContent(team);

      return createdTeam;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("New team could not be created.", e);
    }

  }

  /**
   * Method createTeam.
   * @param teamDetails TeamDetails
   * @return ITeam
   * @throws RemoteException
   * @throws ResourceException
   * @throws ParseException
   */
  private ITeam createTeam(GroupDetails teamDetails) {
    return (ITeam)createGroup(teamDetails);
  }

  /**
   * Method createTeamDetails.
   * @param team ITeam
   * @return TeamDetails
   * @throws ResourceException
   */
  private TeamDetails createTeamDetails(ITeam team) throws ResourceException {
    List<String> members =  team.getMembers();
    String[] membersStr = new String[members.size()];

    // get members
    for (int i = 0; i < members.size(); i++) {
      membersStr[i] = members.get(i);
    }

    // get parent path (ignored by server methods)
    String parentPath = null;
    if(team.getParent() != null) {
      parentPath = team.getParent().toDisplayString();
    }

    // get subgroups (ignored by server methods)
    List<CmsPath> subgroups = team.getSubgroups();
    String[] subgroupsStr = new String[subgroups.size()];
    for(int i = 0; i < subgroups.size(); i++) {
      subgroupsStr[i] = subgroups.get(i).toString();
    }

    return new TeamDetails(team.getPath().toDisplayString(), membersStr, subgroupsStr, parentPath,
        createTeamProperties(team));
  }

  /**
   * Method createTeamProperties.
   * @param team ITeam
   * @return NamedValue[]
   * @throws ResourceException
   */
  private NamedValue[] createTeamProperties(ITeam team) throws ResourceException {
    // fixed web service so it will not erase propertes that are not set,
    // so we don't have to set every one
    logger.debug("in createTeamProperties team description:" + team.getDescription());
    return new NamedValue[] {
        new NamedValue(VeloConstants.PROP_DESCRIPTION, false, 
            team.getDescription(), null),
            new NamedValue(VeloConstants.PROP_TEAM_EMAIL, false,
                team.getEmail(), null),
            new NamedValue(VeloConstants.PROP_TEAM_NAME, false,
                team.getPath().last().getName(), null),
            new NamedValue(VeloConstants.PROP_UUID, false,
                team.getProperty(VeloConstants.PROP_UUID), null)
    };
  }

  /**
   * Method createUser.
   * @param userDetails UserDetails
   * @return IUser
   * @throws RemoteException
   * @throws ResourceException
   * @throws ParseException
   */
  private IUser createUser(UserDetails userDetails) throws RemoteException, ResourceException, ParseException {
    User user = new User();
    if(logger.isDebugEnabled())
      logger.debug("adding user " + userDetails.getUserName());

    for (NamedValue property : userDetails.getProperties()) {
      if(logger.isDebugEnabled())
        logger.debug("loading property: " + property.getName() + "/" + property.getValue());
      loadProperty(user,property);
    }
    user.setAdmin(userDetails.isAdmin());

    return user;
  }  

  /**
   * Method createUserDetails.
   * @param users IUser[]
   * @return UserDetails[]
   * @throws ResourceException
   */
  private UserDetails[] createUserDetails(IUser... users) throws ResourceException {
    UserDetails[] details = new UserDetails[users.length];

    for (int i = 0; i < details.length; i++) {
      details[i] = new UserDetails(users[i].getUsername(), createUserProperties(users[i]), users[i].isAdmin());
    }

    return details;
  }

  /**
   * Method createUserProperties.
   * @param user IUser
   * @return NamedValue[]
   * @throws ResourceException
   */
  private NamedValue[] createUserProperties(IUser user) throws ResourceException {

    // Create the new user objects
    return new NamedValue[] {
        new NamedValue(VeloConstants.PROP_USER_EMAIL, false, user.getEmail(), null),
        new NamedValue(VeloConstants.PROP_USER_FIRSTNAME, false, user.getFirstName(), null),
        new NamedValue(VeloConstants.PROP_USER_LASTNAME, false, user.getLastName(), null),
        new NamedValue(VeloConstants.PROP_USERNAME, false, user.getUsername(), null),
        new NamedValue(VeloConstants.PROP_PRIMARY_PHONE, false, user.getPhoneNumber(), null)
    };
  }

  /**
   * Method createUsers.
   * @param users IUser[]
   * @return IUser[]
   * @throws CatSecurityException
   * @throws ServerException
   * @throws ResourceException
   */
  public IUser[] createUsers(IUser... users) {

    NewUserDetails[] newUsers = new NewUserDetails[users.length];
    for (int i = 0; i < newUsers.length; i++) {
      NamedValue[] userProps = createPersonProperties(users[i]);
      newUsers[i] = new NewUserDetails( users[i].getUsername(), users[i].getPassword(), userProps);
    } 
    // Create the new users
    try {
      UserDetails[] userDetails = resourceService.createUsers(newUsers);
      if (userDetails == null) {
        throw new RuntimeException("New user could not be created.");
      }
      IUser[] createdUsers = new IUser[userDetails.length];
      for (int i = 0; i < userDetails.length; i++) {
        createdUsers[i] = createUser(userDetails[i]);

        // updatePictureContent needs the user's ID in order to know where on the
        // server to save the picture file.
        // Since this user is just now being created, his ID is not available
        // in the User instance.
        // We have to set the ID explicitly because of this.
        ((User)users[i]).setProperty(VeloConstants.PROP_UUID, createdUsers[i].getID());
        updatePictureContent(users[i]);
      }
      return createdUsers;

    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("New user could not be created", e);
    }
  }

  /**
   * Deletes the picture property for the given profile.
   * @param profile The profile to update
   * @throws RemoteException */
  private void deletePicture(IProfilable profile) {
    //Resource resource = new Resource(profile.getID());  can't send the resource with just the picture pop set as null.  
    //When the json of the sparsely filled out resource was deserialized it no longer had 'null' for the profile image but 
    //instead had only the uuid *field* set on the resource and the properties hashmap only had one property set, 
    //cm:name which was set to null.
    Resource resource = resourceService.getResource(profile.getID());
    resource.setProperty(VeloConstants.PROP_PICTURE, (String)null);
    //have to set the name prop too otherwise null is sent and 
    resourceService.setProperties(resource);
  }

  /**
   * Method deleteTeam.
   * @param teamPaths CmsPath[]
   */
  public void deleteTeam(CmsPath... teamPaths) {
    String[] teams = new String[teamPaths.length];
    for(int i = 0; i < teamPaths.length; i++) {
      teams[i] = teamPaths[i].toDisplayString();
    }
    resourceService.deleteGroups(teams);
  }

  /**
   * Method deleteUser.
   * @param userNames String[]
   * @throws CatSecurityException
   * @throws ServerException
   */
  public void deleteUser(String... userNames) {
    resourceService.deleteUsers(userNames);
  }

  /**
   * Gets all teams and groups with bare bones info filled out.  
   * Group associations (i.e., subgroups, members,
   * and parents) are added by the SecurityManager as they require the cache.
   * @return IGroup[]
   * @throws ServerException
   * @throws ResourceException
   */
  public IGroup[] getGroups() {

    List<IGroup> groups = new ArrayList<IGroup>();

    GroupQueryResults results = resourceService.queryGroups(null);

    if(results != null && results.getGroupDetails() != null) {   
      boolean moreResultsAvailable = true;

      while (moreResultsAvailable) {

        for (GroupDetails details : results.getGroupDetails()) {
          groups.add(createGroup(details));
        }

        if (results.getQuerySession() == null) {
          moreResultsAvailable = false;
        } else {
          results = resourceService.fetchMoreGroups(results.getQuerySession());
        }
      }
    }

    if(logger.isDebugEnabled())
      logger.debug("number of groups found = " + groups.size());
    return groups.toArray(new IGroup[groups.size()]);
  }

  /**
   * Method getUser.
   * @param username String
   * @return IUser
   * @throws ServerException
   * @throws ResourceException
   */
  public IUser getUser(String username) {
    try {
      UserDetails details = resourceService.getUser(username);      
      return createUser(details);
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("Failed to get user: " + username, e);
    }
  }  

  /**
   * Method getUsers.
   * @return IUser[]
   * @throws ServerException
   * @throws ResourceException
   */
  public IUser[] getUsers() {
    List<IUser> users = new ArrayList<IUser>();
    if(logger.isDebugEnabled()) {
      logger.debug("Trying to load users");
    }
    try {
      UserQueryResults results = resourceService.queryUsers(null);

      boolean moreResultsAvailable = true;

      while (moreResultsAvailable) {
        if(logger.isDebugEnabled())
          logger.debug("Server query returned " + results.getUserDetails().length + " users ");

        for (UserDetails details : results.getUserDetails()) {
          users.add(createUser(details));
        }

        if (results.getQuerySession() == null) {
          moreResultsAvailable = false;
        } else {
          results = resourceService.fetchMoreUsers(results.getQuerySession());
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("Failed to look up users", e);
    }

    return users.toArray(new IUser[users.size()]);
  }

  /**
   * Checks if the property is a Profile property and loads it into the profile
   * @param profile
   * @param property
   */
  private void loadProperty(IProfilable profile, NamedValue property) {
    if(logger.isDebugEnabled())
      logger.debug("loadding property: " + property);


    String name = property.getName();
    boolean multivalued = false;

    // for some reason the System homeFolderProvider property is coming back with multivalue null!
    // TODO: figure out why this is happening
    if(property.getIsMultiValue() != null) {
      multivalued = property.getIsMultiValue();
    }
    if(!multivalued) {
      String value = property.getValue();
      profile.setProperty(name, value);

    } else {
      String[] values = property.getValues();
      profile.setProperty(name, values);
    }

  }

  /**
   * This is a placeholder for the login system.
   * 

   * @param name String
   * @param pwd String
   * @return boolean
   * @throws CatSecurityException
   * @throws ServerException
   */
  public boolean login(String name, String pwd) throws CatSecurityException, ServerException{

    resourceService.login(name, pwd);
    logger.info("Connected to alfresco as: " + name);

    return true;
  }

  public void logout(){
  }

  /**
   * Method savePictureContent.
   * @param stream InputStream
   * @param mimeType String
   * @param profile IProfilable
   */
  private void savePictureContent(File file, String mimetype, IProfilable profile) throws Exception {
    resourceService.updateContent(profile.getID(), file, VeloConstants.PROP_PICTURE, mimetype);

  }

  /**
   * Method setResourceService.
   * @param service ResourceService
   */
  public void setResourceService(ResourceService service) {
    this.resourceService = service;
  }

  /**
   * Method updatePictureContent.
   * @param profile IProfilable
   * @throws ResourceException
   * @throws IOException
   */
  private void updatePictureContent(IProfilable profile) throws Exception {

    if (profile.getNewPicture() == null) {
      // check if the previous picture has been deleted
      if (profile.isPictureDeleted()) {
        deletePicture(profile);
      } else {
        // nothing needs to be done.
        // they have not updated the user's picture in any way.
      }
    } else {
      // the user has a new picture that we have to save
      //      savePictureContent(profile.getNewPicture(), profile);
      savePictureContent(profile.getNewPicture(), profile.getPictureMimetype(), profile);
    }
  }

  /**
   * Method updateTeam.
   * @param team ITeam
   * @return ITeam
   */
  public ITeam updateTeam(ITeam team) {
    try {
      TeamDetails details = createTeamDetails(team);

      // update the profile
      GroupDetails[] results = resourceService.updateGroups(new TeamDetails[]{details});

      // update the picture content
      updatePictureContent(team);

      return createTeam(results[0]);
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("Failed to update team.", e);
    }

  }

  /**
   * Method updateUser.
   * @param users IUser[]
   * @return IUser[]
   * @throws ServerException
   * @throws ResourceException
   */
  public IUser[] updateUser(IUser... users) {
    try {
      UserDetails[] details = resourceService.updateUsers(createUserDetails(users));
      IUser[] returnedUsers = new IUser[details.length];

      for (int i = 0; i < details.length; i++) {
        returnedUsers[i] = createUser(details[i]);
        updatePictureContent(users[i]);
      }

      return returnedUsers;
    } catch (Exception e) {
      throw new RuntimeException("Failed to update users", e);
    }
  }
  
  public void setPermissions(ACL[] acls) {
    resourceService.setPermissions(acls);
  }
  
  /**
   * Method setPermissions.
   * @param acls ACL[]
   */
  public void setPermissions(ACL[] acls, boolean recursive) {
    resourceService.setPermissions(acls, recursive);
  }
  
  /**
   * @param path
   * @return
   */
  public ACL getPermissions(CmsPath path) {
    return resourceService.getPermissions(path);
  }

  public boolean hasPermissions(CmsPath path, String... permissions) {
    return resourceService.hasPermissions(path, permissions);
  }

}
