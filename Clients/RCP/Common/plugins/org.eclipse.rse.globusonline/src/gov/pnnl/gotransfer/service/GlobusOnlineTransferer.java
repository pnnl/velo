package gov.pnnl.gotransfer.service;
import gov.pnnl.gotransfer.model.UsernamePassword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.auth.x500.X500Principal;

import org.apache.axis.encoding.Base64;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.globusonline.transfer.APIError;
import org.globusonline.transfer.BaseTransferAPIClient;
import org.globusonline.transfer.GoauthAuthenticator;
import org.globusonline.transfer.JSONTransferAPIClient;
import org.globusonline.transfer.JSONTransferAPIClient.Result;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GlobusOnlineTransferer implements Runnable {
  public static String PIC_DTN_ENDPOINT = "pic#dtn";
  public static String APS_CLUTCH_ENDPOINT = "aps#clutch";
  public static String HOPPER_DTN_ENDPOINT = "nersc#hopper";

  private static final int READ_TIMEOUT = 1000 * 60; //1 minute
  private static final int CONNECT_TIMEOUT = 1000 * 30; //30 seconds
  private static final int NEW_FILE_POOLING_INTERVAL =  60 *1000; // 1 minute * 5 5 minutes
  private static final int TASK_MONITORING_POOLING_INTERVAL =  60 *1000; // 1 minute * 5 5 minutes
  
  private static final String dtnMyProxyUrl = "dtn.pnl.gov:7512";
  private static final String clutchMyProxyUrl = "clutch.aps.anl.gov:51000";
  private static final String hopperMyProxyUrl = "hopper.nersc.gov:7512";
  private static final int MAX_NUMBER_OF_RUNNING_TASKS = 1; //i think this works better for only one task to run at a time.
  
  private static String goauthURL = "https://nexus.api.globusonline.org/goauth/token?grant_type=client_credentials";

//private HashMap<String, UsernamePassword> endpointUsernamePassword = new HashMap<String, UsernamePassword>();
  private HashMap endpointUsernamePassword = new HashMap();

//private ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<String>();
  private ConcurrentLinkedQueue messages = new ConcurrentLinkedQueue();
  private JSONTransferAPIClient jsonTransferClient;
  private CopyOnWriteArrayList currentlyRunningTaskIds = new CopyOnWriteArrayList();
//private CopyOnWriteArrayList<String> currentlyRunningTaskIds = new CopyOnWriteArrayList<String>();
  
  private String fromEndpoint;
  private String fromEndpointPath;
  private String toEndpoint;
  private String toEndpointPath;
  private boolean canceled = false;
  
  private String token;
  
  private boolean running = false;
  private TaskMonitor taskMonitor;
  private Thread monitorThread;

//  public List<String> getMessages() {
//    List<String> queuedMessages = new ArrayList<String>();
  public List getMessages() {
    List queuedMessages = new ArrayList();
    while (messages.peek() != null) {
      queuedMessages.add(messages.poll());
    }
    return queuedMessages;
  }

  public void setCanceled(boolean newVal) {
    this.canceled = newVal;
    if(this.canceled){
      logMessage("Terminating...");
    }
  }

  public boolean isCanceled() {
    return canceled;
  }

  public void run() {
    running = true;
    logMessage("Beginning file transfers...");
    
    JSONObject transfer = new JSONObject();
    JSONTransferAPIClient.Result result;
    
    //first create the destination tree if it doesn't already exist:
    recursivelyCreateDir(jsonTransferClient, toEndpoint, toEndpointPath);
    
    //second see if the fromEndpointPath is a path to a file, not a folder - if so, transfer the one file and then quit.
    if(isFile(jsonTransferClient, fromEndpoint, fromEndpointPath)){
      try {
        transfer = createJsonObjectForTransfer(jsonTransferClient);
        
        String name = null;
        if(fromEndpointPath.lastIndexOf("/") != -1){
          name = fromEndpointPath.substring(fromEndpointPath.lastIndexOf("/") + 1);
        }else{
          logMessage("Cannot determine filename at end of path: " +fromEndpointPath);
          logMessage("Please make sure you have the full path entered correctly.");
        }
        
        if(name != null){
          logMessage("Transferring " + fromEndpointPath  + " to " + toEndpointPath);
          JSONObject item = createTransferItem("file", fromEndpointPath, toEndpointPath + name);
          transfer.append("DATA", item);
          result = jsonTransferClient.postResult("/transfer", transfer, null);
          System.out.println("/transfer" + " result.statusCode: "+ result.statusCode + " \nresult.statusMessage: "+ result.statusMessage+ " \nresult.document: "+ result.document.toString());
          logMessage("Transfer status " + result.statusCode + ", message: " + result.statusMessage);
        }
      }catch (Exception e) {
        e.printStackTrace();
        logMessage(e.getMessage());
      }
      
    }else{
      //GO requires the endpoint path end with a slash in order to list its contents...
      if(!fromEndpointPath.endsWith("/")){
        fromEndpointPath = fromEndpointPath + "/";
      }
      while (!isCanceled()) {
        try {
          if(currentlyRunningTaskIds.size() > MAX_NUMBER_OF_RUNNING_TASKS){
            logMessage("Waiting for pending transfer tasks to complete before initiating any more..");
          }else{
            transfer = createJsonObjectForTransfer(jsonTransferClient);
            //add sync option to copy all files/folders created since  
            transfer.put("sync_level", 2);//2 = Copy files if the timestamp of the destination is older than the timestamp of the source
            List contents = getEndpointFolderContents(jsonTransferClient, fromEndpoint, fromEndpointPath);//"/pic/projects/cii/zoetesting/"
//            List<JSONObject> contents = getEndpointFolderContents(jsonTransferClient, fromEndpoint, fromEndpointPath);//"/pic/projects/cii/zoetesting/"
            
            int size = contents.size();
            for(int idx = 0; idx<size; idx++) {
              JSONObject childFileFolder = (JSONObject)contents.get(idx);
//                for (JSONObject childFileFolder : contents) {
              //copy all files/folders found, the 'sync_level' tells GO to only transfer files/folders that aren't at destination or if they are there, they're older 
              logMessage("Transferring " + fromEndpointPath + childFileFolder.getString("name") + " to " + toEndpointPath);
              JSONObject item = createTransferItem(childFileFolder.getString("type"), fromEndpointPath + childFileFolder.getString("name"), toEndpointPath + childFileFolder.getString("name"));
              transfer.append("DATA", item);
            }
            
            if(contents.size() > 0){
              result = jsonTransferClient.postResult("/transfer", transfer, null);
              System.out.println("/transfer" + " result.statusCode: "+ result.statusCode + " \nresult.statusMessage: "+ result.statusMessage+ " \nresult.document: "+ result.document.toString());
              logMessage("Transfer status " + result.statusCode + ", message: " + result.statusMessage);
              //start another thread (if one isn't already running) to monitor when transfer tasks are completed
              monitorTask(result.document.getString("task_id"));
            }else{
              logMessage("No files or folders found to transfer at " + fromEndpointPath);
            }
          }
          Thread.sleep(NEW_FILE_POOLING_INTERVAL);
          
        }catch (APIError apiE){
          logMessage(apiE.message);
          if(apiE.code.endsWith("ActivationRequired")){
            //try to re-activate all endpoints:
        	Iterator endpointItr = endpointUsernamePassword.keySet().iterator();
            while(endpointItr.hasNext()){
              String endpoint = (String)endpointItr.next();
              try {
            	UsernamePassword usernamePassword = (UsernamePassword)endpointUsernamePassword.get(endpoint);
                activateEndpoint(endpoint, getEndpointProxy(endpoint), usernamePassword.getUsername(), usernamePassword.getPassword());
              } catch (Exception e) {
                e.printStackTrace();
                logMessage(e.getMessage());
              }
            }
          }
        }catch (Exception e) {
          e.printStackTrace();
          logMessage(e.getMessage());
        }
      }
    }
    logMessage("Auto file transfer terminated.");
    running = false;
  }
  
  private void monitorTask(String taskId) {
    currentlyRunningTaskIds.add(taskId);
    if(this.taskMonitor == null || this.monitorThread == null || !this.monitorThread.isAlive()){
      this.taskMonitor = new TaskMonitor();
      this.monitorThread = new Thread(taskMonitor);
      monitorThread.start();
    }
  }
  
  private SimpleDateFormat sdf = new SimpleDateFormat("M/d/y K:m:sa");
  private void logMessage(String message){
    messages.add(sdf.format(new Date()) + " " + message);
  }
  
  class TaskMonitor implements Runnable{

    public void run() {
      while(currentlyRunningTaskIds.size() > 0){
    	int size = currentlyRunningTaskIds.size();
        for (int idx = 0;idx<size; idx++) {
          String taskId = (String)currentlyRunningTaskIds.get(idx);
          try {
            String status = getTaskStatus(taskId, jsonTransferClient);
            logMessage("task " + taskId + " status: " + status);
            if(!status.equalsIgnoreCase("ACTIVE")){
              currentlyRunningTaskIds.remove(taskId);
              logMessage("Removing task from monitor: " + taskId);
            }
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        try {
          Thread.sleep(TASK_MONITORING_POOLING_INTERVAL);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    
    public String getTaskStatus(String taskId, JSONTransferAPIClient client) throws IOException, JSONException, GeneralSecurityException, APIError {
      String status = "ACTIVE";
      JSONTransferAPIClient.Result r;

      String resource = "/task/" + taskId;
//      Map<String, String> params = new HashMap<String, String>();
      Map params = new HashMap();
      params.put("fields", "status");

      r = client.getResult(resource, params);
      System.out.println(resource + " result.statusCode: " + r.statusCode + " \nresult.statusMessage: " + r.statusMessage + " \nresult.document: " + r.document.toString());
      status = r.document.getString("status");

      return status;
    }
    
  }

  public void recursivelyCreateDir(JSONTransferAPIClient client, String endpoint, String endpointPath) {
    if(endpointPath.endsWith("/")){
      endpointPath = endpointPath.substring(0, endpointPath.length()-1);
    }
    try{
      getEndpointFolderContents(client, endpoint, endpointPath + "/");
    }catch(Exception e){
      if(endpointPath.contains("/")) {
	      String parentFolder = endpointPath.substring(0, endpointPath.lastIndexOf("/"));
	      recursivelyCreateDir(client, endpoint, parentFolder);
      }
      try {
        createFolder(client, endpoint, endpointPath);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }

  private JSONObject createTransferItem(String type, String sourcePath, String destinationPath) throws JSONException {
    JSONObject item = new JSONObject();
    item.put("DATA_TYPE", "transfer_item");
    item.put("source_endpoint", fromEndpoint);
    item.put("source_path", sourcePath);
    item.put("destination_endpoint", toEndpoint);
    if(type.equalsIgnoreCase("dir")){
      //when transferring a folder recursively, you have to end the destination path with a / and include recursive = true param
      item.put("destination_path", destinationPath +"/");
      item.put("recursive", true);
    }else{
      item.put("destination_path", destinationPath);
    }
    return item;
  }
  
  
  private JSONObject createJsonObjectForTransfer(JSONTransferAPIClient client) throws Exception {
    Result result = client.getResult("/transfer/submission_id");
    String submissionId = result.document.getString("value");
    JSONObject transfer = new JSONObject();
    transfer.put("DATA_TYPE", "transfer");
    transfer.put("submission_id", submissionId);
    return transfer;
  }

  private String getEndpointProxy(String endpoint){
    if(endpoint.equalsIgnoreCase(APS_CLUTCH_ENDPOINT)){
      return clutchMyProxyUrl;
    }else if(endpoint.equalsIgnoreCase(PIC_DTN_ENDPOINT)){
        return dtnMyProxyUrl;
    }else if(endpoint.equalsIgnoreCase(HOPPER_DTN_ENDPOINT)){
        return hopperMyProxyUrl;
    }
    return null;
  }

  public JSONTransferAPIClient activateEndpoint(String endpoint, String hostname, String username, String password) throws Exception{
    //first need to get a go token if we haven't already:
    if(token == null){
      logMessage("Error activating endpoint, no globus online token found.  Authenticate to GO first before attempting endpoint activation.");
      return null;
    }
    
    if(!endpointUsernamePassword.containsKey(endpoint)){
      endpointUsernamePassword.put(endpoint, new UsernamePassword(username, password));
    }
    
    String activatePath = "endpoint/" + endpoint.replaceAll("#", "%23") + "/activate"; //"endpoint/pic%23dtn/activate";//NOTE: endpoint's # has to be encoded
    
    JSONTransferAPIClient client = null;
    try {
//        client = activateEndpoint(activatePath, getEndpointProxy(endpoint), token, username, password, null);
        client = activateEndpoint(activatePath, hostname, token, username, password, null);
    } catch (APIError apiError) {
      String serverDn = parseErrorMessageForServerDn(apiError.message);// IMPORTANT to not use getMessage - that returns the super class's message which will be null...
      if(serverDn != null){
//          client = activateEndpoint(activatePath, getEndpointProxy(endpoint), token, username, password, serverDn);
          client = activateEndpoint(activatePath, hostname, token, username, password, serverDn);
      }else{
        logMessage("Error activating endpoint: " + apiError.toString());
        System.out.println("Error activating endpoint: " + apiError.toString());
        client = null;
      }
    }
    logMessage("Successfully activated endpoint: " + endpoint);
    return client;
  }
  

  private String parseErrorMessageForServerDn(String message) {
    if(message.lastIndexOf("MYPROXY_SERVER_DN") > -1){
      String endSegment = message.substring(message.lastIndexOf("MYPROXY_SERVER_DN"));
      return endSegment.substring(endSegment.indexOf("\"") + 1,endSegment.lastIndexOf("\""));  
    }else{
      return null;
    }
  }
  
  public JSONTransferAPIClient activateEndpoint(String endpointUrl, String myproxy, String token, String endpointUsername, String endpointPassword, String serverDn) throws Exception{
    String activateJsonString = "{"+
        "\"DATA_TYPE\": \"activation_requirements\","+
        "\"DATA\": ["+
            "{"+
                "\"type\": \"myproxy\","+
                "\"name\": \"hostname\","+
                "\"DATA_TYPE\": \"activation_requirement\","+
                "\"value\": \""+myproxy+"\","+
            "},"+
            "{"+
          "\"type\": \"myproxy\","+
                "\"name\": \"username\","+
                "\"DATA_TYPE\": \"activation_requirement\","+
                "\"value\": \""+endpointUsername+"\","+
            "},";
    if(serverDn != null){
      activateJsonString+="{"+
                  "\"type\": \"myproxy\","+
                  "\"name\": \"server_dn\","+
                  "\"DATA_TYPE\": \"activation_requirement\","+
                  "\"value\": \"" +serverDn+"\","+
              "},";
    }
    activateJsonString+="{"+
                "\"type\": \"myproxy\","+
                "\"name\": \"passphrase\","+
                "\"DATA_TYPE\": \"activation_requirement\","+
                "\"value\": \""+endpointPassword+"\","+
              "},"+
          "],"+
      "}";
    JSONObject activateJson = new JSONObject(activateJsonString);
    GoauthAuthenticator auther = new GoauthAuthenticator(token); 
    JSONTransferAPIClient client = new JSONTransferAPIClient(endpointUsername);
    client.setAuthenticator(auther);
    Result result = client.requestResult("POST", endpointUrl, activateJson, null);
    System.out.println("activate result.statusCode: "+ result.statusCode + " \nresult.statusMessage: "+ result.statusMessage+ " \nresult.document: "+ result.document.toString());
    logMessage("activate result.statusCode: "+ result.statusCode + " result.statusMessage: "+ result.statusMessage);
    
    if( result.statusCode == 200 && result.document.getString("code").equals("Activated.MyProxyCredential") ) {
    	return client;
//    } else if( result.document.getString("code").equals("AutoActivationFailed") ) {
//    	return null;
    }
	return null;
	
	
//    return client;
  }

  public JSONTransferAPIClient autoActivateEndpoint(String endpointUrl) throws Exception{

    GoauthAuthenticator auther = new GoauthAuthenticator(token); 
    JSONTransferAPIClient client = new JSONTransferAPIClient("");
    client.setAuthenticator(auther);
    Result result = client.endpointAutoactivate(endpointUrl, null);
    
    if( result.statusCode == 200 && result.document.getString("code").equals("Activated.MyProxyCredential") ) {
    	return client;
//    } else if( result.document.getString("code").equals("AutoActivationFailed") ) {
//    	return null;
    }
	return null;
    
  }
  
  /**
   * 
   * @param client the authenticated JSONTransferAPIClient 
   * @param endpointName the unencoded endpoint name, ie pic#dtn or aps#clutch
   * @param path the path to return contents of
   * @return
   * @throws Exception
   */
//public List<JSONObject> getEndpointFolderContents(JSONTransferAPIClient client, String endpointName, String path) throws Exception {
public List getEndpointFolderContents(JSONTransferAPIClient client, String endpointName, String path) throws Exception {
    List contents = new ArrayList();
    Map params = new HashMap();
//    List<JSONObject> contents = new ArrayList<JSONObject>();
//    Map<String, String> params = new HashMap<String, String>();
    params.put("path", path);

    String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/ls";
    JSONTransferAPIClient.Result r = client.getResult(resource, params);

    JSONArray fileArray = r.document.getJSONArray("DATA");
    for (int i = 0; i < fileArray.length(); i++) {
      JSONObject fileObject = fileArray.getJSONObject(i);
      contents.add(fileObject);
    }

    return contents;
  }
  
  public boolean isFile(JSONTransferAPIClient client, String endpointName, String path) {
    Map params = new HashMap();
//    Map<String, String> params = new HashMap<String, String>();
    params.put("path", path);

    try{
      String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/ls";
      JSONTransferAPIClient.Result r = client.getResult(resource, params);
    }catch(APIError e){
      if(e.code.equalsIgnoreCase("ExternalError.DirListingFailed.NotDirectory")){
        return true;
      }
    }catch(Exception ex){
      logMessage("Unable to list resources on endpoint "+endpointName+", other failures might occur. "); 
    }
    return false;
  }
  
  public void displayLs(JSONTransferAPIClient client, String endpointName, String path) throws IOException, JSONException, GeneralSecurityException, APIError {
    Map params = new HashMap();
//    Map<String, String> params = new HashMap<String, String>();
    if (path != null) {
      params.put("path", path);
    }
    String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/ls";
    JSONTransferAPIClient.Result r = client.getResult(resource, params);
    System.out.println("Contents of " + path + " on " + endpointName + ":");

    JSONArray fileArray = r.document.getJSONArray("DATA");
    for (int i = 0; i < fileArray.length(); i++) {
      JSONObject fileObject = fileArray.getJSONObject(i);
      System.out.println("  " + fileObject.getString("name"));
      Iterator keysIter = fileObject.sortedKeys();
      while (keysIter.hasNext()) {
        String key = (String) keysIter.next();
        if (!key.equals("DATA_TYPE") && !key.equals("LINKS") && !key.endsWith("_link") && !key.equals("name")) {
          System.out.println("    " + key + ": " + fileObject.getString(key));
        }
      }
    }
  }
  
  public void showActivationRequirements(JSONTransferAPIClient client, String endpointName) throws Exception {
	    String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/activation_requirements";
	    JSONTransferAPIClient.Result r = client.getResult(resource, null);
	    System.out.println(r.document.toString());
  }
  
  /**
   * creates a directory on the endpoint with a given path if it doesn't already exist
   * @param client
   * @param endpointName
   * @param path
   * @throws IOException
   * @throws JSONException
   * @throws GeneralSecurityException
   * @throws APIError
   */
  public boolean createFolder(JSONTransferAPIClient client, String endpointName, String path) throws IOException, JSONException, GeneralSecurityException, APIError {
    JSONObject params = new JSONObject();
    params.put("path", path);
    params.put("DATA_TYPE", "mkdir");
    String resource = BaseTransferAPIClient.endpointPath(endpointName) + "/mkdir";
    JSONTransferAPIClient.Result result = client.postResult(resource, params);

    return result.statusCode == 202;
  }
  
  public String authenticateToGlobusOnline(String username, String password) throws Exception{
//    try{
      HttpURLConnection connection = getBasicAuthConnection(goauthURL, username, password);
      String response = executeRequest(connection);
      JSONObject responseJson = new JSONObject(response);
      this.token = responseJson.getString("access_token");
      logMessage("Successfully authenticated to Globus Online.");
      return token;
//    }catch (Exception e){
//      logMessage("Unable to authenticate to Globus Online.  Confirm you are using the correct username and password an try again in a moment.");
//      throw e;
//    }
  }
  
  
 private String executeRequest(HttpURLConnection connection) throws Exception{
    InputStream inputStream = null;
    StringBuffer responseData = new StringBuffer();
//    try {
      inputStream = connection.getInputStream();
      int responseCode = connection.getResponseCode();
      
      if (responseCode >= 200 && responseCode <= 299) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        while (line != null) {
          responseData.append(line);
          line = reader.readLine();
        }
      } else {
        printErrorInfo(connection);
     }
//    }catch (IOException e){
//      e.printStackTrace();
//      if(connection != null){
//        printErrorInfo(connection);
//      }
//    } finally {
//     IOUtils.closeQuietly(inputStream);
//   }
    return responseData.toString();
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
        GlobusOnlineTransferer transferer = new GlobusOnlineTransferer();

//      transferer.setCanceled(false);
//      transferer.setFromEndpoint(GlobusOnlineTransferer.PIC_DTN_ENDPOINT);
//      transferer.setToEndpoint(GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT);
//      transferer.setFromEndpointPath("/pic/projects/cii/zoetesting");
//      transferer.setToEndpointPath("/data/tomo2/tomo/CarinaTest/");

      transferer.authenticateToGlobusOnline("clansing", "Koolcat1");

//      JSONTransferAPIClient client = transferer.activateEndpoint(
//    		  GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT, 
//    		  GlobusOnlineTransferer.clutchMyProxyUrl,  
//    		  "230330", "K00!cats");

      JSONTransferAPIClient client = transferer.autoActivateEndpoint(
    		  GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT);

	  transferer.displayLs(client, GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT, "/");

/*
      try {
    	  client = transferer.autoActivateEndpoint(
    			  GlobusOnlineTransferer.PIC_DTN_ENDPOINT, 
//    		  GlobusOnlineTransferer.dtnMyProxyUrl,
    		  "d3h866");//, "Koolcat1");
    	  
      } catch(Exception exc) {
    	  exc.printStackTrace();
    	  
      }
*/
	  
//      transferer.activateEndpoint(GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT, 
//          "230330", "Kool!1CAT");
//      transferer.setJsonTransferClient(client);
//      transferer.showActivationRequirements(client, GlobusOnlineTransferer.PIC_DTN_ENDPOINT);
//      transferer.displayLs(client, GlobusOnlineTransferer.PIC_DTN_ENDPOINT, "/");

      
//      client = transferer.autoActivateEndpoint(GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT, "230330");
//	    transferer.showActivationRequirements(client, GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT);
//	    transferer.displayLs(client, GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT, "/");
    
//	  client = transferer.autoActivateEndpoint("nersc#hopper",  "derp");
//	  transferer.showActivationRequirements(client, "nersc#hopper");
//	    transferer.displayLs(client, "nersc#hopper", "/");
  
      
//      transferer.createFolder(client, GlobusOnlineTransferer.PIC_DTN_ENDPOINT, "/pic/projects/cii/zoetesting/mkdirTest");
//      transferer.recursivelyCreateDir(client, GlobusOnlineTransferer.PIC_DTN_ENDPOINT, "/pic/projects/cii/zoetesting/parent1/parent2/test/");
	    
    } catch (Exception e) {
      e.printStackTrace();
      
    }
    
//    try {
//      test.setCanceled(false);
//      test.setClutchPassword("Kool!1CAT");
//      test.setClutchUsername("230330");
//      test.setDtnPassword("Koolcat1");
//      test.setDtnUsername("zoe");
//      test.setFromEndpoint(PIC_DTN_ENDPOINT);
//      test.setFromEndpointPath("/pic/projects/cii/zoetesting");
//      test.setGlobusOnlinePassword("Koolcat1");
//      test.setGlobusOnlineUsername("zoeguillen");
//      test.setToEndpoint(APS_CLUTCH_ENDPOINT);
//      test.setToEndpointPath("/data/tomo2/tomo/CarinaTest");
//      Thread testThread = new Thread(test);
//      testThread.start();
//      while (true) {
//        Thread.sleep(1000);
//        List<String> messages = test.getMessages();
//        for (String message : messages) {
//          System.out.println(message);
//        }
//      }
//    } catch (Exception e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
  }

  
  private void printErrorInfo(HttpURLConnection connection) throws IOException {
    int responseCode = connection.getResponseCode();
    String responseMessage = connection.getResponseMessage();
    logMessage("Error: response " + responseCode + " -- " + responseMessage);
  }
  
  private HttpURLConnection getBasicAuthConnection(String urlString, String username, String password) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setReadTimeout(READ_TIMEOUT);
    connection.setConnectTimeout(CONNECT_TIMEOUT);
    connection.setRequestProperty("Authorization", createBasicHeader(username, password));
    return connection;
  }

  private String createBasicHeader(String username, String password) throws UnsupportedEncodingException {
    String authToken = username + ":" + password;
    return "Basic " + Base64.encode(authToken.getBytes());
  }

  public String getFromEndpoint() {
    return fromEndpoint;
  }

  public void setFromEndpoint(String newFromEndpoint) {
    this.fromEndpoint = newFromEndpoint;
  }

  public void setFromEndpointPath(String newFromEndpointPath) {
    this.fromEndpointPath = newFromEndpointPath;
  }

  public void setToEndpoint(String toEndpoint) {
    this.toEndpoint = toEndpoint;
  }

  public void setToEndpointPath(String newToEndpointPath) {
    if(!newToEndpointPath.endsWith("/")){
      newToEndpointPath = newToEndpointPath + "/";
    }
    this.toEndpointPath = newToEndpointPath;
  }

  public void setJsonTransferClient(JSONTransferAPIClient jsonTransferClient) {
    this.jsonTransferClient = jsonTransferClient;
  }

  public JSONTransferAPIClient getJsonTransferClient() {
    return this.jsonTransferClient;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean isRunning) {
    this.running = isRunning;
  }

  
}
