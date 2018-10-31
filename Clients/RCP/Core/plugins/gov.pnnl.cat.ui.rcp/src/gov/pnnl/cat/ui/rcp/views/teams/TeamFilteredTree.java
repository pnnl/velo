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
package gov.pnnl.cat.ui.rcp.views.teams;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.model.TeamInput;
import gov.pnnl.cat.ui.rcp.views.profile.ProfilableFilteredTree;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PatternFilter;


/**
 */
public class TeamFilteredTree extends ProfilableFilteredTree {

  protected static Logger logger = CatLogger.getLogger(TeamFilteredTree.class);
  protected boolean showEveryone = false;
  
  /**
   * Constructor for TeamFilteredTree.
   * @param parent Composite
   */
  public TeamFilteredTree(Composite parent) {
    this(parent, false);
  }
  
  /**
   * Constructor for TeamFilteredTree.
   * @param parent Composite
   */
  public TeamFilteredTree(Composite parent, boolean showEveryone) {
    this(parent, DEFAULT_STYLE, new PatternFilter(), showEveryone);
  }

  /**
   * Constructor for TeamFilteredTree.
   * @param parent Composite
   * @param treeStyle int
   * @param filter PatternFilter
   */
  public TeamFilteredTree(Composite parent, int treeStyle, PatternFilter filter, boolean showEveryone) {
    super(parent, treeStyle, filter);
    this.showEveryone = showEveryone;
    TreeViewer treeViewer = getViewer();
    treeViewer.setSorter(new TeamSorter());

    TeamInput teamInput = new TeamInput(showEveryone);
    treeViewer.setInput(teamInput);

    updateFilterControlEnabledStatus();
  }


  /**
   * Method getInput.
   * @return TeamInput
   */
  public TeamInput getInput() {
    return (TeamInput) getViewer().getInput();
  }


  //
  /**
   * Method expandToPath.
   * @param team ITeam
   */
  public void expandToPath(ITeam team)
  {
    //EZLogger.logWarning("TeamFilteredTree::expandToPath", null);
    logger.warn("TeamFilteredTree::expandToPath");
    if (team == null) {
      return;
    }

    ISecurityManager mgr = ResourcesPlugin.getSecurityManager();
    Stack<CmsPath> parents = new Stack<CmsPath>();
    // make an vector of this resources parent(s):
    ITeam nodeparent = null;
    //CmsPath currentPath = team.getPath();
    ITeam currentNode = team;
    try {
      nodeparent = team;
      while (nodeparent != null) {
        parents.push(nodeparent.getPath());
        currentNode = nodeparent;
        nodeparent = mgr.getTeam(nodeparent.getParent());
      }
    } catch (Exception e) {
      //EZLogger.logError(e, "Failed to retrieve the resource at path: " + currentNode.toString());
      logger.error("Failed to retrieve the resource at path: " + currentNode.toString());
    }

    CmsPath[] foldersToExpand = (CmsPath[]) parents.toArray(new CmsPath[parents.size()]);
    //EZLogger.logWarning("TeamFilteredTree::expandToPath, size="+foldersToExpand.length, null);
    logger.warn("TeamFilteredTree::expandToPath, size="+foldersToExpand.length);
    // reverse the array
    for (int i = 0; i < foldersToExpand.length / 2; i++) {
      int index1 = i;
      int index2 = foldersToExpand.length - i - 1;

      CmsPath temp = foldersToExpand[index1];
      foldersToExpand[index1] = foldersToExpand[index2];
      foldersToExpand[index2] = temp;
    }
 
  }
}
