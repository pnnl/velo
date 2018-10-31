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
package gov.pnnl.cat.ui.rcp.dialogs.properties.team;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.profile.ProfileDetailsView;
import gov.pnnl.cat.ui.rcp.views.teams.TeamDetailsView;
import gov.pnnl.cat.ui.rcp.views.users.GetProfileImageJob;
import gov.pnnl.velo.core.util.ToolErrorHandler;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 */
public class TeamPropertiesComposite extends Composite {

  private ITeam team;
  private Text homeFolderText;
  private Text pictureText;
  private Text descriptionText;
  private Text teamNameText;
  private Text parentText;

  private Label label;
  private Label blankLabel;
  Button browseButton;
  private Image teamImage;
  private Button backButton;
  private Button changePictureButton;
  private Button deletePictureButton;
  private boolean changePicture;
  private String previousPictureText;
  private boolean deletePicture = false;

  public static final int MAX_DESC_SIZE = 180;
  protected static Logger logger = CatLogger.getLogger(TeamPropertiesComposite.class);
  //copied from PersonPropertiesComposite.java
  private final String[] SUPPORTED_IMAGE_TYPES = {
      "*.jpg;*.jpeg;*.gif;*.png;*.bmp"
  };

  /**
   * Constructor for TeamPropertiesComposite.
   * @param parent Composite
   * @param style int
   * @param newTeam ITeam
   */
  public TeamPropertiesComposite(Composite parent, int style, ITeam newTeam) {
    super(parent, style);
    this.team = newTeam;

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    setLayout(gridLayout);

    //add parent team field without a browse button
    final Label parentLabel = new Label(this, SWT.NONE);
    parentLabel.setText("Parent team:");

    parentText = new Text(this, SWT.BORDER);
    parentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    parentText.setEditable(false);

    final Label teamNameLabel = new Label(this, SWT.NONE);
    teamNameLabel.setText("Team Name:");

    teamNameText = new Text(this, SWT.BORDER);
    teamNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    teamNameText.setEditable(false);

    final Label descriptionLabel = new Label(this, SWT.NONE);
    descriptionLabel.setText("Description:");

    //descriptionText = new Text(this, SWT.BORDER);
    descriptionText = new Text(this, SWT.WRAP|SWT.BORDER|SWT.V_SCROLL);
    final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
    gridData.heightHint = 50;
    descriptionText.setLayoutData(gridData);
    descriptionText.setTextLimit(TeamDetailsView.MAX_DESC_LENGTH);

    //Copied from UserPropertiesComposite for SCR212
    //TODO: see if we can do some refactoring for code reuse
    final Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

    final Label homeFolderLabel = new Label(this, SWT.NONE);
    homeFolderLabel.setText("Home Folder:");

    homeFolderText = new Text(this, SWT.BORDER);
    homeFolderText.setEditable(false);
    homeFolderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    
    final Button findHomeButton = new Button(this, SWT.NONE);
    final GridData gridData_1 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1);
    findHomeButton.setLayoutData(gridData_1);
    findHomeButton.setText("Go To Home Folder");
    findHomeButton.setToolTipText("Navigate to this user's home folder");

    if(team.getHomeFolder() != null){
      homeFolderText.setText(team.getHomeFolder().toDisplayString());
    }else{
      homeFolderText.setText("Not Visible");
      findHomeButton.setEnabled(false);
    }

    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

    findHomeButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent event) {
        CmsPath homeFolder = team.getHomeFolder();
        if (team != null && homeFolder != null) {
          try {
            RCPUtil.selectResourceInTree(homeFolder);   
          } catch (ResourceException e) {
            ToolErrorHandler.handleError("Unable to navigate to resource.", e, true);
          }
        }
      }
    });

    final Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
    final GridData gridData_2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
    gridData_2.heightHint = 15;
    separator.setLayoutData(gridData_2);

    final Label pictureLabel = new Label(this, SWT.NONE);
    pictureLabel.setText("Picture:");

    // determine if we are going to show the team picture, or show a
    // Browse... button for him to change it.
    changePicture = !team.hasPicture();
    if (changePicture) {
      createPictureChangeWidgets();
    } else {
      createPictureViewWidgets(team);
    }

    setGeneralInfo();
  }
  
  public void disableComposite() {
    descriptionText.setEditable(false);
    pictureText.setEditable(false);
    browseButton.setEnabled(false);
  }

  //this will be called only for an existing team
  //so we don't need to do that in the constructor - that is used in both
  //existing team and a new team creation
  public void setGeneralInfo()
  {
    if(team == null)
    {
      return;
    }

    descriptionText.setText(team.getDescription());
    
    teamNameText.setText(team.getName());
    if(team.getParent() != null)
    {
      parentText.setText(team.getParent().toDisplayString());
    }

  }
  
  /**
   * Method getDescriptionText.
   * @return Text
   */
  public Text getDescriptionText() {
    return descriptionText;
  }

  /**
   * Method getHomeFolderText.
   * @return Text
   */
  public Text getHomeFolderText() {
    return homeFolderText;
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
   * Method getTeamNameText.
   * @return Text
   */
  public Text getTeamNameText() {
    return teamNameText;
  }
  
  /**
   * Method getParentTeam.
   * @return String
   */
  public String getParentTeam()
  {
    return parentText.getText().toString();
  }

  
  /**
   * Copied from NewFolderWizardPage.java, we might not need this at all
   *
   */
  private void dialogChanged() {
    ;
  }

  /**
   * Creates the UI components that allow the user to view, change, or delete
   * the image.
  
   * @param team ITeam
   */
  private void createPictureViewWidgets(final ITeam team) {
    // 2. DISPLAY THE PICTURE
    label = new Label(this, SWT.BORDER);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 2));

    // create a job to download the picture
    final GetProfileImageJob getImageJob = new GetProfileImageJob("Downloading Team Image", team);
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
                //TODO: use ProfileDetailsView?
                // scale the image down to a reasonable size
                ImageData imgDataScaled = null;
                try {
                  imgDataScaled = ProfileDetailsView.scaleImage(imgData, 300, 130);
                } catch (Exception e) {
                  ImageDescriptor imgDesc = SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_TEAMS_ERROR, SharedImages.CAT_IMG_SIZE_48);
                  imgDataScaled = ProfileDetailsView.scaleImage(imgDesc.getImageData(), 300, 130);
                }

                // create the image to display.
                // we'll have to dispose of this later.
                teamImage = new Image(getDisplay(), imgDataScaled);
                label.setSize(imgDataScaled.height, imgDataScaled.width);
                label.setImage(teamImage);
                TeamPropertiesComposite.this.layout();
              } catch (Exception e) {
                //EZLogger.logWarning("Unable to create ImageData for " + team.getName(), e);
                logger.warn("Unable to create ImageData for " + team.getName(),e);
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
        TeamPropertiesComposite.this.layout();
      }
    });

    // when the user clicks the Change button, we have to change the layout
    // and add an extra button at the bottom to let them come back to see
    // their picture again.
    changePictureButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        diposePictureViewWidgets();
        createPictureChangeWidgets();

        backButton = new Button(TeamPropertiesComposite.this, SWT.NONE);
        final GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false, 3, 1);
        backButton.setLayoutData(gridData);
        backButton.setText("Back to Current Picture");

        // when the user clicks the Back to Current Picture button, we need
        // to change the layout back so they can see the picture again.
        backButton.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            disposePictureChangeWidgets();
            createPictureViewWidgets(team);
            TeamPropertiesComposite.this.layout();
          }
        });

        TeamPropertiesComposite.this.layout();
      }
    });
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
   * Disposes the UI components that were created in createPictureViewWidgets.
   */
  private void diposePictureViewWidgets() {
    label.dispose();
    blankLabel.dispose();
    changePictureButton.dispose();
    deletePictureButton.dispose();
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
