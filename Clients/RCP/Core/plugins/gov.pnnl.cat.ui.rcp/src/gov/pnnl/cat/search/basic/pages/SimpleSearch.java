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
package gov.pnnl.cat.search.basic.pages;

import gov.pnnl.cat.search.basic.query.BasicSearchQuery;
import gov.pnnl.cat.search.basic.query.IBasicSearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.search.ui.SearchPage;
import gov.pnnl.cat.search.ui.util.ISearchPatternData;
import gov.pnnl.cat.search.ui.util.ISearchPatternDataFactory;
import gov.pnnl.cat.search.ui.util.SearchPatternHistory;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class SimpleSearch extends SearchPage {
  
  public static final String ID = "gov.pnnl.cat.search.basic.simpleSearch";
  
  private SimpleSearchComposite searchPageComposite;
  private SearchPatternHistory history;

  public SimpleSearch(){
    this.history = new SearchPatternHistory(new ISearchPatternDataFactory(){
      public ISearchPatternData create(IDialogSettings settings) {
        return SimpleSearchPatternData.create(settings);
      }});
    this.history.loadPreviousHistory(CatRcpPlugin.getDefault().getDialogSettings());
  }

  /**
   * Method dispose.
   * @see gov.pnnl.cat.search.ui.ISearchPage#dispose()
   */
  public void dispose() {
    this.history.saveSearchHistory(CatRcpPlugin.getDefault().getDialogSettings());
  }

  /**
   * Method performAction.
   * @see gov.pnnl.cat.search.ui.ISearchPage#performAction()
   */
  public void performAction() {
    // assume that createSearchPage has already been called and that searchPageComposite != null
//    System.out.println("Simple Search! " + this.searchPageComposite.getNameCombo().getText());
    
    IBasicSearchQuery query = getSearchQuery();    
    NewSearchUI.activateSearchResultView();
    NewSearchUI.runQueryInBackground(query);    
    
    // TEST CODE
/*
    String result = SearchTest.simpleSearch(this.searchPageComposite.getNameText());
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow(); 
    
    MessageDialog.openInformation(
        window.getShell(),
        "Testing Search Results",
        result);    
*/
  }

  /**
   * Method createSearchPage.
   * @param parent Composite
   * @return Composite
   * @see gov.pnnl.cat.search.ui.ISearchPage#createSearchPage(Composite)
   */
  public Composite createSearchPage(Composite parent) {
    if (this.searchPageComposite == null) {
      this.searchPageComposite = new SimpleSearchComposite(parent, SWT.WRAP);
      updateSearchCombo();
    }

    return this.searchPageComposite;
  }

  private void updateSearchCombo() {
    // update the UI to show the new search history
    Combo combo = this.searchPageComposite.getNameCombo();
    combo.setItems(getPreviousSearchPatterns());
  }

  /**
   * Method getSearchQuery.
   * @return IBasicSearchQuery
   */
  private IBasicSearchQuery getSearchQuery() {
    
    BasicSearchQuery query = new BasicSearchQuery();

    // get the search pattern from our UI
    ISearchPatternData patternData = getPatternData();

    // add the new search pattern to the history
    this.history.addSearchPatternHistory(patternData);
    updateSearchCombo();
    this.searchPageComposite.getNameCombo().select(0);

    String name = this.searchPageComposite.getNameCombo().getText();
    
    query.setClustered(container.isClusteredSearchEnabled());
    query.setSearchString(name);
    
    return query;
  }


  /**
   * Return search pattern data and update previous searches.
   * An existing entry will be updated.
  
   * @return the search pattern data */
  private ISearchPatternData getPatternData() {
    ISearchPatternData match = new SimpleSearchPatternData(this.searchPageComposite.getNameCombo().getText());
    return match;
  }
  
  /**
   * Method getPreviousSearchPatterns.
   * @return String[]
   */
  public String[] getPreviousSearchPatterns() {
    List searchPatterns = history.getSearchPatternHistory();
    
    int i = 0;
    String [] patterns= new String[searchPatterns.size()];
    for (Iterator iter = searchPatterns.iterator(); iter.hasNext();) {
      
      SimpleSearchPatternData element = (SimpleSearchPatternData) iter.next();
      patterns[i]= element.getTextPattern();
      i++;
    }
    return patterns;
  }
}
