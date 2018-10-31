package gov.pnnl.velo.tif.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("jobLaunching")
public class JobLaunching {

	public static final String ACTION_COPY = "copy";
	public static final String ACTION_LINK = "link";
	//public static final String ACTION_ARCHIVE = "archive"; // not supported yet. 

	public static final String EXIT_STATE_KEY = "exitState";
	public static final String EXIT_STATE_ANY = "any";
	public static final String EXIT_STATE_SUCCESS = "success";
	public static final String EXIT_STATE_ERROR = "error";

	@XStreamAsAttribute
	private boolean mergeInputs;

	@XStreamAsAttribute
	private boolean mergeOutputs;

	@XStreamAsAttribute
	private boolean mergeJobHandlerParams;

	private String jobHandlerId;
	private String command;
	private List<Fileset> localInputs;
	private List<Fileset> veloServerInputs;
	private List<Fileset> remoteInputs;
	private List<Fileset> outputs;

	private EnsembleConfiguration ensembleConfiguration;
	private List<Parameter> jobHandlerParameters;
	private List<Parameter> envParameters; // may be needed if creating launch
											// script or launching locally
											// without launch script

	/**
	 * Default constructor
	 */
	public JobLaunching() {

	}

	/**
	 * TODO should i merge the subclass code into this one? Or create a new code
	 * object that will be the super and sub-codes merged together and return
	 * it?
	 * 
	 * @param parentCode
	 */
	public void merge(JobLaunching parentCode) {
		// any single-valued parameters will always be override, otherwise pull
		// values that aren't set in this
		// child code's fields from the parent's
		if (this.getCommand() == null || this.getCommand().isEmpty()) {
			this.setCommand(parentCode.getCommand());
		}

		if (this.getJobHandlerId() == null || this.getJobHandlerId().isEmpty()) {
			this.setJobHandlerId(parentCode.getJobHandlerId());
		}

		// if the child code didn't include an 'inputs' section, copy the inputs
		// from the parent
		if (this.getLocalInputs() == null || this.getLocalInputs().isEmpty()) {
			this.setLocalInputs(parentCode.getLocalInputs());

		} else if (this.mergeInputs) { // otherwise only include the parents'
										// input with this child's if merge is
										// true
			for (Fileset input : parentCode.getLocalInputs()) {
				// only add parent input if it is not already in my list
				if (!localInputs.contains(input)) {
					localInputs.add(input);
				}
			}
		}

		// if the child code didn't include an 'inputs' section, copy the inputs
		// from the parent
		if (this.getVeloServerInputs() == null
				|| this.getVeloServerInputs().isEmpty()) {
			this.setVeloServerInputs(parentCode.getVeloServerInputs());

		} else if (this.mergeInputs) { // otherwise only include the parents'
										// input with this child's if merge is
										// true
			for (Fileset input : parentCode.getVeloServerInputs()) {
				// only add parent input if it is not already in my list
				if (!veloServerInputs.contains(input)) {
					veloServerInputs.add(input);
				}
			}
		}

		// if the child code didn't include an 'inputs' section, copy the inputs
		// from the parent
		if (this.getRemoteInputs() == null || this.getRemoteInputs().isEmpty()) {
			this.setRemoteInputs(parentCode.getRemoteInputs());

		} else if (this.mergeInputs) { // otherwise only include the parents'
										// input with this child's if merge is
										// true
			for (Fileset input : parentCode.getRemoteInputs()) {
				// only add parent input if it is not already in my list
				if (!remoteInputs.contains(input)) {
					remoteInputs.add(input);
				}
			}
		}

		// if the child code didn't include an 'outputs' section, copy the
		// outputs from the parent
		if (this.getOutputs() == null || this.getOutputs().isEmpty()) {
			this.setOutputs(parentCode.getOutputs());
		} else if (this.mergeOutputs) { // otherwise only include the parents'
										// output with this child's if merge is
										// true
			for (Fileset output : parentCode.getOutputs()) {
				// only add parent output if it is not already in my list
				if (!outputs.contains(output)) {
					outputs.add(output);
				}
			}
		}

		// not even dealing with how to merge a child's "EnsembleConfiguration"
		// with a parents until
		// a use case is defined. For now, always copy the parent's
		// "EnsembleConfiguration" to the child (this)
		// this.setEnsembleConfiguration(parentCode.getEnsembleConfiguration());

		if (this.getEnsembleConfiguration() == null) {
			this.setEnsembleConfiguration(parentCode.getEnsembleConfiguration());
		} else if (this.mergeOutputs) { // otherwise only include the parents'
										// ensemble output with this child's if
										// merge is true
			this.getEnsembleConfiguration().merge(
					parentCode.getEnsembleConfiguration());
		}

		// if the child code didn't include an 'JobParams' section, copy the
		// JobParams from the parent
		if (this.jobHandlerParameters == null || this.jobHandlerParameters.isEmpty()) {
			this.setJobHandlerParameters(parentCode.getJobHandlerParameters());
		} else if (this.mergeJobHandlerParams) { // otherwise only include the
													// parents' JobParams with
													// this child's if merge is
													// true
			// this.getJobHandlerParameters().addAll(parentCode.getJobHandlerParameters());
			for (Parameter param : parentCode.getJobHandlerParameters()) {
				// only add parent output if it is not already in my list
    			if (jobHandlerParameters == null) {
    			  jobHandlerParameters = new ArrayList<Parameter>();
    	        }
				if (!jobHandlerParameters.contains(param)) {
					jobHandlerParameters.add(param);
				}
			}
		}

	}

	/**
   * @return the jobHandlerId
   */
  public String getJobHandlerId() {
    return jobHandlerId;
  }

  /**
   * @param jobHandlerId the jobHandlerId to set
   */
  public void setJobHandlerId(String jobHandlerId) {
    this.jobHandlerId = jobHandlerId;
  }

  /**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	public List<Fileset> getLocalInputs() {
		if (localInputs == null) {
			// we can't initialize in constructor because xstream sets values
			// via reflection
			localInputs = new ArrayList<Fileset>();
		}
		return localInputs;
	}

	public void setLocalInputs(List<Fileset> localInputs) {
		this.localInputs = localInputs;
	}

	public List<Fileset> getVeloServerInputs() {
		if (veloServerInputs == null) {
			// we can't initialize in constructor because xstream sets values
			// via reflection
			veloServerInputs = new ArrayList<Fileset>();
		}
		return veloServerInputs;
	}

	public void setVeloServerInputs(List<Fileset> veloServerInputs) {
		this.veloServerInputs = veloServerInputs;
	}

	public List<Fileset> getRemoteInputs() {
		if (remoteInputs == null) {
			// we can't initialize in constructor because xstream sets values
			// via reflection
			return new ArrayList<Fileset>();
		}
		return remoteInputs;
	}

	public void setRemoteInputs(List<Fileset> remoteInputs) {
		this.remoteInputs = remoteInputs;
	}

	public List<Fileset> getOutputs() {
		if (outputs == null) {
			return new ArrayList<Fileset>();
		}
		return outputs;
	}

	public void setOutputs(List<Fileset> outputs) {
		this.outputs = outputs;
	}

	public EnsembleConfiguration getEnsembleConfiguration() {
		return ensembleConfiguration;
	}

	public void setEnsembleConfiguration(EnsembleConfiguration config) {
		this.ensembleConfiguration = config;
	}

	public List<Parameter> getJobHandlerParameters() {
		if (jobHandlerParameters == null) {
			return new ArrayList<Parameter>();
		}
		return jobHandlerParameters;
	}

	/**
	 * TODO: hopefully we will only ever have a handful of params, but if this
	 * becomes a performance issue, we will need to add a transient parameter
	 * map for quick access or else change the parameters field to a map and
	 * then see if we can serialize it correctly via xstream
	 * 
	 * @param key
	 * @return
	 */
	public String getJobHandlerParameter(String name) {
		String value = null;
		if (jobHandlerParameters != null) {
			for (Parameter param : jobHandlerParameters) {
				if (param.getName().equals(name)) {
					value = param.getValue();
					break;
				}
			}
		}
		return value;
	}

	public void setJobHandlerParameters(List<Parameter> jobHandlerParameters) {
		this.jobHandlerParameters = jobHandlerParameters;
	}

	/**
	 * @return the envParameters
	 */
	public List<Parameter> getEnvParameters() {
		if (envParameters == null) {
			return new ArrayList<Parameter>();
		}
		return envParameters;
	}

	/**
	 * @param envParameters
	 *            the envParameters to set
	 */
	public void setEnvParameters(List<Parameter> envParameters) {
		this.envParameters = envParameters;
	}

	public boolean isMergeInputs() {
		return mergeInputs;
	}

	public void setMergeInputs(boolean mergeInputs) {
		this.mergeInputs = mergeInputs;
	}

	public boolean isMergeOutputs() {
		return mergeOutputs;
	}

	public void setMergeOutputs(boolean mergeOutputs) {
		this.mergeOutputs = mergeOutputs;
	}

	public boolean isMergeJobHandlerParams() {
		return mergeJobHandlerParams;
	}

	public void setMergeJobHandlerParams(boolean mergeJobParams) {
		this.mergeJobHandlerParams = mergeJobParams;
	}

	public static void main(String... args) {

	}
}
