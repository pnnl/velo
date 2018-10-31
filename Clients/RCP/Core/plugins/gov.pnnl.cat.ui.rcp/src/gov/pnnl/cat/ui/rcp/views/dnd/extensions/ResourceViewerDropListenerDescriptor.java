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
package gov.pnnl.cat.ui.rcp.views.dnd.extensions;

import gov.pnnl.cat.logging.CatLogger;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;

/**
 */
public class ResourceViewerDropListenerDescriptor {
  private final static String EXTENSION_POINT_ID = "gov.pnnl.cat.ui.rcp.resourceViewerDropListener";
  private final static String ATTRIBUTE_CLASS = "class";
  private final static String ATTRIBUTE_ID = "id";
  private final static String ELEMENT_DROP_LISTENER = "dropListener";
  private final static String ELEMENT_TRANSFER = "transfer";

  // all of the descriptors defined in the plugin.xml for all plugins
  private static Collection<ResourceViewerDropListenerDescriptor> descriptors;
  private static Collection<ResourceViewerDropListenerDescriptor> listeners;
  private static Collection<ResourceViewerDropListenerDescriptor> transfers;

  private IConfigurationElement configElement;
  private static Logger logger = CatLogger.getLogger(ResourceViewerDropListenerDescriptor.class);

  /**
   * Constructor for ResourceViewerDropListenerDescriptor.
   * @param element IConfigurationElement
   */
  public ResourceViewerDropListenerDescriptor(IConfigurationElement element) {
    this.configElement = element;
  }

  /**
   * Method getId.
   * @return String
   */
  public String getId() {
    return this.configElement.getAttribute(ATTRIBUTE_ID);
  }

  /**
   * Method getName.
   * @return String
   */
  public String getName() {
    return this.configElement.getName();
  }

  /**
   * Method createListener.
   * @return DropTargetListener
   * @throws CoreException
   */
  public DropTargetListener createListener() throws CoreException {
    return (DropTargetListener) configElement.createExecutableExtension(ATTRIBUTE_CLASS);
  }

  /**
   * Method createTransfer.
   * @return Transfer
   * @throws CoreException
   */
  public Transfer createTransfer() throws CoreException {
    return (Transfer) configElement.createExecutableExtension(ATTRIBUTE_CLASS);
  }

  /**
   * Method getListeners.
   * @return Collection<ResourceViewerDropListenerDescriptor>
   */
  public static Collection<ResourceViewerDropListenerDescriptor> getListeners() {
    if (listeners == null) {
      listeners = filterDescriptors(ELEMENT_DROP_LISTENER);
    }

    return listeners;
  }

  /**
   * Method getTransfers.
   * @return Collection<ResourceViewerDropListenerDescriptor>
   */
  public static Collection<ResourceViewerDropListenerDescriptor> getTransfers() {
    if (transfers == null) {
      transfers = filterDescriptors(ELEMENT_TRANSFER);
    }

    return transfers;    
  }

  /**
   * Method filterDescriptors.
   * @param name String
   * @return Collection<ResourceViewerDropListenerDescriptor>
   */
  private static Collection<ResourceViewerDropListenerDescriptor> filterDescriptors(String name) {
    Collection<ResourceViewerDropListenerDescriptor> descriptors = getResourceViewerDropListenerDescriptors();
    Collection<ResourceViewerDropListenerDescriptor> matches = new ArrayList<ResourceViewerDropListenerDescriptor>();

    for (ResourceViewerDropListenerDescriptor descriptor : descriptors) {
      if (descriptor.getName().equals(name)) {
        matches.add(descriptor);
      }
    }

    return matches;
  }

  /**
   * Method getResourceViewerDropListenerDescriptors.
   * @return Collection<ResourceViewerDropListenerDescriptor>
   */
  public static Collection<ResourceViewerDropListenerDescriptor> getResourceViewerDropListenerDescriptors() {
    if (descriptors == null) {
      descriptors = new ArrayList<ResourceViewerDropListenerDescriptor>();
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IConfigurationElement[] elementExtensions = registry.getConfigurationElementsFor(EXTENSION_POINT_ID);

      for (IConfigurationElement element : elementExtensions) {
        descriptors.add(new ResourceViewerDropListenerDescriptor(element));
      }
    }

    return descriptors;
  }
}
