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

import gov.pnnl.velo.util.VeloConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import java.text.ParseException;


/**
 * TODO: make this load by calling the dictionary web service so we can get namespace 
 * registries dynamically on startup
 *
 * @version $Revision: 1.0 $
 */
public class NamespacePrefixResolver {

  private static Map<String, String> prefixMap;
  private static Map<String, String> nsMap;  

  static {
    prefixMap = new HashMap<String, String>();
    prefixMap.put(VeloConstants.NS_PREFIX_CONTENT, VeloConstants.NAMESPACE_CONTENT);
    prefixMap.put(VeloConstants.NS_PREFIX_CAT, VeloConstants.NAMESPACE_CAT);
    //prefixMap.put(VeloConstants.NS_PREFIX_DMI, VeloConstants.NAMESPACE_DMI);
    prefixMap.put(VeloConstants.NS_PREFIX_TAXONOMY, VeloConstants.NAMESPACE_TAXONOMY);
    prefixMap.put(VeloConstants.NS_PREFIX_NOTIFICATION, VeloConstants.NAMESPACE_NOTIFICATION);
    prefixMap.put(VeloConstants.NS_PREFIX_APP, VeloConstants.NAMESPACE_APP);
    prefixMap.put(VeloConstants.NS_PREFIX_FORUM, VeloConstants.NAMESPACE_FORUM);
    prefixMap.put(VeloConstants.NS_PREFIX_SYSTEM, VeloConstants.NAMESPACE_SYSTEM);
    prefixMap.put(VeloConstants.NS_PREFIX_USER, VeloConstants.NAMESPACE_USER);
    prefixMap.put(VeloConstants.NS_PREFIX_SUBSCRIPTION, VeloConstants.NAMESPACE_SUBSCRIPTION);
    prefixMap.put(VeloConstants.NS_PREFIX_SUBSCRIPTION_REPOSITORY, VeloConstants.NAMESPACE_SUBSCRIPTION_REPOSITORY);
    prefixMap.put(VeloConstants.NS_PREFIX_SUBSCRIPTION_REPOSITORY_SEARCH, VeloConstants.NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH);
    prefixMap.put(VeloConstants.NS_PREFIX_ALERT, VeloConstants.NAMESPACE_ALERT);
    prefixMap.put(VeloConstants.NS_PREFIX_TAGTIMER, VeloConstants.NS_PREFIX_TAGTIMER);

    nsMap = new HashMap<String,String>();
    nsMap.put(VeloConstants.NAMESPACE_CONTENT, VeloConstants.NS_PREFIX_CONTENT);
    nsMap.put(VeloConstants.NAMESPACE_CAT, VeloConstants.NS_PREFIX_CAT);
    nsMap.put(VeloConstants.NAMESPACE_TAXONOMY, VeloConstants.NS_PREFIX_TAXONOMY);
    nsMap.put(VeloConstants.NAMESPACE_NOTIFICATION, VeloConstants.NS_PREFIX_NOTIFICATION);
    nsMap.put(VeloConstants.NAMESPACE_APP, VeloConstants.NS_PREFIX_APP);
    nsMap.put(VeloConstants.NAMESPACE_FORUM, VeloConstants.NS_PREFIX_FORUM);  
    nsMap.put(VeloConstants.NAMESPACE_SYSTEM, VeloConstants.NS_PREFIX_SYSTEM);
    nsMap.put(VeloConstants.NAMESPACE_USER, VeloConstants.NS_PREFIX_USER);
    nsMap.put(VeloConstants.NAMESPACE_SUBSCRIPTION, VeloConstants.NS_PREFIX_SUBSCRIPTION);
    nsMap.put(VeloConstants.NAMESPACE_SUBSCRIPTION_REPOSITORY, VeloConstants.NS_PREFIX_SUBSCRIPTION_REPOSITORY);
    nsMap.put(VeloConstants.NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, VeloConstants.NS_PREFIX_SUBSCRIPTION_REPOSITORY_SEARCH);
    nsMap.put(VeloConstants.NAMESPACE_ALERT, VeloConstants.NS_PREFIX_ALERT);
    nsMap.put(VeloConstants.NAMESPACE_TAGTIMER, VeloConstants.NS_PREFIX_TAGTIMER);
  }

  /**
   * Gets the namespace URI registered for the given prefix
   * 
   * @param prefix  prefix to lookup
  
   * @return  the namespace, or null if the prefix has not been registered */
  public static String getNamespaceURI(String prefix) {
    return prefixMap.get(prefix);
  }

  /**
   * Gets the registered prefix for the given namespace URI
   * 
   * @param namespaceURI  namespace URI to lookup
  
   * @return  the prefix, or null if the namespace has not been registered */
  public static String getPrefix(String namespaceURI) {
    return nsMap.get(namespaceURI);
  }

  /**
   * Gets all registered Prefixes
   * 
  
   * @return set of all registered namespace prefixes */
  Set<String> getPrefixes() {
    return prefixMap.keySet();
  }

  /**
   * Gets all registered Uris
   * 
  
   * @return set of all registered namespace uris */
  Set<String> getURIs() {
    return nsMap.keySet();
  }
  

  public static QName parseQNameStringSafe(String qname) {
    try {
      return parseQNameString(qname);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 
   * @param qname
  
  
   * @return QualifiedName
   * @throws ParseException
   * @see Constants.createQNameString */
  public static QName parseQNameString(String qname) throws ParseException {
    int openBrace = qname.indexOf('{');
    int closeBrace = qname.indexOf('}');

    if (openBrace != 0 || closeBrace == -1) {
      throw new ParseException("Unparsable qualified name: '" + qname + "'", 0);
    }

    String namespace = qname.substring(openBrace + 1, closeBrace);
    String localName = qname.substring(closeBrace + 1);

    return new QName(namespace, localName);
  }
}
