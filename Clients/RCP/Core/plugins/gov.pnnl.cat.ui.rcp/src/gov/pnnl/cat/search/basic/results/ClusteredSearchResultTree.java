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
package gov.pnnl.cat.search.basic.results;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.webservice.search.ClusterDetails;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 */
public class ClusteredSearchResultTree {
  ArrayList<ClusteredSearchResultTree> clusterChildren;
  ClusteredSearchResultTree parent;
  ArrayList<IResource> resources;

  private String path;
  private boolean topLevel;
  private Logger logger = CatLogger.getLogger(ClusteredSearchResultTree.class);

  /**
   * Default constructor is for top level tree 
   * Do we want to make the top level path = null or "/"?
   */
  //might not be needed
  public ClusteredSearchResultTree()
  {
    parent = null;
    topLevel = true;
    clusterChildren = new ArrayList<ClusteredSearchResultTree>();
    resources = new ArrayList<IResource>();
    path = "/";
  }

  /**
   * For a non-top level tree 
   * @param parent
   * @param path
   */
  public ClusteredSearchResultTree(ClusteredSearchResultTree parent, String path)
  {
    this.parent = parent;
    this.path = path;
    clusterChildren = new ArrayList<ClusteredSearchResultTree>();
    resources = new ArrayList<IResource>();
  }

  /**
   * Method addImmediateChild.
   * @param cluster ClusterDetails
   * @throws Exception
   */
  public void addImmediateChild(ClusterDetails cluster) throws Exception 
  {
    System.out.println("adding to tree: " + cluster.getPath());
    ClusteredSearchResultTree child = new ClusteredSearchResultTree(this, cluster.getPath());
    addChild(child);    
  }
  
  //add a child to the tree to the appropriate spot
  /**
   * Method addChild.
   * @param cluster ClusterDetails
   * @return boolean
   */
  public boolean addChild(ClusterDetails cluster)
  {
    boolean isImmediateChild = false;
    String clusterParentPath = cluster.getParentPath();
    
    if( (path == null || path.equals("/")) && (clusterParentPath == null || clusterParentPath.equals("/")))
    {
      isImmediateChild = true;
    }
    else if(path != null && clusterParentPath != null && path.equals(clusterParentPath))
    {
      isImmediateChild = true;
    }

    
    if(isImmediateChild)
    {
      try {
        addImmediateChild(cluster);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        logger.error(e);
      }
      System.out.println("Add " + cluster.getPath() + " to " + path);
      return true;
    }
    else
    {
      boolean added = false;
      for(ClusteredSearchResultTree child : clusterChildren)
      {
        added = child.addChild(cluster);
        if(added) break;
      }
      System.out.println("Cannot add " + cluster.getPath() + " to " + path);
      return added;
    }
  }

//  public ClusteredSearchResultTree(ClusterDetails cluster)
//  {
//    String parentPath = cluster.getParentPath();
//    
//    //make it a top level tree if parentPath = null or "/"
//    if(parentPath == null || parentPath.equals("/"))
//    {
//      parent = null;
//      topLevel = true;
//    }
//    else
//    {
//      
//    }
//    clusterChildren = new ArrayList<ClusteredSearchResultTree>();
//    resources = new ArrayList<IResource>();
//  }

 
  /**
   * Method setParent.
   * @param parent ClusteredSearchResultTree
   */
  public void setParent(ClusteredSearchResultTree parent)
  {
   this.parent = parent; 
  }

  /**
   * This only applies to an immediate child
   * @param child
   */
  public void addChild(ClusteredSearchResultTree child)
  {
    clusterChildren.add(child);
  }

  /**
   * Method addResource.
   * @param resource IResource
   */
  public void addResource(IResource resource)
  {
    resources.add(resource);
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString()
  {
   String treeString = "path=" + path + "\n";
   treeString += "number of children:" + clusterChildren.size();
   return treeString;
  }

  /**
   * A string to represent the tree structure:
   * Current Level A
   *    Second Level B
   *      Third Level C
   *      Third Level D
   *    Second Level E
   *      Third Level F
   *        Forth Level G
   * @param indent int
   * @return String
   */
  public String toString(int indent)
  {
    //is there a simpler way to make the indent string
    char [] indentChr = new char[indent];
    for(int i=0; i<indent; i++)
    {
      indentChr[i] = ' ';
    }
    String indentStr = new String(indentChr);
    
    String treeString = indentStr + path + "\n";
    for(ClusteredSearchResultTree child : clusterChildren)
    {
      treeString += child.toString(indent + 3);
    }
    
    return treeString;
  }

  /**
   * Method getClusterChildren.
   * @return ArrayList<ClusteredSearchResultTree>
   */
  public ArrayList<ClusteredSearchResultTree> getClusterChildren() {
    return clusterChildren;
  }

  /**
   * Method setClusterChildren.
   * @param clusterChildren ArrayList<ClusteredSearchResultTree>
   */
  public void setClusterChildren(ArrayList<ClusteredSearchResultTree> clusterChildren) {
    this.clusterChildren = clusterChildren;
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
   * Method getResources.
   * @return ArrayList<IResource>
   */
  public ArrayList<IResource> getResources() {
    return resources;
  }

  /**
   * Method setResources.
   * @param resources ArrayList<IResource>
   */
  public void setResources(ArrayList<IResource> resources) {
    this.resources = resources;
  }

  /**
   * Method isTopLevel.
   * @return boolean
   */
  public boolean isTopLevel() {
    return topLevel;
  }

  /**
   * Method setTopLevel.
   * @param topLevel boolean
   */
  public void setTopLevel(boolean topLevel) {
    this.topLevel = topLevel;
  }

  /**
   * Method getParent.
   * @return ClusteredSearchResultTree
   */
  public ClusteredSearchResultTree getParent() {
    return parent;
  }
}
