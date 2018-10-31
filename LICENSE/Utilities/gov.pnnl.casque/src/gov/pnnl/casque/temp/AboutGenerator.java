package gov.pnnl.casque.temp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class AboutGenerator {
  private final File rootDir;
  private final File aboutFilesDir;
  private final String libDirPath;
  private final String aboutFilePath;
  private final String aboutTemplatePath;
  
  private final JarLibrary jarLib;
  
  private boolean thirdParty;
  
  /**
   * TODO:
   *  [ ] change resources
   */
  public AboutGenerator(final String rootDirPath) {
    this(rootDirPath,
        rootDirPath + File.separator + AboutConstants.DEFAULT_ABOUT_DIR_OUT,
        rootDirPath + File.separator + AboutConstants.DEFAULT_ABOUT_DIR_IN,
        rootDirPath + File.separator + AboutConstants.DEFAULT_ABOUT_OUT,
        "resources" + File.separator + AboutConstants.DEFAULT_ABOUT_TEMPLATE);
  }

  public AboutGenerator(final String rootDirPath, 
      final String aboutFilesDirPath, 
      final String libDirPath, 
      final String aboutFilePath, 
      final String aboutTemplatePath) {
    this.rootDir = new File(rootDirPath);
    this.aboutFilesDir = new File(aboutFilesDirPath);
    this.libDirPath = libDirPath;
    this.aboutFilePath = aboutFilePath;
    this.aboutTemplatePath = aboutTemplatePath;
    
    this.jarLib = new JarLibrary();
    this.thirdParty = false;
    
    if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG 
        + "rootDir(" + rootDir.getPath() + ")" + System.lineSeparator() + "\t" 
        + "aboutFilesDir(" + aboutFilesDir.getPath() + ")" + System.lineSeparator() + "\t" 
        + "libDirPath(" + libDirPath + ")" + System.lineSeparator() + "\t" 
        + "aboutFilePath(" + aboutFilePath + ")" + System.lineSeparator() + "\t" 
        + "aboutTemplatePath(" + aboutTemplatePath + ")" + System.lineSeparator() + "\t" 
        + "jarLib(" + jarLib.getJars().toString() + ")" + System.lineSeparator() + "\t" 
        + "thirdParty(" + thirdParty + ")");
  }

  public void populateJarLibrary() {
    jarLib.addJars(rootDir);
    jarLib.addJars(libDirPath);
  }

  public void generateAboutFiles() throws IOException {
    if (!jarLib.getJars().isEmpty() && Utils.overwriteDirectory(aboutFilesDir)) {
      thirdParty = true;
      
      for (File jar : jarLib.getJars()) {
        File jarDir = new File(aboutFilesDir.getAbsolutePath() + File.separator 
            + FilenameUtils.removeExtension(jar.getName()));
        if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG + jarDir.getPath());
        jarLib.exportInterestingJarEntries(jar, jarDir);
      }
      
      //  if (aboutFilesDir.listFiles().length < 1) {
      //    if (AboutConstants.DEBUG_MODE) System.err.println(AboutConstants.DEBUG 
      //      + "Removing empty directory " + aboutFilesDir.getName());
      //    FileUtils.deleteQuietly(aboutFilesDir);
      //  }
      
    } else {
      if (AboutConstants.DEBUG_MODE) System.err.println(AboutConstants.DEBUG 
          + "Failed to create directory " + aboutFilesDir.getName());
    }
  }
  
  public void generateAboutHTML() throws Exception {
    ArrayList<Map<String, Serializable>> libList = new ArrayList<Map<String, Serializable>>();

    if (!jarLib.getJars().isEmpty()) {
      thirdParty = true;
      
      for (File jar : jarLib.getJars()) {
        Map<String, Serializable> libMap = new HashMap<String, Serializable>();
        ArrayList<Map<String, String>> fileList = new ArrayList<Map<String, String>>();
        
        String jarName = FilenameUtils.removeExtension(jar.getName());
        libMap.put("name", jarName);

        File jarAboutDir = new File(this.aboutFilesDir.getPath() + File.separator + jarName);
        if (jarAboutDir.isDirectory()) {
          for (File file : jarAboutDir.listFiles()) {
            Map<String, String> libDirMap = new HashMap<String, String>();

            libDirMap.put("name", file.getName());
            libDirMap.put("path", file.getPath());

            fileList.add(libDirMap);
          }
        }

        libMap.put("fileList", fileList);

        libList.add(libMap);
      }
    }

    VelocityContext context = new VelocityContext();
    context.put("date", "2013");
    context.put("thirdParty", thirdParty);
    context.put("libraryList", libList);
    
    VelocityEngine ve = new VelocityEngine();
    ve.init();

    Template t = ve.getTemplate(aboutTemplatePath);

    StringWriter writer = new StringWriter();

    t.merge(context, writer);

    FileWriter fw = new FileWriter(aboutFilePath);
    fw.write(writer.toString());
    
    fw.close();
  }
  
  public File getAboutFilesDir() {
    return aboutFilesDir;
  }

  public String getAboutFilePath() {
    return aboutFilePath;
  }
}
