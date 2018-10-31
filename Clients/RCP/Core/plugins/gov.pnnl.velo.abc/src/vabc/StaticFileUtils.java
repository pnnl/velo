package vabc;

import java.io.File;

public class StaticFileUtils {
  
  private static StaticFileUtils instance;
  
  public static StaticFileUtils getInstance() {
    if(instance == null) {
      System.err.println("StaticFileUtils was not set, defaulting to hard coded values in StaticFileUtils.java");
      instance = new ClasspathFileUtils(); // TODO: Need to figure out how to set this...StaticFileUtils();
    }
    return instance;
  }
  

  /**
   * To be called by applications that choose to use classpathUutils.
   * @param instance
   */
  public static void setStaticFileUtils(StaticFileUtils instance) {
    StaticFileUtils.instance = instance;
  }
  
  public String getXsdPath() {
    return "D:\\ABC2\\abc\\references\\abc.xsd";
  }

  public String getXmlPath() {
    return  "D:\\ABC2\\abc\\references\\clm-agni-abc.xml";
  }

  public String getBaseIconsFolderPath() {
    return "D:\\ABC2\\abc\\references\\icons";
  } 
  
  public File getBaseIconFolder() {
    return new File(getBaseIconsFolderPath());
  }

  public File getWorkingDirectory() {
    return new File("D:\\ABC2\\abc\\references\\.abc");
  }

}
