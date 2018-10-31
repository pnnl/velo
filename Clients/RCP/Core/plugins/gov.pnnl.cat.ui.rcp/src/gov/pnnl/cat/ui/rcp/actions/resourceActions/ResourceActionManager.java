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

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class ResourceActionManager {
  private static ResourceActionManager singleton;
  private static Logger logger = CatLogger.getLogger(ResourceActionManager.class);

  private Map<String, List<ResourceActionDescriptor>> resourceActions;

  /**
   * Method getInstance.
   * @return ResourceActionManager
   */
  public synchronized static ResourceActionManager getInstance() {
    if (singleton == null) {
      singleton = new ResourceActionManager();
    }
    return singleton;
  }

  /**
   * Method executeResourceAction.
   * @param shell Shell
   * @param sourcePaths String[]
   * @param destination IFolder
   * @param operation String
   */
  public void executeResourceAction(Shell shell, String[] sourcePaths, IFolder destination, String operation) {
    logger.debug(operation + " destination: " + destination);
    try {
      loadExtensionPointContributions();
      List<ResourceActionDescriptor> actionList = resourceActions.get(operation);
  
      for (ResourceActionDescriptor action : actionList) {
        boolean actionPerformed = action.getResourceActionBehavior().run(shell, sourcePaths, destination, operation);

        if (actionPerformed) {
          // the current resource action behavior performed the action, so our job is done.
          return;
        }
      }
    } catch (Exception e) {
      logger.error("Could not execute " + operation, e);
      throw new RuntimeException("Could not execute " + operation, e);
    }
  }

  private void loadExtensionPointContributions() {
    if (resourceActions == null) {
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IConfigurationElement[] elements = registry.getConfigurationElementsFor(CatRcpPlugin.PLUGIN_ID, ResourceActionDescriptor.EXTENSION_POINT_ID);
      resourceActions = createResourceActionDescriptors(elements);
    }
  }

  /**
   * Creates descriptors to represent all contributed resource actions.
   * @param elements IConfigurationElement[]
   * @return Map<String,List<ResourceActionDescriptor>>
   */
  private Map<String, List<ResourceActionDescriptor>> createResourceActionDescriptors(IConfigurationElement[] elements) {
    Map<String, List<ResourceActionDescriptor>> result = new HashMap<String, List<ResourceActionDescriptor>>();

    for (IConfigurationElement element : elements) {
      if (ResourceActionDescriptor.EXTENSION_POINT_ID.equals(element.getName())) {
        ResourceActionDescriptor desc = new ResourceActionDescriptor(element);
        String[] actionTypes = desc.getActionTypes();

        for (String actionType : actionTypes) {
          List<ResourceActionDescriptor> list;
  
          if (result.containsKey(actionType)) {
            list = result.get(actionType);
          } else {
            list = new ArrayList<ResourceActionDescriptor>();
            result.put(actionType, list);
          }
  
          list.add(desc);
        }
      }
    }

    // sort the lists
    for (List<ResourceActionDescriptor> list : result.values()) {
      Collections.sort(list);
    }

    return result;
  }
}
