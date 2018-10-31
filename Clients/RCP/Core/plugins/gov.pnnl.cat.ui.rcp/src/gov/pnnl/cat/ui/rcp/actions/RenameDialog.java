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
package gov.pnnl.cat.ui.rcp.actions;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 */
public class RenameDialog extends Dialog {

  private Text name;
  
  protected static Logger logger = CatLogger.getLogger(RenameDialog.class);
  
  private IResource currentResource;

  /**
   * Constructor for RenameDialog.
   * @param parent Shell
   * @param currentResource IResource
   */
  public RenameDialog(Shell parent, IResource currentResource) {
    super(parent);
    this.currentResource = currentResource;
  }
  

 


  /**
   * Method isResizable.
   * @return boolean
   */
  @Override
  protected boolean isResizable() {
    // TODO Auto-generated method stub
    return true;
  }


  /**
   * Method createDialogArea.
   * @param parent Composite
   * @return Control
   */
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    container.setLayout(gridLayout);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    ModifyListener modifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        enableOkButton();
      }
    };

    final Label nameLabel = new Label(container, SWT.NONE);
    nameLabel.setText("Name:");

    name = new Text(container, SWT.BORDER);
    name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    name.setText(currentResource.getName());
    name.addModifyListener(modifyListener);

    return container;
  }

  /**
   * Method createContents.
   * @param parent Composite
   * @return Control
   */
  protected Control createContents(Composite parent) {
    Control ctrl = super.createContents(parent);
    // when dialog first comes up the ok button should be disabled:
    // this.getButton(IDialogConstants.OK_ID).setEnabled(false);
    return ctrl;
  }

  /**
   * Return the initial size of the dialog
   * @return Point
   */
  protected Point getInitialSize() {
    return new Point(320, 150);
  }

  private void enableOkButton() {
    if (this.name.getText().length() >= 1) {
      this.getButton(IDialogConstants.OK_ID).setEnabled(true);
    } else {
      this.getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  protected void okPressed() {
    try {
      String oldname = currentResource.getName();
      String newName = name.getText();

      if(newName.trim().matches(IResource.invalidCharactersRegex))
      {
        String errorMsg = "A file or a folder name " + IResource.invalidCharactersMsg;
        ToolErrorHandler.handleError(errorMsg, null, true);
        return;
      }
      // compare the value with the current name
      if (newName.equals(oldname)) {
        super.okPressed();
        return;// nothing was changed so don't do anything
      }

      currentResource.move(currentResource.getParent().getPath().append(newName.trim()));
      super.okPressed();
    } catch (ResourceException e) {
      ToolErrorHandler.handleError("An error occurred trying to rename.", e, true);
    }
  }

  /**
   * Method configureShell.
   * @param newShell Shell
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Rename");
  }

  /**
   * Method getName.
   * @return String
   */
  public String getName() {
    return this.name.getText();
  }

}
