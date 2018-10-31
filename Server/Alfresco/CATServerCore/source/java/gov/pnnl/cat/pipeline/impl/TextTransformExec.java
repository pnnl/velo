/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.pipeline.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 */
public class TextTransformExec {
  
  private static final Class[] parameters = new Class[] {URL.class};


  /**
   * Method addFileToClasspath.
   * @param f File
   * @throws IOException
   */
  public static void addFileToClasspath(File f) throws IOException
  {
      //f.toURL is deprecated
    addURLToClasspath(f.toURI().toURL());
  }

  /**
   * Method addURLToClasspath.
   * @param u URL
   * @throws IOException
   */
  public static void addURLToClasspath(URL u) throws IOException
  {
      URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      Class sysclass = URLClassLoader.class;

      try {
          Method method = sysclass.getDeclaredMethod("addURL", parameters);
          method.setAccessible(true);
          method.invoke(sysloader, new Object[] {u});
      } catch (Throwable t) {
          t.printStackTrace();
          throw new IOException("Error, could not add URL to system classloader");
      }

  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    
    File source = null;
    File destination = null;
    String sourceEncoding = null;
    String destinationEncoding = null;
    String readerMimetype = null;
    String writerMimetype = null;
    File libFolder = null;
    File classesFolder = null;

    try {
      if (args.length > 0) {
        source = new File(args[1]);
        destination = new File(args[2]);
        sourceEncoding = args[3];
        destinationEncoding = args[4];
        readerMimetype = args[5];
        writerMimetype = args[6];
        libFolder = new File(args[7]);
        classesFolder = new File(args[8]);

        // First we need to dynamically load the classpath
        // We can't pass in the classpath because it is too huge and it is causing
        // the process builder to crash on Windows
        addFileToClasspath(classesFolder);
        for(File file : libFolder.listFiles()) {
          if(file.getName().toLowerCase().endsWith(".jar")) {
            addFileToClasspath(file);
          }
        }
        
        Class transformClass = Class.forName(args[0]);
        ContentTransformer remoteTransform = (ContentTransformer) transformClass.newInstance();

        FileContentReader reader = new FileContentReader(source);
        FileContentWriter writer = new FileContentWriter(destination);

        reader.setMimetype(readerMimetype);
        writer.setMimetype(writerMimetype);
        reader.setEncoding(sourceEncoding);
        writer.setEncoding(destinationEncoding);
        reader.setLocale(I18NUtil.getLocale());
        writer.setLocale(I18NUtil.getLocale());

        remoteTransform.transform(reader, writer);
      } else {
        System.err.println("Bad arguments for text transform.");
        System.exit(2);
      }
    } catch (Throwable t) {
      System.err.println("Failed to transform file.");
      t.printStackTrace();
      System.exit(1);
    }

    System.exit(0);
  }


}
