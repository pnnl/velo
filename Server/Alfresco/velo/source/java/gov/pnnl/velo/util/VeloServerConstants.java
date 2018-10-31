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



import gov.pnnl.velo.tif.util.RegistryConstants;

import org.alfresco.service.namespace.QName;


/**
 */
public class VeloServerConstants extends RegistryConstants{
  
  /** Namespace constants */
  public static final String NAMESPACE_VELO = "http://www.pnl.gov/velo/model/content/1.0";
  public static final String NAMESPACE_ASCEM = NAMESPACE_VELO;
  public static final String NAMESPACE_VELO_PROVENANCE = "http://www.pnl.gov/velo/model/provenance/1.0";
  public static final String NAMESPACE_VELO_ADHOC="http://www.pnl.gov/velo/model/content/adhoc/1.0";
  public static final String NAMESPACE_CONTENT  = "http://www.alfresco.org/model/content/1.0";
  public static final String NAMESPACE_DATASET  = "http://www.pnl.gov/velo/model/dataset/1.0";
  public static final String NAMESPACE_SYSTEM   = "http://www.alfresco.org/model/system/1.0";


  //specific instance can define adhoc(user defined) properties using this prefix example - http://www.pnl.gov/velo/model/content/adhoc/fgen/1.0
  //only properties with namespace pattern will get sync-ed with wiki
  public static final String NAMESPACE_VELO_ADHOC_PREFIX="http://www.pnl.gov/velo/model/content/adhoc/";
  /** Prefix constants */
  static final String VELO_MODEL_PREFIX = "velo";
  static final String ASCEM_MODEL_PREFIX = VELO_MODEL_PREFIX;
  static final String ADHOC_MODEL_PREFIX = "adhoc";
     
  
  /** Types */
  
  /** Aspects */
  public static final QName ASPECT_TYPED_COLLECTION = QName.createQName(NAMESPACE_ASCEM, "typedCollection");
  public static final QName ASPECT_WIKI_IGNORE = QName.createQName(NAMESPACE_ASCEM, "wikiIgnore");
  public static final QName ASPECT_WIKI_SYNC = QName.createQName(NAMESPACE_ASCEM, "wikiSync");
  public static final QName ASPECT_COPYRIGHTED = QName.createQName(NAMESPACE_VELO, "copyrighted"); 
  public static final QName ASPECT_TEXT_TRANSFORM = QName.createQName(VeloConstants.ASPECT_TEXT_TRANSFORM);
  
  /** Properties */
  public static final QName PROP_UUID = QName.createQName(NAMESPACE_SYSTEM, "node-uuid");
  public static final QName PROP_NAME = QName.createQName(NAMESPACE_CONTENT, "name");
  public static final QName PROP_MIMEYPE = QName.createQName(NAMESPACE_ASCEM, "mimetype");
  public static final QName PROP_HAS_COPYRIGHT = QName.createQName(NAMESPACE_VELO, "hasCopyright");
  public static final QName PROP_HAS_LINK_CONTENT = QName.createQName(NAMESPACE_VELO, "hasLinkContent");
  public static final QName PROP_TEMPORARY_REMOTE_URL = QName.createQName(NAMESPACE_VELO, "tempRemoteUrl");
  public static final QName PROP_TEMPORARY_LINK_DESCRIPTION = QName.createQName(NAMESPACE_VELO, "tempLinkDescription");
  public static final QName PROP_TEMPORARY_LINK_TITLE = QName.createQName(NAMESPACE_VELO, "tempLinkTitle");
  public static final QName PROP_WIKI_IGNORE = QName.createQName(NAMESPACE_ASCEM, "wikiIgnore");
  public static final QName PROP_METADATA_FILE = QName.createQName(NAMESPACE_VELO, "metadataFile");
 
  public static final QName PROP_JOB_STATUS = QName.createQName(VeloTifConstants.JOB_STATUS);
  public static final QName PROP_JOB_STATUS_MESSAGE = QName.createQName(VeloTifConstants.JOB_STATUS_MESSAGE);  
  public static final QName PROP_JOB_SUBMIT_TIME = QName.createQName(VeloTifConstants.JOB_SUBMIT_TIME);
  public static final QName PROP_JOB_CODEID = QName.createQName(VeloTifConstants.JOB_CODEID);
  public static final QName PROP_JOB_JOBID = QName.createQName(VeloTifConstants.JOB_JOBID);
  public static final QName PROP_JOB_USER = QName.createQName(VeloTifConstants.JOB_USER);
  public static final QName PROP_JOB_ACCOUNT = QName.createQName(VeloTifConstants.JOB_ACCOUNT);
  public static final QName PROP_JOB_MACHINE = QName.createQName(VeloTifConstants.JOB_MACHINE);
  public static final QName PROP_JOB_PROC_COUNT = QName.createQName(VeloTifConstants.JOB_PROC_COUNT);
  public static final QName PROP_JOB_NODE_COUNT = QName.createQName(VeloTifConstants.JOB_NODE_COUNT);
  public static final QName PROP_JOB_START_TIME = QName.createQName(VeloTifConstants.JOB_START_TIME);
  public static final QName PROP_JOB_STOP_TIME = QName.createQName(VeloTifConstants.JOB_STOP_TIME);
  public static final QName PROP_JOB_TIME_LIMIT = QName.createQName(VeloTifConstants.JOB_TIME_LIMIT);
  public static final QName PROP_JOB_RUNDIR = QName.createQName(VeloTifConstants.JOB_RUNDIR);
  public static final QName PROP_JOB_QUEUE = QName.createQName(VeloTifConstants.JOB_QUEUE);
 
  
  public static final QName PROP_JOB_RUNS_PROGRESS = QName.createQName(VeloTifConstants.JOB_RUNS_PROGRESS);
  
  public static final QName PROP_JOB_RUNS_COMPLETED = QName.createQName(VeloTifConstants.JOB_RUNS_COMPLETED);
  
  public static final QName PROP_JOB_RUNS_FAILED = QName.createQName(VeloTifConstants.JOB_RUNS_FAILED);
  
  public static final QName PROP_JOB_HAS_FAILED_RUNS = QName.createQName(VeloTifConstants.JOB_HAS_FAILED_RUNS);
  
  //needed mainly for reconnect
  public static final QName PROP_JOB_POLL_INTERVAL = QName.createQName(VeloTifConstants.JOB_POLL_INTERVAL);;
  public static final QName PROP_JOB_OUTPUT_PREFERENCE = QName.createQName(VeloTifConstants.JOB_OUTPUT_PREFERENCE);
 
  public static final QName[] JOB_PROPS = {  PROP_JOB_JOBID, PROP_JOB_STATUS, PROP_JOB_USER, PROP_JOB_MACHINE, PROP_JOB_ACCOUNT, PROP_JOB_PROC_COUNT,
	    PROP_JOB_NODE_COUNT, PROP_JOB_TIME_LIMIT, PROP_JOB_RUNDIR, PROP_JOB_SUBMIT_TIME, PROP_JOB_START_TIME, PROP_JOB_STOP_TIME,
	    PROP_JOB_RUNS_PROGRESS, PROP_JOB_RUNS_COMPLETED, PROP_JOB_RUNS_FAILED, PROP_JOB_HAS_FAILED_RUNS,PROP_JOB_POLL_INTERVAL,PROP_JOB_OUTPUT_PREFERENCE,PROP_JOB_STATUS_MESSAGE}; 

  //Dataset properties
  public static final QName PROP_DOI = QName.createQName(NAMESPACE_DATASET, "doi");
  public static final QName PROP_DATASET_STATE = QName.createQName(NAMESPACE_DATASET, "state");
  public static final String DATASET_STATE_DRAFT = "draft";//draft or final
  public static final String DATASET_STATE_FINAL = "final";//draft or final
  public static final QName PROP_DATASET_PUBLISH_DATE = QName.createQName(NAMESPACE_DATASET, "publishDate");
  
  /** XPath Constants */
  public static final String XPATH_VELO = "/app:company_home/cm:Velo";
  public static final String XPATH_REFDATA = "/app:company_home/cm:Velo/cm:refdata";
  public static final String XPATH_USERMANUAL = "/app:company_home/cm:Velo/cm:refdata/cm:UserManualResources";
  public static final String XPATH_REGISTRY = "/app:company_home/cm:Velo/cm:Registry";
  public static final String XPATH_REGISTRY_CODES = "/app:company_home/cm:Velo/cm:Registry/cm:Codes";
  public static final String XPATH_REGISTRY_MACHINES = "/app:company_home/cm:Velo/cm:Registry/cm:Machines";
  public static final String XPATH_REGISTRY_SCRIPTS = "/app:company_home/cm:Velo/cm:Registry/cm:JobScripts";
  public static final String XPATH_PROJECTS = "/app:company_home/cm:Velo/cm:projects";
  public static final String XPATH_SPACE_TEMPLATES = "/app:company_home/app:dictionary/app:space_templates";  
  
  public static final String FOLDER_NAME_REGISTRY = "Registry";
  public static final String FOLDER_NAME_MACHINES = "Machines";
  public static final String FOlDER_NAME_CODES = "Codes";
  public static final String FOLDER_NAME_SCRIPTS = "JobScripts";

  

  
  //special file name which store key value pair metadata of 1 or more files
  //provided by user at the time of file upload.
  public static final String METADATA_FILENAME = "velo-metadata.properties";

}
