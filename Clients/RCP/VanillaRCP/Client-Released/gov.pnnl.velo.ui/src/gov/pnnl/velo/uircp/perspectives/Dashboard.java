package gov.pnnl.velo.uircp.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.views.databrowser.DataBrowserView;
import gov.pnnl.velo.uircp.views.AboutVelo;
import gov.pnnl.velo.uircp.views.DashboardView;

public class Dashboard implements IPerspectiveFactory {
	public static final String ID = Dashboard.class.getName();

	@Override
  public void createInitialLayout(IPageLayout layout) {

    // no editors on this view
	String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible(false);
    
    // views 
    // whole side
    // For Vanilla Velo RCP
    //layout.addStandaloneView(AboutVelo.ID,  false /* show title */, IPageLayout.LEFT, 1.0f, editorArea);
    
    // For SBRSFA
    layout.addStandaloneView(DashboardView.ID,  false /* show title */, IPageLayout.LEFT, 1.0f, editorArea);
    
    layout.addPerspectiveShortcut(DataBrowserView.ID);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.SEARCH);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.USER_PERSPECTIVE);
    layout.addPerspectiveShortcut(CatPerspectiveIDs.TEAM_PERSPECTIVE);
  }

}
