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
package gov.pnnl.cat.ui.rcp.dialogs.properties.team;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.model.UserInput;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.TableCatWorkbenchProvider;
import gov.pnnl.cat.ui.rcp.views.users.UserFilteredTree;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A dialog for managing the members of a team.
 * The dialog allows the user to view the existing team members and add or
 * remove users from this list.
 * 
 * TODO: add drag and drop support
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public class ManageTeamMembersDialog extends Dialog {

  private String teamName;
  private List<IUser> members;
  private UserFilteredTree userFilteredTree;
  private TableViewer tableViewer;
  private Tree tree;
  private Table table;
  private Button addButton;
  private Button removeButton;
  private ISecurityManager mgr;
  private List<IUser> result;
  
  private final static Logger logger = CatLogger.getLogger(ManageTeamMembersDialog.class);

  /**
   * Create the dialog
   * @param parentShell
   * @param name String
   * @param members List<IUser>
   */
  public ManageTeamMembersDialog(String name, List<IUser> members, Shell parentShell) {
    super(parentShell);
    setShellStyle(getShellStyle() | SWT.RESIZE);
    this.teamName = name;
    this.members = members;
    this.mgr = ResourcesPlugin.getSecurityManager();
  }

  /**
   * Create contents of the dialog
   * @param parent
   * @return Control
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    container.setLayout(gridLayout);
    final Label directionsLabel = new Label(container, SWT.NONE);
    final GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
    gridData.heightHint = 25;
    directionsLabel.setLayoutData(gridData);
    directionsLabel.setText("Select users from the left and add them to the team members displayed on the right.");

    final Label usersLabel = new Label(container, SWT.NONE);
    usersLabel.setText("Available Users");
    new Label(container, SWT.NONE);
    final Label teamMembersLabel = new Label(container, SWT.NONE);
    teamMembersLabel.setText(teamName + " Team Members");

    // we'll use the same filtered tree to display the users as we use in the
    // users view
    userFilteredTree = new UserFilteredTree(container);
    userFilteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
    tree = userFilteredTree.getViewer().getTree();
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

    // TODO: consider using an image for the add button
    addButton = new Button(container, SWT.NONE);
    addButton.setText(">>");

    tableViewer = new TableViewer(container, SWT.BORDER | SWT.MULTI);
    tableViewer.setContentProvider(new TableCatWorkbenchProvider());
    tableViewer.setLabelProvider(new CatWorkbenchLabelProvider(tableViewer));
    tableViewer.setInput(new Object());
    table = tableViewer.getTable();
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));

    // TODO: consider using an image for the remove button
    removeButton = new Button(container, SWT.NONE);
    removeButton.setLayoutData(new GridData());
    removeButton.setText("<<");

    // create a selection listener that simply calls our method to update
    // the enabled status of the add and remove buttons
    SelectionAdapter updateButtonsListener = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateButtons();
      }
    };

    // register the table and tree to listen for selection changes to trigger
    // an update of the add and remove buttons.
    tree.addSelectionListener(updateButtonsListener);
    table.addSelectionListener(updateButtonsListener);

    // when the user double-clicks a user in the tree viewer, we need to add
    // him as a member
    userFilteredTree.getViewer().addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        addMember();
      }
    });

    // when the user double-clicks a user in the table, we need to remove him
    // from the member list.
    tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        removeMember();
      }
    });

    // when the user clicks the add button, we need to add the selected user(s)
    // to the table of team members and remove the user from the filtered tree
    // of available users.
    addButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        addMember();
      }
    });

    // when the user clicks the remove button, we need to remove the selected
    // user(s) from the table of team members and add the user back into the
    // filtered tree of available users.
    removeButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        removeMember();
      }
    });

    new Label(container, SWT.NONE);

    // add the existing team members to the table
    for (IUser user : members) {
      tableViewer.add(user);
      updateFilter(user, true);
    }

    updateButtons();
    userFilteredTree.refresh();

    return container;
  }

  /**
   * Removes the currently selected user from the tree of available users
   * and adds him to the table of members.
   */
  private void addMember() {
    TreeSelection selection = (TreeSelection) userFilteredTree.getViewer().getSelection();
    Object[] users = selection.toArray();
    tableViewer.add(users);

    // add a filter to exclude all of the users in the selection
    for (Object user : users) {
      updateFilter((IUser) user, true);
    }

    updateButtons();
    userFilteredTree.refresh();
  }

  /**
   * Removes the currently selected user from the table of team members and
   * adds him to the tree of available users.
   */
  private void removeMember() {
    StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
    Object[] users = selection.toArray();
    tableViewer.remove(users);

    // remove the filters so that we no longer exclude the selected users.
    for (Object user : users) {
      updateFilter((IUser) user, false);
    }

    updateButtons();
    userFilteredTree.refresh();
  }

  /**
   * Method updateFilter.
   * @param user IUser
   * @param filter boolean
   */
  private void updateFilter(IUser user, boolean filter) {
    // get the UserInput that the user tree uses
    UserInput userInput = userFilteredTree.getInput();

    if (filter) {
      userInput.addFilter(user.getUsername());
    } else {
      userInput.removeFilter(user.getUsername());
    }
  }

  /**
   * Updates teh enabled status of the add and remove buttons.
   */
  private void updateButtons() {
    updateRemoveButton();
    updateAddButton();
  }

  /**
   * Updates the enabled status of the remove button.
   */
  private void updateRemoveButton() {
    TableItem[] selection = table.getSelection();
    removeButton.setEnabled(selection.length > 0);
  }

  /**
   * Updates the enabled status of the add button.
   */
  private void updateAddButton() {
    TreeItem[] selection = tree.getSelection();

    // compare each user in the selection to each user already in the
    // members table. if the user has selected someone that is already
    // a member, we need to disable the add button.
    for (TreeItem selectedItem : selection) {
      IUser selectedUser = (IUser) selectedItem.getData();

      for (TableItem memberItem : table.getItems()) {
        IUser member = (IUser) memberItem.getData();

        if (selectedUser.equals(member)) {
          // once we've disabled the add button, there is no reason to
          // continue.
          addButton.setEnabled(false);
          return;
        }
      }
    }

    addButton.setEnabled(selection.length > 0);
  }

  /**
   * Create contents of the button bar
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  /**
   * Return the initial size of the dialog
   * @return Point
   */
  @Override
  protected Point getInitialSize() {
    return new Point(500, 375);
  }

  /**
   * Method configureShell.
   * @param newShell Shell
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Manage Team Members");
  }

  /*
   * Save the team members before we close the dialog.
   * If we don't save the team members here, the table widget may be
   * disposed by the time we are asked for them.
   * 
   * (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  public void okPressed() {
    TableItem[] teamMemberItems = table.getItems();
    List<IUser> teamMembers = new ArrayList<IUser>(teamMemberItems.length);

    for (TableItem teamMemberItem : teamMemberItems) {
      teamMembers.add((IUser) teamMemberItem.getData());
    }

    setResult(teamMembers);
    super.okPressed();
  }

  public void cancelPressed() {
    setResult(new ArrayList<IUser>(0));
    super.cancelPressed();
  }

  /**
   * Method setResult.
   * @param teamMembers List<IUser>
   */
  private void setResult(List<IUser> teamMembers) {
    this.result = teamMembers;
  }

  /**
   * Returns the team members that the user has specified.
  
   * @return a List<IUser> of the team members the user has chosen. */
  public List<IUser> getTeamMembers() {
    return result;
  }
}
