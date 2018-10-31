package gov.pnnl.velo.tif.service;

import java.io.File;
import java.util.List;

public interface RegistryConfigFileProvider {
  
  /**
   * Where executables are placed for any registered codes that
   * can run on localhost.
   * @return
   */
  public File getExecutablesDir();
  
  public List<File> getCodeConfigFiles();
  public List<File> getMachineConfigFiles();
  public List<File> getScriptFiles();

}
