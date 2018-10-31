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
package gov.pnnl.cat.ui.rcp.views.adapters;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.model.CmsPath;

import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * TODO use DeferredContentProvider and SWT.VIRTUAL table?
 * @version $Revision: 1.0 $
 */
public class TableCatWorkbenchProvider extends CatBaseWorkbenchContentProvider {

  //private IStatusLineManager statusLineMgr;
  private HashSet<CmsPath> map = new HashSet<CmsPath>();
  private static Logger logger = CatLogger.getLogger(TableCatWorkbenchProvider.class);

  public TableCatWorkbenchProvider() {
    this(true, null);
  }

  /**
   * Constructor for TableCatWorkbenchProvider.
   * @param statusLineMgr IStatusLineManager
   */
  public TableCatWorkbenchProvider(IStatusLineManager statusLineMgr) {
    this(true, statusLineMgr);
  }

  /**
   * Constructor for TableCatWorkbenchProvider.
   * @param showFiles boolean
   * @param statusLineMgr IStatusLineManager
   */
  public TableCatWorkbenchProvider(boolean showFiles, IStatusLineManager statusLineMgr) {
    super(showFiles);
//    this.statusLineMgr = statusLineMgr;
//    if (this.statusLineMgr != null) {
//      this.statusLineMgr.setMessage("");
//    }
  }

  /**
   * Method inputChanged.
   * @param v Viewer
   * @param oldInput Object
   * @param newInput Object
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
   */
  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    logger.debug("input changed to " + newInput + ". clearing map");
    super.inputChanged(v, oldInput, newInput);
    map.clear();
    updateStatusLine(0);
  }

  /**
   * Method updateDisplay.
   * @param parentsToChildren Map<IResource,Vector<IResource>>
   * @param clearPrevious boolean
   */
  public void updateDisplay(Map<IResource, Vector<IResource>> parentsToChildren, boolean clearPrevious) {
    TableViewer tableViewer = (TableViewer) viewer;

    Object tableInput = tableViewer.getInput();

    logger.debug("clearPrevious: " + clearPrevious);
    logger.debug("total items in table: " + tableViewer.getTable().getItems().length);

    if (clearPrevious && tableViewer.getTable().getItems().length != 0) {
      tableViewer.getTable().removeAll();
      logger.debug("Removing all!");
    }

    for (IResource parentNode : parentsToChildren.keySet()) {
      // Check that the update is actually for this table's input.
      if (!parentNode.equals(tableInput)) {
        continue;
      }

      Vector<IResource> children = parentsToChildren.get(parentNode);
//      logger.debug("ADDING CHILDREN TO TABLE: " + children);

      Vector<IResource> childrenToAdd = new Vector<IResource>();

      if (tableViewer.getTable().getItems().length == 0) {
        logger.debug("table is empty. clearing map");
        map.clear();
      }

      for (IResource child : children) {
        if (!map.contains(child.getPath()) && !RCPUtil.isHiddenFile(child)) {
          map.add(child.getPath());
          childrenToAdd.add(child);
          logger.debug("keeping " + child.getPath());
        } else {
          logger.debug("discarding " + child.getPath());
        }
      }

      // children will be null if the batch notification contains a work complete
      // but not any of the actual values
      if (children != null) {
        tableViewer.add(childrenToAdd.toArray());
      }
    }

    updateStatusLine(tableViewer.getTable().getItemCount());

}
  

  /**
   * Method updateStatusLine.
   * @param totalElements int
   */
  private void updateStatusLine(int totalElements) {
//    if (statusLineMgr != null) {
//      String resources = "resources";
//      if (totalElements == 1) {
//        resources = "resource";
//      }
//      this.statusLineMgr.setMessage(totalElements + " " + resources);
//    }
  }

}
