package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.server.webservice.subscription.Subscription;
import gov.pnnl.cat.server.webservice.subscription.SubscriptionWebService;

import java.io.File;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;



public class CreateSubscription extends AbstractCatWebScript {
  
  private SubscriptionWebService subscriptionWebService;
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    
    ObjectMapper mapper = new ObjectMapper();

    Subscription subscription = mapper.readValue(requestContent, Subscription.class);
    subscriptionWebService.createSubscription(subscription);
    return null;

  }

  
  public void setSubscriptionWebService(SubscriptionWebService subscriptionWebService) {
    this.subscriptionWebService = subscriptionWebService;
  }
}
