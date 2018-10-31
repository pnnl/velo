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
package gov.pnnl.velo.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Utility methods to zip/unzip files.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class ZipUtils
{
  
  //ZCG: stole from internet: http://developer-tips.hubpages.com/hub/Zipping-and-Unzipping-Nested-Directories-in-Java-using-Apache-Commons-Compress
  /**
   * Creates a zip file at the specified path with the contents of the specified directory.
   * NB:
   *
   * @param directoryPath The path of the directory where the archive will be created. eg. c:/temp
   * @param zipPath The full path of the archive to create. eg. c:/temp/archive.zip
  
   * @throws IOException If anything goes wrong */
  public static void createZip(String directoryPath, String zipPath) throws IOException {
      FileOutputStream fOut = null;
      BufferedOutputStream bOut = null;
      ZipArchiveOutputStream tOut = null;

      try {
          fOut = new FileOutputStream(new File(zipPath));
          bOut = new BufferedOutputStream(fOut);
          tOut = new ZipArchiveOutputStream(bOut);
          addFileToZip(tOut, directoryPath, "");
      } finally {
          tOut.finish();
          tOut.close();
          bOut.close();
          fOut.close();
      }

  }

  //ZCG: stole from internet: http://developer-tips.hubpages.com/hub/Zipping-and-Unzipping-Nested-Directories-in-Java-using-Apache-Commons-Compress
  /**
   * Creates a zip entry for the path specified with a name built from the base passed in and the file/directory
   * name. If the path is a directory, a recursive call is made such that the full directory is added to the zip.
   *
   * @param zOut The zip file's output stream
   * @param path The filesystem path of the file/directory being added
   * @param base The base prefix to for the name of the zip file entry
   *
  
   * @throws IOException If anything goes wrong */
  private static void addFileToZip(ZipArchiveOutputStream zOut, String path, String base) throws IOException {
      File f = new File(path);
      String entryName = base + f.getName();
      ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);

      zOut.putArchiveEntry(zipEntry);

      if (f.isFile()) {
          FileInputStream fInputStream = null;
          try {
              fInputStream = new FileInputStream(f);
              IOUtils.copy(fInputStream, zOut);
              zOut.closeArchiveEntry();
          } finally {
              org.apache.commons.io.IOUtils.closeQuietly(fInputStream);
          }

      } else {
          zOut.closeArchiveEntry();
          File[] children = f.listFiles();

          if (children != null) {
              for (File child : children) {
                  addFileToZip(zOut, child.getAbsolutePath(), entryName + "/");
              }
          }
      }
  }
  
  /**
   * Method unzipFile.
   * @param zipFile File
   * @param baseFolder File
   * @throws IOException
   */
  public static void unzipFile(File zipFile, File baseFolder) throws IOException
  {
    int BUFFER = 2048;

    BufferedOutputStream dest = null;
    BufferedInputStream is = null;
    ZipEntry entry;
    ZipFile zipfileObj = new ZipFile(zipFile);
    Enumeration<?> e = zipfileObj.entries();
    while (e.hasMoreElements())
    {
      entry = (ZipEntry) e.nextElement();
      File targetFile = new File(baseFolder, entry.getName());

      if(entry.isDirectory()) {
        targetFile.mkdir();
      
      } else {
        is = new BufferedInputStream(zipfileObj.getInputStream(entry));
        int count;
        byte data[] = new byte[BUFFER];
        targetFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(targetFile);
        dest = new BufferedOutputStream(fos, BUFFER);
        while ((count = is.read(data, 0, BUFFER)) != -1)
        {
          dest.write(data, 0, count);
        }
        dest.flush();
        dest.close();
        is.close();
      }
    }
  }
  
}
