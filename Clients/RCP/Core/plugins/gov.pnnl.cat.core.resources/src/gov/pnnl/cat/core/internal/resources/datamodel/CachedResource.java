package gov.pnnl.cat.core.internal.resources.datamodel;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.model.Resource;

public class CachedResource extends Resource {
  
  private IResource handle;
  
  public CachedResource(IResource handle, Resource rawResource) {
    super(rawResource);
    this.handle = handle;
  }

  /**
   * @return the handle
   */
  public IResource getHandle() {
    return handle;
  }
  
}
