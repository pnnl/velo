package gov.pnnl.cat.ui.rcp.views.databrowser;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.NavigationBar;

public class NavigationView extends ViewPart {

  public static final String ID = "gov.pnnl.cat.ui.rcp.views.databrowser.NavigationView"; //$NON-NLS-1$
  protected NavigationBar navBar;
  
  public NavigationView() {
  }

  /**
   * Create contents of the view part.
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {
    Composite wrapper = new Composite(parent, SWT.NONE);
    final GridLayout gridLayout = new GridLayout(1, true);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    wrapper.setLayout(gridLayout);
    wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    navBar.createControl(wrapper);

    createActions();
    initializeToolBar();
    initializeMenu();
  }

  /**
   * Create the actions.
   */
  private void createActions() {
    // Create the actions
  }

  /**
   * Initialize the toolbar.
   */
  private void initializeToolBar() {
    IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
  }

  /**
   * Initialize the menu.
   */
  private void initializeMenu() {
    IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
  }

  @Override
  public void setFocus() {
    // Set the focus
  }

  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
    navBar = new NavigationBar();
    navBar.init(memento);
    
    IViewReference[] views = site.getPage().getViewReferences();
    ICatExplorerView treeView = null;
    ICatExplorerView tableView = null;

    for (IViewReference viewReference : views) {
      IViewPart view = viewReference.getView(true);
      if(view instanceof ICatExplorerView) {
        ICatExplorerView explorerView = (ICatExplorerView)view;
        
        if(explorerView.getTreeExplorer() != null && explorerView.getTableExplorer() == null) {
          treeView = explorerView;
        } else if(explorerView.getTreeExplorer() == null && explorerView.getTableExplorer() != null) {
          tableView = explorerView;
        }
      }
    }
    navBar.linkExplorers(treeView.getTreeExplorer(), tableView.getTableExplorer());
  }
  
  

}
