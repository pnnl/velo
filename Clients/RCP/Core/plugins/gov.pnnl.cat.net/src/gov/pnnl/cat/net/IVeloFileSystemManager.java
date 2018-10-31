package gov.pnnl.cat.net;


public interface IVeloFileSystemManager {

  /**
   * Attempt to connect mapped drive.  If mapping successful, then
   * set mappedDriveEnabled to true.
   * @throws RuntimeExcpetion if drive mapping failed
   */
  public void mapLocalDrive();

  /**
   * On exit, remove the local drive mapping
   */
  public void unmapLocalDrive();
  
  public boolean isMappedDriveInUse();

  /**
   * Report the status of the drive mapping
   * @return
   */
  public boolean isLocalDriveEnabled();

  /**
   * @param resourceDisplayPath - display path must not contain namespaces and
   * must not include company_home segment
   * @return
   */
  public String getLocalFilePath(String resourceDisplayPath);


}
