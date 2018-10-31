package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.server.webservice.alert.AlertWebService;
import gov.pnnl.cat.server.webservice.alert.RepositoryAlert;

import java.io.File;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;



public class GetAlerts extends AbstractCatWebScript {
  

  private AlertWebService alertWebService;
  
  public void setAlertWebService(AlertWebService alertWebService) {
    this.alertWebService = alertWebService;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    
    ObjectMapper mapper = new ObjectMapper();

    RepositoryAlert[] alerts = alertWebService.getAlerts();

    mapper.writeValue(res.getOutputStream(), alerts);
    return null;

  }

  
}
