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
/**
 * Notice: This computer software was prepared by Battelle Memorial Institute,
 * hereinafter the Contractor for the Department of Homeland Security under the
 * terms and conditions of the U.S. Department of Energy's Operating Contract
 * DE-AC06-76RLO with Battelle Memorial Institute, Pacific Northwest Division.
 * All rights in the computer software are reserved by DOE on behalf of the
 * United States Government and the Contractor as provided in the Contract. You
 * are authorized to use this computer software for Governmental purposes but it
 * is not to be released or distributed to the public. NEITHER THE GOVERNMENT
 * NOR THE CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this sentence
 * must appear on any copies of this computer software.
 */
package gov.pnnl.cat.server.webservice.group;

import gov.pnnl.cat.server.webservice.util.ExceptionUtils;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractQuery;
import org.alfresco.repo.webservice.AbstractQuerySession;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web service to remotely manage groups and teams.  Design
 * of this service was based on Alfresco's AdministrationWebService.
 * @version $Revision: 1.0 $
 */
public class GroupWebService extends AbstractWebService implements GroupServiceSoapPort
{
  /** Log */
  private static Log logger = LogFactory.getLog(GroupWebService.class);

  /** The person service */
  @SuppressWarnings("unused")
  private PersonService personService = null;

  /** The authentication service */
  @SuppressWarnings("unused")
  private AuthenticationService authenticationService = null;

  /** The authentication component */
  private AuthenticationComponent authenticationComponent = null;

  /** The transaction service */
  private TransactionService transactionService = null;

  /** The authority service */
  private AuthorityService authorityService = null;

  /** The copy service */
  protected CopyService copyService;

  /** PermissionService bean reference */
  protected PermissionService permissionService;

  /** OwnableService bean reference */
  protected OwnableService ownableService;

  /** A set of ignored properties */
  private static Set<QName> ignoredProperties = new HashSet<QName>(3);

  /** Node Utils */
  protected NodeUtils nodeUtils;

  /**
   * Constructor
   */
  public GroupWebService()
  {
    // Set properties to ignore
    GroupWebService.ignoredProperties.add(ContentModel.PROP_STORE_PROTOCOL);
    GroupWebService.ignoredProperties.add(ContentModel.PROP_STORE_IDENTIFIER);
    GroupWebService.ignoredProperties.add(ContentModel.PROP_NODE_DBID);
    GroupWebService.ignoredProperties.add(ApplicationModel.PROP_ICON);
  }

  /**
   * Set the transaction service
   * 
   * @param transactionService    the transaction service
   */
  public void setTransactionService(TransactionService transactionService)
  {
    this.transactionService = transactionService;
  }

  /**
   * Set the person service
   * 
   * @param personService     sets the person service
   */
  public void setPersonService(PersonService personService)
  {
    this.personService = personService;
  }

  /**
   * Method setCopyService.
   * @param copyService CopyService
   */
  public void setCopyService(CopyService copyService)
  {
    this.copyService = copyService;
  }

  /**
   * Set the authentication service
   * 
   * @param authenticationService     the authentication service
   */
  public void setAuthenticationService(AuthenticationService authenticationService)
  {
    this.authenticationService = authenticationService;
  }

  /**
   * Set the authority service
   * 
   * @param authorityService     the authority service
   */
  public void setAuthorityService(AuthorityService authorityService)
  {
    this.authorityService = authorityService;
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * @param permissionService      The PermissionService to set.
   */
  public void setPermissionService(PermissionService permissionService)
  {
    this.permissionService = permissionService;
  }

  /**
   * Method setAuthenticationComponent.
   * @param authenticationComponent AuthenticationComponent
   */
  public void setAuthenticationComponent(
      AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }

  /**
   * Method setOwnableService.
   * @param ownableService OwnableService
   */
  public void setOwnableService(OwnableService ownableService) {
    this.ownableService = ownableService;
  }


  /**
   * No filtering is done at this time, so the filter param is not used.
   * 
   * @see gov.pnnl.cat.server.webservice.group.GroupServiceSoapPort#queryGroups(gov.pnnl.cat.server.webservice.group.GroupFilter)
   */
  public GroupQueryResults queryGroups(final GroupFilter filter)
  throws RemoteException, AdministrationFault
  {
    try {
      RetryingTransactionCallback<GroupQueryResults> callback = new RetryingTransactionCallback<GroupQueryResults>()
      {
        public GroupQueryResults execute() throws Throwable {
          return queryGroupsImpl(filter);
        }      
      };
      return transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);

    } catch (Throwable exception) {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new AdministrationFault(0, rootCause.toString());
    }
  }

  /**
   * Query groups, batch by set size
   * 
   * @param filter    used to filter results
  
   * @return          group query results, optionally batched */
  public GroupQueryResults queryGroupsImpl(GroupFilter filter)
  {
    MessageContext msgContext = MessageContext.getCurrentContext();
    
    // Create the query (ignoring the filter for now)
    GroupQuery query = new GroupQuery();
    
    // Create a user query session
    GroupQuerySession groupQuerySession = new GroupQuerySession(Long.MAX_VALUE, Utils.getBatchSize(msgContext), query);
    
    // Get the next batch of results
    GroupQueryResults userQueryResults = groupQuerySession.getNextResults(serviceRegistry);

    String querySessionId = groupQuerySession.getId();
    // add the session to the cache if there are more results to come
    boolean haveMoreResults = groupQuerySession.haveMoreResults();
    if (haveMoreResults)
    {
        querySessionCache.put(querySessionId, groupQuerySession);
    }
    
    // Construct the return value
    // TODO: http://issues.alfresco.com/browse/AR-1689
    // This looks odd, but I've chosen to be specific about when the ID is set on the return
    // results and when it isn't.
    GroupQueryResults result = new GroupQueryResults(
            haveMoreResults ? querySessionId : null,
                    userQueryResults.getGroupDetails());
    
    // Done
    return result;
  }    

  /**
   *  @see gov.pnnl.cat.server.webservice.group.GroupServiceSoapPort#fetchMoreGroups(java.lang.String)
   */
  public GroupQueryResults fetchMoreGroups(final String querySession)
  throws RemoteException, AdministrationFault
  {
    try {
      RetryingTransactionCallback<GroupQueryResults> callback = new RetryingTransactionCallback<GroupQueryResults>()
      {
        public GroupQueryResults execute() throws Throwable {
          return fetchMoreGroupsImpl(querySession);
        }      
      };
      return transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);

    } catch (Throwable exception) {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new AdministrationFault(0, rootCause.toString());
    }
  }

  /**
   * 
  
  
   * @param querySessionId String
   * @return GroupQueryResults
   * @throws AdministrationFault
   */
  public GroupQueryResults fetchMoreGroupsImpl(String querySessionId) throws AdministrationFault
  {
    GroupQuerySession session = null;
    try
    {
        session = (GroupQuerySession) querySessionCache.get(querySessionId);
    }
    catch (ClassCastException e)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Query session was not generated by the GroupWebService: " + querySessionId);
        }
        throw new AdministrationFault(
                4,
                "querySession with id '" + querySessionId + "' is invalid");
    }
    
    GroupQueryResults queryResult = null;
    if (session != null)
    {
        queryResult = session.getNextResults(serviceRegistry);
        if (!session.haveMoreResults())
        {
            this.querySessionCache.remove(querySessionId);
        }
        else
        {
            // Update the cache instance so that it can trigger replication as required
            querySessionCache.put(querySessionId, session);
        }
    }
    
    return queryResult;

  }

  /**
   * @see gov.pnnl.cat.server.webservice.group.GroupServiceSoapPort#getGroup(java.lang.String)
   */
  public GroupDetails getGroup(final String groupName) throws RemoteException, AdministrationFault
  {
    try {
      RetryingTransactionCallback<GroupDetails> callback = new RetryingTransactionCallback<GroupDetails>()
      {
        public GroupDetails execute() throws Throwable {
          return getGroupImpl(groupName);
        }      
      };
      return transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);

    } catch (Throwable exception) {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new AdministrationFault(0, rootCause.toString());
    }    
  }

  /**
   * Get the group details
   * 
   * @param groupName              the group name
  
  
  
   * @return                      the group details object * @throws AdministrationFault * @throws RemoteException */
  public GroupDetails getGroupImpl(String groupName)
  {
    GroupDetails groupDetails = null;
    String groupPath = new Path(groupName).toString();
    String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, groupPath);
    
    Set<String> modifiableGroups = getUserGroups();
    
    if(modifiableGroups.contains(groupAuthority)) {
      if (this.authorityService.authorityExists(groupAuthority)) {
        groupDetails = createGroupDetails(groupPath, modifiableGroups);

      } else {
        // Throw an exception to indicate that the group does not exist
        throw new RuntimeException(MessageFormat.format("The group with name {0} does not exist.", new Object[]{groupName}));
      }
    } 

    return groupDetails;
  }

  /**
   * Given a valid group name, will create appropriate details for corresponding
   * group or team
   * 
  
  
   * @param unformattedGroupPath String
   * @param modifiableGroups Set<String>
   * @return          the group details object populated with the appropriate property values */
  private GroupDetails createGroupDetails(String unformattedGroupPath, Set<String> modifiableGroups)
  {
    GroupDetails groupDetails;
    String groupPath = unformattedGroupPath;
    if(unformattedGroupPath.startsWith("/")) {
      groupPath = new Path(unformattedGroupPath).toString();
    }
    
    // Get the group authority
    String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, groupPath);

    // Find the corresponding profile (if it exists)
    // Team container doesn't have a cm:name property, so we much search via XPath, which searches on
    // the association names, not the cm:name properties
    NodeRef profile = 
      nodeUtils.getNodeByXPath(CatConstants.XPATH_TEAM_CONTAINER + "/cm:" + ISO9075.encode(groupPath));

    // no team profile - this is just a group
    if (profile == null) {
      groupDetails = new GroupDetails();


    } else { // this is a team
      groupDetails = new TeamDetails();

      // Set the various team profile property values
      Map<QName, Serializable> properties = this.nodeService.getProperties(profile);
      List<NamedValue> namedValues = new ArrayList<NamedValue>(properties.size());
      for (Map.Entry<QName, Serializable> entry : properties.entrySet()) {
        if (GroupWebService.ignoredProperties.contains(entry.getKey()) == false) {
          String value = null;
          try {
            // CAT Change - write the home folder path instead so we don't have to hit the server
            if(entry.getKey().equals(CatConstants.PROP_TEAM_HOME_FOLDER)) {
              // write the value as the path instead
              NodeRef homefolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, entry.getValue());
              String path = nodeService.getPath(homefolder).toString();
              value = path;
            } else {
              value = DefaultTypeConverter.INSTANCE.convert(String.class, entry.getValue());
            }

          } catch (Throwable exception) {
            if(entry.getValue() != null) {
              value = entry.getValue().toString();
            } else {
              value = "";
            }
          } 
          NamedValue namedValue = new NamedValue();
          namedValue.setName(entry.getKey().toString());
          namedValue.setIsMultiValue(false);
          namedValue.setValue(value);
          namedValues.add(namedValue);
        }
      }
      ((TeamDetails)groupDetails).setProperties((NamedValue[])namedValues.toArray(new NamedValue[namedValues.size()]));    
    }

    // Set the group name
    groupDetails.setGroupPath(groupPath);

    // Get the members
    Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, groupAuthority, true);
    String[] groupMembers = new String[members.size()];
    int index = 0;

    for(String member : members) {
      groupMembers[index] = authorityService.getShortName(member);
      index++;
    }
    groupDetails.setMembers(groupMembers);

    // Get the subgroups
    Set<String> subgroups = authorityService.getContainedAuthorities(AuthorityType.GROUP, groupAuthority, true);
    List<String> filteredSubgroups = new ArrayList<String>();
    
    for(String subgroup : subgroups) {
      if(modifiableGroups.contains(subgroup)) {
        filteredSubgroups.add(subgroup);
      }
    }
    
    String[] subGroupPaths = new String[filteredSubgroups.size()];
    index = 0;
    for (String subgroup : filteredSubgroups) {
      subGroupPaths[index] = authorityService.getShortName(subgroup);
      index++;
    }
    groupDetails.setSubGroupPaths(subGroupPaths);
    
    // Get the parent group (should only be 1)
    Set<String> parents = authorityService.getContainingAuthorities(AuthorityType.GROUP, groupAuthority, true);
    if(parents.size() == 0) {
      logger.debug("No parent found for " + groupAuthority);
    
    } else if (parents.size() == 1) {
      groupDetails.setParentPath(authorityService.getShortName(parents.iterator().next()));
      
    } else {
      logger.warn("More than one parent found for group: " + groupAuthority);
    }
    
    return groupDetails;            
  }

  /**
   * @see gov.pnnl.cat.server.webservice.group.GroupServiceSoapPort#createGroups(gov.pnnl.cat.server.webservice.group.NewGroupDetails[])
   */
  public GroupDetails[] createGroups(final NewGroupDetails[] newGroups) throws RemoteException, AdministrationFault
  {
    try {
      RetryingTransactionCallback<GroupDetails[]> callback = new RetryingTransactionCallback<GroupDetails[]>()
      {
        public GroupDetails[] execute() throws Throwable {
          return createGroupsImpl(newGroups);
        }      
      };
      return transactionService.getRetryingTransactionHelper().doInTransaction(callback);

    } catch (Throwable exception) {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new AdministrationFault(0, rootCause.toString());
    }    
  }

  /**
   * Create the new groups.  Could be creating groups or
   * teams, depending upon the details objects passed in.
   * 
   * For now, let anyone create new groups!
   * 
   * @param newGroups          the new groups/teams detail
  
  
  
   * @return                   the details of the created groups/teams * @throws Exception
   * @throws AdministrationFault * @throws RemoteException */
  public GroupDetails[] createGroupsImpl(NewGroupDetails[] newGroups) throws Exception
  {
    String currentUser = authenticationComponent.getCurrentUserName();
    boolean isAdmin = authorityService.hasAdminAuthority();
    GroupDetails[] groupDetails = new GroupDetails[newGroups.length];
    Set<String> modifiableGroups = getUserGroups();
    
    AuthenticationUtil.setRunAsUserSystem();
    
    int index = 0;
    logger.debug("calling create groups for " + newGroups.length + " groups");
    for (NewGroupDetails newGroup : newGroups)
    {    
      String pathStr = newGroup.getGroupPath();
      if (pathStr.equals("/") || !pathStr.startsWith("/")) {
        throw new AdministrationFault(1, "Invalid group path.");
      }

      Path path = new Path(pathStr);
      String groupName = path.toString();
      String parentName = path.removeLastSegments(1).toString();

      // create new Group using Authentication Service
      String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, groupName);
      String parentAuthority = null;
      if(!parentName.equals("/")) {
        parentAuthority = this.authorityService.getName(AuthorityType.GROUP, parentName);
      }

      logger.debug("Trying to create group authority: " + groupAuthority);
      logger.debug("Parent authority = " + parentAuthority);

      // Can only create group if you have permissions on the parent
      if (parentAuthority != null && !modifiableGroups.contains(parentAuthority)) {
        throw new AdministrationFault(0, "Access denied. " + currentUser +
            " cannot add child group to: " + parentAuthority);
      }
      
      // Note that this.authorityService.authorityExists(groupAuthority), was returning true if the
      // authority had previously been deleted.  This is because it was getting the authority from the AuthorityDAO, which 
      // pulls it from the transactional cache.  It looks like the authority key was not getting properly removed from the 
      // cache.  This could be because we are no longer deleting the home folder when the team is deleted, and the home folder
      // still has permissions to a group that no longer exists?  In any case, we are getting all authorities
      // in order to check for existence.
      Set<String> authorities = this.authorityService.getAllAuthorities(AuthorityType.GROUP);
      if ( !authorities.contains(groupAuthority) ) {

        this.authorityService.createAuthority(AuthorityType.GROUP, groupName);
        if (parentAuthority != null)
        {
          this.authorityService.addAuthority(parentAuthority, groupAuthority);
        }

      } else {
        throw new AdministrationFault(0, "Group " + groupName + " already exists!");
      }

      // add members
      if (newGroup.getMembers() != null) {
        for(String memberID : newGroup.getMembers()) {
          String userAuthority = this.authorityService.getName(AuthorityType.USER, memberID);
          logger.debug("Trying to add user authority: " + userAuthority);
          this.authorityService.addAuthority(groupAuthority, userAuthority);  
        }
      }
      // If the current user is NOT an admin, make him a member so he can edit his group
      if(!isAdmin) {
        String userAuthority = this.authorityService.getName(AuthorityType.USER, currentUser);
        Set<String> members = authorityService.getAuthoritiesForUser(currentUser);
        if(!members.contains(groupAuthority)){
          this.authorityService.addAuthority(groupAuthority, userAuthority);
        }
      }
      
      // See if we are creating a new team profile
      if (newGroup.getProperties() != null) {
        createTeamProfile(newGroup, currentUser);
      }

      // Add the details to the result
      groupDetails[index] = createGroupDetails(groupName, modifiableGroups);
      index++;
    }
    AuthenticationUtil.setRunAsUser(currentUser);
    
    return groupDetails;
  }

  /**
   * Method createTeamProfile.
   * @param newTeam NewTeamDetails
   */
  private void createTeamProfile(NewGroupDetails newTeam, String creatorID) {
    Path path = new Path(newTeam.getGroupPath());
    String groupPath = path.toString();
    String groupShortName = path.lastSegment();

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
    properties.put(CatConstants.PROP_TEAM_NAME, groupShortName);
    for (NamedValue namedValue : newTeam.getProperties())
    {
      properties.put(QName.createQName(namedValue.getName()), namedValue.getValue());
    }

    // Create the home folder with the template if it doesn't already exist
    NodeRef teamHomeParent = getTeamHomeContainer(groupPath);
    NodeRef teamHome = NodeUtils.getChildByName(teamHomeParent, groupShortName, nodeService);
    
    if(teamHome == null) {
      QName groupQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, groupShortName);
      NodeRef templateNode = nodeUtils.getNodeByXPath(CatConstants.XPATH_TEAM_HOME_FOLDER_TEMPLATE);

      // Copy the template to correct team home location (keeping in mind hierarchical groups)
      teamHome = copyService.copy(templateNode, teamHomeParent, ContentModel.ASSOC_CONTAINS, groupQName, true);

      // update the name property, title, and description properties
      nodeService.setProperty(teamHome, ContentModel.PROP_NAME, groupShortName);
      nodeService.setProperty(teamHome, ContentModel.PROP_TITLE, groupShortName);
      nodeService.setProperty(teamHome, ContentModel.PROP_DESCRIPTION, groupShortName + "'s home folder");      
    }
    
    // make sure the team folder has the team home folder aspect
    nodeService.addAspect(teamHome, CatConstants.ASPECT_TEAM_HOME_FOLDER, null);

    // Set the correct permissions for this new node
    setupHomeSpacePermissions(teamHome, groupPath, creatorID);

    // Set the homefolder property to the newly created folder
    properties.put(CatConstants.PROP_TEAM_HOME_FOLDER, teamHome);

    // Create the team profile node
    QName profileName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, groupPath);
    NodeRef profile = this.nodeService.createNode(nodeUtils.getNodeByXPath(CatConstants.XPATH_TEAM_CONTAINER), 
        ContentModel.ASSOC_CHILDREN, profileName,
        CatConstants.TYPE_TEAM, properties).getChildRef();
    
    // Give the team permissions on their profile so they can edit it
    String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, groupPath);
    permissionService.setPermission(profile, groupAuthority, permissionService.getAllPermission(), true);

  }

  /**
   * Get the parent folder where the team's home folder should go.
   * @param groupPath
  
   * @return NodeRef
   */
  private NodeRef getTeamHomeContainer(String groupPath) {

    String folderXPath = CatConstants.XPATH_TEAM_DOCUMENTS;
    Path path = new Path(groupPath).removeLastSegments(1);
    String[] segments = path.segments();
    for(String segment : segments) {
      folderXPath += "/cm:" + ISO9075.encode(segment);
    }

    return nodeUtils.getNodeByXPath(folderXPath);
  }

  /**
   * Assumes teamPath is already properly formatted with no
   * trailing slash
   * @param teamPath
  
   * @return the team profile node; null if it does not exist */
  private NodeRef getTeamProfile(String teamPath) {

    String xpath = CatConstants.XPATH_TEAM_CONTAINER + "/cm:" +
    ISO9075.encode(teamPath);

    return nodeUtils.getNodeByXPath(xpath);
  }

  /**
   * Return only those groups for which the current user has permissions
  
   * @return Set<String>
   */
  protected Set<String> getUserGroups() {    
    Set<String> groups;
    if(authorityService.hasAdminAuthority()) {
      groups = authorityService.getAllAuthorities(AuthorityType.GROUP);
      
    } else {
      groups = authorityService.getAuthorities();         
    }
    
    return groups;
  }
  
  protected Set<String> getAllGroups() {
    return authorityService.getAllAuthorities(AuthorityType.GROUP);
  }
  
  /**
   * Method setupHomeSpacePermissions.
   * @param homeSpaceRef NodeRef
   * @param groupPath String
   */
  protected void setupHomeSpacePermissions(NodeRef homeSpaceRef, String groupPath, String userID)
  {
    // Admin Authority has full permissions by default (automatic - set in the permission config)
    // give full permissions to the new team
    String groupName = new Path(groupPath).toString();
    String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, groupName);
    
    permissionService.setPermission(homeSpaceRef, groupAuthority, permissionService.getAllPermission(), true);

    if(!userID.equals("admin")) {
      String userAuthority = this.authorityService.getName(AuthorityType.USER, userID);

      // Set the user who created it as the owner of the home folder so they can change permissions
      // set the owner on all the sub folders too, since owner is not inherited by default
      recursiveSetOwner(homeSpaceRef, userAuthority);
    }

    // Not sure we need to do this, since by default owners get all permissions
    permissionService.setPermission(homeSpaceRef, permissionService.getOwnerAuthority(), permissionService.getAllPermission(), true);

    // now detach (if we did this first we could not set any permissions!)
    permissionService.setInheritParentPermissions(homeSpaceRef, false);
  }

  /**
   * Method recursiveSetOwner.
   * @param nodeRef NodeRef
   * @param groupAuthority String
   */
  protected void recursiveSetOwner(NodeRef nodeRef, String groupAuthority)
  {
    ownableService.setOwner(nodeRef, groupAuthority);
    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    for (ChildAssociationRef child : children)
    {
      recursiveSetOwner(child.getChildRef(), groupAuthority);
    }
  }
  
  /**
   * @see gov.pnnl.cat.server.webservice.group.GroupServiceSoapPort#updateGroups(gov.pnnl.cat.server.webservice.group.GroupDetails[])
   */
  public GroupDetails[] updateGroups(final GroupDetails[] groups) throws RemoteException, AdministrationFault
  {
    try {
      RetryingTransactionCallback<GroupDetails[]> callback = new RetryingTransactionCallback<GroupDetails[]>()
      {
        public GroupDetails[] execute() throws Throwable {
          return updateGroupsImpl(groups);
        }      
      };
      return transactionService.getRetryingTransactionHelper().doInTransaction(callback);

    } catch (Throwable exception) {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new AdministrationFault(0, rootCause.toString());
    }
  }

  /**
   * Update the groups details
   * 
   * @param groups     the group details to update
  
   * @return          the updated group details * @throws AdministrationFault
   */
  public GroupDetails[] updateGroupsImpl(GroupDetails[] groups) throws AdministrationFault
  {
    GroupDetails[] groupDetails = new GroupDetails[groups.length];
    String currentUser = authenticationComponent.getCurrentUserName();
    
    // See which groups the user can edit:
    Set<String> modifiableGroups = getUserGroups();
    
    int index = 0;
    for (GroupDetails group : groups) {

      String unformattedPath = group.getGroupPath();
      Path path = new Path(unformattedPath);
      String groupPath = path.toString();
      String groupAuthority = this.authorityService.getName(AuthorityType.GROUP, groupPath);
     
      // only allow the change if current user is a member or an admin
      if(!modifiableGroups.contains(groupAuthority)) {
        throw new AdministrationFault(0, "Access denied. " + currentUser +
            " does not have the authority to update group: " + groupPath);
      }
      
      // update the team properties
      if (group instanceof TeamDetails) {
        TeamDetails teamDetails = (TeamDetails)group;

        // get the team profile node
        NodeRef teamProfile = this.getTeamProfile(groupPath);

        // check propertis are null, because you may only be adding members
        NamedValue[] properties = teamDetails.getProperties();
        if(properties != null) {

          // set the properties one by one so we don't have to send every property
          // every time, even if they stay the same        
          for (NamedValue namedValue : teamDetails.getProperties()) {

            // TODO: maybe filter out some properties that we should always ignore
            // like team name and home folder
            this.nodeService.setProperty(teamProfile, 
                QName.createQName(namedValue.getName()), namedValue.getValue());
          }
        }
      }

      // update the group members
      Set<String> curMembers = 
        authorityService.getContainedAuthorities(AuthorityType.USER, groupAuthority, true);
      
      // switch to admin privs
      AuthenticationUtil.setRunAsUserSystem();
      
      // remove the existing ones
      for (String memberAuthority : curMembers) {
        authorityService.removeAuthority(groupAuthority, memberAuthority);
      }

      // then add the new ones
      // the members array is coming up null on the server when set as an empty
      // array by the client.  why??
      if(group.getMembers() != null) {
        for(String memberID : group.getMembers()) {        
          String userAuthority = this.authorityService.getName(AuthorityType.USER, memberID);
          this.authorityService.addAuthority(groupAuthority, userAuthority);  
        }
      }
      // go back to user privs
      AuthenticationUtil.setRunAsUser(currentUser);

      // Add the details to the result
      groupDetails[index] = createGroupDetails(groupPath, modifiableGroups);
      index++;
    }

    return groupDetails;
  }

  /**
   * @see gov.pnnl.cat.server.webservice.group.GroupServiceSoapPort#deleteGroups(java.lang.String[])
   */
  public void deleteGroups(final String[] groupNames) throws RemoteException,
  AdministrationFault
  {
    try {
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
      {
        public Object execute() throws Throwable {
          deleteGroupsImpl(groupNames);
          return null;
        }      
      };
      transactionService.getRetryingTransactionHelper().doInTransaction(callback);

    } catch (Throwable exception) {
      logger.error("Unexpected error occurred", exception);      
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      throw new AdministrationFault(0, rootCause.toString());
    }
  }

  /**
   * Delete groups
   * 
   * @param groupNames     the names of the groups to delete
   * @throws AdministrationFault
   */
  public void deleteGroupsImpl(String[] groupNames) throws AdministrationFault
  {
    String groupPath;
    String groupAuthority;
    String currentUser = authenticationComponent.getCurrentUserName();

    // See which groups the user can edit:
    Set<String> modifiableGroups = getUserGroups();

    // We need to run as the system user for the remainder of the tx
    // so that subsequent delete policy also runs as system user.  This is because
    // the policy to clean up the team home folder can only run as an admin
    
    // TODO: a better way to allow the delete would be to bind a transaction parameter
    // that the home folder policy can check for
    
    AuthenticationUtil.setRunAsUserSystem();

    for (String groupName : groupNames)
    {
      // make sure the name is properly formatted
      groupPath = new Path(groupName).toString();
      groupAuthority = this.authorityService.getName(AuthorityType.GROUP, groupPath);

      if(modifiableGroups.contains(groupAuthority)) {

        // the AuthorityContainer policy will clean up the team profile, if
        // it exists
        logger.debug("trying to delete group: " + groupAuthority);
        this.authorityService.deleteAuthority(groupAuthority);

      } else {
        throw new RuntimeException("Access denied. " + currentUser +
            " does not have the authority to delete group: " + groupPath);
      }
    }
  }
  
  /**
   * For now we will just ingore the filter, since we aren't using
   * it.
   * @version $Revision: 1.0 $
   */
  private class GroupQuery extends AbstractQuery<GroupQueryResults> {
   
    private static final long serialVersionUID = -1795645943006242706L;

    /**
     * Method execute.
     * @param serviceRegistry ServiceRegistry
     * @return GroupQueryResults
     * @see org.alfresco.repo.webservice.ServerQuery#execute(ServiceRegistry)
     */
    @Override
    public GroupQueryResults execute(ServiceRegistry serviceRegistry) {

      // Get all groups
      Set<String> visibleGroups = getAllGroups();
      List<GroupDetails> groupDetailsList = new ArrayList<GroupDetails>();

      for (String groupAuthority : visibleGroups) {
        if(logger.isDebugEnabled()) {
          logger.debug("group = " + groupAuthority);
        }
        // ignore special authorities that are not GROUP_:
        AuthorityType type = AuthorityType.getAuthorityType(groupAuthority);
      
        // we only care about group authorities, not roles
        if(type.equals(AuthorityType.GROUP) && type.isPrefixed()) {
          String groupName = GroupWebService.this.authorityService.getShortName(groupAuthority);
          GroupDetails groupDetails = GroupWebService.this.createGroupDetails(groupName, visibleGroups);
          groupDetailsList.add(groupDetails);
        }
      }

      GroupQueryResults queryResult = new GroupQueryResults(null, (GroupDetails[])groupDetailsList.toArray(new GroupDetails[groupDetailsList.size()]));
      return queryResult;
    }
  }
  
  /**
   */
  private class GroupQuerySession extends AbstractQuerySession<GroupQueryResults, GroupDetails>
  {
      private static final long serialVersionUID = 1823253197962982642L;

      /**
       * Constructor for GroupQuerySession.
       * @param maxResults long
       * @param batchSize long
       * @param query ServerQuery<GroupQueryResults>
       */
      public GroupQuerySession(long maxResults, long batchSize, ServerQuery<GroupQueryResults> query)
      {
          super(maxResults, batchSize, query);
      }

      /**
       * Method makeArray.
       * @param size int
       * @return GroupDetails[]
       */
      @Override
      protected GroupDetails[] makeArray(int size)
      {
          return new GroupDetails[size];
      }

      /**
       * Method getNextResults.
       * @param serviceRegistry ServiceRegistry
       * @return GroupQueryResults
       * @see org.alfresco.repo.webservice.QuerySession#getNextResults(ServiceRegistry)
       */
      public GroupQueryResults getNextResults(ServiceRegistry serviceRegistry)
      {
          GroupQueryResults queryResults = getQueryResults(serviceRegistry);
          GroupDetails[] allRows = queryResults.getGroupDetails();
          GroupDetails[] batchedRows = getNextResults(allRows);
          // Build the user query results
          GroupQueryResults batchedResults = new GroupQueryResults();
//          batchedResults.setQuerySession(getId());  TODO: http://issues.alfresco.com/browse/AR-1689

          batchedResults.setGroupDetails(batchedRows);
          // Done
          return batchedResults;
      }
  }
  

  /**
   * Group query session used to support batched group query
   * 
   */
//  private class GroupQuerySession implements Serializable
//  {
//    private static final long serialVersionUID = -2960711874297744356L;
//
//    private int batchSize = -1;        
//    private GroupFilter filter;
//    protected int position = 0;        
//    private String id;
//
//    /** 
//     * Constructor
//     * 
//     * @param batchSize
//     * @param filter
//     */
//    public GroupQuerySession(int batchSize, GroupFilter filter)
//    {
//      this.batchSize = batchSize;
//      this.filter = filter;
//      this.id = GUID.generate();
//    }
//
//    /**
//     * @see gov.pnnl.cat.server.webservice.repository.QuerySession#getId()
//     */
//    public String getId()
//    {
//      return this.id;
//    }
//
//    /**
//     * Calculates the index of the last row to retrieve. 
//     * 
//     * @param totalRowCount The total number of rows in the results
//     * @return The index of the last row to return
//     */
//    protected int calculateLastRowIndex(int totalRowCount)
//    {
//      int lastRowIndex = totalRowCount;
//
//      // set the last row index if there are more results available 
//      // than the batch size
//      if ((this.batchSize != -1) && ((this.position + this.batchSize) < totalRowCount))
//      {
//        lastRowIndex = this.position + this.batchSize;
//      }
//
//      return lastRowIndex;
//    }
//
//    /**
//     * Calculates the value of the next position.
//     * If the end of the result set is reached the position is set to -1
//     * 
//     * @param totalRowCount The total number of rows in the results
//     * @param queryResult The QueryResult object being returned to the client,
//     * if there are no more results the id is removed from the QueryResult instance
//     */
//    protected void updatePosition(int totalRowCount, GroupQueryResults queryResult)
//    {
//      if (this.batchSize == -1)
//      {
//        this.position = -1;
//        queryResult.setQuerySession(null);
//      }
//      else
//      {
//        this.position += this.batchSize;
//        if (this.position >= totalRowCount)
//        {
//          // signify that there are no more results 
//          this.position = -1;
//          queryResult.setQuerySession(null);
//        }
//      }
//    }
//
//    /**
//     * Gets the next batch of group details
//     * 
//     * @return  group query results
//     */
//    public GroupQueryResults getNextBatch()
//    {
//      GroupQueryResults queryResult = null;
//
//      if (this.position != -1)
//      {
//        if (logger.isDebugEnabled())
//          logger.debug("Before getNextBatch: " + toString());
//
//        // Only get the groups the user is a member of, or all groups if admin
//        Set<String> visibleGroups = getUserGroups();
//
//        // TODO do the filter of the resulting list here ....
//        List<String> filteredGroups = new ArrayList<String>(visibleGroups);
//
//        int totalRows = filteredGroups.size();
//        int lastRow = calculateLastRowIndex(totalRows);
//        int currentBatchSize = lastRow - this.position;
//
//        if (logger.isDebugEnabled())
//          logger.debug("Total rows = " + totalRows + ", current batch size = " + currentBatchSize);
//
//        List<GroupDetails> groupDetailsList = new ArrayList<GroupDetails>(currentBatchSize);
//
//        for (int x = this.position; x < lastRow; x++)
//        {
//          String groupAuthority = filteredGroups.get(x);
//          if(logger.isDebugEnabled()) {
//            logger.debug("group " + x + " = " + groupAuthority);
//          }
//          // ignore special alfresco admin groups:
//          // GROUP_EVERYONE and GROUP_EMAIL_CONTRIBUTORS (introduced in 2.2E)
//          // TODO: list of groups to ignore should be injected via the bean config file
//          if (!groupAuthority.equals(PermissionService.ALL_AUTHORITIES) &&
//              !groupAuthority.equals("GROUP_EMAIL_CONTRIBUTORS")) {
//            String groupPath = GroupWebService.this.authorityService.getShortName(groupAuthority);
//            GroupDetails groupDetails = GroupWebService.this.createGroupDetails(groupPath, visibleGroups);
//            groupDetailsList.add(groupDetails);
//          }
//        }
//
//        queryResult = new GroupQueryResults(getId(), (GroupDetails[])groupDetailsList.toArray(new GroupDetails[groupDetailsList.size()]));
//
//        // move the position on
//        updatePosition(totalRows, queryResult);
//
//        if (logger.isDebugEnabled())
//          logger.debug("After getNextBatch: " + toString());
//      }
//
//      return queryResult;
//    }
//  }
//  
//  public class GroupQuery extends Abstr
  
}
