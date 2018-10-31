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
package gov.pnnl.cat.search.advanced.pages;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.advanced.AdvancedSearchExtensions;
import gov.pnnl.cat.search.advanced.query.AdvancedSearchQuery;
import gov.pnnl.cat.search.basic.query.IBasicSearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.search.ui.SearchPage;
import gov.pnnl.cat.search.ui.util.ISearchPatternData;
import gov.pnnl.cat.search.ui.util.ISearchPatternDataFactory;
import gov.pnnl.cat.search.ui.util.SearchPatternHistory;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.velo.model.CmsPath;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class AdvancedSearch extends SearchPage {
  protected static Logger logger = CatLogger.getLogger(AdvancedSearch.class);
  public static final String ID = "gov.pnnl.cat.search.jcr.advancedSearch";
  private AdvancedSearchComposite searchPageComposite;
  private SearchPatternHistory history;
  private AdvancedSearchExtensions ext = new AdvancedSearchExtensions();

  public AdvancedSearch(){
    this.history = new SearchPatternHistory(new ISearchPatternDataFactory(){
      public ISearchPatternData create(IDialogSettings settings) {
        return AdvancedSearchPatternData.create(settings);
      }});

    this.history.loadPreviousHistory(CatRcpPlugin.getDefault().getDialogSettings());
    this.ext.loadExtensions();
  }

  /**
   * Method performAction.
   * @see gov.pnnl.cat.search.ui.ISearchPage#performAction()
   */
  public void performAction() {
    // assume that createSearchPage has already been called and that searchPageComposite != null
    IBasicSearchQuery query = getSearchQuery();
    
    NewSearchUI.activateSearchResultView();
    NewSearchUI.runQueryInBackground(query);
  }


  /**
   * Method createSearchPage.
   * @param composite Composite
   * @return Composite
   * @see gov.pnnl.cat.search.ui.ISearchPage#createSearchPage(Composite)
   */
  public Composite createSearchPage(Composite composite) {
    if (this.searchPageComposite == null) {
      this.searchPageComposite = new AdvancedSearchComposite(composite, SWT.FILL, this.ext);
      updateSearchCombo();
    }

    return this.searchPageComposite;
  }


  private void updateSearchCombo() {
    // update the UI to show the new search history
    Combo combo = this.searchPageComposite.getTextCombo();
    combo.setItems(getPreviousSearchPatterns());
  }

  /**
   * Method dispose.
   * @see gov.pnnl.cat.search.ui.ISearchPage#dispose()
   */
  public void dispose() {
    this.history.saveSearchHistory(CatRcpPlugin.getDefault().getDialogSettings());    
  }  

  /**
   * Method getSearchQuery.
   * @return IBasicSearchQuery
   */
  private IBasicSearchQuery getSearchQuery() {
    logger.debug("GetSearchQuery");
    // TODO request a query object
    //  IJcrSearchQuery query = (IJcrSearchQuery) Activator.getDefault().getSearchQuery(IJcrSearchQuery.class.getName());

    AdvancedSearchQuery query = new AdvancedSearchQuery(ext);
    
    query.setClustered(container.isClusteredSearchEnabled());
    
    //get the search pattern from our UI
    ISearchPatternData patternData = getPatternData();

    // add the search history to our UI
    this.history.addSearchPatternHistory(patternData);
    updateSearchCombo();
    this.searchPageComposite.getTextCombo().select(0);
    String name = this.searchPageComposite.getTextCombo().getText();    
    query.setSearchString(name);
    
    //set all attributes for the advanced search
    //Created Date
    if(searchPageComposite.createdDateSpecified())
    {
      query.setCreatedDate(searchPageComposite.getCreatedDateFrom(), searchPageComposite.getCreatedDateTo());  
    }
    else
    {
      query.unSetCreatedDate();
    }
    
    //Modified Date
    if(searchPageComposite.modifiedDateSpecified())
    {
      query.setModifiedDate(searchPageComposite.getModifiedDateFrom(), searchPageComposite.getModifiedDateTo());  
    }
    else
    {
      query.unSetModifiedDate();
    }
    
    query.setAuthor(searchPageComposite.getAuthor());
    query.setTitle(searchPageComposite.getTitle());
    query.setDescription(searchPageComposite.getDescription());
    //query.setMode(searchPageComposite.getMode());
    
    query.setMimeType(searchPageComposite.getMimeType());
    query.resetLocation();
    if(!searchPageComposite.isSearchAllLocations()){
      CmsPath [] paths = searchPageComposite.getCheckedPaths();
      for(CmsPath path : paths)
      {
          query.addLocation(path);
      }
    }
    return query;
  }

  /**
   * Return search pattern data and update previous searches.
   * An existing entry will be updated.
  
   * @return the search pattern data */
  private ISearchPatternData getPatternData() {
    ISearchPatternData match = new AdvancedSearchPatternData(this.searchPageComposite.getText());
    return match;
  }

  /**
   * Method getPreviousSearchPatterns.
   * @return String[]
   */
  @SuppressWarnings("unchecked")
  public String[] getPreviousSearchPatterns() {
    List searchPatterns = this.history.getSearchPatternHistory();

    int i = 0;
    String [] patterns= new String[searchPatterns.size()];
    for (Iterator iter = searchPatterns.iterator(); iter.hasNext();) {
      AdvancedSearchPatternData element = (AdvancedSearchPatternData) iter.next();
      patterns[i]= element.getTextPattern();
      i++;
    }
    return patterns;
  }

}
