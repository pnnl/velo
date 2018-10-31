/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.tif.service.CmsService;

/**
 * @author D3K339
 *
 */
public class CmsServiceNoOp implements CmsService {

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFolder(java.lang.String, java.lang.String)
   */
  @Override
  public void createFolder(String parentPath, String name) {

  }
  
  @Override
  public void createFolder(String path) {
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFolder(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void createFolder(String parentPath, String name, String mimetype) {
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFolders(java.util.List)
   */
  @Override
  public void createFolders(List<Resource> foldersToCreate) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#delete(java.util.List, java.lang.String)
   */
  @Override
  public void delete(List<String> alfrescoPaths, String option) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#delete(java.lang.String)
   */
  @Override
  public void delete(String alfrescoPath) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String, java.lang.String)
   */
  @Override
  public void createFile(String parentPath, String filePath) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void createFile(String parentPath, String fileName, String fileContents) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String, java.io.File)
   */
  @Override
  public void createFile(String parentPath, File file) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#bulkUpload(java.util.Map)
   */
  @Override
  public void bulkUpload(Map<File, String> filesToAlfrescoPath) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#bulkUpload(java.util.List, java.lang.String)
   */
  @Override
  public void bulkUpload(Collection<File> files, String parentPath) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createRemoteLinks(java.util.List)
   */
  @Override
  public void createRemoteLinks(List<RemoteLink> remoteLinks) {
    

  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createRemoteLink(gov.pnnl.velo.model.RemoteLink)
   */
  @Override
  public void createRemoteLink(RemoteLink remoteLink) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createLink(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void createLink(String parentPath, String targetPath, String linkName) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createVirutalFolder(java.lang.String, java.lang.String)
   */
  @Override
  public void createVirtualFolder(String parentPath, String name) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createVirtualFolder(java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public void createVirtualFolder(String parentPath, String name, String[] pathsToAdd) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getFile(java.lang.String, java.lang.String)
   */
  @Override
  public String getFile(String alfrescoPath, String localDownloadFolderPath) {
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getFile(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public String getFile(String alfrescoPath, String version, String localDownloadFolderPath) {
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#move(java.lang.String, java.lang.String)
   */
  @Override
  public void move(String oldPath, String newParentPath) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#rename(java.lang.String, java.lang.String)
   */
  @Override
  public void rename(String alfrescoPath, String newName) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#copy(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void copy(String fileToCopyPath, String destinationFolderPath, String newName, boolean overwrite) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#copy(java.util.List, java.lang.String, java.util.List, boolean)
   */
  @Override
  public void copy(List<String> filesToCopyPaths, String destinationFolderPath, List<String> newNames, boolean overwrite) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#setMimetype(java.lang.String, java.lang.String)
   */
  @Override
  public void setMimetype(String alfrescoPath, String mimetype) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#exists(java.lang.String)
   */
  @Override
  public boolean exists(String alfrescoPath) {
    
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#isIdentical(java.lang.String, java.io.File)
   */
  @Override
  public boolean isIdentical(String alfrescoPath, File localFile) {
    
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getChildren(java.lang.String)
   */
  @Override
  public List<Resource> getChildren(String alfresco) {
    
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#isFolder(java.lang.String)
   */
  @Override
  public boolean isFolder(String alfrescoPath) {
    
    return false;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#setProperty(java.lang.String, java.lang.String, java.lang.Object)
   */
  @Override
  public void setProperty(String alfrescoPath, String fullyQualifiedName, String value) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#removeProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void removeProperty(String alfrescoPath, String fullyQualifiedName) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#removeProperties(java.lang.String, java.util.List)
   */
  @Override
  public void removeProperties(String alfrescoPath, List<String> propNames) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#setProperties(java.lang.String, java.util.Map)
   */
  @Override
  public void setProperties(String alfrescoPath, Map<String, String> properties) {
    

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getProperty(java.lang.String, java.lang.String)
   */
  @Override
  public String getProperty(String alfrescoPath, String fullyQualifiedName) {
    
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getResource(java.lang.String)
   */
  @Override
  public Resource getResource(String alfrescoPath) {
    
    return null;
  }


  @Override
  public String getFolder(String alfrescoPath, String localDownloadFolderPath, boolean recursive) {

   return null;
    
  }

  @Override
  public void bulkUpload(File localDir, String remoteAlfrescoDir, boolean recursive) {

    // TODO Auto-generated method stub
    
  }

  @Override
  public void setRunAsUser(String user) {

    // TODO Auto-generated method stub
    
  }

}
