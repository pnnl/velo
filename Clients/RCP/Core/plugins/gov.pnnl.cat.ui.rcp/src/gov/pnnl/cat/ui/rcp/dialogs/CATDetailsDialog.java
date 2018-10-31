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
package gov.pnnl.cat.ui.rcp.dialogs;

import gov.pnnl.cat.logging.CatLogger;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;




/**
 */
public class CATDetailsDialog extends IconAndMessageDialog {

  private Logger logger = CatLogger.getLogger(CATDetailsDialog.class);
  /**
   * Static to prevent opening of error dialogs for automated testing.
   */
  public static boolean AUTOMATED_MODE = false;

  /**
   * The Details button.
   */
  private Button detailsButton;

  /**
   * The title of the dialog.
   */
  private String title;

  /**
   * The SWT list control that displays the error details.
   */
  private TextViewer textViewer;

  /**
   * Indicates whether the error details viewer is currently created.
   */
  private boolean textViewerCreated = false;

  /**
   * The main status object.
   */
  private IStatus status;


  /**
   * Creates an error dialog. Note that the dialog will have no visual representation (no widgets) until it is told to open.
   * <p>
   * Normally one should use <code>openError</code> to create and open one of these. This constructor is useful only if the error object being displayed contains child items <it>and </it> you need to specify a mask which will be used to filter the displaying of these children.
   * </p>
   * 
   * @param parentShell
   *          the shell under which to create this dialog
   * @param dialogTitle
   *          the title to use for this dialog, or <code>null</code> to indicate that the default title should be used
   * @param message
   *          the message to show in this dialog, or <code>null</code> to indicate that the error's message should be shown as the primary message
   * @param status
   *          the error to show to the user
  
  
   * @see org.eclipse.core.runtime.IStatus#matches(int) */
  public CATDetailsDialog(Shell parentShell, String dialogTitle, String message, IStatus status) {
    super(parentShell);
    this.title = dialogTitle == null ? JFaceResources.getString("Problem_Occurred") : //$NON-NLS-1$
        dialogTitle;
    this.message = message == null ? status.getMessage() : JFaceResources.format("Reason", new Object[] { message, status.getMessage() }); //$NON-NLS-1$
    this.status = status;
    setShellStyle(getShellStyle() | SWT.RESIZE);
  }

  /*
   * (non-Javadoc) Method declared on Dialog. Handles the pressing of the Ok or Details button in this dialog. If the Ok button was pressed then close this dialog. If the Details button was pressed then toggle the displaying of the error details area. Note that the Details button will only be visible if the error being displayed specifies child details.
   */
  /**
   * Method buttonPressed.
   * @param id int
   */
  protected void buttonPressed(int id) {
    if (id == IDialogConstants.DETAILS_ID) {
      // was the details button pressed?
      toggleDetailsArea();
    } else {
      super.buttonPressed(id);
    }
  }

  /*
   * (non-Javadoc) Method declared in Window.
   */
  /**
   * Method configureShell.
   * @param shell Shell
   */
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(title);
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  /**
   * Method createButtonsForButtonBar.
   * @param parent Composite
   */
  protected void createButtonsForButtonBar(Composite parent) {
    // create OK and Details buttons
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createDetailsButton(parent);
  }

  /**
   * Create the details button if it should be included.
   * 
   * @param parent
   *          the parent composite
   * @since 3.2
   */
  protected void createDetailsButton(Composite parent) {
    if (shouldShowDetailsButton()) {
      detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
    }
  }

  /**
   * This implementation of the <code>Dialog</code> framework method creates and lays out a composite and calls <code>createMessageArea</code> and <code>createCustomArea</code> to populate it. Subclasses should override <code>createCustomArea</code> to add contents below the message.
   * @param parent Composite
   * @return Control
   */
  protected Control createDialogArea(Composite parent) {
    createMessageArea(parent);
    // create a composite with standard margins and spacing
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    composite.setLayout(layout);
    GridData childData = new GridData(GridData.FILL_BOTH);
    childData.horizontalSpan = 2;
    composite.setLayoutData(childData);
    composite.setFont(parent.getFont());

    return composite;
  }

  /*
   * @see IconAndMessageDialog#createDialogAndButtonArea(Composite)
   */
  protected void createDialogAndButtonArea(Composite parent) {
    super.createDialogAndButtonArea(parent);
    if (this.dialogArea instanceof Composite) {
      // Create a label if there are no children to force a smaller layout
      Composite dialogComposite = (Composite) dialogArea;
      if (dialogComposite.getChildren().length == 0) {
        new Label(dialogComposite, SWT.NULL);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
   */
  protected Image getImage() {
    if (status != null) {
      if (status.getSeverity() == IStatus.WARNING) {
        return getWarningImage();
      }
      if (status.getSeverity() == IStatus.INFO) {
        return getInfoImage();
      }
    }
    // If it was not a warning or an error then return the error image
    return getErrorImage();
  }

  /**
   * Create this dialog's drop-down list component.
   * 
   * @param parent
   *          the parent composite
  
   * @return the drop-down list component */
  protected TextViewer createTextArea(Composite parent) {
    // create the list
    textViewer = new TextViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
            | SWT.MULTI);
    // fill the list
    populateTextViewer(textViewer);
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
            | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
            | GridData.GRAB_VERTICAL);
    data.heightHint = 100;
    data.horizontalSpan = 2;
    textViewer.getControl().setLayoutData(data);
    textViewer.getControl().setFont(parent.getFont());
    textViewer.setEditable(false);

    textViewerCreated = true;
    return textViewer;
  }
  
  /**
   * Method populateTextViewer.
   * @param textViewer TextViewer
   */
  private void populateTextViewer(TextViewer textViewer) {
    
    Throwable t = status.getException();
    StringBuffer sb = new StringBuffer();

    if (t != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      t.printStackTrace(pw);
      
      sb.append(sw.toString());
    }
    
    textViewer.setDocument(new Document(sb.toString())); 
  }

  /*
   * (non-Javadoc) Method declared on Window.
   */
  /**
   * Extends <code>Window.open()</code>. Opens an error dialog to display the error. If you specified a mask to filter the displaying of these children, the error dialog will only be displayed if there is at least one child status matching the mask.
   * @return int
   */
  public int open() {
    if (!AUTOMATED_MODE) {
      return super.open();
    }
    setReturnCode(OK);
    return OK;
  }


  /**
   * Opens an error dialog to display the given error. Use this method if the error object being displayed contains child items <it>and </it> you wish to specify a mask which will be used to filter the displaying of these children. The error dialog will only be displayed if there is at least one child status matching the mask.
   * 
   * @param parentShell
   *          the parent shell of the dialog, or <code>null</code> if none
   * @param title
   *          the title to use for this dialog, or <code>null</code> to indicate that the default title should be used
   * @param message
   *          the message to show in this dialog, or <code>null</code> to indicate that the error's message should be shown as the primary message
   * @param status
   *          the error to show to the user
  
  
  
   * @return the code of the button that was pressed that resulted in this dialog closing. This will be <code>Dialog.OK</code> if the OK button was pressed, or <code>Dialog.CANCEL</code> if this dialog's close window decoration or the ESC key was used. * @see org.eclipse.core.runtime.IStatus#matches(int) */
  public static int openError(Shell parentShell, String title, String message, IStatus status) {
    CATDetailsDialog dialog = new CATDetailsDialog(parentShell, title, message, status);
    return dialog.open();
  }

  /**
   * Toggles the unfolding of the details area. This is triggered by the user pressing the details button.
   */
  private void toggleDetailsArea() {
    Point windowSize = getShell().getSize();
    Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    if (textViewerCreated) {
      textViewer.getControl().dispose();
      textViewerCreated = false;
      detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
    } else {
      textViewer = createTextArea((Composite) getContents());
      detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
    }
    Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
  }


  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.window.Window#close()
   */
  public boolean close() {
    return super.close();
  }

  /**
   * Show the details portion of the dialog if it is not already visible. This method will only work when it is invoked after the control of the dialog has been set. In other words, after the <code>createContents</code> method has been invoked and has returned the control for the content area of the dialog. Invoking the method before the content area has been set or after the dialog has been disposed will have no effect.
   * 
   * @since 3.1
   */
  protected final void showDetailsArea() {
    if (!textViewerCreated) {
      Control control = getContents();
      if (control != null && !control.isDisposed()) {
        toggleDetailsArea();
      }
    }
  }

  /**
   * Return whether the Details button should be included. This method is invoked once when the dialog is built. By default, the Details button is only included if the status used when creating the dialog was a multi-status or if the status contains an exception. Subclasses may override.
   * 
  
   * @since 3.1
   * @return whether the Details button should be included */
  protected boolean shouldShowDetailsButton() {
    return status.getException() != null;
  }

  /**
   * Set the status displayed by this error dialog to the given status. This only affects the status displayed by the Details list. The message, image and title should be updated by the subclass, if desired.
   * 
   * @param status
   *          the status to be displayed in the details list
   * @since 3.1
   */
  protected final void setStatus(IStatus status) {
    if (this.status != status) {
      this.status = status;
    }

    if (textViewerCreated) {
      repopulateTextViewer();
    }
  }
  
  /**
   * Repopulate the supplied list widget.
   */
  private void repopulateTextViewer() {
      if (textViewer != null && !textViewer.getControl().isDisposed()) {
//        list.setgetControl().removeAll();
        populateTextViewer(textViewer);
      }
  }

}
