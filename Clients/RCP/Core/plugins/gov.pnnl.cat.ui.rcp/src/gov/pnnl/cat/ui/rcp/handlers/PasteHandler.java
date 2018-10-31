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

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatClipboard;
import gov.pnnl.cat.ui.rcp.actions.ActionUtil;
import gov.pnnl.cat.ui.rcp.actions.jobs.AbstractRepositoryJob;
import gov.pnnl.cat.ui.rcp.actions.jobs.CopyJob;
import gov.pnnl.cat.ui.rcp.actions.jobs.MoveJob;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.dnd.ResourceList;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.TreeTableExplorerView;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author D3K388
 *
 * @version $Revision: 1.0 $
 */
public class PasteHandler  extends AbstractHandler {
  protected static Logger logger = CatLogger.getLogger(PasteHandler.class);
  
  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.customPasteBehavior";
  protected static final String ATTRIBUTE = "class";
  protected static List<CustomPasteBehavior> customPasteBehaviors;
  static {
    loadCustomBehaviors();
  }
  
  public static boolean isEnabledPasteShortcut = true; // Determines if the pasteShortcut operation should be allowed?
  public static boolean isPasteWithDelete = false; // Cut and Paste operation
  public static ResourceList clipboardResourceList;
  
  private static void loadCustomBehaviors() {
    customPasteBehaviors = new ArrayList<CustomPasteBehavior>();
    
    try {
      // look up all the extensions for the LaunchSimulation extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof CustomPasteBehavior) {
          CustomPasteBehavior behavior = (CustomPasteBehavior)obj;
          customPasteBehaviors.add(behavior);
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom paste behavior extension points.", e);
    }
  }
  
  public PasteHandler() {
    super();
    // TODO Auto-generated constructor stub
  }



  /**
   * Method execute.
   * @param event ExecutionEvent
   * @return Object
   * @throws ExecutionException
   * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    doPasteAction(event);// COPYOPERATION

    // Remove the orginal?
    // -------------------
    if (isPasteWithDelete) {

      // Retrieve the data from the clipboard.
      CatClipboard catClipboard = CatClipboard.getInstance();
      PasteHandler.isPasteWithDelete = false;
      catClipboard.clearContents();
    }

    return null;
  }

  /**
   * This method only deals with the paste and paste shortcut methods.
   * 
   * @param event ExecutionEvent
   */
  protected void doPasteAction(ExecutionEvent event) {
    IFolder destinationFolder = getDestinationFolder(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event));

    // Retrieve the data from the clipboard.
    CatClipboard catClipboard = CatClipboard.getInstance();
    List<IResource> resourcesOnClipboard = catClipboard.getContents();
    Shell shell = HandlerUtil.getActiveShell(event);
    List<IResource> resourcesForDefaultCopy = new ArrayList<IResource>();
    
    // Paste custom resources
    for(IResource resource : resourcesOnClipboard) {
      boolean pasted = false;
      
      for(CustomPasteBehavior behavior : customPasteBehaviors) {       
        pasted = behavior.paste(resource, destinationFolder, isPasteWithDelete);
        if(pasted) {
          break;
        } 
      }
      if(!pasted) {
        resourcesForDefaultCopy.add(resource);
      }
    }

    // now paste any leftover resources with default paste operation
    String[] sourcePaths = new String[resourcesForDefaultCopy.size()];
    for(int i = 0; i < sourcePaths.length; i++) {
      sourcePaths[i] = resourcesForDefaultCopy.get(i).getPath().toFullyQualifiedString();
    }
    AbstractRepositoryJob job = createJob(sourcePaths, destinationFolder, shell);
    ActionUtil.startJob(job);
  }

  /**
   * Method createJob.
   * @param sourcePaths String[]
   * @param destinationFolder IFolder
   * @param shell Shell
   * @return AbstractRepositoryJob
   */
  protected AbstractRepositoryJob createJob(String[] sourcePaths, IFolder destinationFolder, Shell shell) {
    AbstractRepositoryJob job;

    if (isPasteWithDelete) {
      job = new MoveJob(sourcePaths, destinationFolder.getPath(), shell);
    } else {
      job = new CopyJob(sourcePaths, destinationFolder.getPath(), shell);
    }

    return job;
  }

  /**
   * Method getDestinationFolder.
   * @param selection ISelection
   * @param part IWorkbenchPart
   * @return IFolder
   */
  public static IFolder getDestinationFolder(ISelection selection, IWorkbenchPart part) {
    IFolder destinationFolder = null;
    IStructuredSelection selected = RCPUtil.getCurrentStructuredSelection(selection, part);
    // if something is selected, it is a CatItemNode and we can either
    // paste into it if it is a
    // folder, otherwise its a file and we must paste into its parent
    if (!selected.isEmpty()) {
      IResource selectedResource = RCPUtil.getResource(selected.getFirstElement());
      if (selectedResource instanceof IFolder) {
        destinationFolder = (IFolder) selectedResource;
      } else if (selectedResource instanceof IFile) {
        try {
          IResource parent = ((IFile) selectedResource).getParent();
          // parent might not be a folder (could be a file as with renditions)
          if(parent instanceof IFolder) {
            destinationFolder = (IFolder) parent;
          }
        } catch (ResourceException e) {
          // TODO Auto-generated catch block
          logger.error(e);
        }
      }
    } else if (selected.isEmpty()) {
      // case for when there is an empty selection, only do anything if
      // the viewer is the tableviewer
      // (don't know where to put it if its the treeViewer and nothing is
      // selected

      //      IWorkbenchPart part = HandlerUtil.getActivePart(event);
      if (part instanceof TreeTableExplorerView) {
        ISelectionProvider delegate = ((TreeTableExplorerView)part).getCurrentDelegate();
        if (delegate instanceof TableViewer) {
          destinationFolder = (IFolder) RCPUtil.getResource(((TableViewer)delegate).getInput());
        }
      }

    }

    try {
      // Check to see if the destinationFolder is actually a link and therefore
      // we have to update the final destination location.
      if (destinationFolder instanceof ILinkedResource) {
        destinationFolder = (IFolder) ((ILinkedResource) destinationFolder).getTarget();
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }

    return destinationFolder;
  }
  
}
