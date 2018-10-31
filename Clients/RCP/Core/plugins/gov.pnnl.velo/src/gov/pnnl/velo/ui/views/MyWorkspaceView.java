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

import org.apache.log4j.Logger;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.TreeExplorerView;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer;

/**
 * CAT tree view that starts at the current user's
 * home folder.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class MyWorkspaceView extends TreeExplorerView {
  public static final String ID = MyWorkspaceView.class.getName();
  private static final Logger logger = CatLogger.getLogger(MyWorkspaceView.class);
  private GenericContainer root;
 

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.TreeExplorerView#showFiles()
   */
  @Override
  public boolean showFiles() {
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.TreeExplorerView#getRootResource()
   */
  /**
   * Method getRoot.
   * @return GenericContainer
   */
  @Override
  public GenericContainer getDefaultRoot() {
    if(root == null) {
      root = RCPUtil.getTreeRoot();
    }
    return root;
  }
    
  /**
   * Method isRootIncluded.
   * @return boolean
   */
  @Override
  public boolean isRootIncluded() {
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.TreeExplorerView#showNewWizardInPopupMenu()
   */
  @Override
  public boolean showNewWizardInPopupMenu() {
    return false;
  }

}
