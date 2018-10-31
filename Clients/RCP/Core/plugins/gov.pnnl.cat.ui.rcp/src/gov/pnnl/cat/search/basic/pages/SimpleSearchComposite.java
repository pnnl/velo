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
package gov.pnnl.cat.search.basic.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class SimpleSearchComposite extends Composite {

  private Combo text;

  /**
   * Create the composite
   * @param parent
   * @param style
   */
  public SimpleSearchComposite(Composite parent, int style) {
    super(parent, style);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    setLayout(gridLayout);
    final GridData gridData_1 = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
    setLayoutData(gridData_1);

    text = new Combo(this, SWT.SINGLE | SWT.BORDER);
//    // Not done here to prevent page from resizing
//    mPattern.setItems(getPreviousSearchPatterns());
//    mPattern.select(0);
//    text = toolkit.createText(this, null, SWT.NONE);

    text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
  }

  /**
   * Method getNameCombo.
   * @return Combo
   */
  public Combo getNameCombo() {
    return this.text;
  }

  public void dispose() {
    super.dispose();
  }
}
