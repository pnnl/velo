package gov.pnnl.velo.tif.model;

import java.io.File;


public class SshKeyCredentials implements Credentials {

  String username;
  String credential;
  File   identity;
  
  @Override
  public String getUserName() {
    return username;
  }

  @Override
  public String getCredential() {
    return credential;
  }
  
  public File getIdentityFile(){
    return identity;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }

  public void setCredential(String credential) {
    this.credential = credential;
  }
  
  public void setIdentityFile(File identity){
    this.identity = identity;
  }
  

}
