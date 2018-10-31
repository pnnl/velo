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
package gov.pnnl.cat.ui.rcp.views.dnd;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class ResourceList {
  private ArrayList<IResource> resources = new ArrayList<IResource>();
  private int resourceTypes = 0;
  private Logger logger = CatLogger.getLogger(ResourceList.class);
  
  /**
   * Method add.
   * @param resource IResource
   * @throws ResourceException
   */
  public void add(IResource resource) throws ResourceException {
    resources.add(resource);
    resourceTypes = resourceTypes | resource.getType();
  }

  /**
   * Returns <code>true</code> if this list contains at least one resource of the specified type.
   * @param type the type of resource to search for
  
   * @return <code>true</code> if a resource of this type exists in the list. */
  public boolean contains(int type) {
    return (resourceTypes & type) != 0;
  }

  /**
   * Counts the number of resources in this list that are of the specified type.
   * @param type the type of resource to search for
  
  
   * @return the total number of resources of the given type. * @throws ResourceException  */
  public int count(int type) throws ResourceException {
    int count = 0;

    for (IResource resource : resources) {
      if (resource.isType(type)) {
        count++;
      }
    }

    return count;
  }

  /**
   * Returns <code>true</code> if all of the resources in this list are of the specified type.
   * @param type the type of resource to search for
  
  
   * @return <code>true</code> if all resources are of the given type.
   * <code>false</code> if any resource is not of the given type, or if the list is empty. * @throws ResourceException  */
  public boolean isHomogeneous(int type) throws ResourceException {
    // if our list is empty we will always return false.
    if (resources.isEmpty()) {
      return false;
    }

    return count(type) == resources.size();
  }
  
  /**
   * Method isHomogeneous.
   * @param types int[]
   * @return boolean
   * @throws ResourceException
   */
  public boolean isHomogeneous(int[] types) throws ResourceException {
    // if our list is empty we will always return false.
    if (resources.isEmpty()) {
      return false;
    }
    
    HashSet<IResource> resourcesSet = new HashSet<IResource>(resources);
    
    for(int type: types){
      Iterator<IResource> iterator = resourcesSet.iterator();
      while (iterator.hasNext()) {
        IResource element = (IResource) iterator.next();
        if(element.isType(type)){
          iterator.remove();
        }
      }
    }
    
    return resourcesSet.isEmpty();
  }

  /**
   * Returns <code>true</code> if any resource in this list has the specified resource as its parent.
   * @param parent resource whose status as a parent is to be tested
  
  
   * @return <code>true</code> if the specified resource is a parent to any of the resources contained in this list.
   * <code>false</code> otherwise, or if the list is empty. * @throws ResourceException  */
  public boolean containsAsParent(IResource parent) throws ResourceException {
    for (IResource resource : resources) {
      if (resource.getParent().equals(parent)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if the specified resource is a descendant to any resource in this list.
   * @param descendant resource whose status as a descendant is to be tested
  
   * @return <code>true</code> if the given resource is a descendant.
   * <code>false</code> otherwise, or if the list is empty. */
  public boolean containsAsDescendant(IResource descendant) {
    for (IResource resource : resources) {
      if (resource.getPath().isPrefixOf(descendant.getPath())) {
        return true;
      }
    }
    return false;
  }

  /**
  
   * @param resource IResource
   * @return boolean
   * @see Collection#contains(Object) */
  public boolean contains(IResource resource) {
    return resources.contains(resource);
  }

  /**
   * returns true if any of the resources has in its path the specified type
   * @param type the type of resource to search for
  
   * @return <code>true</code> if a resource of this type exists in any path of any resource in the list. */
  public boolean isTypeInPaths(int type){
    for (IResource resource : resources) {
      try {
        if (resource.isTypeInPath(type)) {
          return true;
        }
      } catch (ResourceException e) {
        // TODO Auto-generated catch block
        logger.error(e);
      }
    }
    return false;
  }
  
  /**
   * Method toList.
   * @return List<IResource>
   */
  public List<IResource> toList() {
    return new ArrayList<IResource>(resources);
  }

  /**
   * Method getResourcesCommonAncestor.
   * @return CmsPath
   */
  public CmsPath getResourcesCommonAncestor() {
    CmsPath commonAncestor = resources.get(0).getPath();
    for (IResource resource : resources) {
      commonAncestor = resource.getPath().getCommonAncestor(commonAncestor);
    }
    return commonAncestor;
  }
  
  /**
   * Method containsExtension.
   * @param exts String[]
   * @return boolean
   */
  public boolean containsExtension(String[] exts) {
    // if our list is empty we will always return false.
    if (resources.isEmpty()) {
      return false;
    }
    
    HashSet<IResource> resourcesSet = new HashSet<IResource>(resources);
    
    for(String ext: exts){
      Iterator<IResource> iterator = resourcesSet.iterator();
      while (iterator.hasNext()) {
        IResource element = (IResource) iterator.next();
        if (element instanceof IFile) {
          IFile theFile = (IFile) element;
          String curExt = RCPUtil.getFileExtension(theFile);
          if( curExt.endsWith(ext) ){
            iterator.remove();
          }
        } else {
          return false;
        }
        
      }
    }
    
    return resourcesSet.isEmpty();
  }
}
