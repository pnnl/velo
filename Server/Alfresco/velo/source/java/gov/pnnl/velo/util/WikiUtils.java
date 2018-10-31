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
package gov.pnnl.velo.util;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.policy.FileNamePolicy;
import gov.pnnl.velo.wiki.content.WikiContentExtractor;
import gov.pnnl.velo.wiki.content.WikiContentExtractorRegistry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class WikiUtils {
  private static final String NO_WIKI = "/no_wiki";
  // Logger
  private static final Log logger = LogFactory.getLog(WikiUtils.class);

  private static String wikiHome;
  private static String wikiExtensions;
  private static Boolean wikiAutoSync = false;
  private static File contentStoreBase;
  private static ContentService contentService;
  private static String defaultWikiTemplatePath;

  // multi-slash regex
  private static String multipleSlashRegex = "/+";
  private static Pattern multipleSlasPattern = Pattern
      .compile(multipleSlashRegex);

  // Wiki/Alfresco path mappings
  // TODO: init via config file
  private static Map<String, String> alfPrefixToWikiPrefix = new HashMap<String, String>();
  private static Map<String, String> wikiPrefixToAlfPrefix = new HashMap<String, String>();

  // path AFTER company home to mount the wiki to
  private static String wikiMountPoint;

  private static MimetypeService mimetypeService;

  /**
   * spring init after properties set
   */
  public void init() {
    if (getWikiExtensions() != null) {
      defaultWikiTemplatePath = getWikiExtensions()
          + "/wikipages/cmsfile_default_template.wiki";
    }
  }

  /**
   * Method setWikiMountPoint.
   * 
   * @param newWikiMountPoint
   *          String
   */
  public void setWikiMountPoint(String newWikiMountPoint) {
    wikiMountPoint = newWikiMountPoint;
  }

  /**
   * Method getWikiMountPoint.
   * 
   * @return String
   */
  public static String getWikiMountPoint() {
    return wikiMountPoint;
  }

  public static String getWikiHome() {
    return wikiHome;
  }

  public void setWikiHome(String wikiHome) {
    if (wikiHome != null && wikiHome.trim().isEmpty()) {
      WikiUtils.wikiHome = null;
    } else {
      WikiUtils.wikiHome = wikiHome;
    }
  }

  public static String getWikiExtensions() {
    return wikiExtensions;
  }

  public void setWikiExtensions(String wikiExtensions) {
    if (wikiExtensions != null && wikiExtensions.trim().isEmpty()) {
      WikiUtils.wikiExtensions = null;
    } else {
      WikiUtils.wikiExtensions = wikiExtensions;
    }
  }

  public static Boolean getWikiAutoSync() {
    return wikiAutoSync;
  }

  public void setWikiAutoSync(Boolean wikiAutoSync) {
    WikiUtils.wikiAutoSync = wikiAutoSync;
  }

  public static String getDefaultWikiTemplatePath() {
    return defaultWikiTemplatePath;
  }

  /**
   * Method setContentStorePath.
   * 
   * @param contentStorePath
   *          String
   */
  public void setContentStorePath(String contentStorePath) {
    contentStoreBase = new File(contentStorePath);
  }

  /**
   * Method setContentService.
   * 
   * @param contentService
   *          ContentService
   */
  public void setContentService(ContentService contentService) {
    WikiUtils.contentService = contentService;
  }

  /**
   * The the path to the content file in the content store relative to the
   * content store base (as required by alfRecordFile.php)
   * 
   * 
   * 
   * @param nodeRef
   *          NodeRef
   * @return String
   */
  public static String getAlfrescoFilePath(NodeRef nodeRef) {

    FileContentReader reader = (FileContentReader) contentService.getReader(
        nodeRef, ContentModel.PROP_CONTENT);
    File alfFile = reader.getFile();

    // truncate the path to alfresco content store base
    String filePath = alfFile.getAbsolutePath();
    String contentStorePath = contentStoreBase.getAbsolutePath();

    String truncatedPath = filePath.substring(contentStorePath.length());

    return truncatedPath;
  }

  /**
   * 
   * 
   * @return String
   */
  public static String getRecordFilePath() {
    if (getWikiExtensions() != null) {
      return getWikiExtensions() + "/maintenance/alfRecordFile.php";
    }
    return null;
  }

  /**
   * Method getImportFilePath.
   * 
   * @return String
   */
  public static String getImportFilePath() {
    if (getWikiExtensions() != null) {
      return getWikiExtensions() + "/maintenance/importTextFile.php";
    }
    return null;
  }

  /**
   * 
   * 
   * @return String
   */
  public static String getDeletePath() {
    if (getWikiExtensions() != null) {
      return getWikiExtensions() + "/maintenance/delete.php";
    }
    return null;
  }

  /**
   * Method getMovePath.
   * 
   * @return String
   */
  public static String getMovePath() {
    if (getWikiExtensions() != null) {
      return getWikiExtensions() + "/maintenance/moveFile.php";
    }
    return null;
  }

  /**
   * Wiki path is everything after WFS: in the wiki url. Alfresco path is a path
   * of association names without the namespace prefixes. (i.e.,
   * /company_home/User Documents/carina/file1.txt)
   * 
   * @param wikiPath
   * 
   * @return String
   */
  public static String getAlfrescoNamePath(String wikiPath) {
    String alfrescoPath = wikiPath;

    // First get rid of extra slashes in the wiki path, as this could
    // confuse alfresco
    alfrescoPath = multipleSlasPattern.matcher(wikiPath).replaceAll("/");

    String userPrefix = "/users/";
    String noVeloPrefix = getNoVeloPrefix();;
    String sitesPrefix = "/sites/";

    if (alfrescoPath.equals("/users")) {
      alfrescoPath = "/company_home/User Documents";

    } else if (alfrescoPath.startsWith(userPrefix)) {
      // We have to put this hack in here to account for the fact that
      // alfresco bootstrap admin name is admin
      // whereas wiki makes it be Admin
      String adminFolderName = "/users/Admin";
      if (alfrescoPath.startsWith(adminFolderName)) {
        alfrescoPath = alfrescoPath.substring(adminFolderName.length());
        alfrescoPath = "/users/admin" + alfrescoPath;
      }

      // change users folder to User Documents
      // this is a special case that should not change
      alfrescoPath = alfrescoPath.substring(userPrefix.length() - 1);
      alfrescoPath = "/company_home/User Documents" + alfrescoPath;

    } else if (alfrescoPath.startsWith(noVeloPrefix)) {
      alfrescoPath = alfrescoPath.replaceAll(noVeloPrefix, "");
      alfrescoPath = "/company_home/" + alfrescoPath;

    } else if (wikiPrefixToAlfPrefix.size() > 0) {
      // TODO: we have custom mappings, so use those

    } else if (alfrescoPath.startsWith(sitesPrefix)){
      // /sites/<sitename>/documentLibrary/
      alfrescoPath = "/company_home/" + alfrescoPath;

    } else {
      // map everything under /company_home/Velo/
      alfrescoPath = wikiMountPoint + alfrescoPath;
    }

    return alfrescoPath;
  }

 
  public static String getNoVeloPrefix() {
    return NO_WIKI + "/";
  }


  /**
   * Method getRenamedWebdavPath.
   * 
   * @param alfrescoWebdavPath
   *          String
   * @param helper
   *          WebDAVHelper
   * @return String
   */
  public static String getRenamedWebdavPath(String alfrescoWebdavPath,
      WebDAVHelper helper) {

    String userDocumentsSegment = "/User Documents";
    String veloSegment = wikiMountPoint;// "/company_home/Velo";
    String renamedPath = "";

    if (alfrescoWebdavPath.startsWith(userDocumentsSegment + "/")
        || alfrescoWebdavPath.startsWith(veloSegment + "/")) {
      String[] paths = alfrescoWebdavPath.substring(1).split("/");
      renamedPath = "/" + paths[0];
      for (int i = 1; i < paths.length; i++) {
        renamedPath += "/" + FileNamePolicy.getFixedName(paths[i]);
      }

    } else if (alfPrefixToWikiPrefix.size() > 0) {
      // TODO: check if path contains one of registerd alfPrefix segments

    } else {
      renamedPath = alfrescoWebdavPath;
    }

    return renamedPath;
  }

  /**
   * Create wiki path for alfresco node (wiki path is everything after WFS: in
   * the wiki URL)
   * 
   * @param node
   * @param nodeService
   * 
   * 
   * @return String
   */
  public static String getWikiPath(NodeRef node, NodeService nodeService) {
    String wikiPath = null;

    // First make sure the node is in the spaces store - if not, just return
    // null;
    if (!node.getStoreRef().equals(CatConstants.SPACES_STORE)) {
      return wikiPath;
    }

    // First compute the alfresco name path
    CmsPath cmsPath = new CmsPath(nodeService.getPath(node).toString());
    String alfrescoPath = cmsPath.toAssociationNamePath();

    // for now, only map resources under a user folder, Projects, or Sites
    // TODO: need a way to register mappings in a config file
    String userDocumentsBase = "/company_home/User Documents";
    String veloBase = wikiMountPoint;

    if (alfrescoPath.startsWith(userDocumentsBase)) {
      // User Documents is special mapping that should not change
      wikiPath = substitutePrefixInWikiPath(userDocumentsBase, "/users",
          alfrescoPath);

      // We need to put hack in here since alfresco calls admin folder
      // admin but wiki expects
      // it to be Admin
      String adminFolderName = "/users/admin";
      if (wikiPath.startsWith(adminFolderName)) {
        wikiPath = wikiPath.substring(adminFolderName.length());
        wikiPath = "/users/Admin" + wikiPath;
      }

    } else if (alfPrefixToWikiPrefix.size() > 0) {
      // user has overloaded custom mappings
      // for(String prefix : alfPrefixToWikiPrefix.keySet()) {
      // String alfPrefix = prefix;
      // if(!alfPrefix.endsWith("/")) {
      // alfPrefix = alfPrefix + "/";
      // }
      //
      // if(path.startsWith(alfPrefix)) {
      // wikiNode = true;
      // break;
      // }
      // }

    } else if (alfrescoPath.equals(veloBase)) {
      wikiPath = "/";

    } else if (alfrescoPath.startsWith(veloBase)) {
      wikiPath = substitutePrefixInWikiPath(veloBase, "", alfrescoPath);

    } else {
      // these paths do not have an associated wiki page
      wikiPath = NO_WIKI + alfrescoPath;

    }

    return wikiPath;
  }

  /**
   * Method substitutePrefixInWikiPath.
   * 
   * @param alfrescoPrefix
   *          String
   * @param wikiPrefix
   *          String
   * @param alfrescoPath
   *          String
   * @return String
   */
  private static String substitutePrefixInWikiPath(String alfrescoPrefix,
      String wikiPrefix, String alfrescoPath) {
    String wikiPath = alfrescoPath.substring(alfrescoPrefix.length());

    // append wiki prefix
    wikiPath = wikiPrefix + wikiPath;

    return wikiPath;
  }

  /**
   * Method isValidNode.
   * 
   * @param nodeRef
   *          NodeRef
   * @param nodeService
   *          NodeService
   * @return boolean
   */
  public static boolean isValidNode(NodeRef nodeRef, NodeService nodeService) {
    boolean validNode = false;

    if (nodeService.exists(nodeRef)) {

      // If we aren't auto-synching then we only care about nodes that have the
      // wiki sync aspect
      // If we are auto-synching then we only care about nodes that don't have
      // the wiki ignore aspect
      if ((getWikiAutoSync() == true && !nodeService.hasAspect(nodeRef,
          VeloServerConstants.ASPECT_WIKI_IGNORE))
          || (getWikiAutoSync() == false && nodeService.hasAspect(nodeRef,
              VeloServerConstants.ASPECT_WIKI_SYNC))) {
        // We only care about stuff in the spaces store
        if (nodeRef.getStoreRef().equals(CatConstants.SPACES_STORE)) {

          // We only care about files and folders
          // TODO: maybe we want to consider sub-types at some point
          if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)
              || nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {

            // We don't want any nodes that are a "rendition of another
            // node
            if (nodeService.hasAspect(nodeRef, RenditionModel.ASPECT_RENDITION) == false) {
              validNode = true;
            }
          }
        }
      }
    }

    return validNode;
  }

  /**
   * Returns true if this node is synched with the wiki
   * 
   * @param node
   * 
   * @param nodeService
   *          NodeService
   * @param namespaceService
   *          NamespaceService
   * @return boolean
   */
  public static boolean isWikiNode(NodeRef node, NodeService nodeService,
      NamespaceService namespaceService) {
    boolean wikiNode = false;

    // make sure node is valid
    if (isValidNode(node, nodeService)) {

      String wikiPath = getWikiPath(node, nodeService);
      wikiNode = wikiPath != null && !wikiPath.startsWith(NO_WIKI);
    }
    return wikiNode;
  }

  /**
   * Returns true if this node is synched with wiki and can be renamed
   * 
   * @param node
   * 
   * @param nodeService
   *          NodeService
   * @param namespaceService
   *          NamespaceService
   * @return boolean
   */
  public static boolean isRenamableWikiNode(NodeRef node,
      NodeService nodeService, NamespaceService namespaceService) {

    boolean validNode = false;

    if (getWikiHome() != null && nodeService.exists(node)) {
      // We only care about stuff in the spaces store
      if (node.getStoreRef().equals(CatConstants.SPACES_STORE)) {

        // We only care about files and folders
        // TODO: maybe we want to consider sub-types at some point
        if (nodeService.getType(node).equals(ContentModel.TYPE_CONTENT)
            || nodeService.getType(node).equals(ContentModel.TYPE_FOLDER)) {

          // We don't want any nodes that are a "rendition of another
          // node
          if (nodeService.hasAspect(node, RenditionModel.ASPECT_RENDITION) == false) {
            validNode = true;
          }
        }
      }
    }

    boolean renamable = false;

    // make sure node is valid
    if (validNode) {
      String wikiPath = getWikiPath(node, nodeService);
      // you can't rename the node corresponding to /users (i.e., User
      // Documents)
      // TODO: also check for root folders we have mapped
      if (wikiPath != null && !wikiPath.equals("/users")
          && !wikiPath.startsWith(NO_WIKI)) {
        renamable = true;
      }
    }

    return renamable;
  }

  /**
   * Method getMimetype.
   * 
   * @param nodeRef
   *          NodeRef
   * @param nodeService
   *          NodeService
   * @param contentService
   *          ContentService
   * @return String
   */
  public static String getMimetype(NodeRef nodeRef, NodeService nodeService,
      ContentService contentService) {
    if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
      return getFolderMimetype(nodeRef, nodeService);

    } else {
      return getFileMimetype(nodeRef, contentService);
    }
  }

  /**
   * Method getFolderMimetype.
   * 
   * @param folder
   *          NodeRef
   * @param nodeService
   *          NodeService
   * @return String
   */
  public static String getFolderMimetype(NodeRef folder, NodeService nodeService) {
    String folderMimetype = (String) nodeService.getProperty(folder,
        VeloServerConstants.PROP_MIMEYPE);
    if (folderMimetype == null) {
      folderMimetype = VeloConstants.MIMETYPE_COLLECTION;
    }
    return folderMimetype;
  }

  /**
   * Returns null if this node is not a file or it has no content
   * 
   * @param file
   * @param contentService
   * 
   * @return String
   */
  public static String getFileMimetype(NodeRef file,
      ContentService contentService) {
    // Get the mimetype from the content reader in alfresco
    String mimetype = null;
    ContentReader reader = contentService.getReader(file,
        ContentModel.PROP_CONTENT);
    if (reader != null) {
      mimetype = reader.getMimetype();
    }
    return mimetype;
  }

  /**
   * @param nodeRef
   * @param mimetype
   * @param contentService
   * @param registry
   * 
   * @param nodeService
   *          NodeService
   * @return a temp file with the wiki page content - or null if there is no
   *         metadata other than mimetype (in this case we can skip the content)
   */
  public static File getWikiContent(NodeRef nodeRef, String mimetype,
      ContentService contentService, NodeService nodeService,
      WikiContentExtractorRegistry registry) {
    File wikiContent = null;

    try {

      // TODO: Chandrika - Once we update the mimetype of remotelinks we can
      // move it to a separate extractor
      // 13-Jun-2014 - TODO: Now that I am adding ability to send a key-value
      // metadata
      // file along with upload, we should probably use the same for
      // remote link creation too. create link can add the title, url and
      // description
      // as default metadata and wikiContent can be generic
      // {{DisplayAdhocMetadata}} template page which each velo instance can
      // overwrite
      // - each instance can parse the template variable and set display label
      // of choice
      if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_REMOTE_LINK)) {
        logger.debug("In log phase2command for remote link");
        wikiContent = TempFileProvider.createTempFile("velo-remotelink-",
            ".metadata");
        appendToFile(wikiContent, "__NOTOC__\n= Metadata  =\n");
        appendToFile(wikiContent, "'''Remote Link''' \n");
        appendToFile(
            wikiContent,
            "'''Name        :''' "
                + nodeService
                    .getProperty(nodeRef, CatConstants.PROP_LINK_TITLE) + "\n");
        appendToFile(
            wikiContent,
            "'''URL         :''' "
                + nodeService.getProperty(nodeRef, CatConstants.PROP_LINK_URL)
                + "\n");
        appendToFile(
            wikiContent,
            "'''Description :''' "
                + nodeService.getProperty(nodeRef,
                    CatConstants.PROP_LINK_DESCRIPTION) + "\n");
      } else if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)){
        // Now do mimetype specific extraction
        if (mimetype != null) {
          // Look up the mimetype from a map of mimetypes to extractors
          WikiContentExtractor extractor = registry
              .getWikiContentExtractor(mimetype);

          if (extractor != null) {
            wikiContent = extractor.extractWikiContent(nodeRef);
          }
        }
        // If there are user provided metadata at the time of upload
        // Make that as wiki page "Attributes" tab after metadata specific extraction
        // extraction
        long startPropSet = System.currentTimeMillis();
        // String metadataFilePath = (String)nodeService.getProperty(nodeRef,
        // VeloServerConstants.PROP_METADATA_FILE);

        // File metadataFile = new File(metadataFilePath);
        BufferedWriter bw=null;
        try{
        if (wikiContent == null) {
          wikiContent = TempFileProvider.createTempFile("velo-usergiven-",
              ".metadata");
          bw = new BufferedWriter(new FileWriter(wikiContent, true));
          bw.write("__NOTOC__");
          bw.newLine();
        }else{
          bw = new BufferedWriter(new FileWriter(wikiContent, true));
        }

        // Properties properties = new Properties();
        // properties.load(new FileInputStream(metadataFile));

        writeAdhocMetadataForWiki(nodeRef, mimetype, nodeService, bw);
        
        }catch(IOException ioe){
          // for now, throw exception up to caller
	      throw new RuntimeException(ioe);
	    } finally {
	      if (bw != null) {
	        try {
	          bw.close();
	        } catch (IOException ioe2) {
	        }
	      }
        }

        long endPropSet = System.currentTimeMillis();
        long propSetTime = (endPropSet - startPropSet);
        logger.debug("propset time in phase 2: " + propSetTime + "\n\n");
        System.out.println("propset time in phase 2: " + propSetTime + "\n\n");

      }

      File defaultTemplate = new File(defaultWikiTemplatePath);
      // if(wikiContent != null) {
      // // add tabs metadata to the bottom (is this right??)
      // //String extractedMetadata =
      // FileUtils.readFileToString(wikiContent);
      // appendToFile(wikiContent, tabsMetadata);
      //
      // } else {
      if (wikiContent == null) {
        // copy default template to temp file
        wikiContent = TempFileProvider.createTempFile("velo-default-",
            ".metadata");
        FileUtils.copyFile(defaultTemplate, wikiContent);
      }

    } catch (Throwable e) {
      if (wikiContent != null) {
        wikiContent.delete();
      }
      logger.error("Unable to create wiki metadata page for node: " + nodeRef,
          e);
      throw new RuntimeException(
          "Unable to create wiki metadata page for node: " + nodeRef, e);
    }
    return wikiContent;
  }

  private static void writeAdhocMetadataForWiki(NodeRef nodeRef, String mimetype, NodeService nodeService, BufferedWriter bw) throws IOException {
   StringBuilder adhocMetadataContent = new StringBuilder();
   
   // First check if there are adhoc metadata properties 
    Map<QName, Serializable> properties = nodeService
        .getProperties(nodeRef);
    for (QName key : properties.keySet()) {
      if (key.getNamespaceURI().startsWith(
          VeloServerConstants.NAMESPACE_VELO_ADHOC_PREFIX)) {
        adhocMetadataContent.append( "|" );
        adhocMetadataContent.append(key.getLocalName()); 
        adhocMetadataContent.append(" = ");
        adhocMetadataContent.append(properties.get(key));
        adhocMetadataContent.append("\n");
      }
    }
    
    if(adhocMetadataContent.length()==0){
      //if there are no adhoc metadata properties do not add the default
      //information such as file creation date, author etc. 
      //so that there is no separate Metadata tab with just the default info
      return;
    }
    bw.write("\n{{DisplayAdhocMetadata \n");
    appendDefaultMetadataAttributes(bw, nodeRef, mimetype, nodeService);
    bw.write(adhocMetadataContent.toString());
    bw.write("}}");
  }

  public static void appendDefaultMetadataAttributes(BufferedWriter bw, NodeRef nodeRef,
		String mimetype, NodeService nodeService) throws IOException {
	
	SimpleDateFormat dateFormat = new SimpleDateFormat(
        "dd-MMM-yyyy HH:mm:ss");

   
    // Add default author details as these are more expensive
    // to query from wiki side. DisplayAttributes template
    // can ignore if these if display of these are not needed
    Map<String, String> displaysByExtension = mimetypeService.getDisplaysByExtension();
    String filename = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    String filetype = null;
    int lastIndex = filename.lastIndexOf(".");
    if(lastIndex>0 & (lastIndex+1)!=filename.length()){ 
      //ignore files without extension and hidden . files
      filetype = displaysByExtension.get(filename.substring(lastIndex+1));
    }
    if(filetype==null){
    	if(mimetype!=null)
    		filetype = mimetype;
    	else
    		mimetype = WikiUtils.getMimetype(nodeRef, nodeService, contentService);
    }
    logger.debug(" creation date " +  nodeService.getProperty(nodeRef,
                ContentModel.PROP_CREATED));
    logger.debug(" creation date " + dateFormat.format((Date) nodeService.getProperty(nodeRef,
            ContentModel.PROP_CREATED)));
    
    bw.write("| FileName=" + filename);
    bw.write("| FileType=" + filetype );
    bw.write("| Creator="
            + (String) nodeService.getProperty(nodeRef,
                ContentModel.PROP_CREATOR));
    bw.write("| CreationDate="
            + dateFormat.format((Date) nodeService.getProperty(nodeRef,
                ContentModel.PROP_CREATED)));
    bw.write("| LastModified="
            + (String) nodeService.getProperty(nodeRef,
                ContentModel.PROP_MODIFIER));
    bw.write("| ModifiedDate="
            + dateFormat.format((Date) nodeService.getProperty(nodeRef,
                ContentModel.PROP_MODIFIED)));

   
}

  /**
   * Append given string to end of given file.
   * 
   * @param file
   * @param additionalContent
   */
  public static void appendToFile(File file, String additionalContent) {
    BufferedWriter bw = null;

    try {
      // open file in append mode
      bw = new BufferedWriter(new FileWriter(file, true));
      bw.write(additionalContent);
      bw.newLine();
      bw.flush();

    } catch (IOException ioe) {
      // for now, throw exception up to caller
      throw new RuntimeException(ioe);

    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException ioe2) {
        }
      }
    }
  }
  

  /**
   * Method createTempFile.
   * 
   * @param content
   *          String
   * @return File
   */
  public static File createTempFile(String content) {
    File tempFile = TempFileProvider.createTempFile("velo-", ".tmp");
    try {
      FileUtils.writeStringToFile(tempFile, content);
    } catch (Throwable e) {
      throw new RuntimeException("Unable to create temp file.", e);
    }
    return tempFile;
  }

  /**
   * Method execCommand.
   * 
   * @param cmdArray
   *          String[]
   * @throws Exception
   */
  public static void execCommand(String[] cmdArray) throws Exception {
    execCommand(cmdArray, null);
  }

  /**
   * Method getCurrentUserId.
   * 
   * @param authenticationComponent
   *          AuthenticationComponent
   * @return String
   */
  public static String getCurrentUserId(
      AuthenticationComponent authenticationComponent) {
    String user = authenticationComponent.getCurrentUserName();
    logger.debug("getCurrentUserId, currentUserName: " + user);
    // Use admin user for all system or unknown operations so the wiki can
    // recognize the user
    if (user == null || user.equalsIgnoreCase("unknown")
        || user.equals(AuthenticationUtil.getSystemUserName())) {
      logger
          .debug("Using admin user for all system or unknown operations so the wiki can recognize the user");
      user = AuthenticationUtil.getAdminUserName();
    } else {
      logger.debug("returning current user: " + user);
    }

    return user;
  }

  /**
   * Method execCommand.
   * 
   * @param cmdArray
   *          String[]
   * @param runDir
   *          File
   * @throws Exception
   */
  public static void execCommand(String[] cmdArray, File runDir)
      throws Exception {

    logger.debug("executing command:");
    try {
      for (String arg : cmdArray) {
        logger.debug(arg);
      }

      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec(cmdArray, null, runDir);

      StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
      StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());

      errorGobbler.start();
      outputGobbler.start();

      int exitVal = proc.waitFor();
      if (exitVal != 0) {
        errorGobbler.join();
        String msg = errorGobbler.getMessage();
        logger.error(msg);
        // throw new RuntimeException(msg);
      }
      logger.debug(outputGobbler.getMessage());
      logger.debug(errorGobbler.getMessage());

    } catch (IOException e) {
      if (e.toString().contains("The system cannot find the file specified")) {
        logger.error(e.toString());
      }
      throw e;

    }

  }

  /**
   */
  public static class StreamGobbler extends Thread {
    private InputStream is;
    private StringBuffer msg = new StringBuffer();

    /**
     * Constructor for StreamGobbler.
     * 
     * @param is
     *          InputStream
     */
    public StreamGobbler(InputStream is) {
      this.is = is;
    }

    /**
     * Method getMessage.
     * 
     * @return String
     */
    public String getMessage() {
      return msg.toString();
    }

    /**
     * Method run.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          msg.append(line + "\n");
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  /**
   * Get the path to the node based on the cm:name property (no associations),
   * but do not include Company Home, since we don't need that for the wiki.
   * Note that this assumes that duplicate child names are not allowed
   * 
   * @param nodeRef
   * 
   * @param nodeService
   *          NodeService
   * @return String
   */
  // private static String getNamePath(NodeRef nodeRef, NodeService nodeService)
  // {
  // String name = (String) nodeService.getProperty(nodeRef,
  // ContentModel.PROP_NAME);
  // NodeRef companyHome = NodeUtils.getCompanyHome(nodeService);
  // NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
  //
  // if (parent == null || parent.equals(companyHome)) {
  // return "/" + name;
  //
  // } else {
  // return getNamePath(parent, nodeService) + "/" + name;
  // }
  // }

  /**
   * Method createUserAccount.
   * 
   * @param userName
   *          String
   * @param password
   *          String
   * @param email
   *          String
   * @param displayName
   *          String
   * @param mutableAuthenticationService
   *          MutableAuthenticationService
   * @param personService
   *          PersonService
   */
  public static void createUserAccount(String userName, String password,
      String email, String displayName,
      MutableAuthenticationService mutableAuthenticationService,
      PersonService personService) {

    logger.debug("Trying to create new user: " + userName);
    // Create user account
    mutableAuthenticationService.createAuthentication(userName,
        password.toCharArray());

    // Create user profile
    Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
    properties.put(ContentModel.PROP_USERNAME, userName);

    // We MUST put first and last name props or CAT will crash because of
    // null pointer exception
    // Plus Alfresco wants you to set these props too
    // TODO: we can pass these as optional params
    if (displayName != null) {
      properties.put(ContentModel.PROP_FIRSTNAME, displayName);
    } else {
      properties.put(ContentModel.PROP_FIRSTNAME, "");
    }

    if (email != null) {
      properties.put(ContentModel.PROP_EMAIL, email);
    } else {
      properties.put(ContentModel.PROP_EMAIL, "");
    }

    properties.put(CatConstants.PROP_PRIMARY_PHONE_NUMBER, "");
    properties.put(ContentModel.PROP_LASTNAME, "");

    personService.createPerson(properties);
  }

  /**
   * We need to make a workaround to
   * mutableAuthenticationService.authenticationExists() because it uses Lucene
   * to check for existence, and it appears when called soon after the account
   * is created, Lucene has not been updated yet, so it returns false instead of
   * true. To work around this, we look for a child node via the NodeService
   * instead of using Lucene.
   * 
   * @param username
   * 
   * @return boolean
   */
  public static boolean userExists(String username) {

    return false;
  }

  /**
   * @param alfrescoPath
   *          - the name path to the file, where each segment corresponds to the
   *          value of the local name of the association QName we don't care
   *          about the namespace, because we assume no two child associations
   *          will have the same local name (e.g., /company_home/Temporary
   *          Files/)
   * @param nodeService
   * 
   * 
   * @return NodeRef
   * @throws FileNotFoundException
   */
  public static NodeRef getNodeByName(String alfrescoPath,
      NodeService nodeService) throws FileNotFoundException {

    NodeRef nodeRef = NodeUtils.getNodeByName(alfrescoPath, nodeService);
    String currentUser = AuthenticationUtil.getRunAsUser();

    if (nodeRef == null) {
      // Now run as admin and see if you still can't find the file
      AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil
          .getAdminUserName());
      nodeRef = NodeUtils.getNodeByName(alfrescoPath, nodeService);
      AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
      if (nodeRef == null) {
        String msg = "Resource " + alfrescoPath
            + " does not exist in alfresco!";
        logger.error(msg);
        throw new FileNotFoundException(msg);

      } else {
        // User does not have permissions to see this file, so return
        // unauthorized status
        throw new AccessDeniedException(
            "User does not have permissions to view: " + alfrescoPath);
      }
    }
    return nodeRef;
  }

  /**
   * Get the root cause of an exception so we can detect real problem from
   * exceptions that get thrown from a spring proxy.
   * 
   * @param e
   * 
   * @return Throwable
   */
  public static Throwable getRootCause(Throwable e) {
    Throwable rootCause;
    Throwable cause = e;
    do {
      rootCause = cause;
      cause = cause.getCause();
    } while (cause != null);

    return rootCause;
  }

  /**
   * Method createLinkedFile.
   * 
   * @param originalFile
   *          NodeRef
   * @param destinationFolder
   *          NodeRef
   * @param linkName
   *          String
   * @param nodeService
   *          NodeService
   * @return NodeRef
   */
  public static NodeRef createLinkedFile(NodeRef originalFile,
      NodeRef destinationFolder, String linkName, NodeService nodeService) {
    String name = linkName;
    if (name == null) {
      // if user has chosen not to rename the link, then use the same name
      // as the original file
      name = (String) nodeService.getProperty(originalFile,
          ContentModel.PROP_NAME);
    }
    Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
    props.put(ContentModel.PROP_NAME, name);
    props.put(ContentModel.PROP_LINK_DESTINATION, originalFile);

    ChildAssociationRef linkRef = nodeService.createNode(destinationFolder,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
        ContentModel.TYPE_LINK, props);
    NodeRef link = linkRef.getChildRef();

    // apply the titled aspect - title and description
    if (nodeService.hasAspect(originalFile, ContentModel.ASPECT_TITLED)) {
      final Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(
          2, 1.0f);
      final String title = (String) nodeService.getProperty(originalFile,
          ContentModel.PROP_TITLE);

      titledProps.put(ContentModel.PROP_TITLE, title);
      titledProps.put(ContentModel.PROP_DESCRIPTION, "Link to " + name);
      nodeService.addAspect(link, ContentModel.ASPECT_TITLED, titledProps);
    }
    return link;
  }

  /**
   * Method getMimetypeService.
   * 
   * @return MimetypeService
   */
  public static MimetypeService getMimetypeService() {
    return mimetypeService;
  }

  /**
   * Method setMimetypeService.
   * 
   * @param mimetypeService
   *          MimetypeService
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    // For now let's just inject the mimetype service into WikiUtils so other
    // non-spring based code
    // can get access to it. This is a hack because I'm in a super hurry.
    WikiUtils.mimetypeService = mimetypeService;
  }

}
