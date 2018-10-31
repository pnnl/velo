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

import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.TreeTableExplorerView;

/**
 * Includes both the tree and table view in one
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class MyWorkspaceViewFull extends TreeTableExplorerView {
  public static final String ID = MyWorkspaceViewFull.class.getName();
  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.TreeTableExplorerView#isRootIncluded()
   */
  @Override
  public boolean isRootIncluded() {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.AbstractExplorerView#getRoot()
   */
  @Override
  public Object getDefaultRoot() {
    return RCPUtil.getTreeRoot();
  }

  @Override
  public boolean showNewWizardInPopupMenu() {
    return false;
  }
}
