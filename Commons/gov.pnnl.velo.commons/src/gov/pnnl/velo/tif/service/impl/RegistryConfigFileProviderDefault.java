package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;
import gov.pnnl.velo.tif.service.ScriptRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

public class RegistryConfigFileProviderDefault implements RegistryConfigFileProvider, InitializingBean {

  private static Logger logger = Logger.getLogger(RegistryConfigFileProviderDefault.class);

  protected Resource scriptsDir;
  protected Resource codesDir;
  protected Resource machinesDir;
  protected Resource exeDir;
  
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
  
  public void setScriptsDir(Resource scriptsDir) {
    this.scriptsDir = scriptsDir;
  }

  public void setCodesDir(Resource codesDir) {
    this.codesDir = codesDir;
  }

  public void setMachinesDir(Resource machinesDir) {
    this.machinesDir = machinesDir;
  }
  
  public void setExeDir(Resource exeDir) {
    this.exeDir = exeDir;
  }

  
  @Override
  public File getExecutablesDir() {
    // TODO append a sub-folder per the current OS
    File exeFolder = null;
    try {
      if(exeDir != null) {
        exeFolder = exeDir.getFile();
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }

    return exeFolder;
  }

  @Override
  public List<File> getCodeConfigFiles() {
    return getConfigFiles(codesDir);
  }

  @Override
  public List<File> getMachineConfigFiles() {
    return getConfigFiles(machinesDir);
  }

  @Override
  public List<File> getScriptFiles() {
    logger.debug("getting script files from " +scriptsDir);
    return getConfigFiles(scriptsDir);
  }
  
  protected List<File> getConfigFiles(Resource resource) {
    List<File> configFiles = new ArrayList<File>();
    try {
      if(resource != null && resource.getFile().exists() && resource.getFile().isDirectory()) {
        for(File file: resource.getFile().listFiles()) {
          logger.debug("Adding resource file " + file.getAbsolutePath() + "/" + file.getName());
          // only add children that are files, not folders
          if(file.isFile()) {
            configFiles.add(file);
          }
        }
      } 
    } catch (IOException e) {
      throw new RuntimeException(e);
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
