/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.velo.core.util;

import gov.pnnl.cat.core.resources.ResourcesPlugin;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorReporter;

/**
 */
public class ToolErrorHandler {

  private static final Logger logger = Logger.getLogger(ToolErrorHandler.class);


  /**
   * Method handleError.
   * @param message String
   * @param exception Throwable
   * @param showToUser boolean
   */
  public static void handleError(final String message, final Throwable exception, boolean showToUser) {
    handleError(message, exception, showToUser, null);
  }

  /**
   * Method handleError.
   * @param message String
   * @param exception Throwable
   * @param showToUser boolean
   * @param component Component
   */
  public static void handleError(final String message, final Throwable exception, boolean showToUser, final Component component) {

    // log error
    logger.error("Exception Handled! " + message, exception);
    
    // Launch Swing or SWT dialog depending on current thread
    if (ThreadUtils.isAwtThread()) {
      // AWT (Swing) thread - launch JXErrorPane dialog
    	if (showToUser) {
	      SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	          showException(component, message, exception, true);
	        }
	      });
	    }
    } else {
      // SWT thread - launch SWT dialog
    	if (showToUser) {
	      Display.getDefault().asyncExec(new Runnable() {
	        @Override
	        public void run() {
	          showException(null, message, exception, false);
	        }
	      });
	    }
    }
  }

  /**
   * Shows error to user.
  
   * @param component Component
   * @param message String
   * @param exception Throwable
   * @param isAwt boolean
   */
  private static void showException(Component component, String message, Throwable exception, boolean isAwt)  {
    
    // First try to capture screen shot before error dialog is popped up
//    File screenShotFile = null;
   
//    try {
//      Robot robot = new Robot();
//      BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
//
//      SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh.mm.ss");
//      File akunaFolder = org.akuna.ui.tools.util.FileUtils.getAkunaFolder();
//      screenShotFile = new File( akunaFolder, "akunaScreenShot " + dateFormat.format( new Date() ) + ".jpg");
//      screenShotFile.deleteOnExit();
//      ImageIO.write(screenShot, "JPG", screenShotFile);
//      
//      attachments.add(screenShotFile);
//    } catch (Throwable e) {
//      e.printStackTrace();
//    }
    
    // now prepare error dialog
    String exceptionMsg = "No message available.";
    if(exception != null) {
      exceptionMsg = exception.getMessage();
    }
    message = message == null ? exceptionMsg: message;
    
    String stackTrace = "";
    String cause = "";
    if(exception != null) {
      Throwable rootCause = ExceptionUtils.getRootCause(exception);
      cause = rootCause == null ? "Error: "+ exceptionMsg : rootCause.toString(); 
      stackTrace = ExceptionUtils.getStackTrace(exception);
    }
    message = message + "\n\nRoot Cause:  "+ cause;
    
    final ErrorInfo info = new ErrorInfo("Error", message, stackTrace, "error", exception, Level.SEVERE, null);
    // Log error
    logger.error("Exception Occured!", info.getErrorException());
    
    // Set appropriate dialog
    if (isAwt) {
	    JXErrorPane awtErrorPane = new JXErrorPane();
	    awtErrorPane.setErrorInfo(info);
	    
	    // NOTE:  This sets the modality to modeless, which means it may get hidden behind another panel. 
	    // Without this setting, the Akuna main window will be brought to the front when closed.
	    final JDialog dialog = JXErrorPane.createDialog(component, awtErrorPane);
	    dialog.setLocationRelativeTo(component);
	    //dialog.setModalityType(ModalityType.MODELESS);    
	
	    awtErrorPane.addPropertyChangeListener(new PropertyChangeListener() {
	      @Override
	      public void propertyChange(PropertyChangeEvent e) {
	        if (e.getPropertyName().equals("value")) {
	          dialog.dispose();
	        }
	      }
	    });
	
	    awtErrorPane.setErrorReporter(new ErrorReporter() {
	
	      @Override
	      public void reportError(ErrorInfo info) throws NullPointerException {
	        openEmailDialog(info);
	        dialog.dispose();
	      }
	    });
	    dialog.setVisible(true);
    }
    else {
    	Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					// create dialog here but let the SWTErrorDialog class handle everything
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					// make sure the shell gets the focus in case it is hidden by swing window
					shell.forceActive();
					SWTErrorDialog dialog = new SWTErrorDialog(shell, info);					
					dialog.open();
				}
			});
    }
  }
  
  /**
   * Method openEmailDialog.
   * @param exception Throwable
   */
  public static void openEmailDialog(ErrorInfo errorInfo) {
    try {
      
      // Send email
      final String from = ResourcesPlugin.getSecurityManager().getUsername();
      String sub = Platform.getProduct().getName().toUpperCase() + " BUG: ";
      if(errorInfo != null && errorInfo.getErrorException() != null) {
        sub += errorInfo.getErrorException().toString();
      }
      final String subject = sub;
      String errorMessage = "";
      if(errorInfo != null) {
        errorMessage = errorInfo.getDetailedErrorMessage();
      }
      
      EmailForm.openEmailDialog(null, from, subject, errorMessage, null);
            
      
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
