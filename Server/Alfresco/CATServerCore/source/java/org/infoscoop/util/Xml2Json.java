/* infoScoop OpenSource
 * Copyright (C) 2010 Beacon IT Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 */

/** 
 * Original code from 
 * https://github.com/infoScoop/infoscoop/blob/master/src/main/java/org/infoscoop/util/Xml2Json.java
 * Made bug fixes for Velo use
 * Modification author - Chandrika Sivaramakrishnan
 */

package org.infoscoop.util;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * repeatable & keypath : 'tagname' : { 'key' : {...}, 'key' : {...}, ...}
 * repeatable : 'tagname' : [ {...}, {...}, ... ]
 * keypath : 'key' : { ... }
 * arrayPath : [[{...}], [{...}]]
 * 
 * @author a-kimura
 * 
 */
/**
 * Fixed arrayPath mapping to arrayPath : [{...}], [{...}] instead of arrayPath : [[{...}], [{...}]]
 * 
 * @author Chandrika Sivaramakrishnan
 *
 */
public class Xml2Json {
  private static Log log = LogFactory.getLog(Xml2Json.class);

  private List keyPaths = new ArrayList();

  private Map pathMaps = new HashMap();

  private List repeatables = new ArrayList();

  private List singles = new ArrayList();

  private List skips = new ArrayList();

  private List arrays = new ArrayList();

  private Map namespaceResolvers = new HashMap();

  private String basePath;

  private Xml2JsonListener listner = new NoOpListner();

  class NoOpListner implements Xml2JsonListener {
    public String text(String text) throws Exception {
      return text;
    }
  }

  
 
  
  public void addPathRule(String xpath, String keyAttrName, boolean isRepeatable, boolean isSingle) {
    if (keyAttrName != null) {
      keyPaths.add(xpath);
      pathMaps.put(xpath, keyAttrName);
    }
    if (isRepeatable) {
      repeatables.add(xpath);
    }
    if (isSingle) {
      singles.add(xpath);
    }
  }

  public void addSkipRule(String xpath) {
    skips.add(xpath);
  }

  public void addArrayPath(String xpath) {
    arrays.add(xpath);
  }
  
  // VELO PATCH START
  public void clearArrayPath() {
    arrays.clear();;
  }
  // VELO PATCH END

  public void addNamespaceResolver(String prefix, String uri) {
    namespaceResolvers.put(uri, prefix);
  }

  public void setListner(Xml2JsonListener textFilter) {
    this.listner = textFilter;
  }

  public JSONObject xml2jsonObj(NodeList nodes) throws Exception {
    this.basePath = null;
    if (nodes == null || nodes.getLength() == 0)
      return null;
    Node baseNode = nodes.item(0).getParentNode();
    if (baseNode == null)
      return null;
    this.basePath = getXPath((Element) baseNode);
    //VELO PATCH changed SequencedHashMap to LinkedMap as SequencedHashMap is deprecated
    Map map = new LinkedMap();
    nodelist2json(map, nodes);
    return new JSONObject(map);
  }

  public JSONObject xml2jsonObj(Element element) throws Exception {
    this.basePath = null;
    Node baseNode = element.getParentNode();
    if (baseNode != null && baseNode.getNodeType() == Node.ELEMENT_NODE)
      this.basePath = getXPath((Element) baseNode);
    JSONObject obj = (JSONObject) node2json(element);
    return obj;
  }

  public String xml2json(NodeList nodes) throws Exception {
    JSONObject obj = xml2jsonObj(nodes);
    if (obj == null)
      return "";
    return obj.toString(1);
  }

  public String xml2json(Element element) throws Exception {
    JSONObject obj = xml2jsonObj(element);
    return obj.toString(1);
  }

  public String xml2json(String xml) throws Exception {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse(new InputSource(new StringReader(xml)));
    Element root = doc.getDocumentElement();
    return xml2json(root);
  }

  private Object node2json(Element element) throws Exception {
    //VELO PATCH changed SequencedHashMap to LinkedMap as SequencedHashMap is deprecated
    Map map = new LinkedMap();
    String xpath = getXPath(element);
    if (singles.contains(xpath)) {
      if (element.getFirstChild() != null)
        return listner.text(element.getFirstChild().getNodeValue());
      else
        return "";
    }
    NamedNodeMap attrs = element.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node attr = attrs.item(i);
      String name = attr.getNodeName();
      String value = attr.getNodeValue();
      map.put(name, listner.text(value));
    }
    NodeList childs = element.getChildNodes();

    // VELO PATCH START
    //if this is a leaf node with only a text content in it, don't enclose it another 
    //JSONObject instead return the node value as a string
    //For example <name>Chandrika<name> will return "name": "Chandrika"
    //if node has children or attributes proceed to nodelist2json
    //<name type="official">Chandrika<name> will return name:{ "type":"official", "content":"Chandrika" }
    if (attrs.getLength() == 0 && childs.getLength() == 1) {
      Node item = childs.item(0);
      if (item.getNodeType() == Node.CDATA_SECTION_NODE || item.getNodeType() == Node.TEXT_NODE) {
        return listner.text(item.getNodeValue());
      }
    }
    // VELO PATCH END
    
    nodelist2json(map, childs);
    return new JSONObject(map);

  }

  private void nodelist2json(Map map, NodeList nodes) throws Exception {
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      switch (node.getNodeType()) {
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE:
          String text = node.getNodeValue().trim();
          if (text.length() > 0) {
            map.put("content", listner.text(node.getNodeValue()));
          }

          break;
        case Node.ELEMENT_NODE:
          Element childElm = (Element) node;
          String childXPath = getXPath(childElm);
          if (skips.contains(childXPath)) {
            nodelist2json(map, childElm.getChildNodes());
          } else if (arrays.contains(childXPath)) {
            JSONArray obj = (JSONArray) map.get(childElm.getNodeName());
            // VELO PATCH START
            // if (obj == null) {
            // obj = new JSONArray();
            // map.put(childElm.getNodeName(), obj);
            // }
            // VELO PATCH END
            JSONArray array = new JSONArray();
            NodeList childNodes = childElm.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
              Node child = childNodes.item(j);
              // TODO need to support the other node types.
              if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
              array.put(node2json((Element) child));
            }

            // VELO PATCH START
            // original code was creating empty array obj variable if obj was null
            // and adding array to obj which resulted in two sets of [[ ]]
            // For example. "phones": [[ {"number": "5097135555"}, {"number": "5097135556"} ]]
            // obj.put(array);
            if (obj == null)
              obj = array;
            else
              obj.put(array);
            map.put(childElm.getNodeName(), obj);
            // VELO PATCH END

          } else {
            String childName = childElm.getNodeName();
            boolean isRepeatable = repeatables.contains(childXPath);
            boolean hasKey = keyPaths.contains(childXPath);
            if (isRepeatable && hasKey) {
              JSONObject obj = (JSONObject) map.get(childName);
              if (obj == null) {
                obj = new JSONObject();
                map.put(childName, obj);
              }
              String attrName = (String) pathMaps.get(childXPath);
              String attrValue = childElm.getAttribute(attrName);
              obj.put(attrValue, node2json(childElm));
            } else if (isRepeatable && !hasKey) {
              JSONArray obj = (JSONArray) map.get(childName);
              if (obj == null) {
                obj = new JSONArray();
                map.put(childName, obj);
              }
              obj.put(node2json(childElm));
            } else if (hasKey) {
              String attrName = (String) pathMaps.get(childXPath);
              String attrValue = childElm.getAttribute(attrName);
              map.put(attrValue, node2json(childElm));
            } else {
              map.put(childName, node2json(childElm));
            }
          }
          break;
        default:
          break;
      }
    }
  }

  private String getXPath(Element element) {
    if (element == null)
      return null;
    StringBuffer xpath = new StringBuffer();
    xpath.append("/");
    String uri = element.getNamespaceURI();
    String prefix = (String) namespaceResolvers.get(uri);
    if (prefix != null)
      xpath.append(prefix).append(":");
    xpath.append(getTagName(element));
    Element parent = element;
    try {
      while (true) {
        parent = (Element) parent.getParentNode();
        if (parent == null)
          break;
        xpath.insert(0, getTagName(parent));
        uri = parent.getNamespaceURI();
        prefix = (String) namespaceResolvers.get(uri);
        if (prefix != null)
          xpath.insert(0, prefix + ":");
        xpath.insert(0, "/");
      }
    } catch (ClassCastException e) {

    }
    String xpathStr = xpath.toString();
    if (this.basePath != null)
      xpathStr = xpathStr.replaceFirst("^" + this.basePath, "");
    return xpathStr;
  }

  private String getTagName(Element elem) {
    String name = elem.getLocalName();
    if (name == null)
      name = elem.getNodeName();
    return name;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      StringBuffer xml = new StringBuffer();
      Reader reader = new InputStreamReader(new FileInputStream("/Users/d3x140/projects/IT_Velo/dataset.xml"), "utf-8");
      try {
        char[] ch = new char[1024];
        int len = 0;
        while ((len = reader.read(ch)) != -1) {
          xml.append(ch, 0, len);
        }
      } finally {
        reader.close();
      }

      Xml2Json x2j = new Xml2Json();
      x2j.setListner(new TextTrimListner());
      x2j.addArrayPath("/datasetMetadata/metadata/tags");
      x2j.addArrayPath("/datasetMetadata/dataAccess/requiredSoftwares");
      x2j.addArrayPath("/datasetMetadata/publications");
      // x2j.addArrayPath("/metadata/publishers");
      // x2j.addArrayPath("/metadata/publishers/publisher/phones");
      // x2j.addPathRule("/metadata/publishers/publisher",null,true,false);
      // x2j.addPathRule("/metadata/publishers/publisher/phones",null,true,false);

      // String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<metadata>\n<publishers>\n<publisher><name>publisher1</name><phones><number>5097135555</number><number>5097135556</number></phones></publisher><publisher><name>publisher2</name><phones><number>5097135555</number><number>5097135556</number></phones></publisher>\n</publishers>\n</metadata>";
      String json = x2j.xml2json(xml.toString());
      
      System.out.println(json);
      JSONObject jo = new JSONObject(json);
      System.out.println( ((JSONObject)jo.get("generalInformation")).get("description"));
    } catch (Exception e) {
      log.error("", e);
    }
  }
}