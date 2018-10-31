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

import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 */
public class TeamTreeDialog extends SelectionDialog {

  protected static Logger logger = CatLogger.getLogger(TeamTreeDialog.class);
  
  private ITeam selectedTeam;
//  private TeamTreeViewer treeViewer;
  private TreeViewer treeViewer;
  
  /**
   * Constructor for TeamTreeDialog.
   * @param parentShell Shell
   */
  public TeamTreeDialog(Shell parentShell) {
    super(parentShell);
    setShellStyle(this.getShellStyle() | SWT.RESIZE);
  }

  /**
   * Method createDialogArea.
   * @param parent Composite
   * @return Control
   */
  protected Control createDialogArea(Composite parent) {
    TeamFilteredTree filteredTree = new TeamFilteredTree(parent);
    //treeViewer = (TeamTreeViewer)filteredTree.getViewer();
    treeViewer = filteredTree.getViewer();
    
    Tree tree = treeViewer.getTree();
    GridData grid_data = createLayoutData();

    parent.getShell().setText("Team Tree");
    tree.setLayoutData(grid_data);

    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent e) {
        if(!e.getSelection().isEmpty()){
          StructuredSelection teamSelection = (StructuredSelection)(e.getSelection());
          Object[] teams = teamSelection.toArray();
          
          selectedTeam = (ITeam)teams[0];
          //EZLogger.logWarning("selection:"+selectedTeam.getName(), null);
          logger.warn("selection:"+selectedTeam.getName());
        }
      }
    });

    treeViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent e) {
        if(!e.getSelection().isEmpty()){
          StructuredSelection teamSelection = (StructuredSelection)(e.getSelection());
          Object[] teams = teamSelection.toArray();
          selectedTeam = (ITeam)teams[0];
        }
        okPressed();
      }
    });

    // if they're already selected something, make that the selected item on the tree:
    //TODO: this is not good yet
    //Since we now use TeamFilteredTree which has its own TreeViewer,
    //can we still implement TreeViewer::expandToPath(ITeam)?
    //treeViewer.expandToPath(this.selectedTeam);
    filteredTree.expandToPath(this.selectedTeam); //as Eric suggested
    
    return treeViewer.getControl();
  }

  /**
   * Method createLayoutData.
   * @return GridData
   */
  private GridData createLayoutData() {
    GridData grid_data = new GridData();

    grid_data.grabExcessHorizontalSpace = true;
    grid_data.grabExcessVerticalSpace = true;
    grid_data.horizontalAlignment = SWT.FILL;
    grid_data.verticalAlignment = SWT.FILL;
    return grid_data;
  }

  /**
   * Method getInitialSize.
   * @return Point
   */
  protected Point getInitialSize() {
    return new Point(375, 480);
  }

  /**
   * Method getSelectedTeam.
   * @return ITeam
   */
  public ITeam getSelectedTeam() {
    return selectedTeam;
  }

  /**
   * Method setSelectedTeam.
   * @param t ITeam
   */
  public void setSelectedTeam(ITeam t) {
    this.selectedTeam = t;
    
  }

}
