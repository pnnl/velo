package vabc;

import org.w3c.dom.Node;

public class NodeWrapper {

  private Node node;
  private String nodeType;
  
  public NodeWrapper() {};
  
  public NodeWrapper(Node node) {
    this.node = node;
    nodeType = node.getNodeName();
  }
  
  public Node getNode() {
    return node;
  }
  
  public String getNodeType() {
    return nodeType;
  }
  
  public String getString(String key) {
    if(node.hasAttributes()) {
      Node attributeNode = node.getAttributes().getNamedItem(key);
      if(attributeNode != null)
        return attributeNode.getNodeValue();
    }   
    return null;
  }
  
  public Boolean getBoolean(String key) {    
    if(node.hasAttributes()) {
      Node attributeNode = node.getAttributes().getNamedItem(key);
      if(attributeNode != null)
        return Boolean.parseBoolean(attributeNode.getNodeValue());
    }   
    return null;
  }
  
  public Double getDouble(String key) {
    if(node.hasAttributes()) {
      Node attributeNode = node.getAttributes().getNamedItem(key);
      if(attributeNode != null)
        return Double.parseDouble(attributeNode.getNodeValue());
    }   
    return null;
  }
  
  public Integer getInteger(String key) {
    if(node.hasAttributes()) {
      Node attributeNode = node.getAttributes().getNamedItem(key);
      if(attributeNode != null)
        return Integer.parseInt(attributeNode.getNodeValue());
    }   
    return null;
  }
}
