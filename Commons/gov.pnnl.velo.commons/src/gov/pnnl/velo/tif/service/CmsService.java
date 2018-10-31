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
package gov.pnnl.velo.tif.service;


import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 */
public interface CmsService {

  public void createFolder(String path);
  public void createFolder(String parentPath, String name);
  public void createFolder(String parentPath, String name, String mimetype);
  public void createFolders(List<Resource> foldersToCreate);

  /**
   * Deletes a set of resources (provided in request body) based on the option specified.
   * Option tells us what to do with associations:
   * 1) (prevent) don't delete if assocs exist
   * 2) (force) delete all assocs too
   * 3) (archive) move assocs to deleted node which is now in the archive (alfresco default)
   * 
   * @param wikiPaths
   * @param option
   */
  public abstract void delete(List<String>alfrescoPaths, String option);
  
  /**
   * @param path - alfresco path to resource that will be deleted
   * (i.e., /company_home/Velo/projects/test)
   */
  public void delete(String alfrescoPath);

  /**
   *  Create file from local file
   * @param parentPath String - alfresco path to parent resource
   * @param filePath String - local file system path to file
   */
  public void createFile(String parentPath, String filePath);

  /**
   * Create file from string.
   * @param parentPath String - alfresco path to parent resource
   * @param fileName String
   * @param fileContents String
   */
  public void createFile(String parentPath, String fileName, String fileContents);

  /**
   *  Create file from local file
   * @param parentPath String - alfresco path to parent resource
   * @param filePath File - local file system file
   */
  public void createFile(String parentPath, File file);
  
  /**
   * 
   * @param localDir local directory to recursively upload
   * @param remoteDir destination directory - alfresco path
   * @param recursive - recursively upload directory - true/false
   */
  public void bulkUpload(File localDir, String remoteAlfrescoDir,boolean recursive);

  /**
   * Upload multiple files in one continuous stream so they get
   * more bandwidth and transfer at an accelerated rate.
   * @param filesToAlfrescoPath
   */
  public void bulkUpload(Map<File, String> filesToAlfrescoPath);
  
  /**
   * Bulk upload a bunch of files to same parent folder.
   * @param files List<File>
   * @param parentPath String - alfresco path to parent resource
   */
  public void bulkUpload(Collection<File> files, String parentPath);

  /**
   * @param remoteLinks
   */
  public void createRemoteLinks(List<RemoteLink> remoteLinks);
  public void createRemoteLink(RemoteLink remoteLink);
  
  /**
   * Method createLink.
   * @param parentPath String
   * @param targetPath String
   * @param linkName String
   */
  public void createLink(String parentPath, String targetPath, String linkName);
  
  /**
   * Creates a virtual folder (i.e., taxonomy)
   * @param parentPath String
   * @param name String
   */
  public void createVirtualFolder(String parentPath, String name);

  /**
   * Creates a virtual folder and adds the specified resources to the
   * folder.
   * @param parentPath String
   * @param name String
   * @param pathsToLink String[]
   */
  public void createVirtualFolder(String parentPath, String name, String[] pathsToAdd);
  
  /**
   * Get the remote resource pointed to by alfrescoPath copied to a local temp file created in the folder
   * specified by localDownloadFolderPath.  If localDownloadFolderPath is null, it will
   * create temp file where ever it wants.
   * It is up to caller to delete temp file when done.
   * @param alfrescoPath String - path to resource (i.e., /company_home/Velo/TestProject/file1.txt)
   * @param localDownloadFolderPath String
   * @return path to downloaded file
   */
  public String getFile(String alfrescoPath, String localDownloadFolderPath);
  
  /**
   * Get a specific version of a file copied to a local temp file created in the folder
   * specified by localDownloadFolderPath.  If localDownloadFolderPath is null, it will
   * create temp file in temp directory.
   * @param alfrescoPath String
   * @param version String
   * @param localDownloadFolderPath String
   * @return path to downloaded file
   */
  public String getFile(String alfrescoPath, String version, String localDownloadFolderPath);
  
  /**
   * Get the remote resource pointed to by alfrescoPath copied to a local temp file created in the folder
   * specified by localDownloadFolderPath.  If localDownloadFolderPath is null, it will
   * create temp dir.
   * It is up to caller to delete temp file when done.
   * @param alfrescoPath String - path to resource (i.e., /company_home/Velo/TestProject/file1.txt)
   * @param localDownloadFolderPath String
   * @param recursive 
   * @return localDownloadFolderPath
   */
  public String getFolder(String alfrescoPath, String localDownloadFolderPath, boolean recursive);

  /**
   * @param oldPath -   path to the resource to be moved
   * (i.e., /company_home/Velo/projects/TestProject/A/file1.txt)
   * @param newParentPath -  path of the folder where the resource
   * will be moved to (i.e., /company_home/Velo/projects/TestProject/B)
   */
  public void move(String oldPath, String newParentPath);

  /**  
   * @param alfrescoPath - path to resource to be renamed
   * (i.e., /company_home/Velo/projects/file.txt)
   * @param newName - new name of resource (i.e. renamedFile.txt)
   */
  public void rename(String alfrescoPath, String newName);

//  /**
//   * Method getRelationships.
//   * @param wikiPath String
//   * @return List<Relationship>
//   */
//  public List<Relationship> getRelationships(String wikiPath);
//
//  /**
//   * Method createRelationships.
//   * @param relationships List<Relationship>
//   */
//  public void createRelationships(List<Relationship> relationships);
//
//  /**
//   * Method deleteRelationships.
//   * @param relationships List<Relationship>
//   */
//  public void deleteRelationships(List<Relationship> relationships);

  /**
   * @param fileToCopyPath
   * @param destinationFolderPath
   * @param newName
   * @param overwrite
   */
  public void copy(String fileToCopyPath, String destinationFolderPath, String newName, boolean overwrite);

  /**
   * @param filesToCopyPaths
   * @param destinationFolderPath
   * @param newNames
   * @param overwrite
   */
  public void copy(List<String> filesToCopyPaths, String destinationFolderPath, List<String>newNames, boolean overwrite);

  /**
   * Method setMimetype.
   * @param alfrescoPath String
   * @param mimetype String
   */
  public void setMimetype(String alfrescoPath, String mimetype);

  /**
   * Check if a file exists on the server
   * @param alfrescoPath
  
   * @return boolean
   */
  public boolean exists(String alfrescoPath);

  /**
   * Computes the MD5 hash for the local file and then compares it to the 
   * hash property of the resource pointed to by alfrescoPath
   * @param alfrescoPath
   * @param localFile
   * @return
   */
  public boolean isIdentical(String alfrescoPath, File localFile);
  
  /**
   * TODO: move this to Util class
   * Compute the hash for an inputstream using the same algorithm used by the
   * server (currently MD5).
   * @param input
  
   * @return String
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  //public String createMd5Hash(final InputStream input) throws IOException, NoSuchAlgorithmException;

  /**
   * Method getCollection.
   * @param wikiPath String
   * @return List<Resource>
   */
  public List<Resource> getChildren(String alfresco);

  /**
   * Check if this resource is a collection.
   * @param wikiPath
  
   * @return boolean
   */
  public boolean isFolder(String alfrescoPath);

  /**
   * Set single valued property
   * @param contextPath
   * @param fullyQualifiedName
   * @param value - can be String, Date, Boolean, Double, or Integer
   */
  public void setProperty(String alfrescoPath, String fullyQualifiedName, String value);

  /**
   * Remove the given property from the given node
   * @param contextPath
   * @param fullyQualifiedName
   */
  public void removeProperty(String alfrescoPath, String fullyQualifiedName);
  
  /**
   * Method removeProperties.
   * @param contextPath String
   * @param propNames List<String>
   */
  public void removeProperties(String alfrescoPath, List<String> propNames);
  
  /**
   * Set single valued properties.
   * @param contextPath String
   * @param properties Map<String,Object>
   */
  public void setProperties(String alfrescoPath, Map<String, String> properties);

  /**
   * @param contextPath
   * @param fullyQualifiedName
   * @return an 0bject of type String, Date, Boolean, Integer, or Double */
  public String getProperty(String alfrescoPath, String fullyQualifiedName);


  /**
   * Gets the resource with all its aspects and properties set.  You can look up any
   * property you want from the resource object.  Resource object deals with 
   * multi-valued properties, so it's easier to work with.
   * @param alfrescoPath
   * @return
   */
  public Resource getResource(String alfrescoPath);

  public void setRunAsUser(String user);
  
  /**
   * Get all the versions for the given node
   * @param contextPath
  
   * @return List<Version>
   */
  //public List<Version> getVersions(String contextPath);

  /**
   * TODO: move to Util class
   * Method createPdfOfImages.
   * @param imageUuids String
   * @param destinationUuid String
   * @param pdfFileName String
   */
  //public void createPdfOfImages(String imageUuids, String destinationUuid, String pdfFileName);

}
