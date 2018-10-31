package gov.pnnl.velo.tools.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import gov.pnnl.velo.tools.ToolsPlugin;

public class RegistryConfigPreferenceIntializer extends AbstractPreferenceInitializer {

  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = ToolsPlugin.getDefault().getPreferenceStore();
    File base_dir = new File(System.getProperty("user.home"),"registry_configs");
    store.setDefault(RegistryConfigPreferencePage.BASE_CONFIG_DIR, base_dir.getAbsolutePath());
  }
}
