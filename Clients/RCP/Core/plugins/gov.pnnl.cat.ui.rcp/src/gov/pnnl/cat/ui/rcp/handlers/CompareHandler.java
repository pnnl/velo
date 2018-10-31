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

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.perspectives.EditorPerspective;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Compares 2 files with each other.  For now we compare any
 * 2 files.  Later we need to add to the property tester for
 * this command to only compare text files - but we need to 
 * add an alfresco property to quickly say if file is text or not.
 * 
 * Also for now opens in an unsaveable compare editor - later we will have
 * to implement save method in our compare editor input and change compare config
 * if we want to make it saveable.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CompareHandler extends AbstractHandler {

  public static final String ID = "gov.pnnl.cat.commands.Compare";
  private Logger logger = CatLogger.getLogger(CompareHandler.class);
  
  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.customCompareBehavior";
  protected static final String ATTRIBUTE = "class";
  protected static List<CustomCompareBehavior> customCompareBehaviors;
  static {
    loadCustomBehaviors();
  }

  private static void loadCustomBehaviors() {
    customCompareBehaviors = new ArrayList<CustomCompareBehavior>();
    
    try {
      // look up all the extensions for the LaunchSimulation extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof CustomCompareBehavior) {
          CustomCompareBehavior behavior = (CustomCompareBehavior)obj;
          customCompareBehaviors.add(behavior);
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom compare behavior extension points.", e);
    }
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
    IStructuredSelection selection = RCPUtil.getCurrentStructuredSelection(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event));

    List<IFile> resourcesToCompare = new ArrayList<IFile>();

    for (Iterator it = selection.iterator(); it.hasNext();) {
      IResource currentResource = RCPUtil.getResource(it.next());
      if(currentResource instanceof IFile) {
        resourcesToCompare.add((IFile)currentResource);
      }
    }

    try {
      if(resourcesToCompare.size() == 2) {
        // First open editor perspective in new window
        IWorkbenchWindow newWindow = PlatformUI.getWorkbench().openWorkbenchWindow(EditorPerspective.ID, null);

        // now download both files
        IResourceManager mgr = ResourcesPlugin.getResourceManager();
        IFile resource1 = resourcesToCompare.get(0);
        IFile resource2 = resourcesToCompare.get(1);

        File leftFile = mgr.getContentPropertyAsFile(resource1.getPath(), VeloConstants.PROP_CONTENT);
        File rightFile = mgr.getContentPropertyAsFile(resource2.getPath(), VeloConstants.PROP_CONTENT);
        
        // Now see if we have custom preprocessors before the compare
        for(CustomCompareBehavior behavior : customCompareBehaviors) {
          behavior.preprocessFiles(leftFile, rightFile);
        }

        // create compare editor input
        CompareConfiguration cc = new CompareConfiguration();
        cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
        CompareEditorInput input = new FileCompareInput(cc, leftFile, rightFile);

        // open in compare editor
        CompareUI.openCompareEditorOnPage(input, newWindow.getActivePage());

      } else {
        throw new RuntimeException("Did not select two resources to compare.");
      }

    } catch (Throwable e) {
      logger.error(e);
      StatusUtil.handleStatus("Failed to open compare editor in new window.", e, StatusManager.SHOW);
    }
    return null;
  }

}
