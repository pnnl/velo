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
package gov.pnnl.velo.ui.views;

import gov.pnnl.cat.core.resources.IResource;


/**
 * Deployments extending the summaryViewSectionFactory extension point should
 * implement this interface to be able to contribute new sections to the Summary View.
 * 
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public interface SummaryViewSectionProvider extends Comparable<SummaryViewSectionProvider> {
  
  /**
   * @param view - view to add the summary to
   * @param selectedResource
   * @return - false if a summary section wasn't created because it didn't apply to this resource
   */
  public boolean createSummarySection(SummaryView view, IResource selectedResource);

}
