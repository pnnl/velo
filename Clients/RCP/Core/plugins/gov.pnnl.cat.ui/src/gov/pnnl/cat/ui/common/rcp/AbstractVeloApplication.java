package gov.pnnl.cat.ui.common.rcp;

import gov.pnnl.cat.logging.CatLogger;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


/**
 * This class controls all aspects of the application's execution
 */
public abstract class AbstractVeloApplication implements IApplication {
  private static final String WARNING_NOTICE = "You are about to access a U.S. " + "Government computer/information system.  Access to this system is " + "restricted to authorized users only.  Unauthorized access, use, or " + "modification of this computer system or of the data contained herein, or " + "in transit to/from this system, may constitute a violation of Title 18, United " + "States Code, Section 1030 and other federal or state criminal and civil laws.  " + "These systems and equipment are subject to monitoring to ensure proper performance " + "of applicable security features or procedures.  Such monitoring may result in the " + "acquisition, recording and analysis of all data being communicated, transmitted, " + "processed or stored in this system by a user.  If monitoring reveals possible " + "misuse or criminal activity, notice of such may be provided to supervisory personnel " + "and law enforcement officials as evidence.\n\n" + "Anyone who accesses a Federal computer system without authorization or exceeds their " + "access authority, and by any means of such conduct obtains, alters, damages, destroys, " + "or discloses information, or prevents authorized use of information on the computer, " + "may be subject to fine or imprisonment or both.";
  
  protected abstract boolean showGovernmentWarningNotice();
  
  protected abstract AbstractCatApplicationWorkbenchAdvisor getWorkbenchAdvisor();
  
  /* (non-Javadoc)
   * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
   */
  public Object start(IApplicationContext context) throws Exception {
    
    // Redirect system output to Log file
    CatLogger.redirectSystemOutput();
    
    Display display = PlatformUI.createDisplay();
    
    try {
      
      // If user passed username/password via VM args (i.e., for unit testing)
      // then don't pop up the government warning
      final String name = System.getProperty("velo.username");
      final String pass = System.getProperty("velo.password");
      if (name == null && pass == null && showGovernmentWarningNotice()) {
        boolean confirm = MessageDialog.openConfirm(null, "Warning Notice", WARNING_NOTICE);

        if(!confirm){
          return IApplication.EXIT_OK;
        }
      }
      int returnCode = PlatformUI.createAndRunWorkbench(display, getWorkbenchAdvisor());
      if (returnCode == PlatformUI.RETURN_RESTART) {
        return IApplication.EXIT_RESTART;
      }
      return IApplication.EXIT_OK;
    } finally {
      display.dispose();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.equinox.app.IApplication#stop()
   */
  public void stop() {  

    if (!PlatformUI.isWorkbenchRunning())
      return;
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final Display display = workbench.getDisplay();
    display.syncExec(new Runnable() {
      public void run() {
        if (!display.isDisposed())
          workbench.close();
      }
    });
  }
}
