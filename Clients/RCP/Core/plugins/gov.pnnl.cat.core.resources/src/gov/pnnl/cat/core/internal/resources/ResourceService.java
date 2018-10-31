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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLCopy;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.CMLMove;
import org.alfresco.webservice.types.CMLRemoveAspect;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.CMLWriteContent;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Predicate;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.Base64Encoder;

import gov.pnnl.cat.core.internal.resources.search.CatQueryResult;
import gov.pnnl.cat.core.internal.resources.search.JsonCatQueryResult;
import gov.pnnl.cat.core.internal.resources.search.SearchManager;
import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourceNotFoundException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.search.SearchContext;
import gov.pnnl.cat.core.resources.security.CatSecurityException;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoUtils;
import gov.pnnl.cat.core.util.AbstractWebScriptClient;
import gov.pnnl.cat.core.util.ProxyConfig;
import gov.pnnl.cat.core.util.WebServiceUrlUtility;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.webservice.alert.RepositoryAlert;
import gov.pnnl.cat.webservice.group.GroupDetails;
import gov.pnnl.cat.webservice.group.GroupFilter;
import gov.pnnl.cat.webservice.group.GroupQueryResults;
import gov.pnnl.cat.webservice.group.NewGroupDetails;
import gov.pnnl.cat.webservice.subscription.Subscription;
import gov.pnnl.cat.webservice.user.NewUserDetails;
import gov.pnnl.cat.webservice.user.UserDetails;
import gov.pnnl.cat.webservice.user.UserFilter;
import gov.pnnl.cat.webservice.user.UserQueryResults;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.ChangePasswordRequest;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Comment;
import gov.pnnl.velo.model.Email;
import gov.pnnl.velo.model.ExecuteActionsRequest;
import gov.pnnl.velo.model.Properties;
import gov.pnnl.velo.model.QueryRequest;
import gov.pnnl.velo.model.Relationship;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.model.SearchResult;
import gov.pnnl.velo.util.PrioritizedThreadFactory;
import gov.pnnl.velo.util.VeloConstants;

/**
 * Provides access to Velo/Alfresco repository web services.
 * 
 * @version $Revision: 1.0 $
 */
public class ResourceService extends AbstractWebScriptClient implements IResourceService, InitializingBean {
  public static final String PLUGIN_ID = "gov.pnnl.cat.core.resources"; //$NON-NLS-1$
  public static final String EXTENSION_POINT_ID = "contentType"; //$NON-NLS-1$
  public static final String ATTRIBUTE_NODE_TYPE_QNAME = "nodeTypeQName"; //$NON-NLS-1$

  public static HashMap<String, Class<IResource>> typeClassMap = new HashMap<String, Class<IResource>>(5);

  /* Remote access URLs */
  private String repositoryUrl;
  protected ThreadPoolTaskScheduler multiThreadedUploadThreadPool;
  String bulkUploadUrl;

  static private ConfigurableMimeFileTypeMap mimeFileTypeMap;
  private static Logger logger = CatLogger.getLogger(ResourceService.class);

  /**
   * Constructs the ResourceService to use the specified repository.
   * @param repositoryURL
   * @param proxyConfig
   * @throws ResourceException
   */
  @SuppressWarnings("unchecked")
  public ResourceService(String repositoryURL, ProxyConfig proxyConfig) throws ResourceException {
    super(repositoryURL, proxyConfig);
    this.repositoryUrl = repositoryURL;
    this.bulkUploadUrl = repositoryURL + "/catupload";

    //wrap in try/catch, in catch hard code the typeClassMap, with our FileImpl
    try{
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IConfigurationElement[] elements = registry.getConfigurationElementsFor(ResourceService.PLUGIN_ID, ResourceService.EXTENSION_POINT_ID);
      for (IConfigurationElement element : elements) {
        Class<IResource> clazz;
        try {
          clazz = (Class<IResource>) Class.forName(element.getAttribute("class"));
          typeClassMap.put(element.getAttribute(ResourceService.ATTRIBUTE_NODE_TYPE_QNAME), clazz);
        } catch (Throwable e) {
          logger.error("invalid contentType extension point: " + element, e);
        } 
      } 
    } catch(Exception e) {
      try{
        typeClassMap.put("{http://www.alfresco.org/model/content/1.0}content", (Class<IResource>) Class.forName("gov.pnnl.cat.core.internal.resources.EclipseFileImpl"));
        typeClassMap.put("{http://www.alfresco.org/model/content/1.0}thumbnail", (Class<IResource>) Class.forName("gov.pnnl.cat.core.internal.resources.EclipseFileImpl"));
        typeClassMap.put("{http://www.alfresco.org/model/content/1.0}folder", (Class<IResource>) Class.forName("gov.pnnl.cat.core.internal.resources.EclipseFolderImpl"));
        typeClassMap.put("{http://www.alfresco.org/model/site/1.0}site", (Class<IResource>) Class.forName("gov.pnnl.cat.core.internal.resources.EclipseFolderImpl"));
        typeClassMap.put("{http://www.alfresco.org/model/content/1.0}link", (Class<IResource>) Class.forName("gov.pnnl.cat.core.internal.resources.EclipseLinkedFile"));
      }catch(ClassNotFoundException cnfe){
        logger.error("unable to registery classes for types" , cnfe);
      }
    }
  }
  
  public void setMultiThreadedUploadThreadPool(ThreadPoolTaskScheduler multiThreadedUploadThreadPool) {
    this.multiThreadedUploadThreadPool = multiThreadedUploadThreadPool;
    this.multiThreadedUploadThreadPool.setThreadFactory(new PrioritizedThreadFactory("Velo Upload Thread Pool", "medium"));
  }

  /**
   * Default init method for InitializingBean interface. Called after all properties have been injected into the bean by the Spring container.
   * 
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {


  }

  /**
   * Method setConfigurableMimeFileTypeMap.
   * @param fileTypeMap ConfigurableMimeFileTypeMap
   */
  public void setConfigurableMimeFileTypeMap(ConfigurableMimeFileTypeMap fileTypeMap) {
    mimeFileTypeMap = fileTypeMap;
  }

  /**
   * Method getConfigurableMimeFileTypeMap.
   * @return ConfigurableMimeFileTypeMap
   */
  static protected ConfigurableMimeFileTypeMap getConfigurableMimeFileTypeMap() {
    if (mimeFileTypeMap == null) {
      mimeFileTypeMap = new ConfigurableMimeFileTypeMap();
      System.err.println("WARNING: creating a new ConfigurableMimeFileTypeMap. This should have been set by Spring.");
    }

    return mimeFileTypeMap;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#isIdentical(gov.pnnl.cat.core.internal.resources.datamodel.Resource, org.eclipse.core.runtime.QualifiedName, java.lang.String)
   */
  @Override
  public boolean isIdentical(Resource resource, String property, String hash) {

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "isIdentical");

    if(resource.getUuid() != null) {
      WebServiceUrlUtility.appendParameter(url, "uuid", resource.getUuid());
    } else {
      WebServiceUrlUtility.appendParameter(url, "path", resource.getPath());    
    }

    WebServiceUrlUtility.appendParameter(url, "property", property);
    WebServiceUrlUtility.appendParameter(url, "hash", hash);

    boolean isIdentical = false;

    CloseableHttpResponse response = null;

    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      String isIdenticalStr = getResponseBodyAsString(response).trim();
      isIdentical = Boolean.valueOf(isIdenticalStr);

    } catch (Throwable e) {
        handleException("Failed to execute method.", e);
        
    }  finally {
      closeQuietly(response);
    }

    return isIdentical;   
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#setProperties(gov.pnnl.velo.model.Resource)
   */
  @Override
  public Resource setProperties(Resource resource) {
    ArrayList<Resource> resources = new ArrayList<Resource>();
    resources.add(resource);
    List<Resource> updatedResources = setProperties(resources);
    if(updatedResources != null && updatedResources.size() > 0) {
      return updatedResources.get(0);
    } else {
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#setProperties(java.util.List)
   */
  @Override
  public List<Resource> setProperties(List<Resource> resources) {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "createUpdateResources");
    List<Resource> updatedResources = null;

    CloseableHttpResponse response = null;

    try {
      HttpPost httppost = new HttpPost(url.toString());

      // serialize resource to json
      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, resources);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      updatedResources = mapper.readValue(response.getEntity().getContent(), new TypeReference<ArrayList<Resource>>() {});
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
    }

    return updatedResources;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getHash(java.io.File)
   */
  @Override
  public String getHash(File file) {
    String hash = null;
    InputStream input = null;
    try {
      // Generate the MD5 hash
      input = new BufferedInputStream(new FileInputStream(file));
      hash = createMd5Hash(input);

    } catch (Throwable e) {
      handleException("Failed to compute hash.", e);

    } finally {
      if(input != null) {
        try{input.close();} catch (Throwable e) {}
      }
    }
    return hash;
  }

  /**
   * Method createMd5Hash.
   * @param input InputStream
   * @return String
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  protected String createMd5Hash(final InputStream input) throws IOException, NoSuchAlgorithmException {
    final java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
    int bread = 0;
    byte[] buf = new byte[64 * 1024];

    do {
      bread = input.read(buf);
      if (bread > 0) {
        md.update(buf, 0, bread);
      }
    } while (bread > -1);
    input.close();
    buf = null;

    final byte[] digest = md.digest();

    final StringBuilder hexString = new StringBuilder();
    for (final byte element : digest) {
      hexString.append(Integer.toHexString(0xFF & element));
    }

    return hexString.toString();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getChildren(gov.pnnl.cat.core.resources.CmsPath)
   */
  @Override
  public List<Resource> getChildren(CmsPath folderPath) throws ResourceException, AccessDeniedException {

    List<Resource> children = new ArrayList<Resource>();
 // note this will work for any path no matter what type of association, as long as there are no duplicate child names
    String path = folderPath.toAssociationNamePath(); 

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getChildren");
    WebServiceUrlUtility.appendParameter(url, "path", path);

    CloseableHttpResponse response = null;

    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      children = mapper.readValue(response.getEntity().getContent(), new TypeReference<ArrayList<Resource>>() {});
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
    }

    return children;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getResource(gov.pnnl.velo.model.CmsPath)
   */
  @Override
  public Resource getResource(CmsPath path) throws ResourceException, ResourceNotFoundException, AccessDeniedException {
    //updateCallCount(path);
    List<CmsPath> paths = new ArrayList<CmsPath>();
    paths.add(path);
    List<Resource> resources = getResourcesByPath(paths);
    if(resources != null && resources.size() > 0) {
      return resources.get(0); 
    }
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getResourcesByPath(java.util.List)
   */
  @Override
  public List<Resource> getResourcesByPath(List<CmsPath>paths) throws ResourceException, ResourceNotFoundException, AccessDeniedException {
    List<Resource> resources = null;

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getResourcesByPath");

    StringBuilder content = new StringBuilder();
    for (int i = 0; i < paths.size(); i++) {
      if(i > 0) {
        content.append("\n");
      }

      String path = paths.get(i).toAssociationNamePath();      
      content.append(path);
    }

    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      StringEntity reqEntity = new StringEntity(content.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      resources = mapper.readValue(response.getEntity().getContent(), new TypeReference<ArrayList<Resource>>() {});
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return resources;
  }

  
  public HashMap<String, HashMap<String, Integer>> getFacetItems(String query, ArrayList<String> fieldsQNames) {
    StringBuilder url = getVeloWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "facetItems");
    WebServiceUrlUtility.appendParameter(url, "query", query);
    HashMap<String, HashMap<String, Integer>> facetItems = null;
    
    CloseableHttpResponse response = null;

    try {
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize list to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, fieldsQNames);
      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      
      response = executeMethod(httpClient, httppost);

      // parse results
      facetItems = mapper.readValue(response.getEntity().getContent(), HashMap.class);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
        handleException("Failed to execute method.", e);
        
    }  finally {
      closeQuietly(response);
    }

    return facetItems;
  } 
  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#search(java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
   */
  public ICatQueryResult search(String query, boolean includeThumbnails, String sortByProp, String order, 
      Integer maxItems, Integer pageNumber)  {
    return search(query, includeThumbnails, sortByProp,order, maxItems, pageNumber, false, null);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#search(java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, boolean, java.util.ArrayList)
   */
  public ICatQueryResult search(String query, boolean includeThumbnails, String sortByProp, String order, 
      Integer maxItems, Integer pageNumber, boolean includeJson, ArrayList<String> facetProperties) {
    //  <url>/cat/search?query={query}&amp;includeThumbnails={?includeThumbnails}&amp;sortByProp={?sortByProp}</url>
    ICatQueryResult results = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "search");
    QueryRequest queryRequest = new QueryRequest(query, includeThumbnails, sortByProp, order, maxItems, pageNumber, facetProperties);

    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      // serialize query request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, queryRequest);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      // parse results
      mapper = new ObjectMapper();
      String rawJson = EntityUtils.toString(response.getEntity());
      SearchResult searchResult =  mapper.readValue(rawJson, SearchResult.class);
      ArrayList<Resource> resources = searchResult.getResults();
      HashMap<String, HashMap<String, Integer>> facets = searchResult.getPropertyFacets();
      
      // TODO: converting to a CAT-specific object should be done outside resource service - get rid if ICatQueryResult object
      CatQueryResult result;
      if(includeJson) {
        result = new JsonCatQueryResult();
        ((JsonCatQueryResult)result).setRawJson(rawJson);
      } else {
        result = new CatQueryResult();
      }
      result.setResources(resources);
      result.setPropertyFacets(facets);
      result.setTotalHits(searchResult.getNumHits());
      results = result;
    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    return results;
  }  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getResource(java.lang.String)
   */
  @Override
  public Resource getResource(String uuid) throws ResourceException, ResourceNotFoundException, AccessDeniedException {
    //updateCallCount(uuid);
    List<String> uuids = new ArrayList<String>();
    uuids.add(uuid);
    List<Resource> resources = getResourcesByUuid(uuids);
    if(resources != null && resources.size() > 0) {
      return resources.get(0); 
    }
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getResourcesByUuid(java.util.List)
   */
  @Override
  public List<Resource> getResourcesByUuid(List<String>uuids) throws ResourceException, ResourceNotFoundException, AccessDeniedException {
    //updateCallCount(uuids);
    List<Resource> resources = null;

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getResourcesByUuid");

    StringBuilder content = new StringBuilder();
    for (int i = 0; i < uuids.size(); i++) {
      if(i > 0) {
        content.append("\n");
      }

      String uuid = uuids.get(i);      
      content.append(uuid);
    }
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      StringEntity reqEntity = new StringEntity(content.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      resources = mapper.readValue(response.getEntity().getContent(), new TypeReference<ArrayList<Resource>>() {});
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return resources;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#createFolder(gov.pnnl.cat.core.internal.resources.datamodel.Resource)
   */
  @Override
  public Resource createFolder(Resource folder) throws ResourceException {
    folder.setType(VeloConstants.TYPE_FOLDER);
    List<Resource> folders = new ArrayList<Resource>();
    folders.add(folder);
    List<Resource> createdFolders = createFolders(folders);
    return createdFolders.get(0);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#createFolders(java.util.List)
   */
  @Override
  public List<Resource> createFolders(List<Resource> foldersToCreate) {
    return createUpdateResources(foldersToCreate);
  }

  public List<Resource> createUpdateResources(List<? extends Resource> resourcesToCreate)  {

    List<Resource> resources = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "createUpdateResources");

    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, resourcesToCreate);
      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);

      // parse results
      resources = mapper.readValue(response.getEntity().getContent(), new TypeReference<ArrayList<Resource>>() {});
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return resources;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#updateContent(java.lang.String, java.io.File, org.eclipse.core.runtime.QualifiedName, java.lang.String)
   */
  @Override
  public Resource updateContent(String uuid, File file, String property, String mimetype) {
    Resource updatedResource = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "updateFileContents");
    WebServiceUrlUtility.appendParameter(url, "uuid", uuid);
    if(property != null) {
      WebServiceUrlUtility.appendParameter(url, "property", property);   
    }
    if(mimetype != null) {
      WebServiceUrlUtility.appendParameter(url, "mimetype", mimetype);
    }

    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      FileEntity reqEntity = new FileEntity(file, ContentType.create("binary/octet-stream"));
      reqEntity.setChunked(true);      
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      updatedResource = mapper.readValue(response.getEntity().getContent(), Resource.class); 
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    return updatedResource;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#updateContent(gov.pnnl.velo.model.CmsPath, java.io.InputStream, java.lang.String, java.lang.String)
   */
  @Override
  public Resource updateContent(CmsPath path, InputStream stream, String property, String mimetype, String offset) {
    Resource updatedResource = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "updateFileContents");
    WebServiceUrlUtility.appendParameter(url, "path", path.toAssociationNamePath());
    if(property != null) {
      WebServiceUrlUtility.appendParameter(url, "property", property);   
    }
    if(mimetype != null) {
      WebServiceUrlUtility.appendParameter(url, "mimetype", mimetype);
    }    
    if(offset != null) {
      WebServiceUrlUtility.appendParameter(url, "offset", offset);
    }

    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      InputStreamEntity nonRepeatableEntity = new InputStreamEntity(stream);
      //ZOE TODO - TEST IF MAPPED DRIVE STILL WORKS WHEN USING THE BUFFERED ENTITY
      
//      BufferedHttpEntity repeatableEntity = new BufferedHttpEntity(nonRepeatableEntity);
     
      httppost.setEntity(nonRepeatableEntity);
      response = executeMethod(httpClient, httppost);
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      updatedResource = mapper.readValue(response.getEntity().getContent(), Resource.class); 
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    return updatedResource;
  
  }
  
  @Override
  public Resource createEmptyFile(Resource resource) {

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "createEmptyFile");
    Resource newResource = null;
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, resource);
      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);

      // parse results
      newResource = mapper.readValue(response.getEntity().getContent(), Resource.class); 
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
    }
    
    return  newResource;
  }

  /**
   * Method getCatBulkUploadUrl.
   * @return StringBuilder
   */
  protected StringBuilder getCatBulkUploadUrl() {
    StringBuilder url = new StringBuilder(repositoryUrl);
    url.append("/catupload"); 
    return url;    
  }

  /**
   * Method getMimeType.
   * @param file File
   * @return String
   * @throws FileNotFoundException
   */
  static public String getMimeType(File file) throws FileNotFoundException {
    BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
    String mimeType = guessMimeType(bufInput);
    logger.debug("content-based mime type = " + mimeType);

    if (mimeType == null || mimeType.equals("application/octet-stream")) {

      // hopefully file extension-based guess will get a better answer
      // use the file name so we can convert it to lower case (we can't register
      // all the possible combinations of upper and lower case)
      String fileName = file.getName().toLowerCase();
      mimeType = getConfigurableMimeFileTypeMap().getContentType(fileName);
      logger.debug("extension-based mime type = " + mimeType);
    }

    // Alfresco hard codes some the mimetypes used in transformers - doh!
    // And the mimetypes for excel, ppt, & xml documents do not match what
    // getMimeType is coming up with!
    // TODO: we should be using Alfresco's MimetypeService to figure out the
    // right mimetype to use, and we need a table of synonyms
    if (mimeType.equals("application/vnd.ms-powerpoint")) {
      mimeType = "application/vnd.powerpoint";

    } else if (mimeType.equals("application/vnd.ms-excel")) {
      mimeType = "application/vnd.excel";

    } else if (mimeType.equals("application/xml")) {
      mimeType = "text/xml";
    }
    logger.debug("final mime type = " + mimeType);

    return mimeType;
  }

  /**
   * Method guessMimeType.
   * @param content BufferedInputStream
   * @return String
   */
  static public String guessMimeType(BufferedInputStream content) {
    try {
      return URLConnection.guessContentTypeFromStream(content);
    } catch (IOException e) {
      System.err.println("Unable to guess content type");
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getPermissions(gov.pnnl.velo.model.CmsPath)
   */
  public ACL getPermissions(CmsPath path) {
    String nodePath = path.toAssociationNamePath();
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getPermissions");
    WebServiceUrlUtility.appendParameter(url, "path", nodePath);
    ACL acl = null;
        
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      acl = mapper.readValue(response.getEntity().getContent(),  ACL.class);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
    return acl;
  }
  
  public boolean hasPermissions(CmsPath path, String... permissions) {
    boolean hasPermissions = false;
    
    // concatenate permissions
    StringBuilder permissionsString = new StringBuilder();
    for(int i = 0; i < permissions.length; i++) {
      if(i > 0) {
        permissionsString.append(",");
      }
      permissionsString.append(permissions[i]);
    }

    String nodePath = path.toAssociationNamePath();
    StringBuilder url = getCatWebScriptUrl();
    // <url>/cat/hasPermissions?path={path}&amp;permissions={comma separated list of permissions as defined in PermissionService}</url>
    WebServiceUrlUtility.appendPaths(url, "hasPermissions");
    WebServiceUrlUtility.appendParameter(url, "path", nodePath);
    WebServiceUrlUtility.appendParameter(url, "permissions", permissionsString.toString());

    CloseableHttpResponse response = null;

    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      String responseStr = getResponseBodyAsString(response).trim();
      hasPermissions = Boolean.valueOf(responseStr);

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
    return hasPermissions;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#setPermissions(gov.pnnl.cat.datamodel.ACL[])
   */
  public void setPermissions(ACL[] acls)  {
    setPermissions(acls, false);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#setPermissions(gov.pnnl.cat.datamodel.ACL[], boolean)
   */
  public void setPermissions(ACL[] acls, boolean recursive)  {

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "setPermissions");
    WebServiceUrlUtility.appendParameter(url, "recursive", String.valueOf(recursive));

    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      // serialize to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, acls);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#deleteResources(java.util.List)
   */
  @Override
  public void deleteResources(List<Resource> resourceList, String deleteOption) throws ResourceException {

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "delete");
    
    if(deleteOption != null) {
      WebServiceUrlUtility.appendParameter(url, "option", deleteOption);
    }
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize list to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, resourceList);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }  

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getComments(gov.pnnl.cat.core.internal.resources.datamodel.Resource)
   */
  @Override
  public Comment[] getComments(Resource resource) {
    Comment[] comments = null;

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getComments");
    WebServiceUrlUtility.appendParameter(url, "uuid", resource.getUuid());
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      comments = mapper.readValue(response.getEntity().getContent(), Comment[].class);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return comments;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#addComment(gov.pnnl.cat.core.internal.resources.datamodel.Resource, java.lang.String)
   */
  @Override
  public Comment addComment(Resource resource, String commentText) {

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "addComment");
    WebServiceUrlUtility.appendParameter(url, "uuid", resource.getUuid());
    Comment comment = null;
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
  
      StringEntity reqEntity = new StringEntity(commentText);
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      comment = mapper.readValue(response.getEntity().getContent(), Comment.class); 
      EntityUtils.consumeQuietly(response.getEntity());


    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    } 
    return comment;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#sendEmail(gov.pnnl.cat.datamodel.Email)
   */
  @Override
  public void sendEmail(Email email) {

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "sendmail");
     
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
  
      // serialize list to xml (could not get jackson to serialize a map properly if the map is contained within another class)
      XStream xstream = new XStream();
      String xml = xstream.toXML(email);

      StringEntity reqEntity = new StringEntity(xml);
      reqEntity.setChunked(true);
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#addAspect(gov.pnnl.velo.model.Resource, org.eclipse.core.runtime.QualifiedName)
   */
  @Override
  public Resource addAspect(Resource resource, String aspect) throws ResourceException {
    CmsPath path = new CmsPath(resource.getPath());
    ICatCML cml = new CatCML();
    cml.addAspect(path, aspect);
    executeCml(cml);
    resource.getAspects().add(aspect);
    return resource;
  }

  /**
   * Method removeAspect.
   * @param resource Resource
   * @param aspect QualifiedName
   * @return Resource
   * @throws ResourceException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#removeAspect(Resource, QualifiedName)
   */
  @Override
  public Resource removeAspect(Resource resource, String aspect) throws ResourceException {
    CmsPath path = new CmsPath(resource.getPath());
    ICatCML cml = new CatCML();
    cml.removeAspect(path, aspect);
    executeCml(cml);
    resource.getAspects().remove(aspect);
    return resource;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getContentPropertyAsFile(gov.pnnl.cat.core.internal.resources.datamodel.Resource, org.eclipse.core.runtime.QualifiedName, java.io.File, java.lang.String)
   */
  @Override
  public File getContentPropertyAsFile(Resource resource, String property, File destinationFile, String version) {

    //first test if resource is a link, and if so return the content of what its pointing to
    if(resource.getType()!=null && resource.getType().equalsIgnoreCase(VeloConstants.TYPE_LINKED_FILE)){
      String destinationNodeRefString = resource.getPropertyAsString(VeloConstants.PROP_LINK_DESTINATION);
      resource = this.getResource(AlfrescoUtils.parseUuidFromReferenceString(destinationNodeRefString));
    }

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getFileContents");
    if(resource.getUuid() != null) {
      WebServiceUrlUtility.appendParameter(url, "uuid", resource.getUuid());
    } else {
      WebServiceUrlUtility.appendParameter(url, "path", resource.getPath());    
    }

    WebServiceUrlUtility.appendParameter(url, "property", property);
    
    if(version != null) {
      WebServiceUrlUtility.appendParameter(url, "version", version);
    }
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      
      // parse results
      InputStream is = response.getEntity().getContent();

      // Then copy it to local file
      FileCopyUtils.copy(new BufferedInputStream(is), new BufferedOutputStream(new FileOutputStream(destinationFile)));     

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
    return destinationFile;
  }

  /**
   * Method organizeProperties.
   * @param properties NamedValue[]
   * @return Map<QualifiedName,List<String>>
   * @throws ResourceException
   */
  public static Map<String, List<String>> organizeProperties(NamedValue[] properties) throws ResourceException {
    Map<String, List<String>> results = new HashMap<String, List<String>>();

    for (NamedValue namedValue : properties) {
      // logger.debug("retrieved property: " + namedValue.getName() + "=" + namedValue.getValue());
      String name = namedValue.getName();

      if (name != null) {
        List<String> values = new ArrayList<String>();
        if(namedValue.getValue() != null) {
          values.add(namedValue.getValue());

        } else if(namedValue.getValues() != null) {
          for(String value : namedValue.getValues()) {
            values.add(value);
          }
        }

        // populate the map
        results.put(name, values);
      }
    }

    return results;
  }

  @Override
  public Resource move(CmsPath oldPath, CmsPath newPath) throws ResourceException, AccessDeniedException {
    ICatCML cml = new CatCML();
    cml.move(oldPath, newPath);
    List<Resource> modifiedResources = executeCml(cml);
    Resource movedResource = null;
    // There will be two results, one for new node and one for deleted node in the move
    for(Resource resource : modifiedResources) {
      String cmlCommand = resource.getPropertyAsString(VeloConstants.PROP_CML_COMMAND);
      if(cmlCommand != null && cmlCommand.equals(VeloConstants.CML_CREATE)) {
        movedResource = resource;
        break;
      }
    }
    return movedResource;
  }


  @Override
  public Resource copy(CmsPath source, CmsPath destination, boolean overwrite) throws ResourceException, AccessDeniedException {
    ICatCML cml = new CatCML();
    cml.copy(source, destination, overwrite);
    List<Resource> modifiedResources = executeCml(cml);
    return modifiedResources.get(0); // there will be only one result
  }
  

  /**
   * Get path for uuid, even if you don't have permissions to read that node.
   * @param uuid
   * @return
   * @throws ResourceException
   */
  public CmsPath getPath(String uuid) throws ResourceException {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getPath");
    WebServiceUrlUtility.appendParameter(url, "uuid", uuid);
    boolean exists = false;
    CmsPath path = null;
    
    CloseableHttpResponse response = null;

    try {     
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      String pathString = getResponseBodyAsString(response).trim();
      if(pathString != null && !pathString.isEmpty()) {
        path = new CmsPath(pathString);
      }

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return path;
  }

  /**
   * Method resourceExists.
   * @param path CmsPath
   * @return boolean
   * @throws ResourceException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#resourceExists(CmsPath)
   */
  public boolean resourceExists(CmsPath path) throws ResourceException {
    String namePath = path.toAssociationNamePath();
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "exists");
    WebServiceUrlUtility.appendParameter(url, "path", namePath);
    boolean exists = false;
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      String existsStr = getResponseBodyAsString(response).trim();
      exists = Boolean.valueOf(existsStr);

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return exists;
  }

  /**
   * Method getThumbnail.  Gets the thumbnail image for this resource as
   * a byte array
   * @param path CmsPath
   * @return byte[]
   * @throws ResourceException
   */
  public byte[] getThumbnail(CmsPath path, String thumbnailName) {
    
    String namePath = path.toAssociationNamePath();
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getThumbnail");
    WebServiceUrlUtility.appendParameter(url, "path", namePath);
    if(thumbnailName != null) {
      WebServiceUrlUtility.appendParameter(url, "thumbnailName", thumbnailName);
      
    }
    byte[] thumbnail = null;
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      String responseBody = getResponseBodyAsString(response).trim();
      thumbnail = new Base64Encoder().decode(responseBody);

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
    return thumbnail;
  }

  /**
   * Method getLinkedResources.
   * @param resource Resource
   * @return Collection<Resource>
   * @throws ResourceException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getLinkedResources(Resource)
   */
  @Override
  public ICatQueryResult getLinkedResources(Resource resource) throws ResourceException {
    ISearchManager searchMgr = ResourcesPlugin.getSearchManager();

    String referenceString = AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, resource.getUuid());
    SearchContext searchContext = new SearchContext();
    searchContext.addAttributeQuery(VeloConstants.PROP_LINK_DESTINATION, referenceString);
    searchContext.setFolderType(VeloConstants.TYPE_LINKED_FILE);
    String queryStr = searchContext.buildQuery(2);

    // TODO: just call search web service directly and don't go through search manager to convert to cat-specific object
    // use SearchResult instead
    ICatQueryResult queryResults = searchMgr.query(queryStr);
    return queryResults;
  }

  /**
   * @param path
   * @param primaryNodeType
   * @param aspects
   * @param properties
   * @return
   * @throws ResourceException
   */
//  public static Resource createResource(CmsPath path, String primaryNodeType, List<String> aspects, Map<String, List<String>> properties) throws ResourceException {
//
//    if (path == null || primaryNodeType == null) {
//      throw new NullPointerException();
//    }
//
//    String uuid = properties.get(VeloConstants.PROP_UUID).get(0);
//    Resource resource = new Resource(uuid);
//    resource.setAspects(aspects);
//    resource.setProperties(properties);
//    resource.setPath(path.toAssociationNamePath());
//    resource.setType(primaryNodeType);
//    if(properties.get(VeloConstants.PROP_CHILD_COUNT) != null){
//      int childCount = Integer.valueOf(properties.get(VeloConstants.PROP_CHILD_COUNT).get(0).trim());
//      resource.setNumChildren(childCount);
//    }else{
//      logger.error("no CHILD_COUNT property returned for resource " + path);
//    }
//
//    return resource;
//  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#createLinks(java.util.List)
   */
  @Override
  public List<Resource> createLinks(List<Resource> linksToCreate) {
    return createUpdateResources(linksToCreate);
  }

  
  /**
   * Method createRemoteLinks.
   * @param remoteLinks List<RemoteLink>
   */
  @Override
  public List<Resource> createRemoteLinks(List<RemoteLink> remoteLinks) {
    // TODO jackson is having a problem if the class is RemoteLink because it's trying to set a field called linkUrl even if there is
    // no such field.  Maybe switching to gson will fix this problem, but until then, we have to convert the remote link object
    
    List<Resource> resources = new ArrayList<Resource>();
    for(RemoteLink remoteLink : remoteLinks) {
      Resource resource = new Resource(remoteLink);
      resources.add(resource);
    }
    return createUpdateResources(resources);  
  }

  /**
   * Method updateLinkTarget.
   * @param link Resource
   * @param destination Resource
   * @return Resource
   * @throws ResourceException
   * @throws AccessDeniedException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#updateLinkTarget(Resource, Resource)
   */
  @Override
  public Resource updateLinkTarget(Resource link, Resource destination) {
     link.setProperty(VeloConstants.PROP_LINK_DESTINATION, destination.getUuid());
     List<Resource> resources = new ArrayList<Resource>();
     resources.add(link);
     return createUpdateResources(resources).get(0);
  }

  /**
   * Method getResourcesByAspect.
   * @param aspect String
   * @return Resource[]
   * @throws ResourceException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getResourcesByAspect(String)
   */
  @Override
  public List<Resource> getResourcesByAspect(String aspect) throws ResourceException {
    // Execute the query
    List<Resource> queryResult;
    try {
      String query = " +(ASPECT:\"" + aspect + "\")";
      // TODO: this method needs to be rewritten to call search() directly
      queryResult = ((SearchManager)ResourcesPlugin.getSearchManager()).query(query).getResources();
      
    } catch (Exception e) {
      throw new ResourceException(e);
    }

    return queryResult;
  }

  /**
   * Method createResources.
   * @param nodes Node[]
   * @return Resource[]
   * @throws ResourceException
   */
//  public static Resource[] createResources(Node[] nodes) throws ResourceException {
//
//    if (nodes == null) {
//      return new Resource[0];
//    }
//
//    List<Resource> resources = new ArrayList<Resource>(nodes.length);
//
//    for (int i = 0; i < nodes.length; i++) {
//      Node node = nodes[i];
//      CmsPath path = new CmsPath(node.getReference().getPath());
//
//      String primaryNodeType = node.getType();
//      NamedValue[] properties = node.getProperties();
//      Map<String, List<String>> props = organizeProperties(properties);
//      List<String> aspects = convertAspects(node.getAspects());
//
//      Resource resource = createResource(path, primaryNodeType, aspects, props);
//
//      if (resource != null) {
//        resources.add(resource);
//      }
//    }
//
//    return (Resource[]) resources.toArray(new Resource[resources.size()]);
//  }
  
  /**
   * Method convertAspects.
   * @param aspects String[]
   * @return List<QualifiedName>
   */
  public static List<String> convertAspects(String[] aspects) {
    List<String> aspectsQNames = new ArrayList<String>();

    for (String aspect : aspects) {
      aspectsQNames.add(aspect);
    }
    return aspectsQNames;
  }

  /**
   * Construct the http url for any node on the server
   * 
   * @param uuid
   * @param fileName
   * @param contentProperty
   * @param attachmentMode

   * @return String
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getHttpUrl(String, String, QualifiedName, String)
   */
  @Override
  public String getHttpUrl(String uuid, String fileName, String contentProperty, String attachmentMode) {

    StringBuffer buf = new StringBuffer(this.repositoryUrl);
    buf.append("/download/");
    buf.append(attachmentMode);
    buf.append("/workspace/SpacesStore/");
    buf.append(uuid);
    buf.append("/");
    buf.append(fileName);
    //    buf.append("?ticket=");
    //    buf.append(AlfrescoWebServiceFactory.getCurrentTicket());
    buf.append("?property=");
    buf.append(contentProperty);

    return buf.toString();
  }

  /**
   * Method getRepositoryUrlBase.
   * @return String
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getRepositoryUrlBase()
   */
  public String getRepositoryUrlBase() {
    return repositoryUrl;
  }

  /**
   * Get the webdav url for any resource on the server
   * 
   * @param path

   * @return String
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getWebdavUrl(CmsPath)
   */
  public String getWebdavUrl(CmsPath path) {
    StringBuffer buf = new StringBuffer(this.repositoryUrl);
    buf.append("/webdav");
    buf.append(path.toDisplayString());
    return buf.toString();
  }

  /**
   * Get a session ticket from alfresco!
   * 
   * @param name String
   * @param pwd String
   * @return boolean
   * @throws CatSecurityException
   * @throws ServerException
   */
  public boolean login(String name, String pwd) {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "startSession");

    CloseableHttpResponse response = null;
    
    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget, name, pwd);
      
      // TODO: save the ticket for reuse
      //String ticket = response.getFirstHeader("ALFRESCO_TICKET").getValue();
      
      EntityUtils.consumeQuietly(response.getEntity());
 
    } catch (Throwable e) {
      // will throw an exception if user has access denied
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
    }

    return true;
  }

  @Override
  public List<Resource> executeCml(ICatCML catCml) {
    List<Resource> modifiedResources = null;
    
    Vector<CMLSet> vecCMLSets = ((CatCML)catCml).cmlManager.getCMLSets();
    
    for (CMLSet currentSet : vecCMLSets) {
      CML cml = new CML();
      // add the operations to the CML that we have been creating
      cml.setCreate(currentSet.getCreates().toArray(new CMLCreate[currentSet.getCreates().size()]));
      cml.setAddAspect(currentSet.getAddAspects().toArray(new CMLAddAspect[currentSet.getAddAspects().size()]));
      cml.setRemoveAspect(currentSet.getRemoveAspects().toArray(new CMLRemoveAspect[currentSet.getRemoveAspects().size()]));
      cml.setUpdate(currentSet.getUpdates().toArray(new CMLUpdate[currentSet.getUpdates().size()]));
      cml.setCopy(currentSet.getCopies().toArray(new CMLCopy[currentSet.getCopies().size()]));
      cml.setMove(currentSet.getMoves().toArray(new CMLMove[currentSet.getMoves().size()]));
      cml.setDelete(currentSet.getDeletes().toArray(new CMLDelete[currentSet.getDeletes().size()]));
      cml.setWriteContent(currentSet.getWriteContents().toArray(new CMLWriteContent[currentSet.getWriteContents().size()]));


      logger.debug("Executing CML with " + currentSet.getSetSize() + " statements");
      long start = System.currentTimeMillis();
      modifiedResources = executeCml(cml);
      long end = System.currentTimeMillis();
      logger.debug("Time to execute cml = " + (end - start));
      
    }
    return modifiedResources;
  }
    
  private List<Resource> executeCml(CML cml) {
    List<Resource> modifiedResources = new ArrayList<Resource>();
   
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "executeCml");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize cml to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, cml);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);

      // parse results
      modifiedResources = mapper.readValue(response.getEntity().getContent(), new TypeReference<ArrayList<Resource>>() {});
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return modifiedResources;
  }

  public void changePassword(String username, String currentPassword, String newPassword) {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "changePassword");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      ChangePasswordRequest request = new ChangePasswordRequest();
      request.setUserName(username);
      request.setOldPassword(currentPassword);
      request.setNewPassword(newPassword);

      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, request);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    } 
  }

  public void deleteUsers(String[] usernames) {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "deleteUsers");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, usernames);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
  }

  public void deleteGroups(String[] groupnames) {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "deleteGroups");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, groupnames);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }

  public void executeActions(Predicate predicate, Action[] actions) {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "executeActions");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ExecuteActionsRequest request = new ExecuteActionsRequest();
      request.setPredicate(predicate);
      request.setWebServiceActions(actions);
      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, request);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }  

  }

  /**
   * Method deleteSubscriptions
   * @param subscription
   */
  public void deleteSubscriptions(Subscription[] subscriptions){
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "deleteSubscriptions");
    
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, subscriptions);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    } 
  }

  /**
   * Method getSubscriptions
   */
  public Subscription[] getSubscriptions(){
    Subscription[] subs = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getSubscriptions");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      subs = mapper.readValue(response.getEntity().getContent(), Subscription[].class);

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    return subs;
  }

  @Override
  public void deleteAlerts(RepositoryAlert[] alerts){
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "deleteAlerts");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, alerts);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(httppost.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }

  @Override
  public void markAlertsAsRead(RepositoryAlert[] alerts){
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "markAlertsAsRead");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, alerts);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }

  @Override
  public RepositoryAlert[] getAlerts(){
    RepositoryAlert[] alerts = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getAlerts");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      alerts = mapper.readValue(response.getEntity().getContent(), RepositoryAlert[].class);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return alerts;
  }


  @Override
  public void createSubscription(Subscription subscription){
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "createSubscription");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, subscription);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }

  public UserDetails[] createUsers(NewUserDetails[] newUsers) {
    UserDetails[] createdUsers = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "createUsers");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, newUsers);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      createdUsers = mapper.readValue(response.getEntity().getContent(), UserDetails[].class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
    return createdUsers;
  }

  public GroupDetails[] createGroups(NewGroupDetails[] newGroups) {
    GroupDetails[] createdGroups = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "createGroups");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, newGroups);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      createdGroups = mapper.readValue(response.getEntity().getContent(), GroupDetails[].class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
    return createdGroups;

  }

  public UserDetails[] updateUsers(UserDetails[] users) {
    UserDetails[] updatedUsers = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "updateUsers");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, users);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      updatedUsers = mapper.readValue(response.getEntity().getContent(), UserDetails[].class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
 
    return updatedUsers;
  }

  public GroupDetails[] updateGroups(GroupDetails[] groups) {
    GroupDetails[] updatedGroups = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "updateGroups");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, groups);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      updatedGroups = mapper.readValue(response.getEntity().getContent(), GroupDetails[].class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return updatedGroups;
  }

  public UserQueryResults queryUsers(UserFilter filter) {
    UserQueryResults users = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "queryUsers");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      if(filter != null) {
        mapper.writeValue(writer, filter);
      }

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      users = mapper.readValue(response.getEntity().getContent(), UserQueryResults.class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return users;
  }

  public GroupQueryResults queryGroups(GroupFilter filter) {
    GroupQueryResults groups = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "queryGroups");

    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      if(filter != null) {
        mapper.writeValue(writer, filter);
      }

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      
      // parse results
      groups = mapper.readValue(response.getEntity().getContent(), GroupQueryResults.class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return groups;
  }

  public UserQueryResults fetchMoreUsers(String querySessionId) {
    UserQueryResults users = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "fetchMoreUsers");
    WebServiceUrlUtility.appendParameter(url, "querySessionId", querySessionId);
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      response = executeMethod(httpClient, httppost);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      users = mapper.readValue(response.getEntity().getContent(), UserQueryResults.class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return users;
  }

  public GroupQueryResults fetchMoreGroups(String querySessionId) {
    GroupQueryResults groups = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "fetchMoreGroups");
    WebServiceUrlUtility.appendParameter(url, "querySessionId", querySessionId);
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      response = executeMethod(httpClient, httppost);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      groups = mapper.readValue(response.getEntity().getContent(), GroupQueryResults.class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return groups;
  }

  public UserDetails getUser(String username) throws ResourceException, AccessDeniedException {
    UserDetails user = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getUser");
    WebServiceUrlUtility.appendParameter(url, "username", username);
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      user = mapper.readValue(response.getEntity().getContent(),  UserDetails.class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return user;
  }

  public GroupDetails getGroup(String groupname) throws ResourceException, AccessDeniedException {
    GroupDetails group = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getGroup");
    WebServiceUrlUtility.appendParameter(url, "groupname", groupname);
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      group = mapper.readValue(response.getEntity().getContent(),  GroupDetails.class);     
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
    
    return group;
  }

  /**
   * Method importUrlList.
   * @param destination CmsPath
   * @param urls List<String>
   * @param monitor IProgressMonitor
   * @throws ResourceException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#importUrlList(CmsPath, List<String>, IProgressMonitor)
   */
  public void importUrlList(CmsPath destination, List<String> urls, IProgressMonitor monitor) throws ResourceException {
    throw new RuntimeException("Method no longer implemented.  This method needs to be moved to the harvester plugin and needs"
       + " to be converted to REST");
//    monitor = resolveMonitor(monitor);
//
//    try {
//      monitor.beginTask("Importing Files", 3);
//      monitor.subTask("Serializing URL list");
//      String xml = XmlUtility.serialize(urls);
//
//      monitor.worked(1);
//      monitor.subTask("Creating data objects");
//
//      // Create the action to run the import
//      Reference reference = AlfrescoUtils.getReference(destination);
//      /*
//       * NamedValue urlListParam = Utils.createNamedValue("url-list-as-xml", xml);
//       * 
//       * // action web service crashes if you don't have a parameter, even // if your action takes no parameters - doh NamedValue[] parameters = new NamedValue[]{urlListParam}; Action importUrlListAction = new Action();
//       * 
//       * importUrlListAction.setActionName("import-urllist"); importUrlListAction.setTitle("Import URL List"); importUrlListAction.setDescription("This will import a list of URLs sent from IN-SPIRE."); importUrlListAction.setParameters(parameters);
//       */
//      monitor.worked(1);
//      monitor.subTask("Sending request to server");
//
//      // Execute the action
//      AlfrescoWebServiceFactory.getImportService().urlImportAction(xml, reference);
//
//      monitor.worked(1);
//      monitor.subTask("Done!");
//    } catch (Exception e) {
//      throw new ResourceException(e);
//    }
  }

  /**
   * Method resolveMonitor.
   * @param monitor IProgressMonitor
   * @return IProgressMonitor
   */
  private IProgressMonitor resolveMonitor(IProgressMonitor monitor) {
    if (monitor == null) {
      return new NullProgressMonitor();
    }
    return monitor;
  }

  /**
   * Method getRelationships.
   * @param path CmsPath
   * @return List<Relationship>
   * @throws ResourceException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#getRelationships(CmsPath)
   */
  @Override
  public List<Relationship> getRelationships(CmsPath path) throws ResourceException {
    List<Relationship> relationships = null;
    StringBuilder url = getCatWebScriptUrl();

    WebServiceUrlUtility.appendPaths(url, "getRelationships");
    WebServiceUrlUtility.appendParameter(url, "path", path.toAssociationNamePath());    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      
      // parse results
      ObjectMapper mapper = new ObjectMapper();
      relationships = 
          mapper.readValue(response.getEntity().getContent(), new TypeReference<ArrayList<Relationship>>() {});
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

    return relationships;
  }  
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#createRelationships(java.util.List)
   */
  @Override
  public void createRelationships(List<Relationship> relationships) {
    
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "createRelationships");

    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, relationships);
      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#deleteRelationships(java.util.List)
   */
  @Override
  public void deleteRelationships(List<Relationship> relationships) {
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "deleteRelationships");

    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());

      ObjectMapper mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, relationships);
      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    } 
  }

  public void bulkDownload(Map<CmsPath, File> filesToDownload) {

    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getFiles");
    StringBuilder content = new StringBuilder();
    int i = 0;
    for(CmsPath filePath : filesToDownload.keySet()) {
      if(i > 0) {
        content.append("\n");
      }
      content.append(filePath.toAssociationNamePath());  
      i++;
    }
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      StringEntity reqEntity = new StringEntity(content.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost);      
      
      // now we have to read the response stream to get the files
      WritableByteChannel fch = null;
      try {
        processDownloadedFiles(response.getEntity().getContent(), fch, filesToDownload);

      } finally {
        if (fch != null) {
          try {
            fch.close();
          } catch (IOException e) {
          }
        }
      }

    } catch (Throwable e) {
      handleException("Error occurred downloading files.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }  

  /**
   * Method processDownloadedFiles.
   * @param inputStream InputStream
   * @param fch WritableByteChannel
   * @param destFolder File
   * @throws Exception
   */
  @SuppressWarnings("unused")
  private void processDownloadedFiles(InputStream inputStream, WritableByteChannel fch, Map<CmsPath, File> destFiles) throws Exception {
    // Create a buffered input stream to start reading from
    BufferedInputStream gzIn = new BufferedInputStream(inputStream, 1048576);
    long total = 0;
    int readLen = -1;

    // Buffer for storing read content
    byte buffer[] = new byte[8192];

    long bytesLeft = 0;
    long dataSize = -1;
    StringBuilder content = new StringBuilder();
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(32768);
    String leftover = null;
    File curFile = null;

    // ContentData properties
    String repositoryPath = null;
    String contentUrl = null;
    long fileSize = 0;

    //ContentWriter writer = null;

    // While we can read in data, read it into the buffer
    while ((readLen = gzIn.read(buffer)) != -1) {
      total += readLen;
      int pos = 0;

      // While the current position in the read that we are at
      // is less than what was read the last time
      while (pos < readLen) {
        if (bytesLeft == 0) { // Read all we can
          // We read all that we are supposed to, so we need to find another
          // break in the stream.
          int end = findBreak(buffer, pos, readLen);
          if (end == -1) {
            // If the break was not found see if there is a ! to mark the
            // end of the upload.
            if (buffer[pos] == (int) '!') {
              return;
            } else {
              // No break was found so store the leftover bytes for
              // use on the next read.
              leftover = new String(buffer, pos, readLen - pos);
              pos += readLen - pos;
            }
          } else { // Found a break
            if (leftover != null) {
              // Leftover from last read, so use that with the new read to
              // find the data length size.
              dataSize = Long.parseLong(leftover + new String(buffer, pos, end - pos));

              leftover = null;
            } else {
              // No leftover, so just parse what is in the buffer to get the
              // data size.
              dataSize = Long.parseLong(new String(buffer, pos, end - pos));
            }
            if (dataSize == -1) {
              // No data size so return.
              return;
            }
            if (dataSize == 0) {
              // file has a 0 byte size. move onto the next file
              fch.close();
              fch = null;
              //writer = null;
              curFile = null;

            }
            // Set the bytes left to read to the data size we just found.
            bytesLeft = dataSize;
            // Set the position in the buffer to the end of what we just read
            // plus 1
            pos = end + 1;

            // If fch is set then we must be reading the content of the file next so
            // we can set the size.
            if (fch != null) {
              fileSize = dataSize;
            }
          }
        } else { // Still have bytes to read.
          int size = -1;
          // if bytes left is less than what we have left in the current
          // buffer that we read, use the bytes left for the read size,
          // else use what is left in the buffer for the size
          if (bytesLeft < (readLen - pos)) {
            //if we get here, then we know size must be an int because its got to be less than buffer's size
            size = (int)bytesLeft;
          } else {
            // size equals what we read in the buffer minus our current
            // position in the buffer.
            size = readLen - pos;
          }
          // if the curFile is null, that means we are reading the name of
          // the file and will store that in the content stringbuffer.
          //if (writer == null) {
          if(curFile == null) {
            content.append(new String(buffer, pos, size));
          } else { // reading file content
            // if the the remaining space in the byteBuffer is less than
            // the size we are working on, we need to write out what we
            // have and clear it before putting the next amount in.
            if (byteBuffer.remaining() < size) {
              byteBuffer.flip();
              fch.write(byteBuffer);
              byteBuffer.clear();
            }
            byteBuffer.put(buffer, pos, size);
          }

          // modify bytes left by reducing it by the amount we
          // just read
          bytesLeft -= size;

          // if we finished reading the file, we need to do some
          // cleanup
          if (bytesLeft == 0) {
            // if curFile is null, that means we just finished
            // reading the file name information
            //if (writer == null) {
            if(curFile == null) {
              repositoryPath = content.toString();

              // create a new output file based upon the name
              // we just read.
              CmsPath path = new CmsPath(repositoryPath);
              curFile = destFiles.get(path);
              //curFile = new File(deskFolder, path.getName());;

              // clean the buffer that stored the file name
              content.delete(0, content.length());

              // if the file already exist, delete it, else
              // create it.
              if (curFile.exists() == false) {
                //createFolder(curFile.getParentFile());
                curFile.createNewFile();
              }

              // Create a RandomAccessFile to for our file content
              // Using RAF since it can provide better performance since
              // you can set the size.
              FileOutputStream fout = new FileOutputStream(curFile);
              fch = fout.getChannel();
              //fch = writer.getWritableChannel();//fout.getChannel();

            } else if (fch != null) { // Just finished reading the content for a file
              // Empty the last byte into the file and clear the buffer
              byteBuffer.flip();
              fch.write(byteBuffer);
              byteBuffer.clear();

              //this triggers content listeners that the writing is done, 
              //which is needed for the replicating content store
              // Close the channel and file
              fch.close();

              // Set values back to null
              fch = null;
              //writer = null;
              curFile = null;
            }
          }
          // Update our position in the buffer with what
          // we just processed
          pos += size;
        }

      }

    }
  }
  /**
   * Method findBreak.
   * @param buf byte[]
   * @param start int
   * @param end int
   * @return int
   */
  private int findBreak(byte[] buf, int start, int end) {
    for (int i = start; i < end; i++) {
      if (buf[i] == (int) ';') {
        return i;
      }
    }
    return -1;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#bulkUpload(java.util.Map, java.util.Map, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void bulkUpload(Map<File, CmsPath> filesToServerPath, Properties globalMetadata,
      Map<String, Properties> fileSpecificMetadata, IProgressMonitor monitor) {
    long start = System.currentTimeMillis();

    // convert the properties to files    
    File globalMetadataFile = getMetadataFile(globalMetadata, "globalMetadata");
    Map<String, File> fileSpecificMetadataFiles = null;
    if(fileSpecificMetadata != null) {

      fileSpecificMetadataFiles = new HashMap<String, File>();
      for(String regex : fileSpecificMetadata.keySet()) {
        Properties properties = fileSpecificMetadata.get(regex);
        File metadataFile = getMetadataFile(properties, "fileMetadata");
        fileSpecificMetadataFiles.put(regex, metadataFile);
      }
    }
    multithreadedUpload(filesToServerPath, globalMetadataFile, fileSpecificMetadataFiles, monitor); 
    //streamingUploadServlet(filesToServerPath, monitor);

    
    long end = System.currentTimeMillis();
    logger.debug("Time for bulk upload: " + (end - start) + " ms");

  }

  private File getMetadataFile(Properties properties, String prefix) {
    File file = null;
    try {
      if(properties != null) {
        file = File.createTempFile(prefix, ".txt");
        file.deleteOnExit();
        Writer writer = new FileWriter(file);
        
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.enableComplexMapKeySerialization().create();
        java.lang.reflect.Type mapType =  new com.google.gson.reflect.TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();
        gson.toJson(properties.getProperties(), mapType, writer);

        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, properties.getProperties());
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    return file;

  }    
  
  /**
   * Method streamingUploadServlet.
   * @param fileUploads Map<File,CmsPath>
   * @param monitor IProgressMonitor
   * @throws ResourceException
   * @see gov.pnnl.cat.core.internal.resources.IResourceService#bulkFileUpload(Map<File,CmsPath>, IProgressMonitor)
   */
  private void streamingUploadServlet(Map<File, CmsPath> fileUploads, IProgressMonitor monitor) throws ResourceException {

    StringBuilder url = getCatBulkUploadUrl();
    url.append("?enablePipeline=true&enableNotififcations=true");
    
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      PipedOutputStream pout = new PipedOutputStream();
      
      InputStreamEntity reqEntity = new InputStreamEntity(new PipedInputStream(pout));
      reqEntity.setChunked(true);
      httppost.setEntity(reqEntity);
      
      if(monitor != null) {
        monitor.subTask("Uploading files.");
      }
      PipeThread pipeThread = new PipeThread(pout, fileUploads, monitor);
      pipeThread.start();

      response = executeMethod(httpClient, httppost);      
      EntityUtils.consumeQuietly(response.getEntity());
      if(monitor != null) {
        monitor.worked(1);
      }

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }

  }
  
  /**
   * @param fileUploads
   * @param monitor
   * @param useMultipartForm
   * @throws ResourceException
   */
  private void multithreadedUpload(Map<File, CmsPath> fileUploads, File globalMetadataFile,
      Map<String, File> fileSpecificMetadataFiles, IProgressMonitor monitor) throws ResourceException {
    monitor = resolveMonitor(monitor); // create null monitor so we don't have to worry about NPE
    int workedByteBatchSize = 100000;
    int totalWorked = 0;

    // If we are using multipart form, we can only intercept at byte transfer, so we must mark progress by the byte
    long bytes = 0;
    for(File file : fileUploads.keySet()) {
      bytes += file.length();
    }
    // if we are saving an empty file, then we only have one unit of work
    if(bytes == 0) {
      totalWorked = 1;
      workedByteBatchSize = 1;
    } else {
      totalWorked = (int)(bytes/(long)workedByteBatchSize);
      while(totalWorked == 0) {
        workedByteBatchSize = workedByteBatchSize/10;
        totalWorked = (int)(bytes/(long)workedByteBatchSize);
      }
    }
    // since eclipse won't let us report large work in long values, we have to convert bytes larger batches
    monitor.beginTask("Uploading " + fileUploads.size() + " files...", totalWorked);

    //System.out.println(Integer.MAX_VALUE); 
    //System.out.println("total work = " + totalWorked);

    int totalFiles = fileUploads.size();
    // alfresco doesn't do well with large multithreaded uploads because the tx take longer to commit
    // TODO: allow the batch size to be a parameter passed to bulkUpload
    int batchSize = 200;
    int numBatches = fileUploads.size()/batchSize;
    if(fileUploads.size() % batchSize != 0) {
      numBatches++;
    }
    //System.out.println("num batches = " + numBatches);
    
    List<Map<File, CmsPath>> fileBatches = new ArrayList<Map<File,CmsPath>>();
    Map<File, CmsPath> currentBatch = new HashMap<File, CmsPath>();
    fileBatches.add(currentBatch);
    int fileNum = 0;
    int batchNum = 0;
    for(File file : fileUploads.keySet()) {
      if(fileNum >= batchSize && batchNum < numBatches-1) {
        // time to move to next batch
        fileNum = 0;
        batchNum++;
        currentBatch = new HashMap<File, CmsPath>();
        fileBatches.add(currentBatch);
      }
      currentBatch.put(file, fileUploads.get(file));
      fileNum++;
    }
    
    //List<ScheduledFuture> futures = new ArrayList<ScheduledFuture>();
    
    for(int i = 0; i < numBatches; i++) {
      Runnable runnable = new MultipartFormUploadRunnable(fileBatches.get(i), globalMetadataFile, fileSpecificMetadataFiles, monitor, totalFiles, workedByteBatchSize);
      //ScheduledFuture future = multiThreadedUploadThreadPool.schedule(runnable, new Date());
      //futures.add(future);
      if(monitor.isCanceled()) {
        return;
      }
      runnable.run();
    }

//    boolean done = false;
//    while(!done) {
//      boolean cancelled = monitor.isCanceled();
//      for(ScheduledFuture future : futures) {
//        done &= future.isDone();
//        if(cancelled) {
//          future.cancel(true);
//        }
//      }
//    }
    monitor.done();    
  }
  
  private void cancelFutures(List<ScheduledFuture> futures) {
    
  }
  
  private class MultipartFormUploadRunnable implements Runnable {
    private Map<File, CmsPath> fileUploads;
    private IProgressMonitor monitor;
    private int totalFiles;
    private int workedByteBatchSize;
    private File globalMetadataFile;
    private Map<String, File> fileSpecificMetadataFiles;

    
    public MultipartFormUploadRunnable(Map<File, CmsPath> fileUploads, File globalMetadataFile,
        Map<String, File> fileSpecificMetadataFiles, IProgressMonitor monitor, int totalFiles, int workedByteBatchSize) {
      super();
      this.fileUploads = fileUploads;
      this.monitor = monitor;
      this.totalFiles = totalFiles;
      this.workedByteBatchSize = workedByteBatchSize;
      this.globalMetadataFile = globalMetadataFile;
      this.fileSpecificMetadataFiles = fileSpecificMetadataFiles;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      StringBuilder url = getCatWebScriptUrl();
      WebServiceUrlUtility.appendPaths(url, "upload");
      WebServiceUrlUtility.appendParameter(url, "enableNotififcations", "true");
      WebServiceUrlUtility.appendParameter(url, "enablePipeline", "true");
      WebServiceUrlUtility.appendParameter(url, "batchSize", "20");
      
      CloseableHttpResponse response = null;

      try {   

        MultipartEntityBuilder reqBuilder = MultipartEntityBuilder.create();
        for(File fileToUpload : fileUploads.keySet()) {
          // File form field has path for key
          // where path = path on server where the file will be located
          CmsPath filePath = fileUploads.get(fileToUpload);
          FileBody file = new FileBody(fileToUpload);          
          reqBuilder = reqBuilder.addPart(filePath.toAssociationNamePath(), file);
        }

        // Then create global metadata file form field
        if(globalMetadataFile != null) {
          FileBody globalMetadata = new FileBody(globalMetadataFile);
          reqBuilder = reqBuilder.addPart("globalMetadataFile", globalMetadata);
        }

        // then create file-specific metadata file
        if(fileSpecificMetadataFiles != null) {
          for(String regex : fileSpecificMetadataFiles.keySet()) {
            File metadataFile = fileSpecificMetadataFiles.get(regex);
            FileBody fileBody = new FileBody(metadataFile);

            String fileKey = "metadataFile_" + regex;
            reqBuilder = reqBuilder.addPart(fileKey, fileBody);
          }
        }

        HttpEntity wrappedEntity = reqBuilder.build();
        HttpEntity reqEntity;
        // decorate http entity with progress monitor
        reqEntity = new HttpEntityWithProgress(wrappedEntity, monitor, workedByteBatchSize);
        //reqEntity = wrappedEntity;
        
        HttpPost httppost = new HttpPost(url.toString());
        httppost.setEntity((HttpEntity)reqEntity);
        response = executeMethod(httpClient, httppost);
        EntityUtils.consumeQuietly(response.getEntity());
        
      } catch (Throwable e) {
        handleException("Failed to execute method.", e);
      }  finally {
        closeQuietly(response);
      } 
    }  
    
  }

  /**
   */
  private class PipeThread extends Thread {
    private OutputStream out = null;
    private IProgressMonitor monitor;
    private Map<File, CmsPath> fileUploads;

    /**
     * Constructor for PipeThread.
     * @param out OutputStream
     * @param fileUploads Map<File,CmsPath>
     * @param monitor IProgressMonitor
     */
    public PipeThread(OutputStream out, Map<File, CmsPath> fileUploads, IProgressMonitor monitor) {
      this.out = new BufferedOutputStream(out);
      this.fileUploads = fileUploads;
      this.monitor = monitor;
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        logger.debug("Piping...");

        for(File fileToUpload : fileUploads.keySet()) {
          if(monitor != null && monitor.isCanceled()) {
            break;
          }
          CmsPath destPath = fileUploads.get(fileToUpload);
          if(monitor != null) {
            monitor.subTask("Uploading File: " + destPath.last());
          }
          byte[] data = destPath.toDisplayString().getBytes();
          out.write(new String(data.length + ";").getBytes());
          out.write(data);

          out.write(new String(fileToUpload.length() + ";").getBytes());
          int len = -1;
          data = new byte[8096];
          BufferedInputStream fin = new BufferedInputStream(new FileInputStream(fileToUpload));
          while ((len = fin.read(data)) != -1) {
            out.write(data, 0, len);
          }
          fin.close();
          if(monitor != null) {
            monitor.worked(1);
          }
        }
        out.write('!');
        out.flush();
        out.close();
        if(monitor != null) {
          monitor.subTask("Committing transaction of " + fileUploads.size() + " files...");
        }
        logger.debug("Done");
      } catch (Throwable th) {
        if (th instanceof RuntimeException) {
          throw (RuntimeException)th;
        } else {
          throw new RuntimeException(th);
        }
      }
    }
  }


}
