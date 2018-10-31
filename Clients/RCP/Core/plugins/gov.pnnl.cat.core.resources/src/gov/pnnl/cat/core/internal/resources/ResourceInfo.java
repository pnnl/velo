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
package gov.pnnl.cat.core.internal.resources;

import gov.pnnl.velo.model.CmsPath;

import java.util.Enumeration;

import org.eclipse.core.runtime.QualifiedName;

/**
 */
public class ResourceInfo {
  
  /**
   * We want to map QNames to their associated properties
   * The properties will come straight out of the propfind enumeration
   * as provided by the WebDAVClient.  Some parsing will be involved.
   * @param properties Enumeration
   */ 
//  private HashMap properties;
  
  public ResourceInfo(Enumeration properties) { 
    // TODO method stub
  }

  /**
   * Returns the path associated with this resource
   * @return CmsPath
   */
  public CmsPath getPath() {
    return null;
  }
  
  /**
   * Shortcut method to quickly access the NAME property
   * @return String
   */
  public String getName() {
    return null;
  }
  
  /**
   * Retrieve a property in string format.  All the properties will have
   * been set in the constructor.
   * @param key QualifiedName
   * @return String
   */
  public String getPropertyAsString(QualifiedName key) {
    return null;
  }
}
