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
package gov.pnnl.cat.ui.rcp.views.users;

import gov.pnnl.cat.ui.rcp.model.UserInput;
import gov.pnnl.cat.ui.rcp.views.profile.ProfilableFilteredTree;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 */
public class UserFilteredTree extends ProfilableFilteredTree {

  /**
   * Constructor for UserFilteredTree.
   * @param parent Composite
   */
  public UserFilteredTree(Composite parent) {
    this(parent, DEFAULT_STYLE, new PatternFilter());
  }

  /**
   * Constructor for UserFilteredTree.
   * @param parent Composite
   * @param treeStyle int
   * @param filter PatternFilter
   */
  public UserFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
    super(parent, treeStyle, filter);

    TreeViewer treeViewer = getViewer();
    treeViewer.setSorter(new UserSorter());

    UserInput userInput = new UserInput();
    userInput.setFilterSpecialUsers(true);
    treeViewer.setInput(userInput);

    updateFilterControlEnabledStatus();
  }


  /**
   * Method getInput.
   * @return UserInput
   */
  public UserInput getInput() {
    return (UserInput) getViewer().getInput();
  }
}
