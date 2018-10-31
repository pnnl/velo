package gov.pnnl.velo.ui.views.facetedsearch;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.search.basic.query.IBasicSearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.search.ui.ISearchPage;
import gov.pnnl.cat.search.ui.ISearchPageContainer;
import gov.pnnl.velo.VeloPlugin;
import gov.pnnl.velo.model.CmsPath;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.javawiki.calendar.CalendarDialog;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

//TODO add as a resource listener and if no facets are selected when an event comes in, re-initialize the view's facets by calling initFacets
public abstract class AbstractFacetedSearchCriteriaView extends ViewPart implements ISearchPage, FacetedSearchResultListener, IResourceEventListener{

  protected FormToolkit toolkit;

  protected ScrolledForm form;

  protected ArrayList<IResource> searchResults = new ArrayList<IResource>();

    
  protected HashMap<String, List> facetNameToUIList = new HashMap<String, List>();

  protected HashMap<String, WritableList> facetNameToModelList = new HashMap<String, WritableList>();

  protected HashMap<String, ArrayList<String>> seletedFacets = new HashMap<String, ArrayList<String>>();

  protected HashMap<String, HashMap<String, Integer>> initialFacetItems;
  
  protected Composite searchPageComposite;

  protected Text fromDateText;

  protected Text toDateText;

  final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

  protected ICatQueryResult catQueryResult;
  

  public AbstractFacetedSearchCriteriaView() {
  }

  @Override
  public void init(IViewSite site) throws PartInitException {
    super.init(site);
    // register for resource change listener
    ResourcesPlugin.getResourceManager().addResourceEventListener(this);
  }
  
  @Override
  public void dispose(){
    super.dispose();
  }
  
  public abstract CmsPath getDatasetParentPath();
  
  @Override
  public void onEvent(IBatchNotification events) {
    
    //only reload if no facets are selected, don't want to reset things once a search has been done and the user is browsing the results
    if(seletedFacets.size() == 0){
      boolean reload = false;
      String datasetsFolder = getDatasetParentPath().toPrefixString(); //PPConstants.PATH_DATASETS.toPrefixString();
      
      for (IResourceEvent event : events) {
        CmsPath path = event.getPath();
        String pathStr = path.toPrefixString();
        if(pathStr.startsWith(datasetsFolder)) {
          reload = true;
          break;
        }
      }
      
      if(reload){
        initFacets();
      }
    }
  }

  @Override
  public void createPartControl(Composite parent) {
    this.toolkit = new FormToolkit(parent.getDisplay());
    this.form = toolkit.createScrolledForm(parent);
    // form.setText("Narrow Down Your Search Results");
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.marginHeight = 10;
    layout.marginWidth = 6;
    layout.horizontalSpacing = 20;
    form.getBody().setLayout(layout);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    form.getBody().setLayoutData(gd);
  }


  
  protected void createDateFilterSection() {
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.marginHeight = 10;
    layout.marginWidth = 6;
    layout.horizontalSpacing = 20;
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    
    Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
    section.setText("Filter by Created Date");
    section.setLayout(layout);
    section.setLayoutData(gd);
    
    Composite client = toolkit.createComposite(section);
    section.setClient(client);
    
    layout = new GridLayout();
    layout.numColumns = 2;
    layout.marginWidth = 2;
    layout.marginHeight = 2;
    client.setLayout(layout);
    gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    client.setLayoutData(gd);
    
    
    toolkit.createLabel(client, "From:", SWT.NONE);
    Composite radioComp = toolkit.createComposite(client);
    radioComp.setLayout(layout);
    radioComp.setLayoutData(gd);
    
    final Shell shell = client.getShell();
    
    this.fromDateText = toolkit.createText(radioComp, "", SWT.NONE);
    this.fromDateText.setEnabled(false);
    final SWTCalendarListener fromDateChangedListener = new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent event) {
        fromDateText.setText(sdf.format(event.getCalendar().getTime()));
        performAction();
      }
    };
    Button popupFromCalendar = toolkit.createButton(radioComp, "", SWT.PUSH);
    popupFromCalendar.setImage(VeloPlugin.getDefault().getImage(VeloPlugin.IMAGE_CALENDAR, 16));
    popupFromCalendar.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
          CalendarDialog dialog = new CalendarDialog(shell);
          if(fromDateText.getText() != null && fromDateText.getText().length() > 0){
            dialog.setDate(sdf.parse(fromDateText.getText()));
          }
          dialog.addDateChangedListener(fromDateChangedListener);
          dialog.open();
        } catch (ParseException e1) {
          MessageDialog.openError(shell, "Format-Error", "Couldn't parse date.");
        }
      }
    });
    
    toolkit.createLabel(client, "To:", SWT.NONE);
    radioComp = toolkit.createComposite(client);
    radioComp.setLayout(layout);
    radioComp.setLayoutData(gd);
    
    this.toDateText = toolkit.createText(radioComp, "", SWT.NONE);
    this.toDateText.setEnabled(false);
    final SWTCalendarListener toDateChangedListener = new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent event) {
        toDateText.setText(sdf.format(event.getCalendar().getTime()));
        performAction();
      }
    };
    Button popupToCalendar = toolkit.createButton(radioComp, "", SWT.PUSH);
    popupToCalendar.setImage(VeloPlugin.getDefault().getImage(VeloPlugin.IMAGE_CALENDAR, 16));
    popupToCalendar.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
          CalendarDialog dialog = new CalendarDialog(shell);
          if(toDateText.getText() != null && toDateText.getText().length() > 0){
            dialog.setDate(sdf.parse(toDateText.getText()));
          }
          dialog.addDateChangedListener(toDateChangedListener);
          dialog.open();
        } catch (ParseException e1) {
          MessageDialog.openError(shell, "Format-Error", "Couldn't parse date.");
        }
      }
    });
    
    
    ToolBar tbar = new ToolBar(section, SWT.FLAT | SWT.HORIZONTAL);
    ToolItem titem = new ToolItem(tbar, SWT.PUSH);
    section.setTextClient(tbar);
    tbar.getItem(0).setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE));
    titem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        toDateText.setText("");
        fromDateText.setText("");
        performAction();
      }
    });
    
  }


  class FacetViewerFilter extends ViewerFilter {

    protected String textEntered;
    protected String searchString;

    @Override
    public boolean select(Viewer viewer, 
        Object parentElement, 
        Object element) {
      if (searchString == null || searchString.length() == 0) {
        return true;
      }
      
      //viewer is a ListViewer, parentElement is a writeableList
      FacetItem facetItem = (FacetItem) element;
      return facetItem.getLabel().toLowerCase().matches(searchString);
    }

    public String getTextEntered() {
      return textEntered;
    }

    public void setTextEntered(String textEntered) {
      this.textEntered = textEntered;
      this.searchString = ".*" + textEntered.toLowerCase() + ".*";
    }
  }
  
  protected Section createFacetSection(final String title) {
    final Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
    
    Composite client = toolkit.createComposite(section);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.marginWidth = 2;
    layout.marginHeight = 2;
    client.setLayout(layout);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    client.setLayoutData(gd);
    section.setClient(client);
    section.setText(title);
    section.setLayout(layout);
    section.setLayoutData(gd);

    final Text filter = toolkit.createText(client, "");
    filter.setToolTipText("Filter the list below by typing here.");
    final FacetViewerFilter viewerFilter = new FacetViewerFilter();
    filter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    
    final List list = new List(client, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    gd = new GridData(SWT.FILL, SWT.FILL, true, false);
    gd.heightHint = 75;
    list.setLayoutData(gd);

    facetNameToUIList.put(title, list);

    ArrayList<FacetItem> items = new ArrayList<FacetItem>();
    final ListViewer listViewer = new ListViewer(list);
    listViewer.setFilters(new FacetViewerFilter[]{viewerFilter});
    listViewer.setContentProvider(new ObservableListContentProvider());
    listViewer.setLabelProvider(new FacetItemLabelProvider());
    WritableList listInput = new WritableList(items, FacetItem.class);
    listViewer.setInput(listInput);
    listViewer.addSelectionChangedListener(new FacetChangeListener(section, title));

    filter.addKeyListener(new KeyAdapter() {
      @Override public void keyReleased(KeyEvent e) {
        //ignore when keys are pressed that don't impact the search (like shift, alt, ect.)
        if(!filter.getText().equalsIgnoreCase(viewerFilter.getTextEntered())){
          viewerFilter.setTextEntered(filter.getText());
          listViewer.refresh();
        }
      }
    });
    facetNameToModelList.put(title, listInput);

    //this toolbar item is the 'x' to remove the selected facet
    ToolBar tbar = new ToolBar(section, SWT.FLAT | SWT.HORIZONTAL);
    final ToolItem titem = new ToolItem(tbar, SWT.PUSH);
    titem.setEnabled(false);
    section.setTextClient(tbar);
    titem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        list.deselectAll();
        titem.setEnabled(false);
        ((ToolBar)section.getTextClient()).getItem(0).setImage(null);
        section.setText(title);
        seletedFacets.remove(title);
        if(seletedFacets.size() == 0){
          initFacets();
        }
        performAction();
      }
    });

    return section;
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub
  }
  
  abstract protected AbstractFacetedSearchQuery initSearchQuery();

  protected IBasicSearchQuery getSearchQuery() {
    AbstractFacetedSearchQuery query = initSearchQuery();
    query.setFacetedSearchResultListener(this);

    query.setClustered(false);
    if(fromDateText != null && !fromDateText.getText().isEmpty() && toDateText != null && !toDateText.getText().isEmpty()){
      try{
        Date fromDate = sdf.parse(fromDateText.getText());
        Date toDate = sdf.parse(toDateText.getText());
        query.setCreatedFromDate(fromDate);
        query.setCreatedToDate(toDate);
      }catch(ParseException e){
        //if somehow the field has text that cannot be parsed into a date, ignore it and don't use it in the search
      }
    }
    return addDomainSpecificSearchTerms(query);
  }


  
  abstract protected IBasicSearchQuery addDomainSpecificSearchTerms(AbstractFacetedSearchQuery query);

  protected java.util.List<String> getSelectedFacetItems(String facetLabel) {
    return this.seletedFacets.get(facetLabel);
  }

  public void clearFacetSelections() {
    for (List uiList : facetNameToUIList.values()) {
      uiList.deselectAll();
    }
  }

  
  
  public void initFacets(){
    AbstractFacetedSearchQuery query = initSearchQuery();
    
    SearchForFacetsJob facetJob;
    try {
      facetJob = new SearchForFacetsJob(query.buildSearchQuery());
      facetJob.setPriority(Job.SHORT);
      facetJob.setUser(false);
      facetJob.schedule();
      facetJob.addJobChangeListener(new JobChangeAdapter(){
        @Override
        public void done(IJobChangeEvent event) {
          scheduleInitPropertyFacetsUIJob();
        }
      });
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  protected void scheduleInitPropertyFacetsUIJob(){
    new InitPropertyFacetsUIJob().schedule();
  }
  
  protected abstract ArrayList<String> getFieldsQNames();
  
  protected class SearchForFacetsJob extends Job {
    protected String query;
    public SearchForFacetsJob(String query) {
      super("Search For Facets Job");
      this.query = query;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        initialFacetItems = ResourcesPlugin.getSearchManager().getFacetItems(query, getFieldsQNames());
      } catch(Exception e){
        e.printStackTrace();
      }
          
      return Status.OK_STATUS;
    }
    
  }
  
  @Override
  public void performAction() {
    IBasicSearchQuery query = getSearchQuery();
    // NewSearchUI.activateSearchResultView(); //I don't want the results view to gain focus with every search
    if(query != null){ //case where user enters invalid values
      NewSearchUI.runQueryInBackground(query);
    }
  }

  
  protected class FacetChangeListener implements ISelectionChangedListener {
    protected Section section;
    protected String facetLabel;

    public FacetChangeListener(Section section, String facetLabel) {
      this.section = section;
      this.facetLabel =facetLabel;
    }

    public void selectionChanged(SelectionChangedEvent event) {
      //once a selection in the facet is made, add the selection to the title bar and enable the 'x' button so that it can later be removed
      if (!event.getSelection().isEmpty()) {
        String text = section.getText();
        ToolBar tbar = ((ToolBar)section.getTextClient());
        tbar.getItem(0).setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE));
        tbar.getItem(0).setEnabled(true);
        
        FacetItem selItem = (FacetItem)((StructuredSelection)event.getSelection()).getFirstElement();
        //if there's already something selected, add a comma instead of a colon
        if(!seletedFacets.containsKey(facetLabel)){
          seletedFacets.put(facetLabel, new ArrayList());
          section.setText(text + ": " + selItem.getLabel());
        }else {
          section.setText(text + " OR " + selItem.getLabel());  //are multiple selections in the same facet AND'd or OR'd???
        }
        
        seletedFacets.get(facetLabel).add(selItem.getLabel());
        performAction();
        
      }
    }
  }
  

  @Override
  public void init(IViewPart viewpart, ISearchPageContainer container) {
  }

  @Override
  public Composite createSearchPage(Composite parent) {
    this.searchPageComposite = parent;
    return searchPageComposite;
  }

  @Override
  public void aboutToShow() {
    // TODO Auto-generated method stub

  }

  @Override
  public void aboutToHide() {
    // TODO Auto-generated method stub

  }

  @Override
  public void searchExecuted(ICatQueryResult result) {
    addToSearchResults(result, true);
    scheduleUIUpdate();
  }

  @Override
  public void searchExecutedMore(ICatQueryResult moreResults) {
    addToSearchResults(moreResults, false);
    scheduleUIUpdate();
  }

  protected synchronized void scheduleUIUpdate() {
    new UpdatePropertyFacetsUIJob().schedule();
  }

  protected synchronized void addToSearchResults(ICatQueryResult result, boolean clear){
    if(clear){
      this.searchResults.clear();
    }
    this.searchResults.addAll(result.getHandles());
    this.catQueryResult = result;
  }
  
  protected void updateFacetLists(String facetName, HashMap<String, Integer> facetItemsCounts){
    populateFacet(facetNameToModelList.get(facetName), facetItemsCounts, facetName);
  }
  
  protected void populateFacet(java.util.List<FacetItem> list, HashMap<String, Integer> values, String facetName){
    list.clear();
    if(values != null){
      // put new values in temporary lists so that we can sort them, got java.lang.UnsupportedOperationException when trying to sort the observable lists
      java.util.List<FacetItem> tempList = new ArrayList<FacetItem>();
      for (String type : values.keySet()) {
        //only add values that weren't already selected
        if(seletedFacets.containsKey(facetName) == false || !seletedFacets.get(facetName).contains(type)){
          list.add(new FacetItem(type, values.get(type)));
        }
      }
      Collections.sort(tempList, new FacetItemComparable());
      
      list.addAll(tempList);
    }
  }

  public abstract void initFacetUILists();
  public abstract void updateFacetListsSearchResults();
  
  public class InitPropertyFacetsUIJob extends UIJob {

    public InitPropertyFacetsUIJob (){
      super("Initalize facets");
      setSystem(true);
    }
    
    @SuppressWarnings("unchecked")
    public IStatus runInUIThread(IProgressMonitor monitor) {
      try {
        initFacetUILists();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return Status.OK_STATUS;
    }
    
  }
  
  public class UpdatePropertyFacetsUIJob extends UIJob {

    public UpdatePropertyFacetsUIJob() {
      super("Update Search Facets");
      setSystem(true);
    }

    @SuppressWarnings("unchecked")
    public IStatus runInUIThread(IProgressMonitor monitor) {
      try {
        updateFacetListsSearchResults();
      } catch (Exception e){
        e.printStackTrace();  //TODO
        throw new RuntimeException("Error updating facet lists with search results", e);
      }
      return Status.OK_STATUS;
    }

  }


  
  public class FacetItemComparable implements Comparator<FacetItem> {
    @Override
    public int compare(FacetItem o1, FacetItem o2) {
      //or i can sort based on number of hits instead of the label...higher number showing first?
      return o1.getLabel().compareTo(o2.getLabel());
    }
  }
  
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }
 
}
