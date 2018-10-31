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

import gov.pnnl.cat.ui.rcp.dialogs.properties.user.PersonPropertiesComposite;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class NewUserWizardPage2 extends WizardPage {

  private PersonPropertiesComposite personPropertiesComposite;
  /**
   * Create the wizard
   */
  public NewUserWizardPage2() {
    super("wizardPage");
    setTitle("Person Information");
    setDescription("Enter information about the person who will use this account.");
  }

  /**
   * Create contents of the wizard
   * @param parent
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    // the person properties composite is where our UI components are.
    // this class just wraps behavior around the UI components.
    personPropertiesComposite = new PersonPropertiesComposite(parent, SWT.NULL);
    setControl(personPropertiesComposite);

    ModifyListener dialogChangedModifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    };

    personPropertiesComposite.getPictureText().addModifyListener(dialogChangedModifyListener);

    personPropertiesComposite.getFirstNameText().setFocus();

    // all info is optional
    setPageComplete(true);
  }

  private void dialogChanged() {
    String imageFile = personPropertiesComposite.getPictureText().getText();

    if (imageFile.length() > 0 && !new File(imageFile).exists()) {
      updateStatus("The file specified does not exist:\n" + imageFile);
    } else {
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
   * Method getFirstName.
   * @return String
   */
  public String getFirstName() {
    return personPropertiesComposite.getFirstNameText().getText();
  }

  /**
   * Method getLastName.
   * @return String
   */
  public String getLastName() {
    return personPropertiesComposite.getLastNameText().getText();
  }

  /**
   * Method getEmail.
   * @return String
   */
  public String getEmail() {
    return personPropertiesComposite.getEmailText().getText();
  }

  /**
   * Method getPhone.
   * @return String
   */
  public String getPhone() {
    return personPropertiesComposite.getPhoneText().getText();
  }

  /**
   * Method getPictureFile.
   * @return File
   */
  public File getPictureFile() {
    return personPropertiesComposite.getPictureFile();
  }
}
