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
package gov.pnnl.cat.util;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

/**
 * Constants class used by CAT embedded policy and actions.
 *
 * @version $Revision: 1.0 $
 */
public interface CatConstants {

  /** Namespace constants */
  public static final String NAMESPACE_VELO = "http://www.pnl.gov/velo/model/content/1.0";
  public static final String NAMESPACE_TAXONOMY = "http://www.pnl.gov/dmi/model/taxonomy/1.0";
  public static final String NAMESPACE_SME = "http://www.pnl.gov/dmi/model/sme/1.0";
  public static final String NAMESPACE_NOTIFICATION = "http://www.pnl.gov/dmi/model/notification/1.0";
  public static final String NAMESPACE_DMI = "http://bkc.llnl.gov/dmi/model/base/1.0";
  public static final String NAMESPACE_CAT = "http://www.pnl.gov/cat/model/content/1.0";
  public static final String NAMESPACE_TRANSFORM_TEXT = "http://www.pnl.gov/cat/model/transform/text/1.0";
  public static final String NAMESPACE_METADATA = "http://www.pnl.gov/cat/model/metadata/1.0";
  public static final String NAMESPACE_TAGTIMER = "http://www.pnl.gov/cat/model/tagtimer/1.0";
  public static final String NAMESPACE_CRIMINAL_INTEL = "http://www.pnl.gov/cat/model/criminal/1.0";
  
  /** Remote link and properties **/
  public static final QName ASPECT_REMOTE_LINK = QName.createQName(NAMESPACE_CAT, "remoteLink");
  public static final QName PROP_LINK_TITLE = ContentModel.PROP_TITLE;
  public static final QName PROP_LINK_URL = QName.createQName(NAMESPACE_CAT, "linkUrl");
  public static final QName PROP_LINK_DESCRIPTION = ContentModel.PROP_DESCRIPTION;  
  
  /** Thumbnail constants */
  static final String THUMBNAIL_MEDIUM = "medium";
  static final String THUMBNAIL_DOCLIB = "doclib";
  static final String THUMBNAIL_PREVIEW_PANE = "previewPaneImage";
  static final String THUMBNAIL_AVATAR = "avatar";
  static final String THUMBNAIL_IMGPREVIEW = "imgpreview";

  /** Prefix constants */
  static final String DMI_MODEL_PREFIX = "dmi";
  static final String TAX_MODEL_PREFIX = "tax";
  static final String NOT_MODEL_PREFIX = "not";
  static final String CAT_MODEL_PREFIX = "cat";
  static final String CONTENT_MODEL_PREFIX = NamespaceService.CONTENT_MODEL_PREFIX;
  static final String APP_MODEL_PREFIX = NamespaceService.APP_MODEL_PREFIX;
     
  /** Types */
  public static final QName TYPE_TEAM = QName.createQName(NAMESPACE_CAT, "team");
  public static final QName TYPE_THUMBNAIL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbnail");
  
  /** Set of Types used in nodeService.getChildAssocs */
  public static final Set<QName> FOLDER_TYPES = new HashSet<QName>(Arrays.asList(new QName[] { ContentModel.TYPE_FOLDER, 
      ContentModel.TYPE_CATEGORYROOT, ContentModel.TYPE_CATEGORY }));

  /** Aspects */
  public static final QName ASPECT_TAXONOMY_ROOT = QName.createQName(NAMESPACE_TAXONOMY, "taxonomyRoot");
  public static final QName ASPECT_TAXONOMY_FOLDER = QName.createQName(NAMESPACE_TAXONOMY, "taxonomyFolder");
  public static final QName ASPECT_TAXONOMY_LINK = QName.createQName(NAMESPACE_TAXONOMY, "taxonomyLink");
  public static final QName ASPECT_TAXONOMY_CLASSIFICATION = QName.createQName(NAMESPACE_TAXONOMY, "classification");
  public static final QName ASPECT_SME_DOMAIN_EXPERTISE = QName.createQName(NAMESPACE_SME, "domainExpertise");
  public static final QName ASPECT_SME_PRIMARY_DISCIPLINE = QName.createQName(NAMESPACE_SME, "primaryDiscipline");
  public static final QName ASPECT_SME_SECONDARY_DISCIPLINE = QName.createQName(NAMESPACE_SME, "secondaryDiscipline");
  //public static final QName ASPECT_NOTIFIABLE = QName.createQName(NAMESPACE_NOTIFICATION, "notifiable");
  public static final QName ASPECT_ISM = QName.createQName(NAMESPACE_DMI, "ISM");

  public static final QName ASPECT_TEXT_TRANSFORM = QName.createQName(NAMESPACE_TRANSFORM_TEXT, "transform");
  public static final QName ASPECT_HOME_FOLDER = QName.createQName(NAMESPACE_CAT, "homeFolder");
  public static final QName ASPECT_USER_HOME_FOLDER = QName.createQName(NAMESPACE_CAT, "userHomeFolder");
  public static final QName ASPECT_TEAM_HOME_FOLDER = QName.createQName(NAMESPACE_CAT, "teamHomeFolder");

  public static final QName ASPECT_PROJECT = QName.createQName(NAMESPACE_CAT, "project");
  public static final QName ASPECT_PROFILE = QName.createQName(NAMESPACE_CAT, "profile");
  public static final QName ASPECT_FAVORITES_ROOT = QName.createQName(NAMESPACE_CAT, "favoritesRoot");
  public static final QName ASPECT_PERSONAL_LIBRARY_ROOT = QName.createQName(NAMESPACE_CAT, "personalLibraryRoot");
  public static final QName ASPECT_IGNORE = QName.createQName(NAMESPACE_CAT, "ignore");

  public static final QName ASPECT_CONFIG_ROOT = QName.createQName(NAMESPACE_CAT, "configRoot");
  public static final QName ASPECT_IDENTIFIABLE = QName.createQName(NAMESPACE_CAT, "identifiable");
//  public static final QName ASPECT_NEEDS_FULL_TEXT_INDEXED = QName.createQName(NAMESPACE_CAT, "needsFullTextIndexed");
  
  public static final QName ASPECT_US_PRESONS_DATA = QName.createQName(NAMESPACE_TAGTIMER, "usPersonsData");
  public static final QName ASPECT_US_PRESONS_DATA_REMOVED = QName.createQName(NAMESPACE_TAGTIMER, "usPersonsDataRemoved");

  public static final QName ASPECT_CRIMINAL_INTEL = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "criminalIntelData");
  public static final QName ASPECT_CRIMINAL_INTEL_REMOVED = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "criminalIntelDataRemoved");
  public static final QName ASPECT_VALIDITABLE = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "validitable");
  
  /** Properties */
  public static final QName PROP_MIMEYPE = QName.createQName(NAMESPACE_VELO, "mimetype");
  public static final QName PROP_PARENT_MIMETYPE = QName.createQName(NAMESPACE_CAT, "parentMimetype");
  public static final QName PROP_THUMBNAIL_WIDTH = QName.createQName(NAMESPACE_CAT, "width");
  public static final QName PROP_THUMBNAIL_HEIGHT = QName.createQName(NAMESPACE_CAT, "height");
  public static final QName PROP_REDIRECT_FOLDER = QName.createQName(NAMESPACE_TAXONOMY, "redirectFolder");
  public static final QName PROP_DISSEMINATION_CONTROLS = QName.createQName(NAMESPACE_DMI, "disseminationControls");
  public static final QName PROP_CLASSIFICATION_LEVEL = QName.createQName(NAMESPACE_DMI, "classificationLevel");
  public static final QName PROP_COMPARTMENTS = QName.createQName(NAMESPACE_DMI, "compartments");
  public static final QName PROP_CATEGORIES = QName.createQName(NAMESPACE_TAXONOMY, "categories");
  public static final QName PROP_SME_CATEGORIES = QName.createQName(NAMESPACE_SME, "categories");

  public static final QName PROP_PRIMARY_PHONE_NUMBER = QName.createQName(NAMESPACE_CAT, "primaryPhoneNumber");  
  public static final QName PROP_IMAGE = QName.createQName(NAMESPACE_CAT, "profileImage");
    
  public static final QName PROP_TEAM_NAME = QName.createQName(NAMESPACE_CAT, "teamName"); 
  public static final QName PROP_USER_HOME_FOLDER = ContentModel.PROP_HOMEFOLDER;
  public static final QName PROP_TEAM_HOME_FOLDER = QName.createQName(NAMESPACE_CAT, "homeFolder");
  public static final QName PROP_DESCRIPTION = ContentModel.PROP_DESCRIPTION;
  public static final QName PROP_USER_EMAIL = ContentModel.PROP_EMAIL;
  public static final QName PROP_TEAM_EMAIL = QName.createQName(NAMESPACE_CAT, "email");
  public static final QName PROP_CLUSTERS = QName.createQName(NAMESPACE_CAT, "clusters");
  
  public static final QName PROP_TEXT_TRANSFORMER = QName.createQName(NAMESPACE_TRANSFORM_TEXT, "transformer");
  public static final QName PROP_TEXT_TRANSFORM_LABEL = QName.createQName(NAMESPACE_TRANSFORM_TEXT, "transformLabel");  
  public static final QName PROP_TEXT_TRANSFORMED_CONTENT = QName.createQName(NAMESPACE_TRANSFORM_TEXT, "transformedContent"); 
  public static final QName PROP_TEXT_TRANSFORM_ERROR = QName.createQName(NAMESPACE_TRANSFORM_TEXT, "transformError");
  public static final QName PROP_PIPELINE_ERROR = QName.createQName(NAMESPACE_TRANSFORM_TEXT, "pipelineError");
  public static final QName PROP_TEXT_NEEDS_TRANSFORM = QName.createQName(NAMESPACE_TRANSFORM_TEXT, "needsTransform");
  public static final QName PROP_NEEDS_FULL_TEXT_INDEXED = QName.createQName(NAMESPACE_CAT, "needsFullTextIndexed");

  public static final QName PROP_HASH = QName.createQName(NAMESPACE_CAT, "hash"); 
  
  public static final QName PROP_METADATA_ERROR = QName.createQName(NAMESPACE_METADATA, "metadataError");
  public static final QName PROP_METADATA_NEEDS_EXTRACTION = QName.createQName(NAMESPACE_METADATA, "needsMetadataExtracted");
  public static final QName PROP_SIZE = QName.createQName(NAMESPACE_CAT, "size");
  public static final QName PROP_THUMBNAIL = QName.createQName(NAMESPACE_CAT, "base64Thumbnail"); 
  public static final QName PROP_CHILD_COUNT = QName.createQName(NAMESPACE_CAT, "childCount");
  
  public static final QName PROP_CREATED_ON_BEHALF = QName.createQName(NAMESPACE_CAT, "createdOnBehalfOf");
  public static final QName PROP_MODIFIED_ON_BEHALF = QName.createQName(NAMESPACE_CAT, "modifiedOnBehalfOf");
  public static final QName PROP_METADATA_FILE = QName.createQName(NAMESPACE_VELO, "metadataFile");
  
  /** Name Constants */
  public static final QName NAME_TEMP_FOLDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Temporary Files");
  public static final QName NAME_TEAM_TEMP_FOLDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Team Temporary Files");
  public static final QName NAME_USER_DOCUMENTS = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "User Documents");
  public static final QName NAME_CONF_FOLDER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Configuration Files");
  
  /** XPath Constants */
  public static final String XPATH_COMPANY_HOME = "/app:company_home";  
  public static final String XPATH_CAT_SYSTEM = "/sys:system/sys:cat";
  public static final String XPATH_TEMP_DOCUMENTS = "/app:company_home/cm:" + ISO9075.encode("Temporary Files");  
  public static final String XPATH_CONFIG_FILES = "/app:company_home/cm:" + ISO9075.encode("Configuration Files");  
  public static final String XPATH_USER_DOCUMENTS = "/app:company_home/cm:" + ISO9075.encode("User Documents");
  public static final String XPATH_TEAM_DOCUMENTS = "/app:company_home/cm:" + ISO9075.encode("Team Documents");
  public static final String XPATH_TEAM_CONTAINER = "/sys:system/sys:teams";    
  public static final String XPATH_REFERENCE_LIBRARY = "/app:company_home/cm:" + ISO9075.encode("Reference Library");
  public static final String XPATH_USER_HOME_FOLDER_TEMPLATE = 
    "/app:company_home/app:dictionary/app:space_templates/cm:" + ISO9075.encode("User Home Folder");
  public static final String XPATH_TEAM_HOME_FOLDER_TEMPLATE = 
    "/app:company_home/app:dictionary/app:space_templates/cm:" + ISO9075.encode("Team Home Folder");
  public static final String XPATH_CONF = "/app:company_home/cm:conf";
  public static final String XPATH_TAXONOMY_CLASSIFICATION = "/cm:categoryRoot/tax:classification";
  public static final String XPATH_SME_CLASSIFICATION = "/cm:categoryRoot/sme:classification";
  public static final String XPATH_CATEGORY_ROOT = "/cm:categoryRoot";  
  //used in RepositoryWebService to determine if a limited search is requested
  public static final String QUERY_LANG_LUCENE_LIMIT = "lucene_limit_";
  
  /** Full Path Constants */
  public static final String FULL_PATH_TEAM_DOCUMENTS = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/content/1.0}Team Documents";
  
  /** Path Constants */
  public static final String PATH_COMPANY_HOME = "/company_home";
  public static final String PATH_TEMP_DOCUMENTS = PATH_COMPANY_HOME + "/Temporary Files";
  public static final String PATH_USER_DOCUMENTS = PATH_COMPANY_HOME + "/User Documents";
  public static final String PATH_TEAM_DOCUMENTS = PATH_COMPANY_HOME + "/Team Documents";
  public static final String PATH_REFERENCE_LIBRARY = PATH_COMPANY_HOME + "/Reference Library";
  public static final String PATH_USER_HOME_FOLDER_TEMPLATE =
    PATH_COMPANY_HOME + "/dictionary/space_templates/User Home Folder";
  public static final String PATH_TEAM_HOME_FOLDER_TEMPLATE =
    PATH_COMPANY_HOME + "/dictionary/space_templates/Team Home Folder";

  public static final String QPATH_SPACE_TEMPLATES = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}dictionary/{http://www.alfresco.org/model/application/1.0}space_templates/";
  
  /** reference to spaces store */
  public static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
  
  /** mimetypes that aren't in alfresco's MimetypeMap class **/
  public static final String MIMETYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  

  public static final String ROOT_VELO_AUDITING_PATH = "velo-access";

  
}
