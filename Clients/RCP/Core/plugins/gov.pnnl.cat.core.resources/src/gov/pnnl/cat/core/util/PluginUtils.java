package gov.pnnl.cat.core.util;

import gov.pnnl.cat.logging.CatLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

public class PluginUtils {
  private static Logger logger = CatLogger.getLogger(PluginUtils.class);
  
  /**
   * Finds a file inside a plugin from an Eclipse plugin url
   * @param pluginUrl - url of the form:
   * platform:/plugin/org.eclipse.datatools.connectivity.sqm.core.ui/icons/server_explorer.gif
   * @return
   */
  public static File getPluginFile(String pluginUrl) {
    try {
      URL url = new URL(pluginUrl);
      return new File(FileLocator.toFileURL(url).getPath());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * This will search through root plugin and fragments to find the location
   * of the file
   * If for some reason more than one file is found (i.e., in both the plugin
   * and the fragment), only the first will be used
   * @param bundle - the plugin
   * @param relativePath - path from the root of the plugin
   * @return
   */
  public static File getPluginFile(Bundle bundle, String relativePath) {
    
    File resourceFile = null;
    
    try {
      
      Enumeration<URL> fileUrls = bundle.findEntries("/", relativePath, false);
      if(fileUrls != null && fileUrls.hasMoreElements()) {
        URL url = fileUrls.nextElement();
        resourceFile = new File(FileLocator.toFileURL(url).getPath());
      }
  
    } catch (Throwable e) {
      logger.error("Failed to convert file url", e);
    }

    return resourceFile;

  }
  
  /**
   * Gets the absolute filesystem path to a file inside a deployed plugin.
   * @param bundle - the plugin
   * @param relativePath - path from the root of the plugin
   * @return
   */
  public static String getAbsolutePath(Bundle bundle, String relativePath) {
    File file = getPluginFile(bundle, relativePath);
    if(file != null) {
      return file.getAbsolutePath();
    }
    return null;
  }
}
