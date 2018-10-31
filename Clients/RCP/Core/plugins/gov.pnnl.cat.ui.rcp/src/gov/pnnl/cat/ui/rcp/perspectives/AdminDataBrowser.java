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
package gov.pnnl.cat.ui.rcp.perspectives;

import gov.pnnl.cat.ui.rcp.CatViewIDs;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 */
public class AdminDataBrowser implements IPerspectiveFactory {

  /**
   * Method createInitialLayout.
   * @param layout IPageLayout
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
   */
  public void createInitialLayout(IPageLayout layout) {
    String[] leftSideViewIds = new String[] { 
        CatViewIDs.DATA_INSPECTOR };
    String[] showViewShortcutViewIds = new String[] {
        CatViewIDs.DATA_INSPECTOR, 
        "gov.pnnl.velo.ui.views.SummaryView",
        "gov.pnnl.velo.ui.views.ScratchPadView",
        "gov.pnnl.cat.discussion.view",
        CatViewIDs.PREVIEW
    };
    String bottomViewId = CatViewIDs.PREVIEW;

    CatPerspectiveLayout catLayout = new CatPerspectiveLayout(leftSideViewIds, showViewShortcutViewIds, bottomViewId);
    catLayout.doCatLayout(layout);
  }

}
