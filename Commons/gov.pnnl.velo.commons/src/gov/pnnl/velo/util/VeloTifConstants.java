package gov.pnnl.velo.util;

/**
 * There are so many properties involving the registry and
 * job launching, they are in their own separate file.
 *
 */
public class VeloTifConstants extends VeloConstants {

  // Paths
  public static final String PATH_REGISTRY = "/app:company_home/cm:Velo/cm:Registry";
  public static final String PATH_REGISTRY_CODES = PATH_REGISTRY + "/cm:Codes";
  public static final String PATH_REGISTRY_MACHINES = PATH_REGISTRY + "/cm:Machines";
  public static final String PATH_REGISTRY_SCRIPTS = PATH_REGISTRY + "/cm:Script_Templates";

  /** 
   * Tool Instance Job Launch Properties
   */
  // State of the job: queue, running, error, etc.
  public static final String JOB_STATUS = createQNameString(NAMESPACE_VELO, "status"); 
  
  // Provides more detail about the status, like an error message
  public static final String JOB_STATUS_MESSAGE = createQNameString(NAMESPACE_VELO, "status_message");

  // The unique identifier of the Workflow/Code being run - so we can find the Workflow/Code Definition
  public static final String JOB_CODEID = createQNameString(NAMESPACE_VELO,"codeid");
  
  // The ID of the job
  public static final String JOB_JOBID = createQNameString(NAMESPACE_VELO,"jobid");
  
  // The user running the job
  public static final String JOB_USER = createQNameString(NAMESPACE_VELO, "jobuser");
  
  // The machine where it is running
  public static final String JOB_MACHINE = createQNameString(NAMESPACE_VELO, "jobmachine");
  
  // The allocation account for that machine (if it applies)
  public static final String JOB_ACCOUNT = createQNameString(NAMESPACE_VELO, "allocation_account");
  
  // The queue we are using (if the machine has a scheduler and we are using it)
  public static final String JOB_QUEUE = createQNameString(NAMESPACE_VELO, "queue_name");
  
  // The number of processors to run on (only applies if we are using scheduler)
  public static final String JOB_PROC_COUNT = createQNameString(NAMESPACE_VELO, "processors");
  
  // The number of processors per node (only applies if we are using scheduler)
  public static final String JOB_PROC_PER_NODE = createQNameString(NAMESPACE_VELO, "proc_per_node");
  
  // Number of nodes to run on (only applies if we are using scheduler)
  public static final String JOB_NODE_COUNT = createQNameString(NAMESPACE_VELO, "nodes");
  
  // Wall time (only applies if we are using scheduler)
  public static final String JOB_TIME_LIMIT = createQNameString(NAMESPACE_VELO, "timelimit");
  
  // The run directory on the remote machine where the job is running
  public static final String JOB_RUNDIR = createQNameString(NAMESPACE_VELO, "rundir");
  
  // When the job was submitted by Velo 
  public static final String JOB_SUBMIT_TIME = createQNameString(NAMESPACE_VELO, "submission_time");
  
  // When the job started running (will be different from submit time if we are using a queue)
  public static final String JOB_START_TIME = createQNameString(NAMESPACE_VELO, "start_time");
  
  // When the job completed
  public static final String JOB_STOP_TIME = createQNameString(NAMESPACE_VELO, "stop_time");
  //total time job ran for
  public static final String JOB_RUN_TIME = createQNameString(NAMESPACE_VELO, "job_run_time");
  
  
  // If the job has multiple runs (or sub-steps), how many sub-components are running
  public static final String JOB_RUNS_PROGRESS = createQNameString(NAMESPACE_VELO, "runs_in_progress");
  
  // If  the job has multiple runs (or sub-steps), how many sub-components have completed
  public static final String JOB_RUNS_COMPLETED = createQNameString(NAMESPACE_VELO, "runs_completed");
  
  // If  the job has multiple runs (or sub-steps), how many sub-components have failed
  public static final String JOB_RUNS_FAILED = createQNameString(NAMESPACE_VELO, "runs_failed");
  
  public static final String JOB_HAS_FAILED_RUNS = createQNameString(NAMESPACE_VELO, "has_failed_runs");
  
  //needed mainly for reconnect
  public static final String JOB_POLL_INTERVAL = createQNameString(NAMESPACE_VELO, "poll_interval");
  public static final String JOB_OUTPUT_PREFERENCE = createQNameString(NAMESPACE_VELO, "output_preference");
  
  public static final String[] JOB_PROPERTIES = { JOB_JOBID, JOB_STATUS, JOB_USER, JOB_MACHINE, JOB_ACCOUNT, JOB_PROC_COUNT, JOB_NODE_COUNT, JOB_TIME_LIMIT,
      JOB_RUNDIR, JOB_SUBMIT_TIME, JOB_START_TIME, JOB_STOP_TIME, JOB_RUNS_PROGRESS, JOB_RUNS_COMPLETED, JOB_RUNS_FAILED, JOB_HAS_FAILED_RUNS,
      JOB_POLL_INTERVAL, JOB_OUTPUT_PREFERENCE, JOB_STATUS_MESSAGE, JOB_CODEID, JOB_QUEUE, JOB_PROC_PER_NODE, JOB_RUN_TIME };

  /**
   * Values for job status
   */
  public static final String STATUS_CANCELLED = "Cancelled";
  public static final String STATUS_SUBMITTING = "Submitting";
  public static final String STATUS_WAIT = "In Queue";
  public static final String STATUS_KILLED = "Killed";
  //below 4 are also used for runs
  public static final String STATUS_START = "Running"; 
  public static final String STATUS_COMPLETE = "Complete"; // unknown exit code
  public static final String STATUS_SUCCESS = "Success";
  public static final String STATUS_ERROR = "Error";
  public static final String STATUS_FAILED = "Failed"; // it failed but we don't know why
  public static final String STATUS_POSTPROCESS = "Job Complete. Post-processing results";
  public static final String STATUS_RECONNECT = "Reconnecting";
  public static final String STATUS_DISCONNECTED = "Disconnected";

  // Default values for Inputs/Outputs folders
  public static final String FOLDER_NAME_INPUTS = "Inputs";
  public static final String FOLDER_NAME_OUTPUTS = "Outputs";
  public static final String FOLDER_NAME_ANALYSIS = "Analysis";
  public static final String FOLDER_NAME_RUNS = "Runs";
  
  
  // Job Handler custom properties
  public static final String JOB_HANDLER_PROP_OVERWRITE_REMOTE_DIR = "overwriteRemoteDir";
}
