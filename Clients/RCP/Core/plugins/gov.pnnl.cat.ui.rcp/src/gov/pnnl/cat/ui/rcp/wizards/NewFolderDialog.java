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
package gov.pnnl.cat.ui.rcp.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 */
public class NewFolderDialog extends StatusDialog {

  private Text text;
  private String[] unavailableNames;
  private String name;

  /**
   * Create the dialog
   * @param parentShell
   * @param unavailableNames String[]
   */
  public NewFolderDialog(Shell parentShell, String[] unavailableNames) {
    super(parentShell);
    this.unavailableNames = unavailableNames;
  }

  /**
   * Create contents of the dialog
   * @param parent
   * @return Control
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);

    final Label nameOfNewLabel = new Label(container, SWT.NONE);
    nameOfNewLabel.setText("Name of new folder:");

    text = new Text(container, SWT.BORDER);
    text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    ControlDecoration dec = new ControlDecoration(text, SWT.LEFT | SWT.TOP);
    dec.setMarginWidth(10);
    dec.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        MessageDialog
            .openInformation(
                getShell(),
                "hmm",
                "selected");
      }
    });

    final FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
        FieldDecorationRegistry.DEC_ERROR_QUICKFIX);

    text.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        name = text.getText();
        boolean available = true;

        for (String unavailableName : unavailableNames) {
          if (name.equalsIgnoreCase(unavailableName)) {
            available = false;
            break;
          }
        }

        getButton(IDialogConstants.OK_ID).setEnabled(available);

        // display "unavailable" message
        if (available) {
          updateStatus(Status.OK_STATUS);
        } else {
          updateStatus(new Status(IStatus.ERROR,
              "org.eclipse.examples.contentassist", 0, //$NON-NLS-1$
              "This name is already taken.", null));
        }
      }
    });
    //
    return container;
  }

  /**
   * Return the initial size of the dialog
   * @return Point
   */
  @Override
  protected Point getInitialSize() {
    return new Point(200, 150);
  }
  /**
   * Method configureShell.
   * @param newShell Shell
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("New Folder");
  }

  /**
   * Returns the name entered by the user.
  
   * @return String
   */
  public String getName() {
    return name;
  }
}
