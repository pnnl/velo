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
package gov.pnnl.cat.ui.rcp.dialogs;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.model.CmsPath;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Workbench-level composite for resource and container specification by the
 * user. Services such as field validation are performed by the group. The group
 * can be configured to accept existing resources, or only new resources.
 * @version $Revision: 1.0 $
 */
public class ResourceAndContainerGroup implements Listener {
  // problem identifiers
  
  /**
   * Constant for no problem.
   */
  public static final int PROBLEM_NONE = 0;
  /**
   * Constant for empty resource.
   */
  public static final int PROBLEM_RESOURCE_EMPTY = 1;

  /**
   * Constant for resource already exists.
   */
  public static final int PROBLEM_RESOURCE_EXIST = 2;

  /**
   * Constant for invalid path.
   */
  public static final int PROBLEM_PATH_INVALID = 4;

  /**
   * Constant for empty container.
   */
  public static final int PROBLEM_CONTAINER_EMPTY = 5;

  /**
   * Constant for project does not exist.
   */
  public static final int PROBLEM_PROJECT_DOES_NOT_EXIST = 6;

  /**
   * Constant for invalid name.
   */
  public static final int PROBLEM_NAME_INVALID = 7;

  /**
   * Constant for path already occupied.
   */
  public static final int PROBLEM_PATH_OCCUPIED = 8;
  
  /**
   * Constant for path already occupied.
   */
  public static final int PROBLEM_CUSTOM_VALIDATOR_FAILED = 9;

  // the client to notify of changes
  private Listener client;

  // whether to allow existing resources
  private boolean allowExistingResources = false;

  // resource type (file, folder, project)
  //private int resourceType = IResource.FILE;

  // problem indicator
  private String problemMessage = "";//$NON-NLS-1$

  private int problemType = PROBLEM_NONE;

  // widgets
  private ContainerSelectionGroup containerGroup;

  private Text resourceNameField;
  private boolean showFiles;
  private Object root;
  private ResourceSelectionValidator parentFolderValidator;

  /**
   * The resource extension for the resource name field.
   * 
   * @see ResourceAndContainerGroup#setResourceExtension(String)
   * @since 3.3
   */
  private String resourceExtension;

  // constants
  private static final int SIZING_TEXT_FIELD_WIDTH = 250;

  /**
   * Create an instance of the group to allow the user to enter/select a
   * container and specify a resource name.
   * 
   * @param parent
   *            composite widget to parent the group
   * @param client
   *            object interested in changes to the group's fields value
   * @param resourceFieldLabel
   *            label to use in front of the resource name field
   * @param resourceType
   *            one word, in lowercase, to describe the resource to the user
   *            (file, folder, project)
   * @param showFiles boolean
   * @param root Object
   * @param parentFolderValidator ParentFolderValidator
   */
  public ResourceAndContainerGroup(Composite parent, Listener client,
      String resourceFieldLabel, String resourceType, boolean showFiles, Object root, ResourceSelectionValidator parentFolderValidator) {
    this(parent, client, resourceFieldLabel, SWT.DEFAULT, showFiles, root, parentFolderValidator);
  }


  /**
   * Create an instance of the group to allow the user to enter/select a
   * container and specify a resource name.
   * 
   * @param parent
   *            composite widget to parent the group
   * @param client
   *            object interested in changes to the group's fields value
   * @param resourceFieldLabel
   *            label to use in front of the resource name field
  
  
   * @param heightHint
   *            height hint for the container selection widget group
   * @param showFiles boolean
   * @param root Object
   * @param parentFolderValidator ParentFolderValidator
   */
  public ResourceAndContainerGroup(Composite parent, Listener client,
      String resourceFieldLabel, int heightHint, boolean showFiles, Object root, ResourceSelectionValidator parentFolderValidator) {
    super();
    this.root = root;
    this.showFiles = showFiles;
    this.parentFolderValidator = parentFolderValidator;
    createContents(parent, resourceFieldLabel, heightHint);
    this.client = client;
  }

  /**
   * Returns a boolean indicating whether all controls in this group contain
   * valid values.
   * 
  
   * @return boolean */
  public boolean areAllValuesValid() {
    return problemType == PROBLEM_NONE;
  }

  /**
   * Creates this object's visual components.
   * 
   * @param parent
   *            org.eclipse.swt.widgets.Composite
   * @param heightHint
   *            height hint for the container selection widget group
   * @param resourceLabelString String
   */
  protected void createContents(Composite parent, String resourceLabelString,
      int heightHint) {

    Font font = parent.getFont();
    // server name group
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    composite.setFont(font);

    // container group
    if (heightHint == SWT.DEFAULT) {
      containerGroup = new ContainerSelectionGroup(composite, this, null, showFiles, root);
    } else {
      containerGroup = new ContainerSelectionGroup(composite, this,
          null, heightHint, SIZING_TEXT_FIELD_WIDTH, showFiles, root);
    }
        
    // resource name group
    Composite nameGroup = new Composite(composite, SWT.NONE);
    layout = new GridLayout();
    layout.numColumns = 2;
    layout.marginWidth = 0;
    nameGroup.setLayout(layout);
    nameGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
        | GridData.GRAB_HORIZONTAL));
    nameGroup.setFont(font);

    Label label = new Label(nameGroup, SWT.NONE);
    label.setText(resourceLabelString);
    label.setFont(font);

    // resource name entry field
    resourceNameField = new Text(nameGroup, SWT.BORDER);
    resourceNameField.addListener(SWT.Modify, this);
    resourceNameField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        handleResourceNameFocusLostEvent();
      }
    });
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
        | GridData.GRAB_HORIZONTAL);
    data.widthHint = SIZING_TEXT_FIELD_WIDTH;
    resourceNameField.setLayoutData(data);
    resourceNameField.setFont(font);
    validateControls();
  }

  /**
   * Returns the path of the currently selected container or null if no
   * container has been selected. Note that the container may not exist yet if
   * the user entered a new container name in the field.
   * 
  
   * @return The path of the container, or <code>null</code> */
  public CmsPath getContainerFullPath() {
    return containerGroup.getDestinationFolderPath();
  }

  /**
   * Returns an error message indicating the current problem with the value of
   * a control in the group, or an empty message if all controls in the group
   * contain valid values.
   * 
  
   * @return java.lang.String */
  public String getProblemMessage() {
    return problemMessage;
  }

  /**
   * Returns the type of problem with the value of a control in the group.
   * 
  
   * @return one of the PROBLEM_* constants */
  public int getProblemType() {
    return problemType;
  }

  /**
   * Returns a string that is the name of the chosen resource, or an empty
   * string if no resource has been entered. <br>
   * <br>
   * The name will include the resource extension if the preconditions are
   * met.
   * 
  
   * 
  
   * @since 3.3
   * @return The resource name * @see ResourceAndContainerGroup#setResourceExtension(String) */
  public String getResourceName() {
    String resource = resourceNameField.getText();
    if (useResourceExtension()) {
      return resource + '.' + resourceExtension;
    }
    return resource;
  }

  /**
   * Returns the resource extension.
   * 
  
  
   * @since 3.3
   * @return The resource extension or <code>null</code>. * @see ResourceAndContainerGroup#setResourceExtension(String) */
  public String getResourceExtension() {
    return resourceExtension;
  }

  /**
   * Determines whether the resource extension should be added to the resource
   * name field. <br>
   * <br>
   * 
  
  
   * @since 3.3
   * @return <code>true</code> if the preconditions are met; otherwise,
   *         <code>false</code>. * @see ResourceAndContainerGroup#setResourceExtension(String) */
  private boolean useResourceExtension() {
    String resource = resourceNameField.getText();
    if ((resourceExtension != null) && (resourceExtension.length() > 0)
        && (resource.length() > 0)
        && (resource.endsWith('.' + resourceExtension) == false)) {
      return true;
    }
    return false;
  }

  /**
   * Handle the focus lost event from the resource name field. <br>
   * Adds the resource extension to the resource name field when it loses
   * focus (if the preconditions are met).
   * 
  
   * @since 3.3
   * @see ResourceAndContainerGroup#setResourceExtension(String) */
  private void handleResourceNameFocusLostEvent() {
    if (useResourceExtension()) {
      setResource(resourceNameField.getText() + '.' + resourceExtension);
    }
  }

  /**
   * Handles events for all controls in the group.
   * 
   * @param e
   *            org.eclipse.swt.widgets.Event
   * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
   */
  public void handleEvent(Event e) {
    validateControls();
    if (client != null) {
      client.handleEvent(e);
    }
  }
  
  /**
   * Sets the flag indicating whether existing resources are permitted.
   * @param value
   */
  public void setAllowExistingResources(boolean value) {
    allowExistingResources = value;
  }

  /**
   * Sets the value of this page's container.
   * 
   * @param path
   *            Full path to the container.
   */
  public void setContainerFullPath(CmsPath path) {
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    IResource initial = null;
    try {
      initial = mgr.forceGetResource(path);
    } catch (Throwable e) {
      // exception means resource doesnt exist
    }
    
    if (initial != null) {
      if (!(initial instanceof IFolder)) {
        initial = initial.getParent();
      }
      containerGroup.setSelectedContainer(initial);
    }
    validateControls();
  }

  /**
   * Gives focus to the resource name field and selects its contents
   */
  public void setFocus() {
    // select the whole resource name.
    resourceNameField.setSelection(0, resourceNameField.getText().length());
    resourceNameField.setFocus();
  }

  /**
   * Sets the value of this page's resource name.
   * 
   * @param value
   *            new value
   */
  public void setResource(String value) {
    resourceNameField.setText(value);
    validateControls();
  }

  /**
   * Set the only file extension allowed for the resource name field. <br>
   * <br>
   * If a resource extension is specified, then it will always be appended
   * with a '.' to the text from the resource name field for validation when
   * the following conditions are met: <br>
   * <br>
   * (1) Resource extension length is greater than 0 <br>
   * (2) Resource name field text length is greater than 0 <br>
   * (3) Resource name field text does not already end with a '.' and the
   * resource extension specified (case sensitive) <br>
   * <br>
   * The resource extension will not be reflected in the actual resource name
   * field until the resource name field loses focus.
   * 
   * @param value
   *            The resource extension without the '.' prefix (e.g. 'java',
   *            'xml')
   * @since 3.3
   */
  public void setResourceExtension(String value) {
    resourceExtension = value;
    validateControls();
  }

  /**
   * Returns a <code>boolean</code> indicating whether a container name
   * represents a valid container resource. An error message
   * is stored for future reference if the name does not represent a valid
   * container.
   * 
  
   * @return <code>boolean</code> indicating validity of the container name */
  protected boolean validateContainer() {
    CmsPath path = containerGroup.getDestinationFolderPath();
    if (path == null) {
      problemType = PROBLEM_CONTAINER_EMPTY;
      problemMessage = "No folder specified.";
      return false;
    }

    // Selected Resource does not exist
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    try {
      IResource resource = mgr.forceGetResource(path);
      if (resource == null) {
        problemType = PROBLEM_PROJECT_DOES_NOT_EXIST;
        problemMessage = "The specified resource does not exist.";
        return false;
      }

      // path is invalid if resource is not a folder
      if(!(resource instanceof IFolder)) {
        problemType = PROBLEM_PROJECT_DOES_NOT_EXIST;
        problemMessage = "Selected resource is not a folder:" + path.toDisplayString();
        return false;
      }
      
      // use custom handler to validate the parent folder
      if(parentFolderValidator != null) {
        String msg = parentFolderValidator.validateParentFolder(resource);
        if(msg != null && !msg.isEmpty()) {
          problemType = PROBLEM_CUSTOM_VALIDATOR_FAILED;
          problemMessage = msg;
          return false;
        }
      }
      
    } catch (Throwable e) {
      problemType = PROBLEM_PROJECT_DOES_NOT_EXIST;
      problemMessage = "The specified resource does not exist.";
      return false;      
    }
    
    return true;
  }

  /**
   * Validates the values for each of the group's controls. If an invalid
   * value is found then a descriptive error message is stored for later
   * reference. Returns a boolean indicating the validity of all of the
   * controls in the group.
   * @return boolean
   */
  protected boolean validateControls() {
    // don't attempt to validate controls until they have been created
    if (containerGroup == null) {
      return false;
    }
    problemType = PROBLEM_NONE;
    problemMessage = "";//$NON-NLS-1$

    if (!validateContainer() || !validateResourceName()) {
      return false;
    }

    CmsPath path = containerGroup.getDestinationFolderPath().append(getResourceName());
    return validateFullResourcePath(path);
  }

  /**
   * Returns a <code>boolean</code> indicating whether the specified
   * resource path represents a valid new resource in the workbench. An error
   * message is stored for future reference if the path does not represent a
   * valid new resource path.
   * 
   * @param resourcePath
   *            the path to validate
  
   * @return <code>boolean</code> indicating validity of the resource path */
  protected boolean validateFullResourcePath(CmsPath resourcePath) {
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    try {
      IResource resource = mgr.forceGetResource(resourcePath);

      if (!allowExistingResources && resource != null) {
        problemType = PROBLEM_RESOURCE_EXIST;
        problemMessage = resourcePath.toDisplayString() + " already exists.";
        return false;
      }
    } catch(Throwable e) {
      // exception means resource doesn't exist, so ignore
    }
    return true;
  }

  /**
   * Returns a <code>boolean</code> indicating whether the resource name
   * rep- resents a valid resource name in the workbench. An error message is
   * stored for future reference if the name does not represent a valid
   * resource name.
   * 
  
   * @return <code>boolean</code> indicating validity of the resource name */
  protected boolean validateResourceName() {
    String resourceName = getResourceName();

    if (resourceName.length() == 0) {
      problemType = PROBLEM_RESOURCE_EMPTY;
      problemMessage = "The name is empty.";
      return false;
    }

    // TODO: later we can validate name for invalid characters if needed
//    if (!Path.ROOT.isValidPath(resourceName)) {
//      problemType = PROBLEM_NAME_INVALID;
//      problemMessage = NLS.bind(
//          IDEWorkbenchMessages.ResourceGroup_invalidFilename,
//          resourceName);
//      return false;
//    }
    return true;
  }

  /**
   * Returns the flag indicating whether existing resources are permitted.
   * 
  
  
   * @since 3.4
   * @return The allow existing resources flag. * @see ResourceAndContainerGroup#setAllowExistingResources(boolean) */
  public boolean getAllowExistingResources() {
    return allowExistingResources;
  }

  /**
  
   * @return the containerGroup */
  public ContainerSelectionGroup getContainerGroup() {
    return containerGroup;
  }

}
