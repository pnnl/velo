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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.util.ISO9075;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import gov.pnnl.cat.core.internal.resources.cache.Cache;
import gov.pnnl.cat.core.internal.resources.datamodel.CachedResource;
import gov.pnnl.cat.core.internal.resources.events.BatchNotification;
import gov.pnnl.cat.core.internal.resources.events.NotificationManagerJMS;
import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourceNotFoundException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.TransformData;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoUtils;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;
import gov.pnnl.cat.webservice.alert.RepositoryAlert;
import gov.pnnl.cat.webservice.subscription.Subscription;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.CmsPath.Segment;
import gov.pnnl.velo.model.Comment;
import gov.pnnl.velo.model.Email;
import gov.pnnl.velo.model.Properties;
import gov.pnnl.velo.model.Relationship;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.NodeNameProcessor;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class ResourceManagerImpl implements IResourceManager, IResourceEventListener {

  // 2006-11-02T13:27:57.393-08:00
  private static final String CACHE_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  
  //private IPath cifsDriveLetter = null;
  private IResourceService service;
  //private IPath cifsUncPath = null;
  private Cache cache;
  private NotificationManagerJMS notificationManager;
  private List<IResourceEventListener> listeners = new ArrayList<IResourceEventListener>();

  private static Logger logger = CatLogger.getLogger(ResourceManagerImpl.class);

  private static NodeNameProcessor nameProcessor;
  /**
   * Constructor for ResourceManagerImpl.
   * @param resourceService IResourceService
   * @param cifsPath String
   */
  public ResourceManagerImpl() {
    this.cache = new Cache(this);
  }

  /**
   * @param service the service to set
   */
  public void setService(IResourceService service) {
    this.service = service;
  }

  public IResourceService getService() {
    return service;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getUserDocumentsPath()
   */
  @Override
  public CmsPath getUserDocumentsPath() {
    return new CmsPath("/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}User Documents");
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getTeamDocumentsPath()
   */
  @Override
  public CmsPath getTeamDocumentsPath() {
    return new CmsPath("/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}Team Documents");
  }

  /**
   * @param notificationManager the notificationManager to set
   */
  public void setNotificationManager(NotificationManagerJMS notificationManager) {
    this.notificationManager = notificationManager;
  }

  /**
   * This method should only be called by the SecurityManager after the user has
   * successfully logged in.
   */
  public void afterLogin() {
    // listen for resource events coming straight from server
    notificationManager.addResourceEventListener(this);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getRoot()
   */
  @Override
  public IFolder getRoot()  {
    return new EclipseFolder(new CmsPath("/{http://www.alfresco.org/model/application/1.0}company_home"), VeloConstants.TYPE_FOLDER, this);
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getChildren(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public List<IResource> getChildren(CmsPath path)  {
    // make sure this resource is loaded before we try to get its children
    getResource(path);

    List<IResource> handles = cache.getChildrenHandles(path);
    if(handles == null) {
      List<Resource> resources = service.getChildren(path);
      cache.setRawChildren(path, resources);
    }
    return cache.getChildrenHandles(path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#refreshChildren(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public void refreshChildren(CmsPath path)  {
    cache.removeResource(path);
    // make sure this resource is loaded before we try to get its children
    getResource(path);
    getChildren(path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getChildCount(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public int getChildCount(CmsPath path) {
    IResource handle = getResource(path);
    if(handle != null) {
      CachedResource resource = cache.getResource(handle);
      return resource.getNumChildren();
    }
    return 0;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getProperty(gov.pnnl.velo.model.CmsPath, java.lang.String, boolean)
   */
  @Override
  public String getProperty(CmsPath path, String key, boolean checkServerIfNotInCache)  {
    IResource handle = getResource(path, checkServerIfNotInCache);
    if(handle != null) {
      CachedResource resource = cache.getResource(handle);
      return resource.getPropertyAsString(key);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getProperty(java.lang.String, java.lang.String)
   */
  @Override
  public String getProperty(String path, String key) {
    CmsPath cmsPath = new CmsPath(path);
    return getProperty(cmsPath, key);
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getMultiValuedProperty(gov.pnnl.velo.model.CmsPath, java.lang.String)
   */
  @Override
  public String[] getMultiValuedProperty(CmsPath path, String key)  {
    String[] ret = null;
    CachedResource resource = cache.getResource(path);
    if(resource != null) {
      List<String> values = resource.getProperties().get(key);
      if(values != null) {
        ret = values.toArray(new String[values.size()]);
      }
    }
    return ret;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#resourceCached(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public boolean resourceCached(CmsPath path) {
    if(path == null || path.size() == 0) {
      return false;
    }

    IResource handle = cache.getHandle(path);
    return handle != null;
  }


  // TODO: not sure if we want to expose this method in the IResourceManager interface or not
  public Resource getCachedResource(IResource resource) {
    return cache.getResource(resource.getPath());    
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getResource(gov.pnnl.cat.core.resources.CmsPath, boolean)
   */
  @Override
  public IResource getResource(CmsPath path, boolean checkServerIfNotInCache)  {
    if(path == null || path.size() == 0) {
      return null;
    }

    IResource handle = cache.getHandle(path);

    if(handle == null && checkServerIfNotInCache) {
      Resource resource = service.getResource(path);
      if(resource != null) {
        cache.addResource(resource);
        handle = cache.getHandle(path);
      }
    }

    return handle;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getResource(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public IResource getResource(CmsPath path)  {
    return getResource(path, true);
  }

  /**
   * Method getResources.
   * @param paths List<CmsPath>
   * @return List<IResource>
   * @
   * @see gov.pnnl.cat.core.resources.IResourceManager#getResources(List<CmsPath>)
   */
  @Override
  public List<IResource>getResources(List<CmsPath> paths)  {
    List<Resource> resources = service.getResourcesByPath(paths);
    List<IResource> handles = new ArrayList<IResource>();
    for(Resource resource : resources) {
      handles.add(cache.addResource(resource));
    }
    return handles;
  }
  
  @Override
  public List<IResource> getResourcesByUuid(List<String> uuids) {
    List<Resource> resources = service.getResourcesByUuid(uuids);
    List<IResource> handles = new ArrayList<IResource>();
    for(Resource resource : resources) {
      handles.add(cache.addResource(resource));
    }
    return handles;
  }

  /**
   * Method getResource.
   * @param uuid String
   * @return IResource
   * @
   * @throws ResourceNotFoundException
   * @throws AccessDeniedException
   * @see gov.pnnl.cat.core.resources.IResourceManager#getResource(String)
   */
  @Override
  public IResource getResource(String uuid)  {    
    IResource handle = cache.getHandle(uuid);
    if(handle == null) {
      Resource resource = service.getResource(uuid);
      if(resource != null) {
        cache.addResource(resource);
      }
    }

    handle = cache.getHandle(uuid);
    return handle;
  }

  /**
   * Method forceGetResource.
   * @param path CmsPath
   * @return IResource
   * @see gov.pnnl.cat.core.resources.IResourceManager#forceGetResource(CmsPath)
   */
  @Override
  public IResource forceGetResource(CmsPath path) {
    Resource resource = service.getResource(path);
    if(resource != null) {
      cache.addResource(resource);
    }
    return cache.getHandle(path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getCML()
   */
  @Override
  public ICatCML getCML() {
    return new CatCML();
  }

  /**
   * Method getCache.
   * @return Cache
   */
  public Cache getCache() {
    return cache;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#executeCml(gov.pnnl.cat.core.internal.resources.CatCML)
   */
  @Override
  public void executeCml(ICatCML cml) {
    List<Resource> modifiedResources = service.executeCml(cml);
    
    if(modifiedResources == null) {
    	return;
    }

    // Now we need to update the cache acordingly and send out an event
    List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();
    for(Resource resource : modifiedResources) {
      String cmlCmd = resource.getPropertyAsString(VeloConstants.PROP_CML_COMMAND);
      resource.getProperties().remove(VeloConstants.PROP_CML_COMMAND); //erase this transient value
      CmsPath nodePath = new CmsPath(resource.getPath());

      if(cmlCmd.equals(VeloConstants.CML_DELETE)) { // removed node
        cache.removeResource(nodePath);
        events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_REMOVED, resource.getUuid(), nodePath));

      } else if (cmlCmd.equals(VeloConstants.CML_CREATE) || 
          cmlCmd.equals(VeloConstants.CML_COPY)) { // new node
        cache.addResource(resource);
        events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, resource.getUuid(), nodePath));

      } else { // modified node
        cache.addResource(resource);
        events.add(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), nodePath));        
      }
    }

    // Send event to UI
    notifyListeners(createBatchNotification(events));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getFavorites()
   */
  @Override
  public IFolder getFavorites()  {
    // get the username for the currently logged-in user
    ISecurityManager scrMgr = ResourcesPlugin.getSecurityManager();
    String username = scrMgr.getUsername();
    IUser user = scrMgr.getUser(username);
    CmsPath favPath = user.getHomeFolder().append(IResourceManager.FAVORITES);
    return (IFolder) getResource(favPath);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getHomeFolder()
   */
  @Override
  public IFolder getHomeFolder()  {
    // get the username for the currently logged-in user
    ISecurityManager scrMgr = ResourcesPlugin.getSecurityManager();
    String username = scrMgr.getUsername();
    IUser user = scrMgr.getUser(username);
    return (IFolder) getResource(user.getHomeFolder());
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getPersonalLibrary()
   */
  @Override
  public IFolder getPersonalLibrary()  {
    // get the username for the currently logged-in user
    ISecurityManager scrMgr = ResourcesPlugin.getSecurityManager();
    String username = scrMgr.getUsername();
    IUser user = scrMgr.getUser(username);
    CmsPath personalLibraryPath = user.getHomeFolder().append(IResourceManager.PERSONAL_LIBRARY);
    return (IFolder) getResource(personalLibraryPath);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getProjects()
   */
  @Override
  public IFolder[] getProjects()  {
    return getFoldersByAspect(VeloConstants.ASPECT_PROJECT);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getTaxonomies()
   */
  public IFolder[] getTaxonomies()  {
    return getFoldersByAspect(VeloConstants.ASPECT_TAXONOMY_ROOT);
  }

  /**
   * Method getFoldersByAspect.
   * @param aspectQname String
   * @return IFolder[]
   */
  private IFolder[] getFoldersByAspect(String aspectQname) {
    List<Resource> resources = service.getResourcesByAspect(aspectQname);
    // convert the resources to IFolders
    IFolder[] folders = new IFolder[resources.size()];
    int i = 0;
    for(Resource resource : resources) {
      IFolder folder = (IFolder)cache.addResource(resource);
      folders[i] = folder;
      i++;
    }
    return folders;	  
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createProject(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public IFolder createProject(CmsPath folderPath, String mimetype)  {
    Resource folderRequest = new Resource(VeloConstants.TYPE_FOLDER, folderPath);
    folderRequest.getAspects().add(VeloConstants.ASPECT_PROJECT);
    if(mimetype != null) {
      folderRequest.setProperty(VeloConstants.PROP_MIMETYPE, mimetype);
    }
    createFolders(folderRequest);
    return (IFolder) getResource(folderPath);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#addTaxonomy(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public IFolder createTaxonomy(CmsPath folderPath) {
    Resource folderRequest = new Resource(VeloConstants.TYPE_FOLDER, folderPath);
    folderRequest.getAspects().add(VeloConstants.ASPECT_TAXONOMY_ROOT);
    createFolders(folderRequest);
    return (IFolder) getResource(folderPath);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#resourceExists(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public boolean resourceExists(CmsPath path)  {
    return service.resourceExists(path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#resourceExists(java.lang.String)
   */
  @Override
  public boolean resourceExists(String path) {
    return resourceExists(new CmsPath(path));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#addFolder(gov.pnnl.cat.core.resources.IResource, java.lang.String, java.lang.String)
   */
  @Override
  public IFolder createFolder(CmsPath path) {      
    return createFolder(path, null, false);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#addFolder(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public IFolder createFolder(CmsPath path, boolean recursive)  {
    return createFolder(path, null, recursive);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#addFolder(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public IFolder createFolder(CmsPath path, String mimetype)  {
    return createFolder(path, mimetype, false);
  }

  protected IFolder createFolder(CmsPath path, String mimetype, boolean recursive)  {
    List<Resource> foldersToCreate = new ArrayList<Resource>();

    if(recursive == true) {
      for(int i = 1; i <= path.size(); i++) {
        CmsPath subpath = path.subPath(i);
        subpath = applyNodeNameRules(subpath, false);
        Resource folderRequest = new Resource(VeloConstants.TYPE_FOLDER, subpath);
        if(i == path.size()) {
          folderRequest.setProperty(VeloConstants.PROP_MIMETYPE, mimetype);
        }
        foldersToCreate.add(folderRequest);
      }
    } else {
      path = applyNodeNameRules(path, false);
      Resource folderRequest = new Resource(VeloConstants.TYPE_FOLDER, path);
      if(mimetype != null) {
        folderRequest.setProperty(VeloConstants.PROP_MIMETYPE, mimetype);
      }
      foldersToCreate.add(folderRequest);
    }    
    createFolders(foldersToCreate);

    return (IFolder)getResource(path);
  }

  protected RepositoryEvent createRepositoryEvent(String eventType, String nodeId, CmsPath nodePath) {
    return createRepositoryEvent(eventType, nodeId, nodePath.getName(), nodePath.toAssociationNamePath());
  }

  protected RepositoryEvent createRepositoryEvent(String eventType, String nodeId, String nodeName, String nodePath) {
    RepositoryEvent event = new RepositoryEvent(eventType);
    if(nodeId != null) {
      event.setNodeId(nodeId);
    }
    event.setNodePath(nodePath);
    event.setPropertyName(nodeName);

    event.setEventPerpetrator(CmsServiceLocator.getSecurityManager().getUsername());
    event.setEventTimestamp(System.currentTimeMillis());
    return event;
  }

  protected IBatchNotification createBatchNotification(List<RepositoryEvent> events) {
    RepositoryEventList eventList = new RepositoryEventList();
    for(RepositoryEvent event : events) {
      eventList.add(event);
    }
    RepositoryEventMessage message = new RepositoryEventMessage();
    message.setEvents(eventList);
    IBatchNotification bn = new BatchNotification(message.getEvents().iterator());
    return bn;
  }

  protected IBatchNotification createBatchNotification(RepositoryEvent... events) {    
    RepositoryEventList eventList = new RepositoryEventList();
    for(RepositoryEvent event : events) {
      eventList.add(event);
    }
    RepositoryEventMessage message = new RepositoryEventMessage();
    message.setEvents(eventList);
    IBatchNotification bn = new BatchNotification(message.getEvents().iterator());
    return bn;
  }

  protected List<IFolder> createFolders(Resource... foldersToCreate) {
    List<Resource> folders = new ArrayList<Resource>();
    for(Resource resource : foldersToCreate) {
      folders.add(resource);
    }
    return createFolders(folders);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createFolders(java.util.List)
   */
  @Override
  public List<IFolder> createFolders(List<Resource> foldersToCreate) {
    boolean processAllSegments = false;
    if (foldersToCreate.size()>1) {
      processAllSegments = true;
      // because you can have a list such as [/home/user1/bad_*_folder, /home/user1/bad_*_folder/subfolder] 
    }
    List<IFolder> ifolders = new ArrayList<IFolder>();
    for (int i=0; i<foldersToCreate.size(); i++){
      Resource resource = foldersToCreate.get(i);
      CmsPath cmsPath = new CmsPath(resource.getPath());
      CmsPath newPath = applyNodeNameRules(cmsPath, processAllSegments);
      resource.setPath(newPath.toAssociationNamePath());
      resource.setName(newPath.getName());
      foldersToCreate.set(i, resource); 
    }
    List<Resource> resources = service.createFolders(foldersToCreate);
    List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();

    for (Resource resource : resources) {
      // Update the cache immediately
      ifolders.add((IFolder)cache.addResource(resource));
      events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, resource.getUuid(), resource.getName(), resource.getPath()));
    }

    // Send event to UI
    notifyListeners(createBatchNotification(events));

    return ifolders;    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#executeActions(org.alfresco.repo.webservice.types.Predicate, org.alfresco.repo.webservice.action.Action[])
   */
  @Override
  public void executeActions(Predicate predicate, Action[] actions){
    service.executeActions(predicate, actions);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createSubscription(gov.pnnl.cat.webservice.subscription.Subscription)
   */
  @Override
  public void createSubscription(Subscription subscription){
    service.createSubscription(subscription);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteSubscriptions(gov.pnnl.cat.webservice.subscription.Subscription[])
   */
  @Override
  public void deleteSubscriptions(Subscription[] subscription){
    service.deleteSubscriptions(subscription);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getSubscriptions()
   */
  @Override
  public Subscription[] getSubscriptions(){
    return service.getSubscriptions();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteAlerts(gov.pnnl.cat.webservice.alert.RepositoryAlert[])
   */
  @Override
  public void deleteAlerts(RepositoryAlert[] alerts) {
    service.deleteAlerts(alerts);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#markAlertsAsRead(gov.pnnl.cat.webservice.alert.RepositoryAlert[])
   */
  @Override
  public void markAlertsAsRead(RepositoryAlert[] alerts) {
    service.markAlertsAsRead(alerts);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getAlerts()
   */
  @Override
  public RepositoryAlert[] getAlerts() {
    return service.getAlerts();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createFolder(gov.pnnl.cat.core.resources.IResource, java.lang.String)
   */
  @Override
  public IFolder createFolder(IResource parentFolder, String folderName) {
    CmsPath path = parentFolder.getPath().append(folderName);
    return createFolder(path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createFile(gov.pnnl.velo.model.CmsPath, java.io.File)
   */
  @Override
  public IFile createFile(CmsPath path, File content)  {
    IFile newFile = null;
    try {
      Resource resource = null;
      path = applyNodeNameRules(path, false);
      
      if(content == null) { // call create empty file and add to cache
        Resource emptyResource = new Resource();
        emptyResource.setPath(path.toAssociationNamePath());
        emptyResource.setName(path.getName());
        emptyResource.setType(VeloConstants.TYPE_FILE);
        resource = service.createEmptyFile(emptyResource);
        
        if(resource != null){
          // update cache immediately
          newFile =  (IFile)cache.addResource(resource);
          
          // Send event to UI
          notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, resource.getUuid(), path)));

        }else {
          throw new ResourceException("File creation failed, null resource returned from server.");
        }

      } else { // bulk upload file, which will automatically add to cache
      
        
        Map<File, CmsPath> filesToDestinationPath = new HashMap<File, CmsPath>();
        filesToDestinationPath.put(content, path);
        
        bulkUpload(filesToDestinationPath, null, true); // we want to automatically add this to the file cache
        newFile = (IFile)getResource(path);
      }
      
    } catch (Throwable e) {
      throw new ResourceException(e);
    }
    return newFile;
  }

  /*
   * Calls the nodeNameProcessor bean to validate the node name and return updated
   * name if needed. Common use case is to remove/replace special characters. 
   * Each application can create its own nodeNameProcessor by creating a bean that 
   * implements gov.pnnl.velo.util.NodeNameProcessor
   */
  private CmsPath applyNodeNameRules(CmsPath path, boolean processAllSegments) {
    if(nameProcessor == null){
      nameProcessor = (NodeNameProcessor) ResourcesPlugin.getBean(VeloConstants.NAME_PROCESS_BEAN);
    }
    CmsPath newPath = path;
    if (processAllSegments){
      newPath = new CmsPath();
      newPath = newPath.append(path.get(0));
      for (int i=1; i<path.size();i++){
        newPath = newPath.append(nameProcessor.processNodeName(path.get(i)));
      }
      
    }else{
      Segment lastSegment = path.get(path.size()-1);
      Segment newSegment = nameProcessor.processNodeName(lastSegment);
      if (!newSegment.equals(lastSegment)){
        newPath = path.getParent().append(newSegment);
      }
    }
    return newPath;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createFile(gov.pnnl.cat.core.resources.IResource, java.io.File)
   */
  @Override
  public IFile createFile(IResource parent, File file) {
    return createFile(parent.getPath().append(file.getName()), file);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createLink(gov.pnnl.velo.model.CmsPath, gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public ILinkedResource createLink(CmsPath path, IResource target)  {
    Resource targetResource = cache.getResource(target.getPath());

    if(targetResource != null) {
      path = applyNodeNameRules(path, false);
      Resource link = new Resource(VeloConstants.TYPE_LINKED_FILE, path);
      link.setProperty(VeloConstants.PROP_LINK_DESTINATION, AlfrescoUtils.getReferenceString(targetResource.getUuid()));
      List<Resource> links = new ArrayList<Resource>();
      links.add(link);
      link = service.createLinks(links).get(0);

      // update cache immediately
      ILinkedResource handle = (ILinkedResource)cache.addResource(link);

      // Send event to UI
      notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, link.getUuid(), path)));
      return handle;

    } else {
      throw new ResourceException("Could not link to " + target.getPath().toDisplayString() + " because it is not in the cache.");
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createRemoteLinks(java.util.List)
   */
  @Override
  public void createRemoteLinks(List<RemoteLink> remoteLinks) {
    List<Resource> newLinks = service.createRemoteLinks(remoteLinks);
    List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();

    for(Resource newLink : newLinks) {
      // update cache immediately
      IResource handle = cache.addResource(newLink);
      events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, newLink.getUuid(), newLink.getName(), newLink.getPath()));
    }
    // Send event to UI
    notifyListeners(createBatchNotification(events));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#copy(gov.pnnl.cat.core.resources.CmsPath, gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public void copy(CmsPath source, CmsPath destination)  {
    copy(source, destination, false);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#copy(gov.pnnl.velo.model.CmsPath, gov.pnnl.velo.model.CmsPath, boolean)
   */
  @Override
  public void copy(CmsPath source, CmsPath destination, boolean overwrite) {
    destination = applyNodeNameRules(destination, false);
    
    Resource newNode = service.copy(source, destination, overwrite);

    // update cache immediately
    cache.addResource(newNode);

    // Send event to UI
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, newNode.getUuid(), destination)));

  }


  @Override
  public void importUrlList(CmsPath destination, List<String> urls, IProgressMonitor monitor)  {
    service.importUrlList(destination, urls, monitor);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#sendEmail(gov.pnnl.cat.datamodel.Email)
   */
  @Override
  public void sendEmail(Email email) {
    service.sendEmail(email);    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteResource(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public void deleteResource(CmsPath path)  {
    List<CmsPath> paths = new ArrayList<CmsPath>();
    paths.add(path);
    deleteResources(paths);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteResource(java.lang.String)
   */
  @Override
  public void deleteResource(String uuid) {
    CmsPath path = cache.getHandle(uuid).getPath();

    Resource resource = new Resource(uuid);
    List<Resource> resourceList = new ArrayList<Resource>();
    resourceList.add(resource);
    service.deleteResources(resourceList, null);

    // remove from cache
    cache.removeResource(uuid);

    // send notification
    if(path != null) {
      notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_NODE_REMOVED, uuid, path)));
    }

  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteResources(java.util.List)
   */
  @Override
  public void deleteResources(List<CmsPath> paths) {
    deleteResources(paths, null);    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteResources(java.util.List, java.lang.String)
   */
  @Override
  public void deleteResources(List<CmsPath> paths, String deleteOption)  {
    ArrayList<Resource> resourceList = new ArrayList<Resource>();
    List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();
    RepositoryEvent event;

    for(CmsPath path : paths) {

      Resource resource = cache.getResource(path);
      Resource alfrescoResource = null;
      if(resource != null) {
        alfrescoResource = new Resource(resource.getUuid());  // if we add uuid to the request, it will run faster than path
        events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_REMOVED, resource.getUuid(), path));

      } else {
        alfrescoResource = new Resource();
        alfrescoResource.setPath(path.toAssociationNamePath());
        events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_REMOVED, null, path));
      }
      resourceList.add(alfrescoResource);
    }
    service.deleteResources(resourceList, deleteOption);

    // update cache and notify listeners
    for(CmsPath path : paths) {
      cache.removeResource(path);
    }
    notifyListeners(createBatchNotification(events));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#move(gov.pnnl.cat.core.resources.CmsPath, gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public void move(CmsPath oldPath, CmsPath newPath)  {
    Resource oldResource = cache.getResource(oldPath);
    newPath = applyNodeNameRules(newPath, false);
    Resource newResource = service.move(oldPath, newPath);
    List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();

    cache.removeResource(oldPath);    
    cache.addResource(newResource);

    events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_REMOVED, oldResource.getUuid(), oldPath));
    events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, newResource.getUuid(), newPath));
    notifyListeners(createBatchNotification(events));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getResourcesLinkedToPath(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public Collection<IResource> getResourcesLinkedToPath(CmsPath path)  {
    CachedResource source = cache.getResource(path);
    // note that right now this uses the search manager to get the resources, which automatically
    // loads search results to cache, so we don't have to add them again to cache
    ICatQueryResult linkedResources = service.getLinkedResources(source);
    return linkedResources.getHandles();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#addResourceEventListener(gov.pnnl.cat.core.resources.events.IResourceEventListener)
   */
  @Override
  public void addResourceEventListener(IResourceEventListener listener) {
    synchronized(this.listeners) {
      // remove it first
      this.listeners.remove(listener);
      this.listeners.add(listener);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#removeResourceEventListener(gov.pnnl.cat.core.resources.events.IResourceEventListener)
   */
  @Override
  public void removeResourceEventListener(IResourceEventListener listener) {
    synchronized (this.listeners) {
      this.listeners.remove(listener);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getPropertyAsDate(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public Calendar getPropertyAsDate(CmsPath path, String key) throws ResourceException, ParseException {
    String value = getProperty(path, key);

    if (value == null || value.length() == 0) {
      return null;
    }

    int lastColon = value.lastIndexOf(':');
    String beforeColon = value.substring(0, lastColon);
    String afterColon = value.substring(lastColon + 1, value.length());
    value = beforeColon + afterColon;

    // 2006-11-02T13:27:57.393-08:00
    SimpleDateFormat format = new SimpleDateFormat(CACHE_DATE_PATTERN);
    Date date = format.parse(value);
    //    System.out.println("THE DATE: " + date);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);

    return calendar;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getContentProperty(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public InputStream getContentProperty(CmsPath path, String property)  {
    try {
      File content = getContentPropertyAsFile(path, property);

      InputStream is = null;
      if(content != null) {
        is = new FileInputStream(content);
      }
      return is;

    } catch (Throwable e) {
      throw new ResourceException(e);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getContentProperty(java.lang.String, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public InputStream getContentProperty(String uuid, String property)  {
    try {
      File content = getContentPropertyAsFile(uuid, property);

      InputStream is = null;
      if(content != null) {
        is = new FileInputStream(content);
      }
      return is;

    } catch (Throwable e) {
      throw new ResourceException(e);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getContentPropertyAsFile(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public File getContentPropertyAsFile(CmsPath path, String property)  {
    String uuid = null;
    String md5Hash = null;
    String fileName = path.getName();
    IResource resource = getResource(path);
    if(resource != null) {
      uuid = getProperty(path, VeloConstants.PROP_UUID);
      md5Hash = getProperty(path, VeloConstants.PROP_HASH);
    }
    return getContentPropertyAsFile(uuid, fileName, property, null, md5Hash, null);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getContentPropertyAsFile(gov.pnnl.velo.model.CmsPath, java.lang.String, java.lang.String, java.io.File)
   */
  @Override
  public File getContentPropertyAsFile(CmsPath path, String property, String version, File destinationFile) {

    String uuid = null;
    String md5Hash = null;
    String fileName = path.getName();
    IResource resource = getResource(path);
    if(resource != null) {
      uuid = getProperty(path, VeloConstants.PROP_UUID);
      md5Hash = getProperty(path, VeloConstants.PROP_HASH);
    }
    return getContentPropertyAsFile(uuid, fileName, property, version, md5Hash, destinationFile);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getContentPropertyAsFile(java.lang.String, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public File getContentPropertyAsFile(String uuid, String property)  {
    return getContentPropertyAsFile(uuid, null, property, null, null, null);
  }
  
  /**
   * Root method that all variations call.
   * @param uuid
   * @param fileName
   * @param property
   * @param version
   * @param destinationFile
   * @return
   */
  private File getContentPropertyAsFile(String uuid, String fileName, String property, 
      String version, String serverMD5Hash, File destinationFile) {
    
    try {
      // If uuid == null, then this resource doesn't exist, so just return  null for the file
      // since without a uuid, we won't be able to put it in the cache anyway
      if(uuid == null) {
        return null;
      }
      
      if(property == null) {
        property = VeloConstants.PROP_CONTENT;
      }
      
      boolean loadFromServer = false;
      Resource resource = new Resource(uuid);      
      File cachedFile = destinationFile;
      
      if(cachedFile == null) {
        // First see if this file is in the cache
        cachedFile = cache.getCachedContent(uuid, fileName, version, property);
      }
      
      if(cachedFile != null && cachedFile.exists()) {
        // see if this file has changed on the server and needs to be refreshed
        String cachedHash = service.getHash(cachedFile);
        if(serverMD5Hash != null) {
          if(!serverMD5Hash.equals(cachedHash)) { // we can compare based on cached property
            loadFromServer = true;
          }
          
        } else if(!service.isIdentical(resource, property, cachedHash)) { // we have to look up from server
          loadFromServer = true;
        }
     
      } else {
        loadFromServer = true;
      }

      if(loadFromServer) {
        service.getContentPropertyAsFile(resource, property, cachedFile, version);
      }

      return cachedFile;

    } catch (Throwable e) {
      throw new ResourceException(e);
    } 
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getThumbnail(gov.pnnl.velo.model.CmsPath, java.lang.String)
   */
  @Override
  public InputStream getThumbnail(CmsPath resourcePath, String thumbnailName) {

    try {
      File thumbnailFile = getThumbnailFile(resourcePath, thumbnailName);
      InputStream is = null;
      if(thumbnailFile != null && thumbnailFile.length() > 0) {
        is = new FileInputStream(thumbnailFile);
      }
      return is;

    } catch (Throwable e) {
      throw new ResourceException(e);
    }
  }
  
  private File getThumbnailFile(CmsPath resourcePath, String thumbnailName) {
    File cachedFile = null;
    
    try {
      String uuid = null;
      String property = VeloConstants.PROP_CONTENT;
      if(thumbnailName == null) {
        thumbnailName = VeloConstants.THUMBNAIL_PREVIEW_PANE;
      }
      IResource resource = getResource(resourcePath);
      if(resource != null) {
        uuid = resource.getPropertyAsString(VeloConstants.PROP_UUID);
      }
      
      boolean loadFromServer = false;
      // First see if this file is in the cache
      cachedFile = cache.getCachedContent(uuid, thumbnailName, null, property);
      
      if(cachedFile != null && cachedFile.exists()) {
        // see if this file has changed on the server and needs to be refreshed
        String hash = service.getHash(cachedFile);
        Resource r = new Resource(resourcePath.append(thumbnailName));
        if(!service.isIdentical(r, property, hash)) {
          loadFromServer = true;
        }
     
      } else {
        loadFromServer = true;
      }

      if(loadFromServer) {
        byte[] thumbnail = service.getThumbnail(resourcePath, thumbnailName);
        FileUtils.writeByteArrayToFile(cachedFile, thumbnail);
      }

      return cachedFile;

    } catch (Throwable e) {
      throw new ResourceException(e);
    } 
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getTarget(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public IResource getTarget(CmsPath path)  {
    return getPropertyAsResource(path, VeloConstants.PROP_LINK_DESTINATION);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#addComment(gov.pnnl.cat.core.resources.CmsPath, java.lang.String)
   */
  @Override
  public Comment addComment(CmsPath path, String commentText) {
    Comment comment = null;

    IResource handle = getResource(path);
    if(handle != null) {
      Resource resource = cache.getResource(handle.getPath());
      comment = service.addComment(resource, commentText);
    }

    return comment;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getComments(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public Comment[] getComments(CmsPath path) {

    IResource handle = getResource(path);
    if(handle != null) {
      Resource resource = cache.getResource(handle.getPath());
      return service.getComments(resource);
    }

    return new Comment[0];
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getPropertyAsResource(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public IResource getPropertyAsResource(CmsPath path, String property)  {

    IResource handle = getResource(path);
    if(handle != null) {
      CachedResource resource = cache.getResource(handle.getPath());
      String destination = resource.getPropertyAsString(property);
      // destination could be null if target was deleted
      if(destination != null) {
        String targetUUID = AlfrescoUtils.parseUuidFromReferenceString(destination);
        IResource target = getResource(targetUUID);
        return target;
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getUUID(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public String getUUID(CmsPath path) {
    IResource handle = getResource(path);
    if(handle != null) {
      Resource resource = cache.getResource(handle);
      return resource.getUuid();

    } else {
      return null;
    }    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getRepositoryUrlBase()
   */
  @Override
  public String getRepositoryUrlBase() {
    return service.getRepositoryUrlBase();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getWebdavUrl(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public URL getWebdavUrl(CmsPath path)  {

    try {
      return new URL(service.getWebdavUrl(path));
    } catch (MalformedURLException e) {
      throw new ResourceException(e);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getHttpUrl(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public URL getHttpUrl(CmsPath path)  {
    return getHttpUrl(path, IFile.DIRECT);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getHttpUrl(gov.pnnl.cat.core.resources.CmsPath, java.lang.String)
   */
  @Override
  public URL getHttpUrl(CmsPath path, String attachmentMode)  {
    IResource resource = this.getResource(path);

    String urlStr = service.getHttpUrl(resource.getPropertyAsString(VeloConstants.PROP_UUID), 
        resource.getPropertyAsString(VeloConstants.PROP_NAME), 
        VeloConstants.PROP_CONTENT, 
        attachmentMode);

    try {
      return new URL(urlStr);
    } catch (MalformedURLException e) {
      throw new ResourceException(e);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getTransforms(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public Map<String, TransformData> getTransforms(CmsPath path)  {

    Map<String, TransformData> map = new HashMap<String, TransformData>();

    // Resource could be null if the node has just been deleted and we are in
    // the middle of a batch delete, and lots of notifications are coming back
    // from the server - this is a timing issue
    // so we have to check for a null condition
    IResource resource = this.getResource(path);

    // Currently only the text transform exists 
    // If has transform aspect then create TransformData object
    if (resource != null && resource.hasAspect(VeloConstants.ASPECT_TEXT_TRANSFORM)) {

      // Create TransformData and store in map
      try {  
        String errorMsg = getProperty(path, VeloConstants.PROP_TEXT_TRANSFORM_ERROR);
        TransformData data = new TransformData(TransformData.TEXT, VeloConstants.PROP_TEXT_TRANSFORMED_CONTENT, errorMsg);
        map.put(data.getTransformerName(), data);
      }
      catch (ResourceException ex) {
        logger.warn("Failed to create TransformData :: " + ex.getMessage());   	  
      }
    }
    return map;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#setProperties(gov.pnnl.cat.core.resources.CmsPath, java.util.Map)
   */
  @Override
  public void setProperties(CmsPath resourcePath, Map<String, String> properties) {
    if(properties != null && properties.size() > 0) {

      Resource resource = new Resource();
      resource.setPath(resourcePath.toAssociationNamePath());
      resource.setName(resourcePath.getName());
      for(String key : properties.keySet()) {
        resource.setProperty(key, properties.get(key));
      }
      resource = service.setProperties(resource);

      // Update the cache immediately
      cache.addResource(resource);

      // Send event to UI
      // TODO: for now we are not sending an event for every property
      notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), resourcePath)));
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#setMultiValuedProperties(gov.pnnl.cat.core.resources.CmsPath, java.util.Map)
   */
  @Override
  public void setMultiValuedProperties(CmsPath resourcePath, Map<String, List<String>> properties) {
    Resource resource = new Resource();
    resource.setPath(resourcePath.toAssociationNamePath());
    resource.setName(resourcePath.getName());

    for(String key : properties.keySet()) {
      resource.setProperty(key, properties.get(key));      
    }
    resource = service.setProperties(resource);   

    // Update the cache immediately
    cache.addResource(resource);

    // Send event to UI
    // TODO: for now we are not sending an event for every property
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), resourcePath)));

  }
  
  @Override
  public List<IResource> getCategories(CmsPath resourcePath) {
    IResource handle = getResource(resourcePath);
    if(handle != null) {
      CachedResource resource = cache.getResource(handle);
      List<IResource> categories = new ArrayList<IResource>();
      List<String> values = resource.getProperties().get(VeloConstants.PROP_CATEGORIES);
      
      if(values != null) {
        List<String> uuids = new ArrayList<String>();
        for(String nodeRefStr : values) {
          int loc = nodeRefStr.lastIndexOf('/');
          String uuid = nodeRefStr.substring(loc+1);
          uuids.add(uuid);
        }
        categories = getResourcesByUuid(uuids);
      }
      return categories;
    }
    return null;

  }
  
  @Override
  public void setCategories(CmsPath resourcePath, List<IResource> categories) {

    Resource resource = new Resource();
    resource.setPath(resourcePath.toAssociationNamePath());
    resource.setName(resourcePath.getName());

    List<String> categoriesProp = new ArrayList<String>();

    for(IResource category : categories) {
      String nodeRefString = "workspace://SpacesStore/" + category.getPropertyAsString(VeloConstants.PROP_UUID);
      categoriesProp.add(nodeRefString);

    }
    resource.setProperty(VeloConstants.PROP_CATEGORIES, categoriesProp);

    // make sure to aded the classifiable aspect
    resource.getAspects().add(VeloConstants.ASPECT_CLASSIFIABLE);

    Resource updatedResource = service.setProperties(resource);

    // Update the cache immediately
    cache.addResource(resource);
 
    // Send event to UI
    // TODO: for now we are not sending an event for every property
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), new CmsPath(resource.getPath()))));

  }

  @Override
  public void addCategories(List<CmsPath> resourcePaths, IResource category) {
    
    List<Resource> resources = new ArrayList<Resource>();
    
    for(CmsPath path : resourcePaths) {
      getResource(path); // make sure it's loaded to be safe
      Resource resource = new Resource();
      resource.setPath(path.toAssociationNamePath());
      resource.setName(path.getName());
      
      List<String> categoriesProp = cache.getResource(path).getProperties().get(VeloConstants.PROP_CATEGORIES);
      
      if(categoriesProp == null) {
        categoriesProp = new ArrayList<String>();
      }
      String nodeRefString = "workspace://SpacesStore/" + category.getPropertyAsString(VeloConstants.PROP_UUID);
      if(!categoriesProp.contains(nodeRefString)) {
        categoriesProp.add(nodeRefString);
      }

      resource.setProperty(VeloConstants.PROP_CATEGORIES, categoriesProp);
      
      // make sure to aded the classifiable aspect
      resource.getAspects().add(VeloConstants.ASPECT_CLASSIFIABLE);
      resources.add(resource);
    }
    
    List<Resource> updatedResources = service.setProperties(resources);

    // Update the cache immediately
    List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();
    for(Resource resource : updatedResources) {
      cache.addResource(resource);
      events.add(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), new CmsPath(resource.getPath())));
    }

    // Send event to UI
    // TODO: for now we are not sending an event for every property
    notifyListeners(createBatchNotification(events));

  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#removeProperties(gov.pnnl.cat.core.resources.CmsPath, java.util.List)
   */
  @Override
  public void removeProperties(CmsPath path, List<String> properties) {
    Resource resource = new Resource();
    resource.setPath(path.toAssociationNamePath());
    resource.setName(path.getName());
    for(String key : properties) {
      resource.setProperty(key, (String)null);
    }
    resource = service.setProperties(resource);    

    // Update the cache immediately
    cache.addResource(resource);

    // Send event to UI
    // TODO: for now we are not sending an event for every property
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_REMOVED, resource.getUuid(), path)));

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#setProperty(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName, java.lang.String)
   */
  @Override
  public void setProperty(CmsPath resourcePath, String key, String value)  {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(key, value);
    Resource resource = new Resource();
    resource.setPath(resourcePath.toAssociationNamePath());
    resource.setName(resourcePath.getName());
    resource.setProperty(key, value);
    resource = service.setProperties(resource);

    // Update the cache immediately
    cache.addResource(resource);

    // Send event to UI
    // TODO: for now we are not sending an event for every property
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), resourcePath)));

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#setProperty(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName, gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public void setProperty(CmsPath resourcePath, String key, IResource value)  {
    String uuid = value.getPropertyAsString(VeloConstants.PROP_UUID);
    // TODO: this needs to be move to Service implementation so we don't have alfresco-specific code in the resource manager API
    String refString = AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, uuid);   
    setProperty(resourcePath, key, refString);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#setProperty(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty(String path, String key, String value) {
    // Path doesn't have to start  with company home (i.e., /User Documents/admin/test1.txt)
    CmsPath cmsPath = new CmsPath(path);
    setProperty(cmsPath, key, value);   
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#updateLinkTarget(gov.pnnl.cat.core.resources.CmsPath, gov.pnnl.cat.core.resources.IFile)
   */
  @Override
  public void updateLinkTarget(CmsPath path, IResource newTarget)  {
    Resource link = cache.getResource(path);
    Resource target = cache.getResource(newTarget.getPath());
    link = this.service.updateLinkTarget(link, target);

    // Update the cache immediately
    cache.addResource(link);

    // Send event to UI
    RepositoryEvent event = createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, link.getUuid(), path);
    event.setPropertyName(VeloConstants.PROP_LINK_DESTINATION);
    event.setPropertyValue(target.getUuid());
    notifyListeners(createBatchNotification(event));

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getProperties(gov.pnnl.cat.core.resources.CmsPath)
   */
  /**
   * Method getPropertyNames.
   * @param path CmsPath
   * @return Set<QualifiedName>
   * @see gov.pnnl.cat.core.resources.IResourceManager#getPropertyNames(CmsPath)
   */
  @Override
  public Set<String> getPropertyNames(CmsPath path) {
    IResource handle = getResource(path);
    if(handle != null) {
      CachedResource resource = cache.getResource(handle);
      return resource.getProperties().keySet();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getPropertiesAsString(gov.pnnl.cat.core.resources.CmsPath, java.util.Vector)
   */
  @Override
  public List<String> getPropertiesAsString(CmsPath path, List<String> keys)  {
    List<String> results = new ArrayList<String>();
    IResource handle = getResource(path);
    if(handle != null) {
      CachedResource resource = cache.getResource(handle);
      for (String key : keys) {
        results.add(resource.getPropertyAsString(key));
      }
    }
    return results;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#addAspect(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public void addAspect(CmsPath path, String aspect)  {
    Resource resource = cache.getResource(path);
    resource = service.addAspect(resource, aspect);

    cache.addResource(resource);
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), path)));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#removeAspect(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public void removeAspect(CmsPath path, String aspect)  {
    Resource resource = cache.getResource(path);
    resource = service.removeAspect(resource, aspect);

    cache.addResource(resource);
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, resource.getUuid(), path)));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getAspects(gov.pnnl.cat.core.resources.CmsPath, boolean)
   */
  @Override
  public List<String> getAspects(CmsPath path, boolean checkServerIfNotInCache) throws AccessDeniedException, ResourceException {
    List<String> aspects = null;
    IResource handle = getResource(path, checkServerIfNotInCache);
    if(handle != null) {
      CachedResource resource = cache.getResource(handle);
      aspects = resource.getAspects();
    } else {
      aspects = new ArrayList<String>();
    }
    return aspects;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getAspects(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public List<String> getAspects(CmsPath path) {
    return getAspects(path, true);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#encodeISO9075(java.lang.String)
   */
  @Override
  public String encodeISO9075(String s) {
    return ISO9075.encode(s);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#decodeISO9075(java.lang.String)
   */
  @Override
  public String decodeISO9075(String s) {
    return ISO9075.decode(s);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getAssociation(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public List<Relationship> getRelationships(CmsPath path)  {

    return service.getRelationships(path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createRelationships(java.util.List)
   */
  @Override
  public void createRelationships(List<Relationship> relationships) {
    // TODO: currently relationships are not cached, but we should send an event
    // so that views with relationships are refreshed (we need to figure out what kind of event to use)
    service.createRelationships(relationships);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#createRelationship(gov.pnnl.velo.model.Relationship)
   */
  @Override
  public void createRelationship(Relationship relationship) {
    List<Relationship> relationships = new ArrayList<Relationship>();
    relationships.add(relationship);
    createRelationships(relationships); 
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteRelationships(java.util.List)
   */
  @Override
  public void deleteRelationships(List<Relationship> relationships) {
    service.deleteRelationships(relationships);    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#deleteRelationship(gov.pnnl.velo.model.Relationship)
   */
  @Override
  public void deleteRelationship(Relationship relationship) {
    List<Relationship> relationships = new ArrayList<Relationship>();
    relationships.add(relationship);
    deleteRelationships(relationships);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#shutdown()
   */
  @Override
  public void shutdown() {
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#bulkFileUpload(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, IProgressMonitor monitor)  {
    bulkUpload(filesToServerPath, null, null, monitor, false);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#bulkUpload(java.util.Map, org.eclipse.core.runtime.IProgressMonitor, boolean)
   */
  @Override
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, IProgressMonitor monitor, boolean addToFileCache) {
    bulkUpload(filesToServerPath, null, null, monitor, addToFileCache);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#bulkUpload(java.util.Map, gov.pnnl.velo.model.Properties, java.util.Map, org.eclipse.core.runtime.IProgressMonitor, boolean)
   */
  @Override
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, Properties globalMetadata,
      Map<String, Properties> fileSpecificMetadata, IProgressMonitor monitor, boolean addToFileCache) {  
    
    
    for(File f:filesToServerPath.keySet()){
       CmsPath cmsPath = filesToServerPath.get(f);
       cmsPath = applyNodeNameRules(cmsPath, true);
       filesToServerPath.put(f, cmsPath);
       cmsPath.toDisplayString();
    }
    
    this.service.bulkUpload(filesToServerPath, globalMetadata, fileSpecificMetadata, monitor);    

    // this adds resources to the resource cache, happens automatically
    updateCacheAndNotify(filesToServerPath, addToFileCache);
  }

  @Override
  public void updateContent(CmsPath path, InputStream stream, String property, String mimetype, String offset) {
    Resource updatedResource = service.updateContent(path, stream, property, mimetype, offset);
    cache.addResource(updatedResource);
    notifyListeners(createBatchNotification(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, updatedResource.getUuid(), path)));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#bulkDownload(java.util.List, java.io.File)
   */
  @Override
  public void bulkDownload(List<CmsPath> filesToDownload, File destinationFolder) {
    Map<CmsPath, File> fileMap = new HashMap<CmsPath, File>();
    for(CmsPath path : filesToDownload) {
      fileMap.put(path, new File(destinationFolder, path.getName()));
    }
    bulkDownload(fileMap);

  }

  @Override
  public void bulkDownload(Map<CmsPath, File> filesToDownload) {
    service.bulkDownload(filesToDownload);

    // TODO: Add the downloaded files to the cache
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getChildByName(gov.pnnl.cat.core.resources.IResource, java.lang.String)
   */
  @Override
  public IResource getChildByName(IResource parent, String childName) {
    IResource child = null;

    try {
      CmsPath path = parent.getPath();
      CmsPath childPath = path.append(childName);
      child = getResource(childPath);
    } catch (Throwable e) {
      // log but don't fail the call
      logger.debug("problem getting child", e);
    }

    return child;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getProperty(gov.pnnl.cat.core.resources.CmsPath, java.lang.String)
   */
  @Override
  public String getProperty(CmsPath path, String qname)  {
    return getProperty(path, qname, true);
  }

  /**
   * Method onEvent.
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  public void onEvent(IBatchNotification events) {
    try {
      updateCache(events);

      // Now send the events to the UI listeners
      notifyListeners(events);
    } catch (Throwable e) {
      Throwable rootCause = ExceptionUtils.getRootCause(e);
      String message = e.toString();
      if(rootCause != null) {
        message = rootCause.toString();
      }
      logger.warn("Failed to update cache: " + message);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO IResourceEventListener needs to be subclassed
  } 

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#clearCache()
   */
  @Override
  public void clearCache() {
    this.cache.clear();

    // trigger cache cleared event
    // Send the batch notification to all the listeners
    ArrayList<IResourceEventListener> listenersCopy;
    synchronized(this.listeners){
      listenersCopy = new ArrayList<IResourceEventListener>(this.listeners);
    }
    for (int i = 0; i < listenersCopy.size(); i++) {
      listenersCopy.get(i).cacheCleared();
    } 
  }

  /**
   * Method notifyListeners.
   * @param events IBatchNotification
   */
  private void notifyListeners(IBatchNotification events) {
    // Send the batch notification to all the listeners
    ArrayList<IResourceEventListener> listenersCopy;
    synchronized(this.listeners){
      listenersCopy = new ArrayList<IResourceEventListener>(this.listeners);
    }
    for (int i = 0; i < listenersCopy.size(); i++) {
      listenersCopy.get(i).onEvent(events);
    }    
  }

  private void updateCacheAndNotify(Map<File, CmsPath> filesToServerPath, boolean addToFileCache) {
    List<CmsPath> pathsToLookup = new ArrayList<CmsPath>();
    List<CmsPath> parentPaths = new ArrayList<CmsPath>();
    List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();
    Map<CmsPath, File> filesToCache = new HashMap<CmsPath, File>();
  
    for (File file : filesToServerPath.keySet()) {
      CmsPath path = filesToServerPath.get(file);
      CmsPath parentPath = path.getParent();

      // We must look up every file in order for us to put it in the file cache, since we need the uuid
      if(addToFileCache) {
        filesToCache.put(path, file);
        pathsToLookup.add(path);   
      } 
      // If the parent isn't in the cache - don't try to load this resource
      if(cache.getResource(parentPath) != null) {

        // If the parent's children aren't in the cache - don't try to load this resource
        // as it hasn't been browsed yet          
        List<CachedResource> parentsChildren = cache.getChildren(parentPath);
        if(parentsChildren != null) {
          if(!pathsToLookup.contains(path)) {
            pathsToLookup.add(path);
          }
          events.add(createRepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED, null, path));
          if(!parentPaths.contains(parentPath)) {
            parentPaths.add(parentPath);
            events.add(createRepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED, null, parentPath));
          }
        }
      }
    }

    // Add resources to the resource cache
    parentPaths.addAll(pathsToLookup);
    List<Resource> newNodes = service.getResourcesByPath(parentPaths);
    for(Resource resource : newNodes) {
      cache.addResource(resource);
    }
    
    // Callers have the option to also copy the uploaded files into the content cache, so they can be quickly
    // retrieved the next time they are requested.  This may not be performant for large data uploads, so 
    // this should be optional
    for(CmsPath path : filesToCache.keySet()) {
      String uuid = getProperty(path, VeloConstants.PROP_UUID);
      File srcFile = filesToCache.get(path);
      try {
        File destFile = cache.getCachedContent(uuid, path.getName(), null, VeloConstants.PROP_CONTENT);
        // only need to copy them over if we are not saving the same file that is already in the cache
        if(!srcFile.equals(destFile)) {
          FileUtils.copyFile(srcFile, destFile);
        }
      } catch (Throwable e) {
        logger.error("Failed to update file cache.", e);
        e.printStackTrace();
      }
    }
    
    notifyListeners(createBatchNotification(events));
  }

  /**
   * Method updateCache.
   * @param events IBatchNotification
   */
  private void updateCache(IBatchNotification events) {
    IResourceEvent event;
    List<CmsPath> pathsToLookup = new ArrayList<CmsPath>();
    List<CmsPath> parentPaths = new ArrayList<CmsPath>();

    logger.debug("Receiving new events...");
    
    for (Iterator<IResourceEvent> iter = events.getAllEvents(); iter.hasNext();) {
      event = (IResourceEvent) iter.next();

      // temporarily ignore /workflow/... events generated by JBPM engine
      if (event.toString().startsWith("/workflow")) {
        continue;
      }

      logger.debug(event.getPath() + " - " + event.getChangeFlags());

      if(event.hasChange(IResourceEvent.ADDED) || event.hasChange(IResourceEvent.PROPERTY_CHANGED) ||
          event.hasChange(IResourceEvent.ASPECTS_CHANGED) || event.hasChange(IResourceEvent.TARGET_CHANGED)) {     
        
        CmsPath path = event.getPath();
        CmsPath parentPath = path.getParent();
        
        // If the parent isn't in the cache - don't try to load this resource
        if(cache.getResource(parentPath) != null) {

          // If the parent's children aren't in the cache - don't try to load this resource
          // as it hasn't been browsed yet          
          List<CachedResource> parentsChildren = cache.getChildren(parentPath);
          if(parentsChildren != null) {
            pathsToLookup.add(path);
            if(!parentPaths.contains(parentPath)) {
              parentPaths.add(parentPath);
            }            
          }
        }
      
      } else if (event.hasChange(IResourceEvent.REMOVED)) {
        logger.debug(event + " resource removed");

        // clear everything we know about that node.      
        cache.removeResource(event.getPath());
      }

    }

    // Alresco 4: don't do a query here since the solr indexes may not be up to date
    if (pathsToLookup.size() > 0) {
      parentPaths.addAll(pathsToLookup);
      List<Resource> newNodes = service.getResourcesByPath(parentPaths);
      for(Resource resource : newNodes) {
        cache.addResource(resource);
      }
    }

  }

  //  /**
  //   * Method wikiEnabled.
  //   * @return boolean
  //   * @see gov.pnnl.cat.core.resources.IResourceManager#wikiEnabled()
  //   */
  //  @Override
  //  public boolean wikiEnabled(){
  //    return service.wikiEnabled();
  //  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#isIdentical(gov.pnnl.cat.core.resources.CmsPath, org.eclipse.core.runtime.QualifiedName, java.lang.String)
   */
  @Override
  public boolean isIdentical(CmsPath path, String property, String hash) {
    Resource resource = new Resource();
    resource.setPath(path.toAssociationNamePath());
    return service.isIdentical(resource, property, hash);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#isIdentical(gov.pnnl.cat.core.resources.CmsPath, java.lang.String)
   */
  @Override
  public boolean isIdentical(CmsPath path, String hash) {
    return isIdentical(path, VeloConstants.PROP_CONTENT, hash);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.IResourceManager#getHash(java.io.File)
   */
  @Override
  public String getHash(File file) {
    return service.getHash(file);
  }



}
