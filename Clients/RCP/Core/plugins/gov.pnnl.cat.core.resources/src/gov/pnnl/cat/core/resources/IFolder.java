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
package gov.pnnl.cat.core.resources;


import java.util.List;

/**
 *
 * @version $Revision: 1.0 $
 */
public interface IFolder extends IResource {

  /**
   * Method getChildren.
   * @return List<IResource>
   * 
   */
  public List<IResource> getChildren();

  /**
   * Returns the total size of all files and folders under the current folder.
   * <br/>This is an expensive operation, as it requires that every child of this folder to be visited.
   * 
   * @return long
   *  * @see getContents(IFolderSizeProgressMonitor) */
  public long getSize();

  /**
   * Returns the contents of the folder as an array where the first item in the array is the total size, the second item is the total number of files within the current folder, and the third item is the total number of folders within the current folder.
   * <br/>This is an expensive operation, as it requires that every child of this folder to be visited.
   * The subtotals for the eventual array returned can be accessed by calling methods on the monitor.
   * 
   * @return a long[] of {size, totalFiles, totalFolders} *  * @see getSize(IFolderSizeProgressMonitor) */

  /**
   * A virtual folder means that this is folder is used for alternate views of 
   * already existing data.  Therefore, it can NOT contain real files, only 
   * links and other virtual folders.
   * @return true if this is a virtual folder, false if not
   */
//  public boolean isVirtual();
}
