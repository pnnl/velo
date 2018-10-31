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

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.jobs.SendToTaxonomyJob;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class SendToTaxonomyWizard extends Wizard implements INewWizard{

  private IWorkbenchWindow workbench;
  private ISelection selection;
  private SendToTaxonomyWizardPage page;
  private IFolder lastDestinationFolder = null;
  private IResourceManager mgr = ResourcesPlugin.getResourceManager();

  private static Logger logger = CatLogger.getLogger(SendToTaxonomyWizard.class);

  /**
   * Constructor
   */
  public SendToTaxonomyWizard(){
    super();
  }

  /**
   * Method performFinish.
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish() {

    boolean success = false;

    try {
      CmsPath destFolder = page.getDestinationFolder();
      IResource destination = mgr.getResource(destFolder);
      IFolder destinationFolder = (IFolder) destination;
      lastDestinationFolder = destinationFolder;
      List<IResource> selectedResources = getSelection();

      SendToTaxonomyJob job = new SendToTaxonomyJob(lastDestinationFolder, selectedResources);
      job.setPriority(Job.LONG);
      job.setUser(true);
      job.schedule();
      success = true;

    } catch (Throwable e) {
      ToolErrorHandler.handleError("Could not send files to taxonomy.", e, true);
    }

    return success;
  }

  /**
   * Method addPages.
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  @Override
  public void addPages() {
    page = new SendToTaxonomyWizardPage(selection, workbench);
    addPage(page);
  }

  /**
   * Method init.
   * @param workbench IWorkbench
   * @param selection IStructuredSelection
   * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.workbench = workbench.getActiveWorkbenchWindow();
    this.selection = selection;
  }


  /**
   * Converts the current selection to a List of IResource objects.
  
   * @return List<IResource>
   */
  protected List<IResource> getSelection() {
    List<IResource> ret = new ArrayList<IResource>();

    // Get the selection as a list of IResources
    // The server action will iterate through folders and ignore any
    // files that don't have raw text available 
    if (selection != null && (selection instanceof StructuredSelection)) {

      StructuredSelection structuredSelection = (StructuredSelection)selection;
      Iterator iterator = structuredSelection.iterator();
      IResource resource;

      while (iterator.hasNext()) {
        Object item = iterator.next();
        resource = RCPUtil.getResource(item);
        if (resource == null) {
          logger.warn("Unexpected item encountered: " + item);
        } else {
          ret.add(resource);
        }      
      }      
    } else {
      logger.debug("invalid selection");
    }
    return ret;
  }
}
