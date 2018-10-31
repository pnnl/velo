package gov.pnnl.velo.tif.model;

import java.io.File;


public class KeyboardInteractiveCredentials implements Credentials {
  String username;
  String credential;
  
  public void setUsername(String username) {
    this.username = username;
  }

  public void setCredential(String credential) {
    this.credential = credential;
  }

  @Override
  public String getUserName() {
    
    // TODO Auto-generated method stub
    return username;
  }

  @Override
  public String getCredential() {
    // TODO Auto-generated method stub
    return credential;
  }

  @Override
  public File getIdentityFile() {
    return null;
  }
  
}