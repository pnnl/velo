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
package gov.pnnl.cat.ui;

import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @version $Revision: 1.0 $
 */
public class UiPlugin extends CatAbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "gov.pnnl.cat.ui";

  // The shared instance
  private static UiPlugin PLUGIN;

  /**
   * Returns the shared instance
   * 
   * @return the shared instance */
  public static UiPlugin getDefault() {
    return PLUGIN;
  }

  /**
   * The constructor
   */
  public UiPlugin() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    PLUGIN = this;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    PLUGIN = null;
    super.stop(context);

    try {
      // make sure we free up our shared resources
      SWTResourceManager.dispose();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
}
