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
package gov.pnnl.cat.util;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class TaxonomyUtils {
  protected static Log logger = LogFactory.getLog(TaxonomyUtils.class);
      
  /**
   * Gets the category assigned to the given taxonomy node.
   * There should only be one.
   * @param taxonomyNodeRef NodeRef
   * @param nodeService NodeService
   * @return NodeRef
   */
  public static NodeRef getTaxonomyCategory(NodeRef taxonomyNodeRef, NodeService nodeService) {
    NodeRef category = null;
    try {
      Serializable value = nodeService.getProperty(taxonomyNodeRef, CatConstants.PROP_CATEGORIES);
      if (value != null) {
        Collection<NodeRef> categories = DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, value);

        // Should only ever be one category per taxonomy root
        if (categories.size() == 1) {
          category = categories.iterator().next();

        } else {

          //TODO: add an error log or throw an exception
          logger.warn("Wrong number of categories assigned to taxonomy node. Found " + categories.size()
              + " categories. Should have 1.");
        } 
      } else {
        logger.debug("Taxonomy node has no categories property!");
      }

    } catch (InvalidNodeRefException e){
      // we don't care if the node doesn't exist
    }
    return category;      
    
  }

}
