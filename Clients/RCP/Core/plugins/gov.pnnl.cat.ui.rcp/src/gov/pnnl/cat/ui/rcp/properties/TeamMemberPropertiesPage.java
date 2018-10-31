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

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.dialogs.properties.team.TeamMembersPropertiesComposite;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 */
public class TeamMemberPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {

  TeamMembersPropertiesComposite teamMembersPropertiesComposite;
  boolean editable = false;

  protected static Logger logger = CatLogger.getLogger(TeamMemberPropertiesPage.class);

  public TeamMemberPropertiesPage() {
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
    teamMembersPropertiesComposite = new TeamMembersPropertiesComposite(parent, SWT.NULL, team);
    ISecurityManager mgr = ResourcesPlugin.getSecurityManager();
    IUser currentUser= mgr.getActiveUser();
    if(currentUser.isAdmin() || team.isMember(currentUser.getUsername())) {
      editable = true;
    } else {
      editable = false;
      teamMembersPropertiesComposite.disableComposite();
    }
    return teamMembersPropertiesComposite;
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
    ITeam team = (ITeam) getElement();
    boolean success = true;
    if (isDirty()) {
      // if the team has changed something, schedule a job to update the team
      ITeam teamToUpdate = team.clone();

      // add the team members
      List<String> mbs = new ArrayList<String>(0);
      teamToUpdate.setMembers(mbs);
      IUser[] members = teamMembersPropertiesComposite.getMembers();
      int userCount = members.length;
      if (userCount < 1) {
        MessageDialog.openError(getShell(), "Error Save Team", "A team must have at least one member.");
        return false;
      }
      try {

        // get the current user
        IUser me = ResourcesPlugin.getSecurityManager().getActiveUser();

        boolean meIncluded = false;
        for (int i = 0; i < members.length; i++) {
          if (members[i].getID().equals(me.getID())) {
            meIncluded = true;
          }
          String userName = members[i].getUsername();
          if (!userName.equals("admin") && !userName.equals("guest")) {
            // EZLogger.logWarning("add user:" + userName, null);
            logger.warn("add user:" + userName);
            teamToUpdate.addMember(userName);
          }
        }
        if (!meIncluded && !me.isAdmin()) {
          String dialogMsg = "If you remove yourself from this team, you will no longer have access. Are you sure you want to do this?";
          MessageDialog dialog = new MessageDialog(getShell(), "Confirm Remove Yourself", null, dialogMsg, MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
          if (dialog.open() == MessageDialog.CANCEL) {
            return false;
          }
        }

        ResourcesPlugin.getSecurityManager().updateTeam(teamToUpdate);
      } catch (Throwable e) {
        if(e.getMessage().contains("Access denied")) {
          ToolErrorHandler.handleError("You do not have sufficient permissions to edit this team.", e, true);
        } else {
          ToolErrorHandler.handleError("Error updating the team."
              , e, true);
          success = false;
        }
      }
    }

    super.performOk();
    return success;
  }

  /**
   * Method isDirty.
   * @return boolean
   */
  private boolean isDirty() {
    ITeam team = (ITeam) getElement();
    String[] teamMembersOld = team.getMembers().toArray(new String[team.getMembers().size()]);
    // TODO: make sure that team.getMembers() returns user name only,
    // otherwise change TeamMemberPropertiesComposite
    String[] teamMembersNew = teamMembersPropertiesComposite.getMembersString();
    Arrays.sort(teamMembersOld);
    Arrays.sort(teamMembersNew);

    return !(Arrays.equals(teamMembersOld, teamMembersNew));
  }

}
