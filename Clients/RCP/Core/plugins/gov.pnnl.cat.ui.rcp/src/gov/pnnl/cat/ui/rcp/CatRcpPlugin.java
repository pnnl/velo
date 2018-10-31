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
package gov.pnnl.cat.ui.rcp;

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPreferencePage;
import gov.pnnl.cat.ui.CatAbstractUIPlugin;
import gov.pnnl.cat.ui.rcp.util.DefaultDocumentLibraryPerspectiveProvider;
import gov.pnnl.cat.ui.rcp.util.DefaultTreeRootProvider;
import gov.pnnl.cat.ui.rcp.util.DocumentLibraryPerspectiveProvider;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.util.TreeRootProvider;
import gov.pnnl.velo.core.util.EmailConstants;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @version $Revision: 1.0 $
 */
public class CatRcpPlugin extends CatAbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "gov.pnnl.cat.ui.rcp";

  // The shared instance
  private static CatRcpPlugin PLUGIN;

  private CatSearchPlugin searchPlugin;


  // Extension points
  public static final String EXT_POINT_EMAIL_ADDRESS = "gov.pnnl.cat.ui.rcp.supportEmailAddressProvider";
  public static final String ATT_TO_EMAIL_ADDRESS = "toAddress";
  public static final String ATT_FROM_EMAIL_DOMAIN = "fromEmailDomain";

  public static final String EXT_POINT_TREE_ROOT = "gov.pnnl.cat.ui.rcp.treeRootProvider";
  public static final String ATT_TREE_ROOT_PROVIDER_CLASS = "class";

  public static final String EXT_POINT_DOC_LIB_PERSPECTIVE = "gov.pnnl.cat.ui.rcp.documentLibraryPerspectiveProvider";
  public static final String ATT_PERSPECTIVE_PROVIDER_CLASS = "class";

  /**
   * The constructor
   */
  public CatRcpPlugin() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    PLUGIN = this;
    searchPlugin = new CatSearchPlugin(PLUGIN);
    initializeRCPConfig();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    PLUGIN = null;
    super.stop(context);
  }

  /**
   * Method getSearchPlugin.
   * @return CatSearchPlugin
   */
  public CatSearchPlugin getSearchPlugin() {
    return searchPlugin;
  }

  /**
   * Returns the shared instance
   * 

   * @return the shared instance */
  public static CatRcpPlugin getDefault() {
    return PLUGIN;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
   */
  @Override
  protected void initializeDefaultPreferences(IPreferenceStore store) {
    SearchPreferencePage.initDefaults(store);
  }

  private void initializeSupportEmail() {
    try {
      // look up all the extensions for the extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXT_POINT_EMAIL_ADDRESS);

      for (IConfigurationElement element : elements) {
        final String toEmail = element.getAttribute(ATT_TO_EMAIL_ADDRESS);
        final String fromDomain = element.getAttribute(ATT_FROM_EMAIL_DOMAIN);
        if (toEmail != null && fromDomain != null) {
		      EmailConstants.setToEmail(toEmail);
		      EmailConstants.setFromEmailDomain(fromDomain);
              break;
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load email extension point.", e);
    }

  }

  private void initializeVeloTreeRoot() {

    try {
      // look up all the extensions for the extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXT_POINT_TREE_ROOT);
      TreeRootProvider provider = null;

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATT_TREE_ROOT_PROVIDER_CLASS);
        if(obj instanceof TreeRootProvider) {
          provider = ((TreeRootProvider)obj);
        }
      }

      if(provider == null) {
        provider = new DefaultTreeRootProvider();
      }

      RCPUtil.setTreeRootProvider(provider);

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load velo tree root extension point.", e);
    }

  }

  private void initializeDefaultExplorerPerspective() {
    try {
      // look up all the extensions for the extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXT_POINT_DOC_LIB_PERSPECTIVE);
      DocumentLibraryPerspectiveProvider provider = null;

          for (IConfigurationElement configurationElement : elements) {
            Object obj = configurationElement.createExecutableExtension(ATT_PERSPECTIVE_PROVIDER_CLASS);
            if(obj instanceof DocumentLibraryPerspectiveProvider) {
              provider = ((DocumentLibraryPerspectiveProvider)obj);
            }
          }

      if(provider == null) {
        provider = new DefaultDocumentLibraryPerspectiveProvider();
      }

      RCPUtil.setDocLibPerspectiveProvider(provider);

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load extension point.", e);
    }
  }

  private void initializeRCPConfig() {
    // 1)  Email address used for error reporting
    initializeSupportEmail();

    // 2)  Velo tree root
    initializeVeloTreeRoot();

    // 3) Default perspective ID for opening documents in the explorer (used by search)
    initializeDefaultExplorerPerspective();
    
  }
  
}
