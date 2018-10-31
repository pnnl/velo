package gov.pnnl.velo.tif.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import gov.pnnl.velo.tif.service.CodeRegistry;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.tif.util.RegistryUtils;

/**
 * JobConfig is a data container for user selected parameters describing the
 * code and machine settings for a launch. It has some overlap with registry
 * information but is conceptually very distinct.
 */
public class JobConfig {
	private String name;

	// Job ID (optional - if the client wants to control the jobId)
	private String jobId;

	// Context parameters
	private File localWorkingDir;

	// contextPath will be an Alfresco path or local filesystem path if running
	// standalone (without alfresco)
	// If running without alfresco then localWorkingDir.getAbsolutePath() =
	// contextPath
	private String contextPath;
	// Only required if you are launching to web service
	// this is your credentials to your web server
	private String cmsUser;	
	
	private int pollingInterval = 10;

	// machine settings
	private Machine machine = null;
	//private String machineId;
	private String account = "";   // allocation account
	private Integer nodes;         // num nodes to request
	private Integer processors;    // num processors to use total (= nodes * procs per node)
	private String queueName = ""; // queue (partition)
	private String timeLimit = ""; // wall time
	private String memoryLimit;    // not supported
	private String remoteDir = ""; // the run dir we actually used (with timestamp)
	private String scratchDir = "";
	private String userName = "";  // compute server user login	
	private boolean doNotQueue = false;   // set it to true to run command line instead of submit to queue

	// ensemble config
	@Deprecated
	private Integer numberOfRuns;  // for backwards compatibility only
	private Integer numberOfTasks; // used for multi-tasked jobs - total number tasks to run
	private Integer tasksPerNode;  // how many tasks can run on a node at a given time
	@Deprecated
	private Integer procsPerRun;   // for backwards compatibility only
	private Integer procsPerTask;  // how many processors should each task use (optional)

	// code settings
	//private String codeId;
	//private String codeVersion;
	private Code code;
	private String command;
	private Map<String, String> jobHandlerParameters = new HashMap<String, String>();
	
	// Dynamic input/output files that may not come from the same place as the regular input files
	
  //files on velo server(alfresco)
	//This variable is not needed anymore since jobConfig now contains an instance of code object
	//instead of setting this variable, you could set jobConfig.code.joblaunching.veloServerInputs
	//But want to keep it backward compatible so leaving it here too. Adding new dynamicLocalInputFiles
  private List<String>dynamicInputFiles = new ArrayList<String>();
  //files on localhost which is running velo and launching job
  private List<File>dynamicLocalInputFiles = new ArrayList<File>();
  private List<Fileset>dynamicOutputFiles = new ArrayList<Fileset>();

  //this should be renamed to clientMonitoring
	private Boolean localMonitoring = null;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            full domain name
	 */
	public JobConfig(String name) {
		setName(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getLocalWorkingDir() {
		return localWorkingDir;
	}

	public void setLocalWorkingDir(File localWorkingDirectory) {
		this.localWorkingDir = localWorkingDirectory;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
	public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getCmsUser() {
    return cmsUser;
  }

  public void setCmsUser(String cmsUser) {
    this.cmsUser = cmsUser;
  }

  public int getPollingInterval() {
    return pollingInterval;
  }

  public void setPollingInterval(int pollingInterval) {
    this.pollingInterval = pollingInterval;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public Integer getNodes() {
    return nodes;
  }

  public void setNodes(Integer nodes) {
    this.nodes = nodes;
  }

  public Integer getProcessors() {
    return processors;
  }
  
  public void setProcessors(Integer processors) {
    this.processors = processors;
  }
  
  public Integer getTasksPerNode() {
    return tasksPerNode;
  }

  public void setTasksPerNode(Integer tasksPerNode) {
    this.tasksPerNode = tasksPerNode;
  }

  public Integer getProcsPerTask() {
    return procsPerTask;
  }

  public void setProcsPerTask(Integer procsPerTask) {
    this.procsPerTask = procsPerTask;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public String getTimeLimit() {
    return timeLimit;
  }

  public void setTimeLimit(String timeLimit) {
    this.timeLimit = timeLimit;
  }

  public String getMemoryLimit() {
    return memoryLimit;
  }

  public void setMemoryLimit(String memoryLimit) {
    this.memoryLimit = memoryLimit;
  }

  public String getRemoteDir() {
    return remoteDir;
  }

  public void setRemoteDir(String remoteDir) {
    this.remoteDir = remoteDir;
  }

  public String getScratchDir() {
    return scratchDir;
  }

  public void setScratchDir(String scratchDir) {
    this.scratchDir = scratchDir;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public boolean isDoNotQueue() {
    return doNotQueue;
  }

  public void setDoNotQueue(boolean doNotQueue) {
    this.doNotQueue = doNotQueue;
  }

  public Integer getNumberOfTasks() {
    return numberOfTasks;
  }

  public void setNumberOfTasks(Integer numberOfTasks) {
    this.numberOfTasks = numberOfTasks;
  }

  public String getMachineId() {
    if (machine!=null){
      return machine.getName();
    }else{
      return null;
    }
  }

  public void setMachineId(String machineId) {
     this.machine = RegistryUtils.getMachine(machineId);
    if (this.code!=null){
      Code mcode = RegistryUtils.getCodeForMachine(this.machine, this.code.getId(), this.code.getVersion());
      if(mcode!=null){
        this.code = mcode;
      }
    }
    
  }
  
  public Machine getMachine() {
    return machine;
  }

  public void setMachine(Machine machine){
    this.machine = machine;
    if (this.code!=null){
      Code mcode = RegistryUtils.getCodeForMachine(this.machine, this.code.getId(), this.code.getVersion());
      if(mcode!=null){
        this.code = mcode;
      }
    }
  }
  
  public String getCodeId() {
    if (this.code!=null){
      return this.code.getId();
    }else{
      return null;
    }
  }
  
  public String getCodeVersion() {
    if (this.code!=null){
      return this.code.getVersion();
    }else{
      return null;
    }
  }
  
  public Code getCode() {
    return code;
  }

  public void setCode(String codeId, String version) {
    code = TifServiceLocator.getCodeRegistry().get(codeId, version);
    setCode(code);
  }
  
  public void setCode(Code code){
    this.code = code;
    if (this.machine!=null){
      Code mcode = RegistryUtils.getCodeForMachine(this.machine, this.code);
      if(mcode!=null){
        this.code = mcode;
      }
    }
  }
  public void setCodeId(String codeId) {
    code = TifServiceLocator.getCodeRegistry().get(codeId, null);
    setCode(code);
  }
  
  public void setCodeVersion(String version) {
    if(this.code==null){
      throw new RuntimeException("Please use setCode or call setCodeId before setting code version");
    }
    code = TifServiceLocator.getCodeRegistry().get(this.code.getId(), version);
    setCode(code);
  }
  
  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public Map<String, String> getJobHandlerParameters() {
    return jobHandlerParameters;
  }

  public void setJobHandlerParameters(Map<String, String> jobHandlerParameters) {
    this.jobHandlerParameters = jobHandlerParameters;
  }

  public List<String> getDynamicInputFiles() {
    return dynamicInputFiles;
  }

  public void setDynamicInputFiles(List<String> dynamicInputFiles) {
    this.dynamicInputFiles = dynamicInputFiles;
  }
  
  public List<File> getDynamicLocalInputFiles() {
    return dynamicLocalInputFiles;
  }

  public void setDynamicLocalInputFiles(List<File> files) {
    this.dynamicLocalInputFiles = files;
  }


  public List<Fileset> getDynamicOutputFiles() {
    return dynamicOutputFiles;
  }

  public void setDynamicOutputFiles(List<Fileset> dynamicOutputFiles) {
    this.dynamicOutputFiles = dynamicOutputFiles;
  }

  public String getName() {
    return name;
  }

  public Boolean getLocalMonitoring() {
    return localMonitoring;
  }

  /**
   * @return the localMonitoring
   */
  public Boolean isLocalMonitoring() {
    
    if (localMonitoring == null) {
      
      Machine machine = TifServiceLocator.getMachineRegistry().get(this.machine.getName());
      localMonitoring = !machine.isServerSideMonitoringSupported();
    }
    return localMonitoring;
  }

  /**
   * @param localMonitoring the localMonitoring to set
   */
  public void setLocalMonitoring(Boolean localMonitoring) {
    this.localMonitoring = localMonitoring;
  }

  public boolean hasTime() {
		return !timeLimit.equals("");
	}

	/**
	 * Returns the time as an tuple hours, minutes, second. Caller should use
	 * hasTime() to see if there is a time. All 0s returned if not.
	 */
	public int[] getTimeParts() {
		int idx;
		int[] ret = new int[3];
		for (idx = 0; idx < 3; idx++)
			ret[idx] = 0;

		if (timeLimit != null && !timeLimit.equals("")) {
			String[] parts = timeLimit.split(":");
			for (idx = 0; idx < parts.length; idx++) {
				ret[idx] = Integer.parseInt(parts[idx]);
			}
		}
		return ret;
	}

	public String getTime() {
		return timeLimit;
	}

	public void setTime(int hours, int minutes, int seconds) {
		timeLimit = String.valueOf(hours) + ":" + String.valueOf(minutes) + ":"
				+ String.valueOf(seconds);
	}

	public String toString() {
		return this.machine.getName() + ":" + this.code.getId() + "_" + this.code.getVersion() +":" + name;
	}

	public static String toXml(JobConfig config) {
		// serialize to xml using xstream
		XStream xstream = getXStream();
		String xml = xstream.toXML(config);
		return xml;
	}

	public static JobConfig fromXml(String xml) {
		XStream xstream = getXStream();
		JobConfig config = (JobConfig) xstream.fromXML(xml);
		
		// TODO: backwards compatibility hacks for akuna - can remove once akuna project is done
		return makeBackwardsCompatible(config);
	}
	
	private static JobConfig makeBackwardsCompatible(JobConfig config) {
    
    String maxSims = config.getJobHandlerParameters().get("maxSimultaneousSimulations");
    if(config.tasksPerNode == null && maxSims != null) {
      config.setTasksPerNode(Integer.valueOf(maxSims));
    }
    // procsPerRun -> procsPerTask
    if(config.procsPerTask == null) {
      config.procsPerTask = config.procsPerRun;
    }
    // numberOfRuns -> numberOfTasks
    if(config.numberOfTasks == null) {
      config.numberOfTasks = config.numberOfRuns;
    }
    return config;
	}

	public static XStream getXStream() {
	  
		XStream xstream = new XStream();
		xstream.alias("JobConfig", JobConfig.class);
		xstream.useAttributeFor(JobConfig.class, "name");
//    xstream.useAttributeFor(Machine.class, "machineId");
//  	xstream.useAttributeFor(Code.class, "codeId");
//		xstream.useAttributeFor(Code.class, "codeVersion");
		xstream.registerConverter(new JobHandlerParametersMapConverter(xstream
		    .getMapper()));
		
		xstream.registerConverter(new JobConfigConverter(xstream.getMapper(), xstream.getReflectionProvider()));
		
		// Backwards compatibility
		// got rid of procsPerNode as this is a machine prop and shouldn't be saved in job config
    // added tasksPerNode   
		xstream.ignoreUnknownElements();
		
		return xstream;
	}

	public static class JobHandlerParametersMapConverter extends MapConverter {

		public JobHandlerParametersMapConverter(Mapper mapper) {
			super(mapper);
		}

		@SuppressWarnings("rawtypes")
		public boolean canConvert(Class type) {
			return type == HashMap.class;
		}

		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			Map<String, String> map = (Map<String, String>) source;
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String value = entry.getValue();
				// Only marshal the node if the value is not null
				if(value != null) {
					writer.startNode("parameter");
					writer.addAttribute("name", entry.getKey());
					context.convertAnother(value);
					writer.endNode();
				}
			}
		}

		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			Map<String, String> map = new HashMap<String, String>();
			populateStringMap(reader, context, map);

			return map;
		}

		protected void populateStringMap(HierarchicalStreamReader reader,
				UnmarshallingContext context, Map<String, String> map) {
			while (reader.hasMoreChildren()) {
				reader.moveDown();
				String key = reader.getAttribute("name");
				String value = reader.getValue();
				reader.moveUp();
				map.put(key, value);
			}
		}
	}

	
	public static class JobConfigConverter extends ReflectionConverter {

	  public JobConfigConverter(Mapper mapper,
        ReflectionProvider reflectionProvider) {
	    super(mapper, reflectionProvider);
    }
    public boolean canConvert(Class clazz) {
            return clazz.equals(JobConfig.class);
    }

    public void marshal(Object value, HierarchicalStreamWriter writer,
                    MarshallingContext context) {
      JobConfig c = (JobConfig)value;
      writer.addAttribute("machineId", c.getMachineId());
      writer.addAttribute("codeId", c.getCodeId());
      writer.addAttribute("codeVersion", c.getCodeVersion());
      super.marshal(value, writer, context);
    }

}
	 

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    // Create job config
    JobConfig conf = new JobConfig("hopper_agni-small-job");
    Code c = new Code();
    c.setId("agni");
    c.setVersion("1.0");
    Machine h = new Machine();
    h.setName("hopper");
    h.setFullDomainName("hopper.nersc.gov");
    conf.setMachine(h);
    conf.setCode(c);
    conf.setRemoteDir("/global/homes/v/vfreedma/scratch/amanzi/Richards-1D-transport");
    conf.setAccount("m1012");
    conf.setUserName("vfreedma");
    conf.setProcessors(240);
    conf.setQueueName("debug");
    conf.setTime(0, 30, 0);
    conf.setCommand("/project/projectdirs/m1012/agni/install/current/bin/Agni/Agni --infile=agni.xml");
    conf.getJobHandlerParameters().put("simulatorCommand",
        "/project/projectdirs/m1012/amanzi/install/current/bin/amanzi");

    String xml = toXml(conf);
    System.out.println(xml);
    
        JobConfig newConfig = fromXml(xml);
        //System.out.println(newConfig.getName());

//    try {
//      File jobConfigFile = new File("C:\\Projects\\Akuna\\test files\\_savedConfig.xml");
//      String xml = FileUtils.readFileAsString(jobConfigFile);      
//      JobConfig config = fromXml(xml);
//      System.out.println(config.getCode().getId());
//    } catch(Throwable e) {
//      e.printStackTrace();
//    }
  }
}
