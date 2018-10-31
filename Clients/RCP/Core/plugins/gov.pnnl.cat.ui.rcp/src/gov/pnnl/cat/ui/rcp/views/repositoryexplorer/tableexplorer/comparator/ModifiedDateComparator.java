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
import gov.pnnl.cat.core.util.DateFormatUtility;
import gov.pnnl.velo.util.VeloConstants;

import java.util.Date;

/**
 * Sort by the {@link IResource} last modified date.
 * @version $Revision: 1.0 $
 */
public class ModifiedDateComparator extends TableExplorerComparator {

  /**
   * {@inheritDoc}
   * 
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TableExplorerComparator#doCompare(gov.pnnl.cat.core.resources.IResource, gov.pnnl.cat.core.resources.IResource)
   */
  protected int doCompare(IResource one, IResource two) {
    int value = 0;

    IResource resource1 = one;
    IResource resource2 = two;

    if (resource1 instanceof ILinkedResource) {
      resource1 = ((ILinkedResource) resource1).getTarget();
    }
    if (resource2 instanceof ILinkedResource) {
      resource2 = ((ILinkedResource) resource2).getTarget();
    }

    String strDate1 = resource1.getPropertyAsString(VeloConstants.PROP_MODIFIED);
    Date date1 = DateFormatUtility.parseJcrDate(strDate1);

    String strDate2 = resource2.getPropertyAsString(VeloConstants.PROP_MODIFIED);
    Date date2 = DateFormatUtility.parseJcrDate(strDate2);

    if (date1 != null && date2 != null) {
      value = date1.compareTo(date2);
    } else if (date1 != null) {
      value = 1;
    } else if (date2 != null) {
      value = -1;
    } else {
      value = 0;
    }

    return value;
  }
}
