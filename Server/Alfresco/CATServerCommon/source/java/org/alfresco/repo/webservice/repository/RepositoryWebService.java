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
package org.alfresco.repo.webservice.repository;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.acegisecurity.AccessDeniedException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.CMLUtil;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.CML;
import org.alfresco.repo.webservice.types.ClassDefinition;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Node;
import org.alfresco.repo.webservice.types.NodeDefinition;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Query;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.repo.webservice.types.Store;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web service implementation of the RepositoryService. The WSDL for this
 * service can be accessed from
 * http://localhost:8080/alfresco/wsdl/repository-service.wsdl
 * 
 * @author gavinc
 */
public class RepositoryWebService extends AbstractWebService implements
        RepositoryServiceSoapPort
{
    private static Log logger = LogFactory.getLog(RepositoryWebService.class);

    private CMLUtil cmlUtil;
    /**
     * CAT Change
     */
    private ServiceRegistry unfilteredServiceRegistry;

    
    /**
     * CAT Change
     * @param unfilteredServiceRegistry the unfilteredServiceRegistry to set
     */
    public void setUnfilteredServiceRegistry(ServiceRegistry unfilteredServiceRegistry) {
      this.unfilteredServiceRegistry = unfilteredServiceRegistry;
    }

    /**
     * Sets the CML Util
     * 
     * @param cmlUtil   CML util object
     */
    public void setCmlUtil(CMLUtil cmlUtil)
    {
        this.cmlUtil = cmlUtil;
    }

    /**
     * {@inheritDoc}
     */
    public Store createStore(String scheme, String address) throws RemoteException, RepositoryFault
    {
        StoreRef storeRef = this.nodeService.createStore(scheme, address);
        return Utils.convertToStore(storeRef);
    }

    /**
     * {@inheritDoc}
     */
    public Store[] getStores() throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<Store[]> callback = new RetryingTransactionCallback<Store[]>()
            {
                public Store[] execute() throws Throwable
                {
                    List<StoreRef> stores = nodeService.getStores();
                    Store[] returnStores = new Store[stores.size()];
                    for (int x = 0; x < stores.size(); x++)
                    {
                        StoreRef storeRef = stores.get(x);
                        
                        if (logger.isDebugEnabled() == true)
                        {
                            logger.debug("Store protocol :" + storeRef.getProtocol());
                        }
                        
                        Store store = Utils.convertToStore(storeRef);
                        returnStores[x] = store;
                    }

                    return returnStores;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        }
        catch (Throwable e)
        {        
          logger.error("Unexpected error occurred", e);
          Throwable rootCause = NodeUtils.getRootCause(e);
          throw new RepositoryFault(0, rootCause.toString());
        }
    }

    
    private ResultSet filterResultSet(ResultSet batchedResults) {
      long start = System.currentTimeMillis();

      // CAT Change: need to filter the batch based on a permissions check
      // since the full query didn't use permissions
      ResultSetRow[] rows = batchedResults.getRows();
      Map<NodeRef, ResultSetRow> filteredRows = new HashMap<NodeRef, ResultSetRow>();

      StringBuilder notIndexedQuery = new StringBuilder();
      for (ResultSetRow row : rows) {
        // Major problem - alfresco hides the Store at this point
        // so we have to assume SpacesStore for everything, which is fine for CAT
        // but may mess somebody else up
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        NodeRef nodeRef = new NodeRef(storeRef, row.getNode().getId());

        // make sure current user can see it
        AccessStatus permission = serviceRegistry.getPermissionService().hasPermission(nodeRef, PermissionService.READ);

        if (!permission.equals(AccessStatus.DENIED)) {
          filteredRows.put(nodeRef, row);
          if (notIndexedQuery.length() != 0) {
            notIndexedQuery.append(" OR ");
          }
          notIndexedQuery.append("ID:\"" + nodeRef.toString() + "\"");
        }
      }

//      long qstart = System.currentTimeMillis();
      org.alfresco.service.cmr.search.ResultSet results = null;
      try {
        if (notIndexedQuery.length() > 0) {
          notIndexedQuery.insert(0, '(');
          notIndexedQuery.append(") AND TEXT:nicm");
          results = this.unfilteredServiceRegistry.getSearchService().query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, notIndexedQuery.toString());
          for (org.alfresco.service.cmr.search.ResultSetRow resultSetRow : results) {
            StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
            NodeRef nodeRef = new NodeRef(storeRef, resultSetRow.getNodeRef().getId());
            ResultSetRow resultRow = filteredRows.get(nodeRef);
            NamedValue[] newProps = Arrays.copyOf(resultRow.getColumns(), resultRow.getColumns().length + 1);
            newProps[newProps.length - 1] = Utils.createNamedValue(dictionaryService, CatConstants.PROP_NEEDS_FULL_TEXT_INDEXED, "YES");
            resultRow.setColumns(newProps);
          }
        }
      } finally {
        if (results != null) {
          results.close();
        }
      }
//      long qend = System.currentTimeMillis();
//      logger.error("time to query for nicm = " + (qend - qstart) + " ms");

      ResultSetRow[] newRows = filteredRows.values().toArray(new ResultSetRow[filteredRows.size()]);
      batchedResults.setRows(newRows);
      batchedResults.setTotalRowCount(newRows.length);

      long end = System.currentTimeMillis();
      if(logger.isDebugEnabled()) {
        logger.error("time to filter result set = " + (end - start) + " ms");
      }
      return batchedResults;
    }
    
    
    
    /**
     * Executes the given query, caching the results as required.
     */
    private QueryResult executeQuery(final MessageContext msgContext, final ServerQuery<ResultSet> query, final long maxResults) throws RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<QueryResult> callback = new RetryingTransactionCallback<QueryResult>()
            {
                public QueryResult execute() throws Throwable
                {
                    // Construct a session to handle the iteration
                    long batchSize = Utils.getBatchSize(msgContext);
                    RepositoryQuerySession session = new RepositoryQuerySession(maxResults, batchSize, query);//Long.MAX_VALUE
                    String sessionId = session.getId();

                    // Get the first batch of results
                    // CAT Change: use the unfiltered search so it executes faster
                    long start = System.currentTimeMillis();
                    ResultSet unfilteredResults = session.getNextResults(unfilteredServiceRegistry);
                    long end = System.currentTimeMillis();
                    if(logger.isDebugEnabled()) {
                      logger.debug("time to execute original query = " + (end - start) + " ms");                    
                    }
                    ResultSet batchedResults = filterResultSet(unfilteredResults);
//                    ResultSet batchedResults = session.getNextResults(serviceRegistry);
                    
                    
                    // Construct the result
                    // TODO: http://issues.alfresco.com/browse/AR-1689
                    boolean haveMoreResults = session.haveMoreResults();
                    QueryResult queryResult = new QueryResult(
                            haveMoreResults ? sessionId : null,
                            batchedResults);

                    // Cache the session
                    if (session.haveMoreResults())
                    {
                        querySessionCache.put(sessionId, session);
                    }

                    // Done
                    return queryResult;
                }
            };
            // Check for empty results sets (if all results were filtered out)
            QueryResult results = Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback, true);
            String querySessionId = results.getQuerySession();
            if(querySessionId != null &&
                results.getResultSet().getRows().length == 0) 
            {
                return fetchMore(querySessionId);
            }
            return results;
        }
        catch (Throwable e)
        {        
          logger.error("Unexpected error occurred", e);
          Throwable rootCause = NodeUtils.getRootCause(e);
          throw new RepositoryFault(0, rootCause.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult query(final Store store, final Query query, final boolean includeMetaData) throws RemoteException, RepositoryFault
    {
        String language = query.getLanguage();
        //CAT CHANGE BEGIN:
        // limit was not exposed in alfresco's web service yet its fully supported.  So, if the CAT client wants to limit search results, 
        // it will pass as the language param "lucene_limit_##" where ##'s will be parsed as the integer to limit with 
        // this is hopefully a temporary fix while waiting for a new release of Alfresco CMIS SQL like query interface that will be more fully implemented
        // (right now there is no way to use it and query for things that do NOT have an aspect- query needs to do a Left Excluding JOIN)
        long maxResults = Long.MAX_VALUE;
        if(language.startsWith(CatConstants.QUERY_LANG_LUCENE_LIMIT)){
          try{
            maxResults = Long.parseLong(language.substring(CatConstants.QUERY_LANG_LUCENE_LIMIT.length()));
            language = Utils.QUERY_LANG_LUCENE;
            query.setLanguage(Utils.QUERY_LANG_LUCENE);
          }catch (NumberFormatException e){
            throw new RepositoryFault(110, "language value: '"
                + language
                + "' invalid");
          }
        }
        //CAT CHANGE END (includes else)
        else if (language.equals(Utils.QUERY_LANG_LUCENE) == false)
        {
            throw new RepositoryFault(110, "Only '"
                    + Utils.QUERY_LANG_LUCENE
                    + "' queries are currently supported!");
        }

        final MessageContext msgContext = MessageContext.getCurrentContext();
        //CAT CHANGE (now passing in maxResults):
        CatSearchQuery serverQuery = new CatSearchQuery(store, query);
        QueryResult queryResult = executeQuery(msgContext, serverQuery, maxResults);
        // Done
        return queryResult;
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult queryChildren(final Reference node) throws RemoteException, RepositoryFault
    {
        final MessageContext msgContext = MessageContext.getCurrentContext();
        ChildAssociationQuery query = new ChildAssociationQuery(node);
        //CAT CHANGE (passing in Long.MAX_VALUE for maxResults):
        QueryResult queryResult = executeQuery(msgContext, query, Long.MAX_VALUE);
        // Done
        return queryResult;
    }
    
    /**
     * {@inheritDoc}
     */
    public QueryResult queryParents(final Reference node) throws RemoteException, RepositoryFault
    {
        final MessageContext msgContext = MessageContext.getCurrentContext();
        ParentAssociationQuery query = new ParentAssociationQuery(node);
        //CAT CHANGE (passing in Long.MAX_VALUE for maxResults):
        QueryResult queryResult = executeQuery(msgContext, query, Long.MAX_VALUE);
        // Done
        return queryResult;
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult queryAssociated(final Reference node, final Association association) throws RemoteException, RepositoryFault
    {
        final MessageContext msgContext = MessageContext.getCurrentContext();
        AssociationQuery query = new AssociationQuery(node, association);
        //CAT CHANGE (passing in Long.MAX_VALUE for maxResults):
        QueryResult queryResult = executeQuery(msgContext, query, Long.MAX_VALUE);
        // Done
        return queryResult;
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult fetchMore(final String querySessionId) throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<QueryResult> callback = new RetryingTransactionCallback<QueryResult>()
            {
                public QueryResult execute() throws Throwable
                {
                    RepositoryQuerySession session = null;
                    try
                    {
                        // try and get the QuerySession with the given id from the cache
                        session = (RepositoryQuerySession) querySessionCache.get(querySessionId);
                    }
                    catch (ClassCastException e)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Query session was not generated by the RepositoryWebService: " + querySessionId);
                        }
                        throw new RepositoryFault(
                                4,
                                "querySession with id '" + querySessionId + "' is invalid");
                    }

                    if (session == null)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Invalid querySession id requested: " + querySessionId);
                        }
                        throw new RepositoryFault(
                                4,
                                "querySession with id '" + querySessionId + "' is invalid");
                    }

                    // CAT Change: use the unfiltered search so it executes faster
                    long start = System.currentTimeMillis();
                    ResultSet unfilteredResults = session.getNextResults(unfilteredServiceRegistry);
                    long end = System.currentTimeMillis();
                    if (logger.isDebugEnabled()) {
                      logger.debug("time to fetch next batch = " + (end - start) + " ms");
                    }
                    ResultSet moreResults = filterResultSet(unfilteredResults);
//                    ResultSet moreResults = session.getNextResults(serviceRegistry);
                    
                    
                    // Drop the cache results if there are no more results expected
                    if (!session.haveMoreResults())
                    {
                        querySessionCache.remove(querySessionId);
                    }
                    else
                    {
                        // We still need to update the cache with the latest session to
                        // ensure that the instance gets replicated to other listening caches
                        querySessionCache.put(querySessionId, session);
                    }

                    // get the next batch of results
                    // TODO: http://issues.alfresco.com/browse/AR-1689
                    boolean haveMoreResults = session.haveMoreResults();
                    QueryResult queryResult = new QueryResult(
                            haveMoreResults ? querySessionId : null,
                            moreResults);

                    // Done
                    return queryResult;
                }
            };
            
            // Check for empty results sets (if all results were filtered out)
            QueryResult results = Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback, true);
            if(results.getQuerySession() != null &&
                results.getResultSet().getRows().length == 0) 
            {
                return fetchMore(querySessionId);
            }
            return results;
        }
        catch (Throwable e)
        {
            if (e instanceof RepositoryFault)
            {
                throw (RepositoryFault) e;
            }
            else
            {
              logger.error("Unexpected error occurred", e);
              Throwable rootCause = NodeUtils.getRootCause(e);
              throw new RepositoryFault(0, rootCause.toString());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public UpdateResult[] update(final CML statements) throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<UpdateResult[]> callback = new RetryingTransactionCallback<UpdateResult[]>()
            {
                public UpdateResult[] execute() throws Throwable
                {
                    return cmlUtil.executeCML(statements);
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
          logger.error("Unexpected error occurred", e);
          Throwable rootCause = NodeUtils.getRootCause(e);
          throw new RepositoryFault(0, rootCause.toString());
        }
    }
    
    public CMLUtil getCmlUtil() {
      return cmlUtil;
    }

    /**
     * {@inheritDoc}
     */
    public NodeDefinition[] describe(final Predicate items) throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<NodeDefinition[]> callback = new RetryingTransactionCallback<NodeDefinition[]>()
            {
                public NodeDefinition[] execute() throws Throwable
                {
                    List<NodeRef> nodes = Utils.resolvePredicate(items, nodeService, searchService, namespaceService);
                    NodeDefinition[] nodeDefs = new NodeDefinition[nodes.size()];
        
                    for (int x = 0; x < nodes.size(); x++)
                    {
                        nodeDefs[x] = setupNodeDefObject(nodes.get(x));
                    }
        
                    return nodeDefs;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        }
        catch (Throwable e)
        {
          logger.error("Unexpected error occurred", e);
          Throwable rootCause = NodeUtils.getRootCause(e);
          throw new RepositoryFault(0, rootCause.toString());
        }
    }

    /**
     * Creates a NodeDefinition web service type object for the given 
     * repository NodeRef instance
     * 
     * @param nodeRef The NodeRef to generate the NodeDefinition for
     * @return The NodeDefinition representation of nodeRef
     */
    private NodeDefinition setupNodeDefObject(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled())
            logger.debug("Building NodeDefinition for node: " + nodeRef);

        TypeDefinition ddTypeDef = this.dictionaryService
                .getType(this.nodeService.getType(nodeRef));

        // create the web service ClassDefinition type from the data dictionary TypeDefinition
        ClassDefinition typeDef = Utils.setupClassDefObject(ddTypeDef);

        Set<QName> aspectsQNames = this.nodeService.getAspects(nodeRef);
        ClassDefinition[] aspectDefs = new ClassDefinition[aspectsQNames.size()];
        int pos = 0;
        for (QName aspectQName : aspectsQNames)
        {
            AspectDefinition aspectDef = this.dictionaryService.getAspect(aspectQName);
            aspectDefs[pos] = Utils.setupClassDefObject(aspectDef);
            pos++;
        }

        return new NodeDefinition(typeDef, aspectDefs);
    }

    /**
     * Gets the nodes associatiated with the predicate provided.  Usefull when the store and ids of the required
     * nodes are known.
     * 
     * {@inheritDoc}
     */
    public Node[] get(final Predicate where) throws RemoteException, RepositoryFault
    {
        final List<NodeRef> failedNodeRefs = new ArrayList<NodeRef>();
        try
        {
            RetryingTransactionCallback<Node[]> callback = new RetryingTransactionCallback<Node[]>()
            {
                public Node[] execute() throws Throwable
                {
                    // Resolve the predicate to a list of node references
                    List<NodeRef> nodeRefs = Utils.resolvePredicate(where, nodeService, searchService, namespaceService);
                    List<Node> nodeList = new ArrayList<Node>();
                    for (NodeRef nodeRef : nodeRefs)
                    {
                        // search can return nodes that no longer exist, so we need to  ignore these
                        if(nodeService.exists(nodeRef) == false) 
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.warn("Search returned node that doesn't exist: " + nodeRef);
                            }
                        }
                        Reference reference = null;
                        
                        // Get the nodes reference
                        try {
                          reference = Utils.convertToReference(nodeService, namespaceService, nodeRef);
                        } catch (RuntimeException e) {
                          failedNodeRefs.add(nodeRef);
                          throw e;
                        }
                        // Get the nodes type
                        String type = nodeService.getType(nodeRef).toString();
                        
                        // Get the nodes aspects
                        Set<QName> aspectQNames = nodeService.getAspects(nodeRef);
                        String[] aspects = new String[aspectQNames.size()];
                        int aspectIndex = 0;
                        for (QName aspectQName : aspectQNames)
                        {
                            aspects[aspectIndex] = aspectQName.toString();
                            aspectIndex++;
                        }
                        
                        // Get the nodes properties
                        Map<QName, Serializable> propertyMap = nodeService.getProperties(nodeRef);
                        
                        // add 3 extra properties
                        NamedValue[] properties = new NamedValue[propertyMap.size() + 3];
                        int propertyIndex = 0;
                        for (Map.Entry<QName, Serializable> entry : propertyMap.entrySet())
                        { 
                            properties[propertyIndex] = Utils.createNamedValue(dictionaryService, entry.getKey(), entry.getValue());
                            propertyIndex++;
                        }
                        
                        // Add one more prop for the node's path
                        properties[propertyIndex++] = Utils.createNamedValue(
                                dictionaryService,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"),
                                nodeService.getPath(nodeRef).toString());
                        
                        // Add one more column for the node's size (if it's a content node)
                        // TODO: should we iterate over child sizes if it's a folder?
                        long size = 0;
                        if(nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
                          ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                          if(reader != null) {
                            size = reader.getSize();
                          }   
                        }
                        properties[propertyIndex++] = Utils.createNamedValue(
                                dictionaryService,
                                CatConstants.PROP_SIZE,
                                String.valueOf(size));
                        
                        // Add one more column for the number of children
                        int childCount = 0;
                        if(nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {      
                          childCount = nodeService.getChildAssocs(nodeRef).size();
                        }         
                        properties[propertyIndex++] = Utils.createNamedValue(
                            dictionaryService,
                            CatConstants.PROP_CHILD_COUNT,
                            String.valueOf(childCount));
                        
                        // Create the node and add to the array
                        Node node = new Node(reference, type, aspects, properties);
                        nodeList.add(node);
                    }
                    
                    Node[] nodes = nodeList.toArray(new Node[nodeList.size()]);
                    
                    return nodes;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
          Throwable rootCause = NodeUtils.getRootCause(e);
          String filename  = "";
          if(failedNodeRefs.size() > 0) {
            filename = (String)unfilteredServiceRegistry.getNodeService().getProperty(failedNodeRefs.get(0), ContentModel.PROP_NAME);
          }
          
          // do not log the whole stack trace for file not found or 
          // access denied because it spams the log
          if(rootCause instanceof AccessDeniedException) {
            logger.error("RepostioryWebService.get() failed.  Access denied for resource: " + filename);
            
          } else if(rootCause instanceof IllegalStateException && rootCause.getMessage().startsWith("Failed to resolve to a single NodeRef") ) {
            logger.error("RepositoryWebService.get() failed.  Resource not found: " + filename);
            
          } else {
            logger.error("Unexpected error occurred", e);            
          }
          throw new RepositoryFault(0, rootCause.toString());
        }
    }
}
