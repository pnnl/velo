package gov.pnnl.velo.filesys.ui;

import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.filesys.callback.FileSystemCachedUploaderThread;
import gov.pnnl.velo.filesys.util.CallbackFilesystemHelper;
import gov.pnnl.velo.util.SpringContainerInitializer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class VeloFilesystemTransfererJFrame extends JFrame implements ActionListener {

  
  private static final long serialVersionUID = 1L;

  private UsernamePassword usernamePassword = new UsernamePassword("", "");

  private MonitorForFilesToUpload monitorerForFileUploads;

  private FileSystemCachedUploaderThread transferer;
  private CallbackFilesystemHelper cbfsHelper;
  
  private Thread cacheThread;
  
  private Logger logger = CatLogger.getLogger(this.getClass());

  
  
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new VeloFilesystemTransfererJFrame();
      }
    });
    
  }
  
 

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
          try {
            CmsServiceLocator.getSecurityManager().login(usernamePassword.getUsername(), usernamePassword.getPassword());
          } catch (Exception e) {
            publishMessage("Unable to authenticate to server.  Did you use the correct username and password?");
            usernamePassword = null;
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
            throw e;
          }
          publishMessage("Successfully authenticated to server.");


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
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        throw e;
      }

      startButton.setEnabled(true);
      stopButton.setEnabled(false);
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

  public VeloFilesystemTransfererJFrame() {
    super("Velo Virtual Filesystem");
    
    SpringContainerInitializer.loadBeanContainerFromClasspath(null);

    LogManager.resetConfiguration();
    
    cbfsHelper = new CallbackFilesystemHelper();
    
    //"C:/Eclipse/workspaces/ESIOS/EldosTest/src/gov/pnnl/velo/filesys/callback/log4j.xml"
    File lo4jFiles = cbfsHelper.lookupClasspathFiles("log4j.xml");
    
    DOMConfigurator.configure(lo4jFiles.getAbsolutePath());
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent winEvt) {
        if(transferer != null){ //will be null if it was never started
          unMount();
        }
        System.exit(0);
      }
    });

    initComponents();
    // Display the window.
    
    startButton.setEnabled(true);
    stopButton.setEnabled(false);
    
    pack();
    setVisible(true);
  }
  
 

  public void actionPerformed(ActionEvent e) {
    if ("Mount" == e.getActionCommand() && !promptForCredsIfNeeded()) {
      JOptionPane.showMessageDialog(this, "Provide username and password for Velo Server.");
    } else if ("Mount" == e.getActionCommand()) {
      startButton.setEnabled(false);
      stopButton.setEnabled(true);
      (this.monitorerForFileUploads = new MonitorForFilesToUpload()).execute();
    } else if ("Unmount" == e.getActionCommand()) {
      unMount();
    }
  }

  private void unMount() {
    if(transferer.isRunning() && (transferer.isUploading() || transferer.isUploadsPending())){
      int selection = JOptionPane.showConfirmDialog(this, "Upload is in progress.  If you unmount now any pending uploads will be canceled.  Do you want to unmount still?");
      if(selection == JOptionPane.CANCEL_OPTION || selection == JOptionPane.NO_OPTION){
        return;
      }
    }
    
    Thread stopThread = new Thread(new Runnable() {
      public void run() {
        stopButton.setEnabled(false);
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
        startButton.setEnabled(true);
        
      }
    });
    stopThread.start();
  }

  public boolean promptForCredsIfNeeded() {
    if (usernamePassword == null || usernamePassword.getPassword().isEmpty() || usernamePassword.getUsername().isEmpty()) {
      usernamePassword = collectUsernamePassword();
      System.out.println("collected username and pass");
    }
    return usernamePassword != null;
  }

  public void addStatusMessage(String... messages) {
    for (String message : messages) {
      jTextArea1.append(message + "\n");
    }
    // this should make the text area scoll to the end
    jTextArea1.select(jTextArea1.getHeight() + 100000, 0);
    jTextArea1.repaint();
    jTextArea1.revalidate();
  }

  private UsernamePassword collectUsernamePassword() {
    PasswordDialog p = new PasswordDialog(null, "Enter username and password for Velo server.");
    if (p.showDialog()) {
      return new UsernamePassword(p.getName(), p.getPass());
    }
    return null;
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jPanel1 = new javax.swing.JPanel();
    startButton = new javax.swing.JButton();
    stopButton = new javax.swing.JButton();
    jLabel7 = new javax.swing.JLabel();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();

    startButton.addActionListener(this);
    stopButton.addActionListener(this);

    setLayout(new java.awt.GridBagLayout());

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 1;
    // gridBagConstraints.gridy = 3;
    // gridBagConstraints.gridwidth = 7;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 1;
    // gridBagConstraints.gridy = 5;
    // gridBagConstraints.gridwidth = 4;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 12;
    // gridBagConstraints.gridy = 5;
    // gridBagConstraints.gridwidth = 14;
    // gridBagConstraints.gridheight = 2;
    // gridBagConstraints.ipadx = 356;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(18, 13, 0, 10);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
    jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    getContentPane().add(jPanel1, gridBagConstraints);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 12;
    // gridBagConstraints.gridy = 3;
    // gridBagConstraints.gridheight = 2;
    // gridBagConstraints.ipadx = 5;
    // gridBagConstraints.ipady = 2;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(17, 13, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 1;
    // gridBagConstraints.gridy = 7;
    // gridBagConstraints.gridwidth = 5;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 1;
    // gridBagConstraints.gridy = 9;
    // gridBagConstraints.gridwidth = 3;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 12;
    // gridBagConstraints.gridy = 7;
    // gridBagConstraints.gridheight = 2;
    // gridBagConstraints.ipadx = 5;
    // gridBagConstraints.ipady = 2;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(17, 13, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 12;
    // gridBagConstraints.gridy = 9;
    // gridBagConstraints.gridwidth = 14;
    // gridBagConstraints.gridheight = 2;
    // gridBagConstraints.ipadx = 356;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(18, 13, 0, 10);

    startButton.setText("Mount");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 10, 12, 0);
    getContentPane().add(startButton, gridBagConstraints);

    stopButton.setText("Unmount");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 6, 12, 0);
    getContentPane().add(stopButton, gridBagConstraints);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 7;
    // gridBagConstraints.gridy = 14;
    // gridBagConstraints.gridwidth = 8;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(18, 14, 12, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 1;
    // gridBagConstraints.gridy = 2;
    // gridBagConstraints.gridwidth = 13;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(18, 10, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 1;
    // gridBagConstraints.gridy = 1;
    // gridBagConstraints.gridwidth = 11;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(13, 10, 0, 0);

    // gridBagConstraints = new java.awt.GridBagConstraints();
    // gridBagConstraints.gridx = 1;
    // gridBagConstraints.gridy = 11;
    // gridBagConstraints.gridwidth = 25;
    // gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    // gridBagConstraints.ipadx = 496;
    // gridBagConstraints.ipady = 9;
    // gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    // gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 10);

    jLabel7.setText("Status:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
    getContentPane().add(jLabel7, gridBagConstraints);

    jTextArea1.setColumns(1);
    jTextArea1.setRows(5);
    jScrollPane1.setViewportView(jTextArea1);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.gridwidth = 25;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 474;
    gridBagConstraints.ipady = 154;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
    getContentPane().add(jScrollPane1, gridBagConstraints);
  }// </editor-fold>
   // Variables declaration - do not modify

  private javax.swing.JButton startButton;

  private javax.swing.JButton stopButton;

  private javax.swing.JLabel jLabel7;

  private javax.swing.JPanel jPanel1;

  private javax.swing.JScrollPane jScrollPane1;

  private javax.swing.JTextArea jTextArea1;
  // End of variables declaration
}
