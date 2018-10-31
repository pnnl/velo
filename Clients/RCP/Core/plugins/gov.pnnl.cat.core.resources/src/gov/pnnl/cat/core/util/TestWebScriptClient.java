package gov.pnnl.cat.core.util;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;


public class TestWebScriptClient extends AbstractWebScriptClient {

  private String username;
  private String password;

  public TestWebScriptClient(String veloServerUrl, ProxyConfig proxyConfig, String username, String password) {
    super(veloServerUrl, proxyConfig);
    this.username = username;
    this.password = password;
  }
  
  /**
   * Method getCatWebScriptUrl.
   * @return StringBuilder
   */
  protected StringBuilder getCiiWebScriptUrl() {
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/");
    url.append("service");
    url.append("/");
    url.append("cii"); 
    return url;
  }
  
  protected StringBuilder getModCatMembersUrl(){
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/api/sites/ModCat/memberships");
    return url;
  }
  
  public void testAddUserToModCat(String addusername, String role){
    StringBuilder url = getModCatMembersUrl();

    CloseableHttpResponse response = null;

    try {
      HttpPost httppost = new HttpPost(url.toString());

      String jsonString = "{\"role\":\""+role+"\",\"person\": { \"userName\" : \""+addusername+"\" }}";

      StringEntity reqEntity = new StringEntity(jsonString);
      httppost.setEntity(reqEntity);
      httppost.addHeader("Content-type", "application/json");
      response = executeMethod(httpClient, httppost, username, password);

    } catch (Throwable e) {
      handleException("Failed to create user.", e);
    }  finally {
      closeQuietly(response);
    }
  }
  
  public Resource testSetProperties(Resource resource){
//    /cat/setProperties
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cat/setProperties");
    
    CloseableHttpResponse response = null;

    try {
      HttpPost httppost = new HttpPost(url.toString());

      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, resource);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost, username, password);
      // parse results
      resource = mapper.readValue(response.getEntity().getContent(), Resource.class); 
      EntityUtils.consumeQuietly(response.getEntity());
      
      return resource;

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
    }
    return null;
  }
  
//  for premier portal - run this from the server workspace
//  public void testCreateUser(User user, String adminUsername, String adminPassword){
//    StringBuilder url = getCiiWebScriptUrl();
//    url.append("/createUser");
//
//    CloseableHttpResponse response = null;
//
//    try {
//      HttpPost httppost = new HttpPost(url.toString());
//
//      // serialize resource to json
//      ObjectMapper mapper = new ObjectMapper();
//      mapper = new ObjectMapper();
//      StringWriter writer = new StringWriter();
//      mapper.writeValue(writer, user);
//
//      StringEntity reqEntity = new StringEntity(writer.toString());
//      httppost.setEntity(reqEntity);
//      response = executeMethod(httpClient, httppost, adminUsername, adminPassword);
//
//    } catch (Throwable e) {
//      handleException("Failed to create user.", e);
//    }  finally {
//      closeQuietly(response);
//    }
//  }
  
  public String testHomeFolderGetChildren(String ticket){
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cat/getChildren");
    WebServiceUrlUtility.appendParameter(url, "alf_ticket", ticket);
//    ?alf_ticket=TICKET_0a748bc2543f2b271dc4cb9955c11a042cad72cd
    WebServiceUrlUtility.appendParameter(url, "path", "/company_home/User Documents/" + username);
    
    CloseableHttpResponse response = null;

    System.out.println("HomeFolderGetChildren url: " + url);
    
    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget, username, password);
      String returnedBody = getResponseBodyAsString(response).trim();
      System.out.println("HomeFolderGetChildren response: "+returnedBody);
      return returnedBody;

    } catch (Throwable e) {
        handleException("Failed to execute method.", e);
        
    }  finally {
      closeQuietly(response);
    }
    return null;
  }
  
  public String testPremierDataSetsGetChildren(String ticket){
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cat/getChildren");
    WebServiceUrlUtility.appendParameter(url, "alf_ticket", ticket);
//    ?alf_ticket=TICKET_0a748bc2543f2b271dc4cb9955c11a042cad72cd
    WebServiceUrlUtility.appendParameter(url, "path", "/company_home/Premier Network/Data Set Library/");
    
    CloseableHttpResponse response = null;

    System.out.println("testPremierDataSetsGetChildren url: " + url);
    
    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget, username, password);
      String returnedBody = getResponseBodyAsString(response).trim();
      System.out.println("testPremierDataSetsGetChildren response: \n\n"+returnedBody);
      return returnedBody;

    } catch (Throwable e) {
        handleException("Failed to execute method.", e);
        
    }  finally {
      closeQuietly(response);
    }
    return null;
  }
  
  
  public void testDeleteDownloads(String ticket){
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cat/delete");
    WebServiceUrlUtility.appendParameter(url, "alf_ticket", ticket);
    
    Resource alfrescoResource = new Resource("7e2a02a7-8487-433e-bb80-efbee7763ee0");
    List<Resource> deletes = new ArrayList<Resource>();
    deletes.add(alfrescoResource);
    
    CloseableHttpResponse response = null;

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize list to json
      ObjectMapper mapper = new ObjectMapper();
      mapper = new ObjectMapper();
      StringWriter writer = new StringWriter();
      mapper.writeValue(writer, deletes);

      StringEntity reqEntity = new StringEntity(writer.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost, "admin", "Koolcat1");
      
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }  
  }
  
  
  public String testStartSession(){
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cat/startSession");
    
    CloseableHttpResponse response = null;

    System.out.println("start session url: " + url);
    
    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget, username, password);
      String ticket = getResponseBodyAsString(response).trim();
      System.out.println("start session response: "+ticket);
      return ticket;
    } catch (Throwable e) {
        handleException("Failed to execute method.", e);
        
    }  finally {
      closeQuietly(response);
    }
    return null;
  }
  
  public void testCssefGetWorkflowPackageTemplates(String ticket){
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cssef/listWorkflowPackageTemplates");
    url.append("?alf_ticket="+ ticket);
    CloseableHttpResponse response = null;

    System.out.println("GetWorkflowPackageTemplates url: " + url);
    
    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget, username, password);
      System.out.println("GetWorkflowPackageTemplates response: "+getResponseBodyAsString(response).trim());

    } catch (Throwable e) {
        handleException("Failed to execute method.", e);
        
    }  finally {
      closeQuietly(response);
    }
  }
  
  public void testCssefLaunchWorkflow(String caseName, String titanUsername) {
    // prompt for RSA ID
    String rsaID = JOptionPane.showInputDialog("Enter your RSA token for titan.");
    
    // invoke web script
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cssef/launchWorkflow");

    CloseableHttpResponse response = null;
    String json =
        " {\"credentials\":{\"credential\":\"" + rsaID + "\",\"username\":\"" + titanUsername + "\"},\"caseName\":\"" + caseName + "\"} ";

    try {
      
      HttpPost httppost = new HttpPost(url.toString());
      
      // serialize request to json
      StringEntity reqEntity = new StringEntity(json);
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost, username, password);
      
      // parse results
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);
    }  finally {
      closeQuietly(response);
      
    }
 
    
  }
  
  public void testMultipartFormUpload(CmsPath parentFolder, File fileToUpload) { 
    
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cat/upload");
    
    CloseableHttpResponse response = null;

    try {
      // Two form fields
      // file // file to upload
      // destPath    // path on server where the file will be uploaded
      FileBody file = new FileBody(fileToUpload);
      StringBody path = new StringBody(parentFolder.toAssociationNamePath(), ContentType.TEXT_PLAIN);
      
      HttpEntity reqEntity = MultipartEntityBuilder.create()
          .addPart("file", file)
          .addPart("destPath", path)
          .build();
      
      HttpPost httppost = new HttpPost(url.toString());
      httppost.setEntity(reqEntity);
      response = executeMethod(httpClient, httppost, username, password);
      EntityUtils.consumeQuietly(response.getEntity());
      
    } catch (Throwable e) {
      handleException("Failed to create user.", e);
    }  finally {
      closeQuietly(response);
    }   
    
  }
  
  public void testCsesfCloneTemplate(String templateName, String caseName){
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/service/cssef/cloneWorkflowPackageTemplate");
    WebServiceUrlUtility.appendParameter(url, "templateName", templateName);
    WebServiceUrlUtility.appendParameter(url, "caseName", caseName);

    System.out.println("CloneTemplate url: " + url);
    CloseableHttpResponse response = null;

    try {
      HttpPost httppost = new HttpPost(url.toString());
//      httppost.addHeader("Content-type", "application/json");
      response = executeMethod(httpClient, httppost, username, password);
      System.out.println("CloneTemplate response: "+getResponseBodyAsString(response).trim());

    } catch (Throwable e) {
      handleException("Failed to create user.", e);
    }  finally {
      closeQuietly(response);
    }
  }
  
  public static String REPO_PREMIER_NETWORK = "https://repo.premiernetwork.org/alfresco";
  public static String REPO_CSSEF = "http://acmetest.ornl.gov/alfresco";
  public static String REPO_CARINA = "http://we22294:8082/alfresco";
  public static String REPO_ZOE = "http://localhost:8082/alfresco";
  public static String REPO_MODCAT = "https://modcat.velo.pnnl.gov/alfresco";
  
  public static void main(String... args){
    String repositoryUrl = REPO_ZOE;
    
    ProxyConfig proxyConfig = new ProxyConfig(repositoryUrl);
//    TestWebScriptClient client = new TestWebScriptClient(repositoryUrl, proxyConfig, "mattben", "1Lakehole!");
//    TestWebScriptClient client = new TestWebScriptClient(repositoryUrl, proxyConfig, "admin", "cssefalfresco");
    TestWebScriptClient client = new TestWebScriptClient(repositoryUrl, proxyConfig, "admin", "Koolcat1");
//    TestWebScriptClient client = new TestWebScriptClient(repositoryUrl, proxyConfig, "zoeguillen", "zoeguillen");

    String ticket = client.testStartSession();
    client.testDeleteDownloads(ticket);
    
//    client.testAddUserToModCat("lmurphy", "SiteConsumer");
//    client.testAddUserToModCat("kmaddox", "SiteConsumer");
//    client.testAddUserToModCat("apettibone", "SiteConsumer");
//    client.testAddUserToModCat("aharvey", "SiteConsumer");
//    client.testAddUserToModCat("jshergur", "SiteCollaborator");
//    System.out.println("successfully added users");
    
//    System.out.println("\n\n\n\n");
//    System.out.println("\n");
       
//    
////    String caseName = "F1850.g37-zoe-case_"+System.currentTimeMillis();
//    String caseName = "TestCase1";
////    client.testCsesfCloneTemplate("F_1850_T31_g37_titan", caseName);
////    System.out.println("\nNow home folder should have another child listed as " + caseName);
////    String childrenJson = client.testHomeFolderGetChildren();
////    if(childrenJson.contains(caseName)){
////      System.out.println("clone case worked");
////    }else{
////      System.out.println("clone case failed, " + caseName + " not found in home folder children");
////    }
//    try {
//
//      String ticket = client.testStartSession();
////      client.testCsesfCloneTemplate("F_1850_T31_g37_titan", caseName);
//      client.testCssefLaunchWorkflow(caseName, "zoe");
////      CmsPath zoeHomeFolder = new CmsPath("/User Documents/zoeguillen");
////      File fileToUpload = new File("C:\\Users\\D3k339\\Pictures\\Princess2.jpg");
////      client.testMultipartFormUpload(zoeHomeFolder, fileToUpload);
//    } catch (Throwable e) {
//      e.printStackTrace();
//    }
//    System.out.println("Done");

//    gov.pnnl.cat.datamodel.Resource transferResource = new gov.pnnl.cat.datamodel.Resource();
//    transferResource.setUuid("194e77e9-ae5b-4ea0-b4d0-6ca2e85d5e88");
//    Map<String, List<String>> transferProperties = transferResource.getProperties();
//    transferProperties.put("{http://www.alfresco.org/model/content/1.0}userStatus", null);
//    transferResource = client.testSetProperties(transferResource);
    
    
  }
  

}
