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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.model.CmsPath;

/**
 * Interface to a CAT tabbed IViewPart
 * @version $Revision: 1.0 $
 */
public interface ICatExplorerView extends IViewPart {
  
  /**
   * The next unique id for viewparts of this type.  Used for the viewpart's secondary identification.
  
   * @return int The next unique id. */
  public int getNextUniqueId();

  /**
   * 'this' viewpart's id.
  
   * @return String 'this' viewpart's id. */
  public String getViewId();
  
  /**
   * sets 'this' viewpart's id.
   * 
   * @param newViewId String
   */
  public void setViewId(String newViewId);
  
  /**
   * Set the root of the view, so others can change it to be something different
   * than the default.
   * @param objNewRoot - the new root to set.  Can be an IResource or a GenericContainer
   */
  public void setRoot(Object objNewRoot);
  
  /**
   * Return the root object that is currently being used.
   * @return
   */
  public Object getRoot();
  
  /**
   * Compute the default root of the tree for this view.
   * @return
   */
  public Object getDefaultRoot();
  
  /**
   * Set the view that appears in the tab's title.
   * @param strTile The tab's string title.
   */
  public void setViewTitle(String strTile);
  
  /**
   * Set the tab's tooltip.  This appears on hover over the tab itself.
   * @param strToolTip The text that is to be displayed.
   */
  public void setViewToolTip(String strToolTip);

  /**
   * Method getPage.
   * @return IWorkbenchPage
   */
  public IWorkbenchPage getPage();
  
  /**
   * Method getActionBars.
   * @return IActionBars
   */
  public IActionBars getActionBars();
  
  public TreeExplorer getTreeExplorer();
  
  public TableExplorer getTableExplorer();
  
  /**
   * Returns true if this path is managed by this explorer (as defined by the roots and
   * the filters)
   * @param path
  
   * @return boolean
   */
  public boolean isManagedPath(CmsPath path);
}
