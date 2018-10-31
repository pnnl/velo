/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.web.scripts.CreateUpdateResources;
import gov.pnnl.cat.web.scripts.Delete;
import gov.pnnl.cat.web.scripts.GetChildren;
import gov.pnnl.cat.web.scripts.IsIdentical;
import gov.pnnl.cat.web.scripts.WebScriptUtils;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.tif.service.CmsService;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.FileContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author d3k339
 *
 */
public class CmsServiceVeloServer implements CmsService {
  private static Log logger = LogFactory.getLog(CmsServiceVeloServer.class);
  
  protected NodeService nodeService;
  protected ContentService contentService;
  protected SearchService searchService;
  protected NamespaceService namespaceService;
  protected MimetypeService mimetypeService;
  protected TransactionService transactionService;
  protected CopyService copyService;
  protected AuthorityService authorityService;
  protected PermissionService permissionService;
  protected OwnableService ownableService;
  protected AuthenticationComponent authenticationComponent;
  protected NodeUtils nodeUtils;
  protected DictionaryService dictionaryService;
  protected FileFolderService fileFolderService;
  protected VersionService versionService;
  protected AuthenticationService authenticationService;
  protected PersonService personService;

  private CreateUpdateResources createUpdateResourcesWebService;
  private Delete deleteWebService;
  private IsIdentical isIdenticalWebService;
  private GetChildren getChildrenWebService;
  
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public void setNamespaceService(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }
  
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }

  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  public void setCopyService(CopyService copyService) {
    this.copyService = copyService;
  }

  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  public void setPermissionService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public void setOwnableService(OwnableService ownableService) {
    this.ownableService = ownableService;
  }

  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }

  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  public void setVersionService(VersionService versionService) {
    this.versionService = versionService;
  }

  public void setAuthenticationService(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  public void setCreateUpdateResourcesWebService(CreateUpdateResources createUpdateResourcesWebService) {
    this.createUpdateResourcesWebService = createUpdateResourcesWebService;
  }
  
  public void setDeleteWebService(Delete deleteWebService) {
    this.deleteWebService = deleteWebService;
  }

  public void setIsIdenticalWebService(IsIdentical isIdenticalWebService) {
    this.isIdenticalWebService = isIdenticalWebService;
  }
  
  public void setGetChildrenWebService(GetChildren getChildrenWebService) {
    this.getChildrenWebService = getChildrenWebService;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFolder(java.lang.String, java.lang.String)
   */
  @Override
  public void createFolder(String parentPath, String name) {
    createFolder(parentPath, name, null);
  }
  
  @Override
  public void createFolder(String pathStr) {
    CmsPath path = new CmsPath(pathStr);
    Resource resource = new Resource(VeloConstants.TYPE_FOLDER, path);
    ArrayList<Resource> resourcesToCreate = new ArrayList<Resource>();
    resourcesToCreate.add(resource);
    createUpdateResourcesWebService.createUpdateResources(resourcesToCreate);
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFolder(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void createFolder(String parentAlfrescoPath, String name, String mimetype) {
    CmsPath path = new CmsPath(parentAlfrescoPath).append(name);
    Resource resource = new Resource(VeloConstants.TYPE_FOLDER, path);
    if(mimetype != null) {
      resource.setProperty(VeloConstants.PROP_MIMETYPE, mimetype);
    }
    ArrayList<Resource> resourcesToCreate = new ArrayList<Resource>();
    resourcesToCreate.add(resource);
    createUpdateResourcesWebService.createUpdateResources(resourcesToCreate);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFolders(java.util.List)
   */
  @Override
  public void createFolders(List<Resource> foldersToCreate) {
    createUpdateResourcesWebService.createUpdateResources(foldersToCreate);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#delete(java.util.List, java.lang.String)
   */
  @Override
  public void delete(List<String> alfrescoPaths, String option) {
    List<Resource> nodesToDelete = new ArrayList<Resource>();
    for(String pathStr : alfrescoPaths) {
      Resource resource = new Resource();
      resource.setPath(pathStr);
      nodesToDelete.add(resource);
    }
    deleteWebService.delete(nodesToDelete, option);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#delete(java.lang.String)
   */
  @Override
  public void delete(String alfrescoPath) {
    List<Resource> nodesToDelete = new ArrayList<Resource>();
    CmsPath cmsPath = new CmsPath(alfrescoPath);
    Resource resource = new Resource();
    resource.setPath(alfrescoPath);
    nodesToDelete.add(resource);
    deleteWebService.delete(nodesToDelete, null);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String, java.lang.String)
   */
  @Override
  public void createFile(String parentAlfrescoPath, String filePath) {
    File content = new File(filePath);
    String name = content.getName();
    NodeRef parent = getNodeRef(parentAlfrescoPath);
    NodeUtils.createFile(parent, name, content, nodeService, contentService, mimetypeService);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void createFile(String parentAlfrescoPath, String fileName, String fileContents) {
    byte[] content = fileContents.getBytes();
    NodeRef parent = getNodeRef(parentAlfrescoPath);

    NodeUtils.createFile(parent, fileName, content, nodeService, contentService, mimetypeService, null);

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createFile(java.lang.String, java.io.File)
   */
  @Override
  public void createFile(String parentAlfrescoPath, File file) {
    String name = file.getName();
    NodeRef parent = getNodeRef(parentAlfrescoPath);
    NodeUtils.createFile(parent, name, file, nodeService, contentService, mimetypeService);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#bulkUpload(java.util.Map)
   */
  @Override
  public void bulkUpload(Map<File, String> filesToAlfrescoPath) {
    // TODO This is slow because we are taking a local file and copying it to the alfresco content
    // store, which results in 2x IO writes. We should consider moving the file to the content
    // store instead, or writing the file to the content store in the first place
    
    for(File file : filesToAlfrescoPath.keySet()) {
      CmsPath alfrescoPath = new CmsPath(filesToAlfrescoPath.get(file));
      NodeRef parentFolder = getNodeRef(alfrescoPath.getParent().toAssociationNamePath());
      uploadFileFromLocalFolder(file, file.getName(), parentFolder);
    }
  }

  private NodeRef uploadFileFromLocalFolder(File srcFile, String fileName, NodeRef destFolder) {

    NodeRef child = null;

    try {
      child = nodeService.getChildByName(destFolder,
          ContentModel.ASSOC_CONTAINS, fileName);

      if (child == null) {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        NodeUtils.createFile(destFolder, fileName, srcFile, nodeService, contentService, mimetypeService, contentProps);

      } else {
        // Update content for the file
        NodeUtils.updateFileContents(child,
            new FileInputStream(srcFile), nodeService,
            contentService);
      }

    } catch (Throwable e) {
      logger.error("Failed to upload file: " + srcFile.getAbsolutePath(), e);
    }

    return child;
  }
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#bulkUpload(java.util.List, java.lang.String)
   */
  @Override
  public void bulkUpload(Collection<File> files, String parentPath) {
    Map<File, String> filesToAlfrescoPath = new HashMap<File, String>();
    CmsPath cmsPath = new CmsPath(parentPath);
    for(File file : files) {
      String alfrescoPath = cmsPath.append(file.getName()).toAssociationNamePath();
      filesToAlfrescoPath.put(file,  alfrescoPath);
    }
    
    bulkUpload(filesToAlfrescoPath);
  }
  
  @Override
  public void bulkUpload(File localDir, String remoteAlfDir, boolean recursive) {
    remoteAlfDir = remoteAlfDir.endsWith("/")?remoteAlfDir:remoteAlfDir+"/";
    if (localDir.isFile()) {
      createFile(remoteAlfDir, localDir);
      return;
    }
    if(recursive){
      File[] files = localDir.listFiles();
      for(int i=0; i<files.length;i++ ){
        if(files[i].isDirectory()){
          createFolder(remoteAlfDir, files[i].getName());
          bulkUpload(files[i], remoteAlfDir + files[i].getName(),true);
        }else{
          createFile(remoteAlfDir, files[i]);
        }
      }
    }else{
      Collection<File> files = FileUtils.listFiles(localDir, TrueFileFilter.INSTANCE, null);
      bulkUpload(files,remoteAlfDir);
    }
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
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createRemoteLinks(java.util.List)
   */
  @Override
  public void createRemoteLinks(List<RemoteLink> remoteLinks) {
    createUpdateResourcesWebService.createUpdateResources(remoteLinks);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createLink(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void createLink(String parentAlfrescoPath, String targetAlfrescoPath, String linkName) {
    CmsPath path = new CmsPath(parentAlfrescoPath).append(linkName);
    NodeRef targetNode = getNodeRef(targetAlfrescoPath);
    String targetUuid = targetNode.getId();
    
    Resource link = new Resource(VeloConstants.TYPE_LINKED_FILE, path);
    link.setProperty(VeloConstants.PROP_LINK_DESTINATION, targetUuid);
    List<Resource> links = new ArrayList<Resource>();
    links.add(link);
    createUpdateResourcesWebService.createUpdateResources(links);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createVirutalFolder(java.lang.String, java.lang.String)
   */
  @Override
  public void createVirtualFolder(String parentAlfrescoPath, String name) {
    CmsPath folderPath = new CmsPath(parentAlfrescoPath).append(name);
    Resource folder = new Resource(VeloConstants.TYPE_FOLDER, folderPath);
    folder.getAspects().add(VeloConstants.ASPECT_TAXONOMY_ROOT);
    List<Resource> folders = new ArrayList<Resource>();
    folders.add(folder);

    createUpdateResourcesWebService.createUpdateResources(folders);

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#createVirtualFolder(java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public void createVirtualFolder(String parentAlfrescoPath, String name, String[] alfrescoPathsToAdd) {
    createVirtualFolder(parentAlfrescoPath, name);
    String folderPath = parentAlfrescoPath + "/" + name;
    NodeRef taxNode = getNodeRef(folderPath);
    QName assocTypeQName = ContentModel.ASSOC_CONTAINS;
    
    // now copy over all the paths - our version of CopyServiceImpl will take care of creating taxonomy links
    for(String alfrescoPath : alfrescoPathsToAdd) {
      NodeRef sourceNode = getNodeRef(alfrescoPath);
      String nodeName = (String)nodeService.getProperty(sourceNode, ContentModel.PROP_NAME);
      QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName);

      copyService.copyAndRename(sourceNode, taxNode, assocTypeQName, assocName, true);
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getFile(java.lang.String, java.lang.String)
   */
  @Override
  public String getFile(String alfrescoPath, String localDownloadFolderPath) {
    return getFile(alfrescoPath, null, localDownloadFolderPath);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getFile(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public String getFile(String alfrescoPath, String version, String localDownloadFolderPath) {
    try {
      if (localDownloadFolderPath == null) {
        File parentDir = TempFileProvider.createTempFile("velo-download", "dir");
        parentDir.delete();
        parentDir.mkdir();
        localDownloadFolderPath = parentDir.getAbsolutePath();
      }
   
      NodeRef nodeRef = getNodeRef(alfrescoPath);
      if(nodeRef == null) {
        throw new RuntimeException("Could not find file : " + alfrescoPath + ".  Are permissions correct?");
      }
      //broke ass code here...
      String nodeName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      File file = new File(localDownloadFolderPath, nodeName);

      FileContentReader reader = (FileContentReader)NodeUtils.getReader(nodeRef, version, ContentModel.PROP_CONTENT, contentService, versionService);

      if(reader != null) {
        FileUtils.copyFile(reader.getFile(), file);
      }
      return file.getAbsolutePath();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public String getFolder(String alfrescoPath, String localDownloadFolderPath, boolean recursive) {

    try {
      if (localDownloadFolderPath == null) {
        File parentDir = TempFileProvider.createTempFile("velo-download", "dir");
        parentDir.delete();
        parentDir.mkdir();
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

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#move(java.lang.String, java.lang.String)
   */
  @Override
  public void move(String oldAlfrescoPath, String newParentAlfrescoPath) {
    NodeRef nodeRef = getNodeRef(oldAlfrescoPath);
    NodeRef newParent = getNodeRef(newParentAlfrescoPath);
    NodeUtils.moveNode(nodeRef, newParent, nodeService);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#rename(java.lang.String, java.lang.String)
   */
  @Override
  public void rename(String alfrescoPath, String newName) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    NodeUtils.renameNode(nodeRef, newName, nodeService);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#copy(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void copy(String fileToCopyPath, String destinationFolderPath, String newName, boolean overwrite) {
    NodeRef destParentNode = getNodeRef(destinationFolderPath);
    NodeRef sourceNode = getNodeRef(fileToCopyPath);
    
    if(overwrite) {
      // TODO: better not have a child of the same name with different association type
      NodeRef child = nodeService.getChildByName(destParentNode, ContentModel.ASSOC_CONTAINS, newName);
      if(child != null) {
        nodeService.deleteNode(child);
      }
    }
    QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName);
    QName assocTypeQName = ContentModel.ASSOC_CONTAINS;
    copyService.copyAndRename(sourceNode, destParentNode, assocTypeQName, assocName, true);

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#copy(java.util.List, java.lang.String, java.util.List, boolean)
   */
  @Override
  public void copy(List<String> filesToCopyPaths, String destinationFolderPath, List<String> newNames, boolean overwrite) {
    int i = 0;
    for(String fileToCopyPath : filesToCopyPaths) {
      copy(fileToCopyPath, destinationFolderPath, newNames.get(i), overwrite);
      i++;
    }

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#setMimetype(java.lang.String, java.lang.String)
   */
  @Override
  public void setMimetype(String alfrescoPath, String mimetype) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    nodeService.setProperty(nodeRef, VeloServerConstants.PROP_MIMEYPE, mimetype);

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#exists(java.lang.String)
   */
  @Override
  public boolean exists(String alfrescoPath) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    return nodeRef != null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#isIdentical(java.lang.String, java.io.File)
   */
  @Override
  public boolean isIdentical(String alfrescoPath, File localFile) {
    try {
      NodeRef nodeRef = getNodeRef(alfrescoPath);
      String md5Hash = NodeUtils.createMd5Hash(new FileInputStream(localFile));
      return isIdenticalWebService.isIdentical(nodeRef, md5Hash, ContentModel.PROP_CONTENT);

    } catch (Throwable e) {
      throw new RuntimeException (e);
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getChildren(java.lang.String)
   */
  @Override
  public List<Resource> getChildren(String alfrescoPath) {
    return getChildrenWebService.getChildren(alfrescoPath);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#isFolder(java.lang.String)
   */
  @Override
  public boolean isFolder(String alfrescoPath) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    QName nodeType = nodeService.getType(nodeRef);
    return  dictionaryService.isSubClass(nodeType, ContentModel.TYPE_FOLDER);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#setProperty(java.lang.String, java.lang.String, java.lang.Object)
   */
  @Override
  public void setProperty(String alfrescoPath, String fullyQualifiedName, String value) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    QName qname = QName.createQName(fullyQualifiedName);
    List<String>values = new ArrayList<String>();
    values.add(value);
    Serializable convertedValue = WebScriptUtils.getPropertyValueFromStringList(dictionaryService, qname, values);
    nodeService.setProperty(nodeRef, qname, convertedValue);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#removeProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void removeProperty(String alfrescoPath, String fullyQualifiedName) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    QName qname = QName.createQName(fullyQualifiedName);
    nodeService.removeProperty(nodeRef, qname);

  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#removeProperties(java.lang.String, java.util.List)
   */
  @Override
  public void removeProperties(String alfrescoPath, List<String> propNames) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    Map<QName, Serializable> alfrescoProps = nodeService.getProperties(nodeRef);
    
    for(String propName : propNames) {
      QName qname = QName.createQName(propName);
      alfrescoProps.remove(qname);  
    }
    
    nodeService.setProperties(nodeRef, alfrescoProps);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#setProperties(java.lang.String, java.util.Map)
   */
  @Override
  public void setProperties(String alfrescoPath, Map<String, String> properties) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    Map<String, List<String>> props = new HashMap<String, List<String>>();
    for(String propname : properties.keySet()) {
      List<String> values = new ArrayList<String>();
      values.add(properties.get(propname));
      props.put(propname, values);
    }
    
    WebScriptUtils.setProperties(nodeRef, props, nodeService, dictionaryService);
  }
  
  private NodeRef getNodeRef(String alfrescoPath) {
    return NodeUtils.getNodeByName(alfrescoPath, nodeService);
  
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getProperty(java.lang.String, java.lang.String)
   */
  @Override
  public String getProperty(String alfrescoPath, String fullyQualifiedName) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    QName qname = QName.createQName(fullyQualifiedName);
    Serializable propertyValue = nodeService.getProperty(nodeRef, qname);
    String valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, propertyValue);
    return valueStr;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.CmsService#getResource(java.lang.String)
   */
  @Override
  public Resource getResource(String alfrescoPath) {
    NodeRef nodeRef = getNodeRef(alfrescoPath);
    return WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService);
  }

  @Override
  public void setRunAsUser(String user) {
    // since setFullyAuthenticatedUser is case sensitive (whereas logins are not), we have to make sure we
    // get the exact spelling correctly for the username
    String normalized = personService.getUserIdentifier(user);
  
    // this sets the user for all methods
    AuthenticationUtil.setFullyAuthenticatedUser(normalized);
    
    // DO NOT use setRunAsUser - this doesn't set the user for all methods, only those that require authentication!
    //AuthenticationUtil.setRunAsUser(user);
  }

  
 

}
