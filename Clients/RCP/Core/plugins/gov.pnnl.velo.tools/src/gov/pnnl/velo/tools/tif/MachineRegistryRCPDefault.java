package gov.pnnl.velo.tools.tif;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tif.service.impl.MachineRegistryDefault;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class MachineRegistryRCPDefault extends MachineRegistryDefault {
  private static final Logger logger = CatLogger.getLogger(MachineRegistryRCPDefault.class);
  private static final String  registryPrefsFileBaseName = ".registry.prefs";
  
  private IResourceManager resourceManager;

  /**
   * @param resourceManager the resourceManager to set
   */
  public void setResourceManager(IResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.impl.MachineRegistryAdapter#init()
   */
  @Override
  public void init() {
    // load machines from server - they will override machines from file system  
    addRegistryProvider(new RegistryConfigFileProviderVeloServer());    
    //Finally load config files from user's client machine. 
    addRegistryProvider(new RegistryFileProviderLocalRCPPref()); 
    super.init();
  }
  
  private String getRegistryPrefsFilename(String machineId) {
    return "." + machineId + registryPrefsFileBaseName;
  }

  @Override
  public void saveUserMachinePrefs(String machineId, Properties userMachinePrefs) {
    try {
      IResource homeFolder = resourceManager.getHomeFolder();
      String registryPrefsFileName = getRegistryPrefsFilename(machineId);
          
      // Write properties to a file
      File tempFile = new File(FileUtils.getTempDirectory(), registryPrefsFileName);
      userMachinePrefs.store(new FileOutputStream(tempFile), "");
      CmsPath registryPrefsFilePath = homeFolder.getPath().append(registryPrefsFileName);
      resourceManager.createFile(registryPrefsFilePath, tempFile);

    } catch (Throwable e) {
      logger.error("Failed to save registry preferences.", e);
    }
  }

  @Override
  public Properties getUserMachinePrefs(String machineId) {
    Properties properties = new Properties();
    
    try {
      IResource homeFolder = resourceManager.getHomeFolder();
      String registryPrefsFileName = getRegistryPrefsFilename(machineId);
      CmsPath registryPrefsFilePath = homeFolder.getPath().append(registryPrefsFileName);
      File tempFile = resourceManager.getContentPropertyAsFile(registryPrefsFilePath, VeloConstants.PROP_CONTENT);
      
      // load properties
      if(tempFile != null) {
        properties.load(new FileInputStream(tempFile));
      }

    } catch (Throwable e) {
      logger.error("Failed to save registry preferences.", e);
    }
    
    return properties;
  }

}
