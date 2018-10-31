package gov.pnnl.velo.ui.rcp;

import gov.pnnl.cat.ui.CatAbstractUIPlugin;
import gov.pnnl.velo.uircp.VeloUIPlugin;

import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class VeloRCPPlugin extends CatAbstractUIPlugin {
      
	// The plug-in ID
	public static final String PLUGIN_ID = "gov.pnnl.velo.ui.rcp"; //$NON-NLS-1$

	// The shared instance
	private static VeloRCPPlugin plugin;
	
	private String buildVersion;
	
	/**
	 * The constructor
	 */
	public VeloRCPPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
    try {
      // Initialize version
      // Get the version for the velo UI plugin - then it will update
      // even if the rcp plugin doesn't get updated
      buildVersion =  VeloUIPlugin.getDefault().getBundle().getVersion().toString();
     // AkunaToolsPlugin.getDefault().setBuildVersion(buildVersion);
    } catch (Throwable e) {
      logger.error("Failed to load wiki config file.", e);
    }
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
	 * @return the shared instance
	 */
	public static VeloRCPPlugin getDefault() {
		return plugin;
	}


  public String getBuildVersion() {
    return buildVersion;
  }
	
	
}
