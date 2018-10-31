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

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class SearchViewDesigner extends ViewPart implements ISearchPageContainer {

  public static final String ID = "%VIEW_ID%"; //$NON-NLS-1$

  private Map availablePages = new HashMap();

  private Map actions = new HashMap();

  private Composite searchPageComposite;

  private StackLayout stackLayout;

  private Composite viewContainer;

  private ISearchPage currentSearchPage;
  
  private PerspectiveAdapter perspectiveAdapter;
  private Button searchButton;
  private Logger logger = CatLogger.getLogger(SearchViewDesigner.class);
  private boolean savedIsClustered = false;
  
  // This is the easy way to turn off page caching per perspective
  // Ultimately we want to remove perspective ID from the extension point
  // descriptor and clean this code up accordingly
  private static final String ALL_PERSPECTIVES = "ALL";

  /**
   * Create contents of the view part
   * @param parent
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
	GridLayout layout = new GridLayout();
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	
	parent.setLayout(layout);
	parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	
    final ScrolledComposite sc1 = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    this.viewContainer = new Composite(sc1, SWT.NONE);
    viewContainer.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    
    sc1.setContent(this.viewContainer);
    sc1.setExpandVertical(true);
    sc1.setExpandHorizontal(true);
    sc1.setMinSize(viewContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    sc1.getVerticalBar().setIncrement(sc1.getVerticalBar().getIncrement()*5);

    sc1.setLayout(layout);
    sc1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    viewContainer.addControlListener(new ControlAdapter() {
      public void controlResized(ControlEvent event) {
        sc1.setMinSize(viewContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        viewContainer.layout();

      }
    });

    final GridLayout gridLayout = new GridLayout();

    final GridData gridData_1 = new GridData(SWT.FILL, SWT.FILL, true, true);
    // create the composite which will hold the pages provided other plugins
    searchPageComposite = new Composite(this.viewContainer, SWT.NONE);
    searchPageComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    this.searchPageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    this.viewContainer.setLayoutData(gridData_1);
    this.stackLayout = new DynamicStackLayout();
    this.searchPageComposite.setLayout(stackLayout);

    searchPageComposite.addControlListener(new ControlAdapter() {
      public void controlResized(ControlEvent event) {
        sc1.setMinSize(viewContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        searchPageComposite.layout();

      }
    });

    // create the search button
//    searchButton = new Button(this.viewContainer, SWT.NONE);
    searchButton = new Button(parent, SWT.NONE);
    searchButton.setText("Search");
    searchButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (currentSearchPage != null) {
          currentSearchPage.performAction();
        }
      }
    });
    final GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    searchButton.setLayoutData(gridData);

    this.viewContainer.setLayout(gridLayout);
    this.viewContainer.setSize(viewContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    this.viewContainer.getShell().setDefaultButton(searchButton);
    viewContainer.layout();
    String openPerspective = getOpenPerspectiveID();
    initialize();
    contributeToActionBars(openPerspective);
    showDefaultSearchPage(openPerspective);

    // Curt requested that we get rid of the clustered search button
    // on 2009-01-15

//    //create an icon on the search viewer toolbar as clustered search toggle  
//    Bundle bundle = Platform.getBundle("gov.pnnl.cat.search");
//    URL url = FileLocator.find(bundle, new Path("icons/cubes.gif"), null);
//    ImageDescriptor desc = ImageDescriptor.createFromURL(url);
//    ToggleAction toggleIcon = new ToggleAction("Clustered Search", desc);
//    getViewSite().getActionBars().getToolBarManager().add(toggleIcon);
//    
//    //set the clusteredSearch toggle to the saved state 
//    toggleIcon.setChecked(savedIsClustered);

    //calling setFocus() here in order to get the down arrow drop down menu - otherwise
    //it doesn't appear until the search view is clicked on
    setFocus();

  }

  /**
   */
  class ToggleAction extends Action {
    /**
     * Constructor for ToggleAction.
     * @param text String
     * @param desc ImageDescriptor
     */
    public ToggleAction(String text, ImageDescriptor desc){
      super(text, desc);
      setId("gov.pnnl.cat.search.clusteredOption");
    }
  }


  /**
   * Displays the default search page for the specified perspective ID, or a
   * blank page if no search page was provided.
   * 
   * @param perspectiveID
   */
  private void showDefaultSearchPage(String perspectiveID) {
    List list = (List) this.availablePages.get(perspectiveID);
    CatSearchPageDescriptor page;

    if (list == null || list.size() == 0) {
      // TODO show blank page
      this.viewContainer.setVisible(false);
    } else {

      // since the list has already been sorted, we can just ask for the first element in it.
      page = (CatSearchPageDescriptor) list.get(0);

      showPage(page);
    }

  }

  /**
   * Method contributeToActionBars.
   * @param perspectiveID String
   */
  private void contributeToActionBars(String perspectiveID) {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager(), perspectiveID);
    bars.updateActionBars();
  }

  /**
   * Method fillLocalPullDown.
   * @param manager IMenuManager
   * @param perspectiveID String
   */
  private void fillLocalPullDown(IMenuManager manager, String perspectiveID) {
    manager.removeAll();
    if (this.actions == null || this.actions.get(perspectiveID) == null) {
      return;
    }

    // add each action for this perspective ID to the pull down menu
    for (Iterator iter = ((List) this.actions.get(perspectiveID)).iterator(); iter.hasNext();) {
      Action action = (Action) iter.next();
      manager.add(action);
    }
  }

  /**
   * Method makeActions.
   * @param pageDescriptors List
   * @param perspectiveID String
   */
  private void makeActions(List pageDescriptors, String perspectiveID) {
    Action curAction;
    List actionList = new LinkedList();

    if (pageDescriptors == null) {
      return;
    }

    if (this.actions.containsKey(perspectiveID)) {
      return;
    }

    for (Iterator iter = pageDescriptors.iterator(); iter.hasNext();) {
      final CatSearchPageDescriptor descriptor = (CatSearchPageDescriptor) iter.next();

      curAction = new UpdateSearchPageAction(descriptor.getLabel(), descriptor);
      actionList.add(curAction);
    }

    this.actions.put(perspectiveID, actionList);
  }

  /**
   * Gets the CUESearchPageDescriptors that are contributing to this extension
   * point and stores them in a Map by perspective ID.
   * 
   */
  private void initialize() {
    List<CatSearchPageDescriptor> cueSearchPageDescriptors = CatRcpPlugin.getDefault().getSearchPlugin().getCatSearchPageDescriptors();
    List list;
    String perspectiveID;
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

    // iterate over all of the CUE search page descriptors, adding them to a map
    // accoring to their perspective ID.
    for (Iterator iter = cueSearchPageDescriptors.iterator(); iter.hasNext();) {
      CatSearchPageDescriptor descriptor = (CatSearchPageDescriptor) iter.next();

      // get the perspective ID for the current descriptor
      //perspectiveID = descriptor.getPerspectiveID();
      perspectiveID = ALL_PERSPECTIVES;

      // add this descriptor to the map according to the perspective ID
      if (this.availablePages.containsKey(perspectiveID)) {
        ((List) this.availablePages.get(perspectiveID)).add(descriptor);
      } else {
        list = new LinkedList();
        list.add(descriptor);
        this.availablePages.put(perspectiveID, list);
      }
    }
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    //window.removePerspectiveListener(perspectiveAdapter);

    // dispose of all of the descriptors
    for (Iterator iter = availablePages.values().iterator(); iter.hasNext();) {
      List descriptorList = (List) iter.next();
      for (Iterator iterator = descriptorList.iterator(); iterator.hasNext();) {
        CatSearchPageDescriptor descriptor = (CatSearchPageDescriptor) iterator.next();
        descriptor.dispose();
      }
      descriptorList.clear();
    }
    //this.currentSearchPage.dispose();
  }

  /**
   * Method getOpenPerspectiveID.
   * @return String
   */
  private String getOpenPerspectiveID() {
    return ALL_PERSPECTIVES;
    //return getSite().getPage().getPerspective().getId();
  }

  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    String curOpenPerspectiveID = getOpenPerspectiveID();
//  System.out.println(curOpenPerspectiveID);
    makeActions((List) this.availablePages.get(curOpenPerspectiveID), curOpenPerspectiveID);
    contributeToActionBars(curOpenPerspectiveID);


  }

  /**
   * Method showPage.
   * @param pageDescriptor CatSearchPageDescriptor
   */
  private void showPage(CatSearchPageDescriptor pageDescriptor) {
    ISearchPage searchPage;
    if (!this.viewContainer.isVisible()) {
      this.viewContainer.setVisible(true);
    }
    //fix for description being too close to left edge 
    this.setContentDescription(" "+ pageDescriptor.getLabel());

    try {
      // create the page from the descriptor
      searchPage = pageDescriptor.createObject();

      // if this is the same page that we are already displaying, just return
      if (searchPage.equals(this.currentSearchPage)) {
        return;
      }

      // tell the old page that we are hiding him
      if (this.currentSearchPage != null) {
        this.currentSearchPage.aboutToHide();
      }

      this.currentSearchPage = searchPage;

      // reset the search button to its default state.
      setSearchButtonVisible(true, currentSearchPage);
      setSearchButtonEnabled(true, currentSearchPage);

      // create the composite for the new page
      // TODO consider moving the burden of caching the composite
      //      from the clients to this class
      searchPage.init(this, this);
      Composite c = searchPage.createSearchPage(this.searchPageComposite);

      // tell the new page that he is about to be shown
      searchPage.aboutToShow();

      // update the stack layout to display the new composite
      this.stackLayout.topControl = c;

      this.searchPageComposite.layout();

      // layout the container again so that the 'Search' button moves to
      // the correct location
      this.viewContainer.layout(true);

    } catch (CoreException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }

  }

  /**
   */
  private class UpdateSearchPageAction extends Action {
    private CatSearchPageDescriptor pageDescriptor;

    /**
     * Constructor for UpdateSearchPageAction.
     * @param label String
     * @param pageDescriptor CatSearchPageDescriptor
     */
    public UpdateSearchPageAction(String label, CatSearchPageDescriptor pageDescriptor) {
      super(label, pageDescriptor.getImage());
      this.pageDescriptor = pageDescriptor;
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      showPage(this.pageDescriptor);
    }
  }

  /**
   * Method isActive.
   * @param page ISearchPage
   * @return boolean
   */
  private boolean isActive(ISearchPage page) {
    return currentSearchPage != null && currentSearchPage.equals(page);
  }

  /**
   * Method setSearchButtonEnabled.
   * @param enabled boolean
   * @param page ISearchPage
   * @see gov.pnnl.cat.search.ui.ISearchPageContainer#setSearchButtonEnabled(boolean, ISearchPage)
   */
  public void setSearchButtonEnabled(boolean enabled, ISearchPage page) {
    if (isActive(page)) {
      searchButton.setEnabled(enabled);
    }
  }

  /**
   * Method setSearchButtonVisible.
   * @param visible boolean
   * @param page ISearchPage
   * @see gov.pnnl.cat.search.ui.ISearchPageContainer#setSearchButtonVisible(boolean, ISearchPage)
   */
  public void setSearchButtonVisible(boolean visible, ISearchPage page) {
    if (isActive(page)) {
      searchButton.setVisible(visible);
    }
  }

  /**
   * Method isClusteredSearchEnabled.
   * @return boolean
   * @see gov.pnnl.cat.search.ui.ISearchPageContainer#isClusteredSearchEnabled()
   */
  public boolean isClusteredSearchEnabled() {
//    IActionBars bars = getViewSite().getActionBars();
//    ActionContributionItem clusteredToggle = (ActionContributionItem)bars.getToolBarManager().find("gov.pnnl.cat.search.clusteredOption");
//    if(clusteredToggle != null){
//      return clusteredToggle.getAction().isChecked();
//    }
    return false;
  }

//  //initialize clustered search toggle state from Memento
//  public void init(IViewSite site, IMemento memento) throws PartInitException{
//    super.init(site, memento);
//
//    savedIsClustered = false;
//
//    // Restore the previous state.
//    if (memento != null && memento.getChild("clusteredSearchToggle") != null)
//    {
//      String clustered = memento.getChild("clusteredSearchToggle").getID();
//      if(clustered.equals("YES"))
//      {
//        savedIsClustered = true;
//      }
//    }
//  }

//  public void saveState(IMemento memento) {
//    super.saveState(memento);
//    memento.createChild("clusteredSearchToggle", isClusteredSearchEnabled()? "YES" : "NO");
//  }

}
