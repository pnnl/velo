package gov.pnnl.velo.tif.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Used for transferring an ssh private key file contents to the Velo server
 * for user authentication.  In this case, we send the contents of the file,
 * not a java File object.
 * server
 * @author d3k339
 *
 */
public class SshKeyTransferCredentials implements Credentials {

  String username;
  String credential;
  String keyFileContents;
  
  @Override
  public String getUserName() {
    return username;
  }

  @Override
  public String getCredential() {
    return credential;
  }
  
  public File getIdentityFile(){
    // create a temporary file with the contents
    File identityFile = null;
    try {
      identityFile = File.createTempFile("key", "id_dsa");
      FileUtils.writeStringToFile(identityFile, keyFileContents);
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return identityFile;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }

  public void setCredential(String credential) {
    this.credential = credential;
  }
  
  /**
   * @param keyFileContents the keyFileContents to set
   */
  public void setKeyFileContents(String keyFileContents) {
    this.keyFileContents = keyFileContents;
  }

}
