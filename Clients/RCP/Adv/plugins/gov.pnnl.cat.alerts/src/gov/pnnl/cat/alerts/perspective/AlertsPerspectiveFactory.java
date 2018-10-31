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
package gov.pnnl.cat.alerts.perspective;

import gov.pnnl.cat.alerts.views.AlertsPreviewPage;
import gov.pnnl.cat.alerts.views.AlertsView;
import gov.pnnl.cat.alerts.views.SubscriptionsView;
import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.CatViewIDs;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 */
public class AlertsPerspectiveFactory implements IPerspectiveFactory {

  /**
   * Method createInitialLayout.
   * @param layout IPageLayout
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
   */
  public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(false);
    layout.setFixed(false);
    layout.addView(SubscriptionsView.ID, IPageLayout.LEFT, 1f, layout.getEditorArea());
    layout.addView(AlertsPreviewPage.ID, IPageLayout.BOTTOM, .6f, SubscriptionsView.ID);
    layout.addView(AlertsView.ID, IPageLayout.RIGHT, .4f, SubscriptionsView.ID);
    IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, .6f, AlertsView.ID);
    folderLayout.addView(CatViewIDs.FAVORITES_VIEW);
    folderLayout.addView(CatViewIDs.PERSONAL_LIBRARY_VIEW);

    // Add view shortcuts
    layout.addShowViewShortcut(SubscriptionsView.ID);
    layout.addShowViewShortcut(AlertsView.ID);
    layout.addShowViewShortcut(AlertsPreviewPage.ID);
    layout.addShowViewShortcut(CatViewIDs.DATA_INSPECTOR);
    layout.addShowViewShortcut(CatViewIDs.FAVORITES_VIEW);
    layout.addShowViewShortcut(CatViewIDs.PERSONAL_LIBRARY_VIEW);

    // Add shortcuts to the Window->Open Perspective menu
    layout.addPerspectiveShortcut(CatPerspectiveIDs.ADMIN_DATA_BROWSER);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.SEARCH);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.USER_PERSPECTIVE);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.TEAM_PERSPECTIVE);
  }

}
