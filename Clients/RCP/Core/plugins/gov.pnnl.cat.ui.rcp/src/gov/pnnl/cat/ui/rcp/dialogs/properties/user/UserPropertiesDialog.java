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

import gov.pnnl.cat.core.internal.resources.ResourceService;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.properties.ResourcePropertiesUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 */
public class UserPropertiesDialog extends Dialog {

  /**
   */
  public enum Tab {
    ACCOUNT_PROPERTIES_TAB, PERSON_PROPERTIES_TAB
  };

  private PersonPropertiesComposite personPropertiesComposite;

  private UserPropertiesComposite userPropertiesComposite;

  private IUser user;

  private Tab defaultTab;

  private Logger logger = CatLogger.getLogger(UserPropertiesDialog.class);

  /**
   * Constructor for UserPropertiesDialog.
   * @param parentShell Shell
   * @param user IUser
   */
  public UserPropertiesDialog(Shell parentShell, IUser user) {
    this(parentShell, user, Tab.ACCOUNT_PROPERTIES_TAB);
  }

  /**
   * Create the dialog
   * 
   * @param parentShell
   * @param user IUser
   * @param defaultTab Tab
   */
  public UserPropertiesDialog(Shell parentShell, IUser user, Tab defaultTab) {
    super(parentShell);
    this.user = user;
    this.defaultTab = defaultTab;
  }

  /**
   * Create contents of the dialog
   * 
   * @param parent
   * @return Control
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);

    final TabFolder tabFolder = new TabFolder(container, SWT.TOP | SWT.MULTI);
    final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    gridData.widthHint = 167;
    tabFolder.setLayoutData(gridData);

    final TabItem accountPropertiesTabItem = new TabItem(tabFolder, SWT.NONE);
    accountPropertiesTabItem.setText("Account Properties");
    userPropertiesComposite = new UserPropertiesComposite(tabFolder, SWT.NULL, user);
    accountPropertiesTabItem.setControl(userPropertiesComposite);

    final TabItem personPropertiesTabItem = new TabItem(tabFolder, SWT.NONE);
    personPropertiesTabItem.setText("Person Properties");
    personPropertiesComposite = new PersonPropertiesComposite(tabFolder, SWT.NULL, this.user);
    personPropertiesTabItem.setControl(personPropertiesComposite);

    switch (this.defaultTab) {
      case ACCOUNT_PROPERTIES_TAB:
        // this is not actually necessary since the first tab is
        // selected by default
        tabFolder.setSelection(accountPropertiesTabItem);
        break;
      case PERSON_PROPERTIES_TAB:
        tabFolder.setSelection(personPropertiesTabItem);
        break;
    }

    return container;
  }

  /**
   * Create contents of the button bar
   * 
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  /**
   * Return the initial size of the dialog
   * @return Point
   */
  @Override
  protected Point getInitialSize() {
    return new Point(500, 375);
  }

  /**
   * Method configureShell.
   * @param newShell Shell
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("User Properties");
  }

  protected void okPressed() {
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
          String mimetype = ResourceService.getMimeType(pictureFile);
          userToUpdate.setPicture(pictureFile);
          userToUpdate.setPictureMimetype(mimetype);
        } catch (FileNotFoundException e) {
          logger.error(e);
          // TODO: alert the user that the file was not found
        }
      }

      try {
        ResourcesPlugin.getSecurityManager().updateUser(userToUpdate);
        
      } catch (Throwable e) {
        ToolErrorHandler.handleError("An error occurred updating user information.", e, true);
      }
    }

    super.okPressed();
  }

  /**
   * Method isDirty.
   * @return boolean
   */
  private boolean isDirty() {
    return !(
    // the dialog is dirty if any of the following is not true
    ResourcePropertiesUtil.stringsEqual(user.getFirstName(), personPropertiesComposite.getFirstNameText().getText()) && ResourcePropertiesUtil.stringsEqual(user.getLastName(), personPropertiesComposite.getLastNameText().getText()) && ResourcePropertiesUtil.stringsEqual(user.getEmail(), personPropertiesComposite.getEmailText().getText()) && ResourcePropertiesUtil.stringsEqual(user.getPhoneNumber(), personPropertiesComposite.getPhoneText().getText()) && personPropertiesComposite.getPictureFile() == null && !personPropertiesComposite.isPictureFileDeleted());
  }
}
