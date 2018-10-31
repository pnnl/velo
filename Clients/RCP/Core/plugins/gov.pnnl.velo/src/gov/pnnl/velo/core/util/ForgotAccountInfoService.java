package gov.pnnl.velo.core.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.AbstractWebScriptClient;
import gov.pnnl.cat.core.util.WebServiceUrlUtility;

public class ForgotAccountInfoService extends AbstractWebScriptClient {

  private static ForgotAccountInfoService instance = new ForgotAccountInfoService();

  public ForgotAccountInfoService() {
    super(ResourcesPlugin.getResourceManager().getRepositoryUrlBase(), ResourcesPlugin.getProxyConfig());
  }

  public static ForgotAccountInfoService getInstance() {
    return instance;
  }

  public void resetPassword(String usernameOrEmail, String siteName, String fromEmailAddress) {
    StringBuilder url = getVeloWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "resetPassword");
    WebServiceUrlUtility.appendParameter(url, "sitename", siteName);
    if(fromEmailAddress != null){
      WebServiceUrlUtility.appendParameter(url, "fromEmail", fromEmailAddress);
    }
    if (usernameOrEmail.contains("@")) {
      WebServiceUrlUtility.appendParameter(url, "usersEmail", usernameOrEmail);
    } else {
      WebServiceUrlUtility.appendParameter(url, "username", usernameOrEmail);
    }
    
    executeGetMethod(url);
  }
  
  public void resetPassword(String usernameOrEmail) {
    resetPassword(usernameOrEmail, ResourcesPlugin.getResourceManager().getRepositoryUrlBase(), null);
  }

  public void emailUsername(String email, String sitename, String fromEmailAddress) {
    StringBuilder url = getVeloWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "emailUsername");
    WebServiceUrlUtility.appendParameter(url, "usersEmail", email);
    WebServiceUrlUtility.appendParameter(url, "sitename", sitename);
    if(fromEmailAddress != null){
      WebServiceUrlUtility.appendParameter(url, "fromEmail", fromEmailAddress);
    }
    executeGetMethod(url);
  }
  
  public void emailUsername(String email) {
    emailUsername(email, ResourcesPlugin.getResourceManager().getRepositoryUrlBase(), null);
  }
  
  
  private void executeGetMethod(StringBuilder url) {
    CloseableHttpResponse response = null;
    try {
      String responseMessage = "";
      HttpGet httpget = new HttpGet(url.toString());
      try {
        CloseableHttpResponse httpResponse = httpClient.execute(httpget); // executing this http method ourselves instead of having parent class do it since this request is UNauthenticated

        int status = httpResponse.getStatusLine().getStatusCode();
        if (status >= 400) {
          // log what the response message was
          responseMessage = getResponseBodyAsString(httpResponse);
        }
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
      if (!responseMessage.isEmpty()) {
        throw new RuntimeException(responseMessage);
      }
    } finally {
      closeQuietly(response);
    }
  }
}
