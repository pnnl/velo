package gov.pnnl.velo.model;

import org.alfresco.webservice.types.Reference;

public class ImportUrlRequest {
  private String xml; 
  private Reference target;
  

  public ImportUrlRequest(){
    
  }
  
  public ImportUrlRequest(String xml, Reference target) {
    super();
    this.xml = xml;
    this.target = target;
  }
  public String getXml() {
    return xml;
  }
  public void setXml(String xml) {
    this.xml = xml;
  }
  public Reference getTarget() {
    return target;
  }
  public void setTarget(Reference target) {
    this.target = target;
  }
}
