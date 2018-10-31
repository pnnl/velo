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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;

/**
 * Interface to be implemented by contributors to the extension point <code>org.eclipse.search.cueSearchPages</code>.
 * Represents a page in the search view.
 * <p>
 * The search dialog calls the <code>performAction</code> method when the Search
 * button is pressed.
 * <p>
 *
 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPage
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public interface ISearchPage {

  /**
   * Performs the action for this page.
   * The search view calls this method when the Search
   * button is pressed.
   */
  public void performAction();

  /**
   * Allows the search page to perform any initialization that might be
   * necessary, and gives it access to the viewpart that it will be in.
   * This method is called immediately before createSearchPage().
   * Currently, this method can be called multiple times per instance,
   * but ultimately this method should only ever be called once per instance.
   * 
   * @param viewpart
   * @param container ISearchPageContainer
   */
  public void init(IViewPart viewpart, ISearchPageContainer container);

  /**
   * Sets the container of this page.
   * The search dialog calls this method to initialize this page.
   * Implementations may store the reference to the container.
   *
  
   * @param composite Composite
   * @return Composite
   */
  public Composite createSearchPage(Composite composite);

  /**
   * This method is called just before the page is displayed to the user.
   */
  public void aboutToShow();

  /**
   * This method is called just before the page is hidden from the user.
   */
  public void aboutToHide();

  public void dispose();
}
