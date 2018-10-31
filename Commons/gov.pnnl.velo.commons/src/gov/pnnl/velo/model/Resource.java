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
import gov.pnnl.velo.util.VeloConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Resource {
  
  private List<String> aspects = new ArrayList<String>();
  private Map<String, List<String>> properties = new HashMap<String, List<String>>();
  private String type;
  private String uuid;
  private String path;
  private int numChildren;
  private int numFolderChildren;
  
  /**
   * Copy constructor - we are doing shallow copy here!
   * @param resource
   */
  public Resource (Resource resource) {
   this.aspects = resource.aspects;
   this.properties = resource.properties;
   this.type = resource.type;
   this.uuid = resource.uuid;
   this.path = resource.path;
   this.numChildren = resource.numChildren;
   this.numFolderChildren = resource.numFolderChildren;
  }
  
  /**
   * DO NOT USE.  For JSON deserialization only.
   * @deprecated 
   */
  public Resource() {
    
  }
  
  /**
   * Can only be used for a pre-existing resource
   * @param uuid
   */
  public Resource(String uuid) {
    setUuid(uuid);
  }
   
  /**
   * Can only be used for a pre-existing resource
   * @param path
   */
  public Resource(CmsPath path) {
    setPath(path.toAssociationNamePath());
    
    // make sure we set the name property to match the path
    setName(path.getName());    
  }
  
  /**
   * Use this method if you are creating a new resource., as type is required
   * @param type
   * @param path
   * @param name
   */
  public Resource(String type, CmsPath path) {
    setType(type);
    setPath(path.toAssociationNamePath());
    
    // make sure we set the name property to match the path
    setName(path.getName());
  }
  
  public void setName(String name) {
    setProperty(VeloConstants.PROP_NAME, name);
  }
  
  public String getName() {
    return getPropertyAsString(VeloConstants.PROP_NAME);
  }
  
  /**
   * Method getAspects.
   * @return List<String>
   */
  public List<String> getAspects() {
    return aspects;
  }
  
  public boolean hasAspect(String aspect) {
    return aspects.contains(aspect);
  }
  
  /**
   * Method getProperties.
   * @return Map<String,List<String>>
   */
  public Map<String, List<String>> getProperties() {
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
    if(values != null && values.size() == 1) {
      value = values.get(0);
    }else if(values != null && values.size() > 1) {
      value = values.toString(); 
    }
    return value;
  }
  
  /**
   * Method getType.
   * @return String
   */
  public String getType() {
    return type;
  }
  
  /**
   * Method setType.
   * @param type String
   */
  public void setType(String type) {
    this.type = type;
  }
  /**
   * Method getUuid.
   * @return String
   */
  public String getUuid() {
    return uuid;
  }
  /**
   * Method setUuid.
   * @param uuid String
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
  /**
   * Method getPath.
   * @return String
   */
  public String getPath() {
    return path;
  }
  /**
   * Method setPath.
   * @param path String
   */
  public void setPath(String path) {
    this.path = path;
  }
  
  /**
   * Method hashCode.
   * @return int
   */
  @Override
  public int hashCode() {
    return uuid.hashCode();
  }
  /**
   * Method equals.
   * @param obj Object
   * @return boolean
   */
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Resource) && ((Resource)obj).getUuid().equals(uuid);
  }
  
  /**
   * Method toString.
   * @return String
   */
  @Override
  public String toString() {
    return path;
  }
  
  /**
   * Method getNumChildren.
   * @return int
   */
  public int getNumChildren() {
    return numChildren;
  }

  /**
   * @return
   */
  public int getNumFolderChildren() {
    return numFolderChildren;
  }
  
  /**
   * Method setNumChildren.
   * @param numChildren int
   */
  public void setNumChildren(int numChildren) {
    this.numChildren = numChildren;
  }
  
  public void setNumFolderChildren(int size) {
	  this.numFolderChildren = size;
  }
  
  public void setAspects(List<String> aspects) {
    this.aspects = aspects;
  }
  
  public void setProperties(Map<String, List<String>> properties) {
    this.properties = properties;
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
  
}
