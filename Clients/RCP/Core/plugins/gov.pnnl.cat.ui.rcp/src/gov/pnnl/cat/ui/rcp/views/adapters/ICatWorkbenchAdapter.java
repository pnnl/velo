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
package gov.pnnl.cat.ui.rcp.views.adapters;

import gov.pnnl.velo.model.CmsPath;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 */
public interface ICatWorkbenchAdapter extends IWorkbenchAdapter {

  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   */
  public boolean hasChildren(Object element);

  /**
   * Method getColumnImage.
   * @param element Object
   * @param columnIndex int
   * @return Image
   */
  public Image getColumnImage(Object element, int columnIndex);

  /**
   * Returns the text for this column.
   * Null indicates that the value is still being loaded.
   *
   * @param element
   * @param columnIndex
  
  
   * @return String
   */
  public String getColumnText(Object element, int columnIndex);

  /**
   * Method getPath.
   * @param element Object
   * @return CmsPath
   */
  public CmsPath getPath(Object element);
}
