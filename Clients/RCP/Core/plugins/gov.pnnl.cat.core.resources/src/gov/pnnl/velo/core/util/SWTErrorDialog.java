package gov.pnnl.velo.core.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jdesktop.swingx.error.ErrorInfo;

/**
 * An SWT dialog used to display unexpected error messages to the user instead of
 * utilizing an instance of org.jdesktop.swingx.JXErrorPane.
 * 
 * This class is meant to be instantiated as-is.
 */

public class SWTErrorDialog extends Dialog {

	/** Contains error information prepared for reporting. */
  private ErrorInfo errorInfo = new ErrorInfo("Error", "Normally this place contains problem description.\n You see this text because one of the following reasons:\n * Either it is a test\n * Developer have not provided error details\n * This error message was invoked unexpectedly and there are no more details available", null, null, null, null, null);
	
  /** For use in toggleing details panel. */
  private boolean seeDetails = true;
  
  /** The Composite to display error details. */
  private Group detailsComponent;
  
  /** Used to close itself. */
  private SWTErrorDialog self;
  
  /**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SWTErrorDialog(Shell parentShell, ErrorInfo info) {
		super(parentShell);
		this.errorInfo = info;
		this.self = this;
	}
  
  /**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		//container.setLayout(new GridLayout(2,false));
		gridLayout.numColumns = 2;
		
		CBanner banner = new CBanner(container, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		
		// get and set icon
		Image systemErrorIcon = Display.getCurrent().getSystemImage(SWT.ICON_ERROR);
		Label errorLabel = new Label(container, SWT.NONE);
		errorLabel.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		errorLabel.setImage(systemErrorIcon);
		
		// show message
		Label basicMessageLabel = new Label(container, SWT.WRAP);
		GridData msgGridData = new GridData();
		msgGridData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		basicMessageLabel.setLayoutData(msgGridData);
		basicMessageLabel.setText(errorInfo.getBasicErrorMessage());
		
		// create details component as a Group rather than a Composite so it can have a title
		detailsComponent = new Group(container, SWT.NONE);
		//detailsComponent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		GridData detailsGridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		detailsGridData.exclude = true;
		detailsComponent.setLayoutData(detailsGridData);
		detailsComponent.setLayout(new GridLayout());
		detailsComponent.setVisible(false);
		detailsComponent.setText("Error Stacktrace");
		
		// add details textbox
		Text text = new Text(detailsComponent, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData textGridData = new GridData();
		textGridData.heightHint = 200;
		textGridData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		text.setLayoutData(textGridData);
		text.setText(errorInfo.getDetailedErrorMessage());
		text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		// add copy to clipboard button
		Button copyButton = new Button(detailsComponent, SWT.PUSH);
		copyButton.setText("Copy to Clipboard");
		copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String toCopy = errorInfo.getDetailedErrorMessage();
				Clipboard clipboard = new Clipboard(Display.getDefault());
				clipboard.setContents(new Object[] { toCopy },
				new Transfer[] { TextTransfer.getInstance() });
        clipboard.dispose();
			}
		});
		
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// button 1 - close
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
		
		// button 2 - open email
		Button reportButton = createButton(parent, IDialogConstants.CANCEL_ID,
				"Report Error", false);
		reportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ToolErrorHandler.openEmailDialog(errorInfo);
				self.close();
			}
		});
		
		// button 3 - see details
		final Button detailsButton = createButton(parent, 64, "See details", false);
		detailsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// toggle display
				GridData data = (GridData) detailsComponent.getLayoutData();
				if(seeDetails) { // add component
					detailsComponent.setVisible(true);
					data.exclude = false;
					detailsButton.setText("Hide details");
					getShell().pack();
					seeDetails = !seeDetails;
				} else { // remove component
					detailsComponent.setVisible(false);
					data.exclude = true;
					detailsButton.setText("See details");
					getShell().pack();
					seeDetails = !seeDetails;
				}
			}
		});
		
	}

//	public ErrorInfo getErrorInfo() {
//		return errorInfo;
//	}
//
//	public void setErrorInfo(ErrorInfo info) {
//		if (info == null) {
//	    throw new NullPointerException("ErrorInfo can't be null. Provide valid ErrorInfo object.");
//	  }
//	  this.errorInfo = info;
//	}

}
