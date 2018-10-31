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
package gov.pnnl.cat.ui.rcp.views.find;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 */
public class CATFind extends Dialog {

  private Text text;
  protected Object result;
  protected Shell shell;
  private StyledText bodyText;
  private int currentLocation = SWT.BEGINNING;
  private static String MSG_STRING_NOT_FOUND = "String Not Found";
  private Label lblStringNotFound;
  private Button matchCaseButton;
  /**
   * Create the dialog
   * @param parent
   * @param style
   */
  public CATFind(Shell parent, int style) {
    super(parent, style);
  }

  /**
   * Create the dialog
   * @param parent
   * @param bodyText StyledText
   */
  public CATFind(Shell parent, StyledText bodyText) {
    this(parent, SWT.NONE);
    this.bodyText = bodyText;
  }

  /**
   * Open the dialog
  
   * @return the result */
  public Object open() {
    createContents();
    shell.open();
    shell.layout();
    
    Display display = getParent().getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return result;
  }

  /**
   * Create contents of the dialog
   */
  protected void createContents() {
    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    shell.setLayout(gridLayout);
    shell.setSize(397, 132);
    shell.setText("Find");

    final Label findLabel = new Label(shell, SWT.NONE);
    findLabel.setText("Find:");

    text = new Text(shell, SWT.BORDER);
    text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    final Button findNextButton = new Button(shell, SWT.NONE);
    findNextButton.setText("Find Next");
   
    matchCaseButton = new Button(shell, SWT.CHECK);
    final GridData gd_matchCaseButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    matchCaseButton.setLayoutData(gd_matchCaseButton);
    matchCaseButton.setText("Match Case");
    new Label(shell, SWT.NONE);

    final Label label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

    lblStringNotFound = new Label(shell, SWT.NONE);
    lblStringNotFound.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
    lblStringNotFound.setText("");
    
    final Button closeButton = new Button(shell, SWT.NONE);
    final GridData gd_closeButton = new GridData(SWT.FILL, SWT.CENTER, false, false);
    closeButton.setLayoutData(gd_closeButton);
    closeButton.setText("Close");

    closeButton.addMouseListener(new MouseListener(){
      public void mouseDoubleClick(MouseEvent e) {
      }
      public void mouseDown(MouseEvent e) {
        shell.close();
        shell.dispose();
        currentLocation = SWT.BEGINNING;
      }
      public void mouseUp(MouseEvent e) {
      }
    });
    
    findNextButton.addMouseListener(new MouseListener(){
      public void mouseDoubleClick(MouseEvent e) {
      }
      public void mouseDown(MouseEvent e) {
        findNextAction();
      }
      public void mouseUp(MouseEvent e) {
      }
    });
    
    text.addKeyListener(new KeyListener(){
      public void keyPressed(KeyEvent e) {
      }
      public void keyReleased(KeyEvent e) {
        findNextButton.setEnabled(text.getText().trim().length() > 0);
        if(findNextButton.isEnabled()){
          if(e.keyCode == 13){
            findNextButton.setFocus();
            findNextAction();
          }
        }
      }
      
    });
    
    findNextButton.addSelectionListener(new SelectionListener(){

      public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
        
      }

      public void widgetSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
        findNextAction();
      }
      
    });
  }
  
  public void findNextAction(){
    boolean matchCase = matchCaseButton.getSelection();
    int start = 0;
    int textLength = text.getText().trim().length();
    
    if(matchCase){
      String textToFind = text.getText().trim();
      if(currentLocation == SWT.BEGINNING){
        start = bodyText.getText().indexOf(textToFind, currentLocation);
      }
      else{
        start = bodyText.getText().indexOf(textToFind, currentLocation + 1);
      }
      currentLocation = start;
      if(start == -1){
        start = bodyText.getText().indexOf(textToFind, currentLocation);
      }
      if(start != -1){
        bodyText.setSelection(start, start + textLength);
        lblStringNotFound.setText("");
        shell.layout();
      }
      if(start == -1){
        lblStringNotFound.setText(MSG_STRING_NOT_FOUND);
        shell.layout();
      }
    }
    else{
      String textToFind = text.getText().trim().toLowerCase();
      String bodyTextLowerCase = bodyText.getText().toLowerCase();
      if(currentLocation == SWT.BEGINNING){
        start = bodyTextLowerCase.indexOf(textToFind, currentLocation);
      }
      else{
        start = bodyTextLowerCase.indexOf(textToFind, currentLocation + 1);
      }
      currentLocation = start;
      if(start == -1){
        start = bodyTextLowerCase.indexOf(textToFind, currentLocation);
      }
      if(start != -1){
        bodyText.setSelection(start, start + textLength);
        lblStringNotFound.setText("");
        shell.layout();
      }
      if(start == -1){
        lblStringNotFound.setText(MSG_STRING_NOT_FOUND);
        shell.layout();
      }
    }
  }
  
}
