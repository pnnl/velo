package abc.validation;

import java.util.ArrayList;
import java.util.List;

import vabc.ABCConstants.Key;
import vabc.NodeWrapper;
import datamodel.DataItem;
import datamodel.DataModelObservable;

public class ABCCheckListItem extends ABCDataItem {

  private static final long serialVersionUID = 1L;

  public static final String LIST_ITEM = "listItem";

  private List<ABCDataItem> items;

  private Integer absoluteMin;
  private Integer absoluteMax;
  private Integer suggestedMin;
  private Integer suggestedMax;
  private int selected = 0;

  public ABCCheckListItem(NodeWrapper nodeWrapper, DataItem dataItem, List<ABCDataItem> items) {
    super(nodeWrapper, dataItem);
    this.items = items;
    selected = this.countSelected();
    for(ABCDataItem item: items) {
      item.addObserver(this);    
    }
    initialize(nodeWrapper);
  }
  
  @Override
  public void initialize(NodeWrapper nodeWrapper) {

    if(nodeWrapper == null)
      return;

    absoluteMin = nodeWrapper.getInteger(Key.ABSOLUTE_MIN);
    absoluteMax = nodeWrapper.getInteger(Key.ABSOLUTE_MAX);
    suggestedMin = nodeWrapper.getInteger(Key.SUGGESTED_MIN);
    suggestedMax = nodeWrapper.getInteger(Key.SUGGESTED_MAX);

  }

  @Override
  public List<String> validate() {

    List<String> errors = new ArrayList<String>();


    // Validate bounds
    if(absoluteMin != null && selected < absoluteMin) {
      errors.add("Error: " +  getAlias()  + " must have at least " + absoluteMin + " items selected, current number is " + selected);
    }

    if(absoluteMax != null && selected > absoluteMax) {
      errors.add("Error: " +  getAlias()  + " must not have more than " + absoluteMax + "items selected, current number is " + selected);
    }

    if(suggestedMin != null && selected < suggestedMin) {
      errors.add("Warning: the suggested minimum items selected for " +  getAlias()  + " is " + suggestedMin + ", current number is " + selected);
    }

    if(suggestedMax != null && selected > suggestedMax) {
      errors.add("Warning: the suggested maximum items selected for " +  getAlias()  + " is " + suggestedMax + ", current number is " + selected);
    }

    return errors;
  }


  @Override
  public void update(DataModelObservable observable, DataModelChange dataModelChange) { 
    if(items != null && items.contains(observable)) {     
      if(dataModelChange.getChange() == null || dataModelChange.getNewValue() == null)
        return;
      if(dataModelChange.getChange().equals(VALUE)) {
        if(dataModelChange.getNewValue().equals(new Boolean(true).toString())) {
          selected++;
        } else 
          selected--;
        this.notifyObservers(new DataModelChange(dataModelChange.getSource(), LIST_ITEM, null, countSelected()));
      }
    } else {
      super.update(observable, dataModelChange);  // If it was a value we also need to update our list items
      if(getParentItems().contains(observable)) {
        return; // Ignore these ones
      }
      if(dataModelChange.getChange().equals(VALUE)) {
        for(ABCDataItem item: items) { 
          if(!(observable instanceof ABCComboListItem)) {
            if(item.getKey().equals(dataModelChange.getNewValue())) {
              // This one
              item.setValue(new Boolean(true).toString());
            } else {
              item.setValue(new Boolean(false).toString());
            }
          }
        }        
      }
    }
  }

  public int countSelected() {
    int selected = 0;
    for(DataItem item: items) {
      if(Boolean.valueOf(item.getValue())) {
        selected++;
      }
    }
    return selected;
  }

  @Override
  public boolean shouldObserve(DataModelObservable observable) {
    if(items != null && items.contains(observable)) {
      return true; // Observe our own items        
    }
    return super.shouldObserve(observable);
  }

}
