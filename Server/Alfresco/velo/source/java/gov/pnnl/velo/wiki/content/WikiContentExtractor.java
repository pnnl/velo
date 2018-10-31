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
package gov.pnnl.velo.wiki.content;

import java.io.File;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Class that can turn an alfresco node into wiki page content.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public interface WikiContentExtractor {

  /**
   * Get the mimetypes that this extractor works with.
   * (Could be more than  one.)
  
   * @return List<String>
   */
  public List<String> getSupportedMimetypes();
  
  /**
   * Create a wiki page describing the given alfresco node.
   * @param alfrescoNode
  
   * @return File
   * @throws Exception
   */
  public File extractWikiContent(NodeRef alfrescoNode) throws Exception;  
}
