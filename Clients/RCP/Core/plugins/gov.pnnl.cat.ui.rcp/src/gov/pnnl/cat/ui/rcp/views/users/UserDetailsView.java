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
package gov.pnnl.cat.ui.rcp.views.users;

import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.views.profile.ProfileDetailsView;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 */
public class UserDetailsView extends ProfileDetailsView {

  private Label firstName;
  private Label lastName;
  private Label email;
  private Label phone;
  //private Label username;
  private ScrolledComposite scrolledComposite;
  private Composite composite;
  private Button sysAdmin;
  private Label pictureLabel;
  private static Logger logger = CatLogger.getLogger(UserDetailsView.class);

  public UserDetailsView() {
  }

  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    composite = new Composite(scrolledComposite, SWT.NONE);
    scrolledComposite.setContent(composite);

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    gridLayout.marginTop = 20;
    gridLayout.marginLeft = 20;
    composite.setLayout(gridLayout);

    final Label firstNameLabel = new Label(composite, SWT.NONE);
    firstNameLabel.setLayoutData(new GridData());
    firstNameLabel.setText("First Name:");

    firstName = new Label(composite, SWT.NONE);
    firstName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    pictureLabel = new Label(composite, SWT.NULL);
    final GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 7);
    pictureLabel.setLayoutData(gridData);

    final Label lastNameLabel = new Label(composite, SWT.NONE);
    lastNameLabel.setLayoutData(new GridData());
    lastNameLabel.setText("Last Name:");

    lastName = new Label(composite, SWT.NONE);
    lastName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    final Label emailLabel = new Label(composite, SWT.NONE);
    emailLabel.setLayoutData(new GridData());
    emailLabel.setText("Email:");

    email = new Label(composite, SWT.NONE);
    email.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    final Label phoneLabel = new Label(composite, SWT.NONE);
    phoneLabel.setText("Phone:");

    phone = new Label(composite, SWT.NONE);
    phone.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    // You shouldn't be able to see the user id for anyone but yourself
//    final Label userIdLabel = new Label(composite, SWT.NONE);
//    userIdLabel.setLayoutData(new GridData());
//    userIdLabel.setText("Username:");
//    username = new Label(composite, SWT.NONE);
//    username.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
// 
    new Label(composite, SWT.NONE);
    sysAdmin = new Button(composite, SWT.CHECK);
    sysAdmin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    sysAdmin.setText("System Administrator");
    sysAdmin.setEnabled(false);

    getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(CatViewIDs.USERS, this);
    layout();
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(CatViewIDs.USERS, this);
    super.dispose();
  }

  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
    this.composite.setFocus();
  }

  /**
   * Method setImage.
   * @param img Image
   */
  protected void setImage(Image img) {
    pictureLabel.setImage(img);
  }

  protected void layout() {
    composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    composite.layout();
    scrolledComposite.layout();
  }

  /**
   * Method getDefaultImageDescriptor.
   * @return ImageDescriptor
   */
  protected ImageDescriptor getDefaultImageDescriptor() {
    return SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_PERSON_PORTRAIT_MALE, SharedImages.CAT_IMG_SIZE_48);
  }

  /**
   * Method getErrorImageDescriptor.
   * @return ImageDescriptor
   */
  protected ImageDescriptor getErrorImageDescriptor() {
    return SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_PERSON_PORTRAIT_MALE_ERROR, SharedImages.CAT_IMG_SIZE_48);
  }

  /**
   * Method selectionChanged.
   * @param profilable IProfilable
   */
  public void selectionChanged(IProfilable profilable) {

    if (profilable == null) {
      // clear the view
      this.email.setText("");
      this.firstName.setText("");
      this.lastName.setText("");
      this.phone.setText("");
      //this.username.setText("");
      this.sysAdmin.setSelection(false);

    } else {
      if (profilable instanceof IUser) {
        IUser user = (IUser) profilable;

        this.email.setText(getNonNullString(user.getEmail()));
        this.firstName.setText(getNonNullString(user.getFirstName()));
        this.lastName.setText(getNonNullString(user.getLastName()));
        this.phone.setText(getNonNullString(user.getPhoneNumber()));
        //this.username.setText(getNonNullString(user.getUsername()));
        this.sysAdmin.setSelection(user.isAdmin());
      }
    }
  }


}
