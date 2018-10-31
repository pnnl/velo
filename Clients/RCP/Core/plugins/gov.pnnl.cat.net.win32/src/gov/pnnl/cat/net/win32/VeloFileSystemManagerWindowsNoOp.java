package gov.pnnl.cat.net.win32;

import org.eclipse.swt.internal.win32.OS;

import gov.pnnl.cat.net.IVeloFileSystemManager;

public class VeloFileSystemManagerWindowsNoOp implements IVeloFileSystemManager {
  
  public VeloFileSystemManagerWindowsNoOp() {
    
    // disable clicking sound
    //http://stackoverflow.com/questions/30525518/how-to-disable-the-swt-browser-clicking-sound-when-used-in-eclipse-rcp-applicati
    OS.CoInternetSetFeatureEnabled(OS.FEATURE_DISABLE_NAVIGATION_SOUNDS, OS.SET_FEATURE_ON_PROCESS, true);    

  }

  @Override
  public void mapLocalDrive() {
    // TODO Auto-generated method stub

  }

  @Override
  public void unmapLocalDrive() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isMappedDriveInUse() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isLocalDriveEnabled() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getLocalFilePath(String resourceDisplayPath) {
    // TODO Auto-generated method stub
    return null;
  }

}
