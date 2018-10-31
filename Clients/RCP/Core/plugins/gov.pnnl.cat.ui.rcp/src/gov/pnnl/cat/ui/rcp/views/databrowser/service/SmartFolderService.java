package gov.pnnl.cat.ui.rcp.views.databrowser.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import gov.pnnl.cat.core.internal.resources.ResourceService;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.OpenWithAction;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.ISmartFolder;

public class SmartFolderService {
  public static final String EXTENSION_POINT_ID = "gov.pnnl.cat.ui.rcp.smartFolderProvider"; //$NON-NLS-1$
  private static SmartFolderService instance = new SmartFolderService();
  private static Logger logger = CatLogger.getLogger(SmartFolderService.class);
  
  private List<ISmartFolder> smartFolders;
  
  public static SmartFolderService getInstance() {
    return instance;
  }
  
  public SmartFolderService() {
    loadExtensions();
  }

  private void loadExtensions() {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] elementExtensions = registry.getConfigurationElementsFor(EXTENSION_POINT_ID);
    smartFolders = new ArrayList<ISmartFolder>();
    for (int i = 0; i < elementExtensions.length; i++) {
      try {
        ISmartFolder smartFolder = (ISmartFolder) elementExtensions[i].createExecutableExtension("class");
        smartFolders.add(smartFolder);
        
      } catch (CoreException e) {
        logger.error("Failed to load extension.", e);
      }
    }

  }
  
  public List<ISmartFolder> getSmartFolders() {
    return smartFolders;
  }
  
  public ISmartFolder[] getSmartFoldersAsArray() {
    return smartFolders.toArray(new ISmartFolder[smartFolders.size()]);
  }
}
