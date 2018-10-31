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
package gov.pnnl.cat.core.resources;

import java.util.ArrayList;
import java.util.List;

import gov.pnnl.cat.core.internal.resources.events.NotificationManagerJMS;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.util.PluginUtils;
import gov.pnnl.cat.core.util.ProxyConfig;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.util.SpringContainerInitializer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;

/**
 * The main plugin class to be used in the desktop.
 * @version $Revision: 1.0 $
 */
public class ResourcesPlugin extends Plugin {

  public static final String PLUGIN_ID = "gov.pnnl.cat.core.resources";
  protected static Logger logger = CatLogger.getLogger(ResourcesPlugin.class);
  
  //The shared instance.
  private static ResourcesPlugin plugin;
  private static ApplicationContext beanContainer = null;

  /**
   * The constructor.
   */
  public ResourcesPlugin() {
    plugin = this;
  }

	/**
	 * This method is called upon plug-in activation.
   * This method starts up the Spring container.
	 * @param context BundleContext
	 * @throws Exception
	 * @see org.osgi.framework.BundleActivator#start(BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		initializeBeanContainer();
	}

	/**
	 * This method is called when the plug-in is stopped
	 * @param context BundleContext
	 * @throws Exception
	 * @see org.osgi.framework.BundleActivator#stop(BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

  /**
   * Returns the shared instance.
   * @return ResourcesPlugin
   */
  public static ResourcesPlugin getDefault() {
    return plugin;
  }
  
  /**
  
   * @return the plugin's unique id from plugin.xml file */
  public static String getPluginId() {
    return getDefault().getBundle().getSymbolicName();
  }
  
  /**
   * Method getVeloGlobalProperties.
   * @return VeloGlobalProperties
   */
  public static VeloGlobalProperties getVeloGlobalProperties() {
    ApplicationContext beanFactory = getBeanFactory();
    VeloGlobalProperties props = (VeloGlobalProperties)beanFactory.getBean("velo-global-properties");
    return props;
  }
  
  public static Object getBean(String beanName) {
    ApplicationContext beanFactory = getBeanFactory();
    return beanFactory.getBean(beanName);
  }
  
  public static ProxyConfig getProxyConfig() {
    ApplicationContext beanFactory = getBeanFactory();
    ProxyConfig proxyConfig = (ProxyConfig)beanFactory.getBean("proxyConfig");
    return proxyConfig;
  }  

  /**
   * Method getBeanFactory.
   * @return ApplicationContext
   */
  public static synchronized ApplicationContext getBeanFactory() {
    return beanContainer;
  }
  
  private static synchronized void initializeBeanContainer() {

    List<String> configLocations = null; // absolute paths to the files
    
    try{
      // Get all the config file paths from the Extension Point  
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IConfigurationElement[] extensions = registry.getConfigurationElementsFor( ResourcesPlugin.PLUGIN_ID, "springBeanProvider");
      configLocations = new ArrayList<String>();
      List<String> otherPluginLocations = new ArrayList<String>();
      
      logger.debug("loading " + extensions.length + " spring config files");
      String toolsPlugin = "gov.pnnl.velo.tools";
      
      for (int i = 0; i < extensions.length; i++ ) {
        IConfigurationElement element = extensions[i];
        String pluginId = element.getDeclaringExtension().getNamespace();
        String url = "file:" + getFilePath(element, "configLocation");
        
        // Make sure that the core resources plugin and tools plugin are always loaded first
        // TODO: maybe we should assign a priority attribute in the extension point?
        if(pluginId.equals(ResourcesPlugin.PLUGIN_ID)) {
          configLocations.add(0, url);
          
        } else if (pluginId.equalsIgnoreCase(toolsPlugin) && configLocations.size() > 0) {
          configLocations.add(1, url);
          
        } else {
          otherPluginLocations.add(url);
        }
      }
      
      for(String url : otherPluginLocations) {
        configLocations.add(url);
      }
      
      beanContainer = SpringContainerInitializer.loadBeanContainerFromFilesystem(configLocations.toArray(new String[configLocations.size()]));
      
    } catch(Exception e) {
      //if we catch an exception trying to load the extensions, assume that we're not running
      //within an RCP and so try to find the config file by the hard coded path:
      logger.error("Failed to load spring bean extension points.", e);
    }

  }
  
  /**
   * For a given extension point contribution, returns the absolute path to a file attribute
   * @param element
   * @param attr
   * @return
   */
  public static String getFilePath(IConfigurationElement element, String attr) {
    String relativePath = element.getAttribute(attr);
    String absolutePath = relativePath;
  
    String extendingPluginId = element.getDeclaringExtension().getContributor().getName();
    Bundle bundle= Platform.getBundle(extendingPluginId);
    logger.debug("Trying to load file: " + relativePath + " from plugin: " + extendingPluginId);
    absolutePath = PluginUtils.getAbsolutePath(bundle, relativePath);
    return absolutePath;
  }
  
  /**
   * Method getResourceManager.
   * @return IResourceManager
   */
  public static synchronized IResourceManager getResourceManager() {
    return CmsServiceLocator.getResourceManager();
  }
  
  /**
   * @return
   */
  public static IRemoteFileManager getRemoteFileManager() {
    return CmsServiceLocator.getRemoteFileManager();
  }

  /**
   * @return
   */
  public static NotificationManagerJMS getNotificationManager() {
    return CmsServiceLocator.getNotificationManager();
  }

   /**
    * Method getSearchManager.
    * @return ISearchManager
    */
   public static synchronized ISearchManager getSearchManager() {
    return CmsServiceLocator.getSearchManager();
  }
  
  /**
   * Method getSecurityManager.
   * @return ISecurityManager
   */
  public static synchronized ISecurityManager getSecurityManager() {
    return CmsServiceLocator.getSecurityManager();
  }
  
  /**
   * Method getMimetypeManager.
   * @return IMimetypeManager
   */
  public static synchronized IMimetypeManager getMimetypeManager() {
    return CmsServiceLocator.getMimetypeManager();
  }
}
