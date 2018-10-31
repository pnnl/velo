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

import org.eclipse.ui.IViewPart;


/**
 */
public abstract class SearchPage implements ISearchPage {

  protected ISearchPageContainer container;
  protected IViewPart viewPart;
  /**
   * Method performAction.
   * @see gov.pnnl.cat.search.ui.ISearchPage#performAction()
   */
  public void performAction() {
    // intentionally left empty
  }

  /**
   * Method aboutToShow.
   * @see gov.pnnl.cat.search.ui.ISearchPage#aboutToShow()
   */
  public void aboutToShow() {
    // intentionally left empty
  }

  /**
   * Method init.
   * @param viewPart IViewPart
   * @param container ISearchPageContainer
   * @see gov.pnnl.cat.search.ui.ISearchPage#init(IViewPart, ISearchPageContainer)
   */
  public void init(IViewPart viewPart, ISearchPageContainer container) {
    this.viewPart = viewPart;
      this.container = container;
  }

  /**
   * Method aboutToHide.
   * @see gov.pnnl.cat.search.ui.ISearchPage#aboutToHide()
   */
  public void aboutToHide() {
    // intentionally left empty
  }

  /**
   * Method dispose.
   * @see gov.pnnl.cat.search.ui.ISearchPage#dispose()
   */
  public void dispose() {
    // intentionally left empty
  }

}
