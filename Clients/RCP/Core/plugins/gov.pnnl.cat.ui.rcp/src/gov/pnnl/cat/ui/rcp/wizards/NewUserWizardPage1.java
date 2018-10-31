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
package gov.pnnl.cat.ui.rcp.wizards;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.ui.rcp.dialogs.properties.user.UserPropertiesWizardComposite;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class NewUserWizardPage1 extends WizardPage {

  // TODO: provide real values for these
  public static final int MIN_USERNAME_LENGTH = 3;
  public static final int MAX_USERNAME_LENGTH = 50;

  public static final int MIN_PASSWORD_LENGTH = 3;
  public static final int MAX_PASSWORD_LENGTH = 50;
  private UserPropertiesWizardComposite userPropertiesComposite;

  /**
   * Create the wizard
   */
  public NewUserWizardPage1() {
    super("wizardPage");
    setTitle("User Information");
    setDescription("Enter information for this new user account.");
  }

  /**
   * Create contents of the wizard
   * @param parent
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    userPropertiesComposite = new UserPropertiesWizardComposite(parent, SWT.NULL);
    setControl(userPropertiesComposite);

    ModifyListener dialogChangedModifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    };

    userPropertiesComposite.getUsernameText().addModifyListener(dialogChangedModifyListener);
    userPropertiesComposite.getPasswordText().addModifyListener(dialogChangedModifyListener);
    userPropertiesComposite.getConfirmText().addModifyListener(dialogChangedModifyListener);

    userPropertiesComposite.getUsernameText().setFocus();
    
    //need to get the error message up right away if they don't even have permissions to create a new user:
    IUser user = ResourcesPlugin.getSecurityManager().getActiveUser();
    if(!user.isAdmin()){
      updateStatus("You do not have sufficient permissions to create a user.");
    }
    setPageComplete(false);
  }

  private void dialogChanged() {
    String username = userPropertiesComposite.getUsernameText().getText();
    String password = userPropertiesComposite.getPasswordText().getText();
    String passwordConfirmed = userPropertiesComposite.getConfirmText().getText();

    boolean prohibitedChar = username.indexOf('/') >=0 || username.indexOf('*') >=0 ||
    username.indexOf('\\') >=0 || username.indexOf(':') >=0 ||
    username.indexOf('?') >=0 || username.indexOf('"') >=0 ||
    username.indexOf('<') >=0 || username.indexOf('>') >=0 ||
    username.indexOf('|') >=0;

    //see if the logged in user even has the ability to create a user:
    try {
      IUser user = ResourcesPlugin.getSecurityManager().getActiveUser();
      if(!user.isAdmin()){
        updateStatus("You do not have sufficient permissions to create a user.");
      }
      else if(prohibitedChar) {
        updateStatus("A user name cannot contain any of these characters: \\/:*?\"<>|");
      }
      else if (username.length() < MIN_USERNAME_LENGTH) {
        updateStatus("Username must be at least " + MIN_USERNAME_LENGTH + " characters.");
      } else if (username.length() > MAX_USERNAME_LENGTH) {
        updateStatus("Username must be " + MAX_USERNAME_LENGTH + " characters or fewer.");
      } else if (password.length() < MIN_PASSWORD_LENGTH) {
        updateStatus("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
      } else if (password.length() > MAX_PASSWORD_LENGTH) {
        updateStatus("Password must be " + MAX_PASSWORD_LENGTH + " characters or fewer.");
      } else if (!password.equals(passwordConfirmed)) {
        updateStatus("Password does not match.");
      } else {
        // nothing is wrong!
        updateStatus(null);
      }
    } catch (Throwable e) {
      updateStatus(null); 
    } 
  }

  /**
   * Method updateStatus.
   * @param message String
   */
  private void updateStatus(String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  /**
   * Method getUsername.
   * @return String
   */
  public String getUsername() {
    return userPropertiesComposite.getUsernameText().getText();
  }

  /**
   * Method getPassword.
   * @return String
   */
  public String getPassword() {
    return userPropertiesComposite.getPasswordText().getText();
  }

  /**
   * Method shouldForcePasswordChange.
   * @return boolean
   */
  public boolean shouldForcePasswordChange() {
    return userPropertiesComposite.getForcePasswordChangeButton().getSelection();
  }
}
