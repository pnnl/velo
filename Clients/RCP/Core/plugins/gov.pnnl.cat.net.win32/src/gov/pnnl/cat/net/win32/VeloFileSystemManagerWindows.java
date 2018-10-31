package gov.pnnl.cat.net.win32;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.net.IVeloFileSystemManager;
import gov.pnnl.velo.filesys.service.VeloFilesystemMapper;

import org.apache.log4j.Logger;

public class VeloFileSystemManagerWindows implements IVeloFileSystemManager {

  private Logger logger = CatLogger.getLogger(VeloFileSystemManagerWindows.class);
  private boolean mappedDriveEnabled = false;
  private String driveLetter = "V";
  private VeloFilesystemMapper driveMapper;


  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#mapLocalDrive()
   */
  @Override
  public void mapLocalDrive() {
    try {

      if(driveMapper == null) {
        driveMapper = new VeloFilesystemMapper() {
          
          @Override
          public void addStatusMessage(String... messages) {
            for(String message : messages) {
              System.out.println(message);
            }

          }
        };
        driveMapper.mount();
        driveLetter = driveMapper.getMappedDriveLetter();
        mappedDriveEnabled = true;
      }

    } catch (RuntimeException e) {
      throw e;
    
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#unmapLocalDrive()
   */
  @Override
  public void unmapLocalDrive() {
    if(mappedDriveEnabled) {
      driveMapper.unMount();
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#isLocalDriveEnabled()
   */
  @Override
  public boolean isLocalDriveEnabled() {
    return mappedDriveEnabled;
  }
  
  @Override
  public boolean isMappedDriveInUse() {
    return driveMapper.isRunning();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.net.IVeloFileSystemManager#getLocalFilePath(java.lang.String)
   */
  @Override
  public String getLocalFilePath(String resourceDisplayPath) {
    String localFilePath =  null;

    if(mappedDriveEnabled) {
      resourceDisplayPath = resourceDisplayPath.replace('/', '\\');
      localFilePath = driveLetter + ":" + resourceDisplayPath;
    }
    return localFilePath;
  }



}
