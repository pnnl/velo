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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.util.StringComparator;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FileFolderSorter;

import java.util.Comparator;

/**
 * Sort {@link IResource} for the {@link TableExplorer}.
 * <p>
 * Add the ability to toggle ascending/descending order.
 * </p>
 * <p>
 * Subclasses need only implement {@link #doCompare(IResource, IResource)} and return values that are in ascending order, the implementation of {@link #compare(IResource, IResource)} will automatically reverse the order if {@link #isDescending()} is true.
 * </p>
 * @version $Revision: 1.0 $
 */
public abstract class TableExplorerComparator implements Comparator {
  protected final static FileFolderSorter FILE_FOLDER_SORTER = new FileFolderSorter();

  protected static final StringComparator STRING_COMPARATOR = new StringComparator();

  private boolean ascending = true;

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public final int compare(Object o1, Object o2) {
    IResource one = RCPUtil.getResource(o1);
    IResource two = RCPUtil.getResource(o2);

    int oneCategory = FILE_FOLDER_SORTER.category(one);
    int twoCategory = FILE_FOLDER_SORTER.category(two);

    int value = 0;

    if (oneCategory == twoCategory) {
      value = doCompare(one, two);
    } else {
      value = oneCategory - twoCategory;
    }

    if (isDescending()) {
      value *= -1;
    }

    return value;
  }

  /**
   * Perform the actual comparison, as if it were in ascending order.
   * <p>
   * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
   * </p>
   * 
   * @param one
   *          the first object to be compared.
   * @param two
   *          the second object to be compared.
  
   * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second. */
  protected abstract int doCompare(IResource one, IResource two);

  /**
  
   * @return boolean true if the in ascending sort order */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * The sort order is descending if it is not ascending.
   * 
  
   * @return boolean */
  public boolean isDescending() {
    return !isAscending();
  }

  /**
   * @param ascending
   *          the ascending to set
   */
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  /**
   * Reverse the current sort order.
   * 
  
  
   * @see #isAscending() * @see #setAscending(boolean) */
  public void toggleSortOrder() {
    setAscending(!isAscending());
  }
}
