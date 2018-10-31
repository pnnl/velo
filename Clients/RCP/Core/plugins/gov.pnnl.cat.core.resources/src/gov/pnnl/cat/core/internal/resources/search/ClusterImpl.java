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
import gov.pnnl.cat.core.resources.search.ICluster;
import gov.pnnl.velo.model.CmsPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class ClusterImpl implements ICluster {
  
  protected CmsPath path;

  protected ICluster parentCluster;

  protected List<ICluster> subclusters = new ArrayList<ICluster>();

  protected List<IResource> resourceMatches = new ArrayList<IResource>();

  /**
   * Method addResourceMatch.
   * @param resourceMatch IResource
   * @see gov.pnnl.cat.core.resources.search.ICluster#addResourceMatch(IResource)
   */
  public void addResourceMatch(IResource resourceMatch) {
    if (!resourceMatches.contains(resourceMatch)) {
      resourceMatches.add(resourceMatch);
    }
  }

  /**
   * Method addSubcluster.
   * @param subcluster ICluster
   * @see gov.pnnl.cat.core.resources.search.ICluster#addSubcluster(ICluster)
   */
  public void addSubcluster(ICluster subcluster) {
    if (!subclusters.contains(subcluster)) {
      this.subclusters.add(subcluster);
    }
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.core.resources.search.ICluster#getName()
   */
  public String getName() {
    return path.last().getName();
  }

  /**
   * Method getParent.
   * @return ICluster
   * @see gov.pnnl.cat.core.resources.search.ICluster#getParent()
   */
  public ICluster getParent() {
    return this.parentCluster;
  }

  /**
   * Method getPath.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.search.ICluster#getPath()
   */
  public CmsPath getPath() {
    return this.path;
  }

  /**
   * Method getReourceMatches.
   * @return List<IResource>
   * @see gov.pnnl.cat.core.resources.search.ICluster#getReourceMatches()
   */
  public List<IResource> getReourceMatches() {
    return this.resourceMatches;
  }
  
  /**
   * return a list of all resource under this tree
   *  which includes the resource under children ICluster
   * @return List<IResource>
   * @see gov.pnnl.cat.core.resources.search.ICluster#getWholeTreeResourceMatches()
   */
  public List<IResource> getWholeTreeResourceMatches()
  {
    List<IResource> allMatches = new ArrayList<IResource>(resourceMatches);
    if(subclusters !=  null)
    {
      for(ICluster child : subclusters)
      {
        List<IResource> childAllMatches = child.getWholeTreeResourceMatches();
        allMatches.addAll(childAllMatches);
      }
    }
    
    return allMatches;
  }

  /**
   * Method getSubclusters.
   * @return List<ICluster>
   * @see gov.pnnl.cat.core.resources.search.ICluster#getSubclusters()
   */
  public List<ICluster> getSubclusters() {
    return this.subclusters;
  }

  /**
   * Method isResourceMatch.
   * @param resource IResource
   * @return boolean
   * @see gov.pnnl.cat.core.resources.search.ICluster#isResourceMatch(IResource)
   */
  public boolean isResourceMatch(IResource resource) {
    return resourceMatches.contains(resource);
  }

  /**
   * Method setParent.
   * @param parent ICluster
   * @see gov.pnnl.cat.core.resources.search.ICluster#setParent(ICluster)
   */
  public void setParent(ICluster parent) {
    this.parentCluster = parent;
  }

  /**
   * Method setPath.
   * @param path CmsPath
   * @see gov.pnnl.cat.core.resources.search.ICluster#setPath(CmsPath)
   */
  public void setPath(CmsPath path) {
    this.path = path;
  }

  /**
   * Method setResourceMatches.
   * @param resources List<IResource>
   * @see gov.pnnl.cat.core.resources.search.ICluster#setResourceMatches(List<IResource>)
   */
  public void setResourceMatches(List<IResource> resources) {
    if(resources == null)
    {
      resourceMatches = new ArrayList<IResource>();
    }
    else
    {
      this.resourceMatches = resources;
    }
  }

  /**
   * Method setSubclusters.
   * @param subclusters List<ICluster>
   * @see gov.pnnl.cat.core.resources.search.ICluster#setSubclusters(List<ICluster>)
   */
  public void setSubclusters(List<ICluster> subclusters) {
    this.subclusters = subclusters;
  }
  
  /**
   * Method toString.
   * @return String
   */
  public String toString(){
    return this.path.toString();
  }

  /**
   * Method getTotalCount.
   * @return int
   * @see gov.pnnl.cat.core.resources.search.ICluster#getTotalCount()
   */
  public int getTotalCount()
  {
    int count = resourceMatches.size();
    for(ICluster child : subclusters)
    {
      count += child.getTotalCount();
    }
    
    return count;
  }
  
  //How about grandchild?
  /**
   * Method hasChild.
   * @param child ICluster
   * @return boolean
   * @see gov.pnnl.cat.core.resources.search.ICluster#hasChild(ICluster)
   */
  public boolean hasChild(ICluster child)
  {
    boolean isChild = false;
    for(ICluster kid : subclusters)
    {
      if(kid.equals(child))
      {
        isChild = true;
        break;
      }
    }
    return isChild;
  }
  
  //if the path the same, then the same?
  /**
   * Method equals.
   * @param obj Object
   * @return boolean
   * @see gov.pnnl.cat.core.resources.search.ICluster#equals(Object)
   */
  public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (!(obj instanceof ClusterImpl))
        return false;
      ClusterImpl target = (ClusterImpl) obj;
      return path.equals(target.getPath());
  }
  
  /**
   * Method printWholeTree.
   * @param level int
   * @return String
   * @see gov.pnnl.cat.core.resources.search.ICluster#printWholeTree(int)
   */
  public String printWholeTree(int level){
    //first make an appropriate indentation
    String indentStr = "";
    if(level > 0)
    {
      int indent = 3 * level;
      char [] indentChr = new char[indent];
      for(int i=0; i<indent; i++)
      {
        indentChr[i] = ' ';
      }
      indentStr = new String(indentChr);
    }
    String wholeTree = indentStr + path.toString() + "\n";
    //also print out resources under this tree:
    for(IResource resource: this.resourceMatches){
      wholeTree += indentStr + resource.getName() + "\n";
    }
    
    for(ICluster child : subclusters)
    {
      wholeTree += child.printWholeTree(level+1);
    }
    return wholeTree;
  }

  /**
   * Method sort.
   * @see gov.pnnl.cat.core.resources.search.ICluster#sort()
   */
  public void sort() {
    Collections.sort(subclusters);
    for(ICluster child : subclusters)
    {
      child.sort();
    }
  }

  /**
   * Method compareTo.
   * @param arg0 Object
   * @return int
   */
  public int compareTo(Object arg0) {
    ICluster cluster0 = (ICluster)arg0;
    return (getName().compareTo(cluster0.getName()));
  }


}
