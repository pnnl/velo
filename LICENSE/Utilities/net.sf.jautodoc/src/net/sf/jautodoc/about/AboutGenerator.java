/*
 * gov.pnnl.velo
 * 
 * Authors: Cody Curry, Zoe Guillen
 * 
 */

package net.sf.jautodoc.about;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import net.sf.jautodoc.JAutodocPlugin;
import net.sf.jautodoc.preferences.AboutConstants;
import net.sf.jautodoc.preferences.Constants;
import net.sf.jautodoc.utils.Utils;

public class AboutGenerator {
  private final File rootDir;
  private final File aboutFilesDir;
  private final String libDirPath;
  private final String aboutFilePath;
  private final String aboutTemplateName;
  private final String date;

  private final JarLibrary jarLib;

  public AboutGenerator(final String rootDirPath) throws Exception {
    this(rootDirPath,
        rootDirPath + File.separator + AboutConstants.DEFAULT_ABOUT_DIR_OUT,
        rootDirPath + File.separator + AboutConstants.DEFAULT_ABOUT_DIR_IN,
        rootDirPath + File.separator + AboutConstants.DEFAULT_ABOUT_OUT,
        AboutConstants.DEFAULT_ABOUT_TEMPLATE,
        AboutConstants.DATE);
  }

  public AboutGenerator(final String rootDirPath, 
      final String aboutFilesDirPath, 
      final String libDirPath, 
      final String aboutFilePath,
      final String aboutTemplateName,
      final String date) throws Exception {
    this.rootDir = new File(rootDirPath);
    this.aboutFilesDir = new File(aboutFilesDirPath);
    this.libDirPath = libDirPath;
    this.aboutFilePath = aboutFilePath;
    this.aboutTemplateName = aboutTemplateName;
    this.date = date;

    this.jarLib = new JarLibrary();
  }

  public void populateJarLibrary() {
    jarLib.addJars(rootDir);
    jarLib.addJars(libDirPath, true);
  }

  public void generateAboutFiles() throws IOException {
    List<File> jars = jarLib.getJars();

    if (!jars.isEmpty() && Utils.overwriteDirectory(aboutFilesDir)) {

      for (File jar : jars) {
        File jarDir = new File(aboutFilesDir.getAbsolutePath() + File.separator 
            + FilenameUtils.removeExtension(jar.getName()));
        if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG + jarDir.getPath());
        jarLib.exportInterestingJarEntries(jar, jarDir);
      }

      if (aboutFilesDir.listFiles().length < 1) {
        if (AboutConstants.DEBUG_MODE) System.err.println(AboutConstants.DEBUG 
            + "Removing empty directory " + aboutFilesDir.getName());
        FileUtils.deleteQuietly(aboutFilesDir);
      }

    } else {
      if (AboutConstants.DEBUG_MODE) System.err.println(AboutConstants.DEBUG 
          + "Failed to create directory " + aboutFilesDir.getName() 
          + (jars.isEmpty() ? " due to missing license(s)" : ""));
    }
  }

  public void generateAboutHTML() throws Exception {
    String thirdPartyLibSection = "<h3>Third Party Content</h3>"
        + Constants.LINE_SEPARATOR
        + "<p>The Content includes items that have been sourced from third parties as set out below. If you did not receive this Content directly from Pacific Northwest National Laboratory, the following is provided for informational purposes only, and you should look to the Redistributor&rsquo;s license for terms and conditions of use.</p>"
        + Constants.LINE_SEPARATOR
        + "";

    URL fileUrl = FileLocator.find(JAutodocPlugin.getDefault().getBundle(), new Path("resources/" + aboutTemplateName), null);
    File aboutFile = new File(aboutFilePath);
    //start with a copy of the template, this is just the top part of the about.html file, the rest is filled out below
    FileUtils.copyURLToFile(fileUrl, aboutFile);

    String content = FileUtils.readFileToString(aboutFile);
    FileUtils.writeStringToFile(aboutFile, content.replaceAll("\\$\\{date\\}", date));

    //    ArrayList<Map<String, Serializable>> libList = new ArrayList<Map<String, Serializable>>();
    List<File> jars = jarLib.getJars();

    if (!jars.isEmpty()) {
      FileUtils.writeStringToFile(aboutFile, thirdPartyLibSection, true);
      Collections.sort(jars, NameFileComparator.NAME_COMPARATOR);

      StringBuilder jarDetails = new StringBuilder();

      for (File jar : jars) {
        String jarName = FilenameUtils.removeExtension(jar.getName());

        jarDetails.append("<h4>" + jarName + "</h4>" + Constants.LINE_SEPARATOR);

        File jarAboutDir = new File(this.aboutFilesDir.getPath() + File.separator + jarName);

        if (jarAboutDir.isDirectory()) {
          jarDetails.append("<ul>" + Constants.LINE_SEPARATOR);
          for (File file : jarAboutDir.listFiles()) {
            jarDetails.append("<li><a href=\"" + aboutFilesDir.getName() + File.separator 
                + FilenameUtils.removeExtension(jar.getName()) + File.separator 
                + file.getName() + "\">" + file.getName() + "</a></li>" + Constants.LINE_SEPARATOR);
          }
          jarDetails.append("</ul>" + Constants.LINE_SEPARATOR);
        }
      }
      FileUtils.writeStringToFile(aboutFile, jarDetails.toString(), true);
    }
    FileUtils.writeStringToFile(aboutFile, "</body>" + Constants.LINE_SEPARATOR + "</html>" + Constants.LINE_SEPARATOR, true);
  }

  //  // uses Velocity engine, but conflicts with Velocity singleton used by original plug-in
  //  public void generateAboutHTML() throws Exception {
  //    ArrayList<Map<String, Serializable>> libList = new ArrayList<Map<String, Serializable>>();
  //    List<File> jars = jarLib.getJars();
  //
  //    if (!jars.isEmpty()) {
  //      thirdParty = true;
  //      Collections.sort(jars, NameFileComparator.NAME_COMPARATOR);
  //
  //      for (File jar : jars) {
  //        Map<String, Serializable> libMap = new HashMap<String, Serializable>();
  //        ArrayList<Map<String, String>> fileList = new ArrayList<Map<String, String>>();
  //
  //        String jarName = FilenameUtils.removeExtension(jar.getName());
  //        libMap.put("name", jarName);
  //
  //        File jarAboutDir = new File(this.aboutFilesDir.getPath() + File.separator + jarName);
  //        if (jarAboutDir.isDirectory()) {
  //          for (File file : jarAboutDir.listFiles()) {
  //            Map<String, String> libDirMap = new HashMap<String, String>();
  //
  //            libDirMap.put("name", file.getName());
  //            libDirMap.put("path", file.getPath());
  //
  //            fileList.add(libDirMap);
  //          }
  //        }
  //
  //        libMap.put("fileList", fileList);
  //
  //        libList.add(libMap);
  //      }
  //    }
  //
  //    VelocityContext context = new VelocityContext();
  //    context.put("date", date);
  //    context.put("thirdParty", thirdParty);
  //    context.put("libraryList", libList);
  //
  //    if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG 
  //        + "getTemplate(" + aboutTemplateFile.getPath() + ")");
  //
  //    Template t = ve.getTemplate(aboutTemplateFile.getName());
  //
  //    StringWriter writer = new StringWriter();
  //
  //    t.merge(context, writer);
  //
  //    FileWriter fw = new FileWriter(aboutFilePath);
  //    fw.write(writer.toString());
  //
  //    fw.close();
  //  }

  public File getAboutFilesDir() {
    return aboutFilesDir;
  }

  public String getAboutFilePath() {
    return aboutFilePath;
  }

  public JarLibrary getJarLib() {
    return jarLib;
  }
}
