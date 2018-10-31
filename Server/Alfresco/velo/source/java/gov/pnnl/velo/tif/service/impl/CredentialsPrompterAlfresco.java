/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import org.kepler.ssh.AuthFailedException;

import gov.pnnl.velo.tif.service.CredentialsPrompter;

/**
 * @author d3k339
 *
 */
public class CredentialsPrompterAlfresco implements CredentialsPrompter {

  @Override
  public String promptForCredentials(String title, String message, String errMessage, String[] prompts, boolean[] echo) {
    // If JSCH is prompting us for credentials here, that means we need to throw an exception to let the client know
    // which credentials are required
    throw new AuthFailedException("Authentication Failed.", title, message, errMessage, prompts);
  }


}
