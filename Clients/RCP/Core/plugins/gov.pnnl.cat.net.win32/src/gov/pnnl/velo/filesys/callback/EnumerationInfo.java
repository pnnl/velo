package gov.pnnl.velo.filesys.callback;

import gov.pnnl.cat.core.resources.IResource;

import java.util.UUID;

class EnumerationInfo {
  // will have either an IResource or a UUID (uuid when its only in cache)
  private IResource resource;

  private UUID id;

  private int index;

  public EnumerationInfo() {
    setId(UUID.randomUUID());
    setIndex(0);
    setResource(null);
  }

  public UUID getId() {
    return id;
  }

  @Override
  public String toString() {
    return "EnumInfo{" + "id=" + getId() + ", resource=" + getResource() + ", Index=" + getIndex() + '}';
  }

  public IResource getResource() {
    return resource;
  }

  public void setResource(IResource resource) {
    this.resource = resource;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setId(UUID id) {
    this.id = id;
  }

}
