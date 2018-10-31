/**
 * 
 */
package gov.pnnl.cat.ui.rcp.views.databrowser.model;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourceNotFoundException;
import gov.pnnl.velo.model.CmsPath;

/**
 * Decorate a resource so it can show up in the favorites container.  We need to know if it is a top-level favorite
 * so we can add the remove action
 * @author D3K339
 *
 */
public class FavoritesFolderWrapper implements IFolder, IFavoriteFolder {
  private IFolder resource;
  
  public FavoritesFolderWrapper(IFolder resource) {
    this.resource = resource;
  }
  
  public IFolder getWrappedResource() {
    return this.resource;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(IResource o) {
    return resource.compareTo(o);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getParent()
   */
  @Override
  public IResource getParent() throws ResourceException, ResourceNotFoundException {
    return resource.getParent();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPath()
   */
  @Override
  public CmsPath getPath() {
    return resource.getPath();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getName()
   */
  @Override
  public String getName() {
    return resource.getName();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertyAsString(java.lang.String)
   */
  @Override
  public String getPropertyAsString(String key) {
    return resource.getPropertyAsString(key);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertyAsResource(java.lang.String)
   */
  @Override
  public IResource getPropertyAsResource(String property) {
    return resource.getPropertyAsResource(property); 
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertiesAsString(java.util.List)
   */
  @Override
  public List<String> getPropertiesAsString(List<String> keys) {
    return resource.getPropertiesAsString(keys);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getPropertyAsDate(java.lang.String)
   */
  @Override
  public Calendar getPropertyAsDate(String key) throws ParseException {
    return resource.getPropertyAsDate(key);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty(String key, String value) {
    resource.setProperty(key, value);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#move(gov.pnnl.velo.model.CmsPath)
   */
  @Override
  public void move(CmsPath destination) {
   resource.move(destination);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#delete()
   */
  @Override
  public void delete() {
    resource.delete();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#isLink()
   */
  @Override
  public boolean isLink() {
    return resource.isLink();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#isType(int)
   */
  @Override
  public boolean isType(int type) {
    return resource.isType(type);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#isTypeInPath(int)
   */
  @Override
  public boolean isTypeInPath(int type) {
    return resource.isTypeInPath(type);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getType()
   */
  @Override
  public int getType() {
    return resource.getType();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getMimetype()
   */
  @Override
  public String getMimetype() {
    return resource.getMimetype();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getNodeType()
   */
  @Override
  public String getNodeType() {
    return resource.getNodeType();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#hasAspect(java.lang.String)
   */
  @Override
  public boolean hasAspect(String aspect) {
    return resource.hasAspect(aspect);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#getAspects()
   */
  @Override
  public List<String> getAspects() {
    return resource.getAspects();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResource#hasMimetype(java.lang.String)
   */
  @Override
  public boolean hasMimetype(String mimetype) {
    return resource.hasMimetype(mimetype);
  }

  @Override
  public List<IResource> getChildren() {
    return resource.getChildren();
  }

  @Override
  public long getSize() {
    return resource.getSize();
  }

  @Override
  public int hashCode() {
    return resource.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return resource.equals(obj);
  }
  
  

}
