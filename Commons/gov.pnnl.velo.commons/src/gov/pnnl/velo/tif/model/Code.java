package gov.pnnl.velo.tif.model;

import gov.pnnl.velo.tif.service.CodeRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("code")
public class Code {

	@XStreamAsAttribute
	private String id;

	@XStreamAsAttribute
	private String version;

	@XStreamAsAttribute
	@XStreamAlias("parent")
	private String parentCodeId;

	@XStreamAsAttribute
	private String parentVersion;

	private String name;

	private String description;

	private String type;

	// Job Launching specifications
	private JobLaunching jobLaunching;

	// Ad hoc parameters that could be associated with the code
	private List<Parameter> customParameters;

	/**
	 * Default constructor
	 */
	public Code() {

	}

	/**
	 * For now we only merge the JobLaunching section TODO should i merge the
	 * subclass code into this one? Or create a new code object that will be the
	 * super and sub-codes merged together and return it?
	 * 
	 * @param parentCode
	 */
	public void merge(Code parentCode) {
		if (jobLaunching == null) {
			jobLaunching = parentCode.getJobLaunching();
			
		} else if (parentCode.getJobLaunching() != null) {
		  // only merge the other code's job launching section if it exists
			jobLaunching.merge(parentCode.getJobLaunching());
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String getParentCodeId() {
		return parentCodeId;
	}

	public void setParentCodeId(String parentCodeId) {
		this.parentCodeId = parentCodeId;
	}

	public String getParentVersion() {
		return parentVersion;
	}

	public void setParentVersion(String parentVersion) {
		this.parentVersion = parentVersion;
	}

	/**
	 * @return the jobLaunching
	 */
	public JobLaunching getJobLaunching() {
		return jobLaunching;
	}

	/**
	 * @param jobLaunching
	 *            the jobLaunching to set
	 */
	public void setJobLaunching(JobLaunching jobLaunching) {
		this.jobLaunching = jobLaunching;
	}

	public List<Parameter> getCustomParameters() {
		if (customParameters == null) {
			customParameters = new ArrayList<Parameter>();
		}
		return customParameters;
	}

	public void setCustomParameters(List<Parameter> customParameters) {
		this.customParameters = customParameters;
	}
	
	public String getCustomParameter(String name) {
      String value = null;
      if (customParameters != null) {
          for (Parameter param : customParameters) {
              if (param.getName().equals(name)) {
                  value = param.getValue();
                  break;
              }
          }
      }
      return value;
  }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((version == null || version == CodeRegistry.VERSION_DEFAULT) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Code other = (Code) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (version == null || version.equals(CodeRegistry.VERSION_DEFAULT)) {
			if (other.version != null && (!other.version.equals(CodeRegistry.VERSION_DEFAULT)))
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	public String getIdAndVersion(){
	  if(version == null || version.isEmpty() || version.equalsIgnoreCase(CodeRegistry.VERSION_DEFAULT)) {
      return id;
    }else{
      return id + "_" + version;
    }
	}

	@SuppressWarnings("unused")
	private void testToXml() {
		Code c = new Code();
		c.setId("agni_amanzis");
		c.setName("Agni Name");
		c.setDescription("blah blah blah");
		c.setVersion("1.0");
		JobLaunching jl = new JobLaunching();
		c.setJobLaunching(jl);
		jl.setCommand("./Agni --infile=agni.xml");
		jl.setJobHandlerId("srJobHandler");

		List<Fileset> inputs = new ArrayList<Fileset>();
		Fileset fileset = Fileset.createDir(".", "amanzi.xml,agni.xml,probin",
				null, null, null);
		inputs.add(fileset);

		jl.setLocalInputs(inputs);

		List<Fileset> outputs = new ArrayList<Fileset>();

		outputs.add(Fileset.createDir(".", "akunajob.out", null,
				"exitState=any", "copy"));
		outputs.add(Fileset.createDir(".", "bigOutputFile", null,
				"exitState=success", "link,archive"));
		outputs.add(Fileset.createDir(".", "*-out.xml", null, "exitState=any",
				"link"));

		jl.setOutputs(outputs);

		List<Fileset> runOutputs = new ArrayList<Fileset>();
		runOutputs.add(Fileset.createDir(".", "amanzi.xml", null,
				"exitState=any", "copy"));

		jl.setEnsembleConfiguration(new EnsembleConfiguration(runOutputs));

		List<Parameter> params = new ArrayList<Parameter>();
		params.add(new Parameter("runtimeMonitor",
				"org.akuna.ui.sr.AmanzisRuntimeMonitor"));
		params.add(new Parameter("simulator", "amanzi"));

		jl.setJobHandlerParameters(params);

		// using annotations instead so callers don't have to have a ton of
		// lines of xstream config each
		// time they are serializing/deserializing codes.
		XStream xstream = new XStream();

		// we can let xstream auto-detect annotations via
		// xstream.autodetectAnnotations(true);
		// but xstream's docs say there is ramificaitons, see
		// http://xstream.codehaus.org/annotations-tutorial.html#AutoDetect
		// xstream.processAnnotations(Code.class);
		// xstream.processAnnotations(Input.class);
		// xstream.processAnnotations(Output.class);
		xstream.autodetectAnnotations(true);
		xstream.toXML(c, System.out);
		System.out.println("/n/n/n/n");

		Code c_sr = new Code();
		JobLaunching c_jl = new JobLaunching();
		c_sr.setJobLaunching(c_jl);
		c_sr.setId("agni_amanzis_sr");
		c_sr.setParentCodeId("agni_amanzis");
		c_jl.setJobHandlerId("srJobHandler");
		List<Fileset> inputs2 = new ArrayList<Fileset>();
		inputs2.add(Fileset.createDir(".", "amanziSR.xml", null, null, null));
		c_jl.setLocalInputs(inputs2);
		c_jl.setMergeInputs(true);

		xstream.toXML(c_sr, System.out);
		System.out.println("/n/n/n/n");

		c_sr.merge(c);
		xstream.toXML(c_sr, System.out);

	}

	private void testFromXml() {
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		xstream.alias("code", Code.class);// sucks I have to do this in code
											// instead of it being smart enough
											// to use the annotation...
		Code amanzi = (Code) xstream
				.fromXML(new File(
						"/Users/d3x140/projects/ASCEM/workspace/gov.pnnl.velo.commons/templates/codes/agni.xml"));
		Code amanziSr = (Code) xstream
				.fromXML(new File(
						"/Users/d3x140/projects/ASCEM/workspace/gov.pnnl.velo.commons/templates/codes/agni_sr.xml"));

		List<Parameter> envParams = new ArrayList<Parameter>();
		amanzi.getJobLaunching().setEnvParameters(envParams);
		envParams.add(new Parameter("PATH", "/usr/X11/bin"));
		amanzi.getJobLaunching().getEnsembleConfiguration()
				.setRecordRuns(false);
		xstream.toXML(amanzi, System.out);
		System.out.println("\n\n\n\n");
		System.out.println("File set " + amanzi.getJobLaunching().getVeloServerInputs());
		System.out.println("File set " + amanzi.getJobLaunching().getVeloServerInputs().get(0).getDir());
		xstream.toXML(amanziSr, System.out);
		System.out.println("\n\n\n\n");
		System.out.println("BEFORE MERGE:"
				+ amanziSr.getJobLaunching().getLocalInputs());
		amanziSr.merge(amanzi);
		xstream.toXML(amanziSr, System.out);

	}

	// TO TEST XSTREAM SERIALIZATION:
	public static void main(String... args) {
		Code code = new Code();
		code.testFromXml();
	}
}
