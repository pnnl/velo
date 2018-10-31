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
package gov.pnnl.cat.search.eclipse.search.internal.ui.util;

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchMessages;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * The default exception handler shows an error dialog when one of its handle methods
 * is called. If the passed exception is a <code>CoreException</code> an error dialog
 * pops up showing the exception's status information. For a <code>InvocationTargetException</code>
 * a normal message dialog pops up showing the exception's message. Additionally the exception
 * is written to the platform log.
 * @version $Revision: 1.0 $
 */
public class ExceptionHandler {

	private static ExceptionHandler fgInstance= new ExceptionHandler();
	
	/**
	 * Logs the given exception using the platform's logging mechanism. The exception is
	 * logged as an error with the error code <code>JavaStatusConstants.INTERNAL_ERROR</code>.
	 * @param t The exception to log
	 * @param message The message to be used for teh status
	 */
	public static void log(Throwable t, String message) {
		CatSearchPlugin.log(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, t));
	}
	
	/**
	 * Handles the given <code>CoreException</code>. The workbench shell is used as a parent
	 * for the dialog window.
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, String title, String message) {
		handle(e, CatSearchPlugin.getActiveWorkbenchShell(), title, message);
	}
	
	/**
	 * Handles the given <code>CoreException</code>. 
	 * 
	 * @param e the <code>CoreException</code> to be handled
	 * @param parent the dialog window's parent shell
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(CoreException e, Shell parent, String title, String message) {
		fgInstance.perform(e, parent, title, message);
	}
	
	/**
	 * Handles the given <code>InvocationTargetException</code>. The workbench shell is used 
	 * as a parent for the dialog window.
	 * 
	 * @param e the <code>InvocationTargetException</code> to be handled
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(InvocationTargetException e, String title, String message) {
		handle(e, CatSearchPlugin.getActiveWorkbenchShell(), title, message);
	}
	
	/**
	 * Handles the given <code>InvocationTargetException</code>. 
	 * 
	 * @param e the <code>InvocationTargetException</code> to be handled
	 * @param parent the dialog window's parent shell
	 * @param title the dialog window's window title
	 * @param message message to be displayed by the dialog window
	 */
	public static void handle(InvocationTargetException e, Shell parent, String title, String message) {
		fgInstance.perform(e, parent, title, message);
	}

	//---- Hooks for subclasses to control exception handling ------------------------------------
	
	/**
	 * Method perform.
	 * @param e CoreException
	 * @param shell Shell
	 * @param title String
	 * @param message String
	 */
	protected void perform(CoreException e, Shell shell, String title, String message) {
		CatSearchPlugin.log(e);
		IStatus status= e.getStatus();
		if (status != null) {
			ErrorDialog.openError(shell, title, message, status);
		} else {
			displayMessageDialog(e, e.getMessage(), shell, title, message);
		}
	}

	/**
	 * Method perform.
	 * @param e InvocationTargetException
	 * @param shell Shell
	 * @param title String
	 * @param message String
	 */
	protected void perform(InvocationTargetException e, Shell shell, String title, String message) {
		Throwable target= e.getTargetException();
		if (target instanceof CoreException) {
			perform((CoreException)target, shell, title, message);
		} else {
			CatSearchPlugin.log(e);
			if (e.getMessage() != null && e.getMessage().length() > 0) {
				displayMessageDialog(e, e.getMessage(), shell, title, message);
			} else {
				displayMessageDialog(e, target.getMessage(), shell, title, message);
			}
		}
	}

	//---- Helper methods -----------------------------------------------------------------------

	/**
	 * Method displayMessageDialog.
	 * @param t Throwable
	 * @param shell Shell
	 * @param title String
	 * @param message String
	 */
	public static void displayMessageDialog(Throwable t, Shell shell, String title, String message) {
		fgInstance.displayMessageDialog(t, t.getMessage(), shell, title, message);
	}

	/**
	 * Method displayMessageDialog.
	 * @param t Throwable
	 * @param title String
	 * @param message String
	 */
	public static void displayMessageDialog(Throwable t, String title, String message) {
		displayMessageDialog(t, CatSearchPlugin.getActiveWorkbenchShell(), title, message);
	}
	
	/**
	 * Method displayMessageDialog.
	 * @param t Throwable
	 * @param exceptionMessage String
	 * @param shell Shell
	 * @param title String
	 * @param message String
	 */
	private void displayMessageDialog(Throwable t, String exceptionMessage, Shell shell, String title, String message) {
		StringWriter msg= new StringWriter();
		if (message != null) {
			msg.write(message);
			msg.write("\n\n"); //$NON-NLS-1$
		}
		if (exceptionMessage == null || exceptionMessage.length() == 0)
			msg.write(SearchMessages.ExceptionDialog_seeErrorLogMessage); 
		else
			msg.write(exceptionMessage);
		MessageDialog.openError(shell, title, msg.toString());			
	}	
}
