/**
 * 
 */
package gov.pnnl.cat.ui.rcp.security;

import gov.pnnl.cat.core.resources.security.IProfilable;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Dialog to select users or groups
 * @author D3K339
 *
 */
public class SelectUsersGroupsDialog extends Dialog {
  private UserGroupSelector groupsSelector;
  private UserGroupSelector usersSelector;
  private List<IProfilable> selectedUsers;
  private List<IProfilable> selectedTeams;

  protected SelectUsersGroupsDialog(Shell parentShell) {
    super(parentShell);
  }

  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout gridLayout = (GridLayout) container.getLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.numColumns = 1;
    gridLayout.makeColumnsEqualWidth = true;
    
    TabFolder tabFolder = new TabFolder(container, SWT.NONE);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    tabFolder.setLayoutData(gridData);
    tabFolder.setLayout(new GridLayout(1, true));
    
    TabItem tbtmGroups = new TabItem(tabFolder, SWT.NONE);
    tbtmGroups.setText("Teams");
    groupsSelector = new UserGroupSelector(tabFolder, SWT.NONE, false);
    groupsSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    tbtmGroups.setControl(groupsSelector);

    TabItem tbtmUsers = new TabItem(tabFolder, SWT.NONE);
    tbtmUsers.setText("Users");
    usersSelector = new UserGroupSelector(tabFolder, SWT.NONE, true);
    usersSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    tbtmUsers.setControl(usersSelector);
    
    
    return container;
  }
  
  // overriding this methods allows you to set the
  // title of the custom dialog
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Selection Users or Groups");
  }

  @Override
  public boolean close() {
    selectedUsers = usersSelector.getSelectedMembers();
    selectedTeams = groupsSelector.getSelectedMembers();
    return super.close();
  }

  public List<IProfilable> getSelectedTeams() {
    return selectedTeams;
  }
  
  public List<IProfilable> getSelectedUsers() {
    return selectedUsers;
  }

  /**
   * Create contents of the button bar.
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  /**
   * Return the initial size of the dialog.
   */
  @Override
  protected Point getInitialSize() {
    return new Point(500, 550);
  }

  @Override
  protected boolean isResizable() {
    return true;
  }
  
  
}
