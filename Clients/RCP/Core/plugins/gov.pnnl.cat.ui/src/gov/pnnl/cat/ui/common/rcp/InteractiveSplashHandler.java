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
package gov.pnnl.cat.ui.common.rcp;

import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.ProxyConfig;
import gov.pnnl.cat.logging.CatLogger;

import java.io.File;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.splash.AbstractSplashHandler;

/**
 * @since 3.3
 * @version $Revision: 1.0 $
 */
public class InteractiveSplashHandler extends AbstractSplashHandler implements IJobChangeListener {
  private Logger logger = CatLogger.getLogger(InteractiveSplashHandler.class);

  private final static int F_LABEL_HORIZONTAL_INDENT = 40;

  private final static int F_BUTTON_WIDTH_HINT = 80;

  private final static int F_TEXT_WIDTH_HINT = 175;

  protected final static int F_COLUMN_COUNT = 3;

  private final static String PREF_DEFAULT_USERNAME = "default_username";

  private final static String PREF_NODE_SPLASH = "gov.pnnl.cat.ui.rcp.splash";

  private Composite loginComposite;

  private Text textUser;

  private Text textPassword;

  private Button ok;

  private Button cancel;

  private boolean fAuthenticated;

  private LoginJob loginJob;

  private Label message;

  private ScopedPreferenceStore preferences = null; 

  private String username;
  private String password;

  /** Boolean detecting if user/pass was passed in on command line */
  private boolean skipInteractiveLogin = false;


  /**
   * 
   */
  public InteractiveSplashHandler() {
    loginComposite = null;
    textUser = null;
    textPassword = null;
    ok = null;
    cancel = null;
    fAuthenticated = false;
    preferences = new ScopedPreferenceStore(new InstanceScope(), PREF_NODE_SPLASH);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.ui.splash.AbstractSplashHandler#init(org.eclipse.swt.widgets
   * .Shell)
   */
  /**
   * Method init.
   * @param splash Shell
   */
  @Override
  public void init(final Shell splash) {
    // Allow for bypassing login screen by reading in username/password from
    // system properties
    final String name = System.getProperty("velo.username");
    final String pass = System.getProperty("velo.password");
    if (name != null && pass != null) {
      username = name;
      password = pass;
      skipInteractiveLogin = true;
    }
    
    // Store the shell
    super.init(splash);
    // Configure the shell layout
    configureUISplash();
    // Create UI
    createUI();
    // Create UI listeners
    createUIListeners();
    // Force the splash screen to layout
    splash.layout(true);
    // Keep the splash screen visible and prevent the RCP application from
    // loading until the close button is clicked.
    doEventLoop();
  }

  /**
   * 
   */
  private void doEventLoop() {
    Shell splash = getSplash();
    while (fAuthenticated == false && !splash.isDisposed()) {
      if (skipInteractiveLogin) {
        handleButtonOKWidgetSelected();
      }
      if (splash.getDisplay().readAndDispatch() == false) {
        splash.getDisplay().sleep();
      }
    }
  }

  /**
   * 
   */
  private void createUIListeners() {
    // Create the OK button listeners
    createUIListenersButtonOK();
    // Create the cancel button listeners
    createUIListenersButtonCancel();
  }

  /**
   * 
   */
  private void createUIListenersButtonCancel() {
    cancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleButtonCancelWidgetSelected();
      }
    });
  }

  /**
   * 
   */
  private void handleButtonCancelWidgetSelected() {
    // Abort the loading of the RCP application
    getSplash().getDisplay().close();
    System.exit(0);
  }

  /**
   * 
   */
  private void createUIListenersButtonOK() {
    ok.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        handleButtonOKWidgetSelected();
      }
    });
  }

  /**
   * 
   */
  private void handleButtonOKWidgetSelected() {
    if (skipInteractiveLogin) {
      textUser.setText(username);
      textPassword.setText(password);
    } else {
      username = textUser.getText();
      password = textPassword.getText();
    }
    ProxyConfig proxyConfig = ResourcesPlugin.getProxyConfig();

    // First check if proxy detected when trying to make a connection to the
    // repository server URL
    if(proxyConfig.getHost() != null) {
      System.out.println("Proxy detected: " + proxyConfig.getHost() + ":" + proxyConfig.getPort());
      System.out.println("Client machine: " + proxyConfig.getClientMachine());
      System.out.println("Client domain: " + proxyConfig.getClientDomain());      
      System.out.println("ProxySelector class: " + proxyConfig.getProxySelector().getClass().toString());

      if(proxyConfig.isProxyAuthenticationRequired()) {
        // prompt for proxy credentials
        ProxyDialog dialog = new ProxyDialog(getSplash());
        dialog.create();
        dialog.setProxyHost(proxyConfig.getHost());
        dialog.setProxyPort(String.valueOf(proxyConfig.getPort()));
        if(proxyConfig.getProxyUsername() != null) {
          dialog.setProxyUser(proxyConfig.getProxyUsername());
        }        
        if (dialog.open() == Dialog.OK) {
          proxyConfig.setProxyUsername(dialog.getProxyUsername());
          proxyConfig.setProxyPassword(dialog.getProxyPassword());
        }         
      }
    }
    
    loginJob = new LoginJob();
    loginJob.setUsername(username);
    loginJob.setPassword(password);
    loginJob.setUser(true);
    loginJob.schedule();

    try {
      message.setText("Authenticating...");
      message.setVisible(true);
      ok.setEnabled(false);
      cancel.setEnabled(false);
      getSplash().layout(true);
      loginJob.join();
      postLogin();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // fAuthenticated = true;
  }

  /**
   * Sub classes can override to change the login screen label color
   * if they don't want to use white.

   * @return Color
   */
  protected Color getLabelColor() {
    return new Color(Display.getCurrent(), new RGB(255,255,255));
  }

  /**
   * Method getTextColor.
   * @return Color
   */
  protected Color getTextColor() {
    return new Color(Display.getCurrent(), new RGB(0,0,0));
  }

  /**
   * Method showServerLabel.
   * @return boolean
   */
  protected boolean showServerLabel() {
    return true;
  }

  /**
   * Method showBuildVersion.
   * @return boolean
   */
  protected boolean showBuildVersion() {
    return false;
  }

  /**
   * Method getBuildVersion.
   * @return String
   */
  protected String getBuildVersion() {
    return "";
  }

  /**
   * 
   */
  private void createUI() {
    ScrolledComposite scrollableContainer = new ScrolledComposite(getSplash(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    scrollableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    scrollableContainer.setLayout(new FillLayout());
    //scrollableContainer.setAlwaysShowScrollBars(true);
    scrollableContainer.setExpandHorizontal(true);
    scrollableContainer.setExpandVertical(true);
    scrollableContainer.setBackgroundMode(SWT.INHERIT_FORCE);
    scrollableContainer.setBackgroundImage(getSplash().getShell().getBackgroundImage());
    loginComposite = new Composite(scrollableContainer, SWT.BORDER);

    //    loginComposite = new Composite(getSplash(), SWT.BORDER);
    scrollableContainer.setContent(loginComposite);
    GridLayout layout = new GridLayout(getColumnCount(), false);
    layout.marginTop = getVerticalIndent();
    loginComposite.setLayout(layout);
    GridData data;

    // Create the server title, so we know where we are logging into
    if(showServerLabel()) {

      // Create the server label
      StyleRange textStyle = new StyleRange();
      textStyle.start = 0;
      textStyle.length = 7;
      textStyle.foreground = getLabelColor();
      textStyle.fontStyle = SWT.BOLD;

      StyledText title = new StyledText(loginComposite, SWT.NONE);
      title.setText("Server:");
      title.setStyleRange(textStyle);
      title.setEditable(false);

      data = new GridData();
      data.horizontalIndent = getLabelHorizontalIndent();
      data.verticalAlignment = SWT.TOP;
      title.setLayoutData(data);

      // Create the server value
      String server = ResourcesPlugin.getResourceManager().getRepositoryUrlBase();

      StyleRange textStyle2 = new StyleRange();
      textStyle2.start = 0;
      textStyle2.length = server.length();
      textStyle2.foreground = getTextColor();
      textStyle2.fontStyle = SWT.BOLD;

      StyledText value = new StyledText(loginComposite, SWT.NONE);
      value.setText(server);
      value.setStyleRange(textStyle2);
      value.setEditable(false);
      data = new GridData(SWT.NONE, SWT.NONE, true, false);
      data.minimumWidth = getTextWidthHint();
      data.horizontalSpan = 2;
      data.verticalAlignment = SWT.TOP;
      value.setLayoutData(data);
      value.setWordWrap(true);

    }

    // Display the build version number, so we know which version we are using
    if(showBuildVersion()) {
      String version = getBuildVersion();
      StyledText title = new StyledText(loginComposite, SWT.NONE);
      String text = "Version:";
      title.setText(text);
      title.setEditable(false);

      StyleRange textStyle = new StyleRange();
      textStyle.start = 0;
      textStyle.length = 7;
      textStyle.foreground = getLabelColor();
      textStyle.fontStyle = SWT.BOLD;
      title.setStyleRange(textStyle);

      data = new GridData();
      data.verticalAlignment = SWT.TOP;
      data.horizontalIndent = getLabelHorizontalIndent();
      title.setLayoutData(data);

      StyledText value = new StyledText(loginComposite, SWT.NONE);
      StyleRange textStyle2 = new StyleRange();
      textStyle2.start = 0;
      textStyle2.length = version.length();
      textStyle2.foreground = getTextColor();
      //textStyle2.fontStyle = SWT.BOLD;

      value.setText(version);
      value.setEditable(false);
      value.setStyleRange(textStyle2);
      data = new GridData(SWT.NONE, SWT.NONE, false, false);
      data.horizontalSpan = 2;
      data.verticalAlignment = SWT.TOP;
      value.setLayoutData(data);

    }

    // Create the label
    Label label = new Label(loginComposite, SWT.NONE);
    label.setText("&User Name:"); //$NON-NLS-1$
    label.setForeground(getLabelColor());
    data = new GridData();
    data.verticalIndent = 10;
    data.horizontalIndent = getLabelHorizontalIndent();
    label.setLayoutData(data);

    // Create the text widget
    textUser = new Text(loginComposite, SWT.BORDER);
    data = new GridData(SWT.NONE, SWT.NONE, true, false);
    data.horizontalSpan = 2;
    data.verticalIndent = 10;
    data.minimumWidth = getTextWidthHint();
    textUser.setLayoutData(data);
    textUser.setBackground(getSplash().getDisplay().getSystemColor(SWT.COLOR_WHITE));

    // remember last logged in user
    String strUserName = preferences.getString(PREF_DEFAULT_USERNAME);
    if (strUserName != null && strUserName.length() > 0) {
      textUser.setText(strUserName);
    }

    // Create the label
    label = new Label(loginComposite, SWT.NONE);
    label.setText("&Password:"); //$NON-NLS-1$
    label.setForeground(getLabelColor());
    data = new GridData();
    data.horizontalIndent = getLabelHorizontalIndent();
    label.setLayoutData(data);

    // Create the text widget
    int style = SWT.PASSWORD | SWT.BORDER;
    textPassword = new Text(loginComposite, style);
    data = new GridData(SWT.NONE, SWT.NONE, true, false);
    data.minimumWidth = getTextWidthHint();
    data.horizontalSpan = 2;
    textPassword.setLayoutData(data);
    textPassword.setBackground(getSplash().getDisplay().getSystemColor(SWT.COLOR_WHITE));


    // THIS DOES NOT WORK ON MAC - trying shell.setDefaultButton() instead
    //    textPassword.addTraverseListener(new TraverseListener() {
    //      
    //      @Override
    //      public void keyTraversed(TraverseEvent event) {
    //        if (event.detail == SWT.TRAVERSE_RETURN) {
    //          // The user pressed Enter 
    //          handleButtonOKWidgetSelected();
    //        }
    //      }
    //    });

    // Create blank label
    message = new Label(loginComposite, SWT.NONE);
    message.setVisible(false);

    // Create the button
    ok = new Button(loginComposite, SWT.PUSH);
    ok.setText("OK"); //$NON-NLS-1$
    data = new GridData(SWT.NONE, SWT.NONE, false, false);
    data.widthHint = getButtonWidthHint();
    data.verticalIndent = 10;
    ok.setLayoutData(data);

    // Set OK as the default button so it will be triggered
    // when enter pressed
    getSplash().setDefaultButton(ok);

    // Create the button
    cancel = new Button(loginComposite, SWT.PUSH);
    cancel.setText("Cancel"); //$NON-NLS-1$
    data = new GridData(SWT.NONE, SWT.NONE, false, false);
    data.widthHint = getButtonWidthHint();
    data.verticalIndent = 10;
    cancel.setLayoutData(data);

    // Hook Forgot My Password / Create Account options
    hookAccountOptions(loginComposite);

    if(!textUser.getText().isEmpty()) {
      textPassword.setFocus();
    } else {
      textUser.setFocus();
    }
  }

  /**
   * So sub classes can hook in links for "forgot my password"
   * or "create account"
   * @param loginComposite
   */
  protected void hookAccountOptions(Composite loginComposite) {
    // default is to do nothing
  }

  /**
   * 
   */
  private void configureUISplash() {
    // Configure layout
    FillLayout layout = new FillLayout();
    getSplash().setLayout(layout);

    // Force shell to inherit the splash background
    getSplash().setBackgroundMode(SWT.INHERIT_DEFAULT);

  }

  protected void postLogin() {
    if (loginJob.isLoggedIn()) {
      fAuthenticated = true;
      preferences.setValue(PREF_DEFAULT_USERNAME, loginJob.getUsername());
      try {
        preferences.save();
      } catch (Throwable e) {
        e.printStackTrace();
      }

    } else {

      // let the user know about the login error
      reportError(loginJob.getError());
      message.setText("Login Failed");
      message.setVisible(false);
      getSplash().layout(true);
      ok.setEnabled(true);
      cancel.setEnabled(true);
      
      if(skipInteractiveLogin) {
        // if we passed username/password via VM args, just cancel if the login failed
        handleButtonCancelWidgetSelected();
      }
    }
  }

  private String examineException(Throwable e) {
    String message = null;

    if(e != null) {
      if(e instanceof AccessDeniedException) {
        message = "Invalid username/password";      

      } else if( (e instanceof SocketTimeoutException) ||
          e.toString().contains("HTTP/1.1 404 Not Found")
          ) {
        message = "Could not connect to server.\n\n" +
            "Please verify that the server is running.";

      } else if(e instanceof AccessDeniedException) {
        message = "Invalid username/password";

      } else {
        // default to the cause's message.
        // if we can do better than that, we will overwrite the value.
        message = e.toString();
      }
    }
    return message;
  }

  /**
   * Method reportError.
   * @param e Exception
   */
  private void reportError(Exception e) {
    logger.error("Error logging in", loginJob.getError());

    Throwable cause = e.getCause(); System.out.println(e);
    Throwable rootCause = ExceptionUtils.getRootCause(e);
    String message = examineException(rootCause);
    if(message == null) {
      message = examineException(cause);
    }
    if(message == null) {
      message = examineException(e);
    }    

    if (message == null || message.trim().length() == 0) {
      message = "A " + e.getClass().getSimpleName() + " was thrown.";
    }

    File logFile = new File(CatLogger.getLogFilePath()); 
    message += "\n\n" + "See  " + logFile.getAbsolutePath() + " for more information.";

    MessageDialog.openError(getSplash(), "Login Failed", message);
    //ToolErrorHandler.handleError(message, null, true);
  }


  /**
   * Method aboutToRun.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(IJobChangeEvent)
   */
  @Override
  public void aboutToRun(IJobChangeEvent event) {
  }

  /**
   * Method awake.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(IJobChangeEvent)
   */
  @Override
  public void awake(IJobChangeEvent event) {
  }

  /**
   * Method done.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(IJobChangeEvent)
   */
  @Override
  public void done(IJobChangeEvent event) {
    postLogin();
  }

  /**
   * Method running.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(IJobChangeEvent)
   */
  @Override
  public void running(IJobChangeEvent event) {
    System.out.println("Running");
  }

  /**
   * Method scheduled.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(IJobChangeEvent)
   */
  @Override
  public void scheduled(IJobChangeEvent event) {
    message.setText("Authenticating...");
  }

  /**
   * Method sleeping.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(IJobChangeEvent)
   */
  @Override
  public void sleeping(IJobChangeEvent event) {
  }

  /**
   * Method getLabelHorizontalIndent.
   * @return int
   */
  protected int getLabelHorizontalIndent() {
    return F_LABEL_HORIZONTAL_INDENT;
  }

  /**
   * Method getVerticalIndent.
   * @return int
   */
  protected int getVerticalIndent() {
    return 180;
  }

  /**
   * Method getButtonWidthHint.
   * @return int
   */
  protected int getButtonWidthHint() {
    return F_BUTTON_WIDTH_HINT;
  }

  /**
   * Method getTextWidthHint.
   * @return int
   */
  protected int getTextWidthHint() {
    return F_TEXT_WIDTH_HINT;
  }

  /**
   * Method getColumnCount.
   * @return int
   */
  protected int getColumnCount() {
    return F_COLUMN_COUNT;
  }
}
