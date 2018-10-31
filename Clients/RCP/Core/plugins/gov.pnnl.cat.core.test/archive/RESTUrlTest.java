/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.core.resources.tests;

import junit.framework.TestCase;

/**
 * Creates a folder remotely using the CMIS REST-based web service API.
 * @version $Revision: 1.0 $
 */
public class RESTUrlTest extends TestCase {

  public void testPost(){
//    try {
//    MultiThreadedHttpConnectionManager cm = new MultiThreadedHttpConnectionManager();
//    CommonsHTTPClientProperties clientProperties = CommonsHTTPClientPropertiesFactory.create();
//    cm.getParams().setDefaultMaxConnectionsPerHost(30);
//    cm.getParams().setMaxTotalConnections(30);
//    // cm.getParams().setSendBufferSize(10000);
//    // If defined, set the default timeouts
//    // Can be overridden by the MessageContext
//    if (clientProperties.getDefaultConnectionTimeout() > 0) {
//      cm.getParams().setConnectionTimeout(clientProperties.getDefaultConnectionTimeout());
//    }
//    if (clientProperties.getDefaultSoTimeout() > 0) {
//      cm.getParams().setSoTimeout(clientProperties.getDefaultSoTimeout());
//    }
//    
//    HttpClient httpClient = new HttpClient(cm);
//    httpClient.getParams().setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
//    Credentials defaultcreds = new UsernamePasswordCredentials("admin", "Koolcat1");
//    httpClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
//    String url = "http://protoapps:8081/alfresco/service/api/path/workspace/SpacesStore/Company%20Home/children";
//    PostMethod method = new PostMethod(url);
//    
//    String filePath ="C:/Alfresco/createfilepost.xml";
//    String contentType = "application/atom+xml;type=entry";
//    File createDoc = new File(filePath);
//    //method.setRequestHeader("name", createDoc.getName());
//    method.setRequestHeader("Content-type", contentType);
//    method.setRequestBody(new FileInputStream(createDoc));
//    int status = httpClient.executeMethod(method);
//    System.out.println("return code is: " + status + "\n\n" + " return body is :"
//          + method.getResponseBodyAsString());
//    
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }
    
 
}
