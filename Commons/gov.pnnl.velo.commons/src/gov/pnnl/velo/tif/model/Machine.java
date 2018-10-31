package gov.pnnl.velo.tif.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

// TODO: look at the xml in templates folder and makes sure this object
// matches
// TODO: remove references to MachineConfig

@XStreamAlias("machine")
public class Machine {
  public static final String CUSTOM_PARAM_ACCESSIBLE_FROM_VELO_SERVER = "accessibleFromVeloServer";
  
  /* TODO: This is just a temporary solution until we support the more robust and dynamic method of
   * detecting the authentication method via a web service call.  Until then, we will hard code
   * one authentication method per machine in the machine.xml file.
   */
  public static final String CUSTOM_PARAM_AUTHENTICATION_METHOD = "authentiationMethod";
  public static final String AUTH_METHOD_PASSWORD = "password";
  public static final String AUTH_METHOD_SSHKEY = "sshkey";
  public static final String AUTH_METHOD_PASSCODE = "passcode";  // i.e, OTP token
  
	private String name = "";
	private String fullDomainName = "";
	private String userHomeParent = "";
	private int nodes = 1;
	private int procsPerNode = 1;
	private Scheduler scheduler = new Scheduler();
	private List<Code> codes;
	private List<Parameter> customParameters;
	
	private List<ConnectionProtocol>connectionProtocols;
	private List<Service>services;
	
	/**
	 * Default constructor
	 */
	public Machine() {

	}

	/**
	 * Constructor
	 * 
	 * @param fullname
	 *            : full domain name
	 */
	public Machine(String fullname) {
	}

	public String getFullDomainName() {
		return fullDomainName;
	}

	public void setFullDomainName(String fullname) {
		this.fullDomainName = fullname;
		if ("".equals(name)) {
			// Only set if user hasn't set their own - don't override...
			if (fullname.indexOf(".") >= 0) {
				name = this.fullDomainName.substring(0, fullname.indexOf("."));
			} else {
				name = this.fullDomainName;
			}
		}
	}
	
	public boolean isServerSideMonitoringSupported() {
	  boolean serverSide = false;
	  // first check if we are running local
    if(!isLocalhost()) {
      // check if machine is accessible from velo server
      String paramStr = getCustomParameter(Machine.CUSTOM_PARAM_ACCESSIBLE_FROM_VELO_SERVER);

      if (paramStr != null) {
        try {
          boolean serverAccessible = Boolean.valueOf(paramStr);
          serverSide = serverAccessible;
        } catch (Throwable e) {
          e.printStackTrace();
          // if we have a problem, default to client-side launch
        }
      } 
    }
    return serverSide;
	}

	public String getUserHomeParent() {
		return userHomeParent;
	}

	public void setUserHomeParent(String userHomeParent) {
		this.userHomeParent = userHomeParent;
	}

	public String getName() {
		return name;
	}

	public void setName(String sname) {
		if (sname == null) {
			name = "";
		} else {
			name = sname;
		}
	}

	public int getNodes() {
		return nodes;
	}

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	public int getProcsPerNode() {
		return procsPerNode;
	}

	public void setProcsPerNode(int procsPerNode) {
		this.procsPerNode = procsPerNode;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	public List<Code> getCodes() {
		if (codes == null)
			codes = new ArrayList<Code>();
		return codes;
	}

	public void setCodes(List<Code> codes) {
		this.codes = codes;
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
	
	public ConnectionProtocol getConnectionProtocol(String type) {
	  ConnectionProtocol protocol = null;
	  for(ConnectionProtocol p : connectionProtocols) {
	    if(p.getType().equals(type)) {
	      protocol = p;
	      break;
	    }
	  }
	  return protocol;
	}
	
	public List<ConnectionProtocol> getConnectionProtocols() {
	  if(connectionProtocols == null) {
	    connectionProtocols = new ArrayList<ConnectionProtocol>();
	  }
    return connectionProtocols;
  }

  public void setConnectionProtocols(List<ConnectionProtocol> connectionProtocols) {
    this.connectionProtocols = connectionProtocols;
  }

  public Service getService(String serviceId) {
    Service service = null;
    for(Service s : services) {
      if(s.getId().equalsIgnoreCase(serviceId)) {
        service = s;
      }
    }
    return service;
  }
  
  public List<Service> getServices() {
    if(services == null) {
      services = new ArrayList<Service>();
    }
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }

  /**
	 * Determine if this machine is the same server we are running on
	 * @return
	 */
	public boolean isLocalhost() {
	  boolean localhost = false;
	  
    if (name == null ||name.trim().equals("")
        || name.equals("localhost") || name.equals("local")) {
      // localhost
      localhost = true;
      
    } else {
      String hostname = "";
      try {
        java.net.InetAddress localMachine = java.net.InetAddress
            .getLocalHost();
        hostname = localMachine.getHostName();
      } catch (java.net.UnknownHostException e) {
        // TODO: handle exception?
        e.printStackTrace();
      }

      if (hostname == name) {
        localhost = true;
      } 
    }
    return localhost;
	}

	// public HashMap<String, Queue> queues = new HashMap<String, Queue>();
	// protected HashMap<String, Code> codes = new HashMap<String, Code>();

	// public void setCode(String key, String fullpath)
	// {
	// if (fullpath == null || fullpath != null && "".equals(fullpath)) return;
	//
	// String mykey = key;
	// if (mykey == null) mykey = "default";
	// String exename = fullpath.substring(fullpath.lastIndexOf("/")+1);
	// Code code = new Code();
	// code.type = mykey;
	// code.exeName = exename;
	// code.exePath = fullpath.substring(0,fullpath.lastIndexOf("/")+1);
	// codes.put(mykey, code);
	// //MachineConfig.Code xxx = (MachineConfig.Code)codes.get(mykey);
	//
	// }
	//
	// public String getCodeName(String key){
	// String exename = "";
	// if(codes!=null){
	// Code code = codes.get(key);
	// if(code!=null) exename = code.exeName;
	// }
	// return exename;
	// }
	//
	// public String getCodePath(String key){
	// String path ="";
	// if(codes!=null){
	// Machine.Code code = (Machine.Code)codes.get(key);
	// if(code!=null) path = code.exePath;
	// }
	// return path;
	// }
	//
	// /**
	// * Return the full path for an executable (path and name) or "" if not
	// found.
	// */
	// public String getCode(String key)
	// {
	// String ret = "";
	// if (codes != null) {
	// Machine.Code code = (Machine.Code)codes.get(key);
	// if (code != null) ret = code.exePath + "/" + code.exeName;
	// }
	//
	// return ret;
	// }
	//
	// static public Machine parse(String data) throws
	// ParserConfigurationException, SAXException, IOException
	// {
	// Machine machine = null;
	// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	// DocumentBuilder db = dbf.newDocumentBuilder();
	// Document doc = db.parse(new StringBufferInputStream(data));
	// doc.getDocumentElement().normalize();
	// NodeList nodeLst = doc.getElementsByTagName("MachineDescriptor");
	//
	// // We are only expecting 1 of these.
	//
	// if (nodeLst.getLength() > 1) throw new
	// SAXException("Expecting just one machine configuration.");
	//
	// NamedNodeMap attr = null;
	// Node nameNode;
	// for (int s = 0; s < nodeLst.getLength(); s++) {
	// Node node = nodeLst.item(s);
	//
	// if( node.getNodeType() == Node.ELEMENT_NODE) {
	// machine = new Machine("");
	//
	// NodeList childList = node.getChildNodes();
	// for (int t = 0; t < childList.getLength(); t++) {
	// Node cld = childList.item(t);
	//
	// if( cld.getNodeType() == Node.ELEMENT_NODE) {
	//
	// if("name".equals(cld.getNodeName())) {
	// if (cld.getTextContent() != null) {
	// machine.setShortName(cld.getTextContent());
	// }
	//
	// } else if("fullDomainName".equals(cld.getNodeName())) {
	// machine.setFullName(cld.getTextContent());
	//
	// } else if("nodes".equals(cld.getNodeName())) {
	// machine.nodes = Integer.parseInt(cld.getTextContent());
	//
	// } else if("procsPerNode".equals(cld.getNodeName())) {
	// machine.ppn = Integer.parseInt(cld.getTextContent());
	//
	// } else if("scheduler".equals(cld.getNodeName())) {
	// // Parse name, allocation flag, path, killpath
	// // Kill path is an extra add on due to chinook having kill program in a
	// different path
	// attr = cld.getAttributes();
	// nameNode = attr.getNamedItem("name");
	// if (nameNode != null) machine.schedulerName = nameNode.getTextContent();
	// Node alloc = attr.getNamedItem("allocation");
	// String allocstr = alloc.getTextContent();
	// if (allocstr.equals("yes") || allocstr.equals("Yes"))
	// machine.schedulerAllocation = true;
	// Node path = attr.getNamedItem("path");
	// if (path != null) machine.schedulerPath = path.getTextContent();
	// Node kill = attr.getNamedItem("kill");
	// if (kill != null) machine.schedulerKill = kill.getTextContent();
	//
	// NodeList qnodes = doc.getElementsByTagName("queue");
	// if (qnodes.getLength() > 0) {
	// for (int q = 0; q < qnodes.getLength(); q++) {
	// Node qnode = qnodes.item(q);
	// Queue queue = new Queue();
	// attr = qnode.getAttributes();
	// Node aname = attr.getNamedItem("name");
	// Node defaultQ = attr.getNamedItem("default");
	// if (aname != null) queue.name = aname.getTextContent();
	// if(defaultQ !=null)
	// queue.defaultQueue = Boolean.valueOf(defaultQ.getTextContent());
	//
	// NodeList aqueue = qnode.getChildNodes();
	// for (int aq = 0; aq < aqueue.getLength(); aq++) {
	// Node aqnode = aqueue.item(aq);
	// if("timeLimit".equals(aqnode.getNodeName())) {
	// queue.maxTime = aqnode.getTextContent();
	// } else if("defaultTimeLimit".equals(aqnode.getNodeName())) {
	// queue.defaultTime = aqnode.getTextContent();
	// } else if("minNodes".equals(aqnode.getNodeName())) {
	// queue.minNodes = Integer.parseInt(aqnode.getTextContent());
	// } else if("maxNodes".equals(aqnode.getNodeName())) {
	// queue.maxNodes = Integer.parseInt(aqnode.getTextContent());
	// }
	// }
	// machine.queues.put(queue.name, queue);
	// }
	// }
	// } else if("code".equals(cld.getNodeName())) {
	// Code code = new Code();
	// //Parse code object consistenting of type, name, path
	// attr = cld.getAttributes();
	// nameNode = attr.getNamedItem("name");
	// if (nameNode != null) code.type = nameNode.getTextContent();
	// machine.codes.put(code.type, code);
	// NodeList codenodes = cld.getChildNodes(); // assuming all children are
	// queues
	// for (int c = 0; c < codenodes.getLength(); c++) {
	// Node cnode = codenodes.item(c);
	// if( cnode.getNodeType() == Node.ELEMENT_NODE) {
	// if("executableName".equals(cnode.getNodeName())) {
	// code.exeName = cnode.getTextContent();
	// } else if("executablePath".equals(cnode.getNodeName())) {
	// code.exePath = cnode.getTextContent();
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// return machine;
	// }
	// public class Code
	// {
	// public String type;
	// public String exeName;
	// public String exePath;
	//
	// public Code() {}
	// }

	@SuppressWarnings("unused")
	private void testToXml() {
		Code c = new Code();
		c.setId("agni_amanzis");
		c.setVersion("1.0");
		JobLaunching jl = new JobLaunching();
		c.setJobLaunching(jl);
		jl.setCommand("./Agni --infile=agni.xml");

		List<Parameter> params = new ArrayList<Parameter>();
		params.add(new Parameter("simulator", "amanzi"));

		jl.setJobHandlerParameters(params);

		Machine machine = new Machine();
		ArrayList<Code> codes = new ArrayList<Code>();
		codes.add(c);
		machine.setCodes(codes);
		machine.setFullDomainName("hopper.nersc.gov");
		machine.setName("Hopper");
		Scheduler scheduler = new Scheduler();
		ArrayList<Queue> queues = new ArrayList<Queue>();
		Queue q = new Queue();
		q.setDefaultTimeLimit("1:00:00");
		q.setTimeLimit("4:00:00");
		q.setMaxNodes(4);
		q.setMinNodes(1);
		scheduler.setQueues(queues);
		machine.setScheduler(scheduler);

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
		System.out.println("\n\n\n\n");

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
		System.out.println("\n\n\n\n");

		c_sr.merge(c);
		xstream.toXML(c_sr, System.out);

	}

	// TO TEST XSTREAM SERIALIZATION:
	public static void main(String[] args) {
    XStream xstream = new XStream();
    xstream.autodetectAnnotations(true);
    xstream.alias("machine", Machine.class);// sucks I have to do this in
                        // code instead of it being
                        // smart enough to use the
                        // annotation...
    Machine machine = (Machine) xstream
        .fromXML(new File("C:\\Eclipse\\Workspaces\\PremierNetwork\\gov.pnnl.bes.demo\\config\\machines\\hopper.xml"));

    System.out.println(machine.getFullDomainName());
	}
}
