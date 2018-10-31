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
package gov.pnnl.cat.ui.rcp.actions;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.util.WindowsExec;
import gov.pnnl.cat.net.VeloNetworkPlugin;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class ChooseProgramAction extends Action {
  private IStructuredSelection selection;

  public ChooseProgramAction() {
    super("&Choose Program...");
  }

  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  @Override
  public void run() {
    boolean error = false;
    Exception exception = null;
    Shell shell = Display.getDefault().getActiveShell();

    try {
      IFile fileResource = (IFile) RCPUtil.getResource(selection.getFirstElement());
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      File localFile = null;
      
      if(VeloNetworkPlugin.getVeloFileSystemManager().isLocalDriveEnabled()) {
        // use mapped file path so we can save directly to server
        localFile = new File(VeloNetworkPlugin.getVeloFileSystemManager().getLocalFilePath(fileResource.getPath().toDisplayString()));
        
      } else {
        // otherwise open from file cache
        localFile = mgr.getContentPropertyAsFile(fileResource.getPath(), VeloConstants.PROP_CONTENT);
      }
      
      String path = localFile.getAbsolutePath();

      // This code might look strange, and you might think "What is he doing here?" but I assure you,
      // it seemed necessary for a particular person at a particular time.
      //
      // The problem is that when you call Runtime.exec(String[]), quotes are added automatically
      // for you if any String in the array contains a space. Normally this is a very convenient feature
      // but in this particular case it gets in the way.
      //
      // EXAMPLE:
      // executing the following:
      //    rundll32.exe shell32.dll,OpenAs_RunDLL "C:\f oo.txt"
      //    (yes there is a space between the "f" and the "oo.")
      // produces a dialog that says: Windows cannot open this file. File: f oo.txt"
      // See the problem? The quote is included in the file extension. This not only looks bad,
      // but also causes incorrect behavior, as Windows does not know what application to open files
      // that end with .txt", so it does't suggest the usual suspects (Notepad, Wordpad, etc.).
      //
      // SOLUTION:
      // The solution I came up with was to split the file's path on spaces, and pass them to
      // Runtime.exec split apart. Then, Runtime.exec puts the pieces back together and we get
      // our spaces back - without the quotes.

      String[] pathSegments = path.split(" ");
      String[] params = new String[2+pathSegments.length];

      params[0] = "rundll32.exe";
      params[1] = "shell32.dll,OpenAs_RunDLL";
      System.arraycopy(pathSegments, 0, params, 2, pathSegments.length);
      
      WindowsExec exec = new WindowsExec(params);
      int result = exec.exec();
      if (result != 0) {
        error = true;
      }
    } catch (Exception e) {
      error = true;
      exception = e;
    }

    if (error) {
      ToolErrorHandler.handleError("An unexpected error occurred.", exception, true);
    }
  }

  /**
   * Method getEnabledStatus.
   * @param newSelection IStructuredSelection
   * @return boolean
   */
  public boolean getEnabledStatus(IStructuredSelection newSelection) {
    selection = newSelection;

    // action is enable for a selection that contain only a single file
    if (newSelection.toArray().length == 1) {
      setEnabled(RCPUtil.getResource(newSelection.getFirstElement()) instanceof IFile);
    } else {
      setEnabled(false);
    }

    return isEnabled();
  }
}
