/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.VeloTifConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * Base class for implementing MachineRegistry interface.
 * Subclasses only need to provide the list of machines (from wherever)
 * and they will be loaded appropriately.
 *
 */
public abstract class MachineRegistryAdapter implements MachineRegistry {
  private static Logger logger = Logger.getLogger(MachineRegistryAdapter.class);
  
  protected Map<String, Machine> machines = new HashMap<String, Machine>();  
  protected List<RegistryConfigFileProvider> providers = new ArrayList<RegistryConfigFileProvider>();
  boolean initialized = false;
  
  /**
   * Default constructor - loads machines provided from getMachineConfigFiles method
   */
  public MachineRegistryAdapter(){
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
    // so we can only run once
    if(!initialized) {
      loadConfigs();
      initialized = true;
    }
  }

  private void loadConfigs() {
    for(RegistryConfigFileProvider provider : providers) {
      for(File file : provider.getMachineConfigFiles()) {
        try {
          registerMachineInternal(new FileInputStream(file));
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        } catch(Exception e){
          throw new RuntimeException(
              "Error parsing machine configuration file: "+ file.getAbsolutePath(), e);
        }
      }
    }
  }
  
  public void reload(){
    //wipe clean
    machines = new HashMap<String, Machine>();  
    //load again
    loadConfigs();
  }
  
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.MachineRegistry#getMachineIDs()
   */
  @Override
  public Collection<String> getMachineIDs() {
    return machines.keySet();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.MachineRegistry#get(java.lang.String)
   */
  @Override
  public Machine get(String machineID) {
    return machines.get(machineID);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.MachineRegistry#getMachineByFullyQualifiedDomainName(java.lang.String)
   */
  @Override
  public Machine getMachineByFullyQualifiedDomainName(String fullyQualifiedDomainName) {
    Machine machine = null;
    for (Machine m : machines.values()) {
      if(m.getFullDomainName().equals(fullyQualifiedDomainName)) {
        machine = m;
        break;
      }
    }
    return machine;
  }

  protected void registerMachineInternal(InputStream xmlFile) { 
    
    // TODO: need to run the xml through a schema validator
    XStream xstream = new XStream();
    xstream.autodetectAnnotations(true); 
    xstream.alias("machine", Machine.class);//sucks I have to do this in code instead of it being smart enough to use the annotation...
    Machine machine = (Machine)xstream.fromXML(xmlFile);
    List<Code> mergedCodes = new ArrayList<Code>();
    for(Code mcode : machine.getCodes()){
      Code parentCode = TifServiceLocator.getCodeRegistry().get(mcode.getId(), mcode.getVersion());
      if(parentCode != null) {
        mcode.merge(parentCode);
      }
      mergedCodes.add(mcode);
    }
    //Check if machines already has a machine definition from a different module. More
    //than one module can load the same machine with different set of codes 
    if(machines.get(machine.getName())==null){
        machine.setCodes(mergedCodes);
    	machines.put(machine.getName(), machine);
    }else{
    	Machine currentMachine = machines.get(machine.getName());
    	List<Code> currentCodes = currentMachine.getCodes();
    	if(currentCodes!=null){
    	  for (Code cur_code: currentCodes){
    	    if (! mergedCodes.contains(cur_code)){
    	      //Only add codes not in list of code
    	      //This way, config provided by registry provider explicitly added at the end
    	      // would get not get replaced by config loaded earlier
    	      mergedCodes.add(cur_code);
    	    }
    	  }
    	}
    		
    	machine.setCodes(mergedCodes);
    	machines.put(machine.getName(), machine);
    }

  }
  
  @Override
  public String getRemoteUserHomeDirectory(String machineId) {
    Machine machine = get(machineId);
    String userHomeParent = machine.getUserHomeParent();

    Properties props = TifServiceLocator.getMachineRegistry().getUserMachinePrefs(machineId);
    String userId = props.getProperty(VeloTifConstants.JOB_USER);
    
    if(userHomeParent != null && userId != null) {
      String runDir = machine.getUserHomeParent() + "/" + userId;
      return runDir;
    }
    return null;
  }

  @Override
  public void saveUserMachinePrefs(String machineName, Properties userMachinePrefs) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Properties getUserMachinePrefs(String machineName) {
    // TODO Auto-generated method stub
    return null;
  }


  
}
