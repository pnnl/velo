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
package gov.pnnl.cat.ui.rcp.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This perspective has nothing but an editor window.  It is used for 
 * opening editors in a separate window.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class EditorPerspective  implements IPerspectiveFactory {
  public static final String ID = EditorPerspective.class.getName();
  
  /**
   * Method createInitialLayout.
   * @param layout IPageLayout
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
   */
  @Override
  public void createInitialLayout(IPageLayout layout) {

    layout.setEditorAreaVisible(true);

    // perspective shortcuts
    layout.addPerspectiveShortcut(ID);
      }

}
