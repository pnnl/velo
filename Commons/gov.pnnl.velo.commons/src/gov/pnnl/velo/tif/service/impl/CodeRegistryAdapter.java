package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * Base class for implementing CodeRegistry interface.
 * Subclasses only need to provide the list of codes (from wherever)
 * and they will be loaded appropriately.
 *
 */
public abstract class CodeRegistryAdapter implements CodeRegistry {
  private static Logger logger = Logger.getLogger(CodeRegistryAdapter.class);
  protected Map<String, Map<String,Code>> codes = new HashMap<String, Map<String,Code>>();
  boolean initialized = false;
  
  // Map each codeID with the local executable path, so we can run locally with executables
  // found in plugin fragments or modules
  protected Map<String, File> codeIdToExeBaseFolder = new HashMap<String, File>(); 
  
  protected List<RegistryConfigFileProvider> providers = new ArrayList<RegistryConfigFileProvider>();
    
  /**
   * Default constructor - loads codes provided from getCodeConfigFiles method
   */
  public CodeRegistryAdapter(){
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CodeRegistry#addRegistryProvider(gov.pnnl.velo.tif.service.RegistryConfigFileProvider)
   */
  @Override
  public void addRegistryProvider(RegistryConfigFileProvider provider) {
    providers.add(provider);    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CodeRegistry#init()
   */
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
      File exeFolder = provider.getExecutablesDir();

      for(File file: provider.getCodeConfigFiles()) {
        try {            
          logger.debug("adding code file: " + file.getAbsolutePath());
          Code code = registerCodeInternal(new FileInputStream(file));
          if(exeFolder != null) {
            codeIdToExeBaseFolder.put(code.getId(), exeFolder);     
          }
        } catch (FileNotFoundException e) {
          logger.error("Failed to register code file: " + file.getAbsolutePath());
        }catch(Exception e){
          throw new RuntimeException(
              "Error parsing code configuration file: "+ file.getAbsolutePath(), e);
        }
      }
    }
    //after all codes have been loaded, look for codes that have parents and then merge the parent's properties
    //that were overwritten into the child code
    mergeParentCodes();
  }
  
  public void reload(){
    //wipe clean
    codes = new HashMap<String, Map<String,Code>>();
    codeIdToExeBaseFolder = new HashMap<String, File>();
    //load again
    loadConfigs();
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CodeRegistry#getCodeIDs()
   */
  @Override
  public Collection<String> getCodeIDs() {
    return codes.keySet();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CodeRegistry#get(java.lang.String)
   */
  public Map<String, Code> get(String codeID) {
    return codes.get(codeID);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CodeRegistry#get(java.lang.String, java.lang.String)
   */
  @Override
  public Code get(String codeId, String version) {
    Code code = null;
    Map<String, Code> versions = codes.get(codeId);
    if(version == null) {
      // use the default version
      version = CodeRegistry.VERSION_DEFAULT;
    }
    if(versions != null) {      
        code = versions.get(version);
    }
    // TODO: do we need to sort by version date?
    return code;
  }

  public String getExeFolderPath(String codeId) {
    return codeIdToExeBaseFolder.get(codeId).getAbsolutePath();
  } 
  
  protected Code registerCodeInternal(InputStream xmlFile) {
    
    // TODO: need to run the xml through a schema validator
    XStream xstream = new XStream();
    xstream.autodetectAnnotations(true); 
    xstream.alias("code", Code.class);//sucks I have to do this in code instead of it being smart enough to use the annotation...
    Code code = (Code)xstream.fromXML(xmlFile);
    
    Map<String, Code> versions = codes.get(code.getId());
    if(versions == null) {
      versions = new HashMap<String, Code>();
      codes.put(code.getId(), versions);      
    }
    String version = code.getVersion();
    if(version == null || version.isEmpty()) {
      version = CodeRegistry.VERSION_DEFAULT;
    }
    versions.put(version, code);
    return code;
    
  }
  
  private void mergeParentCodes() {
    for(Map<String, Code> versions : codes.values()) {
      for(Code code : versions.values()) {
        String parentCodeId = code.getParentCodeId();
        String parentVersion = code.getParentVersion();
        
        // if we have specified a parent code id and version
        if(parentCodeId != null && !parentCodeId.isEmpty()
            && parentVersion != null && !parentVersion.isEmpty()) {
          
          Code parent = get(parentCodeId, parentVersion);
          if(parent != null) {
            code.merge(parent);
          } else {
            throw new RuntimeException("Code with id " + code.getId() + " specified a parent code with id " + parentCodeId
                + " and version " + parentVersion + "but this version is not registered.");
          }
        }        
      }
    }
  }

 
}
