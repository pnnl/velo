package abc.containers;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import abc.table.ABCTable;
import abc.validation.ABCDataItem;
import vabc.ABCConstants;
import vabc.ABCStyle;
import vabc.IABCDataProvider;
import vabc.IABCUserObject;
import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;
import datamodel.DataModelObservable.DataModelObserver;
import datamodel.Key;
import datamodel.collections.DataItemMap;

/** 
 * Should this be a primitive???
 * @author port091
 *
 */
public class ABCSet extends ABCComponent {

  private static final long serialVersionUID = 1L;
  private ParallelGroup parallelGroup;
  private SequentialGroup sequentialGroup;

  public ABCSet(ABC abcParent, ABCComponent parentSection, String key, String label, Node node, IABCUserObject abcUserObject) {
    super(abcParent, key, null, null, abcUserObject);

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    setBackground(ABCStyle.style().getBackgroundColor());

    JPanel allsetspanel = this;  // If not collapsible use current design

    boolean isCollapsible = false;
    boolean shouldCollapse = false;
    Node collapsibleAttr = node.getAttributes().getNamedItem("collapsible");
    if (collapsibleAttr != null && collapsibleAttr.getNodeValue().equals("true")) isCollapsible=true;
    Node shouldCollapseAttr = node.getAttributes().getNamedItem("collapsed");
    if (shouldCollapseAttr != null && shouldCollapseAttr.getNodeValue().equals("true")) shouldCollapse = true;

    if (isCollapsible) {
      CollapsibleGradientPanel p = new CollapsibleGradientPanel(label, null, null);	
      initializeLayout(p.getContent());
      this.setLayout(new BorderLayout());
      this.add(p, BorderLayout.CENTER);
      build(p.getContent(), parentSection, key, null, node, abcParent.dataProvider);
      p.getContent().setBackground(null);
      allsetspanel.setBorder(null); // override superclass
      p.collapse(shouldCollapse);
    } else {
      //TODO create border as in superclass
      initializeLayout(this);
      build(allsetspanel, parentSection, key, label, node, abcParent.dataProvider);
    }		
  }

  private void initializeLayout(JPanel contents) {
    GroupLayout groupLayout = new GroupLayout(contents);
    contents.setLayout(groupLayout);
    parallelGroup = groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
    sequentialGroup = groupLayout.createSequentialGroup();
    // sequentialGroup.addContainerGap(); Trying without gaps
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGroup(parallelGroup)
            //.addContainerGap()
            ));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(sequentialGroup));			
  }

  protected void build(JPanel parent, ABCComponent parentSection, String key, String label, Node node, IABCDataProvider dataProvider) {

    // Each set group contains a hidden ABCSection that uses the set node info
    // We need to remove the collapsible attribute or else we get nested collapsable panels which is kinda busy looking

    int totalElements = 0;
    Node clone = node.cloneNode(true);
    ((Element)clone).setAttribute("collapsible",null); // remove

    Border outsideBorder = null;

    Node alignmentNode = node.getAttributes().getNamedItem(ABCConstants.Key.ALIGNMENT);
    boolean verticalAlignment = alignmentNode == null || alignmentNode.getNodeValue().equals(ABCConstants.Key.ALIGNMENT_VERTICAL);

    // we have a link to a check box list??
    Node link = node.getAttributes().getNamedItem(ABCConstants.Key.LINK);

    if(link != null) {

      List<String> linkedComponents = new ArrayList<String>();
      if(link.getNodeValue().contains("and")) {
        for(String splitLink: link.getNodeValue().split("and")) {
          linkedComponents.add(splitLink.trim());
        }
      } else {
        linkedComponents.add(link.getNodeValue());
      }

      for(String linkedComponentKey: linkedComponents) {
        boolean firstItem = true;

        ABC top = this.abcParent;
        DataItem linkedItemParent = null;
        while(top.getABCParent() != null) {
          if(top.getView().rFind(new Key(linkedComponentKey)) != null) {
            linkedItemParent = top.getView().rFind(new Key(linkedComponentKey));
          }
          top = top.getABCParent();
        }


        // Find the data item first
        if(linkedItemParent == null) linkedItemParent = top.getView().rFind(new Key(linkedComponentKey));   
        // If its in our section
        if(linkedItemParent == null) linkedItemParent = parentSection.rFind(new Key(linkedComponentKey)); 

        ABCComponent linkedComponent = linkedItemParent != null ? top.getView().getABCComponent(linkedItemParent) : null;
        if(linkedComponent == null) linkedComponent = linkedItemParent != null ? parentSection.getABCComponent(linkedItemParent) : null;

        if(linkedComponent != null) {
          // Need the right primitive
          ABCPrimitive listPrimitive = null;
          for(ABCPrimitive primitive: linkedComponent.getPrimitives()) {
            if(primitive.getData().contains(linkedItemParent)) {
              listPrimitive = primitive;
              break;
            }
          }

          for(DataItem item: listPrimitive.getData()) {
            // We'll build a sub component for this, it's visibility will be linked to the value of the check list item
            JPanel setPanel = new JPanel();
            setPanel.setLayout(new BoxLayout(setPanel, verticalAlignment ? BoxLayout.PAGE_AXIS : BoxLayout.X_AXIS));
            setPanel.setBorder(BorderFactory.createMatteBorder(firstItem ? 0 : 1, 0, 0, 0, ABCStyle.style().getBorderColor()));
            setPanel.setBackground(ABCStyle.style().getBackgroundColor());

            Node setClone = clone.cloneNode(true);
            NodeList children = setClone.getChildNodes();

            // First label only.
            for(int j = 0; j < children.getLength(); j++) {
              if(children.item(j).hasAttributes()) {
                String objectLabel = item.getKey().getAlias(); 
                Attr labelAttr = children.item(j).getOwnerDocument().createAttribute(ABCConstants.Key.SET_LABEL);
                labelAttr.setTextContent(objectLabel);
                children.item(j).getAttributes().setNamedItem(labelAttr);
                break;
              }
            }

            ABCSection subSection = new ABCSection(abcParent, key, label, null, setClone, getUserObject());
            outsideBorder = subSection.getBorder();
            subSection.setBorder(null);

            // Attach the listener for the linked item
            item.addObserver(new LinkListener(setPanel, subSection));

            setPanel.add(subSection);
            for(ABCPrimitive primitive: subSection.getPrimitives()) {
              primitive.getData().setLinkedObject(item.getKey().key, ABCSet.this);
              super.addPrimitive(primitive);
            }

            parallelGroup.addComponent(setPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);    
            // Resize everything except tables vertically
            sequentialGroup.addComponent(setPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
            firstItem = false;
            totalElements++;
          }    

        } else {
          System.err.println("Cannot find the linked component attached to a set: " + linkedComponentKey);
        }
      }
    } else {

      // Otherwise we're looking for a list provided by the data provider
      Object[] items = dataProvider.getObjects(node.getAttributes().getNamedItem(ABCConstants.Key.TYPE).getNodeValue());
      List<Key> keys = new ArrayList<Key>();
      for(Object item: items) {
        String identifier = dataProvider.getIdentifier(item);
        String objectLabel =  dataProvider.getLabel(identifier);
        keys.add(new Key(identifier, objectLabel));
      }
      Collections.sort(keys);

      for(int i = 0; i < keys.size(); i++, totalElements++) {		

        Key objectKey = keys.get(i);

        // System.out.println(objectKey.key);

        JPanel setPanel = new JPanel();
        setPanel.setLayout(new BoxLayout(setPanel, verticalAlignment ? BoxLayout.PAGE_AXIS : BoxLayout.X_AXIS));
        setPanel.setBorder(BorderFactory.createMatteBorder(i == 0 ? 0 : 1, 0, 0, 0, ABCStyle.style().getBorderColor()));
        setPanel.setBackground(ABCStyle.style().getBackgroundColor());

        Node setClone = clone.cloneNode(true);
        NodeList children = setClone.getChildNodes();

        // First label only.
        for(int j = 0; j < children.getLength(); j++) {
          if(children.item(j).hasAttributes()) {
            String objectLabel = dataProvider.getLabel(objectKey.key); 
            Attr labelAttr = children.item(j).getOwnerDocument().createAttribute(ABCConstants.Key.SET_LABEL);
            labelAttr.setTextContent(objectLabel);
            children.item(j).getAttributes().setNamedItem(labelAttr);
            break;
          }
        }
        // A bit tricky, material solute sets need solutes as their parent but the parameters
        // belong to a regon.  For now the logic will be, if the abc parent has an object
        // we'll use that one first for getting the parameters out of the data model.
        // After that, we'll use the user object specified in the xml for the linked object.
        // In the case where the parent does not have a user object (assignments panel), we'll 
        // set the user object to the linked object.

        IABCUserObject userObject = null;
        IABCUserObject linkedObject = null;
        Object potentialUserObject = super.getUserObject();//abcParent.abcProvider.getObject(objectKey.key);
        if(potentialUserObject instanceof IABCUserObject) {
          userObject = (IABCUserObject)potentialUserObject;
        }

        // But we need to set the identifier to the solute
        potentialUserObject = dataProvider.getObject(objectKey.key);
        if(potentialUserObject instanceof IABCUserObject) {
          linkedObject = (IABCUserObject)potentialUserObject;
          if(userObject == null) {
            userObject = linkedObject;
            linkedObject = null;
          }
        }

        // Parent would be the material

        // Make sure our set has the right keys before building
        if(linkedObject != null)
          appendUUIDToKeys(setClone, objectKey.key);

        ABCSection subSection = new ABCSection(abcParent, key, label, null, setClone, userObject);
        outsideBorder = subSection.getBorder();
        subSection.setBorder(null);

        // Make sure the parent element is set correctly on all items
        for(ABCPrimitive primitive: subSection.getPrimitives()) {
          primitive.getData().setLinkedObject(objectKey.key, ABCSet.this);
          super.addPrimitive(primitive);
          if(linkedObject != null) {
            for(ABCDataItem item: ((ABCTable)primitive).getABCDataItems()) {
              item.addParentItem(linkedObject.getIdentifier());
            }
          }
        }

        setPanel.add(subSection);

        parallelGroup.addComponent(setPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);		
        // Resize everything except tables vertically
        sequentialGroup.addComponent(setPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
      }	
    }

    parent.setBorder(outsideBorder);

    // Nothing to show, hide the section
    if(totalElements == 0)
      this.setVisible(false); // Hide

  }

  /**
   * Recursive function to append the UUID's onto all the keys is a given xml node
   * @param node
   * @param objectIdentifier
   */
  private void appendUUIDToKeys(Node node, String objectIdentifier) {
    NodeList children = node.getChildNodes();
    for(int j = 0; j < children.getLength(); j++) {
      if(children.item(j).hasAttributes()) {
        // Not items in a combo box.. those can't have the UUID, selections end up in value not the key
        if(!children.item(j).getNodeName().equals(ABCConstants.Key.GROUP)) {
          Node keyAttribute = children.item(j).getAttributes().getNamedItem(ABCConstants.Key.KEY);
          if(keyAttribute != null) {
            keyAttribute.setNodeValue(DataItemMap.makeKey(keyAttribute.getNodeValue(), objectIdentifier));
          }
        }
      } 
      if(children.item(j).hasChildNodes()) {
        appendUUIDToKeys(children.item(j), objectIdentifier);
      }
    }
  }

  @Override
  public void addComponentToUI(ABCComponent component) {
    // Can't add a sub component under a set?
  }


  @Override
  public boolean isDynamic() {
    return false;
  }

  private class LinkListener implements DataModelObserver {

    private ABCSection listeningPrimitive;
    private JPanel container;

    public LinkListener(JPanel container, ABCSection listeningPrimitive) {
      this.container = container;
      this.listeningPrimitive = listeningPrimitive;
    }

    @Override
    public boolean shouldObserve(DataModelObservable observable) {
      return true;
    }

    @Override
    public void startObserving(DataModelObservable observable) {
      // Set the visibility here   
      if(observable instanceof DataItem) {
        // it will be
        boolean visible = new Boolean(((DataItem)observable).getValue()); 
        listeningPrimitive.rShow(null, visible);
        container.setVisible(visible);
      }
    }

    @Override
    public void stopObserving(DataModelObservable observable) { }

    @Override
    public void update(DataModelObservable observable, DataModelChange dataModelChange) {
      if(dataModelChange.getChange() == DataItem.VALUE && observable instanceof DataItem) {
        boolean visible = new Boolean(((DataItem)observable).getValue()); 
        listeningPrimitive.rShow(null, visible);
        container.setVisible(visible);
      }
    }
  }
}
