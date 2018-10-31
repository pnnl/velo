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
package gov.pnnl.cat.alerts;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @version $Revision: 1.0 $
 */
public class AlertsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "gov.pnnl.cat.alerts";

	// The shared instance
	private static AlertsPlugin plugin;

  // Image IDs
  public static final String IMG_SUBSCRIPTION = "IMG_SUBSCRIPTION";
  public static final String IMG_SUBSCRIPTION_SEARCH = "IMG_SUBSCRIPTION_SEARCH";

	private AlertService alertsService;

	/**
	 * The constructor
	 */
	public AlertsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

  /**
   * Method initializeImageRegistry.
   * @param registry ImageRegistry
   */
  @Override
  protected void initializeImageRegistry(ImageRegistry registry) {
    super.initializeImageRegistry(registry);
    Bundle bundle = Platform.getBundle(PLUGIN_ID);

    // register a bunch of images that we'll need
    registerImg(registry, bundle, "icons/16x16/note_find.png", IMG_SUBSCRIPTION_SEARCH);
    registerImg(registry, bundle, "icons/16x16/note.png", IMG_SUBSCRIPTION);
  }

  /**
   * Method registerImg.
   * @param registry ImageRegistry
   * @param bundle Bundle
   * @param path String
   * @param imgId String
   */
  private void registerImg(ImageRegistry registry, Bundle bundle, String path, String imgId) {
    registry.put(imgId, ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(path), null)));
  }

	/**
	 * Returns the shared instance
	 *
	
	 * @return the shared instance */
	public static AlertsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Method getAlertsService.
	 * @return AlertService
	 */
	public synchronized AlertService getAlertsService() {
	  if (alertsService == null) {
	    alertsService = new AlertService();
	  }

	  return alertsService;
	}
}
