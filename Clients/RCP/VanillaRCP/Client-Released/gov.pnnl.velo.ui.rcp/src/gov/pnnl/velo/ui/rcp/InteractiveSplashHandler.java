/**
 * 
 */
package gov.pnnl.velo.ui.rcp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.Hyperlink;

import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.velo.core.util.ForgotAccountInfoService;
import gov.pnnl.velo.ui.validators.NotEmptyStringValidator;

/**
 * Override the CAT default to make some
 * formatting changes.
 *
 */
public class InteractiveSplashHandler extends gov.pnnl.cat.ui.common.rcp.InteractiveSplashHandler {

  protected boolean showServerLabel() {
    // sbrsfa value
    return false;
    
    // vanilla rcp value
    //return true;
  }



  @Override
  protected Color getTextColor() {
    return getSplash().getDisplay().getSystemColor(SWT.COLOR_BLACK);
  }

  @Override
  protected Color getLabelColor() {
    // use black font
    return getSplash().getDisplay().getSystemColor(SWT.COLOR_BLACK);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.common.rcp.InteractiveSplashHandler#showBuildVersion()
   */
  @Override
  protected boolean showBuildVersion() {
    // sbrsfa value
    return false;

    // vanilla value
    //return true;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.common.rcp.InteractiveSplashHandler#getBuildVersion()
   */
  @Override
  protected String getBuildVersion() {
    return VeloRCPPlugin.getDefault().getBuildVersion();
  }

  @Override
  protected void hookAccountOptions(final Composite loginComposite) {
    final List<String> supportEmailAddresses = new ArrayList<String>();
    IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(CatRcpPlugin.EXT_POINT_EMAIL_ADDRESS);

    for (IConfigurationElement element : elements) {
      String toEmail = element.getAttribute(CatRcpPlugin.ATT_TO_EMAIL_ADDRESS);
      if (toEmail != null) {
        supportEmailAddresses.add(toEmail);
      }
    }

    Composite linkComposite = new Composite(loginComposite, SWT.NONE);
    GridLayout linkLayout = new GridLayout(5, false);
    linkLayout.marginHeight = 0;
    linkLayout.marginWidth = 0;
    linkComposite.setLayout(linkLayout);

    GridData linkdata = new GridData(SWT.LEFT, SWT.TOP, false, false);
    linkdata.horizontalSpan = 3;
    linkdata.verticalIndent = 4;
    linkdata.horizontalIndent = getLabelHorizontalIndent();
    linkComposite.setLayoutData(linkdata);
    //linkComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));

    // Draw a create account link
    Hyperlink linkRequestAccount = new Hyperlink(linkComposite, SWT.NONE);
    linkRequestAccount.setText("Request Account"); //$NON-NLS-1$
    linkRequestAccount.setToolTipText("Request Account");
    //    GridData data = new GridData(SWT.LEFT, SWT.FILL, false, false);
    //    data.horizontalIndent = 0;
    //    linkRequestAccount.setLayoutData(data);

    linkRequestAccount.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));

    linkRequestAccount.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
        openEmail(supportEmailAddresses, "Velo Account Request", "Please create a Velo account for the following person:\n\nName:\nEmail:\nInstitution:\n");

        //BareBonesBrowserLaunch.openURL( VeloUIPlugin.getDefault().getWikiUrl() + "/Special:RequestAccount");
      }
    });    

    Label pipe1 = new Label(linkComposite, SWT.NONE);
    pipe1.setText("|");

    // Draw a forgot password link (needs to pop up a dialog asking for the user to enter their username OR email address assoc'd with their account)
    Hyperlink linkForgotPassword = new Hyperlink(linkComposite, SWT.NONE);
    linkForgotPassword.setText("Forgot Password"); //$NON-NLS-1$
    linkForgotPassword.setToolTipText("Request to reset your password.");

    //    data = new GridData(SWT.NONE, SWT.NONE, false, false);
    //    data.horizontalIndent = 8;
    //    linkForgotPassword.setLayoutData(data);
    linkForgotPassword.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));

    linkForgotPassword.addHyperlinkListener(new HyperlinkAdapter() {


      @Override
      public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
        InputDialog dialog = new InputDialog(loginComposite.getShell(), "Enter username or email address", 
            "Enter your username or email address associated with your account",  null, new NotEmptyStringValidator());
        if(dialog.open() == Dialog.OK) {
          String usernameOremail = dialog.getValue();
          try{
            ForgotAccountInfoService.getInstance().resetPassword(usernameOremail, "Velo", supportEmailAddresses.get(0));
            MessageDialog.openInformation(loginComposite.getShell(), "Password reset", "Your password has been reset.  Your new password was sent to your email address.");
          }catch(Throwable ex){
            MessageDialog.openError(loginComposite.getShell(), "Error", ex.getMessage());
          }
        }
      }
    }); 

    Label pipe2 = new Label(linkComposite, SWT.NONE);
    pipe2.setText("|");

    // Draw a forgot username link (needs to pop up a dialog asking for the user to enter their email address assoc'd with their account)
    Hyperlink linkForgotUsername = new Hyperlink(linkComposite, SWT.NONE);
    linkForgotUsername.setText("Forgot Username"); //$NON-NLS-1$
    linkForgotUsername.setToolTipText("Request to have your username sent to you in an email.");

    //    data = new GridData(SWT.NONE, SWT.NONE, false, false);
    //    data.horizontalIndent = 8;
    //    linkForgotUsername.setLayoutData(data);
    linkForgotUsername.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));

    linkForgotUsername.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
        InputDialog dialog = new InputDialog(loginComposite.getShell(), "Enter email address", 
            "Enter the email address associated with your account",  null, new NotEmptyStringValidator());
        if(dialog.open() == Dialog.OK) {
          String email = dialog.getValue();
          try{
            ForgotAccountInfoService.getInstance().emailUsername(email, "Velo", supportEmailAddresses.get(0));
            MessageDialog.openInformation(loginComposite.getShell(), "Username sent", "Your username has been sent to your email address.");
          }catch(Throwable ex){
            MessageDialog.openError(loginComposite.getShell(), "Error", ex.getMessage());
          }
        }
      }
    }); 
  }

  public void openEmail(List<String> to, String subject,String body) {
    
    String mailto = "mailto:";
    for(int i = 0; i < to.size(); i++) {
      if(i > 0) {
        mailto += "; ";
      }
      mailto += enc(to.get(i));
    }
    mailto += "?subject=" + enc(subject) + "&body=" + enc(body);
    Program.launch(mailto);
  }

  private String enc(String p) {
    if (p == null)
      p = "";
    try {
      return URLEncoder.encode(p, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException();
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.common.rcp.InteractiveSplashHandler#getLabelHorizontalIndent()
   */
  @Override
  protected int getLabelHorizontalIndent() { 
    return 305;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.common.rcp.InteractiveSplashHandler#getVerticalIndent()
   */
  @Override
  protected int getVerticalIndent() {
    // sbrsfa value
    return 50;
    
    // vanilla value
    //return 110;
  }



  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.common.rcp.InteractiveSplashHandler#getTextWidthHint()
   */
  @Override
  protected int getTextWidthHint() {
    return 250;
  }


}
