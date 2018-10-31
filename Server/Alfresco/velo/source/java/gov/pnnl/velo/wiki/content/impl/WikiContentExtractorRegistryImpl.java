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
package gov.pnnl.velo.wiki.content.impl;


import gov.pnnl.velo.wiki.content.WikiContentExtractor;
import gov.pnnl.velo.wiki.content.WikiContentExtractorRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * For resources in Alfresco (files or folders), determine what the 
 * default wiki content should be (which shows metadata about the resource).
 * 
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class WikiContentExtractorRegistryImpl implements WikiContentExtractorRegistry {
  //Logger
  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(WikiContentExtractorRegistryImpl.class);
  
  // map of wiki page extractors
  private Map<String, WikiContentExtractor> mimetypeToWikiContentExtractor = new HashMap<String, WikiContentExtractor>(); 
  

  /**
   * Method registerWikiContentExtractor.
   * @param extractor WikiContentExtractor
   * @see gov.pnnl.velo.wiki.content.WikiContentExtractorRegistry#registerWikiContentExtractor(WikiContentExtractor)
   */
  @Override
  public void registerWikiContentExtractor(WikiContentExtractor extractor) {
    List<String> mimetypes = extractor.getSupportedMimetypes();
    for (String mimetype : mimetypes) {
      mimetypeToWikiContentExtractor.put(mimetype, extractor);      
    }
  }

  /**
   * Method getWikiContentExtractor.
   * @param mimetype String
   * @return WikiContentExtractor
   * @see gov.pnnl.velo.wiki.content.WikiContentExtractorRegistry#getWikiContentExtractor(String)
   */
  @Override
  public WikiContentExtractor getWikiContentExtractor(String mimetype) {
    return mimetypeToWikiContentExtractor.get(mimetype);
  }

  
}
