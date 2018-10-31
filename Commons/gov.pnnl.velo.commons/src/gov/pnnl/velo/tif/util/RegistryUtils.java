package gov.pnnl.velo.tif.util;

import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.tif.service.impl.MachineRegistryAdapter;

/**
 * TODO: add static methods for standard job handler behavior
 * @author D3K339
 *
 */
public class RegistryUtils {
  
  public static Code getCodeForMachine(String machineId, String codeId, String version){
    
    Machine machine = getMachine(machineId);
    Code code = TifServiceLocator.getCodeRegistry().get(codeId, version);
    
    //Whole code object can be overridden/merged by machine specific definition. 
    //if machine contains the exact codeid and version use that code object.
    //values for code parameters in machine registry overrides values in code registry
    //merge happens when machines are loaded, so just grab the code from machine obj
    if(machine.getCodes().contains(code)){
      //machine.getCodes().get(machine.getCodes().indexOf(code)).merge(code);
      code = machine.getCodes().get(machine.getCodes().indexOf(code));
    }
    return code;
  }
  
  public static Code getCodeForMachine(Machine machine, String codeId, String version){
    Code code = TifServiceLocator.getCodeRegistry().get(codeId, version);
    
    //Whole code object can be overridden/merged by machine specific definition. 
    //if machine contains the exact codeid and version use that code object.
    //values for code parameters in machine registry overrides values in code registry
    //merge happens when machines are loaded, so just grab the code from machine obj
    if(machine.getCodes().contains(code)){
      //machine.getCodes().get(machine.getCodes().indexOf(code)).merge(code);
      code = machine.getCodes().get(machine.getCodes().indexOf(code));
    }
    return code;
  }
  
  public static Code getCodeForMachine(Machine machine, Code code){
    
    //Whole code object can be overridden/merged by machine specific definition. 
    //if machine contains the exact codeid and version use that code object.
    //values for code parameters in machine registry overrides values in code registry
    //merge happens when machines are loaded, so just grab the code from machine obj
    if(machine.getCodes().contains(code)){
      //machine.getCodes().get(machine.getCodes().indexOf(code)).merge(code);
      code = machine.getCodes().get(machine.getCodes().indexOf(code));
    }
    return code;
  }
  
  public static Machine getMachine(String machineId){
    MachineRegistry registry = TifServiceLocator.getMachineRegistry();
    if(registry!=null){
     return registry.get(machineId);
    }else{
      return null;
    }
  }
  
  

}
