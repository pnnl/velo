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
package gov.pnnl.cat.ui.rcp.actions.resourceActions;

import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IPluginContribution;

/**
 */
public class ResourceActionDescriptor implements IPluginContribution, Comparable<ResourceActionDescriptor> {
  public final static String EXTENSION_POINT_ID = "resourceAction"; //$NON-NLS-1$
  private final static String CHILD_ACTION_TYPE = "actionType"; //$NON-NLS-1$

  private final static String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$
  private final static String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
  private final static String ATTRIBUTE_PRIORITY = "priority"; //$NON-NLS-1$
  private final static String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

  private IConfigurationElement configElement;
  private IResourceActionBehavior resourceActionFactory;

  /**
   * Constructor for ResourceActionDescriptor.
   * @param element IConfigurationElement
   */
  public ResourceActionDescriptor(IConfigurationElement element) {
    configElement = element;
  }

  /**
   * Method getLocalId.
   * @return String
   * @see org.eclipse.ui.IPluginContribution#getLocalId()
   */
  public String getLocalId() {
    return configElement.getAttribute(ATTRIBUTE_ID);
  }

  /**
   * Method getPluginId.
   * @return String
   * @see org.eclipse.ui.IPluginContribution#getPluginId()
   */
  public String getPluginId() {
    return configElement.getNamespaceIdentifier();
  }

  /**
   * Method getActionTypes.
   * @return String[]
   */
  public String[] getActionTypes() {
    Set<String> actionTypes = new HashSet<String>();
    IConfigurationElement[] children = configElement.getChildren(CHILD_ACTION_TYPE);

    for (IConfigurationElement child : children) {
      actionTypes.add(child.getAttribute(ATTRIBUTE_TYPE));
    }

    return actionTypes.toArray(new String[actionTypes.size()]);
  }

  /**
   * Returns the action's priority relative to the other actions of the same type.
  
   * @return  the priority or <code>Integer.MAX_VALUE</code> if not defined in
   *      the plugins.xml file */
  public int getPriorityPosition() {
    int position = Integer.MAX_VALUE / 2;
    String str = configElement.getAttribute(ATTRIBUTE_PRIORITY);
    if (str != null)
      try {
        position= Integer.parseInt(str);
    } catch (NumberFormatException e) {
      String errMsg = "Invalid Input.  An extension point contributor provided an invalid value for the priority attribute.";
      ToolErrorHandler.handleError(errMsg, e, true);
      // position is Integer.MAX_VALUE;
    }
    return position;
  }

  /**
   * Method getResourceActionBehavior.
   * @return IResourceActionBehavior
   * @throws CoreException
   */
  public IResourceActionBehavior getResourceActionBehavior() throws CoreException {
    if (resourceActionFactory == null) {
      resourceActionFactory = (IResourceActionBehavior) configElement.createExecutableExtension(ATTRIBUTE_CLASS);
    }
    return resourceActionFactory;
  }

  /**
   * Method compareTo.
   * @param descriptor ResourceActionDescriptor
   * @return int
   */
  public int compareTo(ResourceActionDescriptor descriptor) {
    int myPos = getPriorityPosition();
    int objsPos= descriptor.getPriorityPosition();

    if (myPos == Integer.MAX_VALUE && objsPos == Integer.MAX_VALUE || myPos == objsPos) {
      return getLocalId().compareTo(descriptor.getLocalId());
    }

    return myPos - objsPos;
  }

}
