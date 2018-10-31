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
package gov.pnnl.cat.ui.rcp.dialogs.properties.user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 */
public class UserPropertiesWizardComposite extends Composite {

  private Text confirmText;
  private Text passwordText;
  private Text usernameText;
  private Button forcePasswordChangeButton;

  /**
   * Constructor for UserPropertiesWizardComposite.
   * @param parent Composite
   * @param style int
   */
  public UserPropertiesWizardComposite(Composite parent, int style) {
    super(parent, style);

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    setLayout(gridLayout);

    final Label usernameLabel = new Label(this, SWT.NONE);
    usernameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    usernameLabel.setText("Username:");

    usernameText = new Text(this, SWT.BORDER);
    usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    final Label passwordLabel = new Label(this, SWT.NONE);
    passwordLabel.setText("Password:");

    passwordText = new Text(this, SWT.BORDER);
    passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    passwordText.setEchoChar('*');

    final Label confirmLabel = new Label(this, SWT.NONE);
    confirmLabel.setText("Confirm:");

    confirmText = new Text(this, SWT.BORDER);
    confirmText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    confirmText.setEchoChar('*');

    new Label(this, SWT.NONE);

    // TODO: uncomment when we are ready to add this functionality to the server
//    forcePasswordChangeButton = new Button(this, SWT.CHECK);
//    forcePasswordChangeButton.setText("Force password change on first login");
//    forcePasswordChangeButton.setSelection(true);
  }

  /**
   * Method getConfirmText.
   * @return Text
   */
  public Text getConfirmText() {
    return confirmText;
  }

  /**
   * Method getForcePasswordChangeButton.
   * @return Button
   */
  public Button getForcePasswordChangeButton() {
    return forcePasswordChangeButton;
  }

  /**
   * Method getPasswordText.
   * @return Text
   */
  public Text getPasswordText() {
    return passwordText;
  }

  /**
   * Method getUsernameText.
   * @return Text
   */
  public Text getUsernameText() {
    return usernameText;
  }
}
