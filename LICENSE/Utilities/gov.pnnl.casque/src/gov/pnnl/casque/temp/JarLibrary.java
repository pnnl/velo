package gov.pnnl.casque.temp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FilenameUtils;

public class JarLibrary {
  private List<File> jars;

  public JarLibrary() {
    jars = new ArrayList<File>();
  }

  public void addJar(final String filePath) {
    addJar(new File(filePath));
  }

  public void addJar(final File file) {
    if (FilenameUtils.isExtension(file.getName(), AboutConstants.LIBRARY_EXTENSION)
        && file.isFile() 
        && isValidJar(file)) {
      jars.add(file);
    }
  }

  public void addJars(final String filePath) {
    addJars(new File(filePath));
  }

  public void addJars(final File file) {
    if (file.isDirectory()) {
      for (File fileEntry : file.listFiles()) {
        addJar(fileEntry);
      }
    } else {
      addJar(file);
    }
  }

  public void exportInterestingJarEntries(final File fileIn, final File dirOut) throws IOException {
    FileInputStream fin = new FileInputStream(fileIn);
    BufferedInputStream bin = new BufferedInputStream(fin);
    JarInputStream jin = new JarInputStream(bin);
    JarEntry je = null;

    while ((je = jin.getNextJarEntry()) != null) {      
      if (pathIsInteresting(je.getName())) {
        if (!dirOut.isDirectory() && !dirOut.mkdir()) {
          if (AboutConstants.DEBUG_MODE) System.err.println(AboutConstants.DEBUG 
              + " unable to create directory " + dirOut.getName());
        } else {
          int len;
          byte[] buffer = new byte[AboutConstants.DEFAULT_BUFFER_SIZE];
          String filenameOut = File.separator + je.getName().substring(je.getName().lastIndexOf("/") + 1);

          OutputStream out = new FileOutputStream(dirOut.getPath() + filenameOut);

          while ((len = jin.read(buffer)) != -1) {
            out.write(buffer, 0, len);
          }

          out.close();
        }
      }
    }
    jin.close();
  }
  
  public static boolean pathIsInteresting(final String filePath) {
    if (filePath.matches(AboutConstants.MATCH_INTERESTING_PATH)) {
      return true;
    }
    return false;
  }

  public boolean isValidJar(final String filePath) {
    return isValidJar(new File(filePath));
  }

  public boolean isValidJar(final File file) {
    try {
      final JarFile jarFile = new JarFile(file);
      jarFile.close();
      return true;
    } catch (final IOException e) {}
    return false;
  }

  public void clearJars() {
    jars.clear();
  }

  public File getJar(final int index) {
    return jars.get(index);
  }

  public List<File> getJars() {
    return jars;
  }

  public void removeJar(final File file, final int index) {
    jars.remove(index);
  }

}
