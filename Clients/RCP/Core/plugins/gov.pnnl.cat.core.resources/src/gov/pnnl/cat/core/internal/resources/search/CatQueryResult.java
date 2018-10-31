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
package gov.pnnl.cat.core.internal.resources.search;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.velo.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 */
public class CatQueryResult implements ICatQueryResult {

  private String querySessionID;
  private List<IResource> handles = new ArrayList<IResource>();
  private List<Resource> resources;
  private Long totalHits;
  private HashMap<String, HashMap<String, Integer>> propertyFacets;
  
  
  /**
   * @return the totalHits
   */
  public Long getTotalHits() {
    return totalHits;
  }

  /**
   * @param totalHits the totalHits to set
   */
  public void setTotalHits(Long totalHits) {
    this.totalHits = totalHits;
  }

  /**
   * Method getQuerySessionID.
   * @return String
   * @see gov.pnnl.cat.core.resources.search.ICatQueryResult#getQuerySessionID()
   */
  public String getQuerySessionID() {
    return querySessionID;
  }

  /**
   * Method getHandles.
   * @return List<IResource>
   * @see gov.pnnl.cat.core.resources.search.ICatQueryResult#getHandles()
   */
  public List<IResource> getHandles() {
    return handles;
  }

  /**
   * Method setQuerySessionID.
   * @param querySessionID String
   */
  public void setQuerySessionID(String querySessionID) {
    this.querySessionID = querySessionID;
  }

  /**
   * Method setHandles.
   * @param resources List<IResource>
   */
  public void setHandles(List<IResource> resources) {
    this.handles = resources;
  }

  /**
  
   * @return the resources * @see gov.pnnl.cat.core.resources.search.ICatQueryResult#getResources()
   */
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * @param resources the resources to set
   */
  public void setResources(List<Resource> resources) {
    this.resources = resources;
  }

  public void setPropertyFacets(HashMap<String, HashMap<String, Integer>> propertyFacets) {
    this.propertyFacets = propertyFacets;
  }

  @Override
  public HashMap<String, HashMap<String, Integer>> getPropertyFacets() {
    return this.propertyFacets;
  }
  
  
  
}
