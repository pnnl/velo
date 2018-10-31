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

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;

/**
 * If the {@link IResource} is a file, sort by its file size.
 * @version $Revision: 1.0 $
 */
public class SizeComparator extends TableExplorerComparator {

  /**
   * {@inheritDoc}
   * 
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TableExplorerComparator#doCompare(gov.pnnl.cat.core.resources.IResource, gov.pnnl.cat.core.resources.IResource)
   */
  protected int doCompare(IResource one, IResource two) {
    int value = 0;

    if (one instanceof IFile && two instanceof IFile) {
      IResource resource1 = one;
      IResource resource2 = two;

      if (resource1 instanceof ILinkedResource) {
        resource1 = ((ILinkedResource) resource1).getTarget();
      }

      if (resource2 instanceof ILinkedResource) {
        resource2 = ((ILinkedResource) resource2).getTarget();
      }

      IFile file1 = (IFile) resource1;
      IFile file2 = (IFile) resource2;

      if (file1.getSize() > file2.getSize()) {
        value = 1;
      } else if (file1.getSize() < file2.getSize()) {
        value = -1;
      } else {
        value = 0;
      }
    } else if (one instanceof IFile) {
      value = 1;
    } else if (two instanceof IFile) {
      value = -1;
    }

    return value;
  }
}
