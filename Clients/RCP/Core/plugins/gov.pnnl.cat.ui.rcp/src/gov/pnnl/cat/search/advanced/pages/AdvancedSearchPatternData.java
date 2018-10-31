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
package gov.pnnl.cat.search.advanced.pages;

import gov.pnnl.cat.search.basic.pages.SimpleSearchPatternData;
import gov.pnnl.cat.search.ui.util.ISearchPatternData;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 */
public class AdvancedSearchPatternData extends SimpleSearchPatternData {
  /**
   * Constructor includes the settings we want to save about this search.
   * @param textPattern String
   */
  public AdvancedSearchPatternData(String textPattern) {
    super(textPattern);
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
    return new AdvancedSearchPatternData(textPattern);
  }
}
