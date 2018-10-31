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

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.exception.CATRuntimeException;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.old.URIUtil;
import org.apache.commons.lang.StringUtils;

/**
 * Build URLs to Alfresco.
 * @version $Revision: 1.0 $
 */
public class WebServiceUrlUtility {
  
  public static final String SERVICE = "/service/";

  private static final String AMPERSAND = "&";

  @SuppressWarnings("unused")
  private static final String COLON = ":";

  private static final String EQUALS = "=";

  @SuppressWarnings("unused")
  private static final String PROTOCOL = "://";

  private static final String QUESTION = "?";

  private static final String SLASH = "/";

  /**
   * Append the given request parameter name and value to the {@link StringBuilder} URL.
   * 
   * @param url
   *          {@link StringBuilder} to append parameter to
   * @param name
   *          String name of the parameter
   * @param value
   *          String value of the parameter
   */
  public static void appendParameter(StringBuilder url, String name, String value) {
    if (StringUtils.isNotBlank(name)) {
      if (!contains(url, QUESTION)) {
        url.append(QUESTION);
      }

      if (!endsWith(url, QUESTION) && !endsWith(url, AMPERSAND)) {
        url.append(AMPERSAND);
      }

      url.append(name);
      url.append(EQUALS);

      if (StringUtils.isNotBlank(value)) {
        // encode the value
        url.append(encodeParameter(value));
      }
    }
  }

  /**
   * Append the given request parameter name/value (represented by the keys and values of the given Map) to the {@link StringBuilder} URL.
   * 
   * @param url
   *          {@link StringBuilder} to append parameter to
   * @param parameters
   *          Map<String, String> of name to value parameter pairs
   */
  public static void appendParameters(StringBuilder url, Map<String, String> parameters) {
    for (Entry<String, String> entry : parameters.entrySet()) {
      appendParameter(url, entry.getKey(), entry.getValue());
    }
  }

  /**
   * Append the given String paths to the {@link StringBuilder} URL, separating with {@link #SLASH}. The {@link StringBuilder} when finished will not end in a {@link #SLASH}.
   * 
   * @param url
   *          {@link StringBuilder} to append to
   * @param paths
   *          String array (varargs) of paths to append
   */
  public static void appendPaths(StringBuilder url, String... paths) {
    if (!endsWith(url, SLASH)) {
      url.append(SLASH);
    }

    for (String path : paths) {
      if (StringUtils.isNotBlank(path)) {
        url.append(path);
      }

      if (!endsWith(url, SLASH)) {
        url.append(SLASH);
      }
    }

    if (endsWith(url, SLASH)) {
      url.deleteCharAt(url.length() - 1);
    }
  }

  /**
   * Test if the given {@link StringBuilder} contains the given String.
   * 
   * @param stringBuilder
   *          {@link StringBuilder} to search within
   * @param value
   *          String to search for
  
   * @return boolean true if the {@link StringBuilder} contains the given String */
  private static boolean contains(StringBuilder stringBuilder, String value) {
    int index = stringBuilder.indexOf(value);

    return index != -1;
  }

  /**
   * Encode the path and query segments of the given {@link StringBuilder} URL.
   * 
   * @param url
   *          {@link StringBuilder} URL with path and query segments
  
   * @return String encoded URL */
  public static String encodeParameter(String parameter) {
    try {
      // How annoying, the old apache httpclient's URIUtil doesn't encode an ampersand in the URL, so we have to add that
      // check separately
      String apacheEncodedUrl = URIUtil.encodePathQuery(parameter);
      
      // now replace ampersand
      String encodedUrl = apacheEncodedUrl.replaceAll("\\&", "%26");
      return encodedUrl;
      
    } catch (Exception e) {
      throw new CATRuntimeException(e);
    }
  }

  /**
   * Test if the given {@link StringBuilder} ends with the given String.
   * 
   * @param stringBuilder
   *          {@link StringBuilder} to test
   * @param value
   *          String to test if at the end of the {@link StringBuilder}
  
   * @return boolean true if the {@link StringBuilder} ends with the String */
  private static boolean endsWith(StringBuilder stringBuilder, String value) {
    int index = stringBuilder.lastIndexOf(value);

    return index == (stringBuilder.length() - 1);
  }

  /**
   * Construct the URL to the given web service.
   * 
  
  
   * @param serviceName String
   * @return StringBuilder with base URL to service */
  public static StringBuilder getService(String serviceName) {
    StringBuilder url = new StringBuilder();
    url.append(ResourcesPlugin.getResourceManager().getRepositoryUrlBase());
    url.append(SERVICE);
    url.append(serviceName);

    return url;
  }

  /**
   * Cannot instantiate
   */
  private WebServiceUrlUtility() {
    super();
  }
}
