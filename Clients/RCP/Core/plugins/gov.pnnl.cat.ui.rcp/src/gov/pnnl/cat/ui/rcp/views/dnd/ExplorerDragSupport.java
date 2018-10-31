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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.net.VeloNetworkPlugin;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;

/**
 */
public class ExplorerDragSupport implements DragSourceListener {

  private final CatResourceTransfer catTransfer = CatResourceTransfer.getInstance();
  private ContentViewer view;
  private Logger logger = CatLogger.getLogger(this.getClass());

  /**
   * Constructor for ExplorerDragSupport.
   * @param view ContentViewer
   */
  public ExplorerDragSupport(ContentViewer view) {
    this.view = view;
  }

  //called first
  /**
   * Method dragStart.
   * @param event DragSourceEvent
   * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(DragSourceEvent)
   */
  public void dragStart(DragSourceEvent event) {
    
    logger.debug("dragStart");
    List<IResource> selectedResources = RCPUtil.getResources((IStructuredSelection) this.view.getSelection());
    boolean hasCategories = false;
    
    // Only start the drag if the source is an IResource
    if(selectedResources.size() == 0) {
      event.doit = false;

    } else {
      // Do not allow categories to be dragged
      for(IResource resource : selectedResources) {
        if(resource.getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
          hasCategories = true;
          break;
        }
      }
      
      if (hasCategories) {
        event.doit = false;
      }
    }

    // For now do not allow categories to be dragged
  }

  //called when they let go
  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
   */
  @Override
  public void dragSetData(DragSourceEvent event) {
    logger.debug("dragSetData");
    try {
      final StructuredSelection selectedResources = (StructuredSelection) this.view.getSelection();
      List<IResource>resources = RCPUtil.getResources(selectedResources);

      logger.debug("dragSetData - CatResourceTransfer dataType");
      String[] filePaths = new String[resources.size()];
      int i = 0;
      boolean catTransferType = catTransfer.isSupportedType(event.dataType) || !VeloNetworkPlugin.getVeloFileSystemManager().isLocalDriveEnabled();
      for (IResource selectedResource : resources) {
        IResource resource = RCPUtil.getResource(selectedResource);
        if(catTransferType) {
          filePaths[i] = resource.getPath().toFullyQualifiedString();
        } else {
          //filePaths[i] =resource.getCifsPath().toOSString();
        }
        logger.debug("Setting File: "+filePaths[i]);
        i++;
      }
      event.data = filePaths;
      if(FileTransfer.getInstance().isSupportedType(event.dataType) && !VeloNetworkPlugin.getVeloFileSystemManager().isLocalDriveEnabled()) {
        event.detail = DND.DROP_NONE;
        event.doit = false;
      }
      
    } catch (Exception e) {
      String errMsg = "Error dragging repository resources.";
      ToolErrorHandler.handleError(errMsg, e, true);
    } 
  }

  //called at the very end
  /**
   * Method dragFinished.
   * @param event DragSourceEvent
   * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(DragSourceEvent)
   */
  public void dragFinished(DragSourceEvent event) {
    logger.debug("dragFinished");
  }
}
