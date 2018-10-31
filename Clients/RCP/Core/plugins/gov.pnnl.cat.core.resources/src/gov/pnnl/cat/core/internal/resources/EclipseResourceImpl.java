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

import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public abstract class EclipseResourceImpl implements IResource {
  protected IResourceManager mgr;
  protected CmsPath path;
  protected String type;
  private static Map<String, Integer> aspectsToTypes = new HashMap<String, Integer>();

  static{
    //init map of aspect strings to our constant ints
    aspectsToTypes.put(VeloConstants.ASPECT_TAXONOMY_ROOT, IResource.TAXONOMY_ROOT);
    aspectsToTypes.put(VeloConstants.ASPECT_TAXONOMY_FOLDER, IResource.TAXONOMY_FOLDER);
    aspectsToTypes.put(VeloConstants.ASPECT_TAXONOMY_LINK, IResource.TAXONOMY_FILE);
    aspectsToTypes.put(VeloConstants.ASPECT_FAVORITES_ROOT, IResource.FAVORITES_ROOT);
    aspectsToTypes.put(VeloConstants.ASPECT_PERSONAL_LIBRARY_ROOT, IResource.PERSONAL_LIBRARY_ROOT);
    aspectsToTypes.put(VeloConstants.ASPECT_USER_HOME_FOLDER, IResource.USER_HOME_FOLDER);
    aspectsToTypes.put(VeloConstants.ASPECT_TEAM_HOME_FOLDER, IResource.TEAM_HOME_FOLDER);
    aspectsToTypes.put(VeloConstants.ASPECT_PROJECT, IResource.PROJECT);
    aspectsToTypes.put(VeloConstants.ASPECT_CONFIG_ROOT, IResource.CONFIG_ROOT);
  }


  /**
   * Constructor for EclipseResourceImpl.
   * @param path CmsPath
   * @param type QualifiedName
   * @param mgr IResourceManager
   */
  public EclipseResourceImpl(CmsPath path, String type, IResourceManager mgr) {
    this.path = path;
    this.mgr = mgr;
    this.type = type;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getMimetype()
   */
  @Override
  public String getMimetype() {
    String mimetype = getPropertyAsString(VeloConstants.PROP_MIMETYPE);
    return mimetype;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#move(gov.pnnl.velo.model.CmsPath)
   */
  @Override
  public void move(CmsPath destination) throws ResourceException {
    this.mgr.move(this.path, destination);

    // if the move was successful (i.e. we didn't throw any exceptions),
    // then we need to update our path
    this.path = destination;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#delete()
   */
  @Override
  public void delete() throws ResourceException {
    this.mgr.deleteResource(this.path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getParent()
   */
  @Override
  public IResource getParent() throws ResourceException {
    if (path.size() == 0) {
      return null;
    }
    CmsPath parentPath = this.path.getParent();

    return this.mgr.getResource(parentPath);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPath()
   */
  @Override
  public CmsPath getPath() {
    return this.path;
  }

//  /* (non-Javadoc)
//   * @see gov.pnnl.cat.core.resources.IResource#getCifsPath()
//   */
//  @Override
//  public IPath getCifsPath() throws ResourceException {
//    return getResourceManager().getCifsDriveSharePath(getPath());
//  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getName()
   */
  @Override
  public String getName() {
    return path.last().getName();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertyAsString(java.lang.String)
   */
  @Override
  public String getPropertyAsString(String key) throws ResourceException {
    return mgr.getProperty(path, key, false);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertyAsResource(java.lang.String)
   */
  @Override
  public IResource getPropertyAsResource(String property) throws ResourceException, AccessDeniedException {
    return this.mgr.getPropertyAsResource(path, property);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertiesAsString(java.util.List)
   */
  @Override
  public List<String> getPropertiesAsString(List<String> keys) {
    return this.mgr.getPropertiesAsString(this.path, keys);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertyAsDate(java.lang.String)
   */
  @Override
  public Calendar getPropertyAsDate(String key) throws ParseException {
    return this.mgr.getPropertyAsDate(this.path, key);
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty(String key, String value) {
    this.mgr.setProperty(this.path, key, value);
  }


  /**
   * Method getResourceManager.
   * @return IResourceManager
   */
  protected IResourceManager getResourceManager() {
    return this.mgr;
  }


  /**
   * Method getAspectsType.
   * @return int
   */
  protected int getAspectsType() {
    int aspectsTypes = 0;
    List<String> aspects = getAspects();

    if (aspects == null){
      return aspectsTypes;
    }

    for (String aspect : aspects) {
      if(aspectsToTypes.containsKey(aspect)){
        aspectsTypes |= aspectsToTypes.get(aspect);
      }
    }
    return aspectsTypes;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o != null && o instanceof EclipseResourceImpl) {
      EclipseResourceImpl resource = (EclipseResourceImpl) o;
      return (resource.getPath() == null && getPath() == null) ||
             (resource.getPath() != null && resource.getPath().equals(getPath()));
    }

    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return path.hashCode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return path.toDisplayString();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#isLink()
   */
  @Override
  public boolean isLink() {
    return false;
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#isType(int)
   */
  @Override
  public boolean isType(int type) {
    return (type & getType()) == type;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#isTypeInPath(int)
   */
  @Override
  public boolean isTypeInPath(int type) {
    //if this isType return true
    //else return isTypeInPath(getParent)
    //hopefully all parents of this resource will already bein the cache
    
    if(isType(type)){
      return true;
    }else if(this.getParent() == null){
      //means we've checked all parents and have not matched, so this type is not in any ancestor
      return false;
    }else{
      return this.getParent().isTypeInPath(type);
    }
    
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(IResource resource) {
    return path.compareTo(resource.getPath());
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#hasAspect(java.lang.String)
   */
  @Override
  public boolean hasAspect(String aspect) {
    return mgr.getAspects(getPath(), false).contains(aspect);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getAspects()
   */
  @Override
  public List<String> getAspects() {
    return mgr.getAspects(getPath(), false);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#hasMimetype(java.lang.String)
   */
  @Override
  public boolean hasMimetype(String mimetype) {
    String mimetypeProp = getPropertyAsString(VeloConstants.PROP_MIMETYPE);
    return (mimetypeProp != null && mimetypeProp.equals(mimetype));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getNodeType()
   */
  @Override
  public String getNodeType() {
    return this.type;
  }
}
