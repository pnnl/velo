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
package gov.pnnl.cat.discussion.wizard;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;

import com.swtdesigner.SWTResourceManager;

/**
 */
public class AddCommentWizardPage extends WizardPage{
  // max size for the column in the database is 255, but the
  // qualifier is 43 characters which limits us to 212 for the name.
  private static final int MAX_SUBJECT_LENGTH = 212;

  private IResource currentSelection;
  private IWorkbenchWindow window;
  private Text txtSubject;
  private Text contents;

  /**
   * Constructor for AddCommentWizardPage.
   * @param selection ISelection
   * @param window IWorkbenchWindow
   */
  public AddCommentWizardPage(ISelection selection, IWorkbenchWindow window){
    super("addComment");
    this.window = window;
    setTitle("Add Comment to Resource");
    setDescription("");
    this.window = window;
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structSel = (IStructuredSelection) selection;
      currentSelection = RCPUtil.getResource(structSel.getFirstElement());
    }
  }

  /**
   * Method createControl.
   * @param parent Composite
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    // TODO Auto-generated method stub
    final Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

    final GridLayout gridLayout_1 = new GridLayout();
    gridLayout_1.numColumns = 2;
    composite.setLayout(gridLayout_1);

    final Label commentLabel = new Label(composite, SWT.NONE);
    commentLabel.setFont(SWTResourceManager.getFont("", 8, SWT.BOLD));
    commentLabel.setText("Subject:");

    int style = SWT.BORDER;

    txtSubject = new Text(composite, style);
    txtSubject.setTextLimit(MAX_SUBJECT_LENGTH);
    txtSubject.setText("");
    final GridData gd_lblComment = new GridData(SWT.FILL, SWT.CENTER, true, false);

    txtSubject.setLayoutData(gd_lblComment);
    txtSubject.setFocus();
    txtSubject.addModifyListener(new ModifyListener(){
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    });

    contents = new Text(composite, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.H_SCROLL);
    final GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    gd_text.widthHint = 0;
    gd_text.heightHint = 225;
    gd_text.minimumHeight = 225;

    contents.setLayoutData(gd_text);
    contents.setText("");
    contents.addModifyListener(new ModifyListener(){
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    });

    setControl(composite);
    setPageComplete(false);
  }

  /**
   * Method getSubject.
   * @return String
   */
  public String getSubject(){
    return txtSubject.getText().trim();
  }

  /**
   * Method getContents.
   * @return String
   */
  public String getContents(){
    return contents.getText().trim();
  }

  /**
   * Method getResource.
   * @return IResource
   */
  public IResource getResource(){
    return currentSelection;
  }

  /**
   * Method updateStatus.
   * @param message String
   */
  private void updateStatus(String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  private void dialogChanged() {
    if(txtSubject.getText().trim().length() == 0){
      updateStatus("The comment must have a subject.");
      return;
    }
    updateStatus(null);
  }
}
