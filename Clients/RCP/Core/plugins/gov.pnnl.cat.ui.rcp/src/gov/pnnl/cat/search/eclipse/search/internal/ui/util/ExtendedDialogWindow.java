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

import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchMessages;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 */
public abstract class ExtendedDialogWindow extends Dialog implements IRunnableContext {
	
	private Control fContents;
	private Button fCancelButton;
	private List fActionButtons;
	// The number of long running operation executed from the dialog.	
	private long fActiveRunningOperations;

	// The progress monitor
	private boolean fUseEmbeddedProgressMonitorPart;
	private ProgressMonitorPart fProgressMonitorPart;
	private MessageDialog fWindowClosingDialog;
	private static final String FOCUS_CONTROL= "focusControl"; //$NON-NLS-1$
	private Cursor fWaitCursor;
	private Cursor fArrowCursor;


	/**
	 * Constructor for ExtendedDialogWindow.
	 * @param shell Shell
	 */
	public ExtendedDialogWindow(Shell shell) {
		super(shell);
		fActionButtons= new ArrayList();
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	//---- Hooks to reimplement in subclasses -----------------------------------
	
	/**
	 * @param enable Use the embedded progress monitor part
	 */
	public void setUseEmbeddedProgressMonitorPart(boolean enable) {
		fUseEmbeddedProgressMonitorPart= enable;
	}
	
	/**
	 * Hook called when the user has pressed the button to perform
	 * the dialog's action. If the method returns <code>false</code>
	 * the dialog stays open. Otherwise the dialog is going to be closed.
	 * @param buttonId Id of the botton activated
	
	 * @return If the method returns <code>false</code>
	 * the dialog stays open. */
	protected boolean performAction(int buttonId) {
		return true;
	}
	 
	/**
	 * Hook called when the user has pressed the button to cancel
	 * the dialog. If the method returns <code>false</code> the dialog 
	 * stays open. Otherwise the dialog is going to be closed.
	
	 * @return If the method returns <code>false</code>
	 * the dialog stays open. */
	protected boolean performCancel() {
		return true;
	}
	 
	//---- UI creation ----------------------------------------------------------

	/**
	 * Create the page area.
	 * @param parent The parent composite
	
	 * @return The created control */
	protected abstract Control createPageArea(Composite parent); 
	 
	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * Subclasses may override.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fCancelButton= createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/**
	 * Method createActionButton.
	 * @param parent Composite
	 * @param id int
	 * @param label String
	 * @param defaultButton boolean
	 * @return Button
	 */
	protected Button createActionButton(Composite parent, int id, String label,
			boolean defaultButton) {
		Button actionButton= createButton(parent, id, label, defaultButton);
		fActionButtons.add(actionButton);
		return actionButton;
	}
	 
	/**
	 * Creates the layout of the extended dialog window.
	 * 	@param parent The parent composite
	
	 * @return The created control */
	protected Control createDialogArea(Composite parent) {
		Composite result= (Composite) super.createDialogArea(parent);
		
		fContents= createPageArea(result);
		fContents.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (fUseEmbeddedProgressMonitorPart) {
			// Insert a progress monitor
			fProgressMonitorPart= new ProgressMonitorPart(result, new GridLayout(), SWT.DEFAULT);
			fProgressMonitorPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fProgressMonitorPart.setVisible(false);
			applyDialogFont(fProgressMonitorPart);
		}

		Label separator= new Label(result, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return result;
	}

	/**
	 * Method buttonPressed.
	 * @param buttonId int
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case IDialogConstants.CANCEL_ID:
				if (fActiveRunningOperations == 0)
					close();
				break;	
			default:
				if (performAction(buttonId))
					close();
		}
	}
	
	//---- Setters and Getters --------------------------------------------------
	
	/**
	 * Set the enable state of the perform action button.
	 * @param state The new state
	 */
	public void setPerformActionEnabled(boolean state) {
		for (Iterator buttons = fActionButtons.iterator(); buttons.hasNext(); ) {
			Button element = (Button) buttons.next();
			element.setEnabled(state);
		}
	} 

	//---- Operation stuff ------------------------------------------------------
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		// The operation can only be canceled if it is executed in a separate thread.
		// Otherwise the UI is blocked anyway.
		Object state= null;
		try {
			fActiveRunningOperations++;
			state= aboutToStart(fork && cancelable);
			if (fUseEmbeddedProgressMonitorPart) {
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			} else {
				new ProgressMonitorDialog(getShell()).run(fork, cancelable, runnable);
			}
		} finally {
			if (state != null)
				stopped(state);
			fActiveRunningOperations--;
		}
	}
	
	/**
	 * About to start a long running operation tiggered through
	 * the wizard. So show the progress monitor and disable
	 * the wizard.
	 * @param enableCancelButton The cancel button enable state
	
	 * @return The saved UI state. */
	protected synchronized Object aboutToStart(boolean enableCancelButton) {
		HashMap savedState= null;
		Shell shell= getShell();
		if (shell != null) {
			Display d= shell.getDisplay();
			
			// Save focus control
			Control focusControl= d.getFocusControl();
			if (focusControl != null && focusControl.getShell() != shell)
				focusControl= null;
				
			// Set the busy cursor to all shells.
			fWaitCursor= new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(d, fWaitCursor);
					
			// Set the arrow cursor to the cancel component.
			fArrowCursor= new Cursor(d, SWT.CURSOR_ARROW);
			fCancelButton.setCursor(fArrowCursor);
	
			// Deactivate shell
			savedState= saveUIState(enableCancelButton);
			if (focusControl != null)
				savedState.put(FOCUS_CONTROL, focusControl);
				
			if (fUseEmbeddedProgressMonitorPart) {
				// Attach the progress monitor part to the cancel button
				fProgressMonitorPart.attachToCancelComponent(fCancelButton);
				fProgressMonitorPart.setVisible(true);
			}
		}
		
		return savedState;
	}
	
	/**
	 * A long running operation triggered through the wizard
	 * was stopped either by user input or by normal end.
	 * @param savedState The saveState returned by <code>aboutToStart</code>.
	
	 * @see #aboutToStart(boolean) */
	protected synchronized void stopped(Object savedState) {
		Assert.isTrue( savedState instanceof HashMap);
		Shell shell= getShell();
		if (shell != null) {
			if (fUseEmbeddedProgressMonitorPart) {
				fProgressMonitorPart.setVisible(false);	
				fProgressMonitorPart.removeFromCancelComponent(fCancelButton);
			}
					
			HashMap state= (HashMap)savedState;
			restoreUIState(state);
	
			setDisplayCursor(shell.getDisplay(), null);	
			fCancelButton.setCursor(null);
			fWaitCursor.dispose();
			fWaitCursor= null;
			fArrowCursor.dispose();
			fArrowCursor= null;
			Control focusControl= (Control)state.get(FOCUS_CONTROL);
			if (focusControl != null && ! focusControl.isDisposed())
				focusControl.setFocus();
		}
	}
	
	/**
	 * Method setDisplayCursor.
	 * @param d Display
	 * @param c Cursor
	 */
	private void setDisplayCursor(Display d, Cursor c) {
		Shell[] shells= d.getShells();
		for (int i= 0; i < shells.length; i++)
			shells[i].setCursor(c);
	}	

	//---- UI state save and restoring ---------------------------------------------
	
	/**
	 * Method restoreUIState.
	 * @param state HashMap
	 */
	private void restoreUIState(HashMap state) {
		restoreEnableState(fCancelButton, state); //$NON-NLS-1$
		for (Iterator actionButtons = fActionButtons.iterator(); actionButtons.hasNext(); ) {
			Button button = (Button) actionButtons.next();
			restoreEnableState(button, state);
		}
		ControlEnableState pageState= (ControlEnableState)state.get("tabForm"); //$NON-NLS-1$
		pageState.restore();
	}
	
	/*
	 * Restores the enable state of the given control.
	 */
	/**
	 * Method restoreEnableState.
	 * @param w Control
	 * @param h HashMap
	 */
	protected void restoreEnableState(Control w, HashMap h) {
		if (!w.isDisposed()) {
			Boolean b= (Boolean)h.get(w);
			if (b != null)
				w.setEnabled(b.booleanValue());
		}
	}
	
	/**
	 * Method saveUIState.
	 * @param keepCancelEnabled boolean
	 * @return HashMap
	 */
	private HashMap saveUIState(boolean keepCancelEnabled) {
		HashMap savedState= new HashMap(10);
		saveEnableStateAndSet(fCancelButton, savedState, keepCancelEnabled); //$NON-NLS-1$
		for (Iterator actionButtons = fActionButtons.iterator(); actionButtons.hasNext(); ) {
			Button button = (Button) actionButtons.next();
			saveEnableStateAndSet(button, savedState, false);
		}
		savedState.put("tabForm", ControlEnableState.disable(fContents)); //$NON-NLS-1$
		
		return savedState;
	}
	
	/**
	 * Method saveEnableStateAndSet.
	 * @param w Control
	 * @param h HashMap
	 * @param enabled boolean
	 */
	private void saveEnableStateAndSet(Control w, HashMap h, boolean enabled) {
		if (!w.isDisposed()) {
			h.put(w, new Boolean(w.isEnabled()));
			w.setEnabled(enabled);
		}	
	}	

	protected void handleShellCloseEvent() {
		if (okToClose())
			super.handleShellCloseEvent();
	}

	/**
	 * The dialog is going to be closed. Check if there is a running
	 * operation. If so, post an alert saying that the wizard can't
	 * be closed.
	
	 * @return If false is returned, the dialog should stay open */
	public boolean okToClose() {
		if (fActiveRunningOperations > 0) {
			synchronized (this) {
				fWindowClosingDialog= createClosingDialog();
			}	
			fWindowClosingDialog.open();
			synchronized (this) {
				fWindowClosingDialog= null;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Method createClosingDialog.
	 * @return MessageDialog
	 */
	private MessageDialog createClosingDialog() {
		MessageDialog result= 
			new MessageDialog(
				getShell(),
				SearchMessages.SearchDialogClosingDialog_title,  
				null, 
				SearchMessages.SearchDialogClosingDialog_message,  
				MessageDialog.QUESTION, 
				new String[] {IDialogConstants.OK_LABEL}, 
				0); 
		return result;		
	}

	/**
	
	 * @return Returns the cancel component that is to be used to cancel 
	 * a long running operation. */
	protected Control getCancelComponent() {
		return fCancelButton;
	}	
}