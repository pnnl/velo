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

import gov.pnnl.cat.core.internal.resources.ResourceService;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.core.resources.security.Team;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 */
public class NewTeamWizard extends Wizard implements INewWizard {

  protected static Logger logger = CatLogger.getLogger(NewTeamWizard.class);
  public static final String ID = NewTeamWizard.class.getName();

  private IWorkbench workbench;

  private IStructuredSelection selection;

  private NewTeamWizardPage1 page1;

  private NewTeamWizardPage2 page2;

  public NewTeamWizard() {
    super();
    // setNeedsProgressMonitor(true);
    setWindowTitle("New Team");
    setDefaultPageImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_TEAM_NEW, SharedImages.CAT_IMG_SIZE_64));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.workbench = workbench;
    this.selection = selection;
  }

  /**
   * Method addPages.
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages() {
    page1 = new NewTeamWizardPage1(selection);
    page2 = new NewTeamWizardPage2();
    addPage(page1);
    addPage(page2);
  }

  /**
   * Method performFinish.
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish() {

    final Team team = new Team();

    // get the team path
    String teamPath = page1.getParentTeam();
    if (teamPath.length() > 0 && !teamPath.startsWith("/")) {
      teamPath = "/" + teamPath;
    }

    if (teamPath.length() > 0 && teamPath.endsWith("/")) {
      teamPath = teamPath.substring(0, teamPath.length() - 1);
    }
    teamPath += "/" + page1.getTeamName();

    team.setPath(new CmsPath(teamPath));

    // team.setHomeFolder(page1.getHomeFolder()); //IPath?
    team.setDescription(page1.getDesc());

    // get the current user
    IUser me = ResourcesPlugin.getSecurityManager().getActiveUser();
    boolean meIncluded = false;

    IUser[] members = page2.getMembers();
    for (int i = 0; i < members.length; i++) {
      String userName = members[i].getUsername();
      if (!userName.equals("admin") && !userName.equals("guest")) {
        team.addMember(userName);
      }
      if (members[i].getID().equals(me.getID())) {
        meIncluded = true;
      }
    }

    if (!meIncluded && !(me.isAdmin())) {
      MessageDialog.openInformation(getShell(), "Team Creator a Default Team Member", "As a non-admininstrative user, you will be automatically included in the team you create.");
    }

    File pictureFile = page1.getPictureFile();
    if (pictureFile != null) {
      try {
        team.setPicture(pictureFile);
        String mimetype = ResourceService.getMimeType(pictureFile);
        team.setPictureMimetype(mimetype);
        // EZLogger.logWarning("team: pictureFile:" + pictureFile.getAbsolutePath(), null);
        logger.debug("team: pictureFile:" + pictureFile.getAbsolutePath());
      } catch (FileNotFoundException e) {
        logger.error(e);
        // TODO: alert the user that the file was not found
      }
    }

    final boolean[] success = { false };

    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          success[0] = doFinish(team);
        } catch (Exception e) {
          throw new InvocationTargetException(e);
        } finally {
          monitor.done();
        }
      }
    };

    try {
      getContainer().run(true, false, runnable);
      
    } catch (InvocationTargetException e) {
      String errMsg = "An error occurred creating the team.";
      ToolErrorHandler.handleError(errMsg, e, true);
      return false;
      
    } catch (InterruptedException e) {
      // thrown if the job is canceled, which it cannot be.
      return false;
    }

    return success[0];
  }

  /**
   * Method doFinish.
   * @param team ITeam
   * @return boolean
   * @throws CatSecurityException
   * @throws ServerException
   * @throws ResourceException
   */
  private boolean doFinish(ITeam team) throws CatSecurityException, ServerException, ResourceException {
    ResourcesPlugin.getSecurityManager().createTeam(team);
    return true;
  }

}
