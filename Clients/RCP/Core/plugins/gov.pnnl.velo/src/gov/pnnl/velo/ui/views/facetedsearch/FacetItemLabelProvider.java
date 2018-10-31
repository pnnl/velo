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

import org.eclipse.jface.viewers.LabelProvider;

/**
 */
public class FacetItemLabelProvider extends LabelProvider  {

  /**
   * Method getText.
   * @param element Object
   * @return String
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
   */
  @Override
  public String getText(Object element) {
    if(element instanceof FacetItem){
      FacetItem item = (FacetItem) element;
      return item.getLabel() + " (" + item.getCount() + ")";
    }
    return element == null ? "" : element.toString();
  }

  
}
