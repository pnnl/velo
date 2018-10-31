package gov.pnnl.casque.temp;

import java.io.File;

import org.apache.commons.io.FileUtils;

public final class Utils {
  private Utils() {
    throw new RuntimeException("Non-instantiable class");
  }

  public static boolean overwriteDirectory(final String dirPath) {
    return overwriteDirectory(new File(dirPath));
  }

  public static boolean overwriteDirectory(final File dir) {
    if (dir.isDirectory()) {
      FileUtils.deleteQuietly(dir);
    }
    if (dir.mkdir()) {
      return true;
    }
    return false;
  }

}
