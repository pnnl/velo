package gov.pnnl.velo.tif.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class defines a protocol by which a client can connect to the server
 */
@XStreamAlias("connectionProtocol")
public class ConnectionProtocol {
  public static final String TYPE_SSH = "ssh";
  public static final String TYPE_GLOBUS_ONLINE = "globusOnline";
  
  // Globus Online Endpoint ID
  public static final String PARAMETER_GLOBUS_ENDPOINT_ID = "endpointId";
  // Globus Online port
  public static final String PARAMTER_GLOBUS_ENDPOINT_MY_PROXY_HOST = "myProxyHost";
  // Path on network filesystem that is the same on globus endpoint and host machine
  // (e.g., /pic is same path on olympus and pic#dtn)
  public static final String PARAMETER_GLOBUS_ACCESSIBLE_PATH = "globusAccessiblePath"; // TODO: maybe need better name for this
  
  @XStreamAsAttribute
  private String type;
  private List<Parameter> parameters;
  
  public ConnectionProtocol(String type) {
    super();
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<Parameter> getParameters() {
    if(parameters == null) {
      parameters = new ArrayList<Parameter>();
    }
    return parameters;
  }

  public void setParameters(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  public String getParameter(String name) {
    String value = null;
    if (parameters != null) {
      for (Parameter param : parameters) {
        if (param.getName().equals(name)) {
          value = param.getValue();
          break;
        }
      }
    }
    return value;
  }
  
}
