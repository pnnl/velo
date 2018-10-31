package gov.pnnl.velo.tools.tif;

import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;
import gov.pnnl.velo.tools.ToolsPlugin;
import gov.pnnl.velo.tools.ui.preferences.RegistryConfigPreferencePage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

public class RegistryFileProviderLocalRCPPref implements RegistryConfigFileProvider {
  
  private File base_dir = new File(System.getProperty("user.home"),"registry_configs");
  IPreferenceStore preferenceStore = ToolsPlugin.getDefault().getPreferenceStore();

  /**
   * Read base directory from RCP preference. If not default to user.home/registry_configs
   */
  private File getBaseDir(){
    String config_dir_str = preferenceStore.getString(RegistryConfigPreferencePage.BASE_CONFIG_DIR);
    File base_dir = null;
    if (config_dir_str !=null){
      base_dir = new File(config_dir_str);
      if(base_dir.isDirectory()){
        this.base_dir = base_dir;
      }
    }
    return this.base_dir;
  }

  @Override
  public File getExecutablesDir() {
    return null;
  }

  @Override
  public List<File> getCodeConfigFiles() {
    //Read before loading as preference could have changed
    return getConfigFiles(new File(getBaseDir(), "codes"));
  }

  @Override
  public List<File> getMachineConfigFiles() {
    //Read before loading as preference could have changed
    return getConfigFiles(new File(getBaseDir(),"machines"));
  }

  @Override
  public List<File> getScriptFiles() {
    //Read before loading as new config directory could have
    // been set using eclipse preference page
    return getConfigFiles(new File(getBaseDir(),"jobScripts"));
  }
  
  private List<File> getConfigFiles(File dir) {
    List<File> configFiles = new ArrayList<File>();
    File[] listFiles = dir.listFiles();  
    if (listFiles != null){
      for (File f:listFiles){
        if(f.isFile()){
          configFiles.add(f);
        }
      }
    }
    return configFiles;
  }

}
