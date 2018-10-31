package gov.pnnl.velo.tif.model;

import java.io.File;

public interface Credentials {
  
  public String getUserName();
  public String getCredential();
  public File   getIdentityFile();

}