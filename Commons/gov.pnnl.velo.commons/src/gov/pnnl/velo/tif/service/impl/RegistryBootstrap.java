package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.ScriptRegistry;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Make sure the registry classes get initialized at the proper time
 * @author d3k339
 *
 */
public class RegistryBootstrap implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {
  private ApplicationContext applicationContext = null;
  private CodeRegistry codeRegistry;
  private MachineRegistry machineRegistry;
  private ScriptRegistry scriptRegistry;
  
  /* (non-Javadoc)
   * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
   */
  public void onApplicationEvent(ApplicationEvent event) {
    
    if (event instanceof ContextRefreshedEvent) {
      ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent)event;
      ApplicationContext refreshContext = refreshEvent.getApplicationContext();
      if (refreshContext != null && refreshContext.equals(applicationContext)) {
        // code registry needs to be initialized only after all  beans have been loaded
        codeRegistry.init();
        machineRegistry.init(); // code registry must be initlialized first
        scriptRegistry.init();
      }
    }
  }

  /* (non-Javadoc)
   * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;    
  }

  public void setCodeRegistry(CodeRegistry codeRegistry) {
    this.codeRegistry = codeRegistry;
  }

  public void setMachineRegistry(MachineRegistry machineRegistry) {
    this.machineRegistry = machineRegistry;
  }

  public void setScriptRegistry(ScriptRegistry scriptRegistry) {
    this.scriptRegistry = scriptRegistry;
  }
  
  
}
