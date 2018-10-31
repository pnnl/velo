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

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

/**
 */
public class CommentsDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
  
  private TableViewer viewer;
  
  /**
   * Constructor for CommentsDragAdapter.
   * @param viewer TableViewer
   */
  public CommentsDragAdapter(TableViewer viewer) {
    super();
    this.viewer = viewer;
  }

  /**
   * Method dragStart.
   * @param event DragSourceEvent
   * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(DragSourceEvent)
   */
  @Override
  public void dragStart(DragSourceEvent event) {
    ISelection selection = viewer.getSelection();
    LocalSelectionTransfer.getTransfer().setSelection(selection);
    LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFFFFFFL);
    event.doit = !selection.isEmpty();
    System.out.println(event.toString());
  }

  /**
   * Method dragSetData.
   * @param event DragSourceEvent
   * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(DragSourceEvent)
   */
  public void dragSetData(DragSourceEvent event) {
    event.data = LocalSelectionTransfer.getTransfer().getSelection();
  }

  /**
   * Method dragFinished.
   * @param event DragSourceEvent
   * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(DragSourceEvent)
   */
  public void dragFinished(DragSourceEvent event) {
    LocalSelectionTransfer.getTransfer().setSelection(null);
    LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);        
  }

  /**
   * Method getTransfer.
   * @return Transfer
   * @see org.eclipse.jface.util.TransferDragSourceListener#getTransfer()
   */
  @Override
  public Transfer getTransfer() {
    return LocalSelectionTransfer.getTransfer();
  }
}
