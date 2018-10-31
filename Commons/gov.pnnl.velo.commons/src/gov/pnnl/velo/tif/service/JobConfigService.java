package gov.pnnl.velo.tif.service;

import gov.pnnl.velo.tif.model.JobConfig;

import java.io.File;
import java.util.Map;

/**
 * Service for interacting with JobConfig objects.  JobConfigs can come from one of 3 places:
 * 1) saved with last run for a given context path as file name .savedConfig.xml
 * 2) saved as a named config (compound key is machineId x codeId) in the .namedConfigs.xml file
 * 3) Loaded from registry defaults
 * 
 */
public interface JobConfigService {

  public static final String FILE_NAME_NAMED_CONFIGS = ".namedConfigs.xml";
  public static final String FILE_NAME_SAVED_CONFIG = ".savedConfig.xml";
  public static final String CONFIG_NAME_REGISTRY_DEFAULTS = "Registry Defaults";
  public static final String CONFIG_NAME_SAVED = "Last Saved Job Config";

  /**
   * Method to initialize the registry by loading entries from appropriate source.
   */
  public void init();
  
  /**
   * If the user has saved named JobConfig presets for the given machine and
   * code
   * @param machineId
   * @param codeId
   * @return
   */
  public Map<String, JobConfig> getNamedConfigs(String machineId, String codeId);

  /**
   * Create a JobConfig based on all default values from the registry
   * @param machineId
   * @param codeId
   * @return
   */
  public JobConfig getRegistryDefault(String machineId, String codeId, String version);

  /**
   * Assuming a JobConfig has been persisted to a file, load that file
   * into a JobConfig object
   * @param jobConfigFile
   * @return JobConfig
   */
  public JobConfig loadConfigFromFile(File jobConfigFile);

  /**
   * Serialize the JobConfig to given file in xml format.
   * @param config
   * @param savedConfigFile
   */
  public void writeConfigToFile(JobConfig config, File savedConfigFile);
  
  /**
   * Look up the named JobConfig for the given machine and code
   * @param configName
   * @param machineId
   * @param codeID
   * @return
   */
  public JobConfig getNamedConfig(String configName, String machineId, String codeID);
  
  /**
   * Delete named config from the named configs file.
   * @param configName
   * @param machineId
   * @param codeId
   */
  public void deleteNamedConfig(String configName, String machineId, String codeId);

  /**
   * Add a named config to the named configs file.
   * @param configName
   * @param machineId
   * @param codeId
   * @param jobConfig
   */
  public void addNamedConfig(JobConfig jobConfig);

}
