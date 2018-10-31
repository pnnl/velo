package abc.containers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.List;

import datamodel.DataModelObservable;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import vabc.ABCConstants;
import vabc.ABCDocument;
import vabc.ABCStyle;
import vabc.IABCActionProvider;
import vabc.IABCDataProvider;
import vabc.IABCErrorHandler;
import vabc.IABCUserObject;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.DataModelObservable.DataModelChange;
import datamodel.DataModelObservable.DataModelObserver;
import datamodel.Key;

/**
 * Top level of ABC Components
 * 
 * <component name="component_name" group="group_name">
 * 	<section ...
 *  <tabs ...
 *  <columns ...
 *  <table ...
 *  <expanded_list ...
 * </component>
 * 
 * @author port091
 *
 */
public class ABC extends JScrollPane implements DataModelObserver, IABCErrorHandler {

  private static final long serialVersionUID = 1L;

  protected IABCErrorHandler errorHandler;    // Optional
  protected IABCActionProvider actionProvider;  // Optional
  protected IABCDataProvider dataProvider;  // Required
  protected ABCDocument abcDocument;      // Required
  
  protected ABCSection view; // Top level view for this Component
  public int labelLength;
  public int unitLength;
  private ABC abcParent; // In case we're nested (expanded lists)

  
  /**
   * From outside the ABC package, this method should be called to build components
   * 
   * @param abcDocument
   * @param component
   * @param dataProvider
   * @param errorHandler
   * @param actionProvider
   */
  public ABC(ABCDocument abcDocument, String component, IABCDataProvider dataProvider,
      IABCErrorHandler errorHandler, IABCActionProvider actionProvider) {
    this(abcDocument, new Key(component), dataProvider, errorHandler, actionProvider);
  }

  public ABC(ABCDocument abcDocument, Key component, IABCDataProvider dataProvider,
      IABCErrorHandler errorHandler, IABCActionProvider actionProvider) {
    
    this.errorHandler = errorHandler;
    this.actionProvider = actionProvider;
    this.dataProvider = dataProvider;
    this.abcDocument = abcDocument;;
    
    setName(component.getAlias());
    if (abcDocument == null) return;
    build(component);
  }

  /**
   * For applications that want to use most of the schema but enforce more specific structure than 'components'
   * 
   * @param abcDocument
   * @param node
   * @param dataProvider
   * @param errorHandler
   * @param actionProvider
   */
  public ABC(ABCDocument abcDocument, Node node, IABCDataProvider dataProvider,
      IABCErrorHandler errorHandler, IABCActionProvider actionProvider) {
    
    this.errorHandler = errorHandler;
    this.actionProvider = actionProvider;
    this.dataProvider = dataProvider;
    this.abcDocument = abcDocument;

    Node nameNode = node.hasAttributes() ? node.getAttributes().getNamedItem("name") : null;
    String name = nameNode != null ? nameNode.getNodeValue() : null;
    setName(name);

    if (abcDocument == null) return;
 
    build(node);
  }
  
  /**
   * From inside the UI, this will be used to create the sections of expanded lists
   * 
   * @param dataItem
   * @param node
   * @param dataProvider
   * @param errorHandler
   * @param actionProvider
   * @param abcParent
   */
  public ABC(DataItem dataItem, Node node, IABCDataProvider dataProvider,
      IABCErrorHandler errorHandler, IABCActionProvider actionProvider, ABC abcParent) {
    
    this.errorHandler = errorHandler;
    this.actionProvider = actionProvider;
    this.dataProvider = dataProvider;
    this.abcDocument = abcParent.abcDocument;
    this.abcParent = abcParent;
    
    build(dataItem, node);
    
    view.getRCollection().addObserver(abcDocument);
    view.getRCollection().addObserver(this);  
  }

  public void build(String component) {
    build(new Key(component));
  }

  protected void build(Key component) {
    setName(component.getAlias());
    Node node = abcDocument.getComponent(component);
    build(node);
  }

  protected void build(Node node) {
    if (node == null) return;    

    // Building a component, register it with the abcdoc
    abcDocument.addComponentABC(this);

    build(null, node);

    view.getRCollection().addObserver(abcDocument);
    view.getRCollection().addObserver(this);	
 //   view.getRCollection().print("ABC");
  }

  protected void build(DataItem identifier, Node node) {

    labelLength = getMaxLabelLength(0, node, ABCConstants.Key.LABEL);
    unitLength = getMaxLabelLength(0, node, ABCConstants.Key.UNITS);
    int temp = getMaxLabelLength(0, node, ABCConstants.Key.DEFAULT_UNIT);
    if(temp > unitLength)
      unitLength = temp;

    // This makes key a required attribute for all components
    Node keyNode = node.hasAttributes() ? node.getAttributes().getNamedItem(ABCConstants.Key.KEY) : null;
    String key = keyNode != null ? keyNode.getNodeValue() : null;

    Node nameNode = node.hasAttributes() ? node.getAttributes().getNamedItem(ABCConstants.Key.NAME) : null;
    String name = nameNode != null ? nameNode.getNodeValue() : null;

    // If we have a user object associated with this entire component, grab it
    Node userObjectNode = node.hasAttributes() ? node.getAttributes().getNamedItem(ABCConstants.Key.USER_OBJECT) : null;
    String userObjectType = userObjectNode != null ? userObjectNode.getNodeValue() : null;
    IABCUserObject userObject = null;
    
    if(userObjectType != null) {
      Object potentialUserObject = dataProvider.getObject(userObjectType);
      if(potentialUserObject instanceof IABCUserObject) {
        userObject = (IABCUserObject)potentialUserObject;  
      }
    }
    
    view = new ABCSection(this, key, null, identifier, node, userObject);
    if (!dataProvider.shouldShow(node)) {
      System.out.println("Hiding entire abc section: ABC.135: " + name);
      view.show(null, false);
    }

    this.setViewportView(view);

    // Parse the component for sub
    setBackground(ABCStyle.style().getBackgroundColor());

    getVerticalScrollBar().setUnitIncrement(7);

    buildChildren(view, node);  

    setBorder(null);
    for(Component child: view.getComponents()) {
      if(child instanceof JComponent) {
        Border currentBorder = ((JComponent)child).getBorder();
        if(currentBorder != null && currentBorder instanceof TitledBorder) {
          continue;
        }				
        ((JComponent)child).setBorder(null);
      }
    }

  }

  private void buildChildren(ABCComponent parentSection, Node node) {

    NodeList children = node.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {

      Node childNode = children.item(i);
      String type = childNode.getNodeName();

      // This makes key a required attribute for all components
      Node keyNode = childNode.hasAttributes() ? childNode.getAttributes().getNamedItem(ABCConstants.Key.KEY) : null;
      String key = keyNode != null ? keyNode.getNodeValue() : null;

      Node labelNode = childNode.hasAttributes() ? childNode.getAttributes().getNamedItem(ABCConstants.Key.LABEL) : null;
      String label = labelNode != null ? labelNode.getNodeValue() : null;

      ABCComponent childSection = null;
     
      if(type.equals(ABCConstants.Key.COMMENT)) continue;
      
      if(type.equals(ABCConstants.Key.SECTION)) {
        childSection = new ABCSection(this, key, label, null, childNode, parentSection.getUserObject());
        parentSection.addComponent(childSection); 
      } 

      // TODO: Sets cannot contain sections, might want to set this up more like expanded lists?
      else if(type.equals(ABCConstants.Key.SET)) {				
        childSection = new ABCSet(this, parentSection, key, label, childNode, parentSection.getUserObject());
        parentSection.addComponent(childSection); 
      } 

      else if(type.equals(ABCConstants.Key.COLUMNS)) {
        childSection = new ABCColumns(this, key, label, childNode, parentSection.getUserObject());	
        parentSection.addComponent(childSection); 
      }

      else if(type.equals(ABCConstants.Key.TABS)) {
        childSection = new ABCTabs(this, key, label, parentSection.getUserObject());	
        parentSection.addComponent(childSection); 
      }	

      // Order is important, leave this before adding to parent section...
      if (childSection != null && !dataProvider.shouldShow(childNode)) {
        System.out.println("We shouldn't show this section (ABC.200): " + label + " " + type);
        childSection.rShow(null, false); // Let's give this a try...
      }

      if(childSection != null) {
        // Don't build expanded list children
        buildChildren(childSection, childNode);
      }

      if(type.equals(ABCConstants.Key.EXPANDED_LIST)) {	
        parentSection.addComponent(new ABCExpandedList(this, key, label, childNode, parentSection.getUserObject()));
      }

      if(type.equals(ABCConstants.Key.CUSTOM_TABLE)) { 
        parentSection.addComponent(new ABCCustomTable(this, key, label, childNode, parentSection.getUserObject()));
      }
    }		
  }


  private int getMaxLabelLength(int max, Node node, String attribute) {

    if(node.hasAttributes() && node.getAttributes().getNamedItem(attribute) != null) {
      if (!node.getNodeName().equals(ABCConstants.Key.GROUP)) {
        String label = node.getAttributes().getNamedItem(attribute).getNodeValue().toString();
        for(Font font: ABCStyle.style().getFonts()) {
          int length = this.getFontMetrics(font).stringWidth(label);
          if(length > max)
            max = length;				
        }
      }
    }

    NodeList children = node.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      int childLength = getMaxLabelLength(max, children.item(i), attribute);
      if(childLength > max)
        max = childLength;
    }

    return max;
  }

  public ABCSection getView() {
    return view;
  }

  public static ABC getTop(JComponent jc) {
    Container c;
    ABC top = null;
    while ((c = jc.getParent()) != null) {
      if (c instanceof ABC) {
        top = (ABC)jc;
        break;
      }
    }
    return top;
  }

  @Override
  public void update(DataModelObservable observable, DataModelChange dataModelChange) {
    if(observable instanceof ABCDataItem) {
      updateErrors((ABCDataItem)observable);
    }
  }        		
  
  private void updateErrors(ABCDataItem dataItem) {
    if(this.errorHandler == null) // Do nothing if this was not provided
      return;
    
    List<String> errors = dataItem.getErrors();
 //   System.out.println("Error checking debug [ABC:206]: Requesting item is active: " + dataItem.isActive() + ", item: " + dataItem);
    boolean debug = false;//true;//false;
    if(debug) {
      // Put everything, active or not!
      if(!errors.isEmpty()) {
        errorHandler.pushErrors(String.valueOf(dataItem.getUUID()), errors);
      } else {
        errorHandler.clearErrors(String.valueOf(dataItem.getUUID()));  
      }
    } else {
      if(!errors.isEmpty() && dataItem.isActive()) {
        // System.out.println("Pushing errors: " + errors);
        errorHandler.pushErrors(String.valueOf(dataItem.getUUID()), errors);
      } else {
        // System.out.println("Clearing errors");
        errorHandler.clearErrors(String.valueOf(dataItem.getUUID()));
      }
    }
  }

  
  
  public ABC getABCParent() {
    return abcParent;
  }

  @Override
  public boolean shouldObserve(DataModelObservable observable) {
    return true; // Observe everything
  }

  @Override
  public void startObserving(DataModelObservable observable) {
    if(observable instanceof ABCDataItem)
      updateErrors((ABCDataItem)observable);
  }

  @Override
  public void stopObserving(DataModelObservable observable) {   
    if(errorHandler == null) // If this was not provided
       return;
    // clear the errors
    if(observable instanceof ABCDataItem)
      errorHandler.clearErrors(((ABCDataItem)observable).getUUID());
  }

  @Override
  public void pushErrors(String key, List<String> errors) {
    if(errorHandler == null) // Do nothing if this was not provided
      return;
    errorHandler.pushErrors(key, errors);
  }

  @Override
  public void clearErrors(String key) {
    if(errorHandler == null) // Do nothing if this was not provided
      return;
    errorHandler.clearErrors(key);
  }

}
