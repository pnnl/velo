package gov.pnnl.casque.temp;

import java.io.File;
import org.apache.commons.io.FileUtils;

public final class Main {

  /**
   *  TODO 
   *  [?] Find any and all interesting files from project root directory
   *    [?] If any found, create/overwrite about_files directory
   *    [?] Copy-over all interesting files found to about_files directory
   *  [X] Find and list all JARs found within project root and lib directories
   *  [X] If JARs list is not empty, then create/overwrite an about_files directory
   *  [X] Within about_files directory:
   *    [X] create a new directory for every Jar file listed ...
   *    [X] only if the Jar file has any interesting files found within
   *    [X] only look in Jar root and META-INF folders
   *  [X] Move exportJarEntries method to JarLibrary class
   *  [X] Move overwriteDirectory method to Utils class
   *  
   *    [X] merge demo code with main
   *  
   *  [ ] ensure no problems with root directory being a path other than "."
   */
  public static void main(String[] args) {
    //    File rootFile = new File("C:\\Projects\\Utilities\\Workspace\\gov.pnnl.casque");
    //    File rootFile = new File("C:\\Projects\\Utilities\\Workspace\\net.sf.jautodoc");
    File rootFile = new File("");
    
    if (AboutConstants.DEBUG_MODE) System.out.println(AboutConstants.DEBUG + rootFile.getAbsolutePath());
    
    AboutGenerator aboutGen = new AboutGenerator(rootFile.getAbsolutePath());

    aboutGen.populateJarLibrary();

    try {
      aboutGen.generateAboutFiles();
      aboutGen.generateAboutHTML();
    } catch (Exception e) {
      e.printStackTrace();
    }

    FileUtils.deleteQuietly(aboutGen.getAboutFilesDir());
    FileUtils.deleteQuietly(new File(aboutGen.getAboutFilePath()));
  }

}
