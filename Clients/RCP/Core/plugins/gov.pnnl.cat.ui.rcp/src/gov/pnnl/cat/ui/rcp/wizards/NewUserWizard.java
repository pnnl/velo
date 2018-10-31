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
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.core.resources.security.User;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 */
public class NewUserWizard extends Wizard implements INewWizard {

  public static final String ID = NewUserWizard.class.getName();
  private IWorkbench workbench;
  private IStructuredSelection selection;
  private NewUserWizardPage1 page1;
  private NewUserWizardPage2 page2;
  private Logger logger = CatLogger.getLogger(NewUserWizard.class);
  
  public NewUserWizard() {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle("New User");
    setDefaultPageImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_USER_NEW, SharedImages.CAT_IMG_SIZE_64));
  }

  /**
   * Method init.
   * @param workbench IWorkbench
   * @param selection IStructuredSelection
   * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.workbench = workbench;
    this.selection = selection;
  }

  /**
   * Method addPages.
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages() {
    page1 = new NewUserWizardPage1();
    page2 = new NewUserWizardPage2();
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
    final User user = new User();
    user.setUsername(page1.getUsername());
    user.setFirstName(page2.getFirstName());
    user.setLastName(page2.getLastName());
    user.setEmail(page2.getEmail());
    user.setPassword(page1.getPassword());
    user.setPhoneNumber(page2.getPhone());

    File pictureFile = page2.getPictureFile();
    if (pictureFile != null) {
      try {
        user.setPicture(pictureFile);
        String mimetype = ResourceService.getMimeType(pictureFile);
        user.setPictureMimetype(mimetype);
      } catch (FileNotFoundException e) {
        logger.error(e);
        // TODO: alert the user that the file was not found
      }
    }

    final boolean[] success = {false};

    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try{
          success[0] = doFinish(user);
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
      
        ToolErrorHandler.handleError("Unable to create user.", e, true);
        return false;
      
    } catch (InterruptedException e) {
      // thrown if the job is canceled, which it cannot be.
      return false;
    }

    return success[0];
  }

  /**
   * Method doFinish.
   * @param user IUser
   * @return boolean
   * @throws CatSecurityException
   * @throws ServerException
   * @throws ResourceException
   */
  private boolean doFinish(IUser user) throws CatSecurityException, ServerException, ResourceException {
    ResourcesPlugin.getSecurityManager().createUser(user);
    return true;
  }
}
