/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.service.CredentialsPrompter;

import java.io.IOException;

import org.kepler.ssh.MaskedTextPasswordField;

/**
 * Use commandline to prompt user for password/passphrase
 */
public class CredentialsPrompterCommandLine implements CredentialsPrompter {

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.PasswordDialogProvider#displayPasswordDialog(java.lang.String)
   */
  @Override
  public String promptForCredentials(String title, String message, String errMessage, String[] prompts, boolean[] echo) {
    
    String credential = null;
    int i = 0;
   
    try {
      for (i = 0; i < prompts.length; i++) {
        char password[] = null;
        password = MaskedTextPasswordField.getPassword(System.in, prompts[i] + " ");
        if (password != null) {
          credential = String.valueOf(password);
        } else {
          break;
        }
      }
    } catch (IOException e) {
      System.err.println("Error at reading password: " + e);
    }
    if (i == prompts.length) {
      return credential;
    } else {
      return null;
    }
  }
  
}
