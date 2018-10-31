package gov.pnnl.velo.tools;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.CatAbstractUIPlugin;

import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ToolsPlugin extends CatAbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "gov.pnnl.velo.tools"; //$NON-NLS-1$
	public static final String BEAN_TOOL_MANAGER = "toolManager";

	// The shared instance
	private static ToolsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ToolsPlugin() {
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
	 * @return the shared instance
	 */
	public static ToolsPlugin getDefault() {
		return plugin;
	}
	
	public ToolManager getToolManager() {
	  return (ToolManager)ResourcesPlugin.getBean(BEAN_TOOL_MANAGER);
	}

}
