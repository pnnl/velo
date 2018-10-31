package gov.pnnl.velo.ui.rcp;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.jcraft.jsch.JSch;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.common.rcp.AbstractCatApplicationWorkbenchAdvisor;
import gov.pnnl.velo.uircp.perspectives.Dashboard;

public class ApplicationWorkbenchAdvisor extends AbstractCatApplicationWorkbenchAdvisor {
  private static Logger logger = Logger.getLogger(ApplicationWorkbenchAdvisor.class);

  private static final String PERSPECTIVE_ID = Dashboard.ID;
  
  @Override
  public void initialize(IWorkbenchConfigurer configurer) {
    super.initialize(configurer);
    // Let's not save and restore for now because the UI is in flux and every time we change the UI layout, the
    // user gets workbench errors
    configurer.setSaveAndRestore(false);
  }

  public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    return new ApplicationWorkbenchWindowAdvisor(configurer);
  }

  public String getInitialWindowPerspectiveId() {
    return PERSPECTIVE_ID;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#preShutdown()
   */
  @Override
  public boolean preShutdown() {
    super.preShutdown();
    restoreIniVariables();
    
    //there are too many potental issues with restoring state to the DatasetEditing perspective.  
    //so for now, if that is the perspective that would be restored to, change instead to the defalt one
    IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    if(perspective.getId().equalsIgnoreCase("gov.pnnl.velo.dataset.perspectives.DatasetEditing")){
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closePerspective(perspective, true, false);
      perspective = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(PERSPECTIVE_ID);
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setPerspective(perspective);
    }
    
    return true;
    //return checkDisconnectedJobs();
  } 
  
 

  //	@Override
  public void postStartup() {
    super.postStartup();
    // Redirect JSch logging
    redirectJSchLogging();
   // flagDisconnectedJobs();
  }
  
  private void redirectJSchLogging() {
    final Logger log4jLogger = Logger.getLogger("JSch");
    log4jLogger.setLevel(Level.DEBUG);
    com.jcraft.jsch.Logger jschLogger = new com.jcraft.jsch.Logger() {

      @Override
      public boolean isEnabled(int level) {
        return true; // log everything!
      }

      @Override
      public void log(int level, String message) {
        if(level == com.jcraft.jsch.Logger.INFO){
          log4jLogger.info(message);    
        } else if (level == com.jcraft.jsch.Logger.DEBUG) {
          log4jLogger.debug(message);      
          
        } else if (level == com.jcraft.jsch.Logger.WARN) {
          log4jLogger.warn(message);    
        } else if (level == com.jcraft.jsch.Logger.ERROR ||
            level == com.jcraft.jsch.Logger.FATAL) {
          log4jLogger.error(message);    
        }
      }
      
    };
    JSch.setLogger(jschLogger);
  }

  private void restoreIniVariables() {
    // Save the JVM args to the ini file
    try {
//      -vm
//      C:\Program Files\Java\jdk1.6.0_34\bin\javaw.exe
      String javaHome = System.getenv("JAVA_HOME");
      String javaExePath = javaHome + File.separatorChar + "bin" + File.separatorChar + "javaw.exe";

      String maxMemory = (String)ResourcesPlugin.getVeloGlobalProperties().getProperties().get("jvm.xmx");
      String logFileName = (String)ResourcesPlugin.getVeloGlobalProperties().getProperties().get(CatLogger.SYS_PROP_LOG_FILE_NAME);
      
      String memoryArg = null;
      File installDir = null;
      if(maxMemory != null) {
        memoryArg = maxMemory;
      }

      Location installLocation = Platform.getInstallLocation();
      if(installLocation != null){
        installDir = new File(installLocation.getURL().getFile());
        logger.warn("Velo installation directory = " + installDir.getAbsolutePath());
      } else {
        logger.error("No installation dir found");
      }

      if(memoryArg != null) {
        if(installDir != null){
          String iniFileName = Platform.getProduct().getName() + ".ini";
          File iniFile = new File(installDir, iniFileName);
          if(iniFile.exists()) {
            String contents = org.apache.commons.io.FileUtils.readFileToString(iniFile);
            boolean changed = false;
            if(contents.contains("$MAX_MEMORY")) {
              contents = contents.replace("$MAX_MEMORY", memoryArg);
              changed = true;
            }
            // check for log file param
            if(contents.contains("$LOG_FILE_NAME") && logFileName != null) {
              contents = contents.replace("$LOG_FILE_NAME", logFileName);
              changed = true;
            }
            // check for JRE param
            if(contents.contains(("$JAVA_EXE")) ) {
              contents = contents.replace("$JAVA_EXE", javaExePath);
              changed = true;              
            }
            
            if(changed) {
              org.apache.commons.io.FileUtils.writeStringToFile(iniFile, contents);
            }
          } else {
            logger.error("No " + iniFileName + " file found in installation directory: " + installDir);
          }
        }
      }
    } catch (Throwable e)  {
      logger.error("Failed to save memory to ini", e);
    }

  }
  
}


