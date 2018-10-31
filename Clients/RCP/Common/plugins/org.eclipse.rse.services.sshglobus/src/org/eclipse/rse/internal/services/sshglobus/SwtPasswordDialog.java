package org.eclipse.rse.internal.services.sshglobus;
/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thomas Koeckerbauer GUP, JKU - modification of InputDialog for passwords
 *******************************************************************************/
//package eu.geclipse.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple password dialog for soliciting a user id and password string from the user.
 * <p>
 * This concrete dialog class can be instantiated as is, or further subclassed as
 * required.
 * </p>
 * NOTE: This is a modified version of org.eclipse.jface.dialogs.InputDialog
 * which was changed to have a password entry text widget.
 */

public class SwtPasswordDialog extends Dialog {
  /**
   * The title of the dialog.
   */
  private String title;

  /**
   * The message to display, or <code>null</code> if none.
   */
  private String message;
  private String prompt;
  
  /**
   * The input value; the empty string by default.
   */
  private String password = ""; //$NON-NLS-1$

  private String username = ""; //$NON_NLS-1$
  
  /**
   * The input validator, or <code>null</code> if none.
   */
  private IInputValidator validator;

  /**
   * Ok button widget.
   */
  private Button okButton;

  /**
   * Input text widgets.
   */
  private Text passwordText;
  private Text usernameText;

  private Label errorMessageLabel;
  
  /**
   * Error message string.
   */
  private String errorMessage;

  /**
   * Creates an input dialog with OK and Cancel buttons. Note that the dialog
   * will have no visual representation (no widgets) until it is told to open.
   * <p>
   * Note that the <code>open</code> method blocks for input dialogs.
   * </p>
   * 
   * @param parentShell
   *            the parent shell, or <code>null</code> to create a top-level
   *            shell
   * @param dialogTitle
   *            the dialog title, or <code>null</code> if none
   * @param dialogMessage
   *            the dialog message, or <code>null</code> if none
   * @param initialValue
   *            the initial input value, or <code>null</code> if none
   *            (equivalent to the empty string)
   * @param validator
   *            an input validator, or <code>null</code> if none
   */
  public SwtPasswordDialog( Shell parentShell, String dialogTitle,
      String dialogMessage, String errMessage, String prompts, String initialUsername, String initialPassword, IInputValidator validator) {
    super(parentShell);
    this.title = dialogTitle;
    this.message = dialogMessage;
    this.errorMessage = errMessage;
    this.prompt = prompt;
    if (initialUsername == null) {
      this.username = "";//$NON-NLS-1$
    } else {
      this.username = initialUsername;
    }
    if(initialPassword == null) {
      this.password = "";
    } else {
      this.password = initialPassword;
    }
    this.validator = validator;
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  @Override
  protected void buttonPressed(final int buttonId) {
    if (buttonId == IDialogConstants.OK_ID) {
      this.username = this.usernameText.getText();
      this.password = this.passwordText.getText();
    } else {
      this.username = null;
      this.password = null;
    }
    super.buttonPressed(buttonId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell( final Shell shell ) {
    super.configureShell(shell);
    if ( this.title != null ) {
      shell.setText( this.title );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    // create OK and Cancel buttons by default
    this.okButton = createButton(parent, IDialogConstants.OK_ID,
                                 IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID,
                 IDialogConstants.CANCEL_LABEL, false);
    //do this here because setting the text will set enablement on the ok
    // button
    this.passwordText.setFocus();
    if ( this.password != null ) {
      this.passwordText.setText( this.password );
      this.passwordText.selectAll();
    }
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  @Override
  protected Control createDialogArea(final Composite parent) {
    // create composite
    Composite composite = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = true;
    composite.setLayout(layout);

    
    // create message
    if ( this.message != null ) {
      Label label = new Label(composite, SWT.WRAP );
      label.setText( this.message );
      
      GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
      data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      label.setLayoutData(data);
      label.setFont(parent.getFont());
    }
    
    // Create Prompt: TODO: support more than one
    Label prompt = new Label(composite, SWT.WRAP );
    prompt.setText( this.prompt );

    GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
    data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
    prompt.setLayoutData(data);
    prompt.setFont(parent.getFont());
    
    int style = SWT.SINGLE | SWT.BORDER | SWT.PASSWORD;

    this.passwordText = new Text(composite, style);
    this.passwordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    this.passwordText.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        validateInput();
      }
    });
    
    if(errorMessage != null) {
      errorMessageLabel = new Label(composite, SWT.WRAP );
      data = new GridData(SWT.FILL, SWT.FILL, true, false);
      errorMessageLabel.setText(errorMessage);
      data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      errorMessageLabel.setLayoutData(data);
      errorMessageLabel.setFont(parent.getFont());
      errorMessageLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
    }

    applyDialogFont(composite);
    return composite;
  }

  /**
   * Returns the error message label.
   * 
   * @return the error message label
   * @deprecated use setErrorMessage(String) instead
   */
  @Deprecated
  protected Label getErrorMessageLabel() {
    return null;
  }

  /**
   * Returns the ok button.
   * 
   * @return the ok button
   */
  protected Button getOkButton() {
    return this.okButton;
  }

  /**
   * Returns the text area.
   * 
   * @return the text area
   */
  protected Text getPasswordText() {
    return this.passwordText;
  }

  /**
   * Returns the validator.
   * 
   * @return the validator
   */
  protected IInputValidator getValidator() {
    return this.validator;
  }

  /**
   * Returns the string typed into this input dialog.
   * 
   * @return the input string
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * Validates the input.
   * <p>
   * The default implementation of this framework method delegates the request
   * to the supplied input validator object; if it finds the input invalid,
   * the error message is displayed in the dialog's message line. This hook
   * method is called whenever the text changes in the input field.
   * </p>
   */
  protected void validateInput() {
//    String errorMsg = null;
//    if ( this.validator != null ) {
//      errorMsg = this.validator.isValid( this.text.getText() );
//    }
//    // Bug 16256: important not to treat "" (blank error) the same as null
//    // (no error)
//    setErrorMessage(errorMsg);
  }

  /**
   * Sets or clears the error message.
   * If not <code>null</code>, the OK button is disabled.
   * 
   * @param errorMessage
   *            the error message, or <code>null</code> to clear
   * @since 3.0
   */
  public void setErrorMessage(final String errorMessage) {
    this.errorMessage = errorMessage;
    if(errorMessageLabel != null) {
      if(errorMessage != null) {
        errorMessageLabel.setText(errorMessage);
      } else {
        errorMessageLabel.setText("");
      }
    }
  }
}