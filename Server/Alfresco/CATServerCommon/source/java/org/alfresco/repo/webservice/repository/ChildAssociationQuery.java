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
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.webservice.AbstractQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A query to retrieve all child associations on a node.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class ChildAssociationQuery extends AbstractQuery<ResultSet>
{
    private static final long serialVersionUID = -4965097420552826582L;

    private Reference node;

    /**
     * @param node
     *            The node to query against
     */
    public ChildAssociationQuery(Reference node)
    {
        this.node = node;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ChildAssociationQuery")
          .append("[ node=").append(node.getUuid())
          .append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public ResultSet execute(ServiceRegistry serviceRegistry)
    {
        SearchService searchService = serviceRegistry.getSearchService();
        NodeService nodeService = serviceRegistry.getNodeService();
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        ContentService contentService = serviceRegistry.getContentService();

        // create the node ref and get the children from the repository
        NodeRef nodeRef = Utils.convertToNodeRef(node, nodeService, searchService, namespaceService);
        List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);

        int totalRows = assocRefs.size();

        ResultSet results = new ResultSet();
        ResultSetRow[] rows = new ResultSetRow[totalRows];

        int index = 0;
        for (ChildAssociationRef assocRef : assocRefs)
        {
            NodeRef childNodeRef = assocRef.getChildRef();
            ResultSetRowNode rowNode = createResultSetRowNode(childNodeRef, nodeService);
            
            // create columns for all the properties of the node
            // get the data for the row and build up the columns structure
            Map<QName, Serializable> props = nodeService.getProperties(childNodeRef);
            NamedValue[] columns = new NamedValue[props.size()+7];
            int col = 0;
            for (QName propName : props.keySet())
            {
               columns[col] = Utils.createNamedValue(dictionaryService, propName, props.get(propName));
               col++;
            }
            
            // Now add the system columns containing the association details
            columns[col] = new NamedValue(SYS_COL_ASSOC_TYPE, Boolean.FALSE, assocRef.getTypeQName().toString(), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_ASSOC_NAME, Boolean.FALSE, assocRef.getQName().toString(), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_IS_PRIMARY, Boolean.FALSE, Boolean.toString(assocRef.isPrimary()), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_NTH_SIBLING, Boolean.FALSE, Integer.toString(assocRef.getNthSibling()), null);
            
            // Add one more column for the node's path
            col++;
            columns[col] = Utils.createNamedValue(
                    dictionaryService,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"),
                    nodeService.getPath(childNodeRef).toString());
            
            // Add one more column for the node's size (if it's a content node)
            long size = 0;
            if(nodeService.getType(childNodeRef).equals(ContentModel.TYPE_CONTENT)) {
              ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
              if(reader != null) {
                size = reader.getSize();
              }   
            }
            col++;
            columns[col] = Utils.createNamedValue(
                    dictionaryService,
                    CatConstants.PROP_SIZE,
                    String.valueOf(size));
            
            // Add one more column for the number of children
            int childCount = 0;
            if(nodeService.getType(childNodeRef).equals(ContentModel.TYPE_FOLDER)) {      
              childCount = nodeService.getChildAssocs(childNodeRef).size();
            }
            col++;
            columns[col] = Utils.createNamedValue(
                    dictionaryService,
                    CatConstants.PROP_CHILD_COUNT,
                    String.valueOf(childCount));
            
            ResultSetRow row = new ResultSetRow();
            row.setRowIndex(index);
            row.setNode(rowNode);
            row.setColumns(columns);

            // add the row to the overall results
            rows[index] = row;
            index++;
        }

        // add the rows to the result set and set the total row count
        results.setRows(rows);
        results.setTotalRowCount(totalRows);

        return results;
    }
}
