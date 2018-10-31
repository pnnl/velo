package gov.pnnl.velo.tools.tif;

import java.io.File;
import java.util.List;

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.velo.tif.service.impl.ScriptRegistryDefault;

public class ScriptRegistryRCPDefault extends ScriptRegistryDefault {
  private IResourceManager resourceManager;

  /**
   * @param resourceManager the resourceManager to set
   */
  public void setResourceManager(IResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  @Override
  public void init() {
    // load scripts from server - they will override machines from file system  
    addRegistryProvider(new RegistryConfigFileProviderVeloServer());   
    //Finally load config files from user's client machine. Machine running Velo
    addRegistryProvider(new RegistryFileProviderLocalRCPPref()); 
    super.init();
  }

}
