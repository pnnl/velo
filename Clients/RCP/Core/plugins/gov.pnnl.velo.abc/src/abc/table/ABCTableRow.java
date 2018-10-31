
package abc.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.event.TableModelEvent;

import org.w3c.dom.Node;

import vabc.ABCConstants;
import vabc.IABCActionProvider;
import vabc.IABCDataProvider;
import vabc.NodeWrapper;
import abc.containers.ABC;
import abc.containers.ABCComponent;
import abc.containers.ABCPrimitive;
import abc.table.components.ABCButton;
import abc.table.components.ABCCheckBox;
import abc.table.components.ABCCheckBoxList;
import abc.table.components.ABCComboBox;
import abc.table.components.ABCCompoundUnit;
import abc.table.components.ABCCustomButton;
import abc.table.components.ABCLabel;
import abc.table.components.ABCTextArea;
import abc.table.components.ABCTextField;
import abc.table.components.ABCComboBox.ComboBox;
import abc.units.ABCUnitFactory;
import abc.validation.ABCCheckListItem;
import abc.validation.ABCDataItem;
import abc.validation.ABCDoubleItem;
import abc.validation.ABCIntegerItem;
import abc.validation.ABCComboListItem;
import abc.validation.ABCStringItem;
import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;
import datamodel.DataModelObservable.DataModelObserver;
import datamodel.Key;
import datamodel.collections.DataItemMap;

public class ABCTableRow implements Iterable<ABCTableCell>, DataModelObserver {

  private List<ABCTableCell> cells; 

  private Map<ABCDataItem, List<ABCTableRow>> children;
  // private ABCTableRow parent;  

  // Some data structures for the parent/child relationships of choices
  private ABCComboListItem parentItem;
  private ABCDataItem group; 

  // Keep track of which child was selected
  //  private ABCDataItem previousSelection;

  // UI data items
  private List<ABCDataItem> items;

  // Row span for components that span multiple rows (text areas)
  public int rowSpan;

  private IABCDataProvider dataProvider;
  private IABCActionProvider actionProvider;

  // The table this row belongs to
  private ABCTable table;

  public final String uuid = UUID.randomUUID().toString();

  public ABCTableRow(ABCTable table, NodeWrapper nodeWrapper, DataItemMap collection,
      IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

    this.dataProvider = dataProvider;
    this.actionProvider = actionProvider;

    this.table = table;
    rowSpan = 1;

    cells = new ArrayList<ABCTableCell>();
    items = new ArrayList<ABCDataItem>();

    addRow(nodeWrapper, collection.get(nodeWrapper.getString(ABCConstants.Key.KEY)));	

    //   for(ABCDataItem item: items) {
    //     item.addObserver(this);
    //   }
  }

  public ABCTableRow(ABCTable table, NodeWrapper nodeWrapper, ABCComponent parent, DataItem dataItem, 
      DataItemMap collection, IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

    this.dataProvider = dataProvider;
    this.actionProvider = actionProvider;

    this.table = table;		
    rowSpan = 1;

    cells = new ArrayList<ABCTableCell>();
    items = new ArrayList<ABCDataItem>();

    addRow(nodeWrapper, dataItem);	

    //   for(ABCDataItem item: items) {
    //     item.addObserver(this);
    //   }
  }

  public ABCTableRow(ABCTable table, NodeWrapper nodeWrapper, ABCTableRow parent, Key group,
      DataItemMap collection, IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

    this.dataProvider = dataProvider;
    this.actionProvider = actionProvider;

    this.table = table;
    //    this.parent = parent;

    rowSpan = 1;

    cells = new ArrayList<ABCTableCell>();
    items = new ArrayList<ABCDataItem>();

    addRow(nodeWrapper, collection.get(nodeWrapper.getString(ABCConstants.Key.KEY)));   

    if(cells.isEmpty())
      return;

    /*
    if(group != null) {           
      for(ABCDataItem item: items) {
        // We belong to a group
        item.setGroupSelected(parent != null && parent.isParentSelected(), ABCTableRow.this);
      }
      if(parent != null) {
        // If we're under the selected parent we should be visible
        if(parent.getSelection().getKey().equals(group)) {
          for(ABCDataItem item: items) {
            item.setVisible(parent.isParentSelected() && parent.isVisible(), ABCTableRow.this);
          }
        } else {
          for(ABCDataItem item: items) {
            item.setVisible(false, ABCTableRow.this);
          }
        }
      }
     */

    if(parent != null && group != null) {
      for(ABCDataItem item: parent.items) {
        if(item instanceof ABCComboListItem && item.matchesKey(group)) {
          this.parentItem = ((ABCComboListItem)item);          
          this.parentItem.addObserver(this); // Listen for changes in the combo list item
          this.group = (ABCDataItem)((ABCComboListItem)item).getKey(group);
          this.group.addObserver(this); // And this particular group item
          break; // Found it
        }
      }
      if(!parentItem.isVisible() || !parentItem.isShowing() || !parentItem.getSelected(parentItem.getValue()).equals(this.group)) {
        for(ABCDataItem item: items) {
          item.setVisible(false, ABCTableRow.this);
        }
      }
      parent.children.get(this.group).add(this);  
    }


    //    if(group != null) {
    //     parent.children.get(this.group).add(this);
    //  }
    // }

    //  for(ABCDataItem item: items) {
    //     item.addObserver(this);
    //   }

  }	

  private void addRow(NodeWrapper nodeWrapper, DataItem dataItem) {

    // Get what we need out of the node wrapper
    String nodeType = nodeWrapper.getNodeType();
    String dataType = nodeWrapper.getString(ABCConstants.Key.TYPE);

    String key = nodeWrapper.getString(ABCConstants.Key.KEY);
    String label = nodeWrapper.getString(ABCConstants.Key.LABEL);

    String defaultValue = nodeWrapper.getString(ABCConstants.Key.DEFAULT);

    String unitFamily = nodeWrapper.getString(ABCConstants.Key.UNITS);
    String defaultUnit = nodeWrapper.getString(ABCConstants.Key.DEFAULT_UNIT);

    Boolean hasAction = nodeWrapper.getBoolean(ABCConstants.Key.HAS_ACTION);
    Boolean couldBeFile = nodeWrapper.getBoolean(ABCConstants.Key.COULD_BE_FILE);
    Boolean isRequired = nodeWrapper.getBoolean(ABCConstants.Key.REQUIRED);

    Integer visibleRows = nodeWrapper.getInteger(ABCConstants.Key.VISIBLE_ROWS);

    String setLabel = nodeWrapper.getString(ABCConstants.Key.SET_LABEL);
    ABCDataItem setLabelItem = setLabel != null ? new ABCStringItem(null, new DataItem(new Key(ABCConstants.Key.SET_LABEL, setLabel))) : null;
    if(setLabelItem != null) {
      items.add(setLabelItem);
    }

    // Set any defaults in case some needed data is null
    // TODO: should set default in the xml
    if(hasAction == null) hasAction = false;    
    if(couldBeFile == null) couldBeFile = false;    
    if(isRequired == null) isRequired = false;
    if(visibleRows == null) visibleRows = 1;

    // If the data item is null and the user object is not, lets see if its in the user object
    if(dataItem == null && table.userObject != null && key != null) {
      dataItem = table.userObject.getItem(key);
    }

    // TODO: Remove this once we're sure there are no abc data items sneeking in here!
    if(dataItem != null && dataItem instanceof ABCDataItem) {
      dataItem = ((ABCDataItem)dataItem).getHijacked(); // Don't share the data item, just the hijacked item?
    }

    // If the data item already existed (either from the user or a shared item)
    // we don't want to override the value it currently holds with a default...
    if(dataItem != null) {
      // So we have an association with the xml labels and the data items
      if(dataItem.getAlias() == null || !dataItem.getAlias().equals(label)) {
        dataItem.setAlias(label, this);        
      }
      defaultValue = null;
    }

    if(nodeType.equals(ABCConstants.Key.DOUBLE) || nodeType.equals(ABCConstants.Key.INTEGER)) {

      if(couldBeFile)
        table.showFileButton();

      // TODO Put this back in when ready to clean up xml files
      if (unitFamily!= null && !unitFamily.isEmpty() && ABCUnitFactory.getABCUnits().doesFamilyExist(unitFamily) == false)  {
        System.err.println("Unit family has not been defined: "+unitFamily);
        //throw new ABCException("Unit family has not been defined: "+unitFamily);
      }

      ABCDataItem item = null;
      if(nodeType.equals(ABCConstants.Key.DOUBLE)) {
        item = dataItem != null && dataItem instanceof ABCDoubleItem ? (ABCDoubleItem) dataItem :
          new ABCDoubleItem(nodeWrapper, dataItem != null ? dataItem : new DataItem(new Key(key, label), defaultValue));
      } else {
        item = dataItem != null && dataItem instanceof ABCIntegerItem ? (ABCIntegerItem) dataItem :
          new ABCIntegerItem(nodeWrapper, dataItem != null ? dataItem : new DataItem(new Key(key, label), defaultValue));    
      }
      getItems().add(item);
      item.addObserver(this);

      // Label
      if(setLabel != null) {


        cells.add(new ABCLabel(setLabelItem, 0, 1, 1));
        cells.add(new ABCLabel(item, 1, 2, 1));

      } else {
        cells.add(new ABCLabel(item, 0, 3, 1));
      }

      // If we have units
      if(defaultUnit != null || unitFamily != null) {					

        // Field
        cells.add(new ABCTextField(item, 3, 2, 1));

        List<ABCDataItem> unitItems = new ArrayList<ABCDataItem>();

        if(unitFamily != null) {
          for(String unitValue: ABCUnitFactory.getABCUnits().getUnitFamily(unitFamily)) {
            unitItems.add(new ABCStringItem(null, new DataItem(new Key(unitValue), "false")));            
          }          
          // No unit family defined, make a list with just the default in it
        } else if(defaultUnit != null) {
          String unitValue = ABCUnitFactory.getABCUnits().getUnit(defaultUnit);
          unitItems.add(new ABCStringItem(null, new DataItem(new Key(unitValue), "false")));     
        }

        if(defaultUnit == null && !unitItems.isEmpty()) {
          defaultUnit = unitItems.get(0).getAlias();
        }

        boolean isCompoundUnit = unitFamily!=null && !unitFamily.isEmpty() && ABCUnitFactory.getABCUnits().isCompoundUnit(unitFamily);

        if(item.getUnit() == null || item.getUnit().isEmpty()) {
          item.getHijacked().setUnit(defaultUnit, this);
        } else {// Item has a unit, make sure it's the standard unit
          String standardUnit = isCompoundUnit ?  ABCUnitFactory.getABCUnits().getStandardCompoundUnit(item.getHijacked().getUnit()) :
            ABCUnitFactory.getABCUnits().getStandardUnit(item.getHijacked().getUnit());
          if(!standardUnit.equals(item.getHijacked().getUnit()))
            System.err.println("Formatting unit: " + item.getHijacked().getUnit() + " to " + standardUnit);
          item.getHijacked().setUnit(standardUnit, this);
        }


        // Can only be compound if we have a family
        if(isCompoundUnit) {
          cells.add(new ABCCompoundUnit(item, unitFamily, 5, 1, 1));
        } else {
          // Unit combo box
          cells.add(new ABCComboBox(unitItems, item, true, 5, 1, 1));
        }

      } else {

        // Field
        cells.add(new ABCTextField(item, 3, 3, 1));
      }

      if(!couldBeFile) {
        cells.add(new ABCLabel(6, 1, 1));
      } else {
        cells.add(new ABCButton(item, 6, 1, 1));
      }

      if(dataItem == null && table.userObject != null) {
        table.userObject.initializeItem(key, item.getHijacked());
      }

    } if(nodeType.equals(ABCConstants.Key.CHECK_BOX)) {

      ABCDataItem item = dataItem != null && dataItem instanceof ABCStringItem ? (ABCStringItem)dataItem : 
        new ABCStringItem(nodeWrapper, dataItem != null ? dataItem : new DataItem(new Key(key, label), defaultValue));			

      getItems().add(item);
      item.addObserver(this);

      // Label
      if(setLabel != null) {

        cells.add(new ABCLabel(setLabelItem, 0, 1, 1));
        cells.add(new ABCLabel(item, 1, 2, 1));

      } else {
        cells.add(new ABCLabel(item, 0, 3, 1));
      }

      // Check box
      cells.add(new ABCCheckBox( "", item, 3, 3, 1));

      cells.add(new ABCLabel(6, 1, 1));

      if(dataItem == null && table.userObject != null) {
        table.userObject.initializeItem(key, item.getHijacked());
      }

    } else if(nodeType.equals(ABCConstants.Key.CHOICE)) {

      ABCDataItem selectedItem = null;
      List<ABCDataItem> options = getChoices(nodeWrapper, false);
      List<ABCDataItem> disabledOptions = getChoices(nodeWrapper, true);

      ABCDataItem first = null;
      for(ABCDataItem enabled: options) {
        for(ABCDataItem disabled: disabledOptions) {
          if(enabled.matchesKey(disabled.getKey())) {
            enabled.setEnabled(disabled.getKey(), false, this);
          }
        }
        if(!enabled.isEnabled())
          continue; // Skip it if its disabled?
        if(first == null) 
          first = enabled;
        if(dataItem == null && selectedItem == null)
          selectedItem = enabled; // First by default
        if(dataItem != null && dataItem.getValue() != null && (enabled.getKey().equals(dataItem.getValue()) || enabled.getAlias().equals(dataItem.getValue()))) {
          selectedItem = enabled;
        } else if(defaultValue != null && enabled.getKey().equals(defaultValue)) {
          selectedItem = enabled;
        }    
        enabled.setValueSelected(false, ABCTableRow.this);
        enabled.setGroupSelected(false, ABCTableRow.this);
      }

      if(options.size() == 0) {
        return; // Will force this component not to be drawn???
      }

      if(children == null)
        children = new HashMap<ABCDataItem, List<ABCTableRow>>();
      for(ABCDataItem choice: options) {
        children.put(choice, new ArrayList<ABCTableRow>());
      }

      ABCComboListItem item = dataItem != null && dataItem instanceof ABCComboListItem ? (ABCComboListItem) dataItem : 
        new ABCComboListItem(nodeWrapper, dataItem != null ? dataItem : new DataItem(new Key(key, label), defaultValue), options);	

      if(items.contains(dataItem)) {
        System.out.println("Already contained the item?");
      }

      getItems().add(item);
      item.addObserver(this);

      if(selectedItem == null) {
        System.err.println("ABCTableRow:396 - Tried to select '" + item.getValue() + "' from drop down menu: '" + item.getKey() + "' but option wasn't in choices: " + options);
        selectedItem = first;
      }
      
      if(selectedItem != null) {
        selectedItem.setGroupSelected(true, ABCTableRow.this);
        selectedItem.setValueSelected(true, ABCTableRow.this);
        selectedItem.setValue(new Boolean(true).toString(), ABCTableRow.this);
        // this.setPreviousSelection(selectedItem);
        item.getHijacked().setValue(selectedItem.getKey().getKey());
      }

      ABCDataItem selected = item.getSelected(item.getValue());
      if(selected == null) {
        // Pick the first value

        // In regions we have shared items (axis in particular), multiple axis menu's will have different choices
        // If we set a default in the one that is not currently active, we'll mess up the correct ones default...

        // Commenting out the setValue, keeping the error message ... error message may not always be accurate

        // What if the item is shared?        
        System.err.println("ABCTableRow:396 - Tried to select '" + item.getValue() + "' from drop down menu: '" + item.getKey() + "' but option wasn't in choices: " + options);
        // What if it's an option in another shared data item??
        /*
          boolean exists = false;
          for(DataItem existingItem: items) {
            if(existingItem.getKey().equals(item.getKey()) && !existingItem.equals(item)) {
              exists = true;
              break;
            }
          }
          if(!exists) {
            item.getHijacked().setValue(first.getKey().getKey()); 
            System.err.println("Selecting '" + first + "' instead");            
          }
         */
      }


      // Label
      if(setLabel != null) {

        cells.add(new ABCLabel(setLabelItem, 0, 1, 1));
        cells.add(new ABCLabel(item, 1, 2, 1));

      } else {
        cells.add(new ABCLabel(item, 0, 3, 1));
      }
      // Choices combo box
      cells.add(new ABCComboBox(options, item, false, 3, 3, 1));

      cells.add(new ABCLabel(6, 1, 1));

      /*
      item.addObserver(new DataModelObserver() {
        @Override public boolean shouldObserve(DataModelObservable observable) {  return true;  }
        @Override public void startObserving(DataModelObservable observable) { }
        @Override public void stopObserving(DataModelObservable observable) { }
        @Override
        public void update(DataModelObservable observable, DataModelChange dataModelChange) {
          if(dataModelChange.getChange().equals(DataItem.VALUE)) { 
            // Make sure the table updates in the cases of children
            // I disabled this push from the combo box editor
            if(dataModelChange.getNewValue() == null)
              return; // TODO: Debug this, we shouldn't hit this?
            ((ABCTableModel)table.getModel()).setValueAt(((ABCComboListItem)observable)
                .getSelected(dataModelChange.getNewValue().toString()),
                table.indexOf(ABCTableRow.this), getSelectedIndex());
          }
        }        
      });
       */

      if(dataItem == null && table.userObject != null) {
        table.userObject.initializeItem(key, item.getHijacked());
      }    

    } else if(nodeType.equals(ABCConstants.Key.LIST)) {
      List<ABCDataItem> backingItems = new ArrayList<ABCDataItem>();      
      for(Object object: dataProvider.getObjects(dataType)) {
        String identifier = dataProvider.getIdentifier(object);
        String objectLabel = dataProvider.getLabel(identifier);
        DataItem objectDataItem = null;
        Key compoundKey = new Key(DataItemMap.makeKey(key, identifier), objectLabel);
        if(table.userObject != null && identifier != null) {
          objectDataItem = table.userObject.getItem(identifier);
          if(objectDataItem == null)
            objectDataItem = table.userObject.getItem(compoundKey.key); // try the compound key?
          // Set the alias and linked object
          if(objectDataItem != null) {
            // In case they are not set... 
            if(!objectDataItem.getAlias().equals(objectLabel))
              objectDataItem.setAlias(objectLabel, ABCTableRow.this);
            if(objectDataItem.getLinkedObject() == null)
              objectDataItem.setLinkedObject(identifier);    
            objectDataItem.setKey(compoundKey, ABCTableRow.this);       
          }
        }
        objectDataItem = objectDataItem != null ? objectDataItem : new DataItem(objectLabel, compoundKey, defaultValue != null ? defaultValue : "false", null, identifier);
        ABCStringItem item = new ABCStringItem(null, objectDataItem);
        backingItems.add(item);
        getItems().add(item); // Data model cares about the individual string items (selected, not selected)
        item.addObserver(this);
        if(table.userObject != null) {
          table.userObject.initializeItem(identifier, objectDataItem);
        }
      } 

      if(backingItems.isEmpty())
        return; // Will force this component not to be drawn???

      ABCCheckListItem backingItem = new ABCCheckListItem(nodeWrapper, new DataItem(new Key(key, label)), backingItems);
      getItems().add(backingItem);

      // Label
      if(setLabel != null) {

        cells.add(new ABCLabel(setLabelItem, 0, 1, 1));
        cells.add(new ABCLabel(backingItem, 1, 2, 1));

      } else {
        cells.add(new ABCLabel(backingItem, 0, 3, 1));
      }

      // Field
      rowSpan = Math.max(1, visibleRows);
      cells.add(new ABCCheckBoxList(backingItems, backingItem, 3, 3, rowSpan));

      cells.add(new ABCLabel(6, 1, 1));

    } else if(nodeType.equals(ABCConstants.Key.STRING)) {

      ABCDataItem item = dataItem != null && dataItem instanceof ABCStringItem ? (ABCStringItem) dataItem : 
        new ABCStringItem(nodeWrapper, dataItem != null ? dataItem : new DataItem(new Key(key, label), defaultValue));    

      getItems().add(item);
      item.addObserver(this);

      // Label
      if(setLabel != null) {

        cells.add(new ABCLabel(setLabelItem, 0, 1, 1));
        cells.add(new ABCLabel(item, 1, 2, 1));

      } else {
        cells.add(new ABCLabel(item, 0, 3, 1));
      }

      // Field
      cells.add(new ABCTextField(item, 3, 3, 1));

      if(hasAction) {
        table.showFileButton();
        if(actionProvider != null) {
          cells.add(new ABCCustomButton(item, table.userObject, actionProvider.getCustomAction(key), 6, 1, 1));  
        } else {
          System.err.println("Custom action requested but actionProvider not specified");
        }
      } else {
        cells.add(new ABCLabel(6, 1, 1));
      }

      if(dataItem == null && table.userObject != null) {
        table.userObject.initializeItem(key, item.getHijacked());
      }

    } else if(nodeType.equals(ABCConstants.Key.FILE)) {

      ABCDataItem item = dataItem != null && dataItem instanceof ABCStringItem ?  (ABCStringItem) dataItem : 
        new ABCStringItem(nodeWrapper, dataItem != null ? dataItem : new DataItem(new Key(key, label), defaultValue));		

      getItems().add(item);
      item.addObserver(this);

      // Label
      if(setLabel != null) {

        cells.add(new ABCLabel(setLabelItem, 0, 1, 1));
        cells.add(new ABCLabel(item, 1, 2, 1));

      } else {
        cells.add(new ABCLabel(item, 0, 3, 1));
      }

      // Field
      cells.add(new ABCTextField(item, 3, 3, 1));

      table.showFileButton();
      cells.add(new ABCButton(item, 6, 1, 1));	

      if(dataItem == null && table.userObject != null) {
        table.userObject.initializeItem(key, item.getHijacked());
      }

    } else if(nodeType.equals(ABCConstants.Key.FIELD)) {

      ABCDataItem item = dataItem != null && dataItem instanceof ABCStringItem ?  (ABCStringItem) dataItem : 
        new ABCStringItem(nodeWrapper, dataItem != null ? dataItem : new DataItem(new Key(key, label), defaultValue));    

      getItems().add(item);
      item.addObserver(this);

      // Label
      if(setLabel != null) {

        cells.add(new ABCLabel(setLabelItem, 0, 1, 1));
        cells.add(new ABCLabel(item, 1, 2, 1));

      } else {
        cells.add(new ABCLabel(item, 0, 3, 1));
      };

      // Area
      rowSpan = Math.max(1, visibleRows);
      cells.add(new ABCTextArea(item, 3, 3, rowSpan));

      if(hasAction) {
        table.showFileButton();
        if(actionProvider != null) {
          cells.add(new ABCCustomButton(item, table.userObject, actionProvider.getCustomAction(key), 6, 1, 1));  
        } else {
          System.err.println("Custom action requested but actionProvider not specified");
        }
      } else {
        cells.add(new ABCLabel(6, 1, 1));
      }      

      if(dataItem == null && table.userObject != null) {
        table.userObject.initializeItem(key, item.getHijacked());
      }
    } 

  }

  private List<ABCDataItem> getChoices(NodeWrapper nodeWrapper, boolean disabledOnly) {

    Boolean isRequired = nodeWrapper.getBoolean(ABCConstants.Key.REQUIRED);
    if(isRequired == null)
      isRequired = false;

    List<ABCDataItem> dataItems = new ArrayList<ABCDataItem>();

    if(disabledOnly) {
      String disabled = nodeWrapper.getString(ABCConstants.Key.DISABLED_ITEMS);
      if(disabled != null) {
        Object[] disabledItems = dataProvider.getDisabledItems(disabled);
        for(Object object: disabledItems) {
          if(object.equals(disabled))
            continue;          
          String identifier = dataProvider.getIdentifier(object);
          String label =  dataProvider.getLabel(identifier);        
          dataItems.add(new ABCStringItem(null, new DataItem(new Key(identifier, label), "false", null, identifier)));
        }
        Collections.sort(dataItems);
        return dataItems;
      }			
    }

    // If the list is linked to a check box item we'll have a link tag
    String link = nodeWrapper.getString(ABCConstants.Key.LINK);
    if(link != null) {
      List<String> linkedComponents = new ArrayList<String>();
      if(link.contains("and")) {
        for(String splitLink: link.split("and")) {
          linkedComponents.add(splitLink.trim());
        }
      } else {
        linkedComponents.add(link);
      }
      
      if(disabledOnly) {
        return new ArrayList<ABCDataItem>(); // None are disabled by default
      }
      ABC top = this.table.abcParent.getABCContainer();
      while(top.getABCParent() != null) {
        top = top.getABCParent();
      }

      for(String likedComponentKey: linkedComponents) {
        // Find the data item first
        DataItem linkedItemParent = top.getView().rFind(new Key(likedComponentKey));      
        ABCComponent linkedComponent = linkedItemParent != null ? top.getView().getABCComponent(linkedItemParent) : null;
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
            ABCStringItem linkedItem = new ABCStringItem(null, new DataItem(new Key(item.getKey().getAlias()), "false"));
            dataItems.add(linkedItem);
            item.addObserver(new LinkedItem(linkedItem));
          }
        } else {
          System.err.println("Could not find the link: " + likedComponentKey);
        }
      }

    } else {

      // Our children will either be a bunch of groups or a type
        String type = nodeWrapper.getString(ABCConstants.Key.TYPE);
        if(type != null) {
          if(disabledOnly)
            return dataItems; // No way to disable
          if(type.contains(",")) {
            for(String label: type.split(" *, *")) {
              dataItems.add(new ABCStringItem(null, new DataItem(new Key(label), "false")));
            }
            Collections.sort(dataItems);
            return dataItems;
          }
          for(Object object: dataProvider.getObjects(type)) {
            if(object.equals(type))
              continue;
            String identifier = dataProvider.getIdentifier(object);
            String label =  dataProvider.getLabel(identifier);        
            dataItems.add(new ABCStringItem(null, new DataItem(new Key(identifier, label), "false", null, identifier)));
          }
          Collections.sort(dataItems);
          if(!isRequired)
            dataItems.add(0, new ABCStringItem(null, new DataItem(new Key(ABCConstants.Key.NOT_SET, null), "true")));        
          return dataItems;
        }

        // Children are in the xml, add them when we parse them??
        Node node = nodeWrapper.getNode();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
          Node child = node.getChildNodes().item(i);
          if(!child.hasAttributes())
            continue;				
          NodeWrapper childNode = new NodeWrapper(child);
          Boolean enabled = childNode.getBoolean(ABCConstants.Key.ENABLED);
          String key = childNode.getString(ABCConstants.Key.KEY);
          String label = childNode.getString(ABCConstants.Key.LABEL);
          String tooltip = childNode.getString(ABCConstants.Key.TIP);
          if((label != null || key != null) && (!disabledOnly || enabled != null)) {
            if(disabledOnly && enabled)
              continue;	
            ABCStringItem newItem = null;
            if(key != null && label != null)
              newItem = new ABCStringItem(null, new DataItem(new Key(key, label), "false"));				
            else if(label != null)
              newItem = new ABCStringItem(null, new DataItem(new Key(label), "false"));	
            if(newItem != null) {
              newItem.setTooltip(tooltip);
              dataItems.add(newItem);          
            }
          }			
        }	
        if(!disabledOnly && !isRequired) {
          dataItems.add(0, new ABCStringItem(null, new DataItem(new Key(ABCConstants.Key.NOT_SET, null), "true")));
        }
      }
      return dataItems;// Don't sort these ones, they come from xml file
    }

    public boolean isVisible() {
      for(ABCDataItem item: items) {
        // Visibility comes from the implementer (rules), showing comes from internal abc
        if(!item.isShowing() || !item.isVisible())
          return false;
      }
      return true;
    }

    public boolean isLastChild() {
      if(!isChild())
        return false;
      return parentItem.isLastChild(group);
    }

    public boolean isExpanded() {
      for(ABCDataItem item: items) {
        if(item instanceof ABCComboListItem) {
          // Get the current selection
          ABCDataItem selection = ((ABCComboListItem)item).getSelected(((ABCComboListItem)item).getValue());
          return !children.get(selection).isEmpty(); // If the current selection has children
        }
      }
      return false;
    }
    /*
  public int getChildRows(ABCDataItem selection) {
//    System.out.println("Get child rows was called: " + selection);
    int total = 0;		
    if(selection == null || !hasChildren()) // Just one row, this one
      return total;    
    ABCDataItem selectedRow = null;
    for(ABCDataItem item: children.keySet()) {
      if(item.equals(selection)) {
        selectedRow = item;
      } else {
        for(ABCTableRow child: children.get(item)) {    
          for(ABCDataItem data: child.items) {
            data.setVisible(false, this);
          }
        }
      }
    }

    if(selectedRow == null) {
      return total;
    }

    // Process the selected one last in case we have shared items
    for(ABCTableRow child: children.get(selectedRow)) {
      boolean isVisible = true;
      for(ABCDataItem data: child.items) {
        data.setVisible(true, this);
        if(!data.isShowing())
          isVisible = false;
      }
      total += isVisible ? child.rowSpan : 0;      
      total += child.getChildRows(child.getSelection());
    }
    System.out.println("total: " + total);
    return total;    
  }*/

    public ABCTableCell getCell(int columnIndex) {
      for(ABCTableCell ABCTableCell: this) {
        if(ABCTableCell.compareTo(columnIndex) == 0)
          return ABCTableCell;
      }
      return null;			
    }

    public boolean isEditing() {
      for(ABCTableCell cell: cells) {
        if(cell.isEditable() && cell.isSelected()) {
          return true;
        }
      }   
      return false;
    }

    public ABCTableCell getPreviousEditableCell(boolean includeCurrent) {
      int startIndex = getSelectedIndex() - (includeCurrent ? 0 : 1);
      for(int i = startIndex < 0 ? cells.size() - 1 : startIndex; i >= 0; i--) {
        if(cells.get(i).isEditable()) return cells.get(i);
      }		
      return null;
    }

    public ABCTableCell getNextEditableCell(boolean includeCurrent) {
      int startIndex = getSelectedIndex() + (includeCurrent ? 0 : 1);
      for(int i = startIndex < 0 ? 0 : startIndex; i < cells.size(); i++) {
        if(cells.get(i).isEditable()) return cells.get(i);
      }		
      return null;
    }	

    public int getSelectedIndex() {
      for(int i = 0; i < cells.size(); i++) {
        if(cells.get(i).isSelected())
          return i;
      }
      return -1;
    }

    public ABCTableCell get(int index) {
      return cells.get(index);
    }

    public int size() {
      return cells.size();
    }

    @Override
    public Iterator<ABCTableCell> iterator() {
      return cells.iterator();
    }

    /*
  public ABCDataItem getPreviousSelection() {
    return previousSelection;
  }

  public void setPreviousSelection(ABCDataItem aValue) {
    this.previousSelection = aValue;
  }
     */

    public List<ABCDataItem> getItems() {
      return items;
    }

    public ABCTable getTable() {
      return table;
    }

    /*
  public ABCDataItem getSelection() {
    for(ABCTableCell cell: cells) {
      if(cell instanceof ABCComboBox) {
        return ((ABCComboBox)cell).getSelection();
      }
    }
    return null;
  }
     */

    public boolean isEmpty() {
      return cells.isEmpty();
    }

    public boolean hasChildren() {
      return children != null;
    }

    public boolean isChild() {
      return parentItem != null;
    }

    @Override
    public boolean shouldObserve(DataModelObservable observable) {
      return true;
    }

    @Override
    public void startObserving(DataModelObservable observable) { }

    @Override
    public void stopObserving(DataModelObservable observable) { }

    @Override
    public void update(DataModelObservable observable, DataModelChange dataModelChange) {    
      // System.out.println("Update occurred: " + observable + ", change: " + dataModelChange);

      // We're listening to changes from the group and the parent item, parent item will be the abc combo list item
      // group will be the specific combo box choice that needs to be selected for this row to be visible

      if(group != null && observable.equals(group)) {
        if(dataModelChange.getChange().equals(ABCDataItem.IS_SELECTED) && dataModelChange.getDetail().equals(ABCDataItem.GROUP)) {
          ((ABCTableModel)table.getModel()).fireTableChanged(new TableModelEvent(table.getModel()));
          // Always set visibility to false when the group is no longer selected, only set it to visible if the parent is also visible
          if(parentItem.isVisible() || !(Boolean)dataModelChange.getNewValue()) {
            for(ABCDataItem item: this.items) {
              item.setVisible((Boolean)dataModelChange.getNewValue(), ABCTableRow.this);
            }
          }
        }
      } else if(parentItem != null && observable.equals(parentItem)) {
        if(dataModelChange.getChange().equals(ABCDataItem.IS_VISIBLE)) {
          // Always set visibility to false when the parent is no longer visible, only set it to visible if the group is also selected
          if(group.isGroupSelected() || !(Boolean)dataModelChange.getNewValue()) {
            // I think we just need to fire this when the group changes? ((ABCTableModel)table.getModel()).fireTableChanged(new TableModelEvent(table.getModel()));
            for(ABCDataItem item: this.items) {
              item.setVisible((Boolean)dataModelChange.getNewValue(), ABCTableRow.this);
            }
          }
        }
      } else {
        if(dataModelChange.getChange().equals(DataItem.VALUE) || dataModelChange.getChange().equals(DataItem.UNIT) ||
            dataModelChange.getChange().equals(ABCDataItem.IS_ENABLED) || dataModelChange.getChange().equals(ABCDataItem.IS_SHOWING)) {
          ((ABCTableModel)table.getModel()).fireTableDataChanged();
        }
      }
    }
    
    private class LinkedItem implements DataModelObserver {

      private ABCDataItem link;
      public LinkedItem(ABCDataItem link) {
        this.link = link;
      }
      
      @Override
      public boolean shouldObserve(DataModelObservable observable) {
        return true;
      }

      @Override
      public void startObserving(DataModelObservable observable) {
        if(observable instanceof DataItem) {
          // it will be
          boolean visible = new Boolean(((DataItem)observable).getValue()); 
          link.setVisible(visible, ABCTableRow.this);
        }
      }

      @Override
      public void stopObserving(DataModelObservable observable) { }

      @Override
      public void update(DataModelObservable observable, DataModelChange dataModelChange) {
        if(dataModelChange.getChange() == DataItem.VALUE && observable instanceof DataItem) {
          // it will be
          boolean visible = new Boolean(((DataItem)observable).getValue()); 
          link.setVisible(visible, dataModelChange.getSource());
        }
      }
      
    }
  }
