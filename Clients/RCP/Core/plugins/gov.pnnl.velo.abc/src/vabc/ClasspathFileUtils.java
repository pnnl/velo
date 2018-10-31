package vabc;

import gov.pnnl.velo.util.ClasspathUtils;

import java.io.File;

public class ClasspathFileUtils extends StaticFileUtils {

  @Override
  public String getXsdPath() {
    return ClasspathUtils.getFileFromClassFolder(ClasspathFileUtils.class, "abc.xsd").getAbsolutePath();
  }

  @Override
  public String getXmlPath() {
    return ClasspathUtils.getFileFromClassFolder(ClasspathFileUtils.class, "params.xml").getAbsolutePath();
  }

  @Override
  public String getBaseIconsFolderPath() {
    return ClasspathUtils.getFileFromClassFolder(ClasspathFileUtils.class, "icons").getAbsolutePath();
  } 

  @Override
  public File getBaseIconFolder() {
    return ClasspathUtils.getFileFromClassFolder(ClasspathFileUtils.class, "icons");
  }

  @Override
  public File getWorkingDirectory() {
    return new File(".");
  }
}
