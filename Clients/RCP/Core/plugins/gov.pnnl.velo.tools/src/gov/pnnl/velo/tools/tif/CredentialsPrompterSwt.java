/**
 * 
 */
package gov.pnnl.velo.tools.tif;

import gov.pnnl.cat.ui.validators.NotEmptyStringValidator;
import gov.pnnl.velo.tif.service.CredentialsPrompter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * TODO: we need to make this dialog correctly handle case of multiple prompts
 *
 */
public class CredentialsPrompterSwt implements CredentialsPrompter {

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.PasswordDialogProvider#displayPasswordDialog(java.lang.String)
   */
  @Override
  public String promptForCredentials(final String title, final String message, final String errMessage, final String[] prompts, final boolean[] echo) {
    
   final String[] password = new String[1];
   Display.getDefault().syncExec(new Runnable() {
     
     @Override
     public void run() {
       Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
       
       SwtPasswordDialog dialog = new SwtPasswordDialog(shell, "Compute Server Authentication", message, errMessage,
           prompts, echo, null, new NotEmptyStringValidator());
       if(dialog.open() == Dialog.OK) {
         
         password[0] = dialog.getValue();
       }
     }
   });
   return password[0];
  }

}
