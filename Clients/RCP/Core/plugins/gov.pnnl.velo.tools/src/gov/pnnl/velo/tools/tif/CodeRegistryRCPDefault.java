package gov.pnnl.velo.tools.tif;

import org.apache.log4j.Logger;

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.tif.service.impl.CodeRegistryDefault;

public class CodeRegistryRCPDefault extends CodeRegistryDefault {
  
  private static final Logger logger = CatLogger.getLogger(CodeRegistryRCPDefault.class);
  protected IResourceManager resourceManager;

  /**
   * @param resourceManager the resourceManager to set
   */
  public void setResourceManager(IResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  } 
  
  @Override
  public void init() {
    // load codes from server - they will override machines from file system  
    addRegistryProvider(new RegistryConfigFileProviderVeloServer());   
    //Finally load config files from user's client machine. Machine running Velo
    
    addRegistryProvider(new RegistryFileProviderLocalRCPPref()); 
    super.init();
  }
  
}
