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
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.teams.TeamDetailsView;
import gov.pnnl.cat.ui.rcp.views.teams.TeamTreeDialog;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * This will be used for New User creation
 * Its cousin TeamPropertiesComposite is used for Team Properties for existing teams
 * 
 * The class differs from TeamPropertiesComoposite on several things:
 * 1. Parent is editable
 * 2. Team name is editable
 * 3. Picture: text field and a browse button
 * @author d3m602
 *
 * @version $Revision: 1.0 $
 */
public class TeamPropertiesWizardComposite extends Composite {

  private ITeam team;
  private Text homeFolderText;
  private Text pictureText;
  private Text descriptionText;
  private Text teamNameText;
  private Text parentText;
  Button btnParentTeamBrowse;
  Button browseButton;
  protected static Logger logger = CatLogger.getLogger(TeamPropertiesWizardComposite.class);
  //copied from PersonPropertiesComposite.java
  private final String[] SUPPORTED_IMAGE_TYPES = {
      "*.jpg;*.jpeg;*.gif;*.png;*.bmp"
  };

  /**
   * Constructor for TeamPropertiesWizardComposite.
   * @param parent Composite
   * @param style int
   * @param newTeam ITeam
   * @param selection IStructuredSelection
   */
  public TeamPropertiesWizardComposite(Composite parent, int style, ITeam newTeam, IStructuredSelection selection) {
    super(parent, style);
    this.team = newTeam;

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    setLayout(gridLayout);

    //add parent team field with a browse button
    final Label parentLabel = new Label(this, SWT.NONE);
    parentLabel.setText("Parent team:");

    parentText = new Text(this, SWT.BORDER);
    parentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    //set the parent team path, if in TeamsView there is a team selection
    ITeam parentTeam = null;
    if (selection != null) {
      try {
        parentTeam = ((ITeam) selection.getFirstElement());
        String parentPath = "";
        CmsPath ancester = parentTeam.getParent();
        if(ancester != null)
        {
          parentPath = ancester.toDisplayString();
        }
        parentPath += "/" + parentTeam.getName();
        parentText.setText(parentPath);
      }
      catch (Exception e)
      {
        ; //do nothing, the selection is not a team. Might be from the Data Inspector
      }
    }

    new Label(this, SWT.NONE);

    btnParentTeamBrowse = new Button(this, SWT.NONE);
    final GridData gridData_0 = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    gridData_0.widthHint = 75;
    btnParentTeamBrowse.setLayoutData(gridData_0);
    btnParentTeamBrowse.setText("Browse...");

    btnParentTeamBrowse.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {

        TeamTreeDialog parentTeamDialog = new TeamTreeDialog(getShell());

        try {
          ISecurityManager mgr = ResourcesPlugin.getSecurityManager();
          ITeam selTeam = null;
          String parentTeamStr = parentText.getText();
          if(parentTeamStr.length() != 0 && !parentTeamStr.startsWith("/"))
          {
            parentTeamStr = "/" + parentTeamStr; 
          }
          CmsPath parentTeamPath = new CmsPath(parentTeamStr);
          selTeam = mgr.getTeam(parentTeamPath);

          if (selTeam != null) {
            //EZLogger.logWarning("parent team preset:" + selTeam.getName(), null);
            logger.warn("parent team preset:" + selTeam.getName());
            parentTeamDialog.setSelectedTeam(selTeam);
          }
        } catch (Exception ex) {
          //EZLogger.logError(ex, "Could not open resource chooser dialog.");
          logger.error("Could not open resource chooser dialog.", ex);
        }
        if (parentTeamDialog.open() == Dialog.OK && parentTeamDialog.getSelectedTeam() != null) {
          parentText.setText(parentTeamDialog.getSelectedTeam().getPath().toString());
          dialogChanged();
        }
      }
    });

    final Label teamNameLabel = new Label(this, SWT.NONE);
    teamNameLabel.setText("Team Name:");

    teamNameText = new Text(this, SWT.BORDER);
    teamNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    
    final Label descriptionLabel = new Label(this, SWT.NONE);
    descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    descriptionLabel.setText("Description:");
    
    descriptionText = new Text(this, SWT.WRAP|SWT.BORDER|SWT.V_SCROLL);
    descriptionText.setTextLimit(TeamDetailsView.MAX_DESC_LENGTH);

    final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    gridData.heightHint = 50;
    descriptionText.setLayoutData(gridData);

    final Label label_1 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
    final GridData gridData_2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
    gridData_2.heightHint = 15;
    label_1.setLayoutData(gridData_2);

    final Label pictureLabel = new Label(this, SWT.NONE);
    pictureLabel.setText("Picture:");

    pictureText = new Text(this, SWT.BORDER);
    pictureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    new Label(this, SWT.NONE);

    browseButton = new Button(this, SWT.NONE);
    final GridData gridData_1 = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    gridData_1.widthHint = 75;
    browseButton.setLayoutData(gridData_1);
    browseButton.setText("Browse...");
    browseButton.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        //System.out.println("Clicked");
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
   * Method getParentText.
   * @return Text
   */
  public Text getParentText() {
    return parentText;
  }

  /**
   * Method getParentTeam.
   * @return String
   */
  public String getParentTeam()
  {
    return parentText.getText().toString();
  }

  public void setDisable()
  {
    teamNameText.setEnabled(false);
    pictureText.setEnabled(false);
    descriptionText.setEnabled(false);
    parentText.setEnabled(false);
    btnParentTeamBrowse.setEnabled(false);
    browseButton.setEnabled(false);
  }

  /**
   * Copied from NewFolderWizardPage.java, we might not need this at all
   *
   */
  private void dialogChanged() {
    ;
  }

}
