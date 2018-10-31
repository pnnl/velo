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
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.dialogs.properties.team.TeamPropertiesWizardComposite;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class NewTeamWizardPage1 extends WizardPage {

  private TeamPropertiesWizardComposite generalTeamPropsComposite;

  // TODO: provide real values for these
  public static final int MIN_TEAMNAME_LENGTH = 3;
  public static final int MAX_TEAMNAME_LENGTH = 50;
  private IStructuredSelection selection; //for parent
  private Logger logger = CatLogger.getLogger(TeamPropertiesWizardComposite.class);
  
  /**
   * Create the wizard
   * @param s IStructuredSelection
   */
  public NewTeamWizardPage1(IStructuredSelection s) {
    super("wizardPage");
    setTitle("Team Information");
    setDescription("Enter information for the new team.");
    selection = s;
  }

  /**
   * Create contents of the wizard
   * @param parent
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    this.generalTeamPropsComposite = new TeamPropertiesWizardComposite(parent, SWT.NULL, null, selection);
    setControl(this.generalTeamPropsComposite);

    ModifyListener dialogChangedModifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    };

    generalTeamPropsComposite.getTeamNameText().addModifyListener(dialogChangedModifyListener);
    generalTeamPropsComposite.getParentText().addModifyListener(dialogChangedModifyListener);
    
    generalTeamPropsComposite.getTeamNameText().setFocus();

    setPageComplete(false);
  }

  private void dialogChanged() {
    String teamname = generalTeamPropsComposite.getTeamNameText().getText();
    
    boolean prohibitedChar = teamname.indexOf('/') >=0 || teamname.indexOf('*') >=0 ||
            teamname.indexOf('\\') >=0 || teamname.indexOf(':') >=0 ||
            teamname.indexOf('?') >=0 || teamname.indexOf('"') >=0 ||
            teamname.indexOf('<') >=0 || teamname.indexOf('>') >=0 ||
            teamname.indexOf('|') >=0;
   if(prohibitedChar) {
      updateStatus("A team name cannot contain any of these characters: \\/:*?\"<>|");
    } else if (teamname.length() < MIN_TEAMNAME_LENGTH) {
      updateStatus("Team name must be at least " + MIN_TEAMNAME_LENGTH + " characters.");
    } else if (teamname.length() > MAX_TEAMNAME_LENGTH) {
      updateStatus("Team name must be " + MAX_TEAMNAME_LENGTH + " characters or fewer.");
    } else {
      // nothing is wrong!
      updateStatus(null);
    }

    validateParentAndTeamName();

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
   * Method getTeamName.
   * @return String
   */
  public String getTeamName() {
    return generalTeamPropsComposite.getTeamNameText().getText();
  }

  /**
   * Method getPictureFile.
   * @return File
   */
  public File getPictureFile() {
    return generalTeamPropsComposite.getPictureFile();
  }

  /**
   * Method getHomeFolder.
   * @return String
   */
  public String getHomeFolder() {
    return generalTeamPropsComposite.getHomeFolderText().getText();
  }

  /**
   * Method getParentTeam.
   * @return String
   */
  public String getParentTeam()
  {
    return generalTeamPropsComposite.getParentTeam();
  }

  /**
   * Method getDesc.
   * @return String
   */
  public String getDesc()
  {
    return generalTeamPropsComposite.getDescriptionText().getText();
  }

  //validate parent and team as we type
  private void validateParentAndTeamName()
  {
    String teamPath = getParentTeam();
    if(teamPath.length() > 0 && !teamPath.startsWith("/"))
    {
      teamPath = "/" + teamPath;
    }

    //need to validate the parent team
    if(teamPath.length() > 0)
    {
      try {
        ITeam parentTeam = ResourcesPlugin.getSecurityManager().getTeam(new CmsPath(teamPath));
        if(parentTeam == null)
        {
          updateStatus("Parent team path not valid.");
          return;
        }

      } catch (Exception e) {
        logger.error(e);
        updateStatus(e.toString());
        return;           
      }

      if(teamPath.length() > 0 && teamPath.endsWith("/"))
      {
        teamPath = teamPath.substring(0, teamPath.length()-1);
      }
    }      

    //check the existence of the team only after team name length is valid
    if(getTeamName().length() >= MIN_TEAMNAME_LENGTH)
    {
      teamPath += "/" + getTeamName();
      //need to check if the team already exists
      ITeam existingTeam;
      try {
        existingTeam = ResourcesPlugin.getSecurityManager().getTeam(new CmsPath(teamPath));
        if(existingTeam != null)
        {
          updateStatus("Team " + teamPath.substring(1) + " already exists.");
          return;
        }
      } catch (Exception e) {
        logger.error(e);
        updateStatus(e.toString());
        return;           
      }
    }

  }
}
