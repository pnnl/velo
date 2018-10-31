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
/**
 * 
 */
package gov.pnnl.cat.search.basic.pages;

import gov.pnnl.cat.search.ui.util.ISearchPatternData;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 */
public class SimpleSearchPatternData implements ISearchPatternData {
  private final String textPattern;
  protected final static String KEY_TEXT_PATTERN = SimpleSearch.ID + ".textPattern";

  /**
   * Constructor includes the settings we want to save about this search.
   * @param textPattern String
   */
  public SimpleSearchPatternData(String textPattern) {
    this.textPattern= textPattern;
  }
  
  
  /**
   * Creates an instance of this class using the settings contained in the IDialogSettings specified.
   * Can return null if the IDialogSettings is not formatted correctly.
   * @param settings IDialogSettings
   * @return ISearchPatternData
   */
  public static ISearchPatternData create(IDialogSettings settings) {
    String textPattern = settings.get(KEY_TEXT_PATTERN);
    if(textPattern == null || textPattern.length() == 0){
      return null;
    }
    return new SimpleSearchPatternData(textPattern);
  }


  /**
   * Method getTextPattern.
   * @return String
   */
  public String getTextPattern() {
    return this.textPattern;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cue.search.ui.util.ISearchPatternData#store(org.eclipse.jface.dialogs.IDialogSettings)
   */
  public void store(IDialogSettings settings) {
    settings.put(KEY_TEXT_PATTERN, this.textPattern);
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return this.textPattern;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cue.search.ui.util.ISearchPatternData#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o.getClass().equals(this.getClass())) {
      SimpleSearchPatternData searchPattern = (SimpleSearchPatternData) o;
      // if both patterns are null OR
      // both patterns are equal (according to String.equals()), return true
      return this.textPattern == null && searchPattern.getTextPattern() == null ||
             (this.textPattern != null && this.textPattern.equals(searchPattern.getTextPattern()));
    }
    return false;
  }
}
