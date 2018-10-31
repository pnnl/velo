package gov.pnnl.cat.ui.rcp.security;

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.velo.model.ACE;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wb.swt.SWTResourceManager;

public class PermissionsForm extends Composite {
  
  private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
  private TableViewer tableViewer;
  private Composite parent;
  private Composite permissionDetails;
  private Label lblPermissionLevel;
  private StyledText lblOwner;
  private Combo permissionsCombo;
  private StyledText lblPermissionDescription;
  private Composite btnComposite;
  private Button btnRemove;
  private Button btnAdd;
  private ACL acl;
  private Set<ACE> aces;
  private boolean enabled = true;
  
  IResourceManager rmgr = ResourcesPlugin.getResourceManager();
  ISecurityManager smgr = ResourcesPlugin.getSecurityManager();

  private List<PermissionDescriptor> permissionDescriptors;
  private Map<String, PermissionDescriptor> alfrescoPermToDescriptor;
    
  /**
   * Create the composite.
   * @param parent
   * @param style
   */
  public PermissionsForm(Composite parent, int style) {
    this(parent, style, null);
  }
  
  public PermissionsForm(Composite parent, int style, List<PermissionDescriptor> permissionDescriptors) {
    super(parent, SWT.NONE);
    this.parent = parent;
    if(permissionDescriptors == null) {
      this.permissionDescriptors = getDefaultPermissionDescriptors();;
    } else {
      this.permissionDescriptors = permissionDescriptors;
    }
    // make a map for quick access
    alfrescoPermToDescriptor = new HashMap<String, PermissionDescriptor>();
    for(PermissionDescriptor desc : this.permissionDescriptors) {
      alfrescoPermToDescriptor.put(desc.getAlfrescoPermission(), desc);
    }
    
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        toolkit.dispose();
      }
    });
    setBackground(parent.getBackground());
    toolkit.adapt(this);
    toolkit.paintBordersFor(this);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = false;
    setLayout(layout);
    
    createOwnerSection();
    createTableSection();
    createButtonSection();
    createPermissionsSection();
    
    new Label(this, SWT.NONE);

  }
  
  private List<PermissionDescriptor> getDefaultPermissionDescriptors() {
    List<PermissionDescriptor> defaults = new ArrayList<PermissionDescriptor>(10);
    
    // Coordinator
    PermissionDescriptor desc = new PermissionDescriptor(ACE.PERMISSION_COORDINATOR, 
        "Read all items.\nCreate new items.\nEdit all items.\nDelete all items.", "Coordinator");
    defaults.add(desc);

    // Collaborator
    desc = new PermissionDescriptor(ACE.PERMISSION_COLLABORATOR, 
        "Read all items.\nCreate new items.\nEdit all items.\nDelete items they own.", "Collaborator");
    defaults.add(desc);

    // Contributor
    desc = new PermissionDescriptor(ACE.PERMISSION_CONTRIBUTOR, 
        "Read all items.\nCreate new items.\nEdit items they own.\nDelete items they own.", "Contributor");
    defaults.add(desc);
      
    // Consumer
    desc = new PermissionDescriptor(ACE.PERMISSION_CONSUMER, "Read all items.", "Consumer");
    defaults.add(desc);
    
    return defaults;
  }
  
  private void createOwnerSection() {
    lblOwner =  new StyledText(this, SWT.FULL_SELECTION);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    gridData.verticalIndent = 5;
    gridData.heightHint = 25;
    lblOwner.setLayoutData(gridData);
  }
  
  private void createTableSection() {
    Composite tableComposite = new Composite(this, SWT.NONE);
    GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
    layoutData.heightHint = 200;
    tableComposite.setLayoutData(layoutData);
    
    tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SCROLL_LINE | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
    tableViewer.getTable().getVerticalBar().setVisible(true);
    tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    toolkit.adapt(tableViewer.getTable());
    toolkit.paintBordersFor(tableViewer.getTable());
    tableViewer.getTable().setHeaderVisible(true);
    tableViewer.getTable().setLinesVisible(true);
    
    tableViewer.setContentProvider(ArrayContentProvider.getInstance());
    
    // Name column
    TableViewerColumn tblclmnName = new TableViewerColumn(tableViewer, SWT.NONE);
    tblclmnName.getColumn().setText("Name");
    tblclmnName.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        ACE ace = (ACE) element;
        String authority = ace.getAuthority();
        if(authority.startsWith("GROUP_")) {
          authority = authority.substring(6);

          if(authority.startsWith("/")) {
            // team could be a path, so we only want the team name
            CmsPath tmp = new CmsPath(authority);
            authority = tmp.getName();
          } else if(authority.equals("EVERYONE")) {
            authority = ITeam.TEAM_NAME_ALL_USERS;
          }
        } else {
          // this is a user - look up full name
          IUser user = smgr.getUser(authority);
          String fullName = user.getFullName();
          if(fullName == null || fullName.isEmpty()) {
            fullName = user.getUsername();
          }
          authority = fullName;
        }
        return authority;
      }
    });
    
    // Permission column
    TableViewerColumn tblclmnPermissionLevel = new TableViewerColumn(tableViewer, SWT.NONE);
    tblclmnPermissionLevel.getColumn().setText("Permission Level");
    tblclmnPermissionLevel.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        ACE ace = (ACE) element;
        PermissionDescriptor desc = alfrescoPermToDescriptor.get(ace.getPermission());
        return desc.getDisplayLabel();
      }
    });
            
    TableColumnLayout tableColumnLayout = new TableColumnLayout();
    tableColumnLayout.setColumnData(tblclmnName.getColumn(), new ColumnWeightData(100));
    tableColumnLayout.setColumnData(tblclmnPermissionLevel.getColumn(), new ColumnWeightData(100));
    tableComposite.setLayout(tableColumnLayout);
    
    // selection listener for row selections
    tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        ACE selected = (ACE)selection.getFirstElement(); // only allow single selection
        if(selected == null) {
          permissionsCombo.clearSelection();
          lblPermissionDescription.setText("");
          permissionsCombo.setEnabled(false);
        } else {
          for(int i = 0; i < permissionDescriptors.size(); i++) {
            PermissionDescriptor desc = permissionDescriptors.get(i);
            if(desc.getAlfrescoPermission().equals(selected.getPermission())) {
              permissionsCombo.select(i);
              setPermissionDescription(desc.getDisplayDescription());
              permissionsCombo.setEnabled(enabled);
              break;
            }
          }
        }
      }
    });

  }
  
  private void setPermissionDescription(String desc) {
    lblPermissionDescription.setText(desc);
    
    // add bullets to the list
    StyleRange style2 = new StyleRange();
    style2.metrics = new GlyphMetrics(0, 0, 120);
    style2.foreground = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    Bullet bullet = new Bullet (ST.BULLET_TEXT, style2);
    bullet.text = "\u2713";

    lblPermissionDescription.setLineBullet(0, lblPermissionDescription.getLineCount(), bullet);
    getShell().layout(true, true); // revalidate so widgets get resized

  }
  
  private void createButtonSection() {
    btnComposite = new Composite(this, SWT.NONE);
    btnComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    toolkit.adapt(btnComposite);
    toolkit.paintBordersFor(btnComposite);
    btnComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
    
    btnAdd = new Button(btnComposite, SWT.NONE);
    toolkit.adapt(btnAdd, true, true);
    btnAdd.setText("Add");
    btnAdd.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showSelectUsersGroupsDialog();
      }
    });
    
    btnRemove = new Button(btnComposite, SWT.NONE);
    toolkit.adapt(btnRemove, true, true);
    btnRemove.setText("Remove");
    btnRemove.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        List<ACE> acesToRemove = new ArrayList<ACE>();
        for(int selected : tableViewer.getTable().getSelectionIndices()) {
          System.out.println("removing ace at index: " + selected);
         
          int i = 0;
          for(ACE ace : aces) {
            if(i == selected) {
              acesToRemove.add(ace);
              break;
            }
            i++;
          }
        }
        for(ACE ace : acesToRemove) {
          aces.remove(ace);
        }
        
          tableViewer.refresh();          
        getShell().layout(true, true); // revalidate so widgets get resized
        }
    });

  }
  
  private void createPermissionsSection() {
    permissionDetails = new Composite(this, SWT.NONE);
    permissionDetails.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    toolkit.adapt(permissionDetails);
    toolkit.paintBordersFor(permissionDetails);
    GridLayout gl_permissionDetails = new GridLayout(2, false);
    gl_permissionDetails.marginHeight = 15;
    permissionDetails.setLayout(gl_permissionDetails);
    
    lblPermissionLevel = new Label(permissionDetails, SWT.NONE);
    toolkit.adapt(lblPermissionLevel, true, true);
    lblPermissionLevel.setText("Permission Level:");
    
    permissionsCombo = new Combo(permissionDetails, SWT.NONE);
    permissionsCombo.setEnabled(false); // only enabled if table item selected
    permissionsCombo.setItems(getPermissionLevels());
    permissionsCombo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        int idx = permissionsCombo.getSelectionIndex();
        if(idx < 0) {
          lblPermissionDescription.setText("");
        } else {
          PermissionDescriptor desc = permissionDescriptors.get(idx);
          setPermissionDescription(desc.getDisplayDescription());
          
          // update the table model
          IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
          ACE ace = (ACE)selection.getFirstElement(); // only allow single selection
          if(ace != null) {
            ace.setPermission(desc.getAlfrescoPermission());
            tableViewer.refresh();
          }

        }
      }
    });
  
    permissionsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    toolkit.adapt(permissionsCombo);
    toolkit.paintBordersFor(permissionsCombo);

    lblPermissionDescription = new StyledText(this, SWT.FULL_SELECTION | SWT.WRAP);
    lblPermissionDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    
  }
  
  private String[] getPermissionLevels() {
    List<String> labels = new ArrayList<String>(10);
    for(PermissionDescriptor desc : permissionDescriptors) {
      labels.add(desc.getDisplayLabel());
    }
    return labels.toArray(new String[labels.size()]);
  }
    
  public void setFormBackgroundColor(Color color) {
    setBackground(color);
    permissionDetails.setBackground(color);
    lblPermissionLevel.setBackground(color);
    lblPermissionDescription.setBackground(color);
    lblOwner.setBackground(color);
    btnComposite.setBackground(color);
    btnAdd.setBackground(color);
    btnRemove.setBackground(color);
  }
  
  public void loadPermissions(ACL permissions) {
    aces = new HashSet<ACE>();
    acl = permissions;
    if(permissions.getAces() != null){
      for(ACE ace : permissions.getAces()) {
        // TODO: we may need to group aces if more than one permission is set for the same user
        String permission = ace.getPermission();
        // we don't need to confuse users by seeing multiple permissions that mean the same thing
        // TODO: we should change the bootstrapped permissions of the home folders to be Coordinator instead of All
        if(permission.toLowerCase().equals("all")) {
          ace.setPermission(ACE.PERMISSION_COORDINATOR);
        }
        aces.add(ace);
      }
    }
    // set the table model
    tableViewer.setInput(aces);
    
    if(!isEditable()) {
      disableDialog();
    }
    String note = "    (only owners, coordinators, or admin can change permissions)";
    String owner = acl.getOwner();
    if(owner.startsWith("GROUP_/")) {
      owner = owner.substring(7);
    }
    String text = "Owner:  " + owner + note;
    lblOwner.setText(text);
    StyleRange styleRange = new StyleRange();
    styleRange.start = 0;
    styleRange.length = 8 ;
    styleRange.fontStyle = SWT.BOLD;
    lblOwner.setStyleRange(styleRange);
    
    StyleRange styleRange2 = new StyleRange();
    styleRange2.start = 8;
    styleRange2.length = owner.length() ;
    styleRange2.fontStyle = SWT.BOLD;
    styleRange2.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
    lblOwner.setStyleRange(styleRange2);
    
    StyleRange styleRange3 = new StyleRange();
    styleRange3.start = 8 + owner.length();
    styleRange3.length = note.length();
    //make font smaller
    styleRange3.font = SWTResourceManager.getFont("Segoe UI", 8, SWT.ITALIC);
    lblOwner.setStyleRange(styleRange3);
  }
  
  /**
   * If the current user isn't the owner, coordinator (TODO) or admin, disable the dialog since we only
   * allow these roles to set permissions
   * @return
   */
  public boolean isEditable() {
    boolean editable = false;
    // if the current user isn't the owner or admin, not editable
    IUser currentUser = smgr.getActiveUser();
    
    boolean coordinator = smgr.hasPermissions(new CmsPath(acl.getNodePath()), ACE.PERMISSION_COORDINATOR);
    if(coordinator || currentUser.isAdmin() || currentUser.getUsername().toLowerCase().equals(acl.getOwner().toLowerCase())) {
      editable = true;
    }
    return editable;
  }
  
  
  private void disableDialog() {
    enabled = false;
    permissionsCombo.setEnabled(false);
    btnAdd.setEnabled(false);
    btnRemove.setEnabled(false);
  }
  
  public ACL getPermissions() {
    // bind the latest changes to the data model
    acl.setAces(aces.toArray(new ACE[aces.size()]));
    acl.setInheritPermissions(false);
    return acl;    
  }
  
  private void showSelectUsersGroupsDialog() {
    SelectUsersGroupsDialog dialog = new SelectUsersGroupsDialog(getShell());
    dialog.create();
    if (dialog.open() == Window.OK) {
      List<IProfilable> selectedTeams = dialog.getSelectedTeams();
      List<IProfilable> selectedUsers = dialog.getSelectedUsers();
      for(IProfilable user : selectedUsers) {
        addAce(((IUser)user).getUsername());
      }
      for(IProfilable team : selectedTeams) {
        String groupName;
        if(((ITeam)team).getPath().getName().equals(ITeam.TEAM_NAME_ALL_USERS)) {
          groupName = "GROUP_EVERYONE";
          
        } else {
          String teamPath = ((ITeam)team).getPath().toDisplayString();
          groupName = "GROUP_"+ teamPath;
        }
        
        addAce(groupName);
      }
      tableViewer.refresh();
    } 
    
  }
  
  private void addAce(String authority) {
    System.out.println("Adding authority: " + authority);

    ACE ace = new ACE();
    ace.setAuthority(authority);
    ace.setPermission(ACE.PERMISSION_CONSUMER);
    ace.setAccessStatus(ACE.ACCESS_STATUS_ALLOWED);
    aces.add(ace); 
  }
  
}
