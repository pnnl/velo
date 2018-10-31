package gov.pnnl.cat.ui.rcp.views.repositoryexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.statushandlers.StatusManager;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.search.advanced.query.AdvancedSearchQuery;
import gov.pnnl.cat.search.basic.pages.SimpleSearchPatternData;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.search.ui.util.ISearchPatternData;
import gov.pnnl.cat.search.ui.util.ISearchPatternDataFactory;
import gov.pnnl.cat.search.ui.util.SearchPatternHistory;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.rcp.IResourceSelection;
import gov.pnnl.cat.ui.rcp.IResourceSelectionListener;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.model.CmsPath;

public class NavigationBar {

  protected static int PATH_HISTORY_SIZE = 10;
  protected static String PARAM_PATH_HISTORY = "pathHistory";

  protected Combo pathText;
  protected List<String>pathHistory; // this is only the list of paths typed in the box
  protected Combo searchText;
  protected SearchPatternHistory searchHistory;
  protected Button backButton;
  protected Button forwardButton;
  protected Button searchButton;
  protected Button homeButton;
  protected boolean homeButtonVisible = true;
  //protected boolean searchResultsVisible = false;

  protected List<CmsPath> history = new ArrayList<CmsPath>(); // this is the hsitory of all paths navigated to, either by typing in the box or by selecting in the tree
  protected int historyIndex = -1;
  protected boolean navButtonPressed = false;
  protected TreeExplorer treeExplorer;
  protected TableExplorer tableExplorer;
  protected IResourceManager mgr = ResourcesPlugin.getResourceManager();

  
  public NavigationBar() {
    
  }
  
  public NavigationBar(boolean homeButtonVisible) {
    this.homeButtonVisible = homeButtonVisible;
  }
  
  public void linkExplorers(TreeExplorer treeExplorer, TableExplorer tableExplorer) {
    this.treeExplorer = treeExplorer;
    this.tableExplorer = tableExplorer;
    
    treeExplorer.addFileExplorerChangeListener(new IResourceSelectionListener() {
      
      // this gets called every time we update the selection in the tree view
      @Override
      public void resourceSelectionChanged(IResourceSelection currentSelection) {
        IResource resource = currentSelection.getIResource();
        setResource(resource);
      }
    });    
  }
  
  public void setResource(IResource resource) {
    updatePathText(resource);
    if(!navButtonPressed) {
      updateHistory(resource);
    }
  }
  
  public Composite createControl(Composite parent) {
    return createTopComposite(parent);
  }
  
  /**
   * Method createTopComposite.
   * @param parent Composite
   * @return Composite
   */
  private Composite createTopComposite(Composite parent) {    
    Composite wrapper = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(8, false);
    layout.horizontalSpacing = 0;
    wrapper.setLayout(layout);
    wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    
    // back button
    backButton = new Button(wrapper, SWT.PUSH);
    backButton.setToolTipText("Go Back");
    backButton.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_BACK, SharedImages.CAT_IMG_SIZE_16));
    backButton.addListener (SWT.Selection, new Listener() {
      
      @Override
      public void handleEvent(Event event) {
        // navigate back in the history
        goBack();
      }
    }); 
    backButton.setEnabled(false);

    // forward button
    forwardButton = new Button(wrapper, SWT.PUSH);
    forwardButton.setToolTipText("Go Forward");
    forwardButton.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_FORWARD, SharedImages.CAT_IMG_SIZE_16));
    forwardButton.addListener (SWT.Selection, new Listener() {
      
      @Override
      public void handleEvent(Event event) {
        // navigate forward in the history
        goForward();
      }
    });
    forwardButton.setEnabled(false);
    
    pathText = new Combo(wrapper, SWT.SINGLE | SWT.BORDER);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    pathText.setLayoutData(gridData);
    pathText.setItems(pathHistory.toArray(new String[pathHistory.size()]));
    
    pathText.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        //System.out.println("Selection: " + combo.getItem(combo.getSelectionIndex()));
       handlePathTextChangeEvent();       
      }
    });
    
    pathText.addListener (SWT.DefaultSelection, new Listener () {
      public void handleEvent (Event e) {
        // if text is empty, do nothing
        if (pathText.getText().trim().isEmpty()) {
          return;
        }       
        handlePathTextChangeEvent();
      }
    });
    
    // When tab is pressed within path box, auto-complete the next child
    pathText.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
          System.out.println("tab pressed");
          e.doit = false; // do not traverse to next widget
          
          String path = pathText.getText();
          int slash = path.lastIndexOf('/');
          if(slash == -1) {
            return;
          }
            
          
          String parentPathStr = path.substring(0, slash);
          CmsPath parentPath = new CmsPath(parentPathStr);
          String childNamePattern = null;
          if(slash + 1 < path.length()) {
            childNamePattern = path.substring(slash+1);
          }
          
          List<IResource> possibleMatches = null;
          List<IResource> children = mgr.getChildren(parentPath);
          if(childNamePattern != null) {
            possibleMatches = new ArrayList<IResource>();
            for(IResource resource : children) {
              if(resource.getName().startsWith(childNamePattern)) {
                possibleMatches.add(resource);
              }
            }
            
          } else {
            possibleMatches = children;
          }
          if(possibleMatches.size() > 0) {
            String newPath = possibleMatches.get(0).getPath().toDisplayString();
            pathText.setText(newPath);
            handlePathTextChangeEvent();
          }
        }
        
      }

    });
    

    if(homeButtonVisible) {
      homeButton = new Button(wrapper, SWT.PUSH);
      homeButton.setToolTipText("Go To Home Folder");
      homeButton.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_HOME_FOLDER, SharedImages.CAT_IMG_SIZE_16));
      gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
      homeButton.setLayoutData(gridData);
      homeButton.addListener (SWT.Selection, new Listener() {

        @Override
        public void handleEvent(Event event) {
          // navigate to user home folder
          try {
            IResource resource = mgr.getHomeFolder();
            if(resource != null) {
              treeExplorer.getTreeViewer().expandToPath(resource.getPath());
            } else {
              displayError();
            }

          } catch (Throwable ex) {
            displayError();
          }

        }
      }); 
    }
    searchText = new Combo(wrapper, SWT.SINGLE | SWT.BORDER);
    gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
    gridData.horizontalIndent = 10;
    gridData.widthHint = 200;
    searchText.setLayoutData(gridData);
    searchText.setItems(getPreviousSearchPatterns());
    
    // when enter key is pressed, initiate search
    searchText.addTraverseListener(new TraverseListener() {
      public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_RETURN) {
          handleSearchEvent();
        }
      }
    });

    // whenever focus is obtained, if font is greyed out, change font and remove text
    searchText.addFocusListener(new FocusListener() {
      
      @Override
      public void focusLost(FocusEvent e) {
        // we don't care 
        // for now don't auto-execute the search because that could be expensive
      }
      
      @Override
      public void focusGained(FocusEvent e) {
        
        // change the color and reset the search field
        if(searchText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
        searchText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        searchText.setText("");
        }
      }
    });
    
    searchButton = new Button(wrapper, SWT.PUSH);
    searchButton.setToolTipText("Search");
    searchButton.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_SEARCH, SharedImages.CAT_IMG_SIZE_16));
    
    // whenever enter is pressed, execute the search
    searchButton.addListener (SWT.Selection, new Listener() {
      
      @Override
      public void handleEvent(Event event) {
        handleSearchEvent();
      }
    }); 
    
    return wrapper;
  }
  
  protected void handlePathTextChangeEvent() {
    try {
      // try to navigate to the new path
      CmsPath path = new CmsPath(pathText.getText());
      IResource resource = ResourcesPlugin.getResourceManager().getResource(path, true);
      if(resource != null) {
        treeExplorer.getTreeViewer().expandToPath(path);  // expand parent folder in tree
        treeExplorer.getTreeViewer().selectResource(resource); // select the resource in the tree
      
      } else {
        displayError();
      }

      // add the path to the history
      addPathToHistory(pathText.getText());
      pathText.setItems(pathHistory.toArray(new String[pathHistory.size()]));
      pathText.select(0);


    } catch (Throwable ex) {
      displayError();
    }
  }

  private void handleSearchEvent() {

    //    if(searchResultsVisible) {
    //      closeSearchResults();
    //      updatePathText();
    //      
    //    } else {
    if(searchText.getText() == null || searchText.getText().isEmpty()) {
      return;
    }

    IResource selectedFolder = mgr.getResource(new CmsPath(pathText.getText()));

    if(selectedFolder != null) {
      if(selectedFolder instanceof IFile) {
        selectedFolder = selectedFolder.getParent();
      }
        
      executeSearch(selectedFolder);
      //        searchResultsVisible = true;

      //        // change icon to delete
      //        searchButton.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DEL, SharedImages.CAT_IMG_SIZE_16));
      //
      //        pathText.setText("Search Results in " + selectedFolder.getName());

    } else {
      searchText.setText("");
    }

    //    }

  }


  // we only need this code if we toggle the search results and the table explorer in the same view
  //private void closeSearchResults() {
  //  // close search results
  //  
  //  // change icon to search
  //  searchButton.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_SEARCH, SharedImages.CAT_IMG_SIZE_16));
  //  
  //  searchResultsVisible = false;
  //
  //}

  private void updatePathText(IResource selectedFolder) {
    if(selectedFolder == null) {
      pathText.setText(""); 
      searchText.setText("");

    } else {
      pathText.setText(selectedFolder.getPath().toDisplayString());

      // also change the value of the search field
      searchText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
      searchText.setText("Search " + selectedFolder.getName());

      // get rid of search results if they are there
      //    if(searchResultsVisible) {
      //      closeSearchResults();
      //    }
    }

  }

  private void updateHistory(IResource selectedFolder) {

    // wipe everything after the current index/
    while(historyIndex < history.size()-1) {
      history.remove(history.size() -1); // pop the end
    }

    history.add(selectedFolder.getPath());
    historyIndex = history.size() -1;  

    forwardButton.setEnabled(false);
    backButton.setEnabled(history.size() > 1);

  }

  private void goBack() {
    if(historyIndex < 0 || (historyIndex > history.size() - 1) ) {
      return; // something went wrong
    }

    historyIndex--;
    CmsPath path = history.get(historyIndex);
    navButtonPressed = true;
    treeExplorer.getTreeViewer().expandToPath(path);  // expand parent folder in tree
    IResource resource = mgr.getResource(path);
    treeExplorer.getTreeViewer().selectResource(resource); // select the resource in the tree
    navButtonPressed = false;
    updatePathText(resource);

    if(historyIndex == 0) {
      backButton.setEnabled(false);
    }
    forwardButton.setEnabled(true);
  }

  private void goForward() {
    if(historyIndex < 0 || (historyIndex > history.size() - 1) ) {
      return; // something went wrong
    }

    historyIndex++;
    CmsPath path = history.get(historyIndex);
    navButtonPressed = true;
    treeExplorer.getTreeViewer().expandToPath(path);  // expand parent folder in tree
    IResource resource = mgr.getResource(path);
    treeExplorer.getTreeViewer().selectResource(resource); // select the resource in the tree

    navButtonPressed = false;
    updatePathText(resource);

    if(historyIndex == history.size() - 1) {
      forwardButton.setEnabled(false);
    }
    backButton.setEnabled(true);
  }

  /**
   * Method executeSearch.
   * @param selectedFolder IResource
   */
  private void executeSearch(IResource selectedFolder) {
    // add the query to the history
    ISearchPatternData patternData = new SimpleSearchPatternData(searchText.getText());
    searchHistory.addSearchPatternHistory(patternData);
    searchText.setItems(getPreviousSearchPatterns());
    searchText.select(0);


    // do a path search
    AdvancedSearchQuery query = new AdvancedSearchQuery();
    query.addLocation(selectedFolder.getPath());
    query.setSearchString(searchText.getText());
    NewSearchUI.activateSearchResultView();
    NewSearchUI.runQueryInBackground(query); 

  }

  /**
   * Method getPreviousSearchPatterns.
   * @return String[]
   */
  protected String[] getPreviousSearchPatterns() {
    List<ISearchPatternData> searchPatterns = searchHistory.getSearchPatternHistory();

    int i = 0;
    String [] patterns= new String[searchPatterns.size()];
    for (ISearchPatternData searchPattern : searchPatterns) {   
      SimpleSearchPatternData pattern = (SimpleSearchPatternData)searchPattern;
      patterns[i]= pattern.getTextPattern();
      i++;
    }
    return patterns;
  }

  /**
   * Method addPathToHistory.
   * @param path String
   */
  private void addPathToHistory(String path) {
    // if the same path is entered multiple times, only save one copy
    if (path != null) {
      int loc = pathHistory.lastIndexOf(path);
      if(loc >= 0) {
        pathHistory.remove(loc);
      }
    }

    pathHistory.add(0, path);

    int currentHistorySize = pathHistory.size();
    int maxSize = PATH_HISTORY_SIZE;
    for (int i = maxSize; i < currentHistorySize; i++) {
      pathHistory.remove(i);
    }
  }

  public void saveState(IMemento memento) {
    // save the path history
    StringBuilder pathStr = new StringBuilder();
    for(int i = 0; i < pathHistory.size(); i++) {
      if(i > 0) {
        pathStr.append(";");
      }
      pathStr.append(pathHistory.get(i));

    }
    memento.putString(PARAM_PATH_HISTORY, pathStr.toString());
  }

  // Restore the previous state.
  public void init(IMemento memento) throws PartInitException {
    
    pathHistory = new ArrayList<String>();
    searchHistory = new SearchPatternHistory(new ISearchPatternDataFactory(){
      public ISearchPatternData create(IDialogSettings settings) {
        return SimpleSearchPatternData.create(settings);
      }});
    searchHistory.loadPreviousHistory(CatRcpPlugin.getDefault().getDialogSettings());

    if (memento != null) {

      // path history
      String pathStr = memento.getString(PARAM_PATH_HISTORY);
      if(pathStr != null) {
        String[] paths = pathStr.split(";");
        for(String path : paths) {
          if(!path.isEmpty()) {
            pathHistory.add(path);
          }
        }
      }

    }
  }

  public void dispose() {
    searchHistory.saveSearchHistory(CatRcpPlugin.getDefault().getDialogSettings());
  }

  private void displayError() {
    StatusUtil.handleStatus("We can't find the resource: " + pathText.getText(),  StatusManager.SHOW);
  }
}
