package gov.pnnl.velo.tif.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("service")
public class Service {
  
  //<service id="dataPublisher" url="http://hopper.nersc.gov/publish" description=""/>
  @XStreamAsAttribute
  private String id;
  
  @XStreamAsAttribute
  private String url;
  
  @XStreamAsAttribute
  private String description;
  
  private List<Parameter> parameters;
  
  public Service(String id, String url, String description) {
    super();
    this.id = id;
    this.url = url;
    this.description = description;
  }
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
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
