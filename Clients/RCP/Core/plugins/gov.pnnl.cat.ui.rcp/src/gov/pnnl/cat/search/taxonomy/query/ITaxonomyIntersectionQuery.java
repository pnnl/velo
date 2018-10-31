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
package gov.pnnl.cat.search.taxonomy.query;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;

import java.util.Map;
import java.util.Set;


/**
 * @author d3l028
 * 
 * @version $Revision: 1.0 $
 */
public interface ITaxonomyIntersectionQuery extends ISearchQuery {

  /**
   * Method getTaxonomies.
   * @return Map
   */
  public Map getTaxonomies();

  /**
   * Method setTaxonomies.
   * @param sets Map<CmsPath,Set<IResource>>
   */
  public void setTaxonomies(Map<CmsPath, Set<IResource>> sets);

  /*
   * just returns the info for debugging purposes
   */
  /**
   * Method toString.
   * @return String
   */
  public String toString();
}
