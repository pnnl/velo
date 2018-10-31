package abc.validation;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vabc.StringUtils;
import vabc.ABCConstants;
import vabc.ABCStyle;
import vabc.NodeWrapper;
import datamodel.DataModelObservable;
import datamodel.Key;
import datamodel.DataItem;
import datamodel.DataModelObservable.DataModelObserver;

public abstract class ABCDataItem extends DataItem implements DataModelObserver {

  private static final long serialVersionUID = 1L;

  /**
   * Used for DataModelChanges when defaultValue has been modified
   * @DataModelChange.getChange()
   */
  public final static String DEFAULT_VALUE = "defaultValue";

  /**
   * Used for DataModelChanges when appDefaultValue has been modified
   * @DataModelChange.getChange()
   */
  public final static String APP_DEFAULT = "appDefaultValue";

  /**
   * Used for DataModelChanges when defaultUnit has been modified
   * @DataModelChange.getChange()
   */
  public final static String DEFAULT_UNIT = "defaultUnit";

  /**
   * Used for DataModelChanges when isRequired has been modified
   * @DataModelChange.getChange()
   */
  public final static String IS_REQUIRED = "isRequired";

  /**
   * Used for DataModelChanges when isVisible has been modified
   * @DataModelChange.getChange()
   */
  public final static String IS_VISIBLE = "isVisible";

  /**
   * Used for DataModelChanges when isEnabled has been modified
   * @DataModelChange.getChange()
   */
  public final static String IS_ENABLED = "isEnabled";

  /**
   * Used for DataModelChanges when isReadOnly has been modified
   * @DataModelChange.getChange()
   */
  public final static String IS_READONLY = "isReadOnly";

  /**
   * Used for DataModelChanges when isSelected has been modified
   * @DataModelChange.getChange()
   */
  public final static String IS_SELECTED = "isSelected";

  /**
   * Used for DataModelChanges when isSelected has been modified
   * @DataModelChange.getChange()
   */
  public final static String IS_SHOWING = "isShowing";


  /**
   * Used for DataModelChanges when isSelected has been modified
   * @DataModelChange.getChange()
   */
  public final static String BACKGROUND = "background";
  public final static String GROUP = "group"; // Used to tell the difference between background color for groups and row or table selections?
  
  /**
   * Used for DataModelChanges when isSelected has been modified
   * @DataModelChange.getChange()
   */
  public final static String BUTTON = "button";

  /**
   * Used for DataModelChanges when couldBeFile has been modified
   * @DataModelChange.getChange()
   */
  public final static String COULD_BE_FILE = "couldBeFile";

  /**
   * Used for DataModelChanges when isFile has been modified
   * @DataModelChange.getChange()
   */
  public final static String IS_FILE = "isFile";

  /**
   * Used for DataModelChanges when parents have been modified
   * @DataModelChange.getChange()
   */
  public final static String PARENT_ITEM = "parentItem";

  /**
   *  The original item
   */
  private DataItem hijacked;

  // Used for setting initial ui values and ui colors
  private String defaultValue;  // UI default
  private String appDefaultValue;  // useful for multistate choices: off is ui default but when !off, some other value is the default
  private String defaultUnit;

  private String tooltip; // TODO

  // Used for error checking
  private boolean isRequired;

  // Used internally to determine if the item is visible and enabled
  private boolean isVisible; 
  private boolean isShowing; // Used by abc only
  private boolean isEnabled;
  private boolean isReadOnly;

  private Map<String, Boolean> isSelected; // Need a boolean for value and unit


  private boolean couldBeFile; // Set from node, can be changed
  private boolean isFile; // Set by the UI, if we know if the value is a file or not...

  private List<DataItem> parentItems; // TODO
  
  private String uuid;

  public ABCDataItem(NodeWrapper nodeWrapper, DataItem dataItem) {
    super(dataItem);
    this.uuid = UUID.randomUUID().toString(); 
    this.hijacked = dataItem;

    initialize(nodeWrapper);
    
    parentItems = new ArrayList<DataItem>();

    // Defaults
    isVisible = true;
    isFile = false;

    isRequired = false;
    isReadOnly = false;
    couldBeFile = false;
    isEnabled = true;    
    isShowing = true;
    isSelected = new HashMap<String, Boolean>();
    isSelected.put(GROUP, false);
    isSelected.put(BACKGROUND, false);
    isSelected.put(VALUE, false);
    isSelected.put(UNIT, false);
    isSelected.put(BUTTON, false);

    if(nodeWrapper != null) {
      defaultValue = nodeWrapper.getString(ABCConstants.Key.DEFAULT);
      defaultUnit = nodeWrapper.getString(ABCConstants.Key.DEFAULT_UNIT);
      appDefaultValue = nodeWrapper.getString(ABCConstants.Key.INTERNAL_DEFAULT);
      tooltip = nodeWrapper.getString(ABCConstants.Key.TIP);

      Boolean isRequired = nodeWrapper.getBoolean(ABCConstants.Key.REQUIRED);
      Boolean isReadOnly = nodeWrapper.getBoolean(ABCConstants.Key.READONLY);
      Boolean couldBeFile = nodeWrapper.getBoolean(ABCConstants.Key.COULD_BE_FILE);
      Boolean isEnabled = nodeWrapper.getBoolean(ABCConstants.Key.ENABLED);

      this.isRequired = isRequired != null ? isRequired : false;
      this.isReadOnly = isReadOnly != null ? isReadOnly : false;
      this.couldBeFile = couldBeFile != null ? couldBeFile : false;
      this.isEnabled = isEnabled != null ? isEnabled : true;   

      if(nodeWrapper.getNodeType().equals(ABCConstants.Key.FILE)) {
        this.couldBeFile = true;
        isFile = true;
      }
    }

    // Merge in all our hijacked items observers
    Object[] observers = hijacked.getObservers().toArray();
    for(Object observer: observers)
      this.addObserver((DataModelObserver)observer);

    this.hijacked.addObserver(this);
    
 
  }	

  public abstract void initialize(NodeWrapper nodeWrapper);

  public Font getFieldFont() {
    Font font = ABCStyle.style().getDefaultFont(); 
    if(isDefaultValue()) {
      font = font.deriveFont(Font.ITALIC);
    }
    return font;
  }

  public Color getFieldForegroundColor() {
    if(!isActive() || isReadOnly())
      return ABCStyle.style().getTextDisabledColor();    
    if(hasErrors())
      return ABCStyle.style().getErrorColor();
    if(isDefaultValue())
      return ABCStyle.style().getDefaultColor();    
    return ABCStyle.style().getFieldForegroundColor();
  }

  public Color getFieldBackgroundColor() {
    if(!isActive() || isReadOnly())
      return ABCStyle.style().getFieldDisabledColor(); 
    //    if(isBackgroundSelected())
    //      return ABCStyle.style().getSelectionColor();
    return ABCStyle.style().getFieldBackgroundColor();
  }

  public Color getUnitBackgroundColor() {
    if(!isActive() || isReadOnly())
      return ABCStyle.style().getFieldDisabledColor(); 
    //    if(isBackgroundSelected())
    //      return ABCStyle.style().getSelectionColor();
    return ABCStyle.style().getFieldBackgroundColor();
  }

  public Color getUnitForegroundColor() {
    if(!isActive() || isReadOnly())
      return ABCStyle.style().getTextDisabledColor();    
    if(hasErrors())
      return ABCStyle.style().getErrorColor();
    //  if(isDefaultUnit())
    //   return ABCStyle.style().getDefaultColor();  
    return ABCStyle.style().getFieldForegroundColor();
  }

  public Font getLabelFont() {
    if(isRequired() || getKey().equals(ABCConstants.Key.SET_LABEL))
      return ABCStyle.style().getRequiredFont();
    return ABCStyle.style().getOptionalFont();
  }

  public Color getLabelColor() {
    if(!isActive() || isReadOnly())
      return ABCStyle.style().getTextDisabledColor();  
    if(hasErrors())
      return ABCStyle.style().getErrorColor();
    if(getKey().equals(ABCConstants.Key.SET_LABEL))
      return ABCStyle.style().getSetLabelColor();
    return ABCStyle.style().getForegroundColor();
  }

  public Color getLabelBackgroundColor() {
    if(isBackgroundSelected())
      return ABCStyle.style().getSelectionColor();
    return ABCStyle.style().getBackgroundColor();
  }
  
  public Color getBackgroundColor() {
    
    if(isGroupSelected())
      return ABCStyle.style().getSelectionColor();

    if(isBackgroundSelected())
      return ABCStyle.style().getSelectionColor();
    
    return ABCStyle.style().getFieldBackgroundColor();
  }

  public Color getBorderColor(String field) {
    if((field.equals(VALUE) && isValueSelected()) || (field.equals(UNIT) && isUnitSelected()) || (field.equals(BUTTON) && isButtonSelected())) {
      if(isGroupSelected())
        return ABCStyle.style().getSelectedGroupBorderColor();
      return ABCStyle.style().getSelectionColor();
    }
    if(field.equals(VALUE) || field.equals(UNIT))
      return ABCStyle.style().getFieldBorderColor();
    return ABCStyle.style().getBackgroundColor();
  }

  public void addParentItem(DataItem item) {
    if(!parentItems.contains(item)) {
      this.parentItems.add(0, item);
      item.addObserver(this);
    }
  }

  public String getDefaultUnit() {
    return defaultUnit;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public boolean couldBeFile() {
    return couldBeFile;
  }

  public boolean isReadOnly() {
    return isReadOnly;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public boolean isEditable() {
    return isVisible && isEnabled && isShowing && !isReadOnly;
  }

  public boolean isActive() {
    return isVisible && isShowing && isEnabled;
  }

  public boolean isVisible() {
    return isVisible;
  }

  public boolean isValueSelected() {
    return isSelected.get(VALUE);
  }

  public boolean isShowing() {
    return isShowing;
  }

  public boolean isUnitSelected() {
    return isSelected.get(UNIT);
  }

  public boolean isButtonSelected() {
    return isSelected.get(BUTTON);
  }

  public boolean isBackgroundSelected() {
    return isSelected.get(BACKGROUND);
  }

  public boolean isGroupSelected() {
    return isSelected.get(GROUP);
  }
  
  public boolean isFile() {
    return isFile;
  }

  public boolean isRequired() {
    return isRequired;
  }

  public String getTooltip() {
    return tooltip;
  }  

  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  public void setReadOnly(boolean shouldBeReadOnly, Object source) {
    if(isReadOnly != shouldBeReadOnly) {
      boolean temp = this.isReadOnly;
      this.isReadOnly = shouldBeReadOnly;
      this.notifyObservers(new DataModelChange(source, IS_READONLY, temp, this.isReadOnly));
    }
  }

  public void setEnabled(Key key, boolean shouldEnable, Object source) {
    if(isEnabled != shouldEnable) {
      boolean temp = this.isEnabled;
      this.isEnabled = shouldEnable;
      this.notifyObservers(new DataModelChange(source, IS_ENABLED, temp, this.isEnabled));
    }
  }

  /**
   * Used externally by users to toggle the visibility of a field.
   * @param isVisible
   * @param source
   */
  public void setShowing(boolean isShowing, Object source) {   
    if(this.isShowing != isShowing) {
      boolean temp = this.isShowing;
      this.isShowing = isShowing;
      this.notifyObservers(new DataModelChange(source, IS_SHOWING, temp, this.isShowing));
    }
  }

  /**
   * Used internally to show or hide items under a choice menu
   * @param isShowing
   * @param source
   */
  public void setVisible(boolean isVisible, Object source) {
    if(this.isVisible != isVisible) {
      boolean temp = this.isVisible;
      this.isVisible = isVisible;
      this.notifyObservers(new DataModelChange(source, IS_VISIBLE, temp, this.isVisible));
    }
  }

  public void setValueSelected(boolean shouldSelect, Object source) {
    if(isValueSelected() != shouldSelect) {
      boolean temp = this.isSelected.get(VALUE);
      this.isSelected.put(VALUE, shouldSelect);
      this.notifyObservers(new DataModelChange(source, IS_SELECTED, VALUE, temp, this.isSelected.get(VALUE)));
    }
  }

  public void setUnitSelected(boolean shouldSelect, Object source) {
    if(isUnitSelected() != shouldSelect) {
      boolean temp = this.isSelected.get(UNIT);
      this.isSelected.put(UNIT, shouldSelect);
      this.notifyObservers(new DataModelChange(source, IS_SELECTED, UNIT, temp, this.isSelected.get(UNIT)));
    }
  }

  public void setButtonSelected(boolean shouldSelect, Object source) {
    if(isButtonSelected() != shouldSelect) {
      boolean temp = this.isSelected.get(BUTTON);
      this.isSelected.put(BUTTON, shouldSelect);
      this.notifyObservers(new DataModelChange(source, IS_SELECTED, BUTTON, temp, this.isSelected.get(BUTTON)));
    }
  }

  public void setBackgroundSelected(boolean shouldSelect, Object source) {
    if(isBackgroundSelected() != shouldSelect) {
      boolean temp = this.isSelected.get(BACKGROUND);
      this.isSelected.put(BACKGROUND, shouldSelect);
      this.notifyObservers(new DataModelChange(source, IS_SELECTED, BACKGROUND, temp, this.isSelected.get(BACKGROUND)));
    }
  }
  
  public void setGroupSelected(boolean shouldSelect, Object source) {
    if(isGroupSelected() != shouldSelect) {
      boolean temp = this.isSelected.get(GROUP);
      this.isSelected.put(GROUP, shouldSelect);
      this.notifyObservers(new DataModelChange(source, IS_SELECTED, GROUP, temp, this.isSelected.get(GROUP)));
    }
  }

  public void setValue(String value, Object source, boolean isFile) {
    hijacked.setValue(value, source); // Update the hijacked value
    setIsFile(isFile, source);
  }

  public void setIsFile(boolean isFile, Object source) {
    if(this.isFile != isFile) {
      boolean temp = this.isFile;
      this.isFile = isFile;
      this.notifyObservers(new DataModelChange(source, IS_FILE, temp, this.isFile));
    }  
  }

  public void setCouldBeFile(boolean couldBeFile, Object source) {
    if(this.couldBeFile != couldBeFile) {
      boolean temp = this.couldBeFile;
      this.couldBeFile = couldBeFile;
      this.notifyObservers(new DataModelChange(source, COULD_BE_FILE, temp, this.couldBeFile));
    }
  }
  
  public boolean matchesKey(Key key) {
    if(key == null)
      return true;
    if(getKey().equals(key))
      return true;
    return false;
  }

  public boolean isDefaultValue() {
    return defaultValue != null && StringUtils.equals(getValue(), defaultValue);
  }

  public boolean isDefaultUnit() {
    return defaultUnit != null && StringUtils.equals(getUnit(), defaultUnit);    
  }

  public void setAsDefault(Object source) {    
    hijacked.setValue(defaultValue, source);
    hijacked.setUnit(defaultUnit, source);
  }

  public boolean hasErrors() {
    return !getErrors().isEmpty();
  }

  public String getAppDefault() {
    return appDefaultValue;
  }

  public List<String> getErrors() {
    List<String> errors = validate();
    boolean debug = false;
    if(debug) errors.add("isVisible: " + isVisible + ", isEnabled: " + isEnabled + ", isReadOnly: " + 
        isReadOnly  + ", isSelected: " + isSelected + ", hijacked: " + hijacked.getAllValues() + ", " + parentItems);	  
    if(parentItems.isEmpty() || errors.isEmpty())
      return errors;
    List<String> localErrors = new ArrayList<String>();
    for(String error: errors) {
      String identifier = "[";
      for(int i = 0; i < parentItems.size(); i++) {
        identifier += parentItems.get(i).getSimpleLabel() + (i != parentItems.size()-1 ? "," : "");
      }
      localErrors.add(identifier.trim() + "] " + error);
    }
    return localErrors;
  }

  public List<DataItem> getParentItems() {
    return parentItems;
  }

  protected abstract List<String> validate();

  public int hashCode() {
    return getHijacked().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getHijacked().equals(obj) || super.equals(obj);
  }

  public DataItem getHijacked() {
    return hijacked;
  }

  public void setHijacked(DataItem hijacked) {
    this.hijacked = hijacked;
  }


  @Override
  public boolean shouldObserve(DataModelObservable observable) {
    if(observable instanceof ABCDataItem) return false;
    return parentItems.contains(observable) || observable.equals(hijacked);
   // boolean shouldObserve = (parentItems.contains(observable) && !(observable instanceof ABCDataItem)) ||  (observable.equals(hijacked) && !(observable instanceof ABCDataItem)); // Only observe this
   // return shouldObserve;
  }
  
  public String getUUID() {
    return uuid;
  }

  
  @Override
  public void startObserving(DataModelObservable observable) {  }

  @Override
  public void stopObserving(DataModelObservable observable) {  }

  @Override
  public void update(DataModelObservable observable, DataModelChange dataModelChange) {
    
    if(parentItems.contains(observable)) {
      this.notifyObservers(new DataModelChange(this, PARENT_ITEM, null, null));
      return;
    }
    
    if(!observable.equals(this.hijacked)) {
        System.out.println("We have a problem here, we should only observe hijacked obj"); 
        return;
    }
        
    if(dataModelChange.getChange().equals(KEY)) {
      setKey((Key)dataModelChange.getNewValue(), dataModelChange.getSource());
    } else if(dataModelChange.getChange().equals(VALUE)) {
      setValue((String)dataModelChange.getNewValue(), dataModelChange.getSource());      
    } else if(dataModelChange.getChange().equals(UNIT)) {
      setUnit((String)dataModelChange.getNewValue(), dataModelChange.getSource());      
    } else if(dataModelChange.getChange().equals(LINKED_OBJECT)) {
      setLinkedObject((String)dataModelChange.getNewValue(), dataModelChange.getSource());  
    } else if(dataModelChange.getChange().equals(ALIAS)) {
      setAlias((String)dataModelChange.getNewValue(), dataModelChange.getSource());    
    }    

    if(dataModelChange.getChange().equals(DataModelObservable.OBSERVERS)) {
      if(dataModelChange.getDetail().equals(DataModelObservable.ADDED)) {
        this.addObserver((DataModelObserver)dataModelChange.getNewValue());
      } else if(dataModelChange.getDetail().equals(DataModelObservable.REMOVED)) {
        this.deleteObserver((DataModelObserver)dataModelChange.getOldValue());        
      } else if(dataModelChange.getDetail().equals(DataModelObservable.CLEARED)) {
        this.removeObservers();        
      }
    }
  }

}


