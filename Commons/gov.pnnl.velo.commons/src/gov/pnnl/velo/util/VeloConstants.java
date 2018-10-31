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

import java.util.Date;

import org.alfresco.webservice.util.ISO9075;





/**
 * TODO need to bundle cat core resources plugin so we get all the core props
 * defined in one place
 */
public class VeloConstants {
  
  /** Space Schemes */
  public static final String WORKSPACE_STORE = "workspace";
  
  /** Query language names */
  public static final String QUERY_LANG_LUCENE = "lucene";

  // Namespaces
  public static final String NAMESPACE_VELO = "http://www.pnl.gov/velo/model/content/1.0";
  public static final String NAMESPACE_CAT = "http://www.pnl.gov/cat/model/content/1.0";
  public static final String NAMESPACE_SYSTEM   = "http://www.alfresco.org/model/system/1.0";
  public static final String NAMESPACE_CONTENT  = "http://www.alfresco.org/model/content/1.0";
  public static final String NAMESPACE_APP = "http://www.alfresco.org/model/application/1.0";
  public static final String NAMESPACE_TAXONOMY = "http://www.pnl.gov/dmi/model/taxonomy/1.0";
  public static final String NAMESPACE_NOTIFICATION = "http://www.pnl.gov/dmi/model/notification/1.0";
  public static final String NAMESPACE_FORUM  = "http://www.alfresco.org/model/forum/1.0";
  public static final String NAMESPACE_ALERT  = "http://www.pnl.gov/dmi/model/alert/1.0";
  public static final String NAMESPACE_SUBSCRIPTION  = "http://www.pnl.gov/dmi/model/subscription/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY = "http://www.pnl.gov/dmi/model/subscription/repository/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH = "http://www.pnl.gov/dmi/model/subscription/repository/search/1.0";
  public static final String NAMESPACE_RENDITION = "http://www.alfresco.org/model/rendition/1.0";
  public static final String NAMESPACE_TAGTIMER = "http://www.pnl.gov/cat/model/tagtimer/1.0";
  public static final String NAMESPACE_TRANSFORM_TEXT = "http://www.pnl.gov/cat/model/transform/text/1.0";
  public static final String NAMESPACE_USER  = "http://www.alfresco.org/model/user/1.0";
    
  /** Prefix constants */
  public static final String NS_PREFIX_VELO = "velo";
  public static final String NS_PREFIX_CAT = "cat";
  public static final String NS_PREFIX_SYSTEM = "sys";
  public static final String NS_PREFIX_CONTENT = "cm";
  public static final String NS_PREFIX_APP = "app";
  public static final String NS_PREFIX_TAXONOMY = "tax";
  public static final String NS_PREFIX_NOTIFICATION = "not";  
  public static final String NS_PREFIX_FORUM = "fm";
  public static final String NS_PREFIX_ALERT = "alrt";
  public static final String NS_PREFIX_SUBSCRIPTION = "sub";
  public static final String NS_PREFIX_SUBSCRIPTION_REPOSITORY_SEARCH = "sub.srch";
  public static final String NS_PREFIX_SUBSCRIPTION_REPOSITORY = "sub.rep";
  public static final String NS_PREFIX_RENDITION = "rn";
  public static final String NS_PREFIX_TAGTIMER = "tagtimer";  
  public static final String NS_PREFIX_TRANSFORM_TEXT = "trans_txt";  
  public static final String NS_PREFIX_USER = "usr";

  // TODO Bio KB stuff - needs to be refactored!!
  public static final String NAMESPACE_OMICS = "{http://www.pnl.gov/sdkdi/model/content/1.0}";
  public static final String ASPECT_OMICS_LINKED_DATA_RESOURCE = NAMESPACE_OMICS + "linkedDataResource";


  // Aspects
  public static final String ASPECT_REMOTE_LINK = createQNameString(NAMESPACE_CAT, "remoteLink");
  public static final String ASPECT_IGNORE = createQNameString(NAMESPACE_CAT, "ignore");
  public static final String ASPECT_HIDDEN_RENDITION = createQNameString(NAMESPACE_RENDITION, "hiddenRendition");
  public static final String ASPECT_FAVORITES_ROOT = createQNameString(NAMESPACE_CAT, "favoritesRoot");
  public static final String ASPECT_PERSONAL_LIBRARY_ROOT = createQNameString(NAMESPACE_CAT, "personalLibraryRoot");
  public static final String ASPECT_TAXONOMY_ROOT = createQNameString(NAMESPACE_TAXONOMY, "taxonomyRoot");
  public static final String ASPECT_TAXONOMY_FOLDER = createQNameString(NAMESPACE_TAXONOMY, "taxonomyFolder");
  public static final String ASPECT_TAXONOMY_LINK = createQNameString(NAMESPACE_TAXONOMY, "taxonomyLink");
  public static final String ASPECT_TEXT_TRANSFORM = createQNameString(NAMESPACE_TRANSFORM_TEXT, "transform");
  public static final String ASPECT_HOME_FOLDER = createQNameString(NAMESPACE_CAT, "homeFolder");
  public static final String ASPECT_USER_HOME_FOLDER = createQNameString(NAMESPACE_CAT, "userHomeFolder");
  public static final String ASPECT_TEAM_HOME_FOLDER = createQNameString(NAMESPACE_CAT, "teamHomeFolder");

  public static final String ASPECT_PROJECT = createQNameString(NAMESPACE_CAT, "project");
  public static final String ASPECT_PROFILE = createQNameString(NAMESPACE_CAT, "profile");
  public static final String ASPECT_CONFIG_ROOT = createQNameString(NAMESPACE_CAT, "configRoot");
  public static final String ASPECT_AUTHOR = createQNameString(NAMESPACE_CONTENT, "author");
  public static final String ASPECT_DISCUSSABLE = createQNameString(NAMESPACE_FORUM, "discussable");
  public static final String ASPECT_NEEDS_FULL_TEXT_INDEXED = createQNameString(NAMESPACE_CAT, "needsFullTextIndexed");
  public static final String ASPECT_FAILED_THUMBNAIL_SOURCE = createQNameString(NAMESPACE_CONTENT, "failedThumbnailSource");
  public static final String ASPECT_VERSIONABLE =     createQNameString(NAMESPACE_CONTENT, "versionable");
  public static final String ASPECT_TITLED =          createQNameString(NAMESPACE_CONTENT, "titled");
  public static final String ASPECT_CLASSIFIABLE =    createQNameString(NAMESPACE_CONTENT, "classifiable"); 
  public static final String ASPECT_COPIED_FROM =    createQNameString(NAMESPACE_CONTENT, "copiedfrom"); 
  public static final String ASPECT_RENDITIONED = createQNameString(NAMESPACE_RENDITION, "renditioned"); 
  public static final String ASPECT_WEB_VIEW = createQNameString(NAMESPACE_VELO, "webView");

  // Mimetypes
  public static final String PROP_MIMETYPE = createQNameString(NAMESPACE_VELO, "mimetype");
  public static final String MIMETYPE_PROJECT = "cmsfile/project";
  public static final String MIMETYPE_PROJECTS = "cmsfile/projects";
  public static final String MIMETYPE_COLLECTION = "cmsfile/collection";
  public static final String MIMETYPE_VIRTUAL_COLLECTION = "cmsfile/virtual-collection";
  public static final String MIMETYPE_USERS = "cmsfile/users";
  public static final String MIMETYPE_TEXT_PLAIN  = "text/plain";
  public static final String MIMETYPE_TEXT_CSS    = "text/css";  
  public static final String MIMETYPE_XML = "text/xml";

  // Types
  public static final String TYPE_SYSTEM_FOLDER = createQNameString(NAMESPACE_CONTENT, "systemfolder");
  public static final String TYPE_CATEGORY = createQNameString(NAMESPACE_CONTENT, "category");
  public static final String TYPE_FOLDER = createQNameString(NAMESPACE_CONTENT, "folder");
  public static final String TYPE_FILE = createQNameString(NAMESPACE_CONTENT, "content");
  public static final String TYPE_CMOBJECT = createQNameString(NAMESPACE_CONTENT, "cmobject");
  
  public static final String TYPE_THUMBNAIL = createQNameString(NAMESPACE_CONTENT, "thumbnail");
  public static final String TYPE_LINKED_FILE = createQNameString(NAMESPACE_CONTENT, "link");
  public static final String TYPE_TEAM = createQNameString(NAMESPACE_CAT, "team");
  public static final String TYPE_FORUMS = createQNameString(NAMESPACE_FORUM, "forums");
  public static final String TYPE_FORUM = createQNameString(NAMESPACE_FORUM, "forum");
  public static final String TYPE_TOPIC = createQNameString(NAMESPACE_FORUM, "topic");
  public static final String TYPE_POST = createQNameString(NAMESPACE_FORUM, "post");
  public static final String TYPE_DISCUSSION = createQNameString(NAMESPACE_FORUM, "forum");
  public static final String TYPE_SUBSCRIPTION_REPOSITORY_SEARCH = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, "subscription");
  public static final String TYPE_SUBSCRIPTION_REPOSITORY = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY, "subscription");

  
  // Alfresco associations
  public static final String ASSOC_TYPE_CHILDREN = createQNameString(NAMESPACE_SYSTEM, "children");    
  public static final String ASSOC_TYPE_CONTAINS = createQNameString(NAMESPACE_CONTENT, "contains");
  public static final String ASSOC_TYPE_ORIGINAL = createQNameString(NAMESPACE_CONTENT, "original"); // used by copiedFrom aspect


  // Alfresco properties
  public static final String PROP_CREATOR = createQNameString(NAMESPACE_CONTENT, "creator");
  public static final String PROP_AUTHOR = createQNameString(NAMESPACE_CONTENT, "author");
  public static final String PROP_CONTENT = createQNameString(NAMESPACE_CONTENT, "content");
  public static final String PROP_FOLDER = createQNameString(NAMESPACE_CONTENT, "folder");
  public static final String PROP_MODIFIED = createQNameString(NAMESPACE_CONTENT, "modified");
  public static final String PROP_MODIFIER = createQNameString(NAMESPACE_CONTENT, "modifier");  
  public static final String PROP_OWNER = createQNameString(NAMESPACE_CONTENT, "owner");  
  public static final String PROP_UUID = createQNameString(NAMESPACE_SYSTEM, "node-uuid");
  public static final String PROP_LINK_DESTINATION = createQNameString(NAMESPACE_CONTENT, "destination");
  public static final String PROP_TITLE = createQNameString(NAMESPACE_CONTENT, "title");
  public static final String PROP_DESCRIPTION = createQNameString(NAMESPACE_CONTENT, "description");
  public static final String PROP_NAME = createQNameString(NAMESPACE_CONTENT, "name");
  public static final String PROP_CREATED = createQNameString(NAMESPACE_CONTENT, "created");
  public static final String PROP_CREATED_ON_BEHALF_OF = createQNameString(NAMESPACE_CONTENT, "createdOnBehalfOf");
  public static final String PROP_MODIFIED_ON_BEHALF_OF =createQNameString(NAMESPACE_CONTENT, "modifiedOnBehalfOf");

  
  public static final String PROP_PATH = createQNameString(NAMESPACE_CONTENT, "path");
  public static final String PROP_THUMBNAIL = createQNameString(NAMESPACE_CAT, "base64Thumbnail");  
  public static final String PROP_SOURCE = "{http://www.alfresco.org/model/content/1.0}source"; // what is this??  
  // TODO: get rid of taxonomy code!
  public static final String PROP_TAX_CATEGORIES = createQNameString(NAMESPACE_TAXONOMY, "categories");
  public static final String PROP_CATEGORIES = createQNameString(NAMESPACE_CONTENT, "categories");
  public static final String PROP_TEXT_TRANSFORMED_CONTENT = createQNameString(NAMESPACE_TRANSFORM_TEXT, "transformedContent");
  public static final String PROP_TEXT_TRANSFORM_ERROR = createQNameString(NAMESPACE_TRANSFORM_TEXT, "transformError");
  public static final String PROP_NEEDS_FULL_TEXT_INDEXED = createQNameString(NAMESPACE_CAT, "needsFullTextIndexed");
  public static final String PROP_PARENT_MIMETYPE = createQNameString(NAMESPACE_CAT, "parentMimetype");
  public static final String PROP_CHILD_COUNT = createQNameString(NAMESPACE_CAT, "childCount");
  public static final String PROP_HASH = createQNameString(NAMESPACE_CAT, "hash");
  public static final String PROP_SIZE = createQNameString(NAMESPACE_CAT, "size");

  // remote link properties
  public static final String PROP_REMOTE_LINK_URL = createQNameString(NAMESPACE_CAT, "linkUrl");  
  public static final String PROP_REMOTE_LINK_MACHINE = createQNameString(NAMESPACE_CAT, "remoteMachine");  
  public static final String PROP_REMOTE_LINK_PATH = createQNameString(NAMESPACE_CAT, "remoteFilePath");  
  public static final String PROP_REMOTE_LINK_TITLE = PROP_TITLE;
  public static final String PROP_REMOTE_LINK_DESCRIPTION = PROP_DESCRIPTION;   

  // thumbnail names
  public static final String THUMBNAIL_PREVIEW = "imgpreview";
  public static final String THUMBNAIL_DOCLIB = "doclib";
  public static final String THUMBNAIL_PREVIEW_PANE = "previewPaneImage";

  //Association Names (for adding to paths)
  public static final String ASSOC_NAME_DISCUSSION = createQNameString(NAMESPACE_FORUM, "discussion");
  public static final String ASSOC_NAME_TEMP_FOLDER = createQNameString(NAMESPACE_CONTENT, "Temporary Files");
  public static final String ASSOC_NAME_USER_DOCUMENTS = createQNameString(NAMESPACE_CONTENT, "User Documents"); // TODO: redundant
 
  // profile props
  public static final String PROP_PICTURE = createQNameString(NAMESPACE_CAT,  "profileImage");
  public static final String PROP_ID      = PROP_UUID;
  public static final String PROP_USER_EMAIL = createQNameString(NAMESPACE_CONTENT, "email");
  public static final String PROP_TEAM_EMAIL = createQNameString(NAMESPACE_CAT, "email");
  public static final String PROP_USER_HOMEFOLDER =  createQNameString(NAMESPACE_CONTENT, "homeFolder");
  public static final String PROP_TEAM_HOMEFOLDER = createQNameString(NAMESPACE_CAT, "homeFolder");
  public static final String PROP_PRIMARY_PHONE   = createQNameString(NAMESPACE_CAT,  "primaryPhoneNumber");
  public static final String PROP_USERNAME =          createQNameString(NAMESPACE_CONTENT, "userName");
  public static final String PROP_USER_FIRSTNAME =    createQNameString(NAMESPACE_CONTENT, "firstName");
  public static final String PROP_USER_MIDDLENAME =   createQNameString(NAMESPACE_CONTENT, "middleName");
  public static final String PROP_USER_LASTNAME =     createQNameString(NAMESPACE_CONTENT, "lastName");
  public static final String PROP_USER_ORGID =        createQNameString(NAMESPACE_CONTENT, "organizationId");


  // team props
  public static final String PROP_TEAM_NAME = createQNameString(NAMESPACE_CAT, "teamName");

  // subscriptions and alerts
  public static final String PROP_SUB_SEARCH_QUERY = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, "query");
  public static final String PROP_SUB_SEARCH_CHANGE_TYPE = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, "changeType");
  
  //web view prop
  public static final String PROP_WEB_VIEW_URL = createQNameString(NAMESPACE_VELO, "url");

  // frequencies for subscriptions and alerts
  public static final String SUBSCRIPTION_FREQ_HOURLY = "Hourly";
  public static final String SUBSCRIPTION_FREQ_DAILY = "Daily";
  public static final String SUBSCRIPTION_FREQ_WEEKLY = "Weekly";

  // owner types
  public static final String SUBSCRIPTION_OWNER_ACCOUNT_USER = "user";

  // delivery channels
  public static final String SUBSCRIPTION_CHANNEL_REPOSITORY = createQNameString(NAMESPACE_SUBSCRIPTION, "repository");
  public static final String SUBSCRIPTION_CHANNEL_EMAIL = createQNameString(NAMESPACE_SUBSCRIPTION, "email");

  /** Change Types */
  public static final String SUBSCRIPTION_CHANGE_TYPE_NEW = "new";
  public static final String SUBSCRIPTION_CHANGE_TYPE_MODIFIED = "modified";
  public static final String SUBSCRIPTION_CHANGE_TYPE_DELETED = "deleted";
  public static final String SUBSCRIPTION_CHANGE_TYPE_EXPIRED = "expired";
  public static final String SUBSCRIPTION_CHANGE_TYPE_ABOUT_TO_EXPIRE = "expiring";
  
  /** XPath Constants - used for search only */
  public static final String XPATH_COMPANY_HOME = "/app:company_home";  
  public static final String XPATH_CAT_SYSTEM = "/sys:system/sys:cat";
  public static final String XPATH_TEMP_DOCUMENTS = "/app:company_home/cm:" + ISO9075.encode("Temporary Files");  
  public static final String XPATH_USER_DOCUMENTS = "/app:company_home/cm:" + ISO9075.encode("User Documents");
  public static final String XPATH_TEAM_DOCUMENTS = "/app:company_home/cm:" + ISO9075.encode("Team Documents");
  public static final String XPATH_REFERENCE_LIBRARY = "/app:company_home/cm:" + ISO9075.encode("Reference Library");
  public static final String XPATH_USER_HOME_FOLDER_TEMPLATE = 
    "/app:company_home/app:dictionary/app:space_templates/cm:" + ISO9075.encode("User Home Folder");
  public static final String XPATH_TEAM_HOME_FOLDER_TEMPLATE = 
    "/app:company_home/app:dictionary/app:space_templates/cm:" + ISO9075.encode("Team Home Folder");
  //used in RepositoryWebService to determine if a limited search is requested
  public static final String QUERY_LANG_LUCENE_LIMIT = "lucene_limit_";
  
  // Paths
  public static final String PATH_COMPANY_HOME = "/app:company_home";
  public static final String PATH_VELO = PATH_COMPANY_HOME + "/cm:Velo";
  public final static String PATH_PROJECTS = PATH_VELO + "/cm:projects";
  public final static String PATH_SITES = PATH_VELO + "/cm:sites";
  public static final String PATH_REFERENCE_LIBRARY = PATH_COMPANY_HOME + "/cm:Reference Library";
  public static final String PATH_USER_DOCUMENTS = PATH_COMPANY_HOME + "/cm:User Documents";
  public static final String PATH_TEAM_DOCUMENTS = PATH_COMPANY_HOME + "/cm:Team Documents";


  // Options for deleting resources when the resource has provenance associations to other resources
  // See Delete web script for more information on what options mean
  public static final String DELETE_OPTION_PREVENT = "prevent";
  public static final String DELETE_OPTION_FORCE = "force";
  public static final String DELETE_OPTION_ARCHIVE = "archive";

  // Attachment Mode parameters for Alfresco's download web service
  // TODO: do we need these - are we ever using this web service anymore?
  //public static final String ATTACHMENT_MODE_ATTACH = "attach";
  //public static final String ATTACHMENT_MODE_DIRECT = "direct";
  
  // The possible ICatCML commands from Alfresco's CML
  // TODO: at some point we may get rid of this or refactor to better match Velo web
  // service APIs
  public static final String CML_CREATE = "create";
  public static final String CML_ADD_ASPECT = "addAspect";
  public static final String CML_REMOVE_ASPECT = "removeAspect";
  public static final String CML_UPDATE = "update"; // setProperty
  public static final String CML_DELETE = "delete";
  public static final String CML_MOVE = "move";
  public static final String CML_COPY = "copy";
  public static final String WRITE_CONTENT = "writeContent";
  //private static final String ADD_CHILD = "addChild";  // not used by Velo
  //private static final String REMOVE_CHILD = "removeChild"; // not used by Velo
  //private static final String CREATE_ASSOCIATION = "createAssociation"; // not used by Velo
  //private static final String REMOVE_ASSOCIATION = "removeAssociation"; // not used by Velo
  
  // This is a transient property used only to pass event information back in the Resource list
  // returned by executeCml web script
  public static final String PROP_CML_COMMAND = createQNameString(NAMESPACE_VELO, "CMLCommand");
  
  public static final String NAME_PROCESS_BEAN = "nodeNameProcessor";

  
  /**
   * Construct the http url for any file node on the server - copied from Resource Service in order to be 
   * used without eclipse RCP
   * 
   * @param repositoryUrl
   * @param uuid
   * @param fileName
   * @param attachmentMode "attach" or "direct" where direct will attempt to load the file contents inside the browser
   * and attach will prompt the user to open or save the file
  
   * @return String
   */
  public static String getContentHttpUrl(String repositoryUrl, String uuid, String fileName, String attachmentMode) {
    StringBuffer buf = new StringBuffer(repositoryUrl);
    buf.append("/download/");
    buf.append(attachmentMode);
    buf.append("/workspace/SpacesStore/");
    buf.append(uuid);
    buf.append("/");
    buf.append(fileName);
//    buf.append("?property="); //paulo found this is not needed, alfresco will default to the cm:content property 
//    buf.append(PROP_CONTENT);
    return buf.toString();
  }
  
  /**
   * Helper function to create a qualified name string from a namespace URI and name
   * 
   * @param namespace     the namespace URI
   * @param name          the name
   * @return              String string
   */
  public static String createQNameString(String namespace, String name) {
    return "{" + namespace + "}" + name;
  }
  
  public static String convertPropertyValueToString(Object value) {
    String valueStr = value.toString();

    if(value instanceof String) {
      valueStr = (String)value;

    } else if (value instanceof Boolean) {
      valueStr = String.valueOf((Boolean)value);

    } else if (value instanceof Integer) {
      valueStr = String.valueOf((Integer)value);

    } else if (value instanceof Double) { 
      valueStr = String.valueOf((Double)value);

    } else if (value instanceof Date) {
      long epochTime = ((Date)value).getTime();
      valueStr = String.valueOf(epochTime);

    } else {
      System.out.println("Unrecognizable property type: " + value.getClass().getName() + " value not interpreted.");
    }
    return valueStr;
  }


}
