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
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.model.UserInput;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.TableCatWorkbenchProvider;
import gov.pnnl.cat.ui.rcp.views.users.UserSorter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 */
public class TeamMembersPropertiesComposite extends Composite {

  private IUser me; //the current user
  private Table table;
  private TableViewer membersTable;
  private Button removeButton;
  private Button addButton;
  private final static Logger logger = CatLogger.getLogger(TeamMembersPropertiesComposite.class);
  boolean disabled = false;

  /**
   * Constructor for TeamMembersPropertiesComposite.
   * @param parent Composite
   * @param style int
   * @param newTeam ITeam
   */
  public TeamMembersPropertiesComposite(Composite parent, int style, final ITeam newTeam) {
    super(parent, style);

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    setLayout(gridLayout);
  
    membersTable = new TableViewer(this);
    table = membersTable.getTable();
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    membersTable.setContentProvider(new TableCatWorkbenchProvider());
    membersTable.setLabelProvider(new CatWorkbenchLabelProvider(membersTable));
    membersTable.setSorter(new UserSorter());

    //get the current user
    me = ResourcesPlugin.getSecurityManager().getActiveUser();

    if(newTeam != null){
      List<String> members = newTeam.getMembers();
      ISecurityManager mgr = ResourcesPlugin.getSecurityManager();
      UserInput userInput = null;

      userInput = new UserInput(mgr.getUsers(members));
      userInput.setFilterSpecialUsers(true);

      membersTable.setInput(userInput);
    }
    else if(!me.isAdmin())//for a New Team, add the creator is not an admin, add the creator
    {
      UserInput userInput = null;
      List<IUser> defaultUsers = new ArrayList<IUser>(1);
      defaultUsers.add(me);
      userInput = new UserInput(defaultUsers);
      membersTable.setInput(userInput);
    }

    addButton = new Button(this, SWT.NONE);
    addButton.setLayoutData(new GridData(75, SWT.DEFAULT));
    addButton.setText("Add...");
    addButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        logger.debug("adding users to the team");
        TableItem[] memberItems = membersTable.getTable().getItems();
        
        List<IUser> members = new ArrayList<IUser>(memberItems.length);
        for (TableItem item : memberItems)
        {
          members.add((IUser)item.getData());
        }
        String teamName = "";
        if(newTeam != null)
        {
          teamName = newTeam.getName();
        }
        ManageTeamMembersDialog manageTeamMembersDialog = new ManageTeamMembersDialog(teamName, members, getShell());

        if (manageTeamMembersDialog.open() == Dialog.OK) {
          //UserInput userInput = new UserInput(new ArrayList<IUser>());
          membersTable.setInput(new Object());

          java.util.List<IUser> teamMembers = manageTeamMembersDialog.getTeamMembers();
          Object[] newMembers = teamMembers.toArray();
          
          membersTable.add(newMembers);

        }
      }
    });

    // create a selection listener that simply calls our method to update
    // the enabled status of the remove button
    SelectionAdapter updateButtonsListener = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateButtons();
      }
    };

    table.addSelectionListener(updateButtonsListener);

    removeButton = new Button(this, SWT.NONE);
    removeButton.setLayoutData(new GridData(75, SWT.DEFAULT));
    removeButton.setText("Remove");
    
    removeButton.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        //EZLogger.logWarning("remove from membersTable:" + membersTable.getSelection().toString(), null);
        logger.warn("remove from membersTable:" + membersTable.getSelection().toString());
        StructuredSelection selection = (StructuredSelection) membersTable.getSelection();
        Object[] users = selection.toArray();
        for(Object userObj : users)
        {
          IUser user = (IUser)userObj;
          membersTable.remove(user);
        }
      }
    });

    updateButtons();
  }

  public void disableComposite() {
    addButton.setEnabled(false);
    removeButton.setEnabled(false);
    disabled = true;
  }
  
  private void updateButtons() {
    updateRemoveButton();
  }

  /**
   * Updates the enabled status of the remove button.
   */
  private void updateRemoveButton() {
    TableItem[] selection = table.getSelection();
    logger.debug(selection.length + " users selected.");
    removeButton.setEnabled(selection.length > 0 && !disabled);
  }
  /**
   * Get the members
  
   * @return IUser[]
   */
  public IUser[] getMembers() {
    TableItem[] items = membersTable.getTable().getItems();
    IUser [] results = new IUser[items.length];
    
    int i=0;
    for (TableItem memberItem : items) {
      IUser member = (IUser) memberItem.getData();
      results[i++] = member;
    }
    return results;
  }

  /**
   * Get an array of username string
  
   * @return String[]
   */
  public String[] getMembersString() {
    TableItem[] items = membersTable.getTable().getItems();
    String [] results = new String[items.length];
    
    int i=0;
    for (TableItem memberItem : items) {
      IUser member = (IUser) memberItem.getData();
      results[i++] = member.getUsername();
    }
    return results;
  }

}
