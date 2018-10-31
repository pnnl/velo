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

import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.CatWizardIDs;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPlaceholderFolderLayout;

/**
 */
public class CatPerspectiveLayout {
  // views that will go on the left side
  private String[] leftSideViewIds = null;

  // place holder of views that will go on the left side
  private String[] leftSidePlaceHolderViewIds = null;

  // views that will go on the right side
  private String[] rightSideViewIds = null;

  // views that will show up in the view shortcut list
  private String[] showViewShortcutViewIds = null;

  // if we need to split left side into 2 views this will not be null (right now only search perspective has this)
  private String[] leftSplitSideViewIds = null;
  
  //new wizards to show on context menu
  private String[] newWizardShortcuts = null;
  
  //perspective shortcuts
  private String[] perspectiveShortcuts = null;
  

  private float leftRatio = LEFT_VALUE;
  private float rightRatio = RIGHT_VALUE;

  private String bottomViewId;

  private IFolderLayout bottomCenter;
  
  public static final float LEFT_VALUE = 0.70f;
  public static final float RIGHT_VALUE = 0.30f;

  /**
   * default is to have FAVORITES_VIEW & PERSONAL_LIBRARY_VIEW as rightSideViewIds and the leftSideViewIds are also the leftSidePlaceHolderViewIds and without splitting the left side
   * 
   * @param leftSideViewIds
   * @param showViewShortcutViewIds
   * @param bottomViewId 
   */
  public CatPerspectiveLayout(String[] leftSideViewIds, String[] showViewShortcutViewIds, String bottomViewId) {
    setViews(leftSideViewIds, showViewShortcutViewIds, leftSideViewIds, null, null, bottomViewId);
  }
  
  public CatPerspectiveLayout(){}


  /**
   * @param leftSideViewIds
   * @param showViewShortcutViewIds
   * @param leftSidePlaceHolderViewIds
   * @param rightSideViewIds
   * @param leftSplitSideViewIds
   */
  public void setViews(String[] leftSideViewIds, String[] showViewShortcutViewIds, String[] leftSidePlaceHolderViewIds, String[] rightSideViewIds, String[] leftSplitSideViewIds, String bottomViewId) {
    this.leftSideViewIds = leftSideViewIds;
    this.leftSidePlaceHolderViewIds = leftSidePlaceHolderViewIds;
    this.rightSideViewIds = rightSideViewIds;
    this.showViewShortcutViewIds = showViewShortcutViewIds;
    this.leftSplitSideViewIds = leftSplitSideViewIds;
    this.bottomViewId = bottomViewId;
  }

  /**
   * gives users ability to reset default values for the proportions.
   * @param left float
   * @param right float
   */
  public void overrideViewRatios(float left, float right) {
    this.leftRatio = left;
    this.rightRatio = right;
  }
  
  
  
  /**
   * will layout the perspective in a 2/3's 1/3 left to right format if only left & right side views exist, otherwise if leftSplitSideViewIds exists, will split the left (2/3's) side into 1/4, 3/4 and the right still gets the 1/3
   * 
   * @param layout
   *          passed from perspectives createInitialLayout method
   */
  public void doCatLayout(IPageLayout layout) {

    // By default, IPageLayout comes with an editor area. Since we don't want
    // it for these perspective, we need to deactivate it
    layout.setEditorAreaVisible(false);

    // We want users to be able to customize this perspective
    layout.setFixed(false);

    // Create a 2/3 width frame for the left column view parts
    String editorArea = layout.getEditorArea();
    IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, this.leftRatio, editorArea);

    // Add the view parts to the left frame
    if (leftSideViewIds != null) {
      for (int i = 0; i < this.leftSideViewIds.length; i++) {
        topLeft.addView(leftSideViewIds[i]);
      }
    }

    // adding placeholders for when user selects 'open in new tab'
    if (leftSidePlaceHolderViewIds != null) {
      for (int i = 0; i < this.leftSidePlaceHolderViewIds.length; i++) {
        topLeft.addPlaceholder(leftSidePlaceHolderViewIds[i] + ":CAT*");
      }
    }

    // create a 1/3 width frame for right column view parts
    if (rightSideViewIds != null && rightSideViewIds.length > 0 ) {
    	IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, this.rightRatio, editorArea);
      for (int i = 0; i < this.rightSideViewIds.length; i++) {
        topRight.addView(rightSideViewIds[i]);
      }
    }

    // only do something if the left side is split
    if (leftSplitSideViewIds != null && leftSplitSideViewIds.length >0) {
      // create a 3/4 width frame for right column (of left side) view parts
      IFolderLayout topCenter = layout.createFolder("topCenter", IPageLayout.RIGHT, 0.46f, "topLeft");
      for (int i = 0; i < this.leftSplitSideViewIds.length; i++) {
        topCenter.addView(leftSplitSideViewIds[i]);
      }
    }

    // just placeholders
    IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, 0.75f, "topLeft");
    bottom.addPlaceholder(CatViewIDs.PROGRESS_MONITOR_VIEW);

    this.bottomCenter = layout.createFolder("bottomCenter", IPageLayout.BOTTOM, 0.67f, "topLeft");
    bottomCenter.addView(bottomViewId);
    
    
    // Add shortcuts to the Window->Show View menu
    if (showViewShortcutViewIds != null) {
      for (int i = 0; i < this.showViewShortcutViewIds.length; i++) {
        layout.addShowViewShortcut(showViewShortcutViewIds[i]);
      }
    }

    // Add shortcuts to the Window->Open Perspective menu
    if(this.perspectiveShortcuts == null){
      //use defaults 
      layout.addPerspectiveShortcut(CatPerspectiveIDs.ADMIN_DATA_BROWSER);
      layout.addPerspectiveShortcut(CatPerspectiveIDs.SEARCH);
      layout.addPerspectiveShortcut(CatPerspectiveIDs.USER_PERSPECTIVE);
      layout.addPerspectiveShortcut(CatPerspectiveIDs.TEAM_PERSPECTIVE);
    }else{
      for (String perspectiveShortcutId : this.perspectiveShortcuts) {
        layout.addPerspectiveShortcut(perspectiveShortcutId);
      }
    }

    // add shortcuts to File->new menu
    if(this.newWizardShortcuts == null){
      layout.addNewWizardShortcut(CatWizardIDs.NEW_FOLDER);
      layout.addNewWizardShortcut(CatWizardIDs.NEW_TAXONOMY);
      layout.addNewWizardShortcut(CatWizardIDs.NEW_PROJECT);
      layout.addNewWizardShortcut(CatWizardIDs.NEW_COMMENT);
    }else{
      for (String newWizardId : this.newWizardShortcuts) {
        layout.addNewWizardShortcut(newWizardId);
      }
    }
  }

  
  
  /**
   * Method setNewWizardShortcuts.
   * @param newWizardShortcuts String[]
   */
  public void setNewWizardShortcuts(String[] newWizardShortcuts) {
    this.newWizardShortcuts = newWizardShortcuts;
  }

  /**
   * Method setPerspectiveShortcuts.
   * @param perspectiveShortcuts String[]
   */
  public void setPerspectiveShortcuts(String[] perspectiveShortcuts) {
    this.perspectiveShortcuts = perspectiveShortcuts;
  }

  /**
   * Method getBottomCenter.
   * @return IFolderLayout
   */
  public IFolderLayout getBottomCenter() {
    return bottomCenter;
  }

}
