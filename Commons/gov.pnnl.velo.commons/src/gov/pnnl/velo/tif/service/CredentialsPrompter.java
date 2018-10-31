/**
 * 
 */
package gov.pnnl.velo.tif.service;

/**
 * This interface provides the password dialog to the underlying SshSession connection so that
 * deployments can create the appropriate UI to match their environment.
 *
 */
public interface CredentialsPrompter
 {
  
  /**
   * Prompt for user password credentials and return the entered password.  Implementers are
   * free to prompt for password any way they want.
   * @param promptLabel
   * @return the password obtained via the prompt
   */
  public String promptForCredentials(String title, String message, String errMessage, String[] prompts, boolean[] echo);

}
