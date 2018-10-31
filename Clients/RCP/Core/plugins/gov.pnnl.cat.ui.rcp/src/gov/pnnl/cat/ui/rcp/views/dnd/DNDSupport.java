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
package gov.pnnl.cat.ui.rcp.views.dnd;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.net.VeloNetworkPlugin;
import gov.pnnl.cat.ui.rcp.views.dnd.extensions.ResourceViewerDropListenerDescriptor;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.CatViewerContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 */
public class DNDSupport {
  private static Logger logger = CatLogger.getLogger(DNDSupport.class);
  private static List<Transfer> dropTransfers;

  /**
   * Load transfers from extension point
   */
  private static void loadDropTransfers() {
    Collection<ResourceViewerDropListenerDescriptor> transferDescriptors = ResourceViewerDropListenerDescriptor.getTransfers();
    dropTransfers = new ArrayList<Transfer>(transferDescriptors.size());

    // default transfers
    dropTransfers.add(CatResourceTransfer.getInstance());  // transfer an IResource
    dropTransfers.add(LocalSelectionTransfer.getTransfer()); // transfer an ISelection from one view to another
    dropTransfers.add(FileTransfer.getInstance()); // transfer a file from local file system
    
    
    // now load extra ones registered from ext point
    for (ResourceViewerDropListenerDescriptor descriptor : transferDescriptors) {
      try {
        dropTransfers.add(descriptor.createTransfer());
      } catch (CoreException e) {
        logger.warn("Unable to create drop listener", e);
      }
    }
  }
  
  private static List<DropTargetListener> getListeners(CatViewerContainer viewer) {
    List<DropTargetListener> listeners = new ArrayList<DropTargetListener>();
    
    // add the default listener
    listeners.add(new ExplorerDropSupport(viewer));
    
    // add any extra listeners loaded from ext point
    Collection<ResourceViewerDropListenerDescriptor> listenerDescriptors = ResourceViewerDropListenerDescriptor.getListeners();
    for (ResourceViewerDropListenerDescriptor descriptor : listenerDescriptors) {
      try {
        listeners.add(descriptor.createListener());
      } catch (Throwable e) {
        logger.warn("Unable to create drop listener", e);
      }
    }
    return listeners;
  }

  /**
   * Method addDropSupport.
   * @param view ICatExplorerInput
   * @param control Control
   * @param dropListener DropTargetListener
   */
  public static void addDropSupport(CatViewerContainer explorer) {
    int operations = DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
    List<DropTargetListener> listeners = getListeners(explorer);

    if (dropTransfers == null) {
      loadDropTransfers();
    }
    
    Transfer[] transferTypes = dropTransfers.toArray(new Transfer[dropTransfers.size()]);

    // now add the drop listeners that have been contributed by other plugins
    for (DropTargetListener listener : listeners) {
      explorer.getViewer().addDropSupport(operations, transferTypes, listener);
    }
  }


  /**
   * Method addDragSupport.
   * @param viewer StructuredViewer
   */
  public static void addDragSupport(StructuredViewer viewer) {
    // Allow data to be copied or moved from this drag source
    int dragOperations = DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
    addDragSupport(viewer, dragOperations);
  }
  
  /**
   * Method addDragSupport.
   * @param viewer StructuredViewer
   * @param dragOperations int
   */
  public static void addDragSupport(StructuredViewer viewer, int dragOperations) {
    // Set our transfer source types.
    Transfer[] typesSource;
    
    if(VeloNetworkPlugin.getVeloFileSystemManager().isLocalDriveEnabled()) {
      typesSource = new Transfer[] { CatResourceTransfer.getInstance(), FileTransfer.getInstance()};
    } else {
      typesSource = new Transfer[] {CatResourceTransfer.getInstance()};
    }
    ExplorerDragSupport dragList = new ExplorerDragSupport(viewer);
    viewer.addDragSupport(dragOperations, typesSource, dragList);
  }
}
