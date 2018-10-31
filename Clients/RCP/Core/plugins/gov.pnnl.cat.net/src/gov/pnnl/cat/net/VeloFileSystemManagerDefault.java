package gov.pnnl.cat.net;

import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Logger;

/**
 * Simple class for connecting to local mapped Velo drive, if it exists.
 * @author D3K339
 *
 */
public class VeloFileSystemManagerDefault implements IVeloFileSystemManager {
  private Logger logger = CatLogger.getLogger(VeloFileSystemManagerDefault.class);
  private boolean mappedDriveEnabled = false;
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#mapLocalDrive()
   */
  @Override
  public void mapLocalDrive() {
    System.out.println("");
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#isMappedDriveInUse()
   */
  @Override
  public boolean isMappedDriveInUse() {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#unmapLocalDrive()
   */
  @Override
  public void unmapLocalDrive() {

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#isLocalDriveEnabled()
   */
  @Override
  public boolean isLocalDriveEnabled() {
    return mappedDriveEnabled;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#getLocalFilePath(java.lang.String)
   */
  @Override
  public String getLocalFilePath(String resourceDisplayPath) {
    return null;
  }

}
