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
/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package gov.pnnl.cat.server.webservice.user;

import gov.pnnl.cat.server.webservice.util.ExceptionUtils;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.administration.UserQuery;
import org.alfresco.repo.webservice.administration.UserQuerySession;
import org.alfresco.repo.webservice.repository.RepositoryFault;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 * @version $Revision: 1.0 $
 */
public class UserWebService extends AbstractWebService implements
        UserServiceSoapPort
{
    /** Log */
    private static Log logger = LogFactory.getLog(UserWebService.class);
    
    /** The person service */
    private PersonService personService = null;
    
    /** The authentication service */
    private MutableAuthenticationService mutableAuthenticationService = null;
    
    /** The transaction service */
    private TransactionService transactionService = null;
    
    /** A set of ignored properties */
    private static Set<QName> ignoredProperties = new HashSet<QName>(3);
    
    // CAT Changes
    private AuthenticationComponent authenticationComponent = null;
    private AuthorityService authorityService = null;
    private DictionaryService dictionaryService = null;
    // End CAT Changes
    
    /**
     * Constructor
     */
    public UserWebService()
    {
        // Set properties to ignore
        UserWebService.ignoredProperties.add(ContentModel.PROP_STORE_PROTOCOL);
        UserWebService.ignoredProperties.add(ContentModel.PROP_STORE_IDENTIFIER);
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
     * Method getDictionaryService.
     * @return DictionaryService
     */
    public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	/**
	 * Method setDictionaryService.
	 * @param dictionaryService DictionaryService
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
    
    /**
     * Set the authentication service
     * 
     * @param authenticationService     the authentication service
     */
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.mutableAuthenticationService = authenticationService;
    }
    
    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#queryUsers(org.alfresco.repo.webservice.administration.UserFilter)
     */
    public UserQueryResults queryUsers(final UserFilter filter)
            throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserQueryResults> callback = new RetryingTransactionCallback<UserQueryResults>()
            {
                public UserQueryResults execute() throws Exception
                {
                    return queryUsersImpl(filter);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            logger.error("Unexpected error occurred", exception);      
            Throwable rootCause = ExceptionUtils.getRootCause(exception);
            throw new AdministrationFault(0, rootCause.toString());
        }
    }

    /**
     * Query users, batch by set size
     * 
     * @param filter    used to filter results
    
     * @return          user query results, optionally batched */
    public UserQueryResults queryUsersImpl(UserFilter filter)
    {
        MessageContext msgContext = MessageContext.getCurrentContext();
        
        // Create the query
        org.alfresco.repo.webservice.administration.UserFilter alfFilter = null;
        if(filter != null) {
          alfFilter = new org.alfresco.repo.webservice.administration.UserFilter(filter.getUserName());
        }
        UserQuery query = new UserQuery(alfFilter);
        
        // Create a user query session
        CatUserQuerySession userQuerySession = new CatUserQuerySession(Long.MAX_VALUE, Utils.getBatchSize(msgContext), query);
        
        // Get the next batch of results
        UserQueryResults userQueryResults = userQuerySession.getCatNextResults(serviceRegistry);

        String querySessionId = userQuerySession.getId();

        // add the session to the cache if there are more results to come
        boolean haveMoreResults = userQuerySession.haveMoreResults();

        if (haveMoreResults)
        {
            querySessionCache.put(querySessionId, userQuerySession);
        }
        
        // Construct the return value
        // TODO: http://issues.alfresco.com/browse/AR-1689
        // This looks odd, but I've chosen to be specific about when the ID is set on the return
        // results and when it isn't.
        UserQueryResults result = new UserQueryResults(
                haveMoreResults ? querySessionId : null,
                        userQueryResults.getUserDetails());
        
        // Done
        return result;
    }    

    /**
     *  @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#fetchMoreUsers(java.lang.String)
     */
    public UserQueryResults fetchMoreUsers(final String querySession)
            throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserQueryResults> callback = new RetryingTransactionCallback<UserQueryResults>()
            {
                public UserQueryResults execute() throws Exception
                {
                    return fetchMoreUsersImpl(querySession);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            logger.error("Unexpected error occurred", exception);
            Throwable rootCause = ExceptionUtils.getRootCause(exception);
            throw new AdministrationFault(0, rootCause.toString());
        }
    }

    /**
     * 
     * @param querySessionId
    
     * @return UserQueryResults
     * @throws RepositoryFault
     */
    public UserQueryResults fetchMoreUsersImpl(String querySessionId) throws RepositoryFault
    {
        CatUserQuerySession session = null;
        try
        {
            session = (CatUserQuerySession) querySessionCache.get(querySessionId);
        }
        catch (ClassCastException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Query session was not generated by the AdministrationWebService: " + querySessionId);
            }
            throw new RepositoryFault(
                    4,
                    "querySession with id '" + querySessionId + "' is invalid");
        }
        
        UserQueryResults queryResult = null;
        if (session != null)
        {
            queryResult = session.getCatNextResults(serviceRegistry);
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
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#getUser(java.lang.String)
     */
    public UserDetails getUser(final String userName) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserDetails> callback = new RetryingTransactionCallback<UserDetails>()
            {
                public UserDetails execute() throws Exception
                {
                    return getUserImpl(userName);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            logger.error("Unexpected error occurred", exception);     
            Throwable rootCause = ExceptionUtils.getRootCause(exception);
            throw new AdministrationFault(0, rootCause.toString());
        }
    }
    
    /**
     * Get the user details
     * 
     * @param userName              the user name
    
    
    
     * @return                      the user details object * @throws AdministrationFault * @throws RemoteException */
    public UserDetails getUserImpl(String userName)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        UserDetails userDetails = null;
        
        if (this.personService.personExists(userName) == true)
        {
            NodeRef nodeRef = this.personService.getPerson(userName);            
            userDetails = createUserDetails(nodeService, userName, nodeRef);
        }
        else
        {
            // Throw an exception to indicate that the user does not exist
            throw new AlfrescoRuntimeException(MessageFormat.format("The user with name {0} does not exist.", new Object[]{userName}));
        }
        
        return userDetails;
    }

    /**
     * Given a valid person node reference will create a user details object
     * 
     * @param nodeRef   the node reference
    
     * @param nodeService NodeService
     * @param userName String
     * @return          the user details object populated with the appropriate property values */
    /* package */ private UserDetails createUserDetails(NodeService nodeService, String userName, NodeRef nodeRef)
    {
        // Create the user details object
        UserDetails userDetails = new UserDetails();
        
        // Set the user name
        userDetails.setUserName(userName);
        
        // Set the various property values
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        List<NamedValue> namedValues = new ArrayList<NamedValue>(properties.size());
        for (Map.Entry<QName, Serializable> entry : properties.entrySet())
        {
            if (UserWebService.ignoredProperties.contains(entry.getKey()) == false)
            {
            	// CAT CHANGE
              // Handle multi-valued props
              NamedValue value = Utils.createNamedValue(this.dictionaryService, entry.getKey(), entry.getValue());
              if(entry.getKey().equals(ContentModel.PROP_HOMEFOLDER)) {
                // write the value as the path instead
                NodeRef homefolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, entry.getValue());
                String path = nodeService.getPath(homefolder).toString();
                value.setValue(path);
              }
              namedValues.add(value);      
            }
        }
        userDetails.setProperties((NamedValue[])namedValues.toArray(new NamedValue[namedValues.size()]));
        
        // CAT CHANGE

        // Find out if the user has the admin authority
        String currentUser = authenticationComponent.getCurrentUserName();
        //AuthenticationUtil.setCurrentUser(userName);
        boolean isAdmin = authorityService.isAdminAuthority(userName);
        //AuthenticationUtil.setCurrentUser(currentUser);        
        userDetails.setAdmin(isAdmin);

        // END CAT CHANGE
        
        return userDetails;        
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#createUsers(org.alfresco.repo.webservice.administration.NewUserDetails[])
     */
    public UserDetails[] createUsers(final NewUserDetails[] newUsers) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserDetails[]> callback = new RetryingTransactionCallback<UserDetails[]>()
            {
                public UserDetails[] execute() throws Exception
                {
                    return createUsersImpl(newUsers);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            logger.error("Unexpected error occurred", exception);
            Throwable rootCause = ExceptionUtils.getRootCause(exception);
            throw new AdministrationFault(0, rootCause.toString());
        }
    }
    
    /**
     * Create the new users
     * 
     * @param newUsers          the new users detail
    
    
    
     * @return                  the details of the created users * @throws AdministrationFault * @throws RemoteException */
    public UserDetails[] createUsersImpl(NewUserDetails[] newUsers)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        UserDetails[] userDetails = new UserDetails[newUsers.length];
        
        int index = 0;
        for (NewUserDetails newUser : newUsers)
        {
            // Create a new authentication
            this.mutableAuthenticationService.createAuthentication(newUser.getUserName(), newUser.getPassword().toCharArray());
            
            // Create a new person
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
            properties.put(ContentModel.PROP_USERNAME, newUser.getUserName());
            for (NamedValue namedValue : newUser.getProperties())
            {
                // CAT Change:  handle multi-valued properties
                QName qname = QName.createQName(namedValue.getName());
                Serializable propValue = Utils.getValueFromNamedValue(this.dictionaryService, qname, namedValue);
                properties.put(qname, propValue);
            }
            NodeRef personNodeRef = this.personService.createPerson(properties);
            
            // Add the details to the result
            userDetails[index] = createUserDetails(nodeService, newUser.getUserName(), personNodeRef);
            index++;
        }
                
        return userDetails;
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#updateUsers(org.alfresco.repo.webservice.administration.UserDetails[])
     */
    public UserDetails[] updateUsers(final UserDetails[] users) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserDetails[]> callback = new RetryingTransactionCallback<UserDetails[]>()
            {
                public UserDetails[] execute() throws Exception
                {
                    return updateUsersImpl(users);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            logger.error("Unexpected error occurred", exception);
            Throwable rootCause = ExceptionUtils.getRootCause(exception);
            throw new AdministrationFault(0, rootCause.toString());
        }
    }
    
    /**
     * Update the users details
     * 
     * @param users     the user details to update
    
     * @return          the updated user details */
    public UserDetails[] updateUsersImpl(UserDetails[] users)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        UserDetails[] userDetails = new UserDetails[users.length];
        
        int index = 0;
        for (UserDetails user : users)
        {
        	NodeRef person = this.personService.getPerson(user.getUserName());
        	
            // Build the property map
            Map<QName, Serializable> properties = nodeService.getProperties(person);
        
            // CAT CHANGE: Don't make user have to pass all the properties in the call if they
            // only want to change one value
            for (NamedValue namedValue : user.getProperties())
            {
                QName qname = QName.createQName(namedValue.getName());
                Serializable propValue = Utils.getValueFromNamedValue(this.dictionaryService, qname, namedValue);
                properties.put(qname, propValue);  
            }
            
            // TODO: we really should be using the PersonService for this, and not
            // bypassing it by going directly to the nodeService.  However, the
            // person service didn't have a way to look up all the properties
            nodeService.setProperties(person, properties);
            
            // Add the details to the result
            userDetails[index] = createUserDetails(nodeService, user.getUserName(), person);
            index++;
        }
                
        return userDetails;
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public void changePassword(final String userName, final String oldPassword, final String newPassword) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    changePasswordImpl(userName, oldPassword, newPassword);
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            logger.error("Unexpected error occurred", exception);
            
            if(exception instanceof AdministrationFault) {
              throw (AdministrationFault)exception;
            
            } else {
              Throwable rootCause = ExceptionUtils.getRootCause(exception);
              throw new AdministrationFault(0, rootCause.toString());
            }
        }
    }

    /**
     * Change the current password of the user
     * 
     * @param userName      the user name
     * @param oldPassword   the old (current) password
     * @param newPassword   the new password
    
     * @throws AdministrationFault  */
    public void changePasswordImpl(String userName, String oldPassword, String newPassword) throws AdministrationFault
    {
    	// Allow the admin to change any password
        if(authorityService.hasAdminAuthority()) {
          this.mutableAuthenticationService.setAuthentication(userName, newPassword.toCharArray());
          
        } else {
          // Allow user to change his own password
          String currentUser = authenticationComponent.getCurrentUserName();
          
          if(currentUser.equalsIgnoreCase(userName)) {
            this.mutableAuthenticationService.updateAuthentication(userName, oldPassword.toCharArray(), newPassword.toCharArray());        
          
          } else {
            throw new AdministrationFault(0, "Access denied. " + currentUser +
                " does not have the authority to change " + userName + "'s password.");
          }
        }
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#deleteUsers(java.lang.String[])
     */
    public void deleteUsers(final String[] userNames) throws RemoteException,
            AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    deleteUsersImpl(userNames);
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            logger.error("Unexpected error occurred", exception);
            Throwable rootCause = ExceptionUtils.getRootCause(exception);
            throw new AdministrationFault(0, rootCause.toString());
        }
    }

    /**
     * Delete users
     * 
     * @param userNames     the names of the users to delete
     */
    public void deleteUsersImpl(String[] userNames)
    {
        for (String userName : userNames)
        {
            this.mutableAuthenticationService.deleteAuthentication(userName);
            this.personService.deletePerson(userName);
        }        
    }

	/**
	 * Method setAuthenticationComponent.
	 * @param authenticationComponent AuthenticationComponent
	 */
	public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
		this.authenticationComponent = authenticationComponent;
	}

	/**
	 * Method getAuthenticationComponent.
	 * @return AuthenticationComponent
	 */
	public AuthenticationComponent getAuthenticationComponent() {
		return authenticationComponent;
	}

	/**
	 * Method setAuthorityService.
	 * @param authorityService AuthorityService
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * Method getAuthorityService.
	 * @return AuthorityService
	 */
	public AuthorityService getAuthorityService() {
		return authorityService;
	}
	
	/**
	 */
	public class CatUserQuerySession extends UserQuerySession {
	 
    /**
     * Constructor for CatUserQuerySession.
     * @param maxResults long
     * @param batchSize long
     * @param query ServerQuery<org.alfresco.repo.webservice.administration.UserQueryResults>
     */
    public CatUserQuerySession(long maxResults, long batchSize, ServerQuery<org.alfresco.repo.webservice.administration.UserQueryResults> query) {
      super(maxResults, batchSize, query);
    }

    /**
     * Method getCatNextResults.
     * @param serviceRegistry ServiceRegistry
     * @return UserQueryResults
     */
    public UserQueryResults getCatNextResults(ServiceRegistry serviceRegistry) {
      
      org.alfresco.repo.webservice.administration.UserQueryResults alfResults = this.getNextResults(serviceRegistry);
      UserQueryResults catResults = new UserQueryResults();
      catResults.setQuerySession(alfResults.getQuerySession());
      UserDetails[] userDetails = new UserDetails[alfResults.getUserDetails().length];
      int i = 0;
      for (org.alfresco.repo.webservice.administration.UserDetails alfDetails : alfResults.getUserDetails()) {
        boolean isAdmin = authorityService.isAdminAuthority(alfDetails.getUserName());
        UserDetails catDetails = new UserDetails(alfDetails.getUserName(), alfDetails.getProperties(), isAdmin);
        userDetails[i] = catDetails;
        i++;
      }
      catResults.setUserDetails(userDetails);
      return catResults;
    }
 
	}
}
