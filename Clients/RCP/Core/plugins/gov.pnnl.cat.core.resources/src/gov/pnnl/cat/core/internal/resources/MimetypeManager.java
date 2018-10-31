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
package gov.pnnl.cat.core.internal.resources;

import gov.pnnl.cat.core.resources.IMimetypeManager;
import gov.pnnl.cat.core.util.AbstractWebScriptClient;
import gov.pnnl.cat.core.util.ProxyConfig;
import gov.pnnl.cat.core.util.WebServiceUrlUtility;
import gov.pnnl.cat.core.util.exception.CATRuntimeException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.util.XmlUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;

/**
 */
public class MimetypeManager extends AbstractWebScriptClient implements IMimetypeManager {
  public static final String MIMETYPE_BINARY = "application/octet-stream";

  private List<String> mimetypes;

  private Map<String, String> extensionsByMimetype;

  private Map<String, String> mimetypesByExtension;

  private Map<String, String> displaysByMimetype;

  private Map<String, String> mimetypesByDisplay;

  private Map<String, String> displaysByExtension;

  private static final Logger logger = CatLogger.getLogger(MimetypeManager.class);

  public MimetypeManager(String repositoryURL, ProxyConfig proxyConfig) {
    super(repositoryURL, proxyConfig);
  }
  
  /**
   * Method getMimetypeMap.
   * @param uri String
   * @return Map<String,String>
   */
  @SuppressWarnings("unchecked")
  private Map<String, String> getMimetypeMap(String uriSegment) {
    Map<String, String> mimetypes = null;
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "mimetypes");
    WebServiceUrlUtility.appendPaths(url, uriSegment);
    
    CloseableHttpResponse response = null;

    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      mimetypes = (Map<String, String>) XmlUtility.deserializeInputStream(response.getEntity().getContent());

    } catch (Throwable e) {
        handleException("Failed to execute method.", e);
        
    }  finally {
      closeQuietly(response);
    }
    
    return mimetypes;
    
//    GetMethod getMethod = new GetMethod();
//
//    try {
//      String charset = getMethod.getParams().getUriCharset();
//      URI uri = new URI(encodedUrl, true, charset);
//
//      getMethod.setURI(uri);
//      executeMethod(getMethod, true);
//
//      if (getMethod.getStatusCode() != HttpStatus.SC_OK) {
//        throw new RuntimeException("Error getting content: " + getMethod.getStatusLine().toString());
//      }
//
//      return (Map<String, String>) XmlUtility.deserializeInputStream(getMethod.getResponseBodyAsStream());
//
//      // throw any exceptions back out to caller
//    } catch (RuntimeException e) {
//      throw e;
//
//    } catch (Throwable e) {
//      throw new RuntimeException(e);
//
//    }

  }

  /**
   * Initialises the maps using the remote MimetypeWebService. This init method can't be called by the Spring container because an authenticated remote session doesn't exist at that point.
   */
  public void init() {
    // don't run if we are already loaded
    if (this.mimetypes != null) {
      return;
    }
    this.mimetypes = new ArrayList<String>(40);
    this.extensionsByMimetype = new HashMap<String, String>(59);
    this.mimetypesByExtension = new HashMap<String, String>(59);
    this.displaysByMimetype = new HashMap<String, String>(59);
    this.displaysByExtension = new HashMap<String, String>(59);
    this.mimetypesByDisplay = new HashMap<String, String>(59);

    try {
      Map<String, String> mimetypeMap = getMimetypeMap("displaysByMimetype");
      Set<String> mimetypeKeys = mimetypeMap.keySet();

      for (String mimetype : mimetypeKeys) {
        // add to list of mimetypes
        this.mimetypes.add(mimetype);

        // add to map of mimetype displays
        if (mimetypeMap.containsKey(mimetype)) {
          this.displaysByMimetype.put(mimetype, mimetypeMap.get(mimetype));
          this.mimetypesByDisplay.put(mimetypeMap.get(mimetype), mimetype);
        }
      }

      mimetypeMap = getMimetypeMap("extensionsByMimetype");
      mimetypeKeys = mimetypeMap.keySet();
      for (String mimetype : mimetypeKeys) {
        // add to map of extensions by mimetype
        this.extensionsByMimetype.put(mimetype, mimetypeMap.get(mimetype));

        // add to map of mimetypes by extension
        this.mimetypesByExtension.put(mimetypeMap.get(mimetype), mimetype);

        // add to map of extension displays
        if (mimetypeMap.containsKey(mimetype)) {
          // use the mimetype's display name for the file extension
          this.displaysByExtension.put(mimetypeMap.get(mimetype), this.displaysByMimetype.get(mimetype));
        }

        // check that there were extensions defined
        if (!mimetypeMap.containsKey(mimetype)) {
          logger.warn("No extensions defined for mimetype: " + mimetype);
        }

      }

      // make the collections read-only
      this.mimetypes = Collections.unmodifiableList(this.mimetypes);
      this.extensionsByMimetype = Collections.unmodifiableMap(this.extensionsByMimetype);
      this.mimetypesByExtension = Collections.unmodifiableMap(this.mimetypesByExtension);
      this.displaysByMimetype = Collections.unmodifiableMap(this.displaysByMimetype);
      this.displaysByExtension = Collections.unmodifiableMap(this.displaysByExtension);
      this.mimetypesByDisplay = Collections.unmodifiableMap(this.mimetypesByDisplay);

    } catch (Exception e) {
      logger.error("Failed to load mimetypes", e);
      throw new CATRuntimeException(e);
    }
  }

  /**
   * Get the file extensions associated with the mimetype.
   * 
   * @param mimetype
   *          a valid mimetype
  
   * @return Returns the list of file extensions associated with this mimetype * @see gov.pnnl.cat.core.resources.IMimetypeManager#getExtension(String)
   */
  public String getExtension(String mimetype) {
    init();
    return this.extensionsByMimetype.get(mimetype);
  }

  /**
   * Method getDisplaysByExtension.
   * @return Map<String,String>
   * @see gov.pnnl.cat.core.resources.IMimetypeManager#getDisplaysByExtension()
   */
  public Map<String, String> getDisplaysByExtension() {
    init();
    return displaysByExtension;
  }

  /**
   * Method getDisplaysByMimetype.
   * @return Map<String,String>
   * @see gov.pnnl.cat.core.resources.IMimetypeManager#getDisplaysByMimetype()
   */
  public Map<String, String> getDisplaysByMimetype() {
    init();
    return displaysByMimetype;
  }

  /**
   * Method getMimetypesByDisplay.
   * @return Map<String,String>
   * @see gov.pnnl.cat.core.resources.IMimetypeManager#getMimetypesByDisplay()
   */
  public Map<String, String> getMimetypesByDisplay() {
    init();
    return this.mimetypesByDisplay;
  }

  /**
   * Method getExtensionsByMimetype.
   * @return Map<String,String>
   * @see gov.pnnl.cat.core.resources.IMimetypeManager#getExtensionsByMimetype()
   */
  public Map<String, String> getExtensionsByMimetype() {
    init();
    return extensionsByMimetype;
  }

  /**
   * Method getMimetypes.
   * @return List<String>
   * @see gov.pnnl.cat.core.resources.IMimetypeManager#getMimetypes()
   */
  public List<String> getMimetypes() {
    init();
    return mimetypes;
  }

  /**
   * Method getMimetypesByExtension.
   * @return Map<String,String>
   * @see gov.pnnl.cat.core.resources.IMimetypeManager#getMimetypesByExtension()
   */
  public Map<String, String> getMimetypesByExtension() {
    init();
    return mimetypesByExtension;
  }

  /**
  
   * @param filename String
   * @return String
   * @see #MIMETYPE_BINARY */
  public String guessMimetype(String filename) {
    filename = filename.toLowerCase();
    String mimetype = MIMETYPE_BINARY;
    // extract the extension
    int index = filename.lastIndexOf('.');
    if (index > -1 && (index < filename.length() - 1)) {
      String extension = filename.substring(index + 1);
      if (mimetypesByExtension.containsKey(extension)) {
        mimetype = mimetypesByExtension.get(extension);
      }
    }
    return mimetype;
  }
}
