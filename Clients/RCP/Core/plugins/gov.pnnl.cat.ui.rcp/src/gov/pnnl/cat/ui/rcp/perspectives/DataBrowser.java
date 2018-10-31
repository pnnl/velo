package gov.pnnl.cat.ui.rcp.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import gov.pnnl.cat.ui.rcp.views.databrowser.ActionsView;

import org.eclipse.ui.IFolderLayout;

public class DataBrowser implements IPerspectiveFactory {
  public static final String ID = DataBrowser.class.getName();

  /**
   * Creates the initial layout for a page.
   */
  public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(false);
    String editorArea = layout.getEditorArea();
    addFastViews(layout);
    addViewShortcuts(layout);
    addPerspectiveShortcuts(layout);
    layout.addStandaloneView("gov.pnnl.cat.ui.rcp.views.databrowser.NavigationView", false, IPageLayout.TOP, 0.05f, IPageLayout.ID_EDITOR_AREA);
    layout.addView("gov.pnnl.cat.ui.rcp.views.databrowser.DataBrowserView", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
    {
      IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, 0.38f, "gov.pnnl.cat.ui.rcp.views.databrowser.DataBrowserView");
      folderLayout.addView("gov.pnnl.cat.ui.rcp.views.TableExplorerView");
      folderLayout.addView("gov.pnnl.cat.search.ui.SearchResultsView");
    }
    {
      IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.68f, "gov.pnnl.cat.ui.rcp.views.TableExplorerView");
      folderLayout.addView("gov.pnnl.velo.ui.views.SummaryView");
      folderLayout.addView("gov.pnnl.cat.ui.rcp.views.ResourceWebBrowser");
    }
    layout.addStandaloneView(ActionsView.ID, false, IPageLayout.LEFT, 0.06f, "gov.pnnl.cat.ui.rcp.views.databrowser.DataBrowserView");
    layout.addView("gov.pnnl.cat.ui.rcp.preview", IPageLayout.BOTTOM, 0.68f, "gov.pnnl.cat.ui.rcp.views.databrowser.DataBrowserView");
    
    // make the views non-closeable
    layout.getViewLayout("gov.pnnl.cat.ui.rcp.views.databrowser.NavigationView").setCloseable(false);
    layout.getViewLayout("gov.pnnl.cat.ui.rcp.views.databrowser.DataBrowserView").setCloseable(false);
    layout.getViewLayout("gov.pnnl.cat.ui.rcp.views.TableExplorerView").setCloseable(false);
    layout.getViewLayout(ActionsView.ID).setCloseable(false);
  }

  /**
   * Add fast views to the perspective.
   */
  private void addFastViews(IPageLayout layout) {
    layout.addFastView("gov.pnnl.cat.discussion.view");
    layout.addFastView("gov.pnnl.velo.ui.views.ScratchPadView");
    layout.addFastView("gov.pnnl.cat.ui.rcp.views.ResourceWebBrowser");
  }

  /**
   * Add view shortcuts to the perspective.
   */
  private void addViewShortcuts(IPageLayout layout) {
    layout.addShowViewShortcut("gov.pnnl.cat.ui.rcp.views.databrowser.DataBrowserView");
    layout.addShowViewShortcut("gov.pnnl.cat.ui.rcp.views.TableExplorerView");
    layout.addShowViewShortcut("gov.pnnl.velo.ui.views.SummaryView");
    layout.addShowViewShortcut("gov.pnnl.cat.ui.rcp.preview");
    layout.addShowViewShortcut("gov.pnnl.cat.discussion.view");
    layout.addShowViewShortcut("gov.pnnl.velo.ui.views.ScratchPadView");
    layout.addShowViewShortcut("gov.pnnl.cat.ui.rcp.views.ResourceWebBrowser");
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
  }

}
