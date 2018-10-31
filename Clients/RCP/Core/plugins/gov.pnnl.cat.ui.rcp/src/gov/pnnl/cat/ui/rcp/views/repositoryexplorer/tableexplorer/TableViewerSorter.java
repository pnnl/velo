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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer;

import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TableExplorerComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FileFolderSorter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Delegate sorting to {@link TableExplorerComparator} implementation for a {@link TableExplorer} column.
 * @version $Revision: 1.0 $
 */
public class TableViewerSorter extends ViewerSorter {
  private final static FileFolderSorter FILE_FOLDER_SORTER = new FileFolderSorter();

  private TableExplorerComparator comparator;

  /**
   * Constructor for TableViewerSorter.
   * @param comparator TableExplorerComparator
   */
  public TableViewerSorter(TableExplorerComparator comparator) {
    this.comparator = comparator;
  }

  /**
   * Method compare.
   * @param viewer Viewer
   * @param e1 Object
   * @param e2 Object
   * @return int
   */
  public int compare(Viewer viewer, Object e1, Object e2) {
    return comparator.compare(e1, e2);
  }

  /**
   * Method category.
   * @param element Object
   * @return int
   */
  public int category(Object element) {
    int compareResults = FILE_FOLDER_SORTER.category(element);

    if (isAscending()) {
      return compareResults;
    } else {
      return compareResults * -1;
    }
  }

  public void toggleSortOrder() {
    comparator.toggleSortOrder();
  }

  /**
   * Method isAscending.
   * @return boolean
   */
  public boolean isAscending() {
    return comparator.isAscending();
  }
}
