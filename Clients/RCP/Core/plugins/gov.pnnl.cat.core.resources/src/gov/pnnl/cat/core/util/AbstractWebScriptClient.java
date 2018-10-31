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
package gov.pnnl.cat.core.util;

import gov.pnnl.cat.core.resources.AccessDeniedException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public abstract class AbstractWebScriptClient {

  private static Logger logger = CatLogger.getLogger(AbstractWebScriptClient.class);

  protected PoolingHttpClientConnectionManager connManager;
  protected RequestConfig defaultRequestConfig;
  protected CloseableHttpClient httpClient; // thread safe and reusable
  protected String repositoryUrl;
  protected String server;
  protected int port;
  protected String protocol;
  protected ProxyConfig proxyConfig;


  public AbstractWebScriptClient(String veloServerUrl, ProxyConfig proxyConfig) {

    try {
      // http://akuna.labworks.org:80/akunaDevAlfresco
      this.repositoryUrl = veloServerUrl;
      URL url = new URL(veloServerUrl);
      this.server = url.getHost();
      this.port = url.getPort();
      this.protocol = url.getProtocol();
      this.proxyConfig = proxyConfig;


      // Create connection configuration
      ConnectionConfig connectionConfig = ConnectionConfig.custom()
          .setMalformedInputAction(CodingErrorAction.IGNORE)
          .setUnmappableInputAction(CodingErrorAction.IGNORE)
          .setCharset(Consts.UTF_8)
          .setMessageConstraints(MessageConstraints.DEFAULT)

          .build();


      // Configure connection manager for https
      // Note that connection manager will override any settings in http client!!
      
      // This is the http components way of creating a custom ssl context - it's not working for me,
      // so I'm using the old ssl context
//      KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
//      FileInputStream instream = new FileInputStream(new File("my.keystore"));
//      try {
//          trustStore.load(instream, "nopassword".toCharArray());
//      } finally {
//          instream.close();
//      }
//      SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();

//      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
//          SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

      // For now we are using the default ssl context - I don't think we need to use a custom one as long as
      // the server uses a valid certificate, not a self-signed one
      SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
      ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();

      Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
          .register("http", plainsf)
          .register("https", sslsf)
          .build();

      this.connManager = new PoolingHttpClientConnectionManager(r);

      // Configure the connection manager to use connection configuration
      this.connManager.setDefaultConnectionConfig(connectionConfig);

      // Allow 5 max connections
      this.connManager.setMaxTotal(5);

      // Increase default max connection per route to 5 (since we only have 1 route)
      this.connManager.setDefaultMaxPerRoute(5);

      // Create global request configuration
      this.defaultRequestConfig = RequestConfig.custom()
          .setExpectContinueEnabled(true)
          .setStaleConnectionCheckEnabled(true)
          .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
          .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.NTLM))
          .setSocketTimeout(600000) // request must complete within 10 minutes
          .setConnectTimeout(10000) // must connect to server within 10 sec
          .setConnectionRequestTimeout(5000) // time to get connection from the pool is 5 sec
          .build();

      // Create http client
      // Set the route planner with our Proxy Selector which should auto-determine the proxy settings
      SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(proxyConfig.getProxySelector());

      // Create an HttpClient with the given custom dependencies and configuration.
      httpClient = HttpClients.custom()
          .setConnectionManager(this.connManager)
          .setRoutePlanner(routePlanner)
          .setDefaultRequestConfig(this.defaultRequestConfig)
          .setSSLSocketFactory(sslsf)
          .build();

      // Start a thread to check for expired connections
      final IdleConnectionMonitorThread connectionChecker = new IdleConnectionMonitorThread(connManager);
      connectionChecker.start();

    } catch (RuntimeException e) {
      throw e;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Create the URL to the velo web service.
   * (i.e., http://ascem.pnl.gov/akunaDevAlfresco/service/ascem)
   * 

   * @return StringBuilder
   */
  protected StringBuilder getVeloWebScriptUrl() {
    StringBuilder url = new StringBuilder(ResourcesPlugin.getResourceManager().getRepositoryUrlBase());
    url.append(WebServiceUrlUtility.SERVICE);
    url.append("velo"); 
    return url;
  }

  private CredentialsProvider getCredentialsProvider(String username, String password) {

    // Use custom credentials provider to set credentials for server and proxy
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();     

    // Set credentials for velo server
    credentialsProvider.setCredentials(
        new AuthScope(this.server, this.port), // velo server and port (i.e., akuna.labworks.org:80)
        new UsernamePasswordCredentials(username, password));

    // Set credentials for proxy
    if(proxyConfig.getHost() != null && proxyConfig.getProxyUsername() != null) {
      // basic scheme
      credentialsProvider.setCredentials(
          new AuthScope(proxyConfig.getHost(), proxyConfig.getPort(), new BasicScheme().getSchemeName()),
          new UsernamePasswordCredentials(proxyConfig.getProxyUsername(), proxyConfig.getProxyPassword()));       

      // NTLM scheme
      credentialsProvider.setCredentials(
          new AuthScope(proxyConfig.getHost(), proxyConfig.getPort(), AuthScope.ANY_REALM, new NTLMScheme().getSchemeName()),
          new NTCredentials(proxyConfig.getProxyUsername(), proxyConfig.getProxyPassword(),
              proxyConfig.getClientMachine(), proxyConfig.getClientDomain()));  
    }
    return credentialsProvider;
  }

  protected void closeQuietly(CloseableHttpResponse response) {
    if(response !=null) {
      try {
        EntityUtils.consumeQuietly(response.getEntity());
        response.close();
      } catch (IOException e) {
        logger.error("Failed to close http response.", e);
      }    
    }
  }

  /**
   * So we can handle response codes consistently
   * @param httpclient
   * @param method
   * @return
   */
  protected CloseableHttpResponse executeMethod(CloseableHttpClient httpclient, HttpRequestBase method) throws SocketTimeoutException {
    String username = ResourcesPlugin.getSecurityManager().getUsername();
    String password = ResourcesPlugin.getSecurityManager().getPassword();
    return executeMethod(httpclient, method, username, password);
  }

  protected CloseableHttpResponse executeMethod(CloseableHttpClient httpclient, HttpRequestBase method, 
      String username, String password) throws SocketTimeoutException {
    CloseableHttpResponse httpResponse = null;

    try {          
      // set Credentials provider (we do it here since user can change username/password in the middle of 
      // the session, so we can't rely on cached value
      HttpClientContext context = HttpClientContext.create();
      // Contextual attributes set the local context level will take
      // precedence over those set at the client level.
      context.setCredentialsProvider(getCredentialsProvider(username, password));

      // preemptive auth
      // Apparently adding an auth cache to the request context makes it use preemptive auth
      AuthCache authCache = new BasicAuthCache();
      HttpHost targetHost = new HttpHost(this.server, this.port, this.protocol);
      BasicScheme basicAuth = new BasicScheme();
      authCache.put(targetHost, basicAuth);
      context.setAuthCache(authCache);

      httpResponse = httpclient.execute(method, context);
      int status = httpResponse.getStatusLine().getStatusCode();
      if (status == HttpStatus.SC_UNAUTHORIZED) {
        throw new AccessDeniedException("The user is not authorized to perform this operation.");
      }

      if (status >= 400) {  // REQUEST HAS ERROR STATUS

        if(status == 511) { // special return code for velo web scripts (TODO: do we need this?)
          throw new RuntimeException("A resource with this name already exists!");

        } else {
          // log what the response message was
          String response = "";
          try {
            response = getResponseBodyAsString(httpResponse);
          } catch (Throwable e) {
            e.printStackTrace();
          }
          if (status == 502 || status == 504) { // proxy timeout caused by long-running request
            throw new ServerTimeoutException(response);

          } else {
            throw new RuntimeException("Error sending content: " + httpResponse.getStatusLine().toString() + "\n" + response);
          }
        }
      }
      return httpResponse;

    } catch (SocketTimeoutException socketTimeout) { 
      throw socketTimeout;

    } catch (RuntimeException e) {
      throw e;

    } catch (Exception e) {
      throw new RuntimeException("Error sending content.", e);
    } 
  }

  /**
   * Read the resonse to a stream and then return a string so we dont
   * get tons of warning messages in the log.
   * @param method

   * @return String
   * @throws Exception
   */
  protected String getResponseBodyAsString(CloseableHttpResponse httpResponse) throws Exception {
    return EntityUtils.toString(httpResponse.getEntity());
  }

  /**
   * Method getCatWebScriptUrl.
   * @return StringBuilder
   */
  protected StringBuilder getCatWebScriptUrl() {
    StringBuilder url = new StringBuilder(this.repositoryUrl);
    url.append("/");
    url.append("service");
    url.append("/");
    url.append("cat"); 
    return url;
  }


  /**
   * Method handleException.
   * @param e Throwable
   */
  protected void handleException(String message, Throwable e) {
    if(e instanceof SocketTimeoutException) {
      throw new ServerTimeoutException("Your operation is taking a long time and will continue to run on the server.  Check back later for results (remote progress monitoring coming soon).");
    
    } else if (e instanceof ServerTimeoutException) {
      throw new ServerTimeoutException("Your operation is taking a long time and will continue to run on the server.  Check back later for results (remote progress monitoring coming soon).");      

    }else if(e instanceof RuntimeException) {
      throw (RuntimeException) e;
    } else {
      throw new RuntimeException(message, e);
    }
  }


  /**
   * Copied from apache http components tutorial
   * https://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html
   */
  public static class IdleConnectionMonitorThread extends Thread {

    private final HttpClientConnectionManager connMgr;
    private volatile boolean shutdown;

    public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
      super();
      this.connMgr = connMgr;
      setDaemon(true); // make sure this thread terminates when JVM shut down
    }

    @Override
    public void run() {
      try {
        while (!shutdown) {
          synchronized (this) {
            wait(5000);
            // Close expired connections
            connMgr.closeExpiredConnections();
            // Optionally, close connections
            // that have been idle longer than 30 sec
            //connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
          }
        }
      } catch (InterruptedException ex) {
        // terminate
      }
    }

    public void shutdown() {
      shutdown = true;
      synchronized (this) {
        notifyAll();
      }
    }

  }


}
