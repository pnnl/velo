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
package gov.pnnl.cat.ui.rcp.dialogs.properties.user;

import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.security.IGroup;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.users.actions.ChangeMyPasswordAction;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 */
public class UserPropertiesComposite extends Composite {

  private Text homeFolderText;
  private Text passwordText;
  private Text usernameText;
  private Button sysAdminButton;

  private IUser user;
  private IUser loggedInUser;

  private Logger logger = CatLogger.getLogger(getClass());

  /**
   * Constructor for UserPropertiesComposite.
   * @param parent Composite
   * @param style int
   * @param newUser IUser
   */
  public UserPropertiesComposite(Composite parent, int style, IUser newUser) {
    super(parent, style);
    this.user = newUser;

    loggedInUser = ResourcesPlugin.getSecurityManager().getActiveUser();

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    setLayout(gridLayout);

    final Label usernameLabel = new Label(this, SWT.NONE);
    usernameLabel.setLayoutData(new GridData());
    usernameLabel.setText("Username:");

    usernameText = new Text(this, SWT.BORDER);
    usernameText.setEditable(false);
    usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    final Label passwordLabel = new Label(this, SWT.NONE);
    passwordLabel.setText("Password:");

    passwordText = new Text(this, SWT.BORDER);
    passwordText.setEditable(false);
    passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    passwordText.setEchoChar('*');

    final Button changePasswordButton = new Button(this, SWT.NONE);
    final GridData gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
    changePasswordButton.setLayoutData(gridData);
    changePasswordButton.setText("Change Password...");

    changePasswordButton.setEnabled(true);

    new Label(this, SWT.NONE);

    //    final String currentUserLoggedIn = ResourcesPlugin.getSecurityManager().getUsername();

    changePasswordButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        ChangeMyPasswordAction action = new ChangeMyPasswordAction();
        action.setUsername(user.getUsername());
        action.run(null);
      }
    });

    sysAdminButton = new Button(this, SWT.CHECK);
    sysAdminButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
    sysAdminButton.setEnabled(false);
    sysAdminButton.setText("System Administrator");

    final Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

    final Label homeFolderLabel = new Label(this, SWT.NONE);
    homeFolderLabel.setText("Home Folder:");

    homeFolderText = new Text(this, SWT.NONE);
    homeFolderText.setEditable(false);
    homeFolderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    final Button findHomeButton = new Button(this, SWT.NONE);
    final GridData gridData_1 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1);
    findHomeButton.setLayoutData(gridData_1);
    findHomeButton.setText("Go To Home Folder");
    findHomeButton.setToolTipText("Navigate to this user's home folder");

    final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

    findHomeButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent event) {
        CmsPath homeFolder = user.getHomeFolder();
        if (user != null && homeFolder != null) {
          try {
            RCPUtil.selectResourceInTree(homeFolder);
          } catch (ResourceException e) {
            ToolErrorHandler.handleError("Unable to navigate to resource.", e, true);
          }
        }
      }
    });

    if (user != null) {
      usernameText.setText(user.getUsername());
      passwordText.setText("**********");
      sysAdminButton.setSelection(user.isAdmin());
      CmsPath userHomePath = user.getHomeFolder();
      String userHomeStr = "";
      if (userHomePath != null) {
        userHomeStr = userHomePath.toDisplayString();
      } else {
        findHomeButton.setEnabled(false);
        userHomeStr = "Not Visible";
      }
      homeFolderText.setText(userHomeStr);
    }
    
    final Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
    final GridData gridData_2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
    gridData_2.heightHint = 15;
    separator.setLayoutData(gridData_2);

    //what groups does the person belong to:
    Label groupsLabel = new Label(this, SWT.NONE);
    groupsLabel.setText("Team(s):");
    groupsLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    Text groupsText = new Text(this, SWT.WRAP | SWT.MULTI);
    groupsText.setEditable(false);
    groupsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    ArrayList<ITeam> groups;
    try {
      groups = ResourcesPlugin.getSecurityManager().getUserTeams(this.user.getUsername());
      String groupsString = "";
      for (IGroup group : groups) {
        groupsString += group.getName() + "\n ";
      }
      if(groups.size() > 0){
        groupsText.setText(groupsString);
      }else {
        groupsText.setText("No Teams");
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ServerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    if( (!loggedInUser.isAdmin() && !loggedInUser.equals(user)) || user.getUsername().equals("guest") ) {
      changePasswordButton.setEnabled(false);
    }
  }


  /**
   * Method getSysAdminButton.
   * @return Button
   */
  public Button getSysAdminButton() {
    return sysAdminButton;
  }

  /**
   * Method getPasswordText.
   * @return Text
   */
  public Text getPasswordText() {
    return passwordText;
  }

  /**
   * Method getUsernameText.
   * @return Text
   */
  public Text getUsernameText() {
    return usernameText;
  }
}
