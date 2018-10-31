/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.core.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import gov.pnnl.cat.logging.CatLogger;

/**
 */
public class VeloGlobalProperties extends PropertyPlaceholderConfigurer implements InitializingBean {
  
  private static final String SYS_PROP_PROPERTIES_FILE_PATH = "repository.properties.path";
  protected Properties properties = new Properties();
  protected Resource repositoryPropertiesFile;
  Logger logger = CatLogger.getLogger(VeloGlobalProperties.class);
  
  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    
    // Don't inject the locations as we resolve them dynamically
    String repositoryPropertiesFilePath = System.getProperty(SYS_PROP_PROPERTIES_FILE_PATH);
    Resource location;
   
    if(repositoryPropertiesFilePath != null) {
      System.out.println("repository.properties.path set to: " + repositoryPropertiesFilePath);
      File repositoryPropertiesFile = new File(repositoryPropertiesFilePath);
      
      // use file from deployed location
      location = new FileSystemResource(repositoryPropertiesFile);
    
    } else {
      throw new RuntimeException("repository.properties.path not set in environment varaibles");
    }
    setLocation(location); 
    repositoryPropertiesFile = location;
    
    // load my copy of the properties
    loadProperties(properties);
    for(String key : properties.stringPropertyNames()) {
      String propval = properties.getProperty(key);
      propval = parseStringValue(propval, properties, new HashSet<String>());
      properties.put(key, propval);
    }
  
  }

  /**
  
   * @return the properties */
  public Properties getProperties() {
    return properties;
  }
  
  public void setProperties (Properties properties) {
    this.properties = properties;
    
    // save properties to file
    try {
      File propsFile = repositoryPropertiesFile.getFile();
      OutputStream out = new FileOutputStream(propsFile);
      properties.store(out, "");
    } catch (Throwable e) {
      throw new RuntimeException("Failed to save repository.properties!", e);
    }
  }

}
