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
package gov.pnnl.cat.ui.rcp.views.profile;


import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.adapters.CatBaseWorkbenchContentProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.velo.util.VeloConstants;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 */
public abstract class ProfilableFilteredTree extends FilteredTree {

  public final static int DEFAULT_STYLE = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;

  private static Logger logger = CatLogger.getLogger(ProfilableFilteredTree.class);

  /**
   * Constructor for ProfilableFilteredTree.
   * @param parent Composite
   */
  public ProfilableFilteredTree(Composite parent) {
    this(parent, DEFAULT_STYLE, new PatternFilter());
  }

  /**
   * Constructor for ProfilableFilteredTree.
   * @param parent Composite
   * @param treeStyle int
   * @param filter PatternFilter
   */
  @SuppressWarnings("deprecation")
  public ProfilableFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
    super(parent, treeStyle, filter);

    TreeViewer treeViewer = getViewer();
    treeViewer.setLabelProvider(new CatWorkbenchLabelProvider(treeViewer));
    treeViewer.setContentProvider(new CatBaseWorkbenchContentProvider(false));
  }


  /**
   * Enables or disables the filter control based on whether it is needed.
   * The filter control is considered unnecessary if there are 0 or 1 items
   * available.
   */
  protected void updateFilterControlEnabledStatus() {
    // if the tree has only one or zero views, disable the filter text control
    if (hasAtMostOneElement(getViewer())) {
      Text filterText = getFilterControl();
      if (filterText != null) {
        filterText.setEnabled(false);
      }
    }
  }


  /**
   * Refreshes the tree and makes an attempt to maintain the current selection.
   */
  public void refreshWithSelection() {
    try {
      ISelection previousSelection = getViewer().getSelection();

      // refresh everything
      refresh();

      // update the selection to the most current data objects
      previousSelection = updateSelection(previousSelection, getViewer());

      // reset the selection to what they had selected previously.
      // this will force the user details view to refresh as well
      getViewer().setSelection(previousSelection);

    } catch (Exception e) {
      logger.error("Failed to reload users.", e);
    }
  }


  /**
   * Refreshes the tree.
   */
  public void refresh() {
    getViewer().refresh();

    // call text changed to cause the filtering code to execute.
    // this will make it look like something was typed into the filter.
    textChanged();
  }


  /**
   * Updates the selection given with the most-recent version of the elements
   * in the selection.
   * The selection must contain users or teams.
   * @param selection
   * @param viewer
  
   * @return approximately the same selection that was passed in, but with internal elements that are up to date. */
  private ISelection updateSelection(ISelection selection, TreeViewer viewer) {
    logger.debug("Updating selection of " + selection);
    ISecurityManager securityMgr = ResourcesPlugin.getSecurityManager();

    if (selection instanceof TreeSelection && !selection.isEmpty()) {
      TreeSelection originalSelection = (TreeSelection) selection;
      TreePath[] paths = originalSelection.getPaths();
      List<TreePath> newPaths = new ArrayList<TreePath>(paths.length);
      List<IProfilable> newProfilables;

      for (TreePath path : paths) {
        newProfilables = new ArrayList<IProfilable>(path.getSegmentCount());
        for (int i = 0; i < path.getSegmentCount(); i++) {
          IProfilable profilable = (IProfilable) path.getSegment(i);
          IProfilable updatedProfilable = null;
          logger.debug("Current profilable last modified: " + profilable.getProperty(VeloConstants.PROP_MODIFIED));

          try {

            if (profilable instanceof IUser) {
              updatedProfilable = securityMgr.getUser(((IUser) profilable).getUsername());
            } else if (profilable instanceof ITeam) {
              updatedProfilable = securityMgr.getTeam(((ITeam) profilable).getPath());
            }


            if (updatedProfilable != null) {
              logger.debug("Updated profilable last modified: " + updatedProfilable.getProperty(VeloConstants.PROP_MODIFIED));
              newProfilables.add(updatedProfilable);
            }
          } catch (Exception e) {
            // don't worry about this too much.
            // it's possible the selection was just deleted
            logger.debug("Unable to process selection.", e);
          }
        }

        if (!newProfilables.isEmpty()) {
          TreePath newPath = new TreePath(newProfilables.toArray());
          newPaths.add(newPath);
        }
      }

      return new TreeSelection(newPaths.toArray(new TreePath[newPaths.size()]));
    }

    return selection;
  }


  /**
   * Method hasAtMostOneElement.
   * @param tree TreeViewer
   * @return boolean
   */
  private boolean hasAtMostOneElement(TreeViewer tree) {
    ITreeContentProvider contentProvider = (ITreeContentProvider) tree.getContentProvider();
    Object[] children = contentProvider.getElements(tree.getInput());

    if (children.length <= 1) {
      if (children.length == 0) {
        return true;
      }
      return !contentProvider.hasChildren(children[0]);
    }
    return false;
  }
}
