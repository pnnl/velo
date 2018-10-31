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

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.views.profile.ProfileDetailsView;
import gov.pnnl.cat.ui.rcp.views.users.GetProfileImageJob;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 */
public class PersonPropertiesComposite extends Composite {
  private Text pictureText;
  private Text phoneText;
  private Text emailText;
  private Text lastNameText;
  private Text firstNameText;
  private Label label;
  private Label blankLabel;
  private Image personImage;
  private Button browseButton;
  private Button backButton;
  private Button changePictureButton;
  private Button deletePictureButton;

  private boolean changePicture;
  private String previousPictureText;
  private boolean deletePicture = false;

  protected static Logger logger = CatLogger.getLogger(PersonPropertiesComposite.class);
  
  private final String[] SUPPORTED_IMAGE_TYPES = {
      "*.jpg;*.jpeg;*.gif;*.png;*.bmp"
  };

  /**
   * Constructor for PersonPropertiesComposite.
   * @param parent Composite
   * @param style int
   */
  public PersonPropertiesComposite(Composite parent, int style) {
    this(parent, style, null);
  }

  /**
   * Constructor for PersonPropertiesComposite.
   * @param parent Composite
   * @param style int
   * @param user IUser
   */
  public PersonPropertiesComposite(Composite parent, int style, final IUser user) {
    super(parent, style);

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    setLayout(gridLayout);

    final Label firstNameLabel = new Label(this, SWT.NONE);
    firstNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    firstNameLabel.setText("First Name:");

    firstNameText = new Text(this, SWT.BORDER);
    firstNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    final Label lastNameLabel = new Label(this, SWT.NONE);
    lastNameLabel.setText("Last Name:");

    lastNameText = new Text(this, SWT.BORDER);
    lastNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    final Label emailLabel = new Label(this, SWT.NONE);
    emailLabel.setText("Email:");

    emailText = new Text(this, SWT.BORDER);
    emailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    final Label phoneLabel = new Label(this, SWT.NONE);
    phoneLabel.setText("Phone:");

    phoneText = new Text(this, SWT.BORDER);
    phoneText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    final Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

    final Label pictureLabel = new Label(this, SWT.NONE);
    pictureLabel.setLayoutData(new GridData());
    pictureLabel.setText("Picture:");

    // determine if we are going to show the user his picture, or show a
    // Browse... button for him to change it.
    changePicture = user == null || !user.hasPicture();

    if (changePicture) {
      createPictureChangeWidgets();
    } else {
      createPictureViewWidgets(user);
    }


    if (user != null) {
      firstNameText.setText(toString(user.getFirstName()));
      lastNameText.setText(toString(user.getLastName()));
      emailText.setText(toString(user.getEmail()));
      phoneText.setText(toString(user.getPhoneNumber()));
      //      piOctureText.setText(user.getPicture().toString());
      IUser loggedInUser = ResourcesPlugin.getSecurityManager().getActiveUser();
      if( (!loggedInUser.isAdmin() && !loggedInUser.equals(user)) || user.getUsername().equals("guest") )
      {
        firstNameText.setEnabled(false);
        lastNameText.setEnabled(false);
        emailText.setEnabled(false);
        phoneText.setEnabled(false);
        disableControl(pictureText);
        disableControl(browseButton);
        disableControl(deletePictureButton);
        disableControl(changePictureButton);
      }
     
    }
  }
  
  /**
   * Method disableControl.
   * @param control Control
   */
  private void disableControl(Control control)
  {
    if(control != null) {
      control.setEnabled(false);
    }
  }
  

  /**
   * Creates the UI components that allow the user to browse and select a file.
   */
  private void createPictureChangeWidgets() {
    // 1. CHANGE THE PICTURE
    pictureText = new Text(this, SWT.BORDER);
    pictureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    if (previousPictureText != null) {
      pictureText.setText(previousPictureText);
    }

    browseButton = new Button(this, SWT.NONE);
    GridData gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    gridData.widthHint = 75;
    browseButton.setLayoutData(gridData);
    browseButton.setText("Browse...");
    browseButton.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        dialog.setFilterExtensions(SUPPORTED_IMAGE_TYPES);
        dialog.setFilterPath(null);
        String result = dialog.open();
        if (result != null) {
          pictureText.setText(result);
        }
      }
    });
  }

  /**
   * Disposes the UI components that were created in createPictureChangeWidgets.
   */
  private void disposePictureChangeWidgets() {
    previousPictureText = pictureText.getText();
    pictureText.dispose();
    browseButton.dispose();

    if (backButton != null && !backButton.isDisposed()) {
      backButton.dispose();
    }
  }

  /**
   * Creates the UI components that allow the user to view, change, or delete
   * the image.
   * @param user the user to whom the image belongs
   */
  private void createPictureViewWidgets(final IUser user) {
    // 2. DISPLAY THE PICTURE
    label = new Label(this, SWT.BORDER);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 2));

    // create a job to download the picture
    final GetProfileImageJob getImageJob = new GetProfileImageJob("Downloading User Image", user);
    getImageJob.setPriority(Job.DECORATE);
    getImageJob.setSystem(true);

    // add a listener so that when the job completes (i.e. the image is
    // downloaded), we update the label to display the image
    getImageJob.addJobChangeListener(new JobChangeAdapter() {
      public void done(IJobChangeEvent event) {
        final ImageData imgData = getImageJob.getImageData();
        if (imgData != null) {
          Display.getDefault().asyncExec(new Runnable() {
            public void run() {
              try {
                // scale the image down to a reasonable size
                ImageData imgDataScaled = null;
                try {
                  imgDataScaled = ProfileDetailsView.scaleImage(imgData, 300, 130);
                } catch (Exception e) {
                  ImageDescriptor imgDesc = SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_PERSON_PORTRAIT_MALE_ERROR, SharedImages.CAT_IMG_SIZE_48);
                  imgDataScaled = ProfileDetailsView.scaleImage(imgDesc.getImageData(), 300, 130);
                }

                // create the image to display.
                // we'll have to dispose of this later.
                personImage = new Image(getDisplay(), imgDataScaled);
                label.setSize(imgDataScaled.height, imgDataScaled.width);
                label.setImage(personImage);
                PersonPropertiesComposite.this.layout();
              } catch (Exception e) {
                //EZLogger.logWarning("Unable to create ImageData for " + user.getUsername(), e);
                logger.warn("Unable to create ImageData for " + user.getUsername(), e);
              }
            }
          });
        }
      }
    });
    getImageJob.schedule();

    changePictureButton = new Button(this, SWT.NONE);
    final GridData gridData_1 = new GridData(SWT.RIGHT, SWT.TOP, false, false);
    gridData_1.widthHint = 75;
    changePictureButton.setLayoutData(gridData_1);
    changePictureButton.setText("Change");
    blankLabel = new Label(this, SWT.NONE);

    deletePictureButton = new Button(this, SWT.NONE);
    final GridData gridData_2 = new GridData(SWT.RIGHT, SWT.TOP, false, false);
    gridData_2.widthHint = 75;
    deletePictureButton.setLayoutData(gridData_2);
    deletePictureButton.setText("Delete");

    // when the user clicks the Delete button, all we do is set a flag
    // and change the layout.
    deletePictureButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        deletePicture = true;
        diposePictureViewWidgets();
        createPictureChangeWidgets();
        PersonPropertiesComposite.this.layout();
      }
    });

    // when the user clicks the Change button, we have to change the layout
    // and add an extra button at the bottom to let them come back to see
    // their picture again.
    changePictureButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        diposePictureViewWidgets();
        createPictureChangeWidgets();

        backButton = new Button(PersonPropertiesComposite.this, SWT.NONE);
        final GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1);
        backButton.setLayoutData(gridData);
        backButton.setText("Back to Current Picture");

        // when the user clicks the Back to Current Picture button, we need
        // to change the layout back so they can see the picture again.
        backButton.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            disposePictureChangeWidgets();
            createPictureViewWidgets(user);

            PersonPropertiesComposite.this.layout();
          }
        });

        PersonPropertiesComposite.this.layout();
      }
    });
  }

  /**
   * Disposes the UI components that were created in createPictureViewWidgets.
   */
  private void diposePictureViewWidgets() {
    label.dispose();
    blankLabel.dispose();
    changePictureButton.dispose();
    deletePictureButton.dispose();
  }

  public void dispose() {
    // we have to dispose the image
    if (personImage != null && !personImage.isDisposed()) {
      personImage.dispose();
    }
  }

  /**
   * Method toString.
   * @param s String
   * @return String
   */
  private String toString(String s) {
    if (s == null) {
      return "";
    }
    return s.toString();
  }

  /**
   * Method getPictureText.
   * @return Text
   */
  public Text getPictureText() {
    return this.pictureText;
  }

  /**
   * Method getFirstNameText.
   * @return Text
   */
  public Text getFirstNameText() {
    return this.firstNameText;
  }

  /**
   * Method getLastNameText.
   * @return Text
   */
  public Text getLastNameText() {
    return this.lastNameText;
  }

  /**
   * Method getEmailText.
   * @return Text
   */
  public Text getEmailText() {
    return this.emailText;
  }

  /**
   * Method getPhoneText.
   * @return Text
   */
  public Text getPhoneText() {
    return this.phoneText;
  }

  /**
   * Method getPictureFile.
   * @return File
   */
  public File getPictureFile() {
    if (pictureText == null || pictureText.isDisposed()) {
      return null;
    }

    String pictureLocation = pictureText.getText();

    if (pictureLocation == null || pictureLocation.trim().length() == 0) {
      return null;
    }

    return new File(pictureLocation);
  }

  /**
   * Method isPictureFileDeleted.
   * @return boolean
   */
  public boolean isPictureFileDeleted() {
    // they had to hit the delete button AND not have specified a new file
    // for the file to be deleted.
    return this.deletePicture && getPictureFile() == null;
  }
}
