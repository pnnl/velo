package gov.pnnl.velo.uircp;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.VeloGlobalProperties;
import gov.pnnl.cat.ui.CatAbstractUIPlugin;

import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class VeloUIPlugin extends CatAbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "gov.pnnl.velo.ui"; //$NON-NLS-1$

	// The shared instance
	private static VeloUIPlugin plugin;

	// Wiki properties
	private String wikiUrl = null;

	/**
	 * The constructor
	 */
	public VeloUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
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
	public static VeloUIPlugin getDefault() {
		return plugin;
	}

	public String getWikiUrl() {
		if (wikiUrl == null) {

			// Initialize wiki url
			try {
				VeloGlobalProperties props = ResourcesPlugin.getVeloGlobalProperties();
				wikiUrl = props.getProperties().getProperty("wiki.url");

			} catch (Throwable e) {
				logger.error("Failed to load wiki properties.", e);
			}
		}

		return wikiUrl;
	}

}
