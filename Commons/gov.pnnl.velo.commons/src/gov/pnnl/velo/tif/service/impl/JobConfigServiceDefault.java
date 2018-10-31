package gov.pnnl.velo.tif.service.impl;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.model.Parameter;
import gov.pnnl.velo.tif.model.Queue;
import gov.pnnl.velo.tif.service.JobConfigService;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.FileUtils;
import gov.pnnl.velo.util.VeloTifConstants;

/**
 * A class to look up and save runtime config named presets.
 */
public class JobConfigServiceDefault implements JobConfigService {

  public static final String FILE_NAME_NAMED_CONFIGS = "namedConfigs.xml";
  public static final String FILE_NAME_SAVED_CONFIG = ".savedConfig.xml";
  public static final String CONFIG_NAME_REGISTRY_DEFAULTS = "Registry Defaults";
  public static final String CONFIG_NAME_SAVED = "Last Saved Job Config";
  public static final String FOLDER_NAME_SAVED_CONFIGS = "JobConfigCache";

  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(JobConfigServiceDefault.class);

  /** saved named presets */
  private Map<NamedRuntimeConfigKey, Map<String, JobConfig>> namedConfigs = new HashMap<NamedRuntimeConfigKey, Map<String, JobConfig>>();


  public JobConfigServiceDefault() {
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobConfigService#init()
   */
  @Override
  public void init() {
    loadNamedConfigs();    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.core.jobs.registry.JobConfigService#getNamedConfigs(java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, JobConfig> getNamedConfigs(String machineId, String codeId) {
    NamedRuntimeConfigKey key = new NamedRuntimeConfigKey(machineId, codeId);
    Map<String, JobConfig> namedConfigsByMachineAndCode = namedConfigs.get(key);
    if(namedConfigsByMachineAndCode == null) {
      namedConfigsByMachineAndCode = new HashMap<String, JobConfig>();
      namedConfigs.put(key, namedConfigsByMachineAndCode);
    }
    return namedConfigsByMachineAndCode;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.core.jobs.registry.JobConfigService#getRegistryDefault(java.lang.String, java.lang.String, String)
   */
  @Override
  public JobConfig getRegistryDefault(String machineId, String codeId, String version) {
    MachineRegistry mregistry = TifServiceLocator.getMachineRegistry();

    JobConfig config = new JobConfig(CONFIG_NAME_REGISTRY_DEFAULTS);
    config.setMachineId(machineId);
    config.setCode(codeId, version);
    Machine machine = config.getMachine();
    Code code = config.getCode();
    config.setProcessors(machine.getProcsPerNode());
    config.setNodes(1);
    config.setTasksPerNode(1); // we assume only one task by default, so use all the node for that run
    config.setProcessors(machine.getProcsPerNode());
    config.setProcsPerTask(machine.getProcsPerNode());
    config.setCommand(code.getJobLaunching().getCommand());
    
    // pre-calculate the run directory
    String runDir = mregistry.getRemoteUserHomeDirectory(machineId);
    if(runDir != null) {
      config.setRemoteDir(runDir);
    }
    
    // load user machine prefs
    Properties props = mregistry.getUserMachinePrefs(machineId);
    String user = props.getProperty(VeloTifConstants.JOB_USER);
    if(user != null) {
      config.setUserName(user);
    }
    
    String allocation = props.getProperty(VeloTifConstants.JOB_ACCOUNT);
    if(allocation != null) {
      config.setAccount(allocation);
    }
    
    // queues may be null
    String defaultQ = null;
    List<Queue> queues = machine.getScheduler().getQueues(); 
    if(queues != null) {
      for (Queue aqueue : machine.getScheduler().getQueues()) {
        if (aqueue.isDefaultQueue()) {
          defaultQ = aqueue.getName();
          break;
        }
      }
    }

    if (defaultQ != null) {
      config.setQueueName(defaultQ);
    }
 
    
    //===== Custom Parameters
    List<Parameter> jobHandlerParams = code.getJobLaunching().getJobHandlerParameters();
    Map<String, String> configJobHandlerParams = config.getJobHandlerParameters();
    
    for(Parameter param : jobHandlerParams) {
      configJobHandlerParams.put(param.getName(), param.getValue());
    }
    
    return config;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.core.jobs.registry.JobConfigService#loadConfigFromFile(java.io.File)
   */
  @Override
  public JobConfig loadConfigFromFile(File jobConfigFile) {
    JobConfig config = null;
    try {

      if (jobConfigFile.exists()) {
        String xml = FileUtils.readFileAsString(jobConfigFile);   
        config = JobConfig.fromXml(xml);        
      }
            
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }    
    
    return config;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.core.jobs.registry.JobConfigService#writeConfigToFile(gov.pnnl.velo.core.jobs.registry.JobConfig, java.io.File)
   */
  @Override
  public void writeConfigToFile(JobConfig config, File savedConfigFile) {
    try  {
      String xml = JobConfig.toXml(config);
      FileUtils.writeStringToFile(savedConfigFile, xml);
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.core.jobs.registry.JobConfigService#getNamedConfig(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public JobConfig getNamedConfig(String configName, String machineId, String codeID) {
    JobConfig config = null;

    if(configName.equals(JobConfigServiceDefault.CONFIG_NAME_REGISTRY_DEFAULTS)) {
      config = getRegistryDefault(machineId, codeID, null);

    } else {
      Map<String, JobConfig> presets = getNamedConfigs(machineId, codeID);
      config = presets.get(configName);

    }
    return config;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.core.jobs.registry.JobConfigService#deleteNamedConfig(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void deleteNamedConfig(String configName, String machineId, String codeId) {
    Map<String, JobConfig> presets = getNamedConfigs(machineId, codeId);
    presets.remove(configName);
    saveNamedConfigs();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.core.jobs.registry.JobConfigService#addNamedConfig(java.lang.String, java.lang.String, java.lang.String, gov.pnnl.velo.core.jobs.registry.JobConfig)
   */
  @Override
  public void addNamedConfig(JobConfig jobConfig) {
    Map<String, JobConfig> namedConfigsByMachineAndCode = getNamedConfigs(jobConfig.getMachineId(), jobConfig.getCode().getIdAndVersion());
    namedConfigsByMachineAndCode.put(jobConfig.getName(), jobConfig);
    saveNamedConfigs();    
  }
  
  protected File getSavedConfigsFolder() {
    File configDir = new File(TifServiceLocator.getVeloWorkspace().getVeloFolder(), FOLDER_NAME_SAVED_CONFIGS);
    try {
      org.apache.commons.io.FileUtils.forceMkdir(configDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return configDir;
  }

  protected void saveNamedConfigs() {
    try  {
      // save named configs to xml file
      File configDir = getSavedConfigsFolder();
      File namedConfigsFile = new File(configDir, FILE_NAME_NAMED_CONFIGS);
      String xml = toXml(namedConfigs);
      FileUtils.writeStringToFile(namedConfigsFile, xml);
      
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
  protected void loadNamedConfigs() {
    try {
      // load namedConfigs.xml file

      // 1) get the xml file from the registry config dir
      // TODO: get the folder from ServiceLocator.getVeloWorkspace()
      File configDir = getSavedConfigsFolder();
      File namedConfigsFile = new File(configDir, FILE_NAME_NAMED_CONFIGS);

      if (namedConfigsFile.exists()) {
        String xml = FileUtils.readFileAsString(namedConfigsFile);   
        namedConfigs = fromXml(xml);
      }
      
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  public static String toXml(Map<NamedRuntimeConfigKey, Map<String, JobConfig>> namedConfigs) {

    // convert multi-map to a list
    List<JobConfig> jobConfigs = new ArrayList<JobConfig>();
    for(Map<String, JobConfig> map : namedConfigs.values()) {
      for(JobConfig config : map.values()) {
        jobConfigs.add(config);
      }
    }
    return getXStream().toXML(jobConfigs);

  }

  private static XStream getXStream() {
    XStream xstream = JobConfig.getXStream();
    xstream.alias("NamedConfigs", List.class);
    return xstream;
  }

  private static Map<NamedRuntimeConfigKey, Map<String, JobConfig>> fromXml(String xml) {
    List<JobConfig> jobConfigs = (List<JobConfig>)getXStream().fromXML(xml);
    Map<NamedRuntimeConfigKey, Map<String, JobConfig>>namedConfigs = new HashMap<NamedRuntimeConfigKey, Map<String, JobConfig>>();

    for(JobConfig jobConfig : jobConfigs) {      
      NamedRuntimeConfigKey key = new NamedRuntimeConfigKey(jobConfig.getMachineId(), jobConfig.getCode().getIdAndVersion());
      Map<String, JobConfig> namedConfigsByMachineAndCode = namedConfigs.get(key);
      if(namedConfigsByMachineAndCode == null) {
        namedConfigsByMachineAndCode = new HashMap<String, JobConfig>();
        namedConfigs.put(key, namedConfigsByMachineAndCode);
      }
      namedConfigsByMachineAndCode.put(jobConfig.getName(), jobConfig);
    }
    return namedConfigs;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    // Create job config
    JobConfig conf = new JobConfig("hopper_agni-small-job");
    conf.setMachineId("hopper");
    conf.setCodeId("agni");
    conf.setRemoteDir("/global/homes/v/vfreedma/scratch/amanzi/Richards-1D-transport");
    conf.setAccount("m1012");
    conf.setUserName("vfreedma");
    conf.setProcessors(240);
    conf.setQueueName("debug");
    conf.setTime(0, 30, 0);
    conf.setCommand("/project/projectdirs/m1012/agni/install/current/bin/Agni/Agni --infile=agni.xml");
    conf.getJobHandlerParameters().put("simulatorCommand", "/project/projectdirs/m1012/amanzi/install/current/bin/amanzi");
   
    JobConfigServiceDefault service = new JobConfigServiceDefault();
    service.addNamedConfig(conf);
    service.saveNamedConfigs();

  }

  public static class NamedRuntimeConfigKey {

    private String machineId;
    private String codeId;

    public NamedRuntimeConfigKey(String machineId, String codeId) {
      super();
      this.machineId = machineId;
      this.codeId = codeId;
    }

    /**
     * @return the machineId
     */
    public String getMachineId() {
      return machineId;
    }

    /**
     * @param machineId the machineId to set
     */
    public void setMachineId(String machineId) {
      this.machineId = machineId;
    }

    /**
     * @return the codeId
     */
    public String getCodeId() {
      return codeId;
    }

    public String getSafeCodeId() {
      if(codeId == null) {
        return "";
      } else {
        return codeId;
      }
    }

    public String getSafeMachineId() {
      if(machineId == null) {
        return "";
      } else {
        return machineId;
      }
    }

    /**
     * @param codeId the codeId to set
     */
    public void setCodeId(String codeId) {
      this.codeId = codeId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      return toString().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if(obj != null && obj instanceof NamedRuntimeConfigKey) {
        return obj.toString().equals(toString());
      } else {
        return false;
      }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return getSafeMachineId() + "/" + getSafeCodeId();
    }



  }

}
