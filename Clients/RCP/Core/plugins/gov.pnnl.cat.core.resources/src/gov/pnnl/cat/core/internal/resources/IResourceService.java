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
package gov.pnnl.cat.core.internal.resources;

import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourceNotFoundException;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.webservice.alert.RepositoryAlert;
import gov.pnnl.cat.webservice.subscription.Subscription;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Comment;
import gov.pnnl.velo.model.Email;
import gov.pnnl.velo.model.Properties;
import gov.pnnl.velo.model.Relationship;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.Predicate;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class is responsible for direct communication with the server.
 * It performs no caching and its methods are generally straightforward calls to the server.
 * Generally, any method called here will result in a call to the server.
 * @version $Revision: 1.0 $
 */
public interface IResourceService {

  /**
   * Method getChildren.
   * @param folderPath CmsPath
   * @return List<Resource>
   * @throws ResourceException
   * @throws AccessDeniedException
   */
  public List<Resource> getChildren(CmsPath folderPath);

  public byte[] getThumbnail(CmsPath path, String thumbnailName);
  
  /**
   * @param path
   * @return Resource
   * @throws ResourceException */
  public Resource getResource(CmsPath path);

  /**
   * Method isIdentical.
   * @param resource Resource
   * @param property QualifiedName
   * @param hash String
   * @return boolean
   */
  public boolean isIdentical(Resource resource, String property, String hash);
  
  /**
   * Compute the hash for a file using the same algorithm used by the
   * server (currently MD5).
   * @param file
   * @return String
   */
  public String getHash(File file);
  
  /**
   * @param uuid
   * @return Resource
   * @throws ResourceException */
  public Resource getResource(String uuid);
  
  /**
   * Method getResourcesByUuid.
   * @param uuids List<String>
   * @return List<Resource>
   */
  public List<Resource> getResourcesByUuid(List<String>uuids);
  
  /**
   * Method getResourcesByAspect.
   * @param aspect String
   * @return Resource[]
   * @throws ResourceException
   */
  public List<Resource> getResourcesByAspect(String aspect);

  /**
   * Method getContentPropertyAsFile.
   * @param resource Resource
   * @param property QualifiedName
   * @param downloadFolder File
   * @return File
   * @throws ResourceException
   */
  public File getContentPropertyAsFile(Resource resource, String property, File destinationFile, String version);

  /**
   * Method setProperties.
   * @param resource - temporary resource that contains the properties to set (don't need
   * all properties here, just the ones that are changing
   * @return - modified resource with all properties as they are on the server
   */
  public Resource setProperties(Resource resource);
  public List<Resource> setProperties(List<Resource> resources);
  
  /**
   * Method addAspect.
   * @param resource Resource
   * @param aspect String
   * @return Resource
   * @throws ResourceException
   */
  public Resource addAspect(Resource resource, String aspect);
  /**
   * Method removeAspect.
   * @param resource Resource
   * @param aspect String
   * @return Resource
   * @throws ResourceException
   */
  public Resource removeAspect(Resource resource, String aspect);

  /**
   * Method createFolders.
   * @param foldersToCreate List<Resource>
   * @return List<Resource>
   * @throws ResourceException
   */
  public List<Resource> createFolders(List<Resource> foldersToCreate);
  
  /**
   * Method executeActions
   * @param predicate
   * @param actions
   */
  public void executeActions(Predicate predicate, Action[] actions);

  /**
   * Method createSubscription
   * @param subscription
   */
  public void createSubscription(Subscription subscription);
  
  
  /**
   * Method deleteSubscriptions
   * @param subscription
   */
  public void deleteSubscriptions(Subscription[] subscription);
  
  /**
   * Method getSubscriptions
   */
  public Subscription[] getSubscriptions();
  
  /**
   * Method createFolder.
   * @param folderToCreate Resource
   * @return Resource
   * @throws ResourceException
   */
  public Resource createFolder(Resource folderToCreate);
  
  /**
   * Method deleteResources.
   * @param resourceList ResourceList
   * @throws ResourceException
   */
  public void deleteResources(List<Resource> resourceList, String deleteOption);
  
  /**
   * Moves a resource to a new location.  Returns the Resource data object
   * containing all the information on the moved resource.
   * @param oldPath
   * @param newPath
   * @return
   */
  public Resource move(CmsPath oldPath, CmsPath newPath);

  /**
   * Copies a resource to desired location.  Returns the Resource data object
   * containing all the information on the copied resource.
   * @param source CmsPath
   * @param destination CmsPath
   * @throws ResourceException
   */
  public Resource copy(CmsPath source, CmsPath destination, boolean overwrite);

  /**
   * Method importUrlList.
   * @param destination CmsPath
   * @param urls List<String>
   * @param monitor IProgressMonitor
   * @throws ResourceException
   */
  public void importUrlList(CmsPath destination, List<String> urls, IProgressMonitor monitor);

  /**
   * Method resourceExists.
   * @param path CmsPath
   * @return boolean
   * @throws ResourceException
   */
  public boolean resourceExists(CmsPath path);
  

  /**
   * Get all resources linked to this resource
   * TODO: don't return ICatQueryResult - return SearchResult instead
   * @param resource
   * @return
   */
  public ICatQueryResult getLinkedResources(Resource resource);

  /**
   * Each resource should contain the path to the new link and the property
   * VeloConstants.prop_link_destination which points to the uuid of the target node.
   * @param linksToCreate
   * @return
   */
  public List<Resource> createLinks(List<Resource> linksToCreate);
  
  public List<Resource> createRemoteLinks(List<RemoteLink> remoteLinks);

  /**
   * Updates the link at the specified path to point to the specified destination.
   * The previous destination is overwritten.
   * 
   * @param link
   * @param destination
  
   * @return Resource
   * @throws ResourceException * @throws AccessDeniedException
   */
  public Resource updateLinkTarget(Resource link, Resource destination);
    
  /**
   * Base URL to the repository web application
  
   * @return String
   */
  public String getRepositoryUrlBase();

  /**
   * Construct the http url for any file node on the server
   * @param uuid
   * @param fileName
   * @param contentProperty
   * @param attachmentMode
  
   * @return String
   */
  public String getHttpUrl(String uuid, String fileName, String contentProperty, String attachmentMode);
  
  /**
   * Get the webdav url for any resource on the server
   * @param path
  
   * @return String
   */
  public String getWebdavUrl(CmsPath path);
    
  /**
   * Method getRelationships.
   * @param path CmsPath
   * @return List<Relationship>
   * @throws ResourceException
   */
  public List<Relationship> getRelationships(CmsPath path);
  public void createRelationships(List<Relationship> relationships);
  public void deleteRelationships(List<Relationship> relationships);
  
  /**
   * @param filesToServerPath - map a file to the path where it will be located on the server
   * @param globalMetadata - a map of properties that will be applied to every file in the upload.  Each property value is represented as a list because
   * the properties could be multivalued.  TODO: create a Property class that contains a single value or a multi value.
   * @param fileSpecificMetadata - this is a map that maps a regular expression to a map of properties.  The regular expression could be a file name, 
   * a relative file path, and/or contain wildcard characters (*, ?) similar to a linux file system regex.  It will point to a subset of files in the 
   * upload set for which the metadata map should be applied.
   * @param monitor
   */
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, Properties globalMetadata,
      Map<String, Properties> fileSpecificMetadata, IProgressMonitor monitor);


  /**
   * Update content of a single file - useful if you do not know the path and only the UUID
   * @param uuid
   * @param file
   * @param property
   * @param mimetype
   * @return
   */
  public Resource updateContent(String uuid, File file, String property, String mimetype);
  
  /**
   * Update the content of a single file with an input stream.
   * @param path
   * @param stream
   * @param property - the content property to update
   * @param mimetype - the mimetype of the file (leave null if you don't want to change the mimetype)
   * @return
   */
  public Resource updateContent(CmsPath path, InputStream stream, String property, String mimetype, String offset);
  
  /**
   * Method getResourcesByPath.
   * @param paths List<CmsPath>
   * @return List<Resource>
   * @throws ResourceException
   * @throws ResourceNotFoundException
   * @throws AccessDeniedException
   */
  List<Resource> getResourcesByPath(List<CmsPath> paths);

  /**
   * 
   * @param filesToDownload
   */
  public void bulkDownload(Map<CmsPath, File> filesToDownload);

  /**
   * Method sendEmail.
   * @param email Email
   */
  public void sendEmail(Email email);
  
  /**
   * Method addComment.
   * @param resource Resource
   * @param commentText String
   */
  public Comment addComment(Resource resource, String commentText);
  
  /**
   * Method getComments.
   * @param resource Resource
   * @return Comment[]
   */
  public Comment[] getComments(Resource resource);

  public void deleteAlerts(RepositoryAlert[] alerts);

  public void markAlertsAsRead(RepositoryAlert[] alerts);

  public RepositoryAlert[] getAlerts();

  /**
   * TODO: figure out best return objects for events and cache updating
   * @param cml
   * @return
   */
  public List<Resource> executeCml(ICatCML cml);
 
  
  public HashMap<String, HashMap<String, Integer>> getFacetItems(String query, ArrayList<String> fieldsQNames);
  
  /**
   * @param query
   * @param includeThumbnails
   * @param sortByProp
   * @param order
   * @param maxItems
   * @param pageNumber
   * @return
   */
  public ICatQueryResult search(String query, boolean includeThumbnails, String sortByProp, String order, 
      Integer maxItems, Integer pageNumber);

  /**
   * @param query
   * @param includeThumbnails
   * @param sortByProp
   * @param order
   * @param maxItems
   * @param pageNumber
   * @param includeJson
   * @param facetProperties
   * @return
   */
  public ICatQueryResult search(String query, boolean includeThumbnails, String sortByProp, String order, 
      Integer maxItems, Integer pageNumber, boolean includeJson, ArrayList<String> facetProperties);
  
  /**
   * Get all the permissions for the given node
   * @param path
   * @return
   */
  public ACL getPermissions(CmsPath path);
  
  /**
   * @param acls
   */
  public void setPermissions(ACL[] acls);
  
  /**
   * @param acls
   * @param recursive
   */
  public void setPermissions(ACL[] acls, boolean recursive);

  public Resource createEmptyFile(Resource resource);

}
