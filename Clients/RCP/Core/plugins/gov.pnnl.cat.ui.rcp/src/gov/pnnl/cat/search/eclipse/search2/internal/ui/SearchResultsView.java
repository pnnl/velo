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
package gov.pnnl.cat.search.eclipse.search2.internal.ui;

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.ISearchHelpContextIds;
import gov.pnnl.cat.search.eclipse.search.ui.IContextMenuConstants;
import gov.pnnl.cat.search.eclipse.search.ui.IQueryListener;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart;
import gov.pnnl.cat.search.eclipse.search2.internal.ui.text.AnnotationManagers;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 */
public class SearchResultsView extends PageBookView implements ISearchResultViewPart, IQueryListener {
  private static final String MEMENTO_TYPE= "view"; //$NON-NLS-1$
  private HashMap fPartsToPages;
  private HashMap fPagesToParts;
  private HashMap fSearchViewStates;
  private SearchPageRegistry fSearchViewPageService;
  private SearchDropDownAction fSearchesDropDownAction;
  private ISearchResult fCurrentSearch;
  private DummyPart fDefaultPart;
  private SearchAgainAction fSearchAgainAction;
  private CancelSearchAction fCancelAction;

  private IMemento fPageState;

  private SashForm sashForm;


  /**
   * Method createStandardGroups.
   * @param menu IContributionManager
   * @param window IWorkbenchWindow
   */
  public static void createStandardGroups(IContributionManager menu, IWorkbenchWindow window) {
    menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
    menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
    menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
    menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
    menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
    menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
    menu.add(new Separator(IContextMenuConstants.GROUP_REMOVE_MATCHES));
    menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
    menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
    menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
    menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
    if(window != null){
      IContributionItem wizardItem = ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(window);
      MenuManager wizardMenu = new MenuManager("&New", IContextMenuConstants.GROUP_NEW);
      wizardMenu.add(wizardItem);
      menu.add(wizardMenu);
    }
  }

  /**
   */
  class DummyPart implements IWorkbenchPart {
    /**
     * Method addPropertyListener.
     * @param listener IPropertyListener
     * @see org.eclipse.ui.IWorkbenchPart#addPropertyListener(IPropertyListener)
     */
    public void addPropertyListener(IPropertyListener listener) {/*dummy*/}
    /**
     * Method createPartControl.
     * @param parent Composite
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent) {/*dummy*/}
    /**
     * Method dispose.
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {/*dummy*/}
    /**
     * Method getSite.
     * @return IWorkbenchPartSite
     * @see org.eclipse.ui.IWorkbenchPart#getSite()
     */
    public IWorkbenchPartSite getSite() { return null; }
    /**
     * Method getTitle.
     * @return String
     * @see org.eclipse.ui.IWorkbenchPart#getTitle()
     */
    public String getTitle() { return null; }
    /**
     * Method getTitleImage.
     * @return Image
     * @see org.eclipse.ui.IWorkbenchPart#getTitleImage()
     */
    public Image getTitleImage() { return null; }
    /**
     * Method getTitleToolTip.
     * @return String
     * @see org.eclipse.ui.IWorkbenchPart#getTitleToolTip()
     */
    public String getTitleToolTip() { return null; }
    /**
     * Method removePropertyListener.
     * @param listener IPropertyListener
     * @see org.eclipse.ui.IWorkbenchPart#removePropertyListener(IPropertyListener)
     */
    public void removePropertyListener(IPropertyListener listener) {/*dummy*/}
    /**
     * Method setFocus.
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {/*dummy*/}
    /**
     * Method getAdapter.
     * @param adapter Class
     * @return Object
     */
    public Object getAdapter(Class adapter) { return null; }
  }



  /**
   */
  class EmptySearchView extends Page implements ISearchResultPage {
    Control fControl;
    private String fId;

    /**
     * Method createControl.
     * @param parent Composite
     * @see org.eclipse.ui.part.IPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
      fControl= new Tree(parent, SWT.NONE);
      //fControl.setText(SearchMessages.getString("SearchView.empty.message")); //$NON-NLS-1$
    }

    /**
     * Method getControl.
     * @return Control
     * @see org.eclipse.ui.part.IPage#getControl()
     */
    public Control getControl() {
      return fControl;
    }

    /**
     * Method setFocus.
     * @see org.eclipse.ui.part.IPage#setFocus()
     */
    public void setFocus() {
      if (fControl != null)
        fControl.setFocus();
    }

    /* (non-Javadoc)
     * @see org.eclipse.search2.ui.ISearchResultsPage#setInput(org.eclipse.search2.ui.ISearchResult, java.lang.Object)
     */
    public void setInput(ISearchResult search, Object viewState) {
      // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.search2.ui.ISearchResultsPage#setViewPart(org.eclipse.search2.ui.ISearchResultView)
     */
    /**
     * Method setViewPart.
     * @param part ISearchResultViewPart
     * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage#setViewPart(ISearchResultViewPart)
     */
    public void setViewPart(ISearchResultViewPart part) {
      // do nothing
    }

    /**
     * Method getUIState.
     * @return Object
     * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage#getUIState()
     */
    public Object getUIState() {
      // empty implementation
      return null;
    }


    /**
     * Method init.
     * @param pageSite IPageSite
     * @see org.eclipse.ui.part.IPageBookViewPage#init(IPageSite)
     */
    public void init(IPageSite pageSite) {
      super.init(pageSite);
      getSite().setSelectionProvider(null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResultPage#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
      // do nothing

    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResultPage#restoreState(org.eclipse.ui.IMemento)
     */
    public void restoreState(IMemento memento) {
      // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResultPage#setID(java.lang.String)
     */
    public void setID(String id) {
      fId= id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResultPage#getID()
     */
    public String getID() {
      return fId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResultPage#getLabel()
     */
    public String getLabel() {
      return ""; //$NON-NLS-1$
    }

    /**
     * Method getViewer.
     * @return ContentViewer
     * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultPage#getViewer()
     */
    public ContentViewer getViewer() {
      return null;
    }
  }

  public SearchResultsView() {
    super();
    fPartsToPages= new HashMap();
    fPagesToParts= new HashMap();
    // Doh they hardcoded the extension point ID here
    fSearchViewPageService= new SearchPageRegistry("gov.pnnl.cat.ui.rcp.searchResultViewPages", "searchResultClass", "id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    fSearchViewStates= new HashMap();
  }

  /**
   * Method createDefaultPage.
   * @param book PageBook
   * @return IPage
   */
  protected IPage createDefaultPage(PageBook book) {
    IPageBookViewPage page= new EmptySearchView();
    page.createControl(book);
    initPage(page);
    DummyPart part= new DummyPart();
    fPartsToPages.put(part, page);
    fPagesToParts.put(page, part);
    fDefaultPart= part;
    return page;
  }

  /**
   * Method doCreatePage.
   * @param part IWorkbenchPart
   * @return PageRec
   */
  protected PageRec doCreatePage(IWorkbenchPart part) {
    IPageBookViewPage page = (IPageBookViewPage) fPartsToPages.get(part);
    initPage(page);
    page.createControl(getPageBook());
    PageRec rec = new PageRec(part, page);
    return rec;
  }

  /**
   * Method doDestroyPage.
   * @param part IWorkbenchPart
   * @param pageRecord PageRec
   */
  protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
    IPage page = pageRecord.page;
    page.dispose();
    pageRecord.dispose();

    // empty cross-reference cache
    fPartsToPages.remove(part);
  }

  /**
   * Method getBootstrapPart.
   * @return IWorkbenchPart
   */
  protected IWorkbenchPart getBootstrapPart() {
    return null;
  }

  /**
   * Method isImportant.
   * @param part IWorkbenchPart
   * @return boolean
   */
  protected boolean isImportant(IWorkbenchPart part) {
    return part instanceof DummyPart;
  }

  /**
   * Method showSearchResult.
   * @param search ISearchResult
   */
  public void showSearchResult(ISearchResult search) {
    if (search != null) {
      InternalSearchUI.getInstance().getSearchManager().touch(search.getQuery());
    }
    ISearchResultPage page= null;
    if (search != null) {
      page= fSearchViewPageService.getExtensionObject(search, ISearchResultPage.class);
      if (page == null) {
        String format= SearchMessages.SearchView_error_noResultPage; 
        String message= MessageFormat.format(format, new Object[] { search.getClass().getName() });
        CatSearchPlugin.log(new Status(IStatus.ERROR, CatSearchPlugin.getID(), 0, message, null));
        return;
      }
    }

    // detach the previous page.
    ISearchResultPage currentPage= (ISearchResultPage) getCurrentPage();
    Object uiState= currentPage.getUIState();
    if (fCurrentSearch != null) {
      if (uiState != null)
        fSearchViewStates.put(fCurrentSearch, uiState);
    }
    currentPage.setInput(null, null);

    // switch to a new page
    if (page != null && page != currentPage) {
      IWorkbenchPart part= (IWorkbenchPart) fPagesToParts.get(page);
      if (part == null) {
        part= new DummyPart();
        fPagesToParts.put(page, part);
        fPartsToPages.put(part, page);
        page.setViewPart(this);
      }
      partActivated(part);
    }

    // connect to the new pages
    fCurrentSearch= search;
    if (page != null) {
      page.setInput(search, fSearchViewStates.get(search));
    }
    updateLabel();
    updateCancelAction();
  }

  /**
   * Method updateLabel.
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart#updateLabel()
   */
  public void updateLabel() {
    ISearchResultPage page= getActivePage();
    String label= ""; //$NON-NLS-1$
    if (page != null) {
      label= page.getLabel();
    }
    setContentDescription(label);
  }

  /**
   * Method getCurrentSearchResult.
   * @return ISearchResult
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart#getCurrentSearchResult()
   */
  public ISearchResult getCurrentSearchResult() {
    return fCurrentSearch;
  }

  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);

    createActions();
    initializeToolBar();
    InternalSearchUI.getInstance().getSearchManager().addQueryListener(this);

    /*
     * Register help.
     * 
     * XXX: This is not dynamic, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=99120
     */ 
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ISearchHelpContextIds.New_SEARCH_VIEW);
  }

  private void initializeToolBar() {
    IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
    createStandardGroups(tbm, getSite().getWorkbenchWindow());
    tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fCancelAction); //$NON-NLS-1$
    tbm.appendToGroup(IContextMenuConstants.GROUP_SEARCH, fSearchesDropDownAction); //$NON-NLS-1$
    getViewSite().getActionBars().updateActionBars();
  }

  private void createActions() {
    fSearchesDropDownAction= new SearchDropDownAction(this);
    fSearchesDropDownAction.setEnabled(InternalSearchUI.getInstance().getSearchManager().getQueries().length != 0);
    fSearchAgainAction= new SearchAgainAction(this);
    // hackery to get the shortcut to show up
    fSearchAgainAction.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
    fCancelAction= new CancelSearchAction(this);
    fCancelAction.setEnabled(false);
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    InternalSearchUI.getInstance().getSearchManager().removeQueryListener(this);
    AnnotationManagers.searchResultActivated(getSite().getWorkbenchWindow(), null);
    super.dispose();
  }

  /**
   * Method queryStarting.
   * @param query ISearchQuery
   * @see gov.pnnl.cat.search.eclipse.search.ui.IQueryListener#queryStarting(ISearchQuery)
   */
  public void queryStarting(ISearchQuery query) {
    updateCancelAction();
  }

  /**
   * Method queryFinished.
   * @param query ISearchQuery
   * @see gov.pnnl.cat.search.eclipse.search.ui.IQueryListener#queryFinished(ISearchQuery)
   */
  public void queryFinished(ISearchQuery query) {
    updateCancelAction();
  }

  private void updateCancelAction() {
    ISearchResult result= getCurrentSearchResult();
    boolean queryRunning= false;
    if (result != null) {
      queryRunning= InternalSearchUI.getInstance().isQueryRunning(result.getQuery());
    }
    fCancelAction.setEnabled(queryRunning);
  }

  /**
   * Method queryAdded.
   * @param query ISearchQuery
   * @see gov.pnnl.cat.search.eclipse.search.ui.IQueryListener#queryAdded(ISearchQuery)
   */
  public void queryAdded(ISearchQuery query) {
    showSearchResult(query.getSearchResult());
    fSearchesDropDownAction.setEnabled(InternalSearchUI.getInstance().getSearchManager().getQueries().length != 0);
  }

  /**
   * Method queryRemoved.
   * @param query ISearchQuery
   * @see gov.pnnl.cat.search.eclipse.search.ui.IQueryListener#queryRemoved(ISearchQuery)
   */
  public void queryRemoved(ISearchQuery query) {
    InternalSearchUI.getInstance().cancelSearch(query);
    if (query.getSearchResult().equals(fCurrentSearch)) {
      showSearchResult(null);
      partActivated(fDefaultPart);
    }
    fSearchViewStates.remove(query.getSearchResult());
    fSearchesDropDownAction.disposeMenu();
    fSearchesDropDownAction.setEnabled(InternalSearchUI.getInstance().getSearchManager().getQueries().length != 0);
  }

  /**
   * Method fillContextMenu.
   * @param manager IMenuManager
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart#fillContextMenu(IMenuManager)
   */
  public void fillContextMenu(IMenuManager manager) {
  }


  // Methods related to saving page state. -------------------------------------------

  /**
   * Method saveState.
   * @param memento IMemento
   * @see org.eclipse.ui.IViewPart#saveState(IMemento)
   */
  public void saveState(IMemento memento) {
    for (Iterator pages = fPagesToParts.keySet().iterator(); pages.hasNext(); ) {
      ISearchResultPage page = (ISearchResultPage) pages.next();
      IMemento child= memento.createChild(MEMENTO_TYPE, page.getID()); //$NON-NLS-1$
      page.saveState(child);
    }
  }


  /**
   * Method init.
   * @param site IViewSite
   * @param memento IMemento
   * @throws PartInitException
   * @see org.eclipse.ui.IViewPart#init(IViewSite, IMemento)
   */
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
    createStandardGroups(site.getActionBars().getMenuManager(), site.getWorkbenchWindow());
    fPageState= memento;
    IWorkbenchSiteProgressService progressService= getProgressService();
    if (progressService != null)
      progressService.showBusyForFamily(InternalSearchUI.FAMILY_SEARCH);
  }


  /**
   * Method initPage.
   * @param page IPageBookViewPage
   */
  protected void initPage(IPageBookViewPage page) {
    super.initPage(page);
    page.getSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), fSearchAgainAction);
    page.getSite().getActionBars().updateActionBars();

    ISearchResultPage srPage= (ISearchResultPage) page;
    IMemento memento= null;
    if (fPageState != null) {
      IMemento[] mementos= fPageState.getChildren(MEMENTO_TYPE);
      for (int i= 0; i < mementos.length; i++) {
        if (mementos[i].getID().equals(srPage.getID())) {
          memento= mementos[i];
          break;
        }
      }
    }
    srPage.restoreState(memento);
  }

  /*
   *  TODO workaround for focus problem. Clarify focus behaviour.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    IPage currentPage= getCurrentPage();
    if (currentPage != null)
      currentPage.setFocus();
    else 
      super.setFocus();
  }

  /**
   * Method getActivePage.
   * @return ISearchResultPage
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart#getActivePage()
   */
  public ISearchResultPage getActivePage() {
    IPage page= getCurrentPage();
    if (page instanceof ISearchResultPage)
      return (ISearchResultPage) page;
    return null;
  }

  /**
   * Method getProgressService.
   * @return IWorkbenchSiteProgressService
   */
  public IWorkbenchSiteProgressService getProgressService() {
    IWorkbenchSiteProgressService service = null;
    Object siteService =
        getSite().getAdapter(IWorkbenchSiteProgressService.class);
    if(siteService != null)
      service = (IWorkbenchSiteProgressService) siteService;
    return service;
  }

  /**
   * Method showBusy.
   * @param busy boolean
   */
  public void showBusy(boolean busy) {
    super.showBusy(busy);
    getProgressService().warnOfContentChange();
  }

}
