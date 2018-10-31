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
package gov.pnnl.cat.webservice.util;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class provide various static methods that relax X509 certificate and 
 * hostname verification for SSL connections.  It should be used with caution,
 * as it could present a security vulnerability in production.  
 * 
 * It also supports Sun's old deprecated API.
 * @version $Revision: 1.0 $
 */
public final class SSLUtilities {

  /* Hostname verifier for the Sun's deprecated API. */
  // keep static instances to save memory
  private static com.sun.net.ssl.HostnameVerifier deprecatedHostnameVerifier;

  /* Hostname verifier. */
  private static HostnameVerifier hostnameVerifier;

  /* Trust managers for Sun's deprecated API. */
  private static com.sun.net.ssl.TrustManager[] deprecatedTrustManagers;

  /* Trust managers. */
  private static TrustManager[] trustManagers;

  /**
   * Trust all host names without question.
   */
  public static void trustAllHostnames() {

    if(isSSLProtocolDeprecated()) {

      if(deprecatedHostnameVerifier == null) {
        deprecatedHostnameVerifier = new DeprecatedAcceptAllHostnameVerifier();
      }

      // Install the all-trusting host name verifier
      com.sun.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(deprecatedHostnameVerifier);


    } else {

      if(hostnameVerifier == null) {
        hostnameVerifier = new AcceptAllHostnameVerifier();
      } // if
      // Install the all-trusting host name verifier:
      HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

    } 
  }

  /**
   * Trust all certificates, even the self-signed ones.
   */
  public static void trustAllHttpsCertificates() {
    if(isSSLProtocolDeprecated()) {
      com.sun.net.ssl.SSLContext context;

      // Create a trust manager that does not validate certificate chains
      if(deprecatedTrustManagers == null) {
        deprecatedTrustManagers = 
          new com.sun.net.ssl.TrustManager[]{new _DeprecatedAcceptAllX509TrustManager()};
      } 
      // Install the all-trusting trust manager
      try {
        context = com.sun.net.ssl.SSLContext.getInstance("SSL");
        context.init(null, deprecatedTrustManagers, new SecureRandom());
      } catch(GeneralSecurityException gse) {
        throw new IllegalStateException(gse.getMessage());
      }
      com.sun.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

    } else {
      SSLContext context;

      // Create a trust manager that does not validate certificate chains
      if(trustManagers == null) {
        trustManagers = new TrustManager[] {new AcceptAllX509TrustManager()};
      }
      // Install the all-trusting trust manager:
      try {
        context = SSLContext.getInstance("SSL");
        context.init(null, trustManagers, new SecureRandom());
      } catch(GeneralSecurityException gse) {
        throw new IllegalStateException(gse.getMessage());
      }
      HttpsURLConnection.setDefaultSSLSocketFactory(context.
          getSocketFactory());
    } 
  }

  /**
   * Return true if the protocol handler property is deprecated.
   * @return boolean
   */
  private static boolean isSSLProtocolDeprecated() {
    return("com.sun.net.ssl.internal.www.protocol".equals(System.
        getProperty("java.protocol.handler.pkgs")));
  }


  /**
   * This class allows any X509 certificates to be used to authenticate the 
   * remote side of a secure socket, including self-signed certificates. This 
   * class uses the old deprecated API from the com.sun.ssl package.
   * @version $Revision: 1.0 $
   */
  public static class _DeprecatedAcceptAllX509TrustManager 
  implements com.sun.net.ssl.X509TrustManager {

    private static final X509Certificate[] acceptedIssuers = new X509Certificate[] {};

    /* (non-Javadoc)
     * @see com.sun.net.ssl.X509TrustManager#isClientTrusted(java.security.cert.X509Certificate[])
     */
    public boolean isClientTrusted(X509Certificate[] chain) {
      return(true);
    }

    /* (non-Javadoc)
     * @see com.sun.net.ssl.X509TrustManager#isServerTrusted(java.security.cert.X509Certificate[])
     */
    public boolean isServerTrusted(X509Certificate[] chain) {
      return(true);
    }

    /* (non-Javadoc)
     * @see com.sun.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
      return(acceptedIssuers);
    }
  }


  /**
   * This hostname verifier trusts any host name. 
   * @version $Revision: 1.0 $
   */
  public static class AcceptAllHostnameVerifier implements HostnameVerifier {

    /* (non-Javadoc)
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
      return(true);
    } 
  } 

  /**
   * This hostname verifier trusts any host name. 
   * This class uses the old deprecated API from the com.sun.
   * ssl package.
   * @version $Revision: 1.0 $
   */
  public static class DeprecatedAcceptAllHostnameVerifier implements com.sun.net.ssl.HostnameVerifier {

    /* (non-Javadoc)
     * @see com.sun.net.ssl.HostnameVerifier#verify(java.lang.String, java.lang.String)
     */
    public boolean verify(String hostname, String session) {
      return(true);
    } 
  } 


  /**
   * This class allows any X509 certificates to be used to authenticate the 
   * remote side of a secure socket, including self-signed certificates.
   * @version $Revision: 1.0 $
   */
  public static class AcceptAllX509TrustManager implements X509TrustManager {

    private static final X509Certificate[] acceptedIssuers = new X509Certificate[] {};
 
    /* (non-Javadoc)
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    public void checkClientTrusted(X509Certificate[] chain, 
        String authType) {
    } 

    /* (non-Javadoc)
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    public void checkServerTrusted(X509Certificate[] chain, 
        String authType) {
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
      return(acceptedIssuers);
    } 
  }


} // SSLUtilities
