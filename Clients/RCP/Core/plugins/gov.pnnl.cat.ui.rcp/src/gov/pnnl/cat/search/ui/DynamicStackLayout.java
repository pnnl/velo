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
package gov.pnnl.cat.search.ui;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * A stack layout that lays out differently depending on the composite that
 * currently occupies the top control. Normally, a stack layout will cause
 * each composite in the stack to lay out at the same size.
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public class DynamicStackLayout extends StackLayout {
  /**
   * Method computeSize.
   * @param composite Composite
   * @param wHint int
   * @param hHint int
   * @param flushCache boolean
   * @return Point
   */
  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
    if (this.topControl == null) {
      return new Point(0, 0);
    }
    return this.topControl.computeSize(wHint, hHint);
  }
}
