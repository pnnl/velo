package gov.pnnl.velo.model;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class Properties {

  private Map<String, List<String>> properties = new HashMap<String, List<String>>();

  public Properties() {

  }

  /**
   * @return
   */
  public Map<String, List<String>> toList() {
    return properties;
  }

  /**
   * Method getPropertyAsString.
   * @param propName String
   * @return String
   */
  public String getPropertyAsString(String propName) {
    String value = null;
    List<String> values = properties.get(propName);
    if(values != null && values.size() > 0) {
      value = values.get(0);
    }
    return value;
  }
  
  public List<String> getProperty(String propName) {
    return properties.get(propName);
  }
  
  /**
   * Set single-valued property
   * @param name
   * @param value
   */
  public void setProperty(String name, String value) {
    if (properties == null) {
      properties = new HashMap<String, List<String>>();
    }
    if(value == null) {
      properties.put(name, (ArrayList<String>)null);
    } else {
      List<String> values = new ArrayList<String>();
      values.add(value);
      properties.put(name, values);
    }
  }
  
  /**
   * Set multivalued property
   * @param name
   * @param value
   */
  public void setProperty(String name, List<String> value) {
    if (properties == null) {
      properties = new HashMap<String, List<String>>();
    }
    properties.put(name, value);    
  }
  
  /**
   * Add a value to the list of values for this property
   * @param name
   * @param value
   */
  public void addProperty(String name, String value) {
    if (properties == null) {
      properties = new HashMap<String, List<String>>();
    }
    List<String> values = properties.get(name);
    if(values == null) {
      values = new ArrayList<String>();
      properties.put(name, values);
    }
    values.add(value);
  }

  public Map<String, List<String>> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, List<String>> properties) {
    this.properties = properties;
  }
  
}
