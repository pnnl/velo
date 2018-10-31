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
package gov.pnnl.cat.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.springframework.util.StringUtils;

/**
 * Utility methods to zip/unzip files.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class ZipUtils
{
  protected static final Log logger = LogFactory.getLog(ZipUtils.class);
  private static final int BUFFER_SIZE = 1024;
  
  /**
   * Method tarGzUnzipFile.
   * @param gzipFile File
   * @param destFolder File
   * @throws Exception
   */
  public static void tarGzUnzipFile(File gzipFile, File destFolder) throws Exception {
    FileInputStream fis = null;
    try {
        fis = new FileInputStream(gzipFile);
        UntarCompressionMethod compression = new UntarCompressionMethod();
        compression.setValue("gzip");
        expandTarStream(gzipFile.getPath(), fis, destFolder, compression);
  
    } finally {
        FileUtils.close(fis);
    }

  }
  
  /**
   * Method expandTarStream.
   * @param name String
   * @param stream InputStream
   * @param dir File
   * @param compression UntarCompressionMethod
   * @throws IOException
   */
  private static void expandTarStream(String name, InputStream stream, File dir, UntarCompressionMethod compression)
  throws IOException {
    TarInputStream tis = null;
    try {
      tis =
        new TarInputStream(compression.decompress(name,
            new BufferedInputStream(stream)));
      logger.debug("Expanding: " + name + " into " + dir);
      TarEntry te = null;
      FileNameMapper mapper = new IdentityMapper();
      while ((te = tis.getNextEntry()) != null) {
        extractTarredFile(FileUtils.getFileUtils(), null, dir, tis,
            te.getName(), te.getModTime(),
            te.isDirectory(), mapper);
      }
      logger.debug("expand complete");
    } finally {
      FileUtils.close(tis);
    }
  }

  /**
   * Method extractTarredFile.
   * @param fileUtils FileUtils
   * @param srcF File
   * @param dir File
   * @param compressedInputStream InputStream
   * @param entryName String
   * @param entryDate Date
   * @param isDirectory boolean
   * @param mapper FileNameMapper
   * @throws IOException
   */
  private static void extractTarredFile(FileUtils fileUtils, File srcF, File dir,
      InputStream compressedInputStream,
      String entryName, Date entryDate,
      boolean isDirectory, FileNameMapper mapper) throws IOException {

    String[] mappedNames = mapper.mapFileName(entryName);
    if (mappedNames == null || mappedNames.length == 0) {
      mappedNames = new String[] {entryName};
    }
    File f = fileUtils.resolveFile(dir, mappedNames[0]);
    try {
      logger.debug("expanding " + entryName + " to " + f);
      // create intermediary directories - sometimes zip don't add them
      File dirF = f.getParentFile();
      if (dirF != null) {
        dirF.mkdirs();
      }

      if (isDirectory) {
        f.mkdirs();
      } else {
        byte[] buffer = new byte[BUFFER_SIZE];
        int length = 0;
        FileOutputStream fos = null;
        try {
          fos = new FileOutputStream(f);

          while ((length =
            compressedInputStream.read(buffer)) >= 0) {
            fos.write(buffer, 0, length);
          }

          fos.close();
          fos = null;
        } finally {
          FileUtils.close(fos);
        }
      }

      fileUtils.setFileLastModified(f, entryDate.getTime());
    } catch (FileNotFoundException ex) {
      logger.warn("Unable to expand to file " + f.getPath());
    }

  }
  
  /**
   * Method untarFile.
   * @param tarFile File
   * @param destFolder File
   * @throws Exception
   */
  public static void untarFile(File tarFile, File destFolder) throws Exception {
    FileInputStream fis = null;
    try {
        fis = new FileInputStream(tarFile);
        UntarCompressionMethod compression = new UntarCompressionMethod();
        expandTarStream(tarFile.getPath(), fis, destFolder, compression);
  
    } finally {
        FileUtils.close(fis);
    }
    
  }
  

  /**
   * Method gUnzipFile.
   * @param gzipFile File
   * @param destFolder File
   * @return File
   * @throws Exception
   */
  public static File gUnzipFile(File gzipFile, File destFolder) throws Exception {
    FileOutputStream outStream = null;
    GZIPInputStream zipInStream = null;
    FileInputStream fileInStream = null;
    
    File unzippedFile;

    try {
      String fileName = gzipFile.getName();
      if(StringUtils.endsWithIgnoreCase(fileName, ".gz")) {
        fileName = fileName.substring(0, fileName.length()-3);
      } else {
        fileName = fileName.substring(0, fileName.length()-4);
      }
      unzippedFile = new File(destFolder, fileName);
      outStream = new FileOutputStream(unzippedFile);
      fileInStream = new FileInputStream(gzipFile);
      zipInStream = new GZIPInputStream(fileInStream);
      byte[] buffer = new byte[8 * 1024];
      int count = 0;
      do {
        outStream.write(buffer, 0, count);
        count = zipInStream.read(buffer, 0, buffer.length);
      } while (count != -1);

    } finally {
      closeStream(fileInStream);
      closeStream(outStream);
      closeStream(zipInStream);
    }

    return unzippedFile;
  }

  /**
   * Method closeStream.
   * @param stream Closeable
   */
  private static void closeStream(Closeable stream) {
    if(stream != null) {
      try {
        stream.close();
      } catch (Throwable e) {
        logger.error("Failed to close stream.", e);
      }
    }
  }


  /**
   * @param filesToZip
   * @param zipFile
   * @throws Exception
   */
  public static void zipFiles(List<File> filesToZip, File zipFile) throws Exception {
    
    FileOutputStream fOut = null;
    BufferedOutputStream bOut = null;
    ZipArchiveOutputStream tOut = null;

    try {
        fOut = new FileOutputStream(zipFile);
        bOut = new BufferedOutputStream(fOut);
        tOut = new ZipArchiveOutputStream(bOut);
        for(File file : filesToZip) {
          addFileToZip(tOut, file.getAbsolutePath(), "");
        }
        
    } finally {
        tOut.finish();
        tOut.close();
        bOut.close();
        fOut.close();
    }
  }

  
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

  /**
   * Method isCompressed.
   * @param file File
   * @return boolean
   */
  public static boolean isCompressed(File file) {
    return isZip(file) || isTarGz(file) || isGZip(file) || isTar(file);
  }
  
  public static boolean isCompressed(String fileName) {
    return isZip(fileName) || isTarGz(fileName) || isGZip(fileName) || isTar(fileName);
  }
  
  /**
   * Method isTar.
   * @param file File
   * @return boolean
   */
  public static boolean isTar(File file) {
   return isTar(file.getName());
  }
  
  public static boolean isTar(String fileName) {
    return(StringUtils.endsWithIgnoreCase(fileName, ".tar"));
  }
  
  /**
   * Method isZip.
   * @param file File
   * @return boolean
   */
  public static boolean isZip(File file) {
    return isZip(file.getName());
  }
  
  public static boolean isZip(String fileName) {
    return(StringUtils.endsWithIgnoreCase(fileName, ".zip"));    
  }
  
  /**
   * Method isTarGz.
   * @param file File
   * @return boolean
   */
  public static boolean isTarGz(File file) {
    return isTarGz(file.getName());
  }
  
  public static boolean isTarGz(String fileName) {
    return (StringUtils.endsWithIgnoreCase(fileName, ".tar.gz") ||
        StringUtils.endsWithIgnoreCase(fileName, ".tgz")); 
  }
  
  /**
   * Method isGZip.
   * @param file File
   * @return boolean
   */
  public static boolean isGZip(File file) {
    return isGZip(file.getName());
  }
  
  public static boolean isGZip(String fileName) {
    return(StringUtils.endsWithIgnoreCase(fileName, ".gz") || 
        StringUtils.endsWithIgnoreCase(fileName, ".gzip"));
  }
  
  
}
