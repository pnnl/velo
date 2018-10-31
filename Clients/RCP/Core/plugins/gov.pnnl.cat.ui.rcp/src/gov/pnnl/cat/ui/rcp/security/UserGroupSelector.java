package gov.pnnl.cat.ui.rcp.security;

import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.model.TeamInput;
import gov.pnnl.cat.ui.rcp.model.UserInput;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.TableCatWorkbenchProvider;
import gov.pnnl.cat.ui.rcp.views.profile.ProfilableFilteredTree;
import gov.pnnl.cat.ui.rcp.views.teams.TeamFilteredTree;
import gov.pnnl.cat.ui.rcp.views.users.UserFilteredTree;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.PatternFilter;

public class UserGroupSelector extends Composite {

  private ProfilableFilteredTree userFilteredTree;
  private TableViewer tableViewer;
  private Tree tree;
  private Table table;
  private Button addButton;
  private Button removeButton;
  private boolean isUsers;
  
  @SuppressWarnings("unused")
  private final static Logger logger = CatLogger.getLogger(UserGroupSelector.class);

  
  public UserGroupSelector(Composite parent, int style, boolean isUsers) {
    super(parent, style);
    this.isUsers = isUsers;
    createControl();
  }

  public void createControl() {
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    this.setLayout(gridLayout);
    final Label directionsLabel = new Label(this, SWT.WRAP);
    final GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 2);
    gridData.heightHint = 45;
    directionsLabel.setLayoutData(gridData);
    
    String labelText = "User";
    if(!isUsers){
      labelText = "Team";
    }
    
    directionsLabel.setText("Select "+labelText.toLowerCase()
        +"s from the left, click the '>>' button to allow them access.\nRemove access by selecting "+
        labelText.toLowerCase()+"s on the right and clicking the '<<' button.");

    final Label usersLabel = new Label(this, SWT.NONE);
    usersLabel.setText("Available "+labelText+"s");
    new Label(this, SWT.NONE);
    final Label teamMembersLabel = new Label(this, SWT.NONE);
    teamMembersLabel.setText("Allowed "+labelText+"s");

    // we'll use the same filtered tree to display the users as we use in the
    // users view
    if(isUsers){
      userFilteredTree = new UserFilteredTree(this);
    }else{
      // set filter to not include /Premier Network Contributors team
      PatternFilter filter = new PatternFilter() {
        public boolean isElementVisible(Viewer viewer, Object element){
          ITeam team = ((ITeam) element);
          return !team.getName().equals("Premier Network Contributors");        
        }
      };
      filter.setPattern("org.eclipse.ui.keys.optimization.false");
      userFilteredTree = new TeamFilteredTree(this, ProfilableFilteredTree.DEFAULT_STYLE,filter, true);
      
    }
    userFilteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
    tree = userFilteredTree.getViewer().getTree();
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

    // TODO: consider using an image for the add button
    addButton = new Button(this, SWT.NONE);
    addButton.setText(">>");

    tableViewer = new TableViewer(this, SWT.BORDER | SWT.MULTI);
    tableViewer.setContentProvider(new TableCatWorkbenchProvider());
    tableViewer.setLabelProvider(new CatWorkbenchLabelProvider(tableViewer));
    tableViewer.setInput(new Object());
    table = tableViewer.getTable();
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3);
    gd.widthHint  = 105;
    table.setLayoutData(gd);

    // TODO: consider using an image for the remove button
    removeButton = new Button(this, SWT.NONE);
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

    new Label(this, SWT.NONE);

//    // add the existing team members to the table
//    for (IUser user : members) {
//      tableViewer.add(user);
//      updateFilter(user, true);
//    }

    updateButtons();
    userFilteredTree.refresh();
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
      updateFilter((IProfilable) user, true);
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

  private void updateFilter(IProfilable user, boolean filter) {
    // get the UserInput that the user tree uses
    if(isUsers){
      UserInput userInput = ((UserFilteredTree)userFilteredTree).getInput();
      if (filter) {
        userInput.addFilter(((IUser)user).getUsername());
      } else {
        userInput.removeFilter(((IUser)user).getUsername());
      }
    }else{
      TeamInput userInput = ((TeamFilteredTree)userFilteredTree).getInput();
      if (filter) {
        userInput.addFilter(((ITeam)user).getName());
      } else {
        userInput.removeFilter(((ITeam)user).getName());
      }
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
      IProfilable selectedUser = (IProfilable) selectedItem.getData();

      for (TableItem memberItem : table.getItems()) {
        IProfilable member = (IProfilable) memberItem.getData();

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
   * Returns the team members that the user has specified.
   * @return a List<IUser> of the team members the user has chosen.
   */
  public List<IProfilable> getSelectedMembers() {
    TableItem[] teamMemberItems = table.getItems();
    List<IProfilable> teamMembers = new ArrayList<IProfilable>(teamMemberItems.length);

    for (TableItem teamMemberItem : teamMemberItems) {
      teamMembers.add(((IProfilable) teamMemberItem.getData()));
    }
    return teamMembers;
  }
}
