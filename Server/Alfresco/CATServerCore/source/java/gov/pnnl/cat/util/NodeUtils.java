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
/**
 * Notice: This computer software was prepared by Battelle Memorial Institute,
 * hereinafter the Contractor for the Department of Homeland Security under the
 * terms and conditions of the U.S. Department of Energy's Operating Contract
 * DE-AC06-76RLO with Battelle Memorial Institute, Pacific Northwest Division.
 * All rights in the computer software are reserved by DOE on behalf of the
 * United States Government and the Contractor as provided in the Contract. You
 * are authorized to use this computer software for Governmental purposes but it
 * is not to be released or distributed to the public. NEITHER THE GOVERNMENT
 * NOR THE CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this sentence
 * must appear on any copies of this computer software.
 */
package gov.pnnl.cat.util;

import gov.pnnl.cat.pipeline.impl.ThumbnailProcessor;
import gov.pnnl.cat.util.DataSniffer.ContentDataInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains commonly used node operations so I don't have to reproduce them everywhere.
 * 
 * @version $Revision: 1.0 $
 */
public class NodeUtils {

  public static final String PATH_SEPARATOR = "/";
  private static final String DELETED_NODES = "gov.pnnl.cat.util.NodeUtils.DeletedNodes";
  
  /** Log */
  private static Log logger = LogFactory.getLog(NodeUtils.class);

  /** The node service */
  protected NodeService nodeService;

  /** The search service */
  protected SearchService searchService;

  /** The content service */
  protected ContentService contentService;

  /** The authentication component */
  protected AuthenticationComponent authenticationComponent;

  protected AuthorityService authorityService;

  protected FileFolderService fileFolderService;

  protected NamespaceService namespaceService;

  protected PermissionService permissionService;

  protected PersonService personService;

  protected static Map<String, String> typeMap;

  protected static NodeRef companyHome;

  /**
   * Constructor
   */
  public NodeUtils() {

  }

  /**
   * Spring initializer
   */
  public void init() {

  }

  /**
   * Sets the node service
   * 
   * @param nodeService
   *          the node service
   */
  public void setNodeService(final NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setAuthenticationComponent.
   * @param authenticationComponent AuthenticationComponent
   */
  public void setAuthenticationComponent(final AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }

  /**
   * Method setPersonService.
   * @param personService PersonService
   */
  public void setPersonService(final PersonService personService) {
    this.personService = personService;
  }

  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(final SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Method setAuthorityService.
   * @param authorityService AuthorityService
   */
  public void setAuthorityService(final AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  /**
   * Method setPermissionService.
   * @param permissionService PermissionService
   */
  public void setPermissionService(final PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  /**
   * Method setTypeMap.
   * @param typeMap Map<String,String>
   */
  public void setTypeMap(final Map<String, String> typeMap) {
    NodeUtils.typeMap = typeMap;
  }

  /**
   * Method createFolder.
   * @param parent NodeRef
   * @param name String
   * @return NodeRef
   */
  public static NodeRef createFolder(final NodeRef parent, final String name, 
      Map<QName, Serializable> nodeProps, NodeService nodeService) {
    return NodeUtils.createNode(parent, name, ContentModel.TYPE_FOLDER, ContentModel.ASSOC_CONTAINS, nodeProps, nodeService);
  }
  
  public NodeRef createFolder(NodeRef parent, String name) {
    return createFolder(parent, name, null, nodeService);
  }

  /**
   * Create a new taxonomy root folder inside the given parent folder
   * 
   * @param parent
   * @param name
  
   * @return NodeRef
   */
  public NodeRef createTaxonomy(final NodeRef parent, final String name) {
    return NodeUtils.createTaxonomy(parent, name, nodeService);
  }
  
  /**
   * Create a new taxonomy root folder inside the given parent folder
   * 
   * @param parent
   * @param name
  
   * @param nodeService NodeService
   * @return NodeRef
   */
  public static NodeRef createTaxonomy(final NodeRef parent, final String name, NodeService nodeService) {
    final NodeRef folder = NodeUtils.createFolder(parent, name, null, nodeService);
    NodeUtils.addTaxonomyRootAspect(folder, nodeService);
    return folder;
  }

  /**
   * A quick way to create a text file for testing
   * 
   * @param parent
   * @param name
   * @param content
  
   * @return NodeRef
   */
  public NodeRef createTextFile(final NodeRef parent, final String name, final String content) {
    return NodeUtils.createTextFile(parent, name, content, MimetypeMap.MIMETYPE_TEXT_PLAIN, this.nodeService, this.contentService);
  }

  /**
   * Method createTextFile.
   * @param parent NodeRef
   * @param name String
   * @param content String
   * @param mimetype String
   * @param nodeService NodeService
   * @param contentService ContentService
   * @return NodeRef
   */
  public static NodeRef createTextFile(final NodeRef parent, final String name, final String content, final String mimetype, final NodeService nodeService, final ContentService contentService) {
    final InputStream input = new ByteArrayInputStream(content.getBytes());
    return NodeUtils.createTextFile(parent, name, input, mimetype, nodeService, contentService);
  }

  /**
   * Method createTextFile.
   * @param parent NodeRef
   * @param name String
   * @param content InputStream
   * @param mimetype String
   * @param nodeService NodeService
   * @param contentService ContentService
   * @return NodeRef
   */
  public static NodeRef createTextFile(final NodeRef parent, final String name, final InputStream content, final String mimetype, final NodeService nodeService, final ContentService contentService) {

    final Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
    contentProps.put(ContentModel.PROP_NAME, name);

    // create content node
    final ChildAssociationRef association = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), ContentModel.TYPE_CONTENT, contentProps);
    final NodeRef contentNode = association.getChildRef();

    // write some content to new node
    final ContentWriter writer = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);
    writer.setMimetype(mimetype);
    writer.setEncoding("UTF-8");
    writer.putContent(content);

    return contentNode;
  }

  /**
   * Method createFile.
   * @param parent NodeRef
   * @param name String
   * @param content File
   * @param nodeService NodeService
   * @param contentService ContentService
   * @param mimetypeService MimetypeService
   * @return NodeRef
   */
  public static NodeRef createFile(final NodeRef parent, final String name, final File content, final NodeService nodeService, final ContentService contentService, final MimetypeService mimetypeService) {
    return createFile(parent, name, content, nodeService, contentService, mimetypeService,null);
  }
  
  public static NodeRef createFile(NodeRef parent, String name, File content, NodeService nodeService, 
      ContentService contentService, MimetypeService mimetypeService, Map<QName, Serializable> nodeProps ) {
   NodeRef contentNode = createNode(parent, name, ContentModel.TYPE_CONTENT, ContentModel.ASSOC_CONTAINS, nodeProps, nodeService);

   // write some content to new node
   ContentWriter writer = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);

   // guess the mime type
   ContentDataInfo info = DataSniffer.sniffContentData(name, content);
   String mimetype = info.getMimetype();
   String encoding = info.getEncoding();
   writer.setMimetype(mimetype);
   writer.setEncoding(encoding);
   writer.putContent(content);
   
   return contentNode;

  }

  public static NodeRef createFile(NodeRef parent, String name, byte[] content, NodeService nodeService, 
      ContentService contentService, MimetypeService mimetypeService, Map<QName, Serializable> nodeProps ) {
    NodeRef contentNode = createNode(parent, name, ContentModel.TYPE_CONTENT, ContentModel.ASSOC_CONTAINS, nodeProps, nodeService);

    // write some content to new node
    ContentWriter writer = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);
    
    ContentDataInfo info = DataSniffer.sniffContentData(name, content);
    String mimetype = info.getMimetype();
    String encoding = info.getEncoding();
    writer.setMimetype(mimetype);
    writer.setEncoding(encoding);
    writer.putContent(new ByteArrayInputStream(content));

    return contentNode;
 }
  
  public static NodeRef createNode(NodeRef parent, String name, QName nodeType, QName assocType,
      Map<QName, Serializable> nodeProps, NodeService nodeService) {
    
    if(nodeProps == null) {
      nodeProps = new HashMap<QName, Serializable>();
      nodeProps.put(ContentModel.PROP_NAME, name);
      
    } else if(!nodeProps.containsKey(ContentModel.PROP_NAME)) {
        nodeProps.put(ContentModel.PROP_NAME, name);
    }
    
    QName associationName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
    QName associationType= assocType;
    if(associationType == null) {
      associationType = ContentModel.ASSOC_CONTAINS;
    }
    
    ChildAssociationRef association =  nodeService.createNode(parent, associationType, associationName, 
        nodeType, nodeProps);

    return association.getChildRef();
  }
  
  /**
   * Method updateFileContents.
   * @param contentNode NodeRef
   * @param content InputStream
   * @param nodeService NodeService
   * @param contentService ContentService
   */
  public static void updateFileContents(final NodeRef contentNode, final InputStream content, final NodeService nodeService, final ContentService contentService) {
    final ContentWriter writer = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);
    writer.putContent(content);
  }

  /**
   * Gets the child node by any association type! This assumes that no two children will have the same name.
   * 
   * @param parentFolder
   * @param childName
  
   * @return NodeRef
   */
  public NodeRef getChildByName(final NodeRef parentFolder, final String childName) {
    return NodeUtils.getChildByName(parentFolder, childName, this.nodeService);
  }

  /**
   * Make a static method so callers can provide their own NodeService, so they can use internal or public beans when necessary.
   * 
   * @param parentFolder
   * @param childName
   * @param nodeService
   * @return NodeRef
   */
  public static NodeRef getChildByName(final NodeRef parentFolder, final String childName, final NodeService nodeService) {
    
    NodeRef node = null;
    
    List<ChildAssociationRef> children = nodeService.getChildAssocs(parentFolder, RegexQNamePattern.MATCH_ALL, new NodeNameQNamePattern(childName));
    
    // Should never be more than one match
    if (children.size() == 1) {
      node = children.get(0).getChildRef();
    } else if (children.size() == 0) {
      if(logger.isDebugEnabled()) {
        logger.debug("No children found with name " + childName);
      }
    } else if (children.size() > 0) {
      if(logger.isDebugEnabled()) {
        logger.debug("More than one child found with same name: " + childName);
      }
    }

    return node;
  }

  /**
   * Method getCompanyHome.
   * @return NodeRef
   */
  public NodeRef getCompanyHome() {
    return getCompanyHome(this.nodeService);
  }

  /**
   * Method getCompanyHome.
   * @param nodeService NodeService
   * @return NodeRef
   */
  public static NodeRef getCompanyHome(final NodeService nodeService) {
    if (NodeUtils.companyHome == null) {
      NodeUtils.companyHome = getNodeByName(CatConstants.PATH_COMPANY_HOME, nodeService);
    }
    return NodeUtils.companyHome;
  }

  /**
   * This method returns ALL permissions for ALL users - even inherited permissions set on a parent node. NOTE that Alfresco's PermissionService methods only look at the current node. So if the node's permissions are inherited, PermissionService.getAllSetPermissions() will return the empty set, which is not what we want. So we have to write our own method to recurse up the node hierarchy.
   * 
   * @param nodeRef
  
   * @return Set<AccessPermission>
   */
  public Set<AccessPermission> getAllPermissions(final NodeRef nodeRef) {

    final HashSet<AccessPermission> accessPermissions = new HashSet<AccessPermission>();
    final Set<String> settablePermissions = this.permissionService.getSettablePermissions(nodeRef);
    final String currentUser = this.authenticationComponent.getCurrentUserName();

    // Note that this method will take longer the more people in the system.
    // Unfortunately Alfresco's NodeTest only returns an AcessStatus, so there
    // currently is no
    // way to get a cached ACL from the permission service. By using
    // hasPermissions, at least
    // these values will be cached.
    final Set<NodeRef> people = this.personService.getAllPeople();

    for (final NodeRef person : people) {
      final String userName = (String) this.nodeService.getProperty(person, ContentModel.PROP_USERNAME);

      // Yikes - PermissionService.hasPermission() only works for the current
      // user! There is no
      // way to check permissions for somebody other than the current user, so
      // we have to hack together
      // a workaround :(
      AuthenticationUtil.setRunAsUser(userName);

      for (final String permission : settablePermissions) {
        if (this.permissionService.hasPermission(nodeRef, permission) == AccessStatus.ALLOWED) {
          accessPermissions.add(new AccessPermissionImpl(permission, AccessStatus.ALLOWED, userName, -1));
        }

      }
    }
    AuthenticationUtil.setRunAsUser(currentUser);

    return accessPermissions;

  }

  /**
   * @param nodeXPath
   *          the path to the node in Alfresco XPath format (e.g., /app:company_home)
  
   * @return null if not found */
  public NodeRef getNodeByXPath(final String nodeXPath) {
    return NodeUtils.getNodeByXPath(nodeXPath, this.nodeService, this.searchService, this.namespaceService);
  }

  /**
   * Method getNodeByXPath.
   * @param nodeXPath String
   * @param nodeService NodeService
   * @param searchService SearchService
   * @param namespaceService NamespaceService
   * @return NodeRef
   */
  public static NodeRef getNodeByXPath(final String nodeXPath, final NodeService nodeService, final SearchService searchService, final NamespaceService namespaceService) {

    NodeRef ret = null;

    // do NOT use a Lucene query because it gets slower as the repository gets
    // bigger
    // lets see how well an xpath search does
    final NodeRef rootNodeRef = nodeService.getRootNode(CatConstants.SPACES_STORE);
    final List<NodeRef> results = searchService.selectNodes(rootNodeRef, nodeXPath, null, namespaceService, false);
    if (results.size() > 0) {
      ret = results.get(0);
    }

    return ret;
  }

  /**
   * 
   * @param namePath String
   * @return null if not found */
  public NodeRef getNodeByName(final String namePath) {
    return getNodeByName(namePath, this.nodeService);
  }
 
  /**
   * Note this method gets the node by association name, not name property.
   * Method getNodeByName.
   * @param namePath String
   * @param nodeService NodeService
   * @return NodeRef - null if the node does not exist
   */
  public static NodeRef getNodeByName(final String namePath, final NodeService nodeService) {

    try {
      final NodeRef rootNode = nodeService.getRootNode(CatConstants.SPACES_STORE);
      return resolveNamePath(rootNode, splitAllPaths(namePath), nodeService);

    } catch (FileNotFoundException fileNotFound) {
      return null;
    } catch (final Exception e) {
      logger.error("error resolving name path", e);
      throw new RuntimeException(e);
    }

  }

  /**
   * Create a new {@link NodeRef} for the given UUID.
   * 
   * @param uuid
   *          String UUID to get {@link NodeRef} for
  
   * @return {@link NodeRef} for given UUID */
  public static NodeRef getNodeByUuid(String uuid) {
    return new NodeRef(CatConstants.SPACES_STORE, uuid);
  }

  /**
   * copied from FileFolderService but changed slightly to find children of any association type, not just ASSOC_CONTAINS
   * 
   * @param rootNodeRef
   * @param pathElements
  
  
   * @return NodeRef
   * @throws FileNotFoundException */
  public NodeRef resolveNamePath(final NodeRef rootNodeRef, final List<String> pathElements) throws FileNotFoundException {
    return resolveNamePath(rootNodeRef, pathElements, this.nodeService);
  }

  /**
   * Method resolveNamePath.
   * @param rootNodeRef NodeRef
   * @param pathElements List<String>
   * @param nodeService NodeService
   * @return NodeRef
   * @throws FileNotFoundException
   */
  public static NodeRef resolveNamePath(final NodeRef rootNodeRef, final List<String> pathElements, final NodeService nodeService) throws FileNotFoundException {
    if (pathElements.size() == 0) {
      throw new IllegalArgumentException("Path elements list is empty");
    }
    // walk the folder tree first
    NodeRef parentNodeRef = rootNodeRef;
    final StringBuilder currentPath = new StringBuilder(pathElements.size() * 20);
    final int folderCount = pathElements.size() - 1;
    for (int i = 0; i < folderCount; i++) {
      final String pathElement = pathElements.get(i);
      final NodeRef folderNodeRef = getChildByName(parentNodeRef, pathElement, nodeService);
      if (folderNodeRef == null) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Folder not found: " + currentPath);
        throw new FileNotFoundException(sb.toString());
      }
      parentNodeRef = folderNodeRef;
    }
    // we have resolved the folder path - resolve the last component
    final String pathElement = pathElements.get(pathElements.size() - 1);

    final NodeRef fileNodeRef = getChildByName(parentNodeRef, pathElement, nodeService);
    if (fileNodeRef == null) {
      final StringBuilder sb = new StringBuilder(128);
      sb.append("File not found: " + currentPath);
      throw new FileNotFoundException(sb.toString());
    }

    return fileNodeRef;
  }

  /**
   * Split the path into all the component directories and filename for use in getNodeByName
   * 
   * @param path
   *          String
  
   * @return List<String> */
  public static List<String> splitAllPaths(final String path) {
    if ((path == null) || (path.length() == 0)) {
      return Collections.emptyList();
    }

    // split the path
    final StringTokenizer token = new StringTokenizer(path, PATH_SEPARATOR);
    final List<String> results = new ArrayList<String>(10);
    while (token.hasMoreTokens()) {
      results.add(token.nextToken());
    }
    return results;
  }

  /**
   * This is a static method that allows callers to pass in their own version of NodeService, so they can use the public bean, or the internal bean, depending on performance/authentication requirements.
   * 
   * @param originalFile
   * @param destinationFolder
   * @param nodeService
  
   * @return NodeRef
   */
  public static NodeRef createLinkedFile(final NodeRef originalFile, final NodeRef destinationFolder, final NodeService nodeService) {
    final String name = (String) nodeService.getProperty(originalFile, ContentModel.PROP_NAME);

    final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
    props.put(ContentModel.PROP_NAME, name);
    props.put(ContentModel.PROP_LINK_DESTINATION, originalFile);

    final ChildAssociationRef linkRef = nodeService.createNode(destinationFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), ContentModel.TYPE_LINK, props);
    final NodeRef link = linkRef.getChildRef();

    // apply the titled aspect - title and description
    if (nodeService.hasAspect(originalFile, ContentModel.ASPECT_TITLED)) {
      final Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
      final String title = (String) nodeService.getProperty(originalFile, ContentModel.PROP_TITLE);

      titledProps.put(ContentModel.PROP_TITLE, title);
      titledProps.put(ContentModel.PROP_DESCRIPTION, "Link to " + name);
      nodeService.addAspect(link, ContentModel.ASPECT_TITLED, titledProps);
    }
    return link;
  }
  
  /**
   * Create link to a remote resource.
   * 
  
   * @param destinationFolder
   * @param nodeService
  
   * @param linkName String
   * @param linkUrl String
   * @param linkTitle String
   * @param linkDescription String
   * @param contentService ContentService
   * @return NodeRef
   */
  public static NodeRef createRemoteLink(NodeRef destinationFolder, String linkName, String linkUrl, String linkTitle, String linkDescription, NodeService nodeService,
      ContentService contentService) {
    return createRemoteLink(destinationFolder, linkName, linkUrl, linkTitle, linkDescription, ContentModel.TYPE_CONTENT, nodeService, contentService);
  }
  
  /**
   * Method createRemoteLink.
   * @param destinationFolder NodeRef
   * @param linkName String
   * @param linkUrl String
   * @param linkTitle String
   * @param linkDescription String
   * @param linkType QName
   * @param nodeService NodeService
   * @param contentService ContentService
   * @return NodeRef
   */
  public static NodeRef createRemoteLink(NodeRef destinationFolder, String linkName, String linkUrl, String linkTitle, String linkDescription,
      QName linkType, NodeService nodeService, ContentService contentService) {

    final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
    props.put(ContentModel.PROP_NAME, linkName);
    props.put(CatConstants.PROP_LINK_URL, linkUrl);
    if(linkTitle != null) {
      props.put(CatConstants.PROP_LINK_TITLE, linkTitle);
    } 
    if(linkDescription != null) {
      props.put(CatConstants.PROP_LINK_DESCRIPTION, linkDescription);
    } 
    
    ChildAssociationRef linkRef = nodeService.createNode(destinationFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, linkName), linkType, props);
    NodeRef link = linkRef.getChildRef();

    // create some data for the file
    if(linkType.equals(ContentModel.TYPE_CONTENT)) {
      StringBuilder content = new StringBuilder("Name:");
      content.append(linkName);
      content.append("\n");
      content.append("Remote Link:");
      content.append(linkUrl);
      content.append("\n");
      if(linkDescription != null) {
        content.append("Description:");
        content.append(linkDescription);
      }
      ContentWriter writer = contentService.getWriter(link, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
      writer.setEncoding("UTF-8");
      writer.putContent(content.toString());
    }
    
    return link;
  }

  /**
   * 
   * Create a linked file. Since this linked file gets automatically generated, let's use the same name as the original file.
   * 
   * @param originalFile
   *          - the file we want to link to
   * @param destinationFolder
   *          - where we want to put the link
  
   * @return NodeRef
   */
  public NodeRef createLinkedFile(final NodeRef originalFile, final NodeRef destinationFolder) {
    return NodeUtils.createLinkedFile(originalFile, destinationFolder, this.nodeService);
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(final ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Turns a folder into a taxonomy root
   * 
   * @param folder
   *          - the folder that Taxonomy Aspect will be added to
   */
  public void addTaxonomyRootAspect(final NodeRef folder) {
    addTaxonomyRootAspect(folder, nodeService);
  }
  
  /**
   * Method addTaxonomyRootAspect.
   * @param folder NodeRef
   * @param nodeService NodeService
   */
  public static void addTaxonomyRootAspect(final NodeRef folder, NodeService nodeService) {
    nodeService.addAspect(folder, CatConstants.ASPECT_TAXONOMY_ROOT, null);
  }

  /**
   * Keep walking up the tree until we find the root taxonomy node
   * 
   * @param taxNode
  
   * @return NodeRef
   */
  public NodeRef getTaxonomyRoot(final NodeRef taxNode) {
    NodeRef currentNode = taxNode;

    // TODO: this would probably be more efficient to do with a search!!
    while (!this.nodeService.hasAspect(currentNode, CatConstants.ASPECT_TAXONOMY_ROOT) && (this.nodeService.hasAspect(currentNode, CatConstants.ASPECT_TAXONOMY_FOLDER) || this.nodeService.getType(currentNode).equals(ContentModel.TYPE_LINK))) {
      // Should always find a taxonomyRoot somewhere up the chain
      currentNode = this.nodeService.getPrimaryParent(currentNode).getParentRef();
    }

    if (!this.nodeService.hasAspect(currentNode, CatConstants.ASPECT_TAXONOMY_ROOT)) {
      return null;
    }

    return currentNode;
  }

  /**
   * Get all the nodes that have been deleted underneath the 
   * @param nodeRef
  
   * @return List<NodeRef>
   */
  public List<NodeRef> getAllFolderChildren(NodeRef nodeRef){
    @SuppressWarnings("unchecked")

    // first get the map from the tx
    Map<NodeRef, List<NodeRef>> deletedNodesMap = (Map<NodeRef, List<NodeRef>> ) AlfrescoTransactionSupport.getResource(DELETED_NODES);
    if (deletedNodesMap == null) {
      deletedNodesMap = new HashMap<NodeRef, List<NodeRef>>();
      AlfrescoTransactionSupport.bindResource(DELETED_NODES, deletedNodesMap);
    }
    // now get the deleted nodes under this parent nodeRef
    List<NodeRef> deletedNodes = deletedNodesMap.get(nodeRef);
    if(deletedNodes == null) {
      deletedNodes = new ArrayList<NodeRef>();
      getChildrenRecursive(nodeRef, deletedNodes);
      deletedNodesMap.put(nodeRef, deletedNodes);
    }
    
    return deletedNodes;
  }
  
  /**
   * Method getChildrenRecursive.
   * @param nodeRef NodeRef
   * @param childNodeRefs List<NodeRef>
   */
  private void getChildrenRecursive(NodeRef nodeRef, List<NodeRef> childNodeRefs){
    // sometimes we are getting children nodes that don't exist - bizarre
    if(nodeService.exists(nodeRef)) {
      childNodeRefs.add(nodeRef);
      if(this.nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
        List<ChildAssociationRef> children = this.nodeService.getChildAssocs(nodeRef);
        NodeRef child;

        for(ChildAssociationRef childRef : children) {
          child = childRef.getChildRef();
          getChildrenRecursive(child, childNodeRefs);
        }
      }
    }
  }
  
  public static long[] getChildrenStatsRecursive(NodeService nodeService,ContentService contentService, NodeRef nodeRef){
    long[] stats = new long[2];
    if(nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
      
      List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
      NodeRef child;

      for(ChildAssociationRef childRef : children) {
        child = childRef.getChildRef();
        long[] newstats = getChildrenStatsRecursive(nodeService, contentService, child);
        stats[0] += newstats[0];
        stats[1] += newstats[1];
      }
    }else{
      ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
      //don't need down to the 'byte' precision, convert to KB before adding to our count 
      long bytes = reader.getSize();
      //increment filecount
      stats[0]++;
      //add to filesize
      stats[1] += (bytes/1000);
    }
    
    
    return stats;
  }
  
  
  /**
   * Return true if nodeRef is a descendant of one of possibleParents nodes.
   * 
   * @param nodeRef
   * @param possibleParents
  
   * @return boolean
   */
  public boolean isDescendant(final NodeRef nodeRef, final NodeRef... possibleParents) {
    boolean isDescendant = false;
    final NodeRef rootNode = this.nodeService.getRootNode(CatConstants.SPACES_STORE);
    NodeRef currentNode = nodeRef;
    NodeRef parentNode;

    while (!isDescendant && !currentNode.equals(rootNode)) {
      parentNode = this.nodeService.getPrimaryParent(currentNode).getParentRef();
      for (final NodeRef possibleParent : possibleParents) {
        if (parentNode.equals(possibleParent)) {
          isDescendant = true;
          break;
        }
      }
      currentNode = parentNode;
    }
    return isDescendant;
  }

  /**
   * Method getDatedFolder.
   * @param parentFolder NodeRef
   * @return NodeRef
   */
  public NodeRef getDatedFolder(final NodeRef parentFolder) {

    final Date date = new Date();
    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    final String formattedDate = df.format(date);
    final QName dateQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, formattedDate);
    NodeRef datedFolder = this.nodeService.getChildByName(parentFolder, ContentModel.ASSOC_CONTAINS, formattedDate);

    if (datedFolder == null) {
      datedFolder = this.createFolder(parentFolder, dateQName.getLocalName(), null, nodeService);
    }

    return datedFolder;

  }

  /**
   * Method setFileFolderService.
   * @param fileFolderService FileFolderService
   */
  public void setFileFolderService(final FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  /**
   * Method setNamespaceService.
   * @param namespaceService NamespaceService
   */
  public void setNamespaceService(final NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  /**
   * Method getBrowseUrlForNodeRef.
   * @param nodeRef NodeRef
   * @return URL
   */
  public URL getBrowseUrlForNodeRef(final NodeRef nodeRef) {
    return getBrowseUrlForNodeRef(nodeRef, this.nodeService, this.namespaceService);
  }

  /**
   * CAT Change - add this so we can report better error messages instead
   * of something like "transaction rolled back"
   * @param e
  
   * @return Throwable
   */
  public static Throwable getRootCause(Throwable e) {
    Throwable rootCause;
    Throwable cause = e;
    do {
      rootCause = cause;
      cause = cause.getCause();
    } while(cause != null);
    
    return rootCause;
  }

  /**
   * Retrieves the config folder. If the folder does not exist, it will be created automatically by calling this method.
   * 
  
   * @param nodeService NodeService
   * @return NodeRef
   */
  public static NodeRef getConfFolder(final NodeService nodeService) {
    final NodeRef companyHomeNode = getCompanyHome(nodeService);
    NodeRef confNode = getChildByName(companyHomeNode, CatConstants.NAME_CONF_FOLDER.getLocalName(), nodeService);

    if (confNode == null) {
      final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
      properties.put(ContentModel.PROP_NAME, CatConstants.NAME_CONF_FOLDER.getLocalName());

      confNode = nodeService.createNode(companyHomeNode, ContentModel.ASSOC_CONTAINS, CatConstants.NAME_CONF_FOLDER, ContentModel.TYPE_FOLDER, properties).getChildRef();

      nodeService.addAspect(confNode, CatConstants.ASPECT_CONFIG_ROOT, null);

    }

    return confNode;
  }

  /**
   * Method getBrowseUrlForNodeRef.
   * @param nodeRef NodeRef
   * @param nodeService NodeService
   * @param namespaceService NamespaceService
   * @return URL
   */
  public static URL getBrowseUrlForNodeRef(final NodeRef nodeRef, final NodeService nodeService, final NamespaceService namespaceService) {
    if (nodeService.exists(nodeRef) == false) {
      return null;
    }
    final QName nodeType = nodeService.getType(nodeRef);
    final String prefixString = nodeType.toPrefixString(namespaceService);
    String nodeUrl = typeMap.get(prefixString);
    if (nodeUrl == null) {
      return null;
    }
    final String filename = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    nodeUrl = nodeUrl.replaceAll("NODEID", nodeRef.getId());
    nodeUrl = nodeUrl.replaceAll("NAME", filename);
    try {
      return new URL(nodeUrl);
    } catch (final MalformedURLException e) {
      return null;
    }
  }

  /**
   * Method createMd5Hash.
   * @param input InputStream
   * @return String
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String createMd5Hash(final InputStream input) throws IOException, NoSuchAlgorithmException {
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

  /**
   * Given the required arguments, figures out the CIFS path to a given node
   * 
   * @param nodeService
  
  
  
  
  
  
   * @param nodeRef NodeRef
   * @param newName String
   */
  // public static String getCifsPath(NodeService nodeService, CIFSServer
  // cifsServer, Path path, String separator, String prefix)
  // {
  // first, find the CIFS root node
  // DiskSharedDevice diskShare =
  // cifsServer.getConfiguration().getPrimaryFilesystem();
  //
  // ContentContext contentCtx = (ContentContext) diskShare.getContext();
  // NodeRef rootNode = contentCtx.getRootNode();
  //
  // // now, build the path
  // StringBuilder buf = new StringBuilder(128);
  //
  // // ignore root node check if not passed in
  // boolean foundRoot = (rootNode == null);
  //
  // if (prefix != null)
  // {
  // buf.append(prefix);
  // }
  //
  // // skip first element as it represents repo root '/'
  // for (int i=1; i<path.size(); i++)
  // {
  // Path.Element element = path.get(i);
  // String elementString = null;
  // if (element instanceof Path.ChildAssocElement)
  // {
  // ChildAssociationRef elementRef =
  // ((Path.ChildAssocElement)element).getRef();
  // if (elementRef.getParentRef() != null)
  // {
  // // only append if we've found the root already
  // if (foundRoot == true)
  // {
  // Object nameProp = nodeService.getProperty(elementRef.getChildRef(),
  // ContentModel.PROP_NAME);
  // if (nameProp != null)
  // {
  // elementString = nameProp.toString();
  // }
  // else
  // {
  // elementString = element.getElementString();
  // }
  // }
  //
  // // either we've found root already or may have now
  // // check after as we want to skip the root as it represents the CIFS share
  // name
  // foundRoot = (foundRoot || elementRef.getChildRef().equals(rootNode));
  // }
  // }
  // else
  // {
  // elementString = element.getElementString();
  // }
  //
  // if (elementString != null)
  // {
  // buf.append(separator);
  // buf.append(elementString);
  // }
  // }
  //
  // return buf.toString();
  // }

  public static void renameNode(final NodeRef nodeRef, final String newName, final NodeService nodeService) {
    // First change the name property
    nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, newName);

    // Now change the child association to match the name by moving the node
    // we use the same assoc type and namespace as before - just change the local name
    final ChildAssociationRef oldAssoc = nodeService.getPrimaryParent(nodeRef);
    final NodeRef parentRef = oldAssoc.getParentRef();
    nodeService.moveNode(nodeRef, parentRef, oldAssoc.getTypeQName(), QName.createQName(oldAssoc.getQName().getNamespaceURI(), newName));

  }

  /**
   * Method moveNode.
   * @param nodeRef NodeRef
   * @param newParent NodeRef
   * @param nodeService NodeService
   */
  public static void moveNode(final NodeRef nodeRef, final NodeRef newParent, final NodeService nodeService) {

    final ChildAssociationRef oldAssoc = nodeService.getPrimaryParent(nodeRef);
    nodeService.moveNode(nodeRef, newParent, oldAssoc.getTypeQName(), oldAssoc.getQName());

  }

  public static NodeRef getThumbnail(ThumbnailService thumbnailService, ThumbnailProcessor thumbnailProcessor,
      NodeService nodeService, NodeRef nodeRef) {
    //Default is to get the medium-sized thumbnail
    return getThumbnail(thumbnailService, thumbnailProcessor, nodeService, nodeRef, CatConstants.THUMBNAIL_MEDIUM);
  }
  
  public static NodeRef getThumbnail(ThumbnailService thumbnailService, ThumbnailProcessor thumbnailProcessor,
      NodeService nodeService, NodeRef nodeRef, String thumbnailName) {
    NodeRef thumbnail = null;
    
    // First make sure the node still exists
    if(nodeService.exists(nodeRef)) {

      ThumbnailDefinition thumbnailDefinition = thumbnailService.getThumbnailRegistry().getThumbnailDefinition(thumbnailName);
      thumbnail = thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, thumbnailDefinition.getName());
      
      if (thumbnail == null) {
        thumbnail = thumbnailProcessor.createThumbnail(nodeRef);
      }

    } 
    return thumbnail;

  }
  
  public static String base64EncodeFile(File file) {
    String encoded = null;
    try {
      encoded = Base64.encodeFromFile(file.getAbsolutePath());
      
    } catch (Throwable e) {
      logger.error("Failed to base64 encode file contents.", e);
    }
    return encoded;
  }

  /**
   * Return true if this node is in the version store. We have to do this because version nodes have cm:content type, so we have no other way to distinguish them. We need to check if nodes are version nodes before running our policy because in some cases it will cause Alfresco to hang.
   * 
   * @param nodeRef
  
   * @return boolean
   */
  public static boolean isVersionNode(NodeRef nodeRef) {

    // If this node ref points to the version store (either using the version 1 or version 2
    // id, then it is a version node
    if (nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID) || nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID)) {
      return true;
    }
    return false;
  }
  
  /**
   * Get the path to the node based on the cm:name property (no associations),
   * but do not include Company Home, since we don't need that for webdav.
   * 
   * @param nodeRef
  
   * @param nodeService NodeService
   * @return String
   */
  public static String getNamePath(NodeRef nodeRef, NodeService nodeService) {
    String name = (String) nodeService.getProperty(nodeRef,
        ContentModel.PROP_NAME);
    NodeRef companyHome = NodeUtils.getCompanyHome(nodeService);
    NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();

    if (parent == null || parent.equals(companyHome)) {
      return "/" + name;

    } else {
      return getNamePath(parent, nodeService) + "/" + name;
    }
  }
  
  public static ContentReader getReader(NodeRef nodeRef, String versionLabel, QName contentPropQName,
      ContentService contentService, VersionService versionService) {
 
    // If we passed in a version number, get that version instead
    if(versionLabel != null) {
      nodeRef = versionService.getVersionHistory(nodeRef).getVersion(versionLabel).getFrozenStateNodeRef();
    }
    
    // Pass the binary content to the response stream
    return contentService.getReader(nodeRef, contentPropQName);
    
  }
}
