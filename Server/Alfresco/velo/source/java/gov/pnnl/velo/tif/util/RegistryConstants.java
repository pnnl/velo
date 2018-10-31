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
package gov.pnnl.velo.tif.util;

/**
 */
public class RegistryConstants {

  /** Namespace constants */
  public static final String NAMESPACE_REGISTRY = "http://www.pnnl.gov/velo/model/registry/1.0";

  /** Prefix constants */
  static final String REGISTRY_MODEL_PREFIX = "reg";

  /** Mimetypes **/
  public static final String MIMETYPE_REGISTRY_OBJECT_CONTENT = "registryObjectContent/regml+xml";
  public static final String MIMETYPE_REGISTRY_ROOT = "registryContainer/root";
  public static final String MIMETYPE_MACHINE = "registryObject/machine";
  public static final String MIMETYPE_MACHINE_CONTAINER = "registryContainer/machines";
  public static final String MIMETYPE_DATASET = "registryObject/dataSet";
  public static final String MIMETYPE_DATASET_CONTAINER = "registryContainer/dataSets";
  public static final String MIMETYPE_SCRIPT_TEMPLATE_CONTAINER = "registryContainer/scriptTemplates";
  public static final String MIMETYPE_SCRIPT_TEMPLATE = "registryObject/scriptTemplate";
  public static final String MIMETYPE_CODE = "registryObject/code";
  public static final String MIMETYPE_CODE_CONTAINER = "registryContainer/codes";
  public static final String MIMETYPE_TOOL_INSTANCE = "registryObjectInstance/code";

  /** Types */

  /** Aspects */
  public static final String ASPECT_REGISTRY_OBJECT = createQNameString(NAMESPACE_REGISTRY, "registryObject");
  public static final String ASPECT_MACHINE = createQNameString(NAMESPACE_REGISTRY, "machine");
  public static final String ASPECT_TOOL = createQNameString(NAMESPACE_REGISTRY, "tool");
  public static final String ASPECT_TOOL_INSTANCE = createQNameString(NAMESPACE_REGISTRY, "toolInstance");
  public static final String ASPECT_TOOL_CONTAINER = createQNameString(NAMESPACE_REGISTRY, "toolContainer");
  public static final String ASPECT_DATA_SET = createQNameString(NAMESPACE_REGISTRY, "dataSet");
  public static final String ASPECT_DATA_SET_CONTAINER = createQNameString(NAMESPACE_REGISTRY, "dataSetContainer");
  public static final String ASPECT_MEDICI_ENABLED = createQNameString(NAMESPACE_REGISTRY, "mediciEnabled");
  public static final String ASPECT_ARCHIVE_ENABLED = createQNameString(NAMESPACE_REGISTRY, "archiveEnabled");
  
  /** General Properties */
  public static final String PROP_URI = createQNameString(NAMESPACE_REGISTRY, "uri");
  public static final String PROP_VERSION_NUMBER = createQNameString(NAMESPACE_REGISTRY, "versionNumber");
  public static final String PROP_VERSION_COMMENT = createQNameString(NAMESPACE_REGISTRY, "versionComment");

  /** Properties for the oascis content type (what the serialized xml of the object is stored in) */
  public static final String PROP_OASCIS_CONTENT = createQNameString(NAMESPACE_REGISTRY, "content");

  /** Model properties **/
  public static final String PROP_MODEL_TYPE = createQNameString(NAMESPACE_REGISTRY, "modelType");
  
  /** Tool properties **/
  public static final String PROP_TOOL_CODE_ID = createQNameString(NAMESPACE_REGISTRY, "codeID");
  public static final String PROP_TOOL_SUPPORTED_OS = createQNameString(NAMESPACE_REGISTRY, "supportedOperatingSystems");
  public static final String PROP_TOOL_SUPPORTED_ARCH = createQNameString(NAMESPACE_REGISTRY, "supportedArchitectures");
  public static final String PROP_TOOL_URI = PROP_URI;
  public static final String PROP_TOOL_EXE_PATH = createQNameString(NAMESPACE_REGISTRY, "exePath");
  public static final String PROP_TOOL_EXECUTABLE = createQNameString(NAMESPACE_REGISTRY, "executable");
  public static final String PROP_TOOL_RECORD_RUNS = createQNameString(NAMESPACE_REGISTRY, "recordRuns");
  public static final String PROP_TOOL_OUTPUTS = createQNameString(NAMESPACE_REGISTRY, "outputs");
  public static final String PROP_TOOL_DEFAULT_OUTPUTS = createQNameString(NAMESPACE_REGISTRY, "defaultOutputs");
  public static final String PROP_TOOL_INPUTS = createQNameString(NAMESPACE_REGISTRY, "inputs");
  public static final String PROP_TOOL_REMOTE_INPUTS = createQNameString(NAMESPACE_REGISTRY, "remoteInputs");
  public static final String PROP_TOOL_DEFAULT_JOB_HANDLER = createQNameString(NAMESPACE_REGISTRY, "jobHandler");
  
  // properties that will be metadata extracted
  public static final String[] TOOL_EXTRACTED_PROPERTIES = {
    PROP_TOOL_CODE_ID,
    PROP_TOOL_SUPPORTED_OS,
    PROP_TOOL_SUPPORTED_ARCH,
    PROP_TOOL_URI,
    PROP_TOOL_EXE_PATH,
    PROP_TOOL_EXECUTABLE,
    PROP_TOOL_RECORD_RUNS,
    //PROP_TOOL_REMOTE_INPUTS,
    //PROP_TOOL_OUTPUTS,
    //PROP_TOOL_DEFAULT_OUTPUTS,
    PROP_TOOL_INPUTS,
    PROP_TOOL_DEFAULT_JOB_HANDLER
  };

  /** Data Set properties **/
  public static final String PROP_DATA_SET_TYPE = createQNameString(NAMESPACE_REGISTRY, "dataSetType");

  public static final String[] EXTRACTED_DATA_SET_PROPERTIES = {
    PROP_DATA_SET_TYPE
  };
  
  /** Machine properties **/
  public static final String PROP_MACHINE_ACTIVE = createQNameString(NAMESPACE_REGISTRY, "active"); 
  public static final String PROP_MACHINE_INACTIVE_EXPLANATION = createQNameString(NAMESPACE_REGISTRY, "inactiveExplanation"); 
  public static final String PROP_MACHINE_ALLOCATION_ACCOUNT_REQUIRED = createQNameString(NAMESPACE_REGISTRY, "allocationAccountRequired"); 
  public static final String PROP_MACHINE_SCHEDULER_NAME = createQNameString(NAMESPACE_REGISTRY, "schedulerName"); 
  public static final String PROP_MACHINE_SCHEDULER_PATH = createQNameString(NAMESPACE_REGISTRY, "schedulerPath"); 
  public static final String PROP_MACHINE_SCHEDULER_KILL = createQNameString(NAMESPACE_REGISTRY, "schedulerKill"); 
  public static final String PROP_MACHINE_OS = createQNameString(NAMESPACE_REGISTRY, "os");
  public static final String PROP_MACHINE_NUM_NODES = createQNameString(NAMESPACE_REGISTRY, "numNodes");
  public static final String PROP_MACHINE_PROCESSORS_PER_NODE = createQNameString(NAMESPACE_REGISTRY, "processorsPerNode");
  public static final String PROP_MACHINE_FULL_DOMAIN_NAME = createQNameString(NAMESPACE_REGISTRY, "fullDomainName");
  public static final String PROP_MACHINE_IP = createQNameString(NAMESPACE_REGISTRY, "ip");
  public static final String PROP_MACHINE_QUEUES = createQNameString(NAMESPACE_REGISTRY, "queues");
  public static final String PROP_MACHINE_CODES = createQNameString(NAMESPACE_REGISTRY, "codes");
  public static final String PROP_MACHINE_USER_HOME_PATH = createQNameString(NAMESPACE_REGISTRY, "userHomePath");
  public static final String PROP_MACHINE_MEDICI_ROOT_PATH = createQNameString(NAMESPACE_REGISTRY, "mediciRootPath");
  public static final String PROP_MACHINE_ARCHIVE_ROOT_PATH = createQNameString(NAMESPACE_REGISTRY, "archiveRootPath");
  
  public static final String[] EXTRACTED_MACHINE_PROPERTIES = {
    PROP_MACHINE_ACTIVE, 
    PROP_MACHINE_INACTIVE_EXPLANATION, 
    PROP_MACHINE_ALLOCATION_ACCOUNT_REQUIRED, 
    PROP_MACHINE_SCHEDULER_NAME, 
    PROP_MACHINE_SCHEDULER_PATH, 
    PROP_MACHINE_SCHEDULER_KILL, 
    PROP_MACHINE_OS, 
    PROP_MACHINE_NUM_NODES, 
    PROP_MACHINE_PROCESSORS_PER_NODE, 
    PROP_MACHINE_FULL_DOMAIN_NAME ,
    PROP_MACHINE_IP ,
    PROP_MACHINE_QUEUES, 
    PROP_MACHINE_CODES, 
    PROP_MACHINE_USER_HOME_PATH, 
    PROP_MACHINE_MEDICI_ROOT_PATH,
    PROP_MACHINE_ARCHIVE_ROOT_PATH
  };
  
  /** Assocations */
  public static final String ASSOC_HAS_PARENT_TOOL = createQNameString(NAMESPACE_REGISTRY, "hasParentTool");
  public static final String ASSOC_HAS_VERSION_CONTAINER = createQNameString(NAMESPACE_REGISTRY, "hasVersionContainer");
  public static final String ASSOC_HAS_PERVIOUS_VERSION = createQNameString(NAMESPACE_REGISTRY, "hasPreviousVersion");
  public static final String ASSOC_RUNS_ON_MACHINE = createQNameString(NAMESPACE_REGISTRY, "runsOnMachine");
  public static final String ASSOC_RAN_ON_MACHINE = createQNameString(NAMESPACE_REGISTRY, "ranOnMachine");
  
  /**
   * Helper function to create a qualified name string from a namespace URI and name
   * 
   * @param namespace     the namespace URI
   * @param name          the name
  
   * @return              String string */
  public static String createQNameString(String namespace, String name) {
    return "{" + namespace + "}" + name;
  }
}
