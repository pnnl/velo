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
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.webservice.repository;

import gov.pnnl.cat.util.CatConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Query;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.repo.webservice.types.Store;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CAT CHANGE: this class is a replacement for SearchQuery in order for the maxResults param to be used.  It is a copy of SeachQuery, 
 * but instead of extending the Abstract class AbstractQuery<ResultSet> it includes the method from it createResultSetRowNode and has an
 * implementation for execute(ServiceRegistry, long) unlike the alfresco class (alfresco's ignored the maxResults param).  Because this
 * class no longer extends AbstractQuery<ResultSet>, I also had to add that this class implements the interface ServerQuery<ResultSet>.  
 * Everything in this class was copied from SearchQuery unless noted with a CAT CHANGE comment
 * 
 * A query to using full search.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class CatSearchQuery implements ServerQuery<ResultSet>
{
    private static final long serialVersionUID = 5429510102265380433L;
    private static Log logger = LogFactory.getLog(CatSearchQuery.class);

    private Store store;
    private Query query;

    /**
     * @param node              The node to query against
     * @param association       The association type to query or <tt>null</tt> to query all
     */
    public CatSearchQuery(Store store, Query query)
    {
        this.store = store;
        this.query = query;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("SearchQuery")
          .append("[ store=").append(this.store.getScheme()).append(":").append(this.store.getAddress())
          .append(" language=").append(this.query.getLanguage())
          .append(" statement=").append(this.query.getStatement())
          .append("]");
        return sb.toString();
    }

    public ResultSet execute(ServiceRegistry serviceRegistry, long maxResults)
    {
      //CAT CHANGE (impl copied from execute(serviceRegistry) method except I added SearchParameters bit below
//        return execute(serviceRegistry); //alf's code just used super's impl
      SearchService searchService = serviceRegistry.getSearchService();
      NodeService nodeService = serviceRegistry.getNodeService();
      DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
      ContentService contentService = serviceRegistry.getContentService();
      
      // handle the special search string of * meaning, get everything
      String statement = query.getStatement();
      if (statement.equals("*"))
      {
          statement = "ISNODE:*";
      }
      org.alfresco.service.cmr.search.ResultSet searchResults = null;
      try
      {
          StoreRef storeRef = Utils.convertToStoreRef(store);
          //CAT CHANGE BEGIN
//        searchResults = searchService.query(storeRef, query.getLanguage(), statement);
          SearchParameters sp = new SearchParameters();
          sp.setLanguage(query.getLanguage());
          sp.setQuery(statement);
          sp.addStore(storeRef);

          // limit search results size as configured
          if (maxResults > 0)
          {
            // discovered that "Limit" would not work for me since it's intended use was to limit "after pruning by permissions"
            // and cat does its own permission filter, but leaving this in case code is refactored to no longer do our own permission filtering
             if (maxResults < Integer.MIN_VALUE || maxResults > Integer.MAX_VALUE) {
               logger.debug(maxResults + " cannot be cast to int without changing its value.");
             }else{ 
               sp.setLimitBy(LimitBy.FINAL_SIZE);
               sp.setLimit((int)maxResults); //not used if search results aren't permission filtered, so set the max items as well
               sp.setMaxItems((int)maxResults);
             }
          }
          searchResults = searchService.query(sp);
          //CAT CHANGE END
          
          return convert(
                  nodeService,
                  dictionaryService,
                  contentService,
                  searchResults);
      }
      finally
      {
          if (searchResults != null)
          {
              try
              {
                  searchResults.close();
              }
              catch (Throwable e)
              {
              }
          }
      }
        //CAT CHANGE END
    }

    
    /**
     * {@inheritDoc}
     */
    public ResultSet execute(ServiceRegistry serviceRegistry)
    {
      SearchService searchService = serviceRegistry.getSearchService();
      NodeService nodeService = serviceRegistry.getNodeService();
      DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
      ContentService contentService = serviceRegistry.getContentService();
      
      // handle the special search string of * meaning, get everything
      String statement = query.getStatement();
      if (statement.equals("*"))
      {
          statement = "ISNODE:*";
      }
      org.alfresco.service.cmr.search.ResultSet searchResults = null;
      try
      {
          StoreRef storeRef = Utils.convertToStoreRef(store);
          searchResults = searchService.query(storeRef, query.getLanguage(), statement);
          return convert(
                  nodeService,
                  dictionaryService,
                  contentService,
                  searchResults);
      }
      finally
      {
          if (searchResults != null)
          {
              try
              {
                  searchResults.close();
              }
              catch (Throwable e)
              {
              }
          }
      }
    }

    private ResultSet convert(
            NodeService nodeService,
            DictionaryService dictionaryService,
            ContentService contentService,
            org.alfresco.service.cmr.search.ResultSet searchResults)
    {
        ResultSet results = new ResultSet();
        List<ResultSetRow> rowsList = new ArrayList<org.alfresco.repo.webservice.types.ResultSetRow>();

        int index = 0;
        for (org.alfresco.service.cmr.search.ResultSetRow searchRow : searchResults)
        {
            NodeRef nodeRef = searchRow.getNodeRef();
            // Search can return nodes that no longer exist, so we need to ignore these
            if (!nodeService.exists(nodeRef))
            {
                continue;
            }
            ResultSetRowNode rowNode = createResultSetRowNode(nodeRef, nodeService);
            
            // get the data for the row and build up the columns structure
            Map<String, Serializable> values = searchRow.getValues();
            NamedValue[] columns = new NamedValue[values.size() + 3];
            int col = 1;
            for (String attributeName : values.keySet())
            {
                columns[col] = Utils.createNamedValue(dictionaryService, QName.createQName(attributeName), values.get(attributeName));
                col++;
            }
            
            // add one extra column for the node's path
            columns[0] = Utils.createNamedValue(dictionaryService, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"), nodeService.getPath(nodeRef).toString());

            // add one extra column for the content size
            long size = 0;
            if(nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
              ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
              if(reader != null) {
                size = reader.getSize();
              }   
            }
            columns[col] = Utils.createNamedValue(
                    dictionaryService,
                    CatConstants.PROP_SIZE,
                    String.valueOf(size));
            col++;
            
            // Add one more column for the number of children
            int childCount = 0;
            if(nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {      
              childCount = nodeService.getChildAssocs(nodeRef).size();
            }
            columns[col] = Utils.createNamedValue(
                    dictionaryService,
                    CatConstants.PROP_CHILD_COUNT,
                    String.valueOf(childCount));
            col++;
            
            
            ResultSetRow row = new org.alfresco.repo.webservice.types.ResultSetRow();
            row.setColumns(columns);
            row.setScore(searchRow.getScore());
            row.setRowIndex(index);
            row.setNode(rowNode);

            // add the row to the overall results list
            rowsList.add(row);
            index++;
        }

        // Convert list to array
        int totalRows = rowsList.size();
        ResultSetRow[] rows = rowsList.toArray(new org.alfresco.repo.webservice.types.ResultSetRow[totalRows]);

        // add the rows to the result set and set the total row count
        results.setRows(rows);
        results.setTotalRowCount(totalRows);

        return results;
    }
    
    /**
     * Create a result set row node object for the provided node reference
     * 
     * @param nodeRef
     *      the node reference
     * @param nodeService
     *      the node service
     * @return
     *      the result set row node
     */
    protected ResultSetRowNode createResultSetRowNode(NodeRef nodeRef, NodeService nodeService)
    {
        // Get the type
        String type = nodeService.getType(nodeRef).toString();

        // Get the aspects applied to the node
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        String[] aspectNames = new String[aspects.size()];
        int index = 0;
        for (QName aspect : aspects)
        {
            aspectNames[index] = aspect.toString();
            index++;
        }

        // Create and return the result set row node
        return new ResultSetRowNode(nodeRef.getId(), type, aspectNames);
    }
}
