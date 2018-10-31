package gov.pnnl.velo.dataset.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.perspectives.DataBrowser;
import gov.pnnl.velo.dataset.views.ActionsView;
import gov.pnnl.velo.dataset.views.DatasetNavigator;
import gov.pnnl.velo.dataset.views.StepsToPublish;
import gov.pnnl.velo.ui.views.SummaryView;

public class DatasetEditing implements IPerspectiveFactory {

  public static final String ID = DatasetEditing.class.getName();

  @Override
  public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(true);
    String editorArea = layout.getEditorArea();
    addFastViews(layout);
    addViewShortcuts(layout);
    addPerspectiveShortcuts(layout);

    layout.addStandaloneView("gov.pnnl.velo.dataset.views.NavigationView", false, IPageLayout.TOP, 0.05f, IPageLayout.ID_EDITOR_AREA);

    // views for mashup layout:
    IFolderLayout leftPane = layout.createFolder("left", IPageLayout.LEFT, 0.25f, layout.getEditorArea());
    layout.addStandaloneView(ActionsView.ID, false, IPageLayout.LEFT, 0.09f, "left");
    IFolderLayout rightPane = layout.createFolder("right", IPageLayout.RIGHT, 0.75f, layout.getEditorArea());
    IPlaceholderFolderLayout bottomMiddlePane = layout.createPlaceholderFolder("bottomMiddle", IPageLayout.BOTTOM, 0.55f, layout.getEditorArea());

    leftPane.addView(DatasetNavigator.ID);

    // TODO bottom middle placeholder isn't working :( summary and preview are getting stacked in the leftPane
    bottomMiddlePane.addPlaceholder(SummaryView.ID + ":" + ID);
    bottomMiddlePane.addPlaceholder(CatViewIDs.PREVIEW + ":" + ID);
    rightPane.addView(StepsToPublish.ID);    
        
    // make the views non-closeable
    layout.getViewLayout("gov.pnnl.velo.dataset.views.NavigationView").setCloseable(false);
    layout.getViewLayout(DatasetNavigator.ID).setCloseable(false);
  }
  

  /**
   * Add fast views to the perspective.
   */
  private void addFastViews(IPageLayout layout) {
  }

  /**
   * Add view shortcuts to the perspective.
   */
  private void addViewShortcuts(IPageLayout layout) {
    layout.addShowViewShortcut("gov.pnnl.velo.ui.views.SummaryView");
    layout.addShowViewShortcut("gov.pnnl.cat.ui.rcp.preview");
    layout.addShowViewShortcut("gov.pnnl.cat.discussion.view");
    layout.addShowViewShortcut("gov.pnnl.velo.ui.views.ScratchPadView");
    layout.addShowViewShortcut(DatasetNavigator.ID);
    layout.addShowViewShortcut(StepsToPublish.ID);
  }

  /**
   * Add perspective shortcuts to the perspective.
   */
  private void addPerspectiveShortcuts(IPageLayout layout) {
    layout.addPerspectiveShortcut("gov.pnnl.cat.ui.rcp.perspectives.userperspective");
    layout.addPerspectiveShortcut("gov.pnnl.cat.ui.rcp.perspectives.teamsperspective");
    layout.addPerspectiveShortcut("gov.pnnl.cat.ui.rcp.perspectives.search");
    layout.addPerspectiveShortcut("gov.pnnl.cat.alerts.perspective");
    layout.addPerspectiveShortcut("org.eclipse.rse.ui.view.SystemPerspective");
    layout.addPerspectiveShortcut(DataBrowser.ID);
  }

}
