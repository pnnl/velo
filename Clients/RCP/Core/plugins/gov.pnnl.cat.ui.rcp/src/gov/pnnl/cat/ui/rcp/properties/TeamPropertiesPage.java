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
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.dialogs.properties.team.TeamPropertiesComposite;
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
public class TeamPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {

  TeamPropertiesComposite teamPropertiesComposite;
  boolean editable;

  private Logger logger = CatLogger.getLogger(TeamPropertiesPage.class);

  public TeamPropertiesPage() {
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
    ITeam team = (ITeam) getElement();
    teamPropertiesComposite = new TeamPropertiesComposite(parent, SWT.NULL, team);
    ISecurityManager mgr = ResourcesPlugin.getSecurityManager();
    IUser currentUser= mgr.getActiveUser();
    if(currentUser.isAdmin() || team.isMember(currentUser.getUsername())) {
      editable = true;
    } else {
      editable = false;
      teamPropertiesComposite.disableComposite();
    }
    return teamPropertiesComposite;
  }

  /**
   * Method performOk.
   * @return boolean
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  @Override
  public boolean performOk() {
    boolean success = true;
    
    if(editable) {
      success = saveInternal();
    }
    super.performOk();

    return success;
  }
  
  protected boolean saveInternal() {
    boolean success = true;
    ITeam team = (ITeam) getElement();
    if (isDirty()) {
      // if the team has changed something, schedule a job to update the team
      ITeam teamToUpdate = team.clone();

      teamToUpdate.setDescription(teamPropertiesComposite.getDescriptionText().getText());
      teamToUpdate.setDeletePicture(teamPropertiesComposite.isPictureFileDeleted());

      File pictureFile = teamPropertiesComposite.getPictureFile();
      if (pictureFile != null) {
        try {
          teamToUpdate.setPicture(pictureFile);
          String mimetype = ResourceService.getMimeType(pictureFile);
          teamToUpdate.setPictureMimetype(mimetype);
        } catch (FileNotFoundException e) {
          logger.error(e);
          // TODO: alert the user that the file was not found
        }
      }

      try {
        ResourcesPlugin.getSecurityManager().updateTeam(teamToUpdate);
      } catch (Throwable e) {
        ToolErrorHandler.handleError("Error updating the team."
            , e, true);
        success = false;
      }
    }
    return success;
  }

  /**
   * Method isDirty.
   * @return boolean
   */
  private boolean isDirty() {
    ITeam team = (ITeam) getElement();
    return !(ResourcePropertiesUtil.stringsEqual(team.getDescription(), teamPropertiesComposite.getDescriptionText().getText()) && teamPropertiesComposite.getPictureFile() == null && !teamPropertiesComposite.isPictureFileDeleted());
  }

}
