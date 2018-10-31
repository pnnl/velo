/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.model.Parameter;
import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;
import gov.pnnl.velo.tif.service.ScriptRegistry;
import gov.pnnl.velo.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
public abstract class ScriptRegistryAdapter implements ScriptRegistry {

  private Map<String, File> scripts = new HashMap<String, File>();
  protected List<RegistryConfigFileProvider> providers = new ArrayList<RegistryConfigFileProvider>();
  boolean initialized = false;
  
  /**
   * Default constructor - loads scripts provided from getScriptFiles method
   */
  public ScriptRegistryAdapter(){
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CodeRegistry#addRegistryProvider(gov.pnnl.velo.tif.service.RegistryConfigFileProvider)
   */
  @Override
  public void addRegistryProvider(RegistryConfigFileProvider provider) {
    providers.add(provider);    
  }

  @Override
  public void init() {
    // prevent init from running twice
    if(!initialized) {
      loadConfigs();
      initialized = true;
    }
  }

  private void loadConfigs() {
    for(RegistryConfigFileProvider provider : providers) {
      for(File file : provider.getScriptFiles()) {
        scripts.put(file.getName(), file);        
      }
    }
  }
  
  
  public void reload(){
    //wipe clean
    scripts = new HashMap<String, File>();  
    //load again
    loadConfigs();
  }
  
  @Override
  public File getForkScript() {
    return scripts.get(ScriptRegistry.SCRIPT_NAME_FORK);
  }

  @Override
  public File getJobScriptTemplate(String codeId, String codeVersion, String machineId) {
    if(codeVersion == null || codeVersion.isEmpty()) {
      codeVersion = CodeRegistry.VERSION_DEFAULT;
    }

    // First look for the most specific template - based on the code and target machine
    // This often will get a hit for queued machines
   
    String key = codeId +"_" + codeVersion + "_" + machineId;
    File template = scripts.get(key);
   
    if(template == null && codeVersion.equalsIgnoreCase(CodeRegistry.VERSION_DEFAULT)) {
      key = codeId +"_" + machineId;
      template = scripts.get(key);
    }

    // If no template, look for a template for the code
    // This often works for workstations
    if(template == null) {
      key = codeId +"_" + codeVersion;
      template = scripts.get(key);
    }
    
    if(template == null && codeVersion.equalsIgnoreCase(CodeRegistry.VERSION_DEFAULT)) {
      key = codeId;
      template = scripts.get(key);
    }

    // If no template, look for a template for the machine
    if(template == null) {
      key = machineId;
      template = scripts.get(key);
    }

    // If no template, try the generic template; good for running commands more so than jobs?
    if(template == null) {
      key = ScriptRegistryDefault.SCRIPT_NAME_DEFAULT;
      template = scripts.get(key);
    }

    return template;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.ScriptRegistry#createJobScript(java.io.File, java.io.File, gov.pnnl.velo.tif.model.JobConfig)
   */
  @Override
  public void createJobScript(File jobScriptTemplate, File jobScript, JobConfig runConfig) {

    try {
      String script = FileUtils.readFileAsString(jobScriptTemplate);

      if (script != null) {
        script = script.replace("##code##", runConfig.getCommand());
        script = script.replace("##runDir##", runConfig.getRemoteDir());
        if (runConfig.getAccount() != null) {
          script = script.replace("##account##", runConfig.getAccount());
        }
        script = script.replace("##procs##", String.valueOf(runConfig.getProcessors()));
        script = script.replace("##nodes##", String.valueOf(runConfig.getNodes()));
        if(runConfig.getJobId() != null) {
          script = script.replace("##name##", runConfig.getJobId());
        }
        if(runConfig.getQueueName() !=null)
          script = script.replace("##queuename##", runConfig.getQueueName());
        if (runConfig.getTimeLimit() != null) {
          script = script.replace("##walltime##", runConfig.getTimeLimit());
        }
        
        // Any custom ##'s
        if(runConfig.getJobHandlerParameters() != null) {
          for(String key: runConfig.getJobHandlerParameters().keySet()) {
            String replacement = runConfig.getJobHandlerParameters().get(key);
            if(replacement != null) {
              script = script.replace("##" + key + "##", replacement);
            }
          }
        }

        // Also process custom machine parameters
        Machine machine = runConfig.getMachine();
        if(machine != null) {
          if(machine.getCustomParameters() != null) {
            for(Parameter param: machine.getCustomParameters()) {
              String replacement = param.getValue();
              if(replacement != null) {
                script = script.replace("##" + param.getName() + "##", replacement);
              }
            }
          }
        }
        FileUtils.writeStringToFile(jobScript, script);

      } else {
        throw new RuntimeException("Unable to generate job script.");
      }
      
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("Unable to create job script.", e);
    }
  }


}
