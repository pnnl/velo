package gov.pnnl.velo.tools.tif;

import gov.pnnl.cat.core.util.PluginUtils;
import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;
import gov.pnnl.velo.tif.service.ScriptRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.InitializingBean;

public class RegistryConfigFileProviderRCP implements RegistryConfigFileProvider, InitializingBean {

  private static Logger logger = Logger.getLogger(RegistryConfigFileProviderRCP.class);
  
  protected String pluginId;
  protected String scriptsDirPath;
  protected String codesDirPath;
  protected String machinesDirPath;
  protected String exeDirPath;
  
  protected CodeRegistry codeRegistry;
  protected MachineRegistry machineRegistry;
  protected ScriptRegistry scriptRegistry;


  public void setCodeRegistry(CodeRegistry codeRegistry) {
    this.codeRegistry = codeRegistry;
  }

  public void setMachineRegistry(MachineRegistry machineRegistry) {
    this.machineRegistry = machineRegistry;
  }

  public void setScriptRegistry(ScriptRegistry scriptRegistry) {
    this.scriptRegistry = scriptRegistry;
  }

  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  public void setScriptsDirPath(String scriptsDirPath) {
    this.scriptsDirPath = scriptsDirPath;
  }

  public void setCodesDirPath(String codesDirPath) {
    this.codesDirPath = codesDirPath;
  }

  public void setMachinesDirPath(String machinesDirPath) {
    this.machinesDirPath = machinesDirPath;
  }

  public void setExeDirPath(String exeDirPath) {
    this.exeDirPath = exeDirPath;
  }
  
  protected File locateFile(String relativePath) {
    File file = null;
    if(relativePath != null) {
      Bundle bundle = Platform.getBundle(pluginId);
      file = PluginUtils.getPluginFile(bundle, relativePath);
    }
    return file;
  }

  @Override
  public File getExecutablesDir() {
    return locateFile(exeDirPath);
  }

  @Override
  public List<File> getCodeConfigFiles() {
    return getConfigFiles(codesDirPath);
  }

  @Override
  public List<File> getMachineConfigFiles() {
    return getConfigFiles(machinesDirPath);
  }

  @Override
  public List<File> getScriptFiles() {
    return getConfigFiles(scriptsDirPath);
  }

  protected List<File> getConfigFiles(String relativePath) {
    List<File> configFiles = new ArrayList<File>();

    File folder = locateFile(relativePath);
    if(folder != null && folder.exists() && folder.isDirectory()) {
      for(File file: folder.listFiles()) {
        if(file.isFile()) {
          configFiles.add(file);
        }
      }
    } 

    return configFiles;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    codeRegistry.addRegistryProvider(this);
    machineRegistry.addRegistryProvider(this);
    scriptRegistry.addRegistryProvider(this);
  }


}
