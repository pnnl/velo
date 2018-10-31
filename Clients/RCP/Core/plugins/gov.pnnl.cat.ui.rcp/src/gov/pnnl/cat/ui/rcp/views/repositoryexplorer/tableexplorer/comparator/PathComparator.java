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

import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;

/**
 * Sort by the {@link IResource#getPath()}
 * @version $Revision: 1.0 $
 */
public class PathComparator extends TableExplorerComparator {

  /**
   * {@inheritDoc}
   * 
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TableExplorerComparator#doCompare(gov.pnnl.cat.core.resources.IResource, gov.pnnl.cat.core.resources.IResource)
   */
  protected int doCompare(IResource one, IResource two) {
    int value = 0;
    
    IResource resource1 = one;
    IResource resource2 = two;

    if (one instanceof ILinkedResource) {
      resource1 = ((ILinkedResource) one).getTarget();
    }

    if (two instanceof ILinkedResource) {
      resource2 = ((ILinkedResource) two).getTarget();
    }

    value = STRING_COMPARATOR.compare(resource1.getPath().toString(), resource2.getPath().toString());

    return value;
  }
}
