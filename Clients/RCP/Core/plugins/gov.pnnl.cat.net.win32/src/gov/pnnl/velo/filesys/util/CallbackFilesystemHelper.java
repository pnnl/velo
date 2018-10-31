package gov.pnnl.velo.filesys.util;

import eldos.cbfs.CallbackFileSystem;
import eldos.cbfs.ECBFSError;
import eldos.cbfs.ServiceStatus;
import eldos.cbfs.boolRef;
import eldos.cbfs.longRef;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.filesys.callback.FileSystemCachedUploaderThread;
import gov.pnnl.velo.filesys.callback.Handler;
import gov.pnnl.velo.filesys.ui.VeloFilesystemTransfererJFrame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class CallbackFilesystemHelper {
  
  
  public static void main(String... args) throws Exception{
//    String offsetStr = "3";
    
    File xyzFile = new File("C:/data/xyz.txt");
    long offset = xyzFile.length();//Long.parseLong(offsetStr);

//    System.out.println(xyzFile.length());
    File abcFile = new File("C:/data/abc.txt");
    File requestContent = new File("C:/data/def.txt");

    FileChannel fc = new FileOutputStream(abcFile, true).getChannel();

    
//    FileChannel fc = writer.getFileChannel(false);
    fc = fc.truncate(offset);//remove everything from the offset on in case the file has shrunk
    fc.position(offset);
    FileChannel appendFc = new FileInputStream(requestContent).getChannel(); 
    appendFc.transferTo(0, requestContent.length(), fc);  
  }
  
  

  private static final String DEFAULT_REGISTRATION_KEY = "A04F2A4B638C662C09D61B180D3ADF3CF1464DF675139D4D6320D5C265B384ED7FB0893350ADB9695B28FC3FE572214BA60F83323E414ED31059A66BE869167FDC09F61F7CED3AFBF8ADFABBB8A956BF1C7D0A0B88ED3AFBF8ED1ABF1C8A38"; 
      //"DF0720C472FBF940FDAACFAC818E135095ED0C1B75DCFF5D06CB48BD102A2A0F9C9F921B361C1E5540F5E247786D9A3FA83DCE53A83DCE53ACA18AAFCC81AACFA83DCE539CD17A1F6C614A6F4C212EB3F89E";
  public static final String DEFAULT_MOUNT_POINT = "V:;Velo;Velo Virtual Filesystem";
  public static final String DEFULT_DRIVE_LETTER = "V";
  private static final String PRODUCT_NAME = "713CC6CE-B3E2-4fd9-838D-E28F558F6866";

  private Logger logger = CatLogger.getLogger(this.getClass());
  private CallbackFileSystem cbfs;
  
  public File lookupClasspathFiles(String name) {
  try {
      InputStream resource = VeloFilesystemTransfererJFrame.class.getClassLoader().getResourceAsStream(name);
      File file = File.createTempFile(name, "");
      FileUtils.copyInputStreamToFile(resource, file);
      return file;
  } catch (Exception e) {
      throw new RuntimeException("could not lookup files with name: "+name, e);
  }
  
}
  public String getDriveLetter() {
    return DEFULT_DRIVE_LETTER; // TODO: how can we get real drive letter from cbfs?
  }
  
  public boolean isDriverInstalled() {
    CallbackFileSystem cbfs = new CallbackFileSystem();
    cbfs.setRegistrationKey(DEFAULT_REGISTRATION_KEY);

    boolRef installed = new boolRef();
    longRef highVersion = new longRef();
    longRef lowVersion = new longRef();
    ServiceStatus status = new ServiceStatus();
    try {
      cbfs.getModuleStatus(PRODUCT_NAME, CallbackFileSystem.CBFS_MODULE_DRIVER, installed, highVersion, lowVersion, status);
      // if (!installed.getValue()) {
      // System.out.println("driver is not installed");
      // } else {
      // System.out.println("driver version " + highVersion.getValue() + ":" + lowVersion.getValue() + " is installed");
      // System.out.println("driver status: " + status.CurrentState);
      // }
      return installed.getValue();
    } catch (ECBFSError ecbfsError) {
      logger.error("failed to check module status");
      ecbfsError.printStackTrace();
    }
    return false;
  }

  public boolean installDriver() throws Exception {
    CallbackFileSystem cbfs = new CallbackFileSystem();
    cbfs.setRegistrationKey(DEFAULT_REGISTRATION_KEY);

    boolean supportPnP = false;
    int modulesToInstall = CallbackFileSystem.CBFS_MODULE_MOUNT_NOTIFIER_DLL | CallbackFileSystem.CBFS_MODULE_NET_REDIRECTOR_DLL;
    boolRef rebootNeeded = new boolRef();
    File cabFiles = lookupClasspathFiles("cbfs.cab");

    String cabFileName =cabFiles.getAbsolutePath();

    cbfs.install(cabFileName, PRODUCT_NAME, "", supportPnP, false, modulesToInstall, rebootNeeded);
    return true;
  }
  
  public FileSystemCachedUploaderThread createMount() throws ECBFSError{
    FileSystemCachedUploaderThread transferer = new FileSystemCachedUploaderThread();
    Handler handler = new Handler(transferer);
    
    this.cbfs = new CallbackFileSystem(handler);
    cbfs.initialize(PRODUCT_NAME);
    cbfs.setRegistrationKey(DEFAULT_REGISTRATION_KEY);
    // When SerializeCallbacks is true, all callback functions are called sequentially from a single thread. When SerializeCallbacks is false, the callback functions are called from a single thread for the same file (see ParallelProcessingAllowed property for exception), but for different files the callback functions can be called in parallel. The number of parallel threads is deterined by ThreadPoolSize property
    cbfs.setSerializeCallbacks(true);
    //cbfs.setThreadPoolSize(3);// TODO figure out how many threads is a 'good' value

    // Parallel processing speeds up read operations to some extent. When parallel processing is allowed and two or more requests, which don't change filesystem information, are made over the same file, these requests are (given there are worker threads available) sent to the corresponding callback/event handler in parallel. If parallel processing is not allowed, such requests are serialized.
    cbfs.setAllowParallelProcessing(false);
    cbfs.setProcessRestrictionsEnabled(false);
    cbfs.createStorage();

//    // this icon code isn't working even after I rebooted...maybe the filesystem always has to run as admin??
//    if (!cbfs.iconInstalled("veloVFS4")) {
//      try {
//        boolRef rebootNeeded = new boolRef();
//        
//        File icon = lookupClasspathFiles("z-square-icon.ico");
//        
//        cbfs.installIcon(icon.getAbsolutePath(), "veloVFS4", rebootNeeded);
//        logger.debug("installed icon, rebootNeeded " + rebootNeeded.getValue());
//      } catch (Exception e) {
//        logger.debug("unable to install veloVFS icon, not running as admin?");
//      }
//    } else {
//      cbfs.setIcon("veloVFS");
//    }

    // cbfs.disableMetaDataCache(true);
    cbfs.addMountingPoint(DEFAULT_MOUNT_POINT, CallbackFileSystem.CBFS_SYMLINK_NETWORK | CallbackFileSystem.CBFS_SYMLINK_NETWORK_ALLOW_MAP_AS_DRIVE, null); // CBFS_SYMLINK_NETWORK_WRITE_NETWORK_ACCESS

    // (param is timeout) Timeout is specified in milliseconds. The value of 0 (zero) means "timeout is not used", i.e. "callbacks work as long as they need". It's good idea to set timeout to 0 when you do debugging of your callback functions. Remember to set timeout to some reasonable value (for example, 30 seconds) for release version.
    cbfs.mountMedia(0);
    
    return transferer;
  }
  
  public void unMount() throws ECBFSError{
    if (cbfs != null) {
      cbfs.unmountMedia(true);
      try{
      while (cbfs.getMountingPointCount() != 0) {
        cbfs.deleteMountingPoint(0);
      }
      }catch(Exception e){
        logger.error("Error removing mount point", e);
      }
      cbfs.deleteStorage(false);
    }
  }
  
}
