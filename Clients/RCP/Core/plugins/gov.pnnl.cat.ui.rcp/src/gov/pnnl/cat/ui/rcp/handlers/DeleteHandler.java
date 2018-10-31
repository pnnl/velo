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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter;
import gov.pnnl.cat.ui.rcp.dialogs.FileDialogs;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 */
public class DeleteHandler extends AbstractHandler {
  
  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.customDeleteBehavior";
  protected static final String ATTRIBUTE = "class";
  protected static List<CustomDeleteBehavior> customDeleteBehaviors;
  static {
    loadCustomBehaviors();
  }

  private static void loadCustomBehaviors() {
    customDeleteBehaviors = new ArrayList<CustomDeleteBehavior>();
    
    try {
      // look up all the extensions for the LaunchSimulation extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof CustomDeleteBehavior) {
          CustomDeleteBehavior behavior = (CustomDeleteBehavior)obj;
          customDeleteBehaviors.add(behavior);
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom delete behavior extension points.", e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    Shell shell = HandlerUtil.getActivePart(event).getSite().getShell();
    IStructuredSelection selection = RCPUtil.getCurrentStructuredSelection(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event));

    final List<IResource> resourcesForDefaultDelete = new ArrayList<IResource>();
    
    // See if we have custom delete behavior for any of the resources
    for (Iterator<?> it = selection.iterator(); it.hasNext();) {
      
      IResource resource = RCPUtil.getResource(it.next());
      boolean deleted = false;
      
      for(CustomDeleteBehavior behavior : customDeleteBehaviors) {
        deleted = behavior.delete(resource);
        if(deleted) {
          break;
        } 
      }
      if(!deleted) {
        resourcesForDefaultDelete.add(resource);
      }
    }

    if(resourcesForDefaultDelete.size() > 0) {      
      // now delete any leftover resources with default delete operation
      ITransferObjectAdapter[] transferObjects = getTransferObjects(resourcesForDefaultDelete);
      
      if (FileDialogs.confirmDeleteDialog(shell, transferObjects)) {
        Job job = new Job("Deleting Files") {

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            IResourceManager mgr = ResourcesPlugin.getResourceManager();
            List<CmsPath> pathsToDelete = new ArrayList<CmsPath>();
            for (IResource resource : resourcesForDefaultDelete) {
              pathsToDelete.add(resource.getPath());              
            }
            try {
              mgr.deleteResources(pathsToDelete);
            } catch (Throwable e) {
              ToolErrorHandler.handleError("Error deleting files", e, true);
            }
            return Status.OK_STATUS;
          }
        };

        // Let's not use DeleteJob because it's really slow
        //DeleteJob job = new DeleteJob(sourcePaths, shell);
        job.setUser(true);
        job.schedule();
      }
    }
    return null;
  }
  
  /**
   * Method getTransferObjects.
   * @param selectedResources List<IResource>
   * @return ITransferObjectAdapter[]
   */
  private ITransferObjectAdapter[] getTransferObjects(List<IResource> selectedResources) {
    ITransferObjectAdapter[] transferObjects = new ITransferObjectAdapter[selectedResources.size()];
    for (int i = 0; i < selectedResources.size(); i++) {
      IResource resource = selectedResources.get(i);
      transferObjects[i] = (ITransferObjectAdapter) ((IAdaptable)resource).getAdapter(ITransferObjectAdapter.class);
    }
    return transferObjects;
  }

}
