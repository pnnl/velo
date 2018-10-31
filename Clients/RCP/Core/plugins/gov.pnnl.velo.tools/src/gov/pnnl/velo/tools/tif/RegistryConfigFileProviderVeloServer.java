package gov.pnnl.velo.tools.tif;

import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RegistryConfigFileProviderVeloServer implements RegistryConfigFileProvider {
  
  private CmsPath registryPath = new CmsPath("/Velo/Registry");
  private IResourceManager resourceManager = CmsServiceLocator.getResourceManager();


  @Override
  public File getExecutablesDir() {
    return null;
  }

  @Override
  public List<File> getCodeConfigFiles() {
    return getConfigFiles("Codes");
  }

  @Override
  public List<File> getMachineConfigFiles() {
    return getConfigFiles("Machines");
  }

  @Override
  public List<File> getScriptFiles() {
    return getConfigFiles("JobScripts");
  }
  
  private List<File> getConfigFiles(String relativePath) {
    List<File> configFiles = new ArrayList<File>();
    CmsPath path = registryPath.append(relativePath);

    List<IResource> children = resourceManager.getChildren(path);
    for(IResource child : children) {
      File file = resourceManager.getContentPropertyAsFile(child.getPath(), VeloConstants.PROP_CONTENT);
      if(file != null && file.exists()) {
        configFiles.add(file);
      }
    }

    return configFiles;
  }

}
