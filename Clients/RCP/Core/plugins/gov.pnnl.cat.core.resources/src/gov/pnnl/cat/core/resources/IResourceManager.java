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
package gov.pnnl.cat.core.resources;

import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.webservice.alert.RepositoryAlert;
import gov.pnnl.cat.webservice.subscription.Subscription;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Comment;
import gov.pnnl.velo.model.Email;
import gov.pnnl.velo.model.Properties;
import gov.pnnl.velo.model.Relationship;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.Predicate;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class provides an interface for communicating with the server.
 * It relies heavily on caching and asynchronous notification to improve
 * performance.
 * @version $Revision: 1.0 $
 */
public interface IResourceManager {

  //prevent deletes if provenance assocs are present on the node
  public static final String DELETE_OPTION_PREVENT = "prevent";

  // delete node and all proveance assocs to it
  public static final String DELETE_OPTION_FORCE = "force";

  // delete node and allow alfresco to reroute associations to the archived node
  // (this is the alfresco default behavior)
  public static final String DELETE_OPTION_ARCHIVE = "archive";

	// constants for user directory hierarchy
	public final static String FAVORITES        = "Favorites";
	public final static String PERSONAL_LIBRARY = "Personal Library";
	public final static String PROJECTS         = "Projects";
	public final static String TEMPORARY_FILES  = "Temporary Files";
	public final static String TAXONOMIES       = "Taxonomies";
	public final static String REFERENCE_LIBRARY = "Reference Library";


	/*********************************/
	/** WRITE OPERATIONS            **/
	/*********************************/


	/**
	 * Returns a new {@link ICatCML} that can be used to execute multiple
	 * write operations in a single transaction.
	 * <p>
	 * Use a CML (or other properly named block operation object) to record
	 * a bunch of operations that will be executed in a single Alfresco CML
	 * statement when executeCML() is called.
	 * <p>
	 * This will work for all methods EXCEPT any addFile method, because addFile
	 * needs to use the ContentService (which executes in a separate transaction).
	 * Therefore, the addFile methods should never be included in the CML object.
	 * <p>
	 * Also setPropertyAsStream for the content property (or a new setContent method
	 * if we decide to add it) would also execute in 
	 * a separate transaction so it should not be called inside the CML operation block.
	 * <p>
	 * CML blocks are only used for write operations that modify nodes.  Content is
	 * NOT included in these operations.
	 * <p>
	 * Create a new CML object to record operations.  Caching is not necessary.
	 * 
   * @return ICatCML
	 */
	public ICatCML getCML();
	
	
  /**
   * Perform multiple operations in one call
   * @param cml
   */
  public void executeCml(ICatCML cml);

	/**
	 * Adds a folder at the specified path to the repository.
	 * @param path
	 * @return
	 */
	public IFolder createFolder(CmsPath path);

	/**
	 * Method addFolder.
	 * @param path CmsPath
	 * @param recursive boolean
	 * @return IFolder
	 
	 */
	public IFolder createFolder(CmsPath path, boolean recursive);

	/**
	 * Method addFolder.
	 * @param parentFolder IResource
	 * @param folderName String
	 * @return IFolder
	 */
	public IFolder createFolder(IResource parentFolder, String folderName);
	
	/**
	 * @param path
	 * @param mimetype
	 * @return
	 */
	public IFolder createFolder(CmsPath path, String mimetype);

	/**
	 * Method createFolders
	 * @param foldersToCreate
	 * @return List<IFolder>
	 
	 */
	public List<IFolder> createFolders(List<Resource> foldersToCreate);
	
	 /**
   * Method addFile.  Will overwrite content if this file already exists on the server.
   * @param path CmsPath
   * @param content File - passing null will create an empty file on the server
   * @return IFile
   
   */
  public IFile createFile(CmsPath path, File content);

  /**
   * Method addFile.  Will overwrite content if this file already exists on the server.
   * @param parent IResource
   * @param file File - passing null will create an empty file on the server
   * @return IFile
   */
  public IFile createFile(IResource parent, File file);
  
   /**
   * Method bulkUpload.
   * @param fileUploads Map<File,CmsPath> - local files mapped to the path where they will be on the server
   * @param monitor IProgressMonitor   
   */
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, IProgressMonitor monitor);
  
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, IProgressMonitor monitor, boolean addToFileCache);
  
  /**
   * @param filesToServerPath - map a file to the path where it will be located on the server
   * @param globalMetadata - a map of properties that will be applied to every file in the upload.  Each property value is represented as a list because
   * the properties could be multivalued.  TODO: create a Property class that contains a single value or a multi value.
   * @param fileSpecificMetadata - this is a map that maps a regular expression to a map of properties.  The regular expression could be a file name, 
   * a relative file path, and/or contain wildcard characters (*, ?) similar to a linux file system regex.  It will point to a subset of files in the 
   * upload set for which the metadata map should be applied.
   * @param monitor
   * @param addToFileCache - if true, the files will be copied into the local file cache for faster future reference
   */
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, Properties globalMetadata,
      Map<String, Properties> fileSpecificMetadata, IProgressMonitor monitor, boolean addToFileCache);
  
  /**
   * Update the content of a single file with an input stream.
   * @param path
   * @param stream
   * @param property - the content property to update
   * @param mimetype - the mimetype of the file (leave null if you don't want to change the mimetype)
   * @return
   */
  public void updateContent(CmsPath path, InputStream stream, String property, String mimetype, String offset);

  /**
   * Creates a project at the specified location.
   * @param folderPath
   * @return IFolder
    */
  public IFolder createProject(CmsPath folderPath, String mimetype);

  /**
   * Creates a taxonomy at the specified location.
   * @param folderPath
   * @return IFolder
    */
  public IFolder createTaxonomy(CmsPath folderPath);
  
  /**
   * Creates a link to the specified <code>IFile</code> at the specified path.
   * @param path where the link will go
   * @param target what the link will point to
   * @return the new link 
   */
  public ILinkedResource createLink(CmsPath path, IResource target);

  public void createRemoteLinks(List<RemoteLink> remoteLinks);
  
  /**
   * Removes the resource at the specified path from the repository. If the path
   * represents a folder, all children will also be removed as a result of
   * calling this method.
   * 
   * @param path
    */
  public void deleteResource(CmsPath path);
  
  /**
   * Delete resource by UUID
   * @param uuid
   */
  public void deleteResource(String uuid);
  
  /**
   * Method deleteResources.
   * @param paths List<CmsPath>
   
   */
  public void deleteResources(List<CmsPath> paths);
  
  public void deleteResources(List<CmsPath> paths, String deleteOption);
  
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
  

  public void deleteAlerts(RepositoryAlert[] alerts);

  public void markAlertsAsRead(RepositoryAlert[] alerts);

  public RepositoryAlert[] getAlerts();
	
  /**
   * Method addComment.
   * @param path CmsPath
   * @param commentText String
   */
  public Comment addComment(CmsPath path, String commentText);
  
  /**
   * Method getComments.
   * @param path CmsPath
   * @return Comment[]
   */
  public Comment[] getComments(CmsPath path);


	/**
	 * Copies the resource at the source path provided to the specified destination.
	 *
	 * @param source the location of the resource to copy
	 * @param destination the location of the new copy of the original resource
	  */
	public void copy(CmsPath source, CmsPath destination);
	public void copy(CmsPath source, CmsPath destination, boolean overwrite);


	/**
	 * Import the documents contained at the given http urls.
	 * TODO: not sure if this still works
	 * @param destination CmsPath
	 * @param urls List<String>
	 * @param monitor IProgressMonitor
	 
	 */
	public void importUrlList(CmsPath destination, List<String> urls, IProgressMonitor monitor);

	/**
	 * Moves the resource at the source path provided to the specified destination.
	 *
	 * @param oldPath CmsPath
	 * @param newPath CmsPath
	  */
	public void move(CmsPath oldPath, CmsPath newPath);


	// PROPERTIES AND CONTENT

	/**
	 * Sets the specified property on the resource at the path given to the value provided.
	 * @param contentPath
	 * @param key
	 * @param value
	  */
	public void setProperty(CmsPath resourcePath, String key, String value);
	
	/**
	 * Sets the specified property on the resource at the path given to the value provided.
	 * @param contentPath
	 * @param key
	 * @param value
	  */
	public void setProperty(CmsPath contentPath, String key, IResource value);
		
	/**
	 * @param path
	 * @param key
	 * @param value
	 */
	public void setProperty(String path, String key, String value);

	public void setProperties(CmsPath resourcePath, Map<String, String> properties);
	
	/**
	 * Add a set of multi-valued properties to a single resource;
	 * @param resourcePath
	 * @param properties
	 */
	public void setMultiValuedProperties(CmsPath resourcePath, Map<String, List<String>> properties);
	
	/**
	 * Get all the categories set on the current resource.
	 * @param resourcePath
	 * @return
	 */
	public List<IResource> getCategories(CmsPath resourcePath);

	/**
	 * Set all the categories on a node
	 * @param categories - this must be the complete list of categories to set on the node
	 * @param resourcesToAdd
	 */
	public void setCategories(CmsPath resourcePath, List<IResource> categories);
	
	/**
	 * Add the given category to the given resources.
	 * @param resourcePaths
	 * @param category
	 */
	public void addCategories(List<CmsPath> resourcePaths, IResource category);

	/**
	 * Updates the link at the specified path to point to the specified destination.
	 * The previous destination is overwritten.
	 * 
	 * @param path the location of the link
	 * @param newTarget the location of the new target resource
	  */
	public void updateLinkTarget(CmsPath path, IResource newTarget);

	/**
	 * Adds the specified aspect to the resource at the path given.
	 * @param path
	 * @param aspect the aspect to add
	  */
	public void addAspect(CmsPath path, String aspect);

	/**
	 * Removes the specified aspect from the resource at the path given.
	 * @param path
	 * @param aspect the aspect to remove
	  */
	public void removeAspect(CmsPath path, String aspect);

	public void removeProperties(CmsPath path, List<String> properties);

	 
	/*********************************/
	/** READ OPERATIONS             **/
	/*********************************/


	// FOLDERS AND FILES


	/**
	 * Returns the resource at the root of the CAT server.
	 * @return IFolder the resource at "/"
   
	 */
	public IFolder getRoot();

	/**
	 * Returns the resource at the specified path.
	 * @param path
	 * @return IResource
	  */
	public IResource getResource(CmsPath path);

	/**
	 * Returns the resource at the specified path.
	 * @param path
	 * @param checkServerIfNotInCache - if false, then the resource will only be looked up from the cache - if it's not
   * in the cache, it will return an empty list.  This is so that we can use this method for UI action enablement, without
   * taking any performance hits, since the only resources we care about will be in the cache.
	 * @return IResource
	  */
	public IResource getResource(CmsPath path, boolean checkServerIfNotInCache);
	
	/**
	 * Method getResources.
	 * @param paths List<CmsPath>
	 * @return List<IResource>
	 
	 */
	public List<IResource> getResources(List<CmsPath> paths);
	
	public List<IResource> getResourcesByUuid(List<String> uuids);

	/**
	 * Returns the resource with the specified uuid.
	 * 
	 * Note that this method does not perform any caching.
	 * 
	 * @param uuid the uuid for the resource
	 * @return IResource
	  */
	public IResource getResource(String uuid);

	/**
	 * Returns <code>true</code> if the resource at the specified path exists,
	 * <code>false</code> if it does not.
	 * @param path
	 * @return boolean
	  */
	public boolean resourceExists(CmsPath path);
	public boolean resourceExists(String path);

	/**
	 * Some times (like for selection events) we only need to check if the resource is currently in the 
	 * cache, not whether it exists in the CMS or not.
	 * @param path
	 * @return boolean
	 */
	public boolean resourceCached(CmsPath path);

	/**
	 * Returns the target for the link at the specified path.
	 * @param path
	 * @return IResource
	  * @throws AccessDeniedException */
	public IResource getTarget(CmsPath path);

	/**
	 * Returns the target node referenced by the given property.
	 * @param path
	 * @param property
	 * @return IResource
	  if the referenced node does not exist * @throws AccessDeniedException if user does not have permissions on the referenced node */
	public IResource getPropertyAsResource(CmsPath path, String property);

	/**
	 * Returns the linked resources that link to the resource at the specified path.
	 * @param path
	 * @return Collection<IResource>
	  */
	public Collection<IResource> getResourcesLinkedToPath(CmsPath path);


	/**
	 * Returns the home folder for the current user.
	 * @return the current user's home folder  * @throws ServerException */
	public IFolder getHomeFolder();

	public CmsPath getUserDocumentsPath();
	public CmsPath getTeamDocumentsPath();

	/**
	 * Returns the personal library folder for the current user.
	 * @return IFolder
	  * @throws ServerException */
	public IFolder getPersonalLibrary();


	/**
	 * Returns the favorites folder for the current user.
	 * @return IFolder
	  * @throws ServerException */
	public IFolder getFavorites();

	/**
	 * Returns all taxonomies that are available to the current user.
	 * @return IFolder[]
	  * @throws AccessDeniedException */
	public IFolder[] getTaxonomies();


	/**
	 * Returns all projects that are available to the current user.
	 * @return IFolder[]
	  * @throws AccessDeniedException */
	public IFolder[] getProjects();

	/**
	 * Returns the children of the folder at the specified path.
	 * @param folderPath
	 * @return List<IResource>
	  */
	public List<IResource> getChildren(CmsPath folderPath);

	/**
	 * Get the number of children without having to load all the children
	 * @param folderPath
	
	 * @return int
	 */
	public int getChildCount(CmsPath folderPath);
	
	/**
	 * Return the child of the given name.  Returns null if child not found.
	 * @param parent
	 * @param childName
	 * @return IResource
	 */
	public IResource getChildByName(IResource parent, String childName);


	/**
	 * Returns the aspects that have been applied to the resource at the specified path.
	 * @param path	
	 * @return List<QualifiedName>
	 * @throws AccessDeniedException
	 
	 * @throws AccessDeniedException, ResourceException  */
	public List<String> getAspects(CmsPath path);

  /**
   * Returns the aspects that have been applied to the resource at the specified path.
   * @param path
   * @param checkServerIfNotInCache - if false, then the resource will only be looked up from the cache - if it's not
   * in the cache, it will return an empty list.  This is so that we can use this method for UI action enablement, without
   * taking any performance hits, since the only resources we care about will be in the cache. 
   * @return List<QualifiedName>
   */
  public List<String> getAspects(CmsPath path, boolean checkServerIfNotInCache);
	
	/**
	 * Gets the unique identifier for the resource at this path
	 * @param path
	 * @return String
	 */
	public String getUUID(CmsPath path);

	/**
	 * Returns the value of the property for the specified key on the resource at the path given.
	 * @param path
	 * @param key
	 * @return String
	  */
	public String getProperty(CmsPath path, String key);	
	public String getProperty(String path, String key);
		
	/**
	 * @param path
	 * @param key
	 * @param checkServerIfNotInCache - if false, then the resource will only be looked up from the cache - if it's not
   * in the cache, it will return an empty list.  This is so that we can use this method for UI action enablement, without
   * taking any performance hits, since the only resources we care about will be in the cache.
	 * @return String
	  */
	public String getProperty(CmsPath path, String key, boolean checkServerIfNotInCache);
	
	/**
	 * Returns the values of the properties specified for the resource at the path given.
	 * @param path
	 * @param keys
	 * @return Vector<String>
	  */
	public List<String> getPropertiesAsString(CmsPath path, List<String> keys);
	
	/**
	 * Get all the properties that are set on this resource
	 * @param path
	 * @return Set<QualifiedName>
	 */
	public Set<String> getPropertyNames(CmsPath path);

	/**
	 * Returns the specified content property for the resource at the path provided.
	 * @param path
	 * @param property
	 * @return InputStream
	  */
	public InputStream getContentProperty(CmsPath path, String property);
	
	/**
	 * Returns the specified content property for the resource with the specified uuid.
	 * @param uuid
	 * @param property
	 * @return InputStream
	  */
	public InputStream getContentProperty(String uuid, String property);
	
	/**
	 * Get a specific version of the file downloaded to the given local file path.  If 
	 * destinationFile is null, file will be stored in the resource manager cache.
	 * @param path
	 * @param property
	 * @param version
	 * @param destinationFile
	 * @return
	 */
	public File getContentPropertyAsFile(CmsPath path, String property, String version, File destinationFile);
  public File getContentPropertyAsFile(CmsPath path, String property);
  
	/**
	 * Get file content based on uuid.  Only use this method if your resource isn't cached.  Otherwise,
	 * use CmsPath to identify your resource.
	 * Method getContentPropertyAsFile.
	 * @param uuid String
	 * @param property QualifiedName
	 * @return File	 
	 */
	public File getContentPropertyAsFile(String uuid, String property);
	
	/**
	 * Get a thumbnail for the given resource.
	 * @param resourcePath
	 * @param thumbnailName
	 * @return
	 */
	public InputStream getThumbnail(CmsPath resourcePath, String thumbnailName);
	
	/**
	 * Returns the specified property for the resource at the path provided as a <code>Calendar</code>.
	 * @param path
	 * @param key
	 * @return Calendar
	  * @throws ParseException */
	public Calendar getPropertyAsDate(CmsPath path, String key) throws ParseException;

	/**
	 * Returns the specified multi-valued property for the resource at the path provided as a <code>String[]</code>.
	 * @param path
	 * @param key
	 * @return String[]
	  */
	public String[] getMultiValuedProperty(CmsPath path, String key);

	/**
	 * Returns the base URL to the repository web application.
	 * For a server running on <tt>localhost</tt>, this would return <tt>http://localhost:8080/alfresco</tt>
	 * 
	 * @return the base repository URL */
	public String getRepositoryUrlBase();

	/**
	 * Returns the CIFS path to access the content for the file at the specified path using a drive letter.
	 *
	 * @param path
	 * @return IPath
	  */
	//public IPath getCifsDriveSharePath(CmsPath path);

	/**
	 * Sets the drive letter for CIFS access..
	 *
	 * @param path IPath
	  */
	//public void setCifsDriveLetter(IPath path);

  /**
   * Returns the CIFS path to access the content for the file at the specified path.
   *
   * @param path
  
  
   * @return IPath
    */
  //public IPath getCifsUncPath(CmsPath path);
  
	/**
	 * 
	 * @param path CmsPath
	 * @return URL
	 
	 */

	/**
	 * Returns the WebDAV URL to access the content for the file at the specified path.
	 * <p>
	 * WebDAV URL can NOT include an authentication ticket or a transform parameter.
	 * It only works on the original document. 
	 * @param path
	 * @return
	 
	 */
	public URL getWebdavUrl(CmsPath path);

	/**
	 * Returns the Http URL for the file at the specified path.
	 * Http URLs will have an authentication ticket that will last
	 * until the CAT session has expired.
	 * @param path CmsPath
	 * @return URL
	  */
	public URL getHttpUrl(CmsPath path);

	/**
	 * Returns the Http URL for the file at the specified path.
	 * Http URLs will have an authentication ticket that will last
	 * until the CAT session has expired.
	 * TODO: get rid of attachmentMode param once we switch to CIFS drive paths
	 * @param attachmentMode Should the contents be returned directly or as a file attachment
	 * @param path CmsPath
	 * @return URL
	  */
	public URL getHttpUrl(CmsPath path, String attachmentMode);

	/**
	 * Get all the transforms available for this node (including the 
	 * raw text).
	 * @param path CmsPath
	 * @return Map<String,TransformData>
	  */
	public Map<String, TransformData> getTransforms(CmsPath path);

	/**
	 * Adds a listener for notifications.
	 * @param listener IResourceEventListener
	 */
	public void addResourceEventListener(IResourceEventListener listener);

	/**
	 * Removes the specified <code>IResourceEventListener</code>.
	 * @param listener
	 */
	public void removeResourceEventListener(IResourceEventListener listener);

	/**
	 * Currently we don't cache resources associated with relationship since the user
	 * may not have permissions to access them.
	 * Additionally, we don't cache the relationship itself - may want to add to cache at some point w.r.t provenance views.
	 * Method getAssociation.
	 * @param path CmsPath
	 * @return IResource[] 
	 */
	public List<Relationship> getRelationships(CmsPath path)throws ResourceException;
	
	public void createRelationships(List<Relationship> relationships);
	public void createRelationship(Relationship relationship);
	public void deleteRelationships(List<Relationship> relationships);
	public void deleteRelationship(Relationship relationship);

	/**
	 * @param s
	 * @return String
	 */
	public String encodeISO9075(String s);
	
	/**
	 * Method decodeISO9075.
	 * @param s String
	 * @return String
	 */
	public String decodeISO9075(String s);

	public void clearCache();

	public void shutdown();

	/**
   * Method bulkDownload.
   * @param filesToDownload List<CmsPath>
   * @param destinationFolder File
   */
  public void bulkDownload(List<CmsPath> filesToDownload, File destinationFolder);
  public void bulkDownload(Map<CmsPath, File> filesToDownload);

  /**
   * Force the resource to be retrieved from the server.  Do not check the cache first.
   * This is to avoid a bug where sometimes the cache says it's complete, but the resource is null.
   * 
   * @param path
  
   * @return IResource
   */
  public IResource forceGetResource(CmsPath path);

  /**
   * Method wikiEnabled.
   * @return boolean
   */
  //public boolean wikiEnabled();

  /**
   * Method sendEmail.
   * @param email Email
   */
  public void sendEmail(Email email);
  
  /**
   * Method refreshChildren.
   * @param path CmsPath
   
   */
  void refreshChildren(CmsPath path);
  
  /**
   * Compare specified content property of a resource with a hash of another document
   * Method isIdentical.
   * @param resource Resource
   * @param property QualifiedName
   * @param hash String
   * @return boolean
   */
  public boolean isIdentical(CmsPath path, String property, String hash);
  
  /**
   * Defaults to principal ontent property
   * @param path
   * @param hash
   * @return
   */
  public boolean isIdentical(CmsPath path, String hash);
  
  /**
   * Method getHash.
   * @param file File
   * @return String
   */
  public String getHash(File file);
 

}
