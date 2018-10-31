package gov.pnnl.velo.filesys.service;

import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.filesys.callback.FileSystemCachedUploaderThread;
import gov.pnnl.velo.filesys.ui.PasswordDialog;
import gov.pnnl.velo.filesys.ui.UsernamePassword;
import gov.pnnl.velo.filesys.util.CallbackFilesystemHelper;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

public abstract class VeloFilesystemMapper {

  private MonitorForFilesToUpload monitorerForFileUploads;

  private FileSystemCachedUploaderThread transferer;
  private CallbackFilesystemHelper cbfsHelper;
  
  private Thread cacheThread;
  
  private Logger logger = CatLogger.getLogger(this.getClass());
  
  //this inner class is what does all the work
  private class MonitorForFilesToUpload extends SwingWorker<Void, String> {
    @Override
    protected void process(List<String> chunks) {
      addStatusMessage(chunks.toArray(new String[chunks.size()]));
    }

    @Override
    protected Void doInBackground() throws Exception {
//      CallbackFileSystem cbfs = null;
      try {
        try {
          // first make sure the required dll's and drivers are installed
          logger.debug("path " + new File("test").getAbsolutePath());
          logger.debug("java.library.path: " + System.getProperty("java.library.path"));
          
          if (!cbfsHelper.isDriverInstalled()) {
            publishMessage("Installing filesystem driver.");
            logger.debug("Installing filesystem driver.");
            if (!cbfsHelper.installDriver()) {
              publishMessage("Unable to install filesystem driver.  Drive cannot be mapped.");
              logger.error("Unable to install filesystem driver.  Drive cannot be mapped.");
              cancel(true);
              return null;
            }
          }
          logger.debug("driver installed.");


//          transferer = new Cache();
//          Handler handler = new Handler(transferer);

          
//          CallbackFileSystem cbfs = new CallbackFileSystem(handler);
//          cbfs.initialize(PRODUCT_NAME);
//          cbfs.setRegistrationKey(DEFAULT_REGISTRATION_KEY);
//          // When SerializeCallbacks is true, all callback functions are called sequentially from a single thread. When SerializeCallbacks is false, the callback functions are called from a single thread for the same file (see ParallelProcessingAllowed property for exception), but for different files the callback functions can be called in parallel. The number of parallel threads is deterined by ThreadPoolSize property
//          cbfs.setSerializeCallbacks(false);
//          cbfs.setThreadPoolSize(3);// TODO figure out how many threads is a 'good' value
//
//          // Parallel processing speeds up read operations to some extent. When parallel processing is allowed and two or more requests, which don't change filesystem information, are made over the same file, these requests are (given there are worker threads available) sent to the corresponding callback/event handler in parallel. If parallel processing is not allowed, such requests are serialized.
//          cbfs.setAllowParallelProcessing(true);
//          cbfs.setProcessRestrictionsEnabled(false);
//          cbfs.createStorage();

//          // this icon code isn't working even after I rebooted...maybe the filesystem always has to run as admin??
//          if (!cbfs.iconInstalled("veloVFS4")) {
//            try {
//              boolRef rebootNeeded = new boolRef();
//              
//              File icon = lookupClasspathFiles("z-square-icon.ico");
//              
//              cbfs.installIcon(icon.getAbsolutePath(), "veloVFS4", rebootNeeded);
//              logger.debug("installed icon, rebootNeeded " + rebootNeeded.getValue());
//            } catch (Exception e) {
//              logger.debug("unable to install veloVFS icon, not running as admin?");
//            }
//          } else {
//            cbfs.setIcon("veloVFS");
//          }
//
//          // cbfs.disableMetaDataCache(true);
//          cbfs.addMountingPoint(DEFAULT_MOUNT_POINT, CallbackFileSystem.CBFS_SYMLINK_NETWORK | CallbackFileSystem.CBFS_SYMLINK_NETWORK_ALLOW_MAP_AS_DRIVE, null); // CBFS_SYMLINK_NETWORK_WRITE_NETWORK_ACCESS
//
//          // (param is timeout) Timeout is specified in milliseconds. The value of 0 (zero) means "timeout is not used", i.e. "callbacks work as long as they need". It's good idea to set timeout to 0 when you do debugging of your callback functions. Remember to set timeout to some reasonable value (for example, 30 seconds) for release version.
//          cbfs.mountMedia(0);
          
          transferer = cbfsHelper.createMount();
          publishMessages(transferer);

          transferer.logMessage("Sucessfully mounted drive");

          cacheThread = new Thread(transferer);
          cacheThread.start();

          // here's the meat of this swingworker
          while (!isCancelled()) {
            Thread.sleep(1000);
            publishMessages(transferer);
          }

          transferer.setCanceled(true);
        } catch(Exception e){
          logger.error("Error mounting filesystem", e);
//          publishMessage("Unexpected error occurred: " + e.getMessage());
         throw e;
        } finally {
          cbfsHelper.unMount();
        }
      } catch (Exception e) {
        e.printStackTrace();
        publishMessage("Unexpected error occurred: " + e.getMessage());
        publishMessages(transferer);
        throw e;
      }

      return null;
    }

    private void publishMessages(FileSystemCachedUploaderThread transferer) {
      List<String> messages = transferer.getMessages();
      for (String message : messages) {
        publish(message);
      }
    }

    private void publishMessage(String message) {
      publish(FileSystemCachedUploaderThread.messageDateFormat.format(new Date()) + " " + message);
    }

  }

  public VeloFilesystemMapper() {
    cbfsHelper = new CallbackFilesystemHelper();    
  }
  
  public String getMappedDriveLetter() {
    return cbfsHelper.getDriveLetter();
  }
   
  public void mount() {
    (this.monitorerForFileUploads = new MonitorForFilesToUpload()).execute();
    
  }

  public boolean isRunning() {
    return transferer.isRunning() && (transferer.isUploading() || transferer.isUploadsPending());
  }
  
  public void unMount() {
    
    Thread stopThread = new Thread(new Runnable() {
      public void run() {
        addStatusMessage(FileSystemCachedUploaderThread.messageDateFormat.format(new Date()) + " " + "Unmounting...please wait");
        transferer.setCanceled(true);
        if(cacheThread != null){
          try {
            cacheThread.join();
          } catch (InterruptedException e1) {}      
          //getting this message at the same time as the "__ thread stopped" so sleep this thread very slightly to put the messages in the correct order
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) { }
        }
        addStatusMessage(FileSystemCachedUploaderThread.messageDateFormat.format(new Date()) + " " + "Unmount completed.");
        if (monitorerForFileUploads != null) {
          monitorerForFileUploads.cancel(false);
        }
        monitorerForFileUploads = null;
        
      }
    });
    stopThread.start();
  }

  public abstract void addStatusMessage(String... messages);

}
