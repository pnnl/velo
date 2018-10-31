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
package gov.pnnl.velo;

import gov.pnnl.cat.ui.CatAbstractUIPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * TODO: this needs to be merged with cat rcp plugin when we refactor
 * @version $Revision: 1.0 $
 */
public class VeloPlugin extends CatAbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "gov.pnnl.velo"; //$NON-NLS-1$

  // The shared instance
  private static VeloPlugin plugin;
  
  // Image constants, for lack of a better place
  public static String IMAGE_CALENDAR = "text_calendar.png";

  /**
   * The constructor
   */
  public VeloPlugin() {
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
   * Returns the shared instance
   *

   * @return the shared instance */
  public static VeloPlugin getDefault() {
    return plugin;
  }


}
