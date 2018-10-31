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
package gov.pnnl.cat.discussion.wizard;

import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IMimetypeManager;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.discussion.DiscussionConstants;
import gov.pnnl.cat.logging.CatLogger;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.webservice.util.Constants;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class AddCommentWizard extends Wizard implements INewWizard{
  private Logger logger = CatLogger.getLogger(this.getClass());
  private ISelection selection;
  private IWorkbenchWindow workbenchWindow;
  private AddCommentWizardPage page;

  public AddCommentWizard(){
    super();
    setWindowTitle("New Comment...");
  }

  /**
   * Method addPages.
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages(){
    page = new AddCommentWizardPage(selection, workbenchWindow);
    addPage(page);
  }

  /**
   * Method performFinish.
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    // TODO Auto-generated method stub
    String subject = page.getSubject();
    String contents = page.getContents();
    IResource resource = page.getResource();

    try{

      //verify user input
      if(contents.trim().length() == 0){
        boolean createTopic = MessageDialog.openConfirm(getShell(), "Empty Message Body", "Are you sure you want to create a topic with no message?");
        if(!createTopic){
          return false;
        }
      }

      //check for subject duplicate
      CmsPath discussionPath = resource.getPath().append(DiscussionConstants.NAME_DISCUSSION);
      CmsPath topicPath = discussionPath.append(subject);
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      ICatCML cml = mgr.getCML();

      if (mgr.resourceExists(topicPath)) {
        MessageDialog.openError(
            getShell(),
            "Duplicate Topic",
            "The subject name you have specified is already in use. Please choose a different one and try again.");
        return false;
      }

      //make sure node has discussable aspect
      if (!resource.hasAspect(DiscussionConstants.ASPECT_DISCUSSABLE)) {
        // unfortunately, we have to add the aspect outside of the CML
        mgr.addAspect(resource.getPath(), DiscussionConstants.ASPECT_DISCUSSABLE);
        //      cml.addAspect(resource.getPath(), DiscussionConstants.ASPECT_DISCUSSABLE);
      }


      //create topic node
      cml.addNode(
          discussionPath,
          VeloConstants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, subject),
          VeloConstants.ASSOC_TYPE_CONTAINS,
          subject,
          DiscussionConstants.TYPE_TOPIC);

      //create post
      StringBuilder sb = new StringBuilder();
      sb.append("posted-");
      sb.append(new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()));
      sb.append(".html");
      String childName = sb.toString();

      cml.addNode(
          topicPath,
          VeloConstants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, childName),
          VeloConstants.ASSOC_TYPE_CONTAINS,
          childName,
          DiscussionConstants.TYPE_POST);

      CmsPath postPath = topicPath.append(childName);
      cml.writeContent(postPath, VeloConstants.PROP_CONTENT, contents.trim(), IMimetypeManager.MIMETYPE_TEXT_PLAIN);
      mgr.executeCml(cml);
    }
    catch (Exception e) {
      ToolErrorHandler.handleError("AnAn error has occurred saving the comment.", e, true);
    }
    return true;
  }

  /**
   * Method init.
   * @param workbench IWorkbench
   * @param selection IStructuredSelection
   * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    // TODO Auto-generated method stub
    this.selection = selection;
    this.workbenchWindow = workbench.getActiveWorkbenchWindow();
  }

}
