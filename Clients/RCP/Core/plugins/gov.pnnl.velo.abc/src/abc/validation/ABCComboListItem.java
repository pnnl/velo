package abc.validation;

import java.util.ArrayList;
import java.util.List;

import vabc.ABCConstants;
import vabc.NodeWrapper;
import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.Key;

public class ABCComboListItem extends ABCDataItem {

  private static final long serialVersionUID = 1L;

  public static final String LIST_ITEM = "listItem";

  private List<ABCDataItem> items;

  public ABCComboListItem(NodeWrapper nodeWrapper, DataItem dataItem, List<ABCDataItem> items) {
    super(nodeWrapper, dataItem);
    this.items = items;
    for(DataModelObserver o: dataItem.getObservers()) {
      for(DataItem item: items) {
        item.addObserver(o);
      }
    }
    initialize(nodeWrapper);
  }

  @Override
  public List<String> validate() {

    List<String> errors = new ArrayList<String>();

    if(isRequired() && (getValue() == null || getValue().isEmpty() || getValue().equals(ABCConstants.Key.NOT_SET))) {
      errors.add("Error: " +  getAlias()  + " is a required parameter, the value cannot be empty");
      return errors;
    }

    return errors;
  }

  @Override
  public boolean matchesKey(datamodel.Key key) {
    if(key == null)
      return true;
    if(getKey().equals(key))
      return true;
    for(DataItem item: getItems()) {
      if(item.getKey().equals(key))
        return true;
    }
    return false;
  }
  
  public DataItem getKey(datamodel.Key key) {
    for(DataItem item: getItems()) {
      if(item.getKey().equals(key))
        return item;
    }
    return null;
  }
 
  public void setEnabled(Key key, boolean shouldEnable, Object source) {
    if(key != null && key.equals("aq_tort_fx")) {
      System.out.println();
    }
    if(key == null || getKey().equals(key)) {
      super.setEnabled(key, shouldEnable, source);
    } else {  
      for(DataItem item: items) {
        if(item.getKey().equals(key)) {
          ((ABCDataItem)item).setEnabled(key, shouldEnable, source);
        }
      }      
    }
  }
  
  public boolean isLastChild(DataItem item) {
    return items.indexOf(item) == items.size() - 1;
  }
 
  @Override
  public void initialize(NodeWrapper nodeWrapper) {
    // TODO Auto-generated method stub

  }

  @Override
  public void update(DataModelObservable observable, DataModelChange dataModelChange) { 
    super.update(observable, dataModelChange);  // If it was a value we also need to update our list items

    if(getParentItems().contains(observable)) {
      return; // Ignore these ones
    }

    if(dataModelChange.getChange().equals(VALUE)) {
      for(ABCDataItem item: getItems()) { 
        if(!(observable instanceof ABCComboListItem)) {
          if(item.getKey().equals(dataModelChange.getNewValue())) {
            // This one
            item.setValue(new Boolean(true).toString(), LIST_ITEM, ABCComboListItem.this);
          } else {
            item.setValue(new Boolean(false).toString(), LIST_ITEM, ABCComboListItem.this);
          }
        }
      }
    }
  }

  public ABCDataItem getSelected(String value) {
    for(ABCDataItem item: getItems()) { 
      if(item.getKey().equals(value) || item.getAlias().equalsIgnoreCase(value)) {
        // I think this happens if we have a shared item depending on the order
        // listeners were added in, go ahead and fix it...
        if(!new Boolean(item.getValue())) {
          updateListItems(value);
          if(!new Boolean(item.getValue())) {
            System.err.println("Book keeping not working on items in list, value should be 'true' " + item.getValue() + " for " + item.getKey().getKey());
            System.err.println("Hijacked item: " + getHijacked().getAllValues());
          }
        }
        /*
        if(!item.isValueSelected()) {
          updateListItems(value);
          if(!item.isValueSelected()) {
            System.err.println("Book keeping not working on items in list, isSelected[VALUE] should be 'true' " + item.isValueSelected() + " for " + item.getKey().getKey());
            System.err.println("Hijacked item: " + getHijacked().getAllValues());
          }
        }
        */
        return item;
      }
    }
    
    System.err.println("ABCTableRow.getSelected - item not found " + value + "  Items are:");
    for(ABCDataItem item: getItems()) {
      System.err.println(item.getKey().key);
    }
    System.err.println("");
    return null;
  }
  
  @Override
  public void addObserver(DataModelObserver observer) {
    super.addObserver(observer);
    for(ABCDataItem item: getItems()) { 
      item.addObserver(observer);
    }
  }

  private List<ABCDataItem> getItems() {
    if(items == null)
      items = new ArrayList<ABCDataItem>();
    return items;
  }

  private void updateListItems(String selection) {
    for(ABCDataItem item: getItems()) { 
      if(item.getKey().equals(selection)) {
        // This one
        item.setValue(new Boolean(true).toString());
      } else {
        item.setValue(new Boolean(false).toString());
      }
    }
  }
}
