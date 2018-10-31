package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.cat.core.util.AbstractWebScriptClient;
import gov.pnnl.cat.core.util.ProxyConfig;
import gov.pnnl.cat.core.util.WebServiceUrlUtility;
import gov.pnnl.velo.tif.model.Credentials;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.JobLaunchParameters;
import gov.pnnl.velo.tif.model.KeyboardInteractiveCredentials;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.model.SshKeyTransferCredentials;
import gov.pnnl.velo.tif.service.CredentialsPrompter;
import gov.pnnl.velo.tif.service.JobLaunchWebServices;
import gov.pnnl.velo.tif.service.TifServiceLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.kepler.ssh.AuthCancelException;
import org.kepler.ssh.AuthFailedException;
import org.kepler.ssh.VeloUserInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;

public class JobLaunchWebServicesImpl extends AbstractWebScriptClient implements JobLaunchWebServices {

  private static final Log logger = LogFactory.getLog(JobLaunchWebServicesImpl.class);

  public JobLaunchWebServicesImpl(String veloServerUrl, ProxyConfig proxyConfig) {
    super(veloServerUrl, proxyConfig);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchWebServices#getCredentialsToComputeServer(gov.pnnl.velo.tif.model.JobConfig, java.lang.String)
   */
  @Override
  public Credentials getCredentialsToComputeServer(JobConfig config, AuthFailedException authFailed) {
    Credentials credentials = null;
    Machine machine = config.getMachine();
    String fullDomainName = config.getMachineId();
    if(machine != null) {
      fullDomainName = machine.getFullDomainName();
    }
    
    if(authFailed != null) {

      CredentialsPrompter prompter = TifServiceLocator.getCredentialsPrompter();
      String title = authFailed.getTitle();
      if(title == null || title.isEmpty()) {
        title = VeloUserInfo.TITLE;
      }
      
      String message = authFailed.getPromptMessage();
      if(message == null || message.isEmpty()) {
        message = "Authenticating to: " + config.getUserName() + "@" + fullDomainName;
      }
      boolean[] echo = new boolean [1];
      echo[0] = false;

      String errMessage = authFailed.getErrMessage();     
      String[] prompts = authFailed.getPrompts();

      String password = prompter.promptForCredentials(title, message, errMessage, prompts, echo);
      if(password == null) { // user cancelled
        throw new AuthCancelException("authentication cancelled");
      }
      KeyboardInteractiveCredentials creds = new KeyboardInteractiveCredentials();
      creds.setUsername(config.getUserName());
      creds.setCredential(password);
      credentials = creds;

    }
    return credentials;
  }

  public Credentials getSshKeyCredentials(JobConfig config) {
    // assume the cert will be the contents of the key file, not the path to the key file
    String cert = findCert(config);
    SshKeyTransferCredentials creds = new SshKeyTransferCredentials();
    creds.setUsername(config.getUserName());
    creds.setKeyFileContents(cert);
   
    return creds;
  }
  
  private String findCert(JobConfig config) {
    // TODO: SshSession has code to find the cert
    // for now just return null;
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchWebServices#executeLaunchJobWebScript(gov.pnnl.velo.tif.model.JobLaunchParameters)
   */
  @Override
  public String executeLaunchJobWebScript(JobLaunchParameters jobLaunchParameters) throws AuthFailedException {

    StringBuilder url = getVeloWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "joblaunching");
    WebServiceUrlUtility.appendPaths(url, "launch");
    logger.debug("URL is " + url);

    CloseableHttpResponse response = null;
    String jobID = null;
    AuthFailedException authFailed = null;

    try {

      HttpPost httppost = new HttpPost(url.toString());

      // serialize list to xml (could not get jackson to serialize a map properly if the map is contained within another class)
      XStream xstream = new XStream();
      String xml = xstream.toXML(jobLaunchParameters);

      StringEntity reqEntity = new StringEntity(xml);
      reqEntity.setChunked(true);
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost); 
      String responseText = getResponseBodyAsString(response).trim();
      if(responseText.startsWith("Job ID:")) {
        jobID = responseText.substring(7);

      } else {
        // this is an authentication failed response
        ObjectMapper mapper = new ObjectMapper();
        authFailed = mapper.readValue(responseText, AuthFailedException.class);
      }

    } catch (Throwable e) {
      // something else went wrong - show to user
      handleException("Failed to execute method.", e);

    }  finally {
      closeQuietly(response);

    }
    if(authFailed != null) {
      throw authFailed;
    }
    return jobID;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchWebServices#executeReconnectJobWebScript(gov.pnnl.velo.tif.model.JobLaunchParameters)
   */
  @Override
  public void executeReconnectJobWebScript(JobLaunchParameters jobParameters) throws AuthFailedException {

    StringBuilder url = getVeloWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "joblaunching");
    WebServiceUrlUtility.appendPaths(url, "reconnect");
    logger.debug("URL is " + url);

    CloseableHttpResponse response = null;
    AuthFailedException authFailed = null;

    try {

      HttpPost httppost = new HttpPost(url.toString());

      // serialize list to xml (could not get jackson to serialize a map properly if the map is contained within another class)
      XStream xstream = new XStream();
      String xml = xstream.toXML(jobParameters);

      StringEntity reqEntity = new StringEntity(xml);
      reqEntity.setChunked(true);
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost); 
      String responseText = getResponseBodyAsString(response).trim();
      if(responseText != null && !responseText.isEmpty()) {
        // this is an authentication failed response
        ObjectMapper mapper = new ObjectMapper();
        authFailed = mapper.readValue(responseText, AuthFailedException.class);
      }

    } catch (Throwable e) {
      // something else went wrong - show to user
      handleException("Failed to execute method.", e);

    }  finally {
      closeQuietly(response);
    }

    if(authFailed != null) {
      throw authFailed;
    }
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchWebServices#executeKillJobWebScript(gov.pnnl.velo.tif.model.JobLaunchParameters)
   */
  @Override
  public String executeKillJobWebScript(JobLaunchParameters jobParameters) throws AuthFailedException {

    StringBuilder url = getVeloWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "joblaunching");
    WebServiceUrlUtility.appendPaths(url, "kill");
    logger.debug("URL is " + url);

    CloseableHttpResponse response = null;
    AuthFailedException authFailed = null;
    String status = "false";
    try {

      HttpPost httppost = new HttpPost(url.toString());

      // serialize list to xml (could not get jackson to serialize a map properly if the map is contained within another class)
      XStream xstream = new XStream();
      String xml = xstream.toXML(jobParameters);

      StringEntity reqEntity = new StringEntity(xml);
      reqEntity.setChunked(true);
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost); 

      String responseText = getResponseBodyAsString(response).trim();
      if(responseText != null && !responseText.isEmpty()) {
    	responseText = responseText.trim();
    	if(responseText.equalsIgnoreCase("true") || responseText.equalsIgnoreCase("false")){
    		status = responseText;
    	}else{
	        // this is an authentication failed response
	        ObjectMapper mapper = new ObjectMapper();
	        authFailed = mapper.readValue(responseText, AuthFailedException.class);
    	}
      }

    } catch (Throwable e) {
      // something else went wrong - show to user
      handleException("Failed to execute method.", e);

    }  finally {
      closeQuietly(response);
    }

    if(authFailed != null) {
      throw authFailed;
    }
    return status;

  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.JobLaunchWebServices#executeKillJobWebScript(gov.pnnl.velo.tif.model.JobLaunchParameters)
   */
  @Override
  public String executeJobStatusWebScript(JobLaunchParameters jobParameters) throws AuthFailedException {

    StringBuilder url = getVeloWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "joblaunching");
    WebServiceUrlUtility.appendPaths(url, "status");
    logger.debug("URL is " + url);

    CloseableHttpResponse response = null;
    AuthFailedException authFailed = null;
    String status = null;
    try {

      HttpPost httppost = new HttpPost(url.toString());

      // serialize list to xml (could not get jackson to serialize a map properly if the map is contained within another class)
      XStream xstream = new XStream();
      String xml = xstream.toXML(jobParameters);

      StringEntity reqEntity = new StringEntity(xml);
      reqEntity.setChunked(true);
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost); 
      
      String responseText = getResponseBodyAsString(response).trim();
      if(responseText != null && !responseText.isEmpty()) {
    	  if(responseText.startsWith("joibID:")) {
    	        status = responseText;

    	  } else {
    		  	// this is an authentication failed response
    		  	ObjectMapper mapper = new ObjectMapper();
    		  	authFailed = mapper.readValue(responseText, AuthFailedException.class);
    	  }
      }

    } catch (Throwable e) {
      // something else went wrong - show to user
      handleException("Failed to execute method.", e);

    }  finally {
      closeQuietly(response);
    }

    if(authFailed != null) {
      throw authFailed;
    }
    return status;

  }

}
