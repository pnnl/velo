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
package gov.pnnl.cat.ui.rcp.views.users.actions;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.wizards.NewUserWizardPage1;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 */
public class ChangePasswordDialog extends Dialog {

  protected static Logger logger = CatLogger.getLogger(ChangePasswordDialog.class);

  private Text confirmedNewPassword;

  private Text newPassword;

  private Text currentPassword;

  private boolean confirmCurrentPassword;

  private String username;

  /**
   * Constructor for ChangePasswordDialog.
   * @param parent Shell
   * @param confirmCurrentPassword boolean
   * @param username String
   */
  public ChangePasswordDialog(Shell parent, boolean confirmCurrentPassword, String username) {
    super(parent);
    this.confirmCurrentPassword = confirmCurrentPassword;
    this.username = username;
  }

  /**
   * Method createDialogArea.
   * @param parent Composite
   * @return Control
   */
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    container.setLayout(gridLayout);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    ModifyListener modifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        enableOkButton();
      }
    };

    if (confirmCurrentPassword) {
      final Label currentPasswordLabel = new Label(container, SWT.NONE);
      currentPasswordLabel.setText("Current Password:");

      currentPassword = new Text(container, SWT.BORDER);
      currentPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
      currentPassword.setEchoChar('*');
      currentPassword.addModifyListener(modifyListener);
    }

    final Label newPasswordLabel = new Label(container, SWT.NONE);
    newPasswordLabel.setText("New Password:");

    newPassword = new Text(container, SWT.BORDER);
    newPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    newPassword.setEchoChar('*');
    newPassword.addModifyListener(modifyListener);

    final Label confirmnewpasswordLabel = new Label(container, SWT.NONE);
    confirmnewpasswordLabel.setText("Confirm New Password");

    confirmedNewPassword = new Text(container, SWT.BORDER);
    confirmedNewPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    confirmedNewPassword.setEchoChar('*');
    confirmedNewPassword.addModifyListener(modifyListener);

    return container;
  }

  /**
   * Method createContents.
   * @param parent Composite
   * @return Control
   */
  protected Control createContents(Composite parent) {
    Control ctrl = super.createContents(parent);
    // when dialog first comes up the ok button should be disabled:
    this.getButton(IDialogConstants.OK_ID).setEnabled(false);
    return ctrl;
  }

  /**
   * Return the initial size of the dialog
   * @return Point
   */
  protected Point getInitialSize() {
    return new Point(320, 160);
  }

  private void enableOkButton() {
    boolean enabled = false;
    if ((!confirmCurrentPassword || this.currentPassword.getText().length() >= 1 && this.currentPassword.getText().length() <= 20) && this.newPassword.getText().length() >= NewUserWizardPage1.MIN_PASSWORD_LENGTH && this.newPassword.getText().length() <= NewUserWizardPage1.MAX_PASSWORD_LENGTH && this.confirmedNewPassword.getText().equals(this.newPassword.getText())) {
      enabled = true;
    }
    this.getButton(IDialogConstants.OK_ID).setEnabled(enabled);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    try {
      ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager();
      // will ignore the current password if the user logged in is an admin
      securityMgr.changePassword(this.username, getCurrentPassword(), getNewPassword());
      // if no exceptions thrown, call super's okPressed so dialog will close
      logger.debug("password changed successfully");
      // EZLogger.logMessage("password changed successfully");
      String message = "The password has been changed successfully."; 
        if(this.confirmCurrentPassword){
          message += "\nYou will need to use this new password the next time you login.";
        }
      MessageDialog.openInformation(getShell(), "The Password Has Been Changed", message);
      super.okPressed();
    } catch (Throwable e) {
      // TODO alert the user to the error somehow (another dialog or can we
      // change this one
      // to have a red error message at the top or soemthing???) and let them
      // try again on
      // this same dialog.
      String errMsg = "The current password provided is not correct.";
      ToolErrorHandler.handleError(errMsg, e, true);
    }
  }

  /**
   * Method configureShell.
   * @param newShell Shell
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Change Password");
  }

  /**
   * Method getCurrentPassword.
   * @return String
   */
  public String getCurrentPassword() {
    if (this.currentPassword != null) {
      return this.currentPassword.getText();
    }
    return null;
  }

  /**
   * Method getNewPassword.
   * @return String
   */
  public String getNewPassword() {
    return this.newPassword.getText();
  }

}
