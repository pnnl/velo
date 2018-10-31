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
package gov.pnnl.velo.ui.views.facetedsearch;

/**
 */
public class FacetItem {
  private String label;
  private int count;
  
  /**
   * Constructor for FacetItem.
   * @param label String
   * @param countInt Integer
   */
  public FacetItem(String label, Integer countInt) {
    this.label = label;
    this.count = countInt.intValue();
  }
  
  /**
   * Method getLabel.
   * @return String
   */
  public String getLabel() {
    return label;
  }
  /**
   * Method setLabel.
   * @param label String
   */
  public void setLabel(String label) {
    this.label = label;
  }
  /**
   * Method getCount.
   * @return int
   */
  public int getCount() {
    return count;
  }
  /**
   * Method setCount.
   * @param count int
   */
  public void setCount(int count) {
    this.count = count;
  }

}
