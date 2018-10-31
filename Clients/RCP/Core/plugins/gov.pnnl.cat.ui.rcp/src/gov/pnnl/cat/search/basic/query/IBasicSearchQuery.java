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
package gov.pnnl.cat.search.basic.query;

import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;

/**
 * @author d3l028
 * 
 * @version $Revision: 1.0 $
 */
public interface IBasicSearchQuery extends ISearchQuery {

  /*
   * returns the search string
   */
  /**
   * Method getSearchString.
   * @return String
   */
  public String getSearchString();

  /*
   * builds the search query (ready for SearchManager to run the search on the web service/server) 
   */
  /**
   * Method buildSearchQuery.
   * @return String
   * @throws Exception
   */
  public String buildSearchQuery() throws Exception;

  /**
   * sets the search string
   * NOTE: searchString overwrites any search attributes added previously with addSearchAttribute
  
   *  
   * @param searchString String
   * @see addSearchAttribute */
  public void setSearchString(String searchString);
  
  /*
   * just returns the info for debugging purposes
   */
  /**
   * Method toString.
   * @return String
   */
  public String toString();
}
