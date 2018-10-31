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
package gov.pnnl.cat.search.ui;

/**
 */
public interface ISearchPageContainer {
  /**
   * Sets the enabled status of the search button if the specified page is currently active.
   * @param enabled
   * @param page
   */
  public void setSearchButtonEnabled(boolean enabled, ISearchPage page);

  /**
   * Sets the visibility of the search button if the specified page is currently active.
   * @param visible
   * @param page
   */
  public void setSearchButtonVisible(boolean visible, ISearchPage page);
  
  /**
   * Needs to be called in the UI thread
  
   * @return if this search page's 'clustered' search toggle is enabled  */
  public boolean isClusteredSearchEnabled();
}
