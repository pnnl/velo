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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 * @version $Revision: 1.0 $
 */

public class NewFolderWizard extends Wizard implements INewWizard {
	private NewFolderWizardPage page;
	private ISelection selection;
  private boolean success = false;
  private Logger logger = CatLogger.getLogger(this.getClass());
  private IWorkbenchWindow workbenchWindow;
	/**
	 * Constructor for NewFolderWizard.
	 */
	public NewFolderWizard() {
		super();
		setNeedsProgressMonitor(true);
    setWindowTitle("New Folder...");
    setDefaultPageImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_FOLDER_NEW, SharedImages.CAT_IMG_SIZE_64));
	}
	
	/**
	 * Adding the page to the wizard.
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */

	public void addPages() {
		page = new NewFolderWizardPage(selection, IResource.FOLDER, this.workbenchWindow);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 * @return boolean
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
    success = false;
		final CmsPath parentFolder = page.getParentFolder();
		final String fileName = page.getFolderName();
    
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try{
          success = doFinish(parentFolder, fileName, monitor);
				} finally {
					monitor.done();
				}
        
			}
		};
    try {
      getContainer().run(true, false, op);

      // open the parent folder first
      RCPUtil.selectResourceInTree(parentFolder);
      
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getCause();
      logger.error("Error", realException);
      ToolErrorHandler.handleError("An unexpected error occurred.", e, true);
      return false;
    }
    
    return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
  public void init(IWorkbench workbench, IStructuredSelection selection) { this.selection = selection;
    this.workbenchWindow = workbench.getActiveWorkbenchWindow();
    
  }

	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @param parentFolder CmsPath
	 * @param newFolderName String
	 * @param monitor IProgressMonitor
	 * @return boolean
	 */

	private boolean doFinish(CmsPath parentFolder, String newFolderName,	IProgressMonitor monitor) {
		monitor.beginTask("Creating " + newFolderName, 2);
//    try {
		CmsPath newFolderPath = parentFolder.append(newFolderName);
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      
    
  		try {
        if (mgr.resourceExists(newFolderPath)) {
        	String errMsg = "A folder with the same name already exists.  Rename the new folder and try again.";
        	ToolErrorHandler.handleError(errMsg, null, true);
          return false;
        }
      
        mgr.createFolder(newFolderPath);
    		monitor.worked(1);
      } catch (Throwable e) {
        String errMsg = "An error occurred creating the folder.";
        ToolErrorHandler.handleError(errMsg, e, true);
        return false;
      }
    return true;
	}

  /**
   * Method setNewFolderName.
   * @param rootFolderName String
   */
  public void setNewFolderName(String rootFolderName) {
    page.setFolderName(rootFolderName);
  }
	
	/**
	 * We will initialize file contents with a sample text.
	 */
//
//	private InputStream openContentStream() {
//		String contents =
//			"This is the initial file contents for *.mpe file that should be word-sorted in the Preview page of the multi-page editor";
//		return new ByteArrayInputStream(contents.getBytes());
//	}
//
//	private void throwCoreException(String message) throws CoreException {
//		IStatus status =
//			new Status(IStatus.ERROR, "gov.pnnl.cat.ui.rcp", IStatus.OK, message, null);
//		throw new CoreException(status);
//	}
//
//	/**
//	 * We will accept the selection in the workbench to see if
//	 * we can initialize from it.
//	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
//	 */
//	public void init(IWorkbench workbench, IStructuredSelection selection) {
//		this.selection = selection;
//	}
}
