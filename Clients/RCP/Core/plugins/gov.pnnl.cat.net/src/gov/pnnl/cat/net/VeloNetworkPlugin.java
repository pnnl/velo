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
package gov.pnnl.cat.net;

import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @version $Revision: 1.0 $
 */
public class VeloNetworkPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "gov.pnnl.cat.cifs";

	// The shared instance
	private static VeloNetworkPlugin plugin;
	private static IVeloFileSystemManager filesystemManager;
	
	/**
	 * The constructor
	 */
	public VeloNetworkPlugin() {
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
	public static VeloNetworkPlugin getDefault() {
		return plugin;
	}
	
	public static IVeloFileSystemManager getVeloFileSystemManager() {
	  // TODO: this should be looked up via extension point instead
	  if(filesystemManager == null) {
	    String classname = "gov.pnnl.cat.net.VeloFileSystemManagerDefault";

	    // See if a fragment has provided a different class to use
	    try {
	      File classnameFile = getClassnameFile();
	      if(classnameFile != null) {
	        classname = FileUtils.readFileAsString(classnameFile);
	      }
	      
	      filesystemManager = (IVeloFileSystemManager)Class.forName(classname).newInstance();
	      
	    } catch (Throwable e) {
	      filesystemManager = new VeloFileSystemManagerDefault();
	      ToolErrorHandler.handleError("Failed to initialize VeloFileSystemManager.", e, true);
	    }

	  }
	  
	  return filesystemManager;
	}
	
  private static File getClassnameFile() throws Exception {
    // This will search through root plugin and fragment to find the location of the classname.txt file
    File classnameFile = null;

    Enumeration<URL> fileUrls = plugin.getBundle().findEntries("/", "classname.txt", false);

    if(fileUrls != null && fileUrls.hasMoreElements()) {
      classnameFile = new File(FileLocator.toFileURL(fileUrls.nextElement()).getPath());
    }

    return classnameFile;
  }
	
  public static File getFragmentFolder() throws Exception {
    File classnameFile = getClassnameFile();
    File fragmentFolder = null;
    if(classnameFile != null) {
      fragmentFolder = classnameFile.getParentFile();
    }
    return fragmentFolder;
  }

}
