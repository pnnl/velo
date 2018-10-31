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
package gov.pnnl.cat.search.ui.util;

import gov.pnnl.cat.logging.CatLogger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogSettings;

/**
 */
public class SearchPatternHistory {
  protected static Logger logger = CatLogger.getLogger(SearchPatternHistory.class);
  private static final String STORE_HISTORY= "HISTORY"; 
  private static final String STORE_HISTORY_SIZE= "HISTORY_SIZE"; 
  private static final int HISTORY_SIZE= 10;
  
  // holds the previous things that the user has searched for.
  private List<ISearchPatternData> mPreviousSearchPatterns= new ArrayList<ISearchPatternData>(HISTORY_SIZE);
  private ISearchPatternDataFactory factory;

  /**
   * Constructor for SearchPatternHistory.
   * @param factory ISearchPatternDataFactory
   */
  public SearchPatternHistory(ISearchPatternDataFactory factory){
    this.factory = factory;
    
  }
  
  /**
   * Method getSearchPatternHistory.
   * @return List<ISearchPatternData>
   */
  public List<ISearchPatternData> getSearchPatternHistory() {
    return this.mPreviousSearchPatterns;
  }

//  public ISearchPatternData findInPrevious(ISearchPatternData pattern) {
//    for (Iterator iter= mPreviousSearchPatterns.iterator(); iter.hasNext();) {
//      ISearchPatternData element= (ISearchPatternData) iter.next();
//      if (pattern.equals(element)) {
//        return element;
//      }
//    }
//    return null;
//  }




  /**
   * Method addSearchPatternHistory.
   * @param pattern ISearchPatternData
   */
  public void addSearchPatternHistory(ISearchPatternData pattern) {
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
   * Initializes itself from the stored page settings.
   * @param settings IDialogSettings
   */
  private void readConfiguration(IDialogSettings settings) {
//    IDialogSettings s= plugin.getDialogSettings();
//    mIsCaseSensitive= s.getBoolean(STORE_CASE_SENSITIVE);

    try {
      int historySize= settings.getInt(STORE_HISTORY_SIZE);
      for (int i= 0; i < historySize; i++) {
        IDialogSettings histSettings= settings.getSection(STORE_HISTORY + i);
        if (histSettings != null) {
          ISearchPatternData data = factory.create(histSettings);
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
   * @param settings 
   */
  private void writeConfiguration(IDialogSettings settings) {
    int historySize= Math.min(mPreviousSearchPatterns.size(), HISTORY_SIZE);
    settings.put(STORE_HISTORY_SIZE, historySize);
    for (int i= 0; i < historySize; i++) {
      IDialogSettings histSettings= settings.addNewSection(STORE_HISTORY + i);
      ISearchPatternData data= ((ISearchPatternData) mPreviousSearchPatterns.get(i));
      data.store(histSettings);
    }
  }

  /**
   * Method loadPreviousHistory.
   * @param settings IDialogSettings
   */
  public void loadPreviousHistory(IDialogSettings settings) {
    //System.out.println("loading: " + settings.getSections().length);
    logger.debug("loading: " + settings.getSections().length);
    readConfiguration(settings);
  }

  /**
   * Method saveSearchHistory.
   * @param settings IDialogSettings
   */
  public void saveSearchHistory(IDialogSettings settings) {
    //System.out.println("saving: " + settings.getSections().length);
    logger.debug("saving: " + settings.getSections().length);
    writeConfiguration(settings);
  }
}
