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

import gov.pnnl.cat.ui.rcp.CatActionIDs;
import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.CatWizardIDs;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 */
public class TeamsPerspective implements IPerspectiveFactory {

  /**
   * Method createInitialLayout.
   * @param layout IPageLayout
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
   */
  public void createInitialLayout(IPageLayout layout) {
    // adding edit menu with copy and paste actions to this perspective
    layout.addActionSet(CatActionIDs.EDIT_MENU_ACTION_SET);
    layout.addActionSet(CatActionIDs.DELETE_ACTION_SET);

    // By default, IPageLayout comes with an editor area. Since we don't want
    // it for these perspective, we need to deactivate it
    layout.setEditorAreaVisible(false);

    // We want users to be able to customize this perspective
    layout.setFixed(false);

    // Create a 2/3 width frame for the left column view parts
    String editorArea = layout.getEditorArea();
    IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, CatPerspectiveLayout.RIGHT_VALUE, editorArea);

    topLeft.addView(CatViewIDs.TEAM);

    // create a 1/3 width frame for right column view parts
    IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, CatPerspectiveLayout.LEFT_VALUE, editorArea);

    topRight.addView(CatViewIDs.TEAM_DETAILS);

    layout.addNewWizardShortcut(CatWizardIDs.NEW_TEAM);
    layout.addNewWizardShortcut(CatWizardIDs.NEW_USER);

    layout.addShowViewShortcut(CatViewIDs.TEAM);
    layout.addShowViewShortcut(CatViewIDs.TEAM_DETAILS);
    
    // Add shortcuts to the Window->Open Perspective menu
    layout.addPerspectiveShortcut(CatPerspectiveIDs.SEARCH);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.USER_PERSPECTIVE);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.TEAM_PERSPECTIVE);
  }

}
