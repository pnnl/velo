package org.kepler.ssh;

/**
 * Added so we can communicate back from the velo server during a server-side 
 * job launch that the provided authentication credentials were invalid.
 * (e.g., user made a typo in the password)
 * @author d3k339
 */
public class AuthFailedException extends SshException {
  private String title;
  private String promptMessage;
  private String errMessage;
  private String[] prompts;
  
  private static final long serialVersionUID = 1L;

  public AuthFailedException() {
    super("");
  }

  public AuthFailedException(String message, String title, String promptMessage, String errMessage, String[] prompts) {
    super(message);
    this.title = title;
    this.promptMessage = promptMessage;
    this.errMessage = errMessage;
    this.prompts = prompts;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPromptMessage() {
    return promptMessage;
  }

  public void setPromptMessage(String promptMessage) {
    this.promptMessage = promptMessage;
  }

  public String getErrMessage() {
    return errMessage;
  }

  public void setErrMessage(String errMessage) {
    this.errMessage = errMessage;
  }

  public String[] getPrompts() {
    return prompts;
  }

  public void setPrompts(String[] prompts) {
    this.prompts = prompts;
  }
  
  
}
