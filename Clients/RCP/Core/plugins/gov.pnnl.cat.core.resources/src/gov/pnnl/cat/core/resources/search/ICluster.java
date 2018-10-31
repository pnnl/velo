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
package gov.pnnl.cat.core.resources.search;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.model.CmsPath;

import java.util.List;

/**
 */
public interface ICluster extends Comparable {
/*-------------- Read Operations --------------------*/
  
  /**
  
   * @return the matches in the cluster */
  public List<IResource> getReourceMatches();
  
  /**
   * The resource list for the whole tree, not only the immediate children
  
   * @return List<IResource>
   */
  public List<IResource> getWholeTreeResourceMatches();
  
  /**
   * 
   * @param resource
  
   * @return true if the given resource is a match in this cluster */
  public boolean isResourceMatch(IResource resource);

  /**
   * Use this to display the cluster's hierarchy.
   * E.g., /BKC/CAT/CAT Developers.
   * 
   * 
  
   * @return An CmsPath representing the cluster's hierarchy.
   * This is NOT a real path to a node. */
  public CmsPath getPath();
  
  /**
   * This is the last segment in the cluster path, which is
   * the display name for the group (i.e., CAT Developers)
  
   * @return String
   */
  public String getName();
  
  /**
   * Gets the parent cluster
   * (This can be determined from the path.)
  
   * @return null if this is a top level cluster */
  public ICluster getParent();
  
  /**
   * Gets the child clusters.
   * @return List<ICluster>
   */
  public List<ICluster> getSubclusters();
  
  
  /*-------------- Write Operations --------------------*/
 
  
  /**
   * Method addResourceMatch.
   * @param resourceMatch IResource
   */
  public void addResourceMatch(IResource resourceMatch);
  /**
   * Method addSubcluster.
   * @param subcluster ICluster
   */
  public void addSubcluster(ICluster subcluster);
  
  /**
   * Method setPath.
   * @param path CmsPath
   */
  public void setPath(CmsPath path);
  
  /**
   * Method setResourceMatches.
   * @param resourceMatches List<IResource>
   */
  public void setResourceMatches(List<IResource> resourceMatches);
  
  /**
   * Method setParent.
   * @param parent ICluster
   */
  public void setParent(ICluster parent);
  
  /**
   * Method setSubclusters.
   * @param subclusters List<ICluster>
   */
  public void setSubclusters(List<ICluster> subclusters);

  /**
   * Method getTotalCount.
   * @return int
   */
  public int getTotalCount();
  
  /**
   * Method printWholeTree.
   * @param i int
   * @return String
   */
  public String printWholeTree(int i);

  /**
   * Method hasChild.
   * @param child ICluster
   * @return boolean
   */
  public boolean hasChild(ICluster child);
  
  /**
   * Method equals.
   * @param obj Object
   * @return boolean
   */
  public boolean equals(Object obj);
  
  //the sort method sort the child clusters by names
  public void sort();
}
