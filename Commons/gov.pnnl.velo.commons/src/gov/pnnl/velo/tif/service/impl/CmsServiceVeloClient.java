/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.cat.core.internal.resources.ResourceManagerImpl;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.tif.service.CmsService;
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

/**
 * @author D3K339
 * 
 */
public class CmsServiceVeloClient implements CmsService {

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createFolder(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void createFolder(String parentPath, String name) {

    CmsPath path = new CmsPath(parentPath).append(name);
    CmsServiceLocator.getResourceManager().createFolder(path);

  }
  
  @Override
  public void createFolder(String path) {
    CmsServiceLocator.getResourceManager().createFolder(new CmsPath(path));
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createFolder(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void createFolder(String parentPath, String name, String mimetype) {

    CmsPath path = new CmsPath(parentPath).append(name);
    CmsServiceLocator.getResourceManager().createFolder(path, mimetype);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createFolders(java.util.List)
   */
  @Override
  public void createFolders(List<Resource> foldersToCreate) {

    CmsServiceLocator.getResourceManager().createFolders(foldersToCreate);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#delete(java.util.List,
   * java.lang.String)
   */
  @Override
  public void delete(List<String> alfrescoPaths, String option) {

    List<CmsPath> paths = new ArrayList<CmsPath>();
    for (String alfPath : alfrescoPaths) {
      paths.add(new CmsPath(alfPath));
    }
    CmsServiceLocator.getResourceManager().deleteResources(paths, option);

  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#delete(java.lang.String)
   */
  @Override
  public void delete(String alfrescoPath) {

    CmsServiceLocator.getResourceManager().deleteResource(new CmsPath(alfrescoPath));
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void createFile(String parentPath, String filePath) {

    File file = new File(filePath);
    CmsPath path = new CmsPath(parentPath).append(file.getName());
    CmsServiceLocator.getResourceManager().createFile(path, file);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void createFile(String parentPath, String fileName, String fileContents) {

    try {
      File tmpDir = File.createTempFile("velo-upload", "dir");
      tmpDir.delete();
      tmpDir.mkdir();
      File file = new File(tmpDir, fileName);
      FileUtils.writeStringToFile(file, fileContents);
      CmsPath path = new CmsPath(parentPath).append(fileName);
      CmsServiceLocator.getResourceManager().createFile(path, file);
      file.delete();

    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String,
   * java.io.File)
   */
  @Override
  public void createFile(String parentPath, File file) {

    CmsPath path = new CmsPath(parentPath).append(file.getName());
    CmsServiceLocator.getResourceManager().createFile(path, file);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#bulkUpload(java.util.Map)
   */
  @Override
  public void bulkUpload(Map<File, String> filesToAlfrescoPath) {

    Map<File, CmsPath> filesToUpload = new HashMap<File, CmsPath>();
    for (File file : filesToAlfrescoPath.keySet()) {
      CmsPath path = new CmsPath(filesToAlfrescoPath.get(file));
      filesToUpload.put(file, path);
    }
    CmsServiceLocator.getResourceManager().bulkUpload(filesToUpload, null);

  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#bulkUpload(java.util.List,
   * java.lang.String)
   */
  @Override
  public void bulkUpload(Collection<File> files, String parentPath) {

    Map<File, CmsPath> filesToUpload = new HashMap<File, CmsPath>();
    for (File file : files) {
      CmsPath path = new CmsPath(parentPath).append(file.getName());
      filesToUpload.put(file, path);
    }
    CmsServiceLocator.getResourceManager().bulkUpload(filesToUpload, null);

  }

  @Override
  public void bulkUpload(File localDir, String remoteAlfDir, boolean recursive) {
    remoteAlfDir = remoteAlfDir.endsWith("/")?remoteAlfDir:remoteAlfDir+"/";
    if (localDir.isFile()) {
      createFile(remoteAlfDir, localDir);
      return;
    }

    Map<File, CmsPath> filesToUpload = new HashMap<File, CmsPath>();
    Collection<File> files = null;
    List<Resource> foldersToCreate = new ArrayList<Resource>();
    CmsPath folderPath = new CmsPath(remoteAlfDir).append(localDir.getName());
    foldersToCreate.add(new Resource(VeloConstants.TYPE_FOLDER, folderPath));
    if (recursive) {
      getSubFolders(localDir, remoteAlfDir, foldersToCreate);
      createFolders(foldersToCreate); //create all folders in one go
      getFilesRecursive(localDir, remoteAlfDir+ localDir.getName(), filesToUpload);
      
    } else {
      // get only files in that directory. don't list directories
      files = FileUtils.listFiles(localDir, TrueFileFilter.INSTANCE, null);
      for (File file : files) {
        CmsPath path = new CmsPath(remoteAlfDir).append(file.getName());
        filesToUpload.put(file, path);
      }
    }
    
    //Now create all the files
    CmsServiceLocator.getResourceManager().bulkUpload(filesToUpload, null);
  }

  private  void getFilesRecursive(File localDir, String remoteAlfDir, Map<File, CmsPath> filesToUpload) {
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

  private  void getSubFolders(File localDir, String remoteAlfDir, List<Resource> dirResources) {
    remoteAlfDir = remoteAlfDir.endsWith("/")?remoteAlfDir:remoteAlfDir+"/";
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
        getSubFolders(subdir,remoteAlfDir+ subdir.getName(),dirResources);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createRemoteLinks(java.util.List)
   */
  @Override
  public void createRemoteLinks(List<RemoteLink> remoteLinks) {

    CmsServiceLocator.getResourceManager().createRemoteLinks(remoteLinks);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createRemoteLink(gov.pnnl.velo.model.RemoteLink)
   */
  @Override
  public void createRemoteLink(RemoteLink remoteLink) {
    List<RemoteLink> links = new ArrayList<RemoteLink>();
    links.add(remoteLink);
    createRemoteLinks(links);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#createLink(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void createLink(String parentPath, String targetPath, String linkName) {

    IResourceManager mgr = CmsServiceLocator.getResourceManager();
    IResource target = mgr.getResource(new CmsPath(targetPath));
    CmsPath resourcePath = new CmsPath(parentPath).append(linkName);
    mgr.createLink(resourcePath, target);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.pnnl.velo.tif.service.CmsService#createVirutalFolder(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void createVirtualFolder(String parentPath, String name) {

    CmsPath path = new CmsPath(parentPath).append(name);
    CmsServiceLocator.getResourceManager().createTaxonomy(path);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.pnnl.velo.tif.service.CmsService#createVirtualFolder(java.lang.String,
   * java.lang.String, java.lang.String[])
   */
  @Override
  public void createVirtualFolder(String parentPath, String name, String[] pathsToAdd) {

    IResourceManager mgr = CmsServiceLocator.getResourceManager();
    CmsPath path = new CmsPath(parentPath).append(name);
    IResource tax = mgr.createTaxonomy(path);

    for (String resourcePath : pathsToAdd) {
      mgr.copy(new CmsPath(resourcePath), tax.getPath());

    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#getFile(java.lang.String,
   * java.lang.String)
   */
  @Override
  public String getFile(String alfrescoPath, String localDownloadFolderPath) {

    return getFile(alfrescoPath, null, localDownloadFolderPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#getFile(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public String getFile(String alfrescoPath, String version, String localDownloadFolderPath) {

    try {
      CmsPath path = new CmsPath(alfrescoPath);
      File parentDir = null;
      if (localDownloadFolderPath != null) {
        parentDir = new File(localDownloadFolderPath);
      } else {
        parentDir = File.createTempFile("velo-download", "dir");
        parentDir.delete();
        parentDir.mkdir();
      }
      File destinationFile = new File(parentDir, path.getName());
      String property = VeloConstants.PROP_CONTENT;
      CmsServiceLocator.getResourceManager().getContentPropertyAsFile(path, property, version, destinationFile);
      return destinationFile.getAbsolutePath();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#move(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void move(String oldPath, String newParentPath) {

    CmsPath sourcePath = new CmsPath(oldPath);
    CmsPath destPath = new CmsPath(newParentPath).append(sourcePath.getName());
    CmsServiceLocator.getResourceManager().move(sourcePath, destPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#rename(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void rename(String alfrescoPath, String newName) {

    CmsPath sourcePath = new CmsPath(alfrescoPath);
    CmsPath destPath = sourcePath.getParent().append(newName);
    CmsServiceLocator.getResourceManager().move(sourcePath, destPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#copy(java.lang.String,
   * java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void copy(String fileToCopyPath, String destinationFolderPath, String newName, boolean overwrite) {

    CmsPath sourcePath = new CmsPath(fileToCopyPath);
    CmsPath destinationPath = new CmsPath(destinationFolderPath).append(newName);
    CmsServiceLocator.getResourceManager().copy(sourcePath, destinationPath, overwrite);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#copy(java.util.List,
   * java.lang.String, java.util.List, boolean)
   */
  @Override
  public void copy(List<String> filesToCopyPaths, String destinationFolderPath, List<String> newNames, boolean overwrite) {

    ICatCML cml = CmsServiceLocator.getResourceManager().getCML();
    CmsPath destParentPath = new CmsPath(destinationFolderPath);
    int i = 0;
    for (String fileToCopy : filesToCopyPaths) {
      CmsPath source = new CmsPath(fileToCopy);
      String newName = newNames.get(i);
      if (newName == null) {
        newName = source.getName();
      }
      CmsPath destination = destParentPath.append(newName);
      cml.copy(source, destination, overwrite);
      i++;
    }
    CmsServiceLocator.getResourceManager().executeCml(cml);

  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#setMimetype(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void setMimetype(String alfrescoPath, String mimetype) {

    CmsPath resourcePath = new CmsPath(alfrescoPath);
    CmsServiceLocator.getResourceManager().setProperty(resourcePath, VeloConstants.PROP_MIMETYPE, mimetype);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#exists(java.lang.String)
   */
  @Override
  public boolean exists(String alfrescoPath) {

    CmsPath resourcePath = new CmsPath(alfrescoPath);
    return CmsServiceLocator.getResourceManager().resourceExists(resourcePath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#isIdentical(java.lang.String,
   * java.io.File)
   */
  @Override
  public boolean isIdentical(String alfrescoPath, File localFile) {

    IResourceManager mgr = CmsServiceLocator.getResourceManager();
    CmsPath resourcePath = new CmsPath(alfrescoPath);
    String hash = mgr.getHash(localFile);
    return mgr.isIdentical(resourcePath, hash);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#getChildren(java.lang.String)
   */
  @Override
  public List<Resource> getChildren(String alfrescoPath) {

    ResourceManagerImpl mgr = (ResourceManagerImpl) CmsServiceLocator.getResourceManager();
    List<IResource> children = mgr.getChildren(new CmsPath(alfrescoPath));
    List<Resource> rawChildren = new ArrayList<Resource>();
    for (IResource child : children) {
      Resource rawChild = mgr.getCachedResource(child);
      rawChildren.add(rawChild);
    }
    return rawChildren;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#isFolder(java.lang.String)
   */
  @Override
  public boolean isFolder(String alfrescoPath) {

    CmsPath path = new CmsPath(alfrescoPath);
    IResource resource = CmsServiceLocator.getResourceManager().getResource(path);
    return resource instanceof IFolder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#setProperty(java.lang.String,
   * java.lang.String, java.lang.Object)
   */
  @Override
  public void setProperty(String alfrescoPath, String fullyQualifiedName, String value) {

    CmsPath path = new CmsPath(alfrescoPath);
    CmsServiceLocator.getResourceManager().setProperty(path, fullyQualifiedName, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#removeProperty(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void removeProperty(String alfrescoPath, String fullyQualifiedName) {

    setProperty(alfrescoPath, fullyQualifiedName, null);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.pnnl.velo.tif.service.CmsService#removeProperties(java.lang.String,
   * java.util.List)
   */
  @Override
  public void removeProperties(String alfrescoPath, List<String> propNames) {

    Map<String, String> properties = new HashMap<String, String>();
    for (String propName : propNames) {
      properties.put(propName, null);
    }
    setProperties(alfrescoPath, properties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#setProperties(java.lang.String,
   * java.util.Map)
   */
  @Override
  public void setProperties(String alfrescoPath, Map<String, String> props) {

    CmsPath path = new CmsPath(alfrescoPath);
    CmsServiceLocator.getResourceManager().setProperties(path, props);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#getProperty(java.lang.String,
   * java.lang.String)
   */
  @Override
  public String getProperty(String alfrescoPath, String fullyQualifiedName) {

    return CmsServiceLocator.getResourceManager().getProperty(alfrescoPath, fullyQualifiedName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.velo.tif.service.CmsService#getResource(java.lang.String)
   */
  @Override
  public Resource getResource(String alfrescoPath) {

    ResourceManagerImpl mgr = (ResourceManagerImpl) CmsServiceLocator.getResourceManager();
    CmsPath path = new CmsPath(alfrescoPath);
    IResource resource = mgr.getResource(path);
    return mgr.getCachedResource(resource);
  }

  @Override
  public String getFolder(String alfrescoPath, String localDownloadFolderPath, boolean recursive) {

    try {
      File parentDir = null;
      if (localDownloadFolderPath == null) {
        parentDir = gov.pnnl.velo.util.FileUtils.createTempDirectory("velo-download");
        localDownloadFolderPath = parentDir.getAbsolutePath();
      }

      List<Resource> children = getChildren(alfrescoPath);
      for (Resource child : children) {
        if (child.getType().equals(VeloConstants.PROP_FOLDER) && recursive) {
          File f = new File(localDownloadFolderPath, child.getName());
          f.mkdir();
          getFolder(child.getPath(), f.getAbsolutePath(), true);
        } else if (child.getType().equals(VeloConstants.PROP_CONTENT)) {
          getFile(child.getPath(), localDownloadFolderPath);
        }
      }
    } catch (Throwable e) {
      throw new RuntimeException("Exception in getFolder:" + e.getMessage());
    }
    return localDownloadFolderPath;

  }

  @Override
  public void setRunAsUser(String user) {
  //used only by the server
  }

}
