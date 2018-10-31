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
package gov.pnnl.cat.ui.rcp.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.dialogs.CATSaveAsDialog;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.velo.model.CmsPath;

/**
 */
public class SaveAsHandler extends AbstractHandler {

  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.customSaveAsBehavior";
  protected static final String ATTRIBUTE = "class";
  protected static List<CustomSaveAsBehavior> customSaveAsBehaviors;
  static {
    loadCustomBehaviors();
  }

  private static void loadCustomBehaviors() {
    customSaveAsBehaviors = new ArrayList<CustomSaveAsBehavior>();

    try {
      // look up all the extensions for the LaunchSimulation extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof CustomSaveAsBehavior) {
          CustomSaveAsBehavior behavior = (CustomSaveAsBehavior)obj;
          customSaveAsBehaviors.add(behavior);
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom save as behavior extension points.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IStructuredSelection selection = RCPUtil.getCurrentStructuredSelection(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event));

    final List<IResource> resourcesForDefaultSaveAs = new ArrayList<IResource>();

    // See if we have custom save as behavior for any of the resources
    for (Iterator<?> it = selection.iterator(); it.hasNext();) {

      IResource resource = RCPUtil.getResource(it.next());
      boolean saved = false;

      for(CustomSaveAsBehavior behavior : customSaveAsBehaviors) {
        saved = behavior.saveAs(resource);
        if(saved) {
          break;
        } 
      }
      if(!saved) {
        resourcesForDefaultSaveAs.add(resource);
      }
    }

    if(resourcesForDefaultSaveAs.size() > 0) {     
      for(IResource resource : resourcesForDefaultSaveAs) {
        defaultSaveAs(resource);
      }
    }
    return null;
  }

  /**
   * Method defaultSaveAs.
   * @param resource IResource
   */
  private void defaultSaveAs(IResource resource) {

    Shell shell = Display.getDefault().getActiveShell();

    try {
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      //PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setCursor(new org.eclipse.swt.graphics.Cursor(null, SWT.CURSOR_WAIT));

      // Set the root of the dialog to use the provided root for the deployment
      RepositoryContainer root = RCPUtil.getTreeRoot();
      CATSaveAsDialog saveAsDialog = new CATSaveAsDialog(shell, true, root, null);
      saveAsDialog.setOriginalFile(resource);

      saveAsDialog.create();

      if (saveAsDialog.open() == Dialog.OK && saveAsDialog.getResult() != null) {
        CmsPath destination = saveAsDialog.getResult();

        // Copy resource to new location
        copyResourceWithProgress(resource.getPath(), destination);

      }
    } catch (final Throwable e) {
      StatusUtil.handleStatus("Failed to copy " + resource.getPath().toDisplayString(), e, StatusManager.SHOW);

    }

  }
  
  /**
   * Method copyResourceWithProgress.
   * @param source CmsPath
   * @param destination CmsPath
   */
  private void copyResourceWithProgress(final CmsPath source, final CmsPath destination) {
    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    try {
      service.run(true, false, new IRunnableWithProgress(){
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {      
          IResourceManager mgr = ResourcesPlugin.getResourceManager();
          mgr.copy(source, destination);
        }
      });
    } catch (Exception e) {
      StatusUtil.handleStatus("Failed to copy " + source.toDisplayString(), e, StatusManager.SHOW);
    } 
  }

}
