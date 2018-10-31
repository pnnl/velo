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

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.AbstractExplorerView;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 */
public class SelectAllHandler extends AbstractHandler {

  private Logger logger = CatLogger.getLogger(this.getClass());
  
  /* (non-Javadoc)
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    logger.debug("in SelectAll's run");
    TableViewer tableviewer = getTableViewer(HandlerUtil.getActivePart(event));
    if (tableviewer != null) {
      TableItem[] items = tableviewer.getTable().getItems();
      
      ArrayList<Object> list = new ArrayList<Object> (items.length);
      for (int i = 0; i < items.length; i++) {
        Widget item = items[i];
        Object e = item.getData();
        if (e != null)
          list.add(e);
      }
      tableviewer.setSelection(new StructuredSelection(list), true);
      // Fire the selection change listener. This listener will update the
      // right-click popup menu. (i.e. activating the paste menu item)
      tableviewer.setSelection(tableviewer.getSelection());
    }
    return null;
  }
  
  /**
   * Method getTableViewer.
   * @param part IWorkbenchPart
   * @return TableViewer
   */
  private TableViewer getTableViewer(IWorkbenchPart part) {
    if (part instanceof AbstractExplorerView) {
      AbstractExplorerView tableView = (AbstractExplorerView)part;
      return tableView.getTableViewer();
    }
    return null;
  }

}
