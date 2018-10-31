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
package gov.pnnl.velo.ui.adapters;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.dnd.CatResourceTransfer;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

/**
 */
public class ScratchPadDropAdapter extends ViewerDropAdapter {
  private static Logger logger = CatLogger.getLogger(ScratchPadDropAdapter.class);
  
  private final CatResourceTransfer resourceTransfer = CatResourceTransfer.getInstance();
  private Transfer currentTransfer;
  
  private TransferData fCurrentTransferType = null;
  private boolean fInsertBefore;
  private int fDropType;

  private static final int DROP_TYPE_DEFAULT = 0;
  private static final int DROP_TYPE_LINKED_DATA = 1;

  /**
   * Constructor takes the viewer this drop adapter applies to.
   * @param viewer the viewer to add drop to
   */
  public ScratchPadDropAdapter(TableViewer viewer) {
    super(viewer);
    setFeedbackEnabled(true);
    setSelectionFeedbackEnabled(false);
    setScrollExpandEnabled(false);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragEnter(DropTargetEvent event) {
    fDropType = DROP_TYPE_DEFAULT;
    logger.warn("event.detail = DND.DROP_NONE");
    event.detail = DND.DROP_NONE;

    for (int i = 0; i < event.dataTypes.length; i++) {
//      if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataTypes[i])) {
//        if (isLinkedDataDrop()){
//          event.currentDataType = event.dataTypes[i];
//          logger.warn("event.detail = DND.DROP_COPY");
//          event.detail = DND.DROP_COPY;
//          fDropType = DROP_TYPE_LINKED_DATA;
//          break;
//        }
//      }else 
        if (resourceTransfer.isSupportedType(event.dataTypes[i])) {
        event.currentDataType = event.dataTypes[i];
        logger.warn("event.detail = DND.DROP_COPY");
        event.detail = DND.DROP_COPY;
        fDropType = DROP_TYPE_LINKED_DATA;
        break;
      }
        
    }

    super.dragEnter(event);
  }

//  /**
//   * @return whether the selection transfer contains only LinkedDataResource
//   */
//  private boolean isLinkedDataDrop() {
//    IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
//    Iterator iterator = selection.iterator();
//    while (iterator.hasNext()) {
//      Object element = iterator.next();
//      //the element should be LinkedDataResource
//      if (!(element instanceof LinkedDataResource)){
//        return false;
//      } 
////      else if(((RDFObject)element).isStringLiteral()){
////        return false;
////      }
//    }
//    return true;
//  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragOver(DropTargetEvent event) {
    super.dragOver(event);
    // Allow scrolling (but not expansion)
    event.feedback |= DND.FEEDBACK_SCROLL;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
   */
  public boolean validateDrop(Object target, int operation, TransferData transferType) {
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#drop(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void drop(DropTargetEvent event) {
    fCurrentTransferType = event.currentDataType;
    // Unless insert after is explicitly set, insert before
    fInsertBefore = getCurrentLocation() != LOCATION_AFTER;
    super.drop(event);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
   */
  public boolean performDrop(Object data) {
//    if (LocalSelectionTransfer.getTransfer().isSupportedType(fCurrentTransferType)) {
//      IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
//      if (fDropType == DROP_TYPE_LINKED_DATA){
//        return performLinkedDataDrop(selection);
//      } 
//    }  else 
      if (resourceTransfer.isSupportedType(fCurrentTransferType)) {
      String[] paths = (String[])data;
      for (String path : paths) {
        IResource resource = ResourcesPlugin.getResourceManager().getResource(
            new CmsPath(path));
        performResourceDrop(resource);
      }
      return true;
    }
    return false;
  }

  /**
   * Method performResourceDrop.
   * @param selection IResource
   * @return boolean
   */
  private boolean performResourceDrop(IResource selection) {
    TableViewer tableViewer = (TableViewer)getViewer();
    boolean success = false;
    //dont add the same resource over again
    
//    tableViewer.add(selection);
    ((WritableSet)tableViewer.getInput()).add(selection);
       success = true;
     return success;

  }
  
//  protected boolean performLinkedDataDrop(IStructuredSelection selection) {
//   Iterator i = selection.iterator();
//   TableViewer tableViewer = (TableViewer)getViewer();
//   
//   boolean success = false;
//    while ( i.hasNext() ) {
//      LinkedDataResource resource = (LinkedDataResource)i.next();
//      tableViewer.add(resource);
//      success = true;
//    }
//    return success;
//
//  }

}


