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
package gov.pnnl.cat.ui.rcp.properties;

import gov.pnnl.cat.core.internal.resources.ResourceService;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.dialogs.properties.user.PersonPropertiesComposite;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 */
public class UserPersonPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {
  private PersonPropertiesComposite personPropertiesComposite;

  private Logger logger = CatLogger.getLogger(UserPersonPropertiesPage.class);

  public UserPersonPropertiesPage() {
    super();
    noDefaultAndApplyButton();
  }

  /**
   * Method createContents.
   * @param parent Composite
   * @return Control
   */
  @Override
  protected Control createContents(Composite parent) {
    IUser user = (IUser) getElement();
    personPropertiesComposite = new PersonPropertiesComposite(parent, SWT.NULL, user);
    return personPropertiesComposite;
  }

  /**
   * Method performOk.
   * @return boolean
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    IUser user = (IUser) getElement();

    if (isDirty()) {
      // if the user has changed something, schedule a job to update the user
      IUser userToUpdate = user.clone();

      userToUpdate.setFirstName(personPropertiesComposite.getFirstNameText().getText());
      userToUpdate.setLastName(personPropertiesComposite.getLastNameText().getText());
      userToUpdate.setEmail(personPropertiesComposite.getEmailText().getText());
      userToUpdate.setPhoneNumber(personPropertiesComposite.getPhoneText().getText());
      userToUpdate.setDeletePicture(personPropertiesComposite.isPictureFileDeleted());

      File pictureFile = personPropertiesComposite.getPictureFile();

      if (pictureFile != null) {
        try {
          userToUpdate.setPicture(pictureFile);
          String mimetype = ResourceService.getMimeType(pictureFile);
          userToUpdate.setPictureMimetype(mimetype);
        } catch (FileNotFoundException e) {
          logger.error(e);
          // TODO: alert the user that the file was not found
        }
      }

      try {
        ResourcesPlugin.getSecurityManager().updateUser(userToUpdate);
      } catch (Throwable e) {
        String errMsg = "A error occurred while updating the user account.";
        ToolErrorHandler.handleError(errMsg, e, true);
      }
    }

    super.performOk();
    return true;
  }

  /**
   * Method isDirty.
   * @return boolean
   */
  private boolean isDirty() {
    IUser user = (IUser) getElement();
    return !(
    // the dialog is dirty if any of the following is not true
    ResourcePropertiesUtil.stringsEqual(user.getFirstName(), personPropertiesComposite.getFirstNameText().getText()) && ResourcePropertiesUtil.stringsEqual(user.getLastName(), personPropertiesComposite.getLastNameText().getText()) && ResourcePropertiesUtil.stringsEqual(user.getEmail(), personPropertiesComposite.getEmailText().getText()) && ResourcePropertiesUtil.stringsEqual(user.getPhoneNumber(), personPropertiesComposite.getPhoneText().getText()) && personPropertiesComposite.getPictureFile() == null && !personPropertiesComposite.isPictureFileDeleted());
  }

}
