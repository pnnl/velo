package gov.pnnl.cat.imageflow.model;

import gov.pnnl.cat.core.resources.IFolder;


public class ImageContainer {

  private IFolder imageFolder;
  private boolean imagesOnly;
  public boolean isImagesOnly() {
    return imagesOnly;
  }

  public void setImagesOnly(boolean imagesOnly) {
    this.imagesOnly = imagesOnly;
  }

  public boolean isIncludeAllSubfolders() {
    return includeAllSubfolders;
  }

  public void setIncludeAllSubfolders(boolean includeAllSubfolders) {
    this.includeAllSubfolders = includeAllSubfolders;
  }

  private boolean includeAllSubfolders;
  
  public ImageContainer(IFolder imageFolder) {
    this(imageFolder, false, false);
  }

  public ImageContainer(IFolder imageFolder, boolean imagesOnly, boolean includeAllSubfolders) {
    super();
    this.imageFolder = imageFolder;
    this.imagesOnly = imagesOnly;
    this.includeAllSubfolders = includeAllSubfolders;
  }

  public void setImageFolder(IFolder imageFolder) {
    this.imageFolder = imageFolder;
  }

  public IFolder getImageFolder() {
    return imageFolder;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if(obj != null && obj.getClass().equals(ImageContainer.class)){
      ImageContainer compare = (ImageContainer)obj;
      return compare.imagesOnly == this.imagesOnly && compare.includeAllSubfolders == this.includeAllSubfolders &&
               compare.imageFolder.getPath().equals(this.imageFolder.getPath());
    }
    return false;
  }
  
  
  
}
