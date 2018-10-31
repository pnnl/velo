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
package org.alfresco.repo.webservice.repository;
import org.alfresco.repo.service.ServiceDescriptorRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Overload methods as needed to use unfiltered (i.e., no wrapping security interceptor) beans
 *
 */
public class UnfilteredServiceDescriptorRegistry extends ServiceDescriptorRegistry {

  static final QName UNFILTERED_NODE_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "nodeService");
  static final QName UNFILTERED_SEARCH_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "searchService");
  static final QName UNFILTERED_DICTIONARY_SERVICE = QName.createQName(NamespaceService.ALFRESCO_URI, "dictionaryService");
  
  
  @Override
  public NodeService getNodeService() {
    return (NodeService)getService(UNFILTERED_NODE_SERVICE);
  }


  @Override
  public DictionaryService getDictionaryService() {
    return (DictionaryService)getService(UNFILTERED_DICTIONARY_SERVICE);
  }


  @Override
  public SearchService getSearchService() {
   return (SearchService)getService(UNFILTERED_SEARCH_SERVICE);
  }
  
  

}
