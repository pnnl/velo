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
package gov.pnnl.cat.search.basic;


import gov.pnnl.cat.search.basic.query.BasicSearchQuery;
import gov.pnnl.cat.search.basic.query.IBasicSearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class PeopleSearchView extends ViewPart {
  private static final String STORE_CASE_SENSITIVE= "CASE_SENSITIVE"; //$NON-NLS-1$
  private static final String STORE_HISTORY= "HISTORY"; //$NON-NLS-1$
  private static final String STORE_HISTORY_SIZE= "HISTORY_SIZE"; //$NON-NLS-1$
  private static final int HISTORY_SIZE= 10;

  private Combo mPattern;
  private Button mCaseSensitiveButton;
  private boolean mIsCaseSensitive;
  public Control mButtonBar;
  private HashMap mButtons = new HashMap();
  
  // holds the previous things that the user has searched for.
  private List mPreviousSearchPatterns= new ArrayList(20);

  private static final int SEARCH_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;
  private static final int CLEAR_BUTTON_ID  = IDialogConstants.CLIENT_ID + 2;
  
  public PeopleSearchView() {
    super();
  }
  
  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    Composite result= new Composite(parent, SWT.NONE);
    result.setFont(parent.getFont());
    
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    result.setLayout(layout);
    result.setLayoutData(new GridData(GridData.FILL_BOTH));

    readConfiguration();
    addTextPatternControls(result);
    
    mButtonBar = createButtonBar(result);
    updateSearchButtonStatus();
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    // before we dispose of ourselves, save the configuration.
    writeConfiguration();
    super.dispose();
  }

  // taken from SearchDialog
  /**
   * Method createButtonBar.
   * @param parent Composite
   * @return Control
   */
  protected Control createButtonBar(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    // create a layout with spacing and margins appropriate for the font
    // size.
    GridLayout layout = new GridLayout();
    layout.numColumns = 0; // this is incremented by createButton
    layout.makeColumnsEqualWidth = true;
    layout.marginWidth = 10;//convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.marginHeight = 10;//convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.horizontalSpacing = 10;//convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    layout.verticalSpacing = 10;//convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    composite.setLayout(layout);
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
    composite.setLayoutData(data);
    composite.setFont(parent.getFont());
    // Add the buttons to the button bar.
    createButtonsForButtonBar(composite);
    return composite;
  }

  /**
   * Method getPattern.
   * @return String
   */
  public String getPattern() {
    return mPattern.getText();
  }

  /**
   * Method ignoreCase.
   * @return boolean
   */
  public boolean ignoreCase() {
    return !mIsCaseSensitive;
  }

  /**
   * Method findInPrevious.
   * @param pattern String
   * @return SearchPatternData
   */
  private SearchPatternData findInPrevious(String pattern) {
    for (Iterator iter= mPreviousSearchPatterns.iterator(); iter.hasNext();) {
      SearchPatternData element= (SearchPatternData) iter.next();
      if (pattern.equals(element.textPattern)) {
        return element;
      }
    }
    return null;
  }

  /**
   * Return search pattern data and update previous searches.
   * An existing entry will be updated.
  
   * @return the search pattern data */
  private SearchPatternData getPatternData() {
    SearchPatternData match= findInPrevious(mPattern.getText());
    if (match == null) {
      match = new SearchPatternData(getPattern(), ignoreCase());
    }

//    if (mPreviousSearchPatterns.size() > HISTORY_SIZE) {
//      mPreviousSearchPatterns.remove(mPreviousSearchPatterns.size());
//    }
//    mPattern.setItems(getPreviousSearchPatterns());
//    mPattern.select(0);
    return match;
  }

  /**
   * Method addSearchPatternHistory.
   * @param pattern SearchPatternData
   */
  private void addSearchPatternHistory(SearchPatternData pattern) {
    if (pattern != null) {
      mPreviousSearchPatterns.remove(pattern);
    }

    mPreviousSearchPatterns.add(0, pattern);

    int currentHistorySize = mPreviousSearchPatterns.size();
    int maxSize = HISTORY_SIZE;
    for (int i = maxSize; i < currentHistorySize; i++) {
      mPreviousSearchPatterns.remove(i);
    }
  }

  /**
   * Method getSearchQuery.
   * @return IBasicSearchQuery
   */
  private IBasicSearchQuery getSearchQuery() {
    // TODO request a query object
//    IJcrSearchQuery query = (IJcrSearchQuery) Activator.getDefault().getSearchQuery(IJcrSearchQuery.class.getName());
    IBasicSearchQuery query = new BasicSearchQuery();

    SearchPatternData patternData= getPatternData();
    addSearchPatternHistory(patternData);
    mPattern.setItems(getPreviousSearchPatterns());
    mPattern.select(0);
    this.mCaseSensitiveButton.setSelection(!patternData.isIgnoreCase());
    String name = mPattern.getText();

    query.setSearchString(name);
    return query;
  }

  private void executeSearch() {
    IBasicSearchQuery query = getSearchQuery();
    NewSearchUI.runQueryInBackground(query);
    NewSearchUI.activateSearchResultView();
  }

  /**
   * Method createButtonsForButtonBar.
   * @param parent Composite
   */
  protected void createButtonsForButtonBar(Composite parent) {
    // create OK and Cancel buttons by default
    Button searchButton = createButton(parent, SEARCH_BUTTON_ID, "Search", true);
    Button clearButton  = createButton(parent, CLEAR_BUTTON_ID,  "Clear",  false);

    searchButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        executeSearch();
      }
    });


    clearButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        clearInputControls();
      }
    });
  }

  /**
   * Method createButton.
   * @param parent Composite
   * @param id int
   * @param label String
   * @param defaultButton boolean
   * @return Button
   */
  protected Button createButton(Composite parent, int id, String label,
      boolean defaultButton) {
    // increment the number of columns in the button bar
    ((GridLayout) parent.getLayout()).numColumns++;
    Button button = new Button(parent, SWT.PUSH);
    button.setText(label);
    button.setFont(JFaceResources.getDialogFont());
    button.setData(new Integer(id));
    if (defaultButton) {
      Shell shell = parent.getShell();
      if (shell != null) {
        shell.setDefaultButton(button);
      }
    }
    mButtons.put(new Integer(id), button);
    setButtonLayoutData(button);
    return button;
  }

  /**
   * Method setButtonLayoutData.
   * @param button Button
   */
  protected void setButtonLayoutData(Button button) {
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    int widthHint = 10;//convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
    Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    data.widthHint = Math.max(widthHint, minSize.x);
    button.setLayoutData(data);
  }

  private void updateSearchButtonStatus() {
    Button searchButton = (Button) this.mButtons.get(new Integer(SEARCH_BUTTON_ID));

    if (this.mPattern.getText() != null &&
        this.mPattern.getText().length() > 0) {
      searchButton.setEnabled(true);
    } else {
      searchButton.setEnabled(false);
    }
  }

  private void clearInputControls() {
    this.mPattern.setText("");
  }
  private void handleWidgetSelected() {
    int selectionIndex= mPattern.getSelectionIndex();
    if (selectionIndex < 0 || selectionIndex >= mPreviousSearchPatterns.size())
      return;

    SearchPatternData patternData= (SearchPatternData) mPreviousSearchPatterns.get(selectionIndex);
    if (!mPattern.getText().equals(patternData.textPattern))
      return;
    mCaseSensitiveButton.setSelection(!patternData.isIgnoreCase());
    mPattern.setText(patternData.textPattern);
  }
  /**
   * Method addTextPatternControls.
   * @param group Composite
   */
  private void addTextPatternControls(Composite group) {
    // grid layout with 2 columns
    
    // Info text        
    Label label= new Label(group, SWT.LEAD);
    label.setText("Name:"); 
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
    label.setFont(group.getFont());
    
    // Pattern combo
    mPattern = new Combo(group, SWT.SINGLE | SWT.BORDER);
    // Not done here to prevent page from resizing
    mPattern.setItems(getPreviousSearchPatterns());
    mPattern.select(0);

    mPattern.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateSearchButtonStatus();
        handleWidgetSelected();
      }
    });
    
    // add some listeners for regex syntax checking
    mPattern.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateSearchButtonStatus();
      }
    });
    mPattern.setFont(group.getFont());
    GridData data = new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1);
    data.widthHint = 100;//convertWidthInCharsToPixels(50);
    mPattern.setLayoutData(data);
    
    mCaseSensitiveButton= new Button(group, SWT.CHECK);
    mCaseSensitiveButton.setText("Case sensitive"); 
    mCaseSensitiveButton.setSelection(!mIsCaseSensitive);
    mCaseSensitiveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        mIsCaseSensitive = mCaseSensitiveButton.getSelection();
      }
    });
    mCaseSensitiveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    mCaseSensitiveButton.setFont(group.getFont());
  }
  
  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    mPattern.setFocus();
  }

  /**
   * Method getPreviousSearchPatterns.
   * @return String[]
   */
  private String[] getPreviousSearchPatterns() {
    int size= mPreviousSearchPatterns.size();
    String [] patterns= new String[size];
    for (int i= 0; i < size; i++) {
      patterns[i]= ((SearchPatternData) mPreviousSearchPatterns.get(i)).textPattern;
    }
    return patterns;
  }

  /**
   * Initializes itself from the stored page settings.
   */
  private void readConfiguration() {
    IDialogSettings s= CatRcpPlugin.getDefault().getDialogSettings();
    mIsCaseSensitive= s.getBoolean(STORE_CASE_SENSITIVE);

    try {
      int historySize= s.getInt(STORE_HISTORY_SIZE);
      for (int i= 0; i < historySize; i++) {
        IDialogSettings histSettings= s.getSection(STORE_HISTORY + i);
        if (histSettings != null) {
          SearchPatternData data= SearchPatternData.create(histSettings);
          if (data != null) {
            mPreviousSearchPatterns.add(data);
          }
        }
      }
    } catch (NumberFormatException e) {
      // ignore
    }
  }

  /**
   * Stores it current configuration in the dialog store.
   */
  private void writeConfiguration() {
    IDialogSettings settings= CatRcpPlugin.getDefault().getDialogSettings();
    settings.put(STORE_CASE_SENSITIVE, mIsCaseSensitive);
    
    int historySize= Math.min(mPreviousSearchPatterns.size(), HISTORY_SIZE);
    settings.put(STORE_HISTORY_SIZE, historySize);
    for (int i= 0; i < historySize; i++) {
      IDialogSettings histSettings= settings.addNewSection(STORE_HISTORY + i);
      SearchPatternData data= ((SearchPatternData) mPreviousSearchPatterns.get(i));
      data.store(histSettings);
    }
  }

  /**
   * A small, internal class used to saved and load search settings.
   * @author d3m517
   *
   * @version $Revision: 1.0 $
   */
  private static class SearchPatternData {
    private final boolean ignoreCase;
    private final String textPattern;

    /**
     * Constructor includes the settings we want to save about this search.
     * @param textPattern String
     * @param ignoreCase boolean
     */
    public SearchPatternData(String textPattern, boolean ignoreCase) {
      this.ignoreCase= ignoreCase;
      this.textPattern= textPattern;
    }

    /**
     * Method isIgnoreCase.
     * @return boolean
     */
    public boolean isIgnoreCase() {
      return this.ignoreCase;
    }

    /**
     * Method getTextPattern.
     * @return String
     */
    public String getTextPattern() {
      return this.textPattern;
    }

    /**
     * Stores the settings known to this class in the IDialogSettings specified.
     * @param settings IDialogSettings
     */
    public void store(IDialogSettings settings) {
      settings.put("ignoreCase", ignoreCase); //$NON-NLS-1$
      settings.put("textPattern", textPattern); //$NON-NLS-1$
    }

    /**
     * Creates an instance of this class using the settings contained in the IDialogSettings specified.
     * Can return null if the IDialogSettings is not formatted correctly.
     * @param settings IDialogSettings
     * @return SearchPatternData
     */
    public static SearchPatternData create(IDialogSettings settings) {
      String textPattern= settings.get("textPattern"); //$NON-NLS-1$

      try {
        boolean ignoreCase= settings.getBoolean("ignoreCase"); //$NON-NLS-1$

        return  new SearchPatternData(textPattern, ignoreCase);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    /**
     * Method toString.
     * @return String
     */
    public String toString() {
      return this.textPattern;
    }
  }
}
