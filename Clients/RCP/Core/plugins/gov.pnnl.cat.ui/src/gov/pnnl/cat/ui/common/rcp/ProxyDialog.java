package gov.pnnl.cat.ui.common.rcp;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class ProxyDialog extends Dialog {
  private Text textProxyHost;
  private Text textProxyPort;
  private Text textProxyUsername;
  private Text textProxyPassword;
  
  private String proxyHost;
  private String proxyPort;
  private String proxyUsername;
  private String proxyPassword;

  /**
   * Create the dialog.
   * @param parentShell
   */
  public ProxyDialog(Shell parentShell) {
    super(parentShell);
    
  }

  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout gridLayout = (GridLayout) container.getLayout();
    gridLayout.numColumns = 2;
    
    Label lblAProxyWas = new Label(container, SWT.NONE);
    lblAProxyWas.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
    lblAProxyWas.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
    lblAProxyWas.setText("A proxy was detetected that requires authorization:");
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    
    Label lblProxyHost = new Label(container, SWT.NONE);
    lblProxyHost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblProxyHost.setText("Proxy Host:");
    
    textProxyHost = new Text(container, SWT.BORDER);
    textProxyHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    textProxyHost.setEnabled(false);
    
    Label lblProxyPort = new Label(container, SWT.NONE);
    lblProxyPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblProxyPort.setText("Proxy Port:");
    
    textProxyPort = new Text(container, SWT.BORDER);
    textProxyPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    textProxyPort.setEnabled(false);
    
    Label lblProxyUsername = new Label(container, SWT.NONE);
    lblProxyUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblProxyUsername.setText("Proxy Username:");
    
    textProxyUsername = new Text(container, SWT.BORDER);
    textProxyUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    
    Label lblProxyPassword = new Label(container, SWT.NONE);
    lblProxyPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblProxyPassword.setText("Proxy Password:");
    
    textProxyPassword = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
    textProxyPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    return container;
  }

  /**
   * Create contents of the button bar.
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }
//
//  /**
//   * Return the initial size of the dialog.
//   */
//  @Override
//  protected Point getInitialSize() {
//    return new Point(450, 300);
//  }

  // overriding this methods allows you to set the
  // title of the custom dialog
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Proxy Server Configuration");
  }

  /**
   * @return the textProxyHost
   */
  public String getProxyHost() {
    return proxyHost;
  }
  
  public void setProxyHost(String proxyHost) {
    textProxyHost.setText(proxyHost);
  }

  /**
   * @return the textProxyPort
   */
  public String getProxyPort() {
    return proxyPort;
  }
  
  public void setProxyPort(String proxyPort) {
    textProxyPort.setText(proxyPort);
  }
  
  public void setProxyUser(String proxyUser) {
    textProxyUsername.setText(proxyUser);
  }

  /**
   * @return the textProxyUsername
   */
  public String getProxyUsername() {
    return proxyUsername;
  }

  /**
   * @return the textProxyPassword
   */
  public String getProxyPassword() {
    return proxyPassword;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {
    proxyHost = textProxyHost.getText();
    proxyPort = textProxyPort.getText();
    proxyUsername = textProxyUsername.getText();
    proxyPassword = textProxyPassword.getText();
    super.okPressed();
  }
  
}
