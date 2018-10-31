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
package gov.pnnl.velo.ui.commands;

import java.io.File;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.ui.validators.NotEmptyStringValidator;
import gov.pnnl.velo.util.VeloConstants;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Create a new model (could be replaced by external tool)
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CreateFolder extends AbstractHandler {
  @SuppressWarnings("unused")
  private static Logger logger = CatLogger.getLogger(CreateFolder.class);

  /**
   * Method execute.
   * @param event ExecutionEvent
   * @return Object
   * @throws ExecutionException
   * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    // Get the current selection 
    IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
      return null;
    }

    try {
      // We know the selection is a folder or else the command will be disabled
      IResource resource = RCPUtil.getResource(selection.getFirstElement());

      // Pop up dialog prompting for folder name
      Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      InputDialog dialog = new InputDialog(shell, "Creating folder in " + resource.getPath().toDisplayString(), "Folder Name:", null, new NotEmptyStringValidator());
      if(dialog.open() == Dialog.OK) {


        // Create folder with collection mimetype
        String folderName = dialog.getValue();

        //make sure that the folder name does not contain any invalid characters

        if(folderName.matches(IResource.invalidCharactersRegex)) {
          String errorMsg = "A folder name " + IResource.invalidCharactersMsg;
          ToolErrorHandler.handleError(errorMsg, null, true);
        
        } else {
          IResource folder = mgr.createFolder(resource.getPath().append(folderName), VeloConstants.MIMETYPE_COLLECTION);

          // request to select the newly created object
          RCPUtil.selectResourceInTree(folder.getPath());
        }

      }
      
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Unable to create folder.", e, true);
    }
    return null;
  }

}
