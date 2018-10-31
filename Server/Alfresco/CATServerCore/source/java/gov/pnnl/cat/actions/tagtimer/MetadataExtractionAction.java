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
package gov.pnnl.cat.actions.tagtimer;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.TagTimerConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class MetadataExtractionAction extends ActionExecuterAbstractBase {

	private NodeService nodeService;
	private SearchService searchService;
	private List<String> usPersonDataKeywords;
	private static final Log logger = LogFactory.getLog(MetadataExtractionAction.class);
	
	/**
	 * Set the node service
	 * 
	 * @param nodeService
	 *            set the node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Method setSearchService.
	 * @param searchService SearchService
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	/**
	 * Method setUsPersonDataKeywords.
	 * @param usPersonDataKeywords List<String>
	 */
	public void setUsPersonDataKeywords(List<String> usPersonDataKeywords){
		this.usPersonDataKeywords = usPersonDataKeywords;
	}

	/**
	 * Method executeImpl.
	 * @param action Action
	 * @param actionedUponNodeRef NodeRef
	 */
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		// TODO Auto-generated method stub
		// build the query
        StringBuffer query = new StringBuffer();  
        
		query.append("ID:\"");
		query.append(actionedUponNodeRef.toString());
		query.append("\"");
        query.append(" AND TEXT:(");

        for(int i = 0; i < usPersonDataKeywords.size() - 1; i++){
        	query.append("\"");
        	query.append(usPersonDataKeywords.get(i));
        	query.append("\"");
        	query.append(" OR ");
        }
        
        query.append("\"");
        query.append(usPersonDataKeywords.get(usPersonDataKeywords.size()-1));
        query.append("\"");
        query.append(")");

        if(logger.isDebugEnabled())
          logger.debug("query = " + query.toString());

        long start = System.currentTimeMillis();
        ResultSet result = null;
        try {
          result = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());
          long end = System.currentTimeMillis();
          logger.debug("time to execute query = " + (end - start));

          if(result != null && result.length() > 0){
        	  HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        	  properties.put(TagTimerConstants.PROP_MATERIAL_TYPE, TagTimerConstants.TYPE_INTEL);
        	  nodeService.addAspect(actionedUponNodeRef, TagTimerConstants.ASPECT_US_PRESONS_DATA,properties);
          }
        } finally {
          if(result != null) {
            result.close();
          }
        }
	}

	/**
	 * Method addParameterDefinitions.
	 * @param paramList List<ParameterDefinition>
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub

	}

}
