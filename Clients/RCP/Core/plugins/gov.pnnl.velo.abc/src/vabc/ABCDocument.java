package vabc;

import gov.pnnl.velo.util.ClasspathUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import datamodel.DataModelObservable;

import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import datamodel.DataItem;
import datamodel.DataModelObservable.DataModelChange;
import datamodel.DataModelObservable.DataModelObserver;
import datamodel.Key;
import datamodel.collections.DataItemMap;
import datamodel.collections.RecursiveDataItemMap;
import abc.containers.ABC;
import abc.containers.ABCComponent;
import abc.rules.ABCCalculationRule;
import abc.rules.ABCContextRule;
import abc.rules.ABCExpressionRule;
import abc.rules.ABCRuleEvaluator;
import abc.units.ABCUnitFactory;
import abc.validation.ABCCheckListItem;
import abc.validation.ABCDataItem;
import abc.validation.ABCDoubleItem;
import abc.validation.ABCIntegerItem;

/**
 * Wrapper for an XML Document with convenience methods specific to ABC schema. 
 * 
 * @author karen
 *
 */
public class ABCDocument implements DataModelObserver {
  
  // Container for the ABC "component" associated with each xml document for rule evaluation
	// The string key is the ABCDocument.getId();
	static Map<String, Set<ABC>> componentABCs = new HashMap<String,Set<ABC>>();
	
	// Use this to look for duplicate keys and also to quickly locate node info for rules
	Map<String, Node> keyNodeMap = new HashMap<String,Node>();

	// Break this out into separate class so people can subclass
	protected ABCRuleEvaluator ruleEvaluator = null;
	protected String uuidString;
	
	protected Document document;

	// When evaluating target due to add, don't process updates from the rule execution
	private	boolean  targetInProgress = false;

	public ABCDocument(Document document) {
	  uuidString = UUID.randomUUID().toString();
		this.document = document;
		createKeyMap();
		ruleEvaluator = new ABCRuleEvaluator(this);
		setRuleEvaluator(ruleEvaluator);
	}
	public ABCDocument(Document document, ABCRuleEvaluator ruleEvaluator) {
	  uuidString = UUID.randomUUID().toString();
		this.document = document;
		createKeyMap();
		setRuleEvaluator(ruleEvaluator);
	}
	
	public void setRuleEvaluator(ABCRuleEvaluator ruleEvaluator) {
		this.ruleEvaluator = ruleEvaluator;
		parseRules();
	}

	public Document getDocument() {
		return document;
	}
	
	public static void purge(ABCDocument abcdoc) {
	  if (abcdoc != null) {
	    componentABCs.remove(abcdoc.getId());
	  }
	}

	public String getId() {
		return uuidString;
	}

        /**
	 * Total hack.  Fixing bug where two models of teh same simulator get rules conflicts because
	 * the document ids are the same.  I'm setting an id based on teh datamodel now.
	 * @param id
	 */
	public void setId(String id) {
		uuidString = id;
	}

	public void addComponentABC(ABC abc) {	  
		Set<ABC> list = componentABCs.get(getId());
		if (list == null) {
			list = new HashSet<ABC>();
		}
		ABC removeMe = null;
		for(ABC exists: list) {
		  if(exists.getName().equals(abc.getName())) {
		    removeMe = exists;
		    break;
		  }
		}
		if(removeMe != null) {
		  // Clear the observers, should make it easier to debug?
		  removeMe.getView().getRCollection().removeObservers();
		  list.remove(removeMe);		
		}
		list.add(abc);
		componentABCs.put(getId(), list);
	}
	
	public ABCRuleEvaluator getRuleEvaluator() {
	  return ruleEvaluator;
	}
	
	public Node getComponent(Key identifier) {
		try {
			NodeList children = document.getDocumentElement().getChildNodes();
			for(int i = 0; i < children.getLength(); i++) {
				if(children.item(i).getNodeName().equals(ABCConstants.Key.COMPONENT)) {
					if(identifier.getAlias().equals(children.item(i).getAttributes().getNamedItem(ABCConstants.Key.NAME).getNodeValue())) {
						return children.item(i);
					}
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();		
		} 
		return null; 

	}

	public Node getComponent(String key) {
		try{
			String parent = key.split("/")[0];
			NodeList nl = document.getDocumentElement().getElementsByTagName(parent);
			if(!parent.equals(key)) {
				String child = key.split("/")[1];
				nl = nl.item(0).getChildNodes();
				for(int i = 0; i < nl.getLength(); i++) {
					if(nl.item(i).getNodeName().equals(child)) {
						return nl.item(i);
					}
				}
			}
			if(nl.item(0) == null) {
				return document.getDocumentElement();
			}
			return nl.item(0);	
		} catch (Exception e) {
			e.printStackTrace();
			return null; // TODO: Handle exception?
		}
	}

       public String findLabelFromKey(String component, String key) {

	  String label = key; // fallback
	  Node node = findFromKey(component,key);
	  if (node != null) {
	    String tmp = getLabel(node); 
	    if (tmp != null && !tmp.isEmpty()) label = tmp;
	  }
          return label;
	}
       
  /**
   * Looks up definition of the specified key and returns true if the could_be_file
   * attribute is set.
   * @param component
   * @param key
   * @return
   */
  public boolean isCouldBeFile(String component, String key) {
    boolean maybe = false;
    Node node = findFromKey(component,key);
    if (node != null) {
      String attr = getAttrValue(node,ABCConstants.Key.COULD_BE_FILE);
      if (attr != null && attr.equals("true")) maybe = true;
    }

    return maybe;
  }

	public Node findFromKey(String component, String key) {
	  Node node = null;
	  if (component != null && !component.isEmpty())
	    node = getComponent(component);
	  if (node == null) node = document.getDocumentElement();
	  return findFromKey(node,key);
	  
	}
	
	public Node findFromKey(Node node, String key) {
          NodeList children = node.getChildNodes();
	  for (int idx = 0; idx < children.getLength(); idx++) {
            Node child = children.item(idx);
            String curKey = getKey(child);
            if (curKey != null && curKey.equalsIgnoreCase(key)) {
              return child;
            } else {
              Node match = findFromKey(child,key);
              if (match != null) return match;
            }
          }
	  return null;
	}
	
	public Node findFromLabel(String component, String label) {
	  Node node = null;
	  if (component != null && !component.isEmpty())
	    node = getComponent(component);
	  if (node == null) node = document.getDocumentElement();
	  return findFromLabel(node,label);
	  
	}
	
	public Node findFromLabel(Node node, String label) {
    NodeList children = node.getChildNodes();
	  for (int idx = 0; idx < children.getLength(); idx++) {
        Node child = children.item(idx);
        String curLabel = getLabel(child);
        if (curLabel != null && curLabel.equalsIgnoreCase(label))
          return child;
        else {
          Node match = findFromLabel(child,label);
          if (curLabel != null) return match;
        }
    }
	  return null;
	}


	public static ABCDocument load(File xmlfile) throws Exception {
		try {
			final String SCHEMA_VERSION = "http://www.w3.org/XML/XMLSchema/v1.1";
			String filename = "abc.xsd";

			StreamSource[] schemaDocuments;
			File file = ClasspathUtils.getFileFromClassFolder( ABCDocument.class, filename);
			schemaDocuments = new StreamSource[] { new StreamSource(file) };


			SchemaFactory sf = SchemaFactory.newInstance(SCHEMA_VERSION);
			Schema s = sf.newSchema(schemaDocuments);

			// Parse and store the registry document
			// We want to support xinclude to make modular files to reduce
			// duplication for example across simulator modes
			// Parser needs to be told to do this and the schema validated AFTER
			// the
			// includes are processed.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			try {
				dbf.setXIncludeAware(true);
				dbf.setNamespaceAware(true);
				dbf.setSchema(s);
				// See http://stackoverflow.com/questions/22774425/attempting-to-connect-xml-files-with-xinclude-attribute-xmlbase-error
				dbf.setFeature(
						"http://apache.org/xml/features/xinclude/fixup-base-uris",
						false);
			} catch (UnsupportedOperationException e) {
				throw new ABCException("XML parse error - possible error with xinclude?");

			}

			Document doc = dbf.newDocumentBuilder().parse(xmlfile);
			return new ABCDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * See if the names element exists and has content in the form of sub elements.
	 * Can be used to determine if a paraticular part of the UI should be created
	 * @param element_name
	 * @return
	 */
	public boolean existsWithContent(String element_name) {
		Node node = getElement(element_name);
		if (node != null) {
			return node.getFirstChild() != null;
		}
		return false;

	}

	/**
	 * Return 1st element with the given name from the current registry selection.
	 * Useful to get starting point for key searches such as finding keys within
	 * a particular section.
	 * @param element_name
	 * @return
	 */
	public Node getElement(String element_name) {
		if (document != null) {
			Element root = document.getDocumentElement();
			NodeList nodes = root.getElementsByTagName(element_name);
			if (nodes != null && nodes.getLength() > 0)
				return nodes.item(0);
		}
		return null;
	}

	/**
	 * Determine if the specified key exists within the document.
	 * 
	 * @param element_name starting point for the search, defaults to the top
	 * @param keyvalue the search target
	 * @return true if the key is found in the documnet
	 */
	public boolean isValidKey(String element_name, String keyvalue) {
		if (findNodeForKey(element_name,keyvalue) == null)
			return false;
		return true;
	}


	/**
	 * Search for specified key within the document.
	 * The keyvalue can be hierarchical (sort of) such as ic_aqueous_pressure/value
	 * This was added because we used non unique keys under choice menus to carry values over
	 * in the UI.  Limited functionality tested only for that one use case.
	 * 
	 * @param element_name starting point for the search, defaults to the top
	 * @param keyvalue the search target
	 * @return the node that matches the key
	 */
	public Node findNodeForKey(String element_name, String keyvalue) {
		Node ret = null;

		Node start = getElement(element_name);
		if (start == null) start = document.getDocumentElement();

		// We support hierarchical searches currently assuming only two parts to hierarchy
		String[] keyvalues = keyvalue.split("/");
		for (int kdx=0; kdx<keyvalues.length; kdx++) {
			Node match = findNodeForKey(start, keyvalues[kdx]);
			if (match != null) {
				start = match;
				if (kdx == keyvalues.length-1) ret = match;
			} else {
				break;  // didn't find this part so quit looking
			}
		}
		return ret;
	}

	/**
	 * Same as above but always start search at root.
	 * @param keyvalue
	 * @return
	 */
	public Node findNodeForKey(String keyvalue) {
		Node ret = null;

		Node start = document.getDocumentElement();

		// We support hierarchical searches currently assuming only two parts to hierarchy
		String[] keyvalues = keyvalue.split("/");
		for (int kdx=0; kdx<keyvalues.length; kdx++) {
			Node match = findNodeForKey(start, keyvalues[kdx]);
			if (match != null) {
				start = match;
				if (kdx == keyvalues.length-1) ret = match;
			} else {
				break;  // didn't find this part so quit looking
			}
		}
		return ret;
	}

	public Node findNode(Node parent, String key) throws ABCException {
		if(parent != null) {
			String parentKey = getKey(parent);
			if(parentKey != null && key.equals(parentKey)) {
				return parent;
			}
		} else {
			parent = document.getDocumentElement().getParentNode();
		}
		NodeList children = parent.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node child = findNode(children.item(i), key);
			if(child != null)
				return child;
		}
		return null;
	}

	/**
	 * 
	 * @param start
	 * @param keyvalue   key to search for;  CANNOT be a path by the time we get here
	 * @return
	 */
	protected Node findNodeForKey(Node start, String keyvalue) {

		Node ret = null;
		NodeList children = start.getChildNodes();
		for (int idx=0; idx<children.getLength(); idx++) {
			Node child = children.item(idx);
			if (child.hasAttributes()) {
				NamedNodeMap attrs = child.getAttributes();
				// We have key and simulation_key attributes.  Ellen is looking to see if there
				// is a reason.  Meanwhile, look for either.
				Node match = attrs.getNamedItem("simulator_key");
				if (match == null)  match = attrs.getNamedItem("key");
				if (match != null && match instanceof Attr) {
					String matchvalue = ((Attr)match).getValue();
					if (matchvalue.equals(keyvalue)) {
						// Have a match 
						ret = child;
						break;
					}
				}
			}
			// No match on this children.  do a depth first search
			Node match = findNodeForKey(child,keyvalue);
			if (match != null) {
				ret = match;
				break;
			}
		}
		return ret;
	}


	protected String getAttrValue(Node node, String key) {

		if (node.hasAttributes()) {
			NamedNodeMap attrs = node.getAttributes();
			Node match = attrs.getNamedItem(key);
			if (match != null)
				return ((Attr)match).getValue();
		}
		return null;
	}

	public String getLabel(Node node) {
		return getAttrValue(node,"label");

	}
	public boolean isCollapsible(Node node) {
		String value = getAttrValue(node,"collapsible");
		if (value != null && value.equals("true"))
			return true;
		return false;

	}

	public boolean hasUnits(Node node) {
		return getAttrValue(node,"units") != null;
	}

	public String getUnits(Node node) {
		return getAttrValue(node,"units");
	}

	public String getDefaultUnit(Node node) {
		return getAttrValue(node,"default_unit");
	}

	public String getDefaultValue(Node node) {
		return getAttrValue(node,"default");
	}

	public String getKey(Node node) {
		return getAttrValue(node,"key");
	}
	public String getSimKey(Node node) {
		return getAttrValue(node,"simulator_key");
	}


	public String getAbsMin(Node node) {
		return getAttrValue(node,"absolute_min");
	}
	public boolean hasAbsMin(Node node) {
		return getAttrValue(node,"absolute_min") != null;
	}
	public String getAbsMax(Node node) {
		return getAttrValue(node,"absolute_max");
	}
	public boolean hasAbsMax(Node node) {
		return getAttrValue(node,"absolute_max") != null;
	}

	public boolean isGroup(Node node) {
		if (node != null) {
			String name = node.getNodeName();
			if (name.equals("group")) return true;
		}
		return false;
	}

	public boolean isComplexChoice(Node node) {
		if (node != null) {
			String name = node.getNodeName();
			if (name.equals("choice")) return true;
		}
		return false;
	}

	public boolean isNumber(Node node) {
		if (node != null) {
			String name = node.getNodeName();
			if (name.equals("double") || name.equals("integer")) return true;
		}
		return false;
	}

	/**
	 * Validate against the absolute minimum value.
	 * @param node
	 * @return
	 */
	public float getMinValue(Node node) {
		try {
			return Float.parseFloat(getAbsMin(node));
		} catch (Exception e) {
			//This is an optional attribute so if we can't get it and parse it, that is ok.
			// Serves as a hasMinValue function
			return -Float.MAX_VALUE;
		}
	}

	/**
	 * Validate against the absolute maximum value.
	 * @param node
	 * @return
	 */
	public float getMaxValue(Node node) {
		try {
			return Float.parseFloat(getAbsMax(node));
		} catch (Exception e) {
			//This is an optional attribute so if we can't get it and parse it, that is ok.
			// Serves as a hasMinValue function
			return Float.MAX_VALUE;
		}
	}

	public boolean isOptional(Node node) {
		if (node != null) {
			String value = getAttrValue(node,"required");
			if (value != null && value.equals("false")) return true;
		}
		return true; // default is to consider everything optional
	}

	/**
	 * After we create a DataItem (a primitive type), make sure its valid.
	 * This is called when reading input files so checks normally done by the UI can't be relied upon.
	 * However if you use the findNode method, you should know if a menu is correct already.
	 * I left some existing checks in place in case they are needed when converting to XML (see
	 * Validator.validateDataItem) but main checks are:
	 *    numeric value is in range - we check only against the absolute range.
	 * @param param
	 * @param definition
	 * @return
	 */
	public boolean validateDataItem(DataItem param, Node definition) {
		// If we're dealing with a string.

		boolean optional = isOptional(definition);
		boolean empty = param.getValue().isEmpty();

		if(optional && empty) return true;	// Nothing to validate :)


		if (isNumber(definition)) {
			System.err.println("validateDataItem currently commented out.");
			/*

			// Convert to default (min and max are in default values)
			try {
				String units = getUnits(definition);
				String fixedValue = param.getValue().replace("d","e"); // Dang fortran people....Not sure if this causes problems later
				Float curValue = hasUnits(definition) ? ParameterConverter.convertValue(
					fixedValue, param.getUnit(), getDefaultUnit(definition)).asFloat()
					: ParameterConverter.convertValue(fixedValue, "", "").asFloat();

				Float min = hasAbsMin(definition) ? getMinValue(definition) : null;
				Float max = hasAbsMax(definition) ? getMaxValue(definition) : null;
				if(min != null && max != null) {
					return curValue >= min && curValue <= max; // TODO: When we implement a 'warning' in the amanzi reader, bound check these.
				} else if(max != null) {
					return curValue <= max;
				} else if(min != null) {
					return curValue >= min;
				} else
					return true;
			} catch (Exception e) {
				//KLS TODO currently XML does not have this type

				//if(definition.getType().equalsIgnoreCase("file or number")) {
				//	// Might be a file, let it go?
				//	return true;
				//}
				return false;
			}

			 */
		}
		return true;
	}

	/**
	 * Debugging aide.
	 * @param node
	 */
	protected void dumpAttrs(Node node) {
		NamedNodeMap attrs = node.getAttributes();
		for (int i=0; i<attrs.getLength(); i++) {
			Attr attr = (Attr)attrs.item(i); 
//			System.out.println("Attr: "+attr.getName()+"/"+attr.getValue());
		}

	}

	/**
	 * Debugging aide.
	 * @param doc
	 */
	public void dump() {

		try {
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer transformer = tranFactory.newTransformer();

			Source src = new DOMSource(getDocument());
			Result dest = new StreamResult(System.out);

			transformer.transform(src, dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Backward compatible static versions...
	 * @param node
	 * @return
	 */
	public static String getSimulatorKey(Node node) {
		if (!node.hasAttributes())
			return "";
		Node key = node.getAttributes().getNamedItem("simulator_key");
		if (key != null)
			return key.getNodeValue();
		return "";
	}

	public static String getKey_s(Node node) {
		if (!node.hasAttributes())
			return "";
		Node key = node.getAttributes().getNamedItem("key");
		if (key != null)
			return key.getNodeValue();
		return "";
	}


	public static String getDefaultValue_s(Node node) {
		Node defaultValue = node.getAttributes().getNamedItem("default");
		if (defaultValue != null)
			return defaultValue.getNodeValue();
		return "";
	}	

	public static boolean getEnabled_s(Node node) {
		if(!node.hasAttributes())
			return true;
		Node enabled = node.getAttributes().getNamedItem("enabled");
		if(enabled != null) 
			return Boolean.parseBoolean(enabled.getNodeValue());
		return true; // True by default
	}

	public static String getLabel_s(Node node) {
		if (!node.hasAttributes())
			return "";
		Node label = node.getAttributes().getNamedItem("label");
		if (label != null)
			return label.getNodeValue();
		return "";
	}

	public static String getObjectType_s(Node node) {
		Node typeNode = node.getAttributes().getNamedItem("type");
		if (typeNode != null) {
			return typeNode.getNodeValue();
		}
		return "";
	}

	public static String getSelection_s(Node node) {
		Node selectionNode = node.getAttributes().getNamedItem("selection");
		if (selectionNode != null) {
			return selectionNode.getNodeValue();
		}
		return "";
	}

	protected void parseRules() {

		Node node = getElement("ruleset");
		try {
			if (node != null) { 
				// Rules are optional
				NodeList children = node.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node child = children.item(i);
					if (child instanceof Element) {
						if (child.getNodeName().equalsIgnoreCase(ABCConstants.Key.COMMENT)) {
						  continue;
						}
						ABCExpressionRule rule = ruleEvaluator.parseRule(child);
						if (rule == null) {
							System.err.println("WARNING: skipping unrecognized rule type:"+child.getNodeName() + "=" + ABCConstants.Key.COMMENT);
						} else {
						  ruleEvaluator.addRule(rule);
						}
					}
				}
			}
		} catch (Exception ex) {
			System.err.println("Some problem parsing rules"+ex.getMessage());
		}
		//ruleEvaluator.dump();
	}
	
	
	protected void createKeyMap() {
	  if (getDocument() != null) {
	    NodeList children = document.getDocumentElement().getChildNodes();
	    for(int i = 0; i < children.getLength(); i++) {
	      if(children.item(i).getNodeName().equals(ABCConstants.Key.COMPONENT)) {
	        addKeysToMap(children.item(i));
	      }
	    }
	  }
	}

	protected void addKeysToMap(Node node) {
	  NodeList children = node.getChildNodes();
	  for (int idx = 0; idx < children.getLength(); idx++) {
	    Node child = children.item(idx);
	    String key = getKey(child);
	    if (key != null && !key.isEmpty()) {
	      if (keyNodeMap.containsKey(key)) {
	        if (child.getParentNode().getParentNode() != keyNodeMap.get(key).getParentNode()) {
	          // Comment out for now since so many in the plotting
	          //System.err.println("WARNING: Possible duplicate key: "+key);
	        }
	      }
	      keyNodeMap.put(key, child);
	    } else {
	      String labelkey = getLabel(child);
	      if (child.getNodeName().equals(ABCConstants.Key.GROUP)) {
	        //  System.err.println("no key "+child.getNodeName()+" "+labelkey );
	        if (labelkey != null && !labelkey.isEmpty()) {
	          keyNodeMap.put(labelkey, child);
	        }
	      }
	    }
	    addKeysToMap(child);
	  }

	}
	
	public boolean containsKey(String key) {
	  return keyNodeMap.get(key)!=null;
	}
	
	public ABC getABC(String name) {
	  if(componentABCs.get(this.getId()) != null) {
	    String alias = Key.toAlias(name);
	    for (ABC abc: componentABCs.get(this.getId())) {
	      if (abc.getName().equalsIgnoreCase(alias))  {
	        return abc;
	      }
	    }

	  }
	  return null;
	}

	
	public DataItem find(String key) {
	  DataItem di = null;
		Key tmpKey = new Key(key,"XXX"); // switched this to key, key so we don't catch alias
		                                 // Changed again because if you have a label somewhere that
		                                 // is the same as a key, it gets confused
		for (ABC abc: componentABCs.get(this.getId())) {
		  di = abc.getView().rFind(tmpKey);
			if (di != null) break;
		}
	  return di;
	}

	public ABC findABC(String key) {
		ABC top = null;
		Node node = keyNodeMap.get(key);
		Key tmpKey = new Key(key,"XXX"); // switched this to key, key so we don't catch alias
		                                 // Changed again because if you have a label somewhere that
		                                 // is the same as a key, it gets confused
		    		
		if(componentABCs.get(this.getId()) != null) {
  		for (ABC abc: componentABCs.get(this.getId())) {
  			if (abc.getView().rExists(tmpKey)) {
  				top=abc;
  				break;
  			}
  		}
		}
		// Not an error if not created yet
		//if (top == null) System.err.println("Warning: Unable to find item with key: "+key);
		return top;
	}
	
	public DataItem  getTarget(String fieldKey,ABCComponent context) {
	  return getDataItem(fieldKey,context);
	}

	public void enable(String target,boolean bresult,ABCComponent context) {
	  Key key = new Key(target,null);
    enable(key,bresult,context);
	}
	public void enable(DataItem target,boolean bresult,ABCComponent context) {
     enable(target.getKey(),bresult,context);
	}
	public void enable(Key key,boolean bresult,ABCComponent context) {
	  boolean found = false;
	  if (context != null) {
	    found = context.rEnable(key,bresult);            
	  } 
	  if (!found) {
	    for (ABC abc: componentABCs.get(this.getId())) {
	      if (abc.getView().rEnable(key,bresult))
	        break;
	    }
	  }
	}

	public void show(String target,boolean bresult,ABCComponent context) {
	  Key key = new Key(target,null);
    show(key,bresult,context);
	}
	public void show(DataItem target,boolean bresult,ABCComponent context) {
     show(target.getKey(),bresult,context);
	}
	public void show(Key key,boolean bresult,ABCComponent context) {
	  boolean found = false;
	  if (context != null) {
	    found = context.rShow(key,bresult);            
	  } 
	  if (!found) {
	    for (ABC abc: componentABCs.get(this.getId())) {
	      if (abc.getView().rShow(key,bresult))
	        break;
	    }
	  }
	}

	public void setValue(String target,String value,ABCComponent context) {
	  DataItem di = getDataItem(target, context);
	  if (di != null) {
	    di.setValue(value);;
	  }
	}
	
	

	/**
	 * Respond to events issued by changes in the UI.
	 */
	@Override
	public void update(DataModelObservable arg0, DataModelChange event) {
	  
		
	  if (targetInProgress) return;

		if (event != null && event.getDetail() != null && !event.getDetail().isEmpty()) {
		  // We aren't doing anything on clear events right now.  Put this here so we can bypass in debugger
		  if (event.getDetail().equals(DataModelObservable.CLEARED)) 
		    return;

		}

		// Could also be a DataItemMap (for a section for example) - we are not doing anything with these now
		if(!(arg0 instanceof DataItem)) {
			return;
		}
		
		// We have to observe DataItem in order to observe ABCDataItem.  But we don't want to process both and
		// must process ABCDataItem to make use of UUIDs used in sets....
		if (!(arg0 instanceof ABCDataItem)) {
		  return;
		}

		ABCDataItem trigger = (ABCDataItem)arg0;
		
		if(event != null && event.getDetail() != null && event.getDetail().equals(DataItemMap.ADDED)) {
		  // Done in should observe but we have to do it here too.  Most things won't be findable in context
		  // until this ADDED event fires.
		  targetInProgress = true;
		  ruleEvaluator.evaluateTarget((DataItem)trigger);
		  targetInProgress = false;
		}

		if(event.getChange().equals(DataItem.VALUE) || event.getChange().equals(DataItem.UNIT)) {
		  // TODO: Do we want to filter any specific changes?

		  //System.out.println("processing change event"+event.source+" " +event.change);

		  String value = trigger.getValue();
		  if (value == null || (value != null && value.isEmpty())) return;

		  ruleEvaluator.evaluateTrigger(trigger );

		}
	}

	
	/**
	 * Find the evaluation context for a rule.
	 * If the item is in an expanded list, the context will be that list.
	 * Otherwise the context is global which is represented by null.
	 * @param di
	 * @return
	 */
	public ABCComponent getEvaluationContext(DataItem di) {
	  ABCComponent context = null;
	  for(ABC window: componentABCs.get(this.getId())) {
	    //context = window.getView().getContext(di);
	    context = window.getView().getABCComponent(di);
	    if(context != null) {
	      if (context.equals(window.getView())) context = null;  // top window which means its not in a list so we can look anywhere
	      break; // Data item should only be within one component
	    }
	  }
	  return context;
	  
	}
	
	
	/**
	 * Fill a map with all the DataItems found in the rule expression.
	 * The map keys will contain keys as in the xml document - ie no uuids in them
	 * @param rule
	 * @param triggerdi - could be trigger or target
	 * @return
	 */
	public Map<String,DataItem> getRuleExpressionVariables(ABCExpressionRule rule, DataItem refdi, ABCComponent context) {
	  // Build up the map of all the variables needed to evaluate this rule.
	  // They may be found across multiple windows.
	  Map<String,DataItem> ruleVariables = new HashMap<String,DataItem>(); 
	  List<String> variables = rule.getTriggerKeys();
	  for (String var: variables) {
	    boolean found = false;
	    
	    if (var.startsWith("$")) continue;

	    // Check the simple case where the incoming refdi has the same key as the variable we are looking for
	    if(refdi != null && refdi.getUnlinkedKey().equals(var)) {            
	      ruleVariables.put(refdi.getUnlinkedKey(), refdi);
	      //System.out.println("stashing trigger "+refdi.getKey().key+"="+refdi.getValue());
	      found = true;
	    } else {
	      //For 
	      String linkedVar = var;
	      if(refdi != null && refdi.getLinkedObject() != null) {
	          linkedVar += ABCConstants.Key.UUID_SEPERATOR + refdi.getLinkedObject();
	      }
	      DataItem di = getDataItem(linkedVar, context);
	      if (di != null) {
	        ruleVariables.put(var, di);
	        found = true;
	      }
	    }
// May not exist yet depending on cration order
//	    if (!found) throw new ABCException("Variable needed by rule expression not found: "+ var);
	  }
	  return ruleVariables;
	}
	
	/**
	 * 
	 * @param keyToFind should have uuid appended already if needed
	 * @param context
	 * @return
	 */
	public DataItem getDataItem(String keyToFind, ABCComponent context) {

	  DataItem foundItem = null;
	  if (context != null) {
	    // If we have a context, first try to find it there
	    foundItem = context.getRCollection().getItem(keyToFind);
	  }
	  if (foundItem == null) {
	    // Otherwise look globally
	    for (ABC abc: componentABCs.get(this.getId())) {
	      foundItem = abc.getView().getRCollection().getItem(keyToFind);
	      if (foundItem != null)  break;
	    }
	  }
	  return foundItem;
	}
	
		
	
	/**
	 * If a data item is a trigger in a rule, we need to observe.
	 * If a data item is a target in a rule, we need to observe and also apply the rules immediately.
	 *     Note: not sure why we can't wait for the add message - would have to ask Ellen.
   * To observe ABCDataItem events, you also have to observe DataItem events so we can't filter
   * those here.  Filter them in the update method instead.
	 * We have to observe that maps too in order to observe items in the map?
	 * 
	 * So basically we observer any container and all items with rules.
	 * We observe ABC items because for complex structures like sets, they have keys with UUIDs to uniquely
	 * identify them.
	 */
  @Override
  public boolean shouldObserve(DataModelObservable observable) {   
    if(observable instanceof ABCDataItem || observable instanceof DataItem) {
      DataItem di = (DataItem)observable; 
      
      if (ruleEvaluator.isTrigger(di.getUnlinkedKey()) || ruleEvaluator.isTarget(di.getUnlinkedKey())) {
      //System.out.println("shouldObserve "+di.getKey().getKey());
        // Evaluate?
        // We do this here because it wasn't reliable to look for ADDED events in the update method.
        // TODO debug and switch back to events
        if(ruleEvaluator.isTarget(di.getUnlinkedKey())) {
          ruleEvaluator.evaluateTarget(di);
        }
        return true;
      }
      return false;
    }
    return true;  // Maps and recursive maps
  }

  @Override
  public void startObserving(DataModelObservable observable) {  
  }

  @Override
  public void stopObserving(DataModelObservable observable) {  
  }


}

