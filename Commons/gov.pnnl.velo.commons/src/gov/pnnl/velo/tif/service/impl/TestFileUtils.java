package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;


public class TestFileUtils {

  /**
   * @param args
   */
  public static void main(String[] args) {

    File localDir = new File("/Users/d3x140/projects/EPA");
//    Collection<File> listFiles = FileUtils.listFiles(localDir, TrueFileFilter.INSTANCE, null);
//    System.out.println(listFiles);
//    for(File file:listFiles){
//      System.out.println(file.getName() + " : " + file.isFile());
//    }
//    System.out.println("======");
//    listFiles = FileUtils.listFilesAndDirs(localDir,TrueFileFilter.INSTANCE,null);
//    listFiles = Arrays.asList(localDir.listFiles());
//    for(File file:listFiles){
//      System.out.println(file.getName() + " : " + file.isFile());
//    }
    
    System.out.println("======");
    List<Resource> dirResources = new ArrayList<Resource>();
    getSubFolders(localDir,"/User Home/Admin/",dirResources);
    for(Resource resource:dirResources){
      System.out.println(resource.getPath());
    }
    
    System.out.println("======");
    Map<File, CmsPath> filesToUpload = new HashMap<File, CmsPath> ();
    getFilesRecursive(localDir,"/User Home/Admin/"+localDir.getName(),filesToUpload);
    for(File localFile:filesToUpload.keySet()){
      System.out.println(localFile.getAbsolutePath() + ": " + filesToUpload.get(localFile).toDisplayString());
    }
  }
  
  private static void getSubFolders(File localDir, String remoteAlfDir, List<Resource> dirResources) {

    List<File> dirs;
    FileFilter f = DirectoryFileFilter.INSTANCE;
    dirs = Arrays.asList(localDir.listFiles(f));
    if(dirs.isEmpty())
      return;
    else {
      for(File subdir:dirs){
        CmsPath path = new CmsPath(remoteAlfDir).append(subdir.getName());
        Resource r = new Resource(VeloConstants.TYPE_FOLDER, path);
        dirResources.add(r);
        getSubFolders(subdir,remoteAlfDir+"/"+subdir.getName(),dirResources);
      }
    }
  }

  private static void getFilesRecursive(File localDir, String remoteAlfDir, Map<File, CmsPath> filesToUpload) {
    remoteAlfDir = remoteAlfDir.endsWith("/")?remoteAlfDir:remoteAlfDir+"/";
    Collection<File> files;
    files = Arrays.asList(localDir.listFiles());
    for (File file : files) {
      if(file.isDirectory()){
        getFilesRecursive(file, remoteAlfDir+file.getName(), filesToUpload);
      }else{
        CmsPath path = new CmsPath(remoteAlfDir).append(file.getName());
        filesToUpload.put(file, path);
      }
    }
  }
}


