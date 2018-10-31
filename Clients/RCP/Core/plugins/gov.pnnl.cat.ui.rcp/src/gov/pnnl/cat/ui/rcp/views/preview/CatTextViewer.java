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
package gov.pnnl.cat.ui.rcp.views.preview;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

/**
 * Use the custom {@link CatStyledText} instead of {@link StyledText}.
 * @version $Revision: 1.0 $
 */
public class CatTextViewer extends TextViewer {

  /**
   * {@inheritDoc}
   */
  public CatTextViewer() {
    super();
  }

  /**
   * {@inheritDoc}
   * @param parent Composite
   * @param styles int
   */
  public CatTextViewer(Composite parent, int styles) {
    super(parent, styles);
  }

  /**
   * Return an instance of the custom {@link CatStyledText}.
   * <p>
   * {@inheritDoc}
   * </p>
   * 
   * @see org.eclipse.jface.text.TextViewer#createTextWidget(org.eclipse.swt.widgets.Composite, int)
   */
  @Override
  protected StyledText createTextWidget(Composite parent, int styles) {
    CatStyledText styledText = new CatStyledText(parent, styles);
    styledText.setLeftMargin(Math.max(styledText.getLeftMargin(), 2));

    return styledText;
  }
}
