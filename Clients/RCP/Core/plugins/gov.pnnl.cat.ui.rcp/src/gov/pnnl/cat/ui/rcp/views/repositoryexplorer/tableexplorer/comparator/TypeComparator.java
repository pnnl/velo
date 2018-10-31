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
 * Sort by the {@link IResource} type
 * @version $Revision: 1.0 $
 */
public class TypeComparator extends TableExplorerComparator {

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

    String type1 = resource1.getMimetype();
    String type2 = resource2.getMimetype();

    if (type1 != null && type2 != null) {
      value = STRING_COMPARATOR.compare(type1, type2);
    } else if (type1 != null) {
      value = 1;
    } else if (type2 != null) {
      value = -1;
    }

    return value;
  }
}
