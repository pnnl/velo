package datamodel;

import vabc.ABCConstants;
import vabc.StringUtils;
import abc.units.ABCUnitFactory;

public class DataItem extends NamedItem implements Comparable<Object> {

  private static final long serialVersionUID = 1L;

  /**
   * Used for DataModelChanges when key has been modified
   * @DataModelChange.getChange()
   */
  public final static String KEY = Key.KEY;
  
  
  /**
   * Used for DataModelChanges when value has been modified
   * @DataModelChange.getChange()
   */
  public final static String VALUE = "value";
  
  
  /**
   * Used for DataModelChanges when unit has been modified
   * @DataModelChange.getChange()
   */
  public final static String UNIT = "unit";
  
  
  /**
   * Used for DataModelChanges when linkedObject has been modified
   * @DataModelChange.getChange()
   */
  public final static String LINKED_OBJECT = "linkedObject";
  
  /**
   * Key
   */
  private Key key;

  /**
   * Value, can be null
   */
  private String value;
  
  /**
   * Unit, can be null
   */
  private String unit;

  /**
   * Identifier of an object associated with this item
   * Usually UUID, can be null
   */
  private String linkedObject;

  //---------------------------------------------------------------------------
  // Constructors
  //---------------------------------------------------------------------------
  
  public DataItem(Key key) {
    this(key, null);
  }

  public DataItem(String key, String value) {
    this(key, value, null);
  }

  public DataItem(String key, String value, String unit) {
    this(key, value, unit, null);
  }

  public DataItem(String key, String value, String unit, String linkedObject) {
    this(new Key(key, key==null?key:key.replaceAll("_", " ")), value, unit, linkedObject);
  }

  public DataItem(Key key, String value) {
    this(key, value, null);
  }

  public DataItem(Key key, String value, String unit) {
    this(key, value, unit, null);
  }

  public DataItem(Key key, String value, String unit, String linkedObject) {
    this(key.getAlias(), key, value, unit, linkedObject);
  }

  public DataItem(String alias, Key key, String value, String unit, String linkedObject) {
    super(alias);
    this.key = key;
    this.value = value;
    this.unit = unit;
    this.linkedObject = linkedObject;
  }

  public DataItem(DataItem parameter) {
    this(parameter.getAlias(), parameter.key, parameter.value, parameter.unit, parameter.linkedObject);
  }

  //---------------------------------------------------------------------------
  // Getters/Setters
  //---------------------------------------------------------------------------
  
  public void setAs(DataItem dataItem, Object source) {		
    setKey(dataItem.getKey(), source);
    setUnit(dataItem.getUnit(), source);
    setValue(dataItem.getValue(), source);
    setAlias(dataItem.getAlias(), source);
    setLinkedObject(dataItem.getLinkedObject(), source);
  }	

  public DataItem copy() {
    return new DataItem(getAlias(), key, value, unit, linkedObject);
  }

  public DataItem copy(String linkedObject) {
    return new DataItem(getAlias(), key, value, unit, this.linkedObject != null ? linkedObject : null);
  }

  public Key getKey() {
    return key;
  }
  
  public String getUnlinkedKey() {
    if(linkedObject != null && key.getKey().endsWith(linkedObject)) {
      return key.getKey().split(ABCConstants.Key.UUID_SEPERATOR)[0];
    }
    return key.getKey();    
  }

  public void setKey(Key key, Object source) {
    // If the final key's don't match, update the key
    if(!this.key.getKey().equals(key.getKey())) {
      Key temp = this.key;
      this.key = key;
      this.notifyObservers(new DataModelChange(source, KEY, temp, this.key));     
      // If the alias also changed, we need to update it as well
      setAlias(key.getAlias(), source);            
    } 
  }

  public String getValue() {
    return value;
  }
  
  public void setValue(String value) {
    setValue(value, DataItem.this);
  }
  
  public void setUnit(String unit) {
    setUnit(unit, DataItem.this);
  }
  
  public void setLinkedObject(String linkedObject) {
    setLinkedObject(linkedObject, DataItem.this);
  }
  
  public void setValue(String value, Object source) {
    if(!StringUtils.equals(this.value, value)) {
      String temp = this.value;
      this.value = value;
      this.notifyObservers(new DataModelChange(source, VALUE, temp, this.value));     
    }		
  }
  
  public void setValue(String value, String detail, Object source) {
    if(!StringUtils.equals(this.value, value)) {
      String temp = this.value;
      this.value = value;
      this.notifyObservers(new DataModelChange(source, detail, VALUE, temp, this.value));     
    }   
  }

  public String getUnit() {
    return unit;
  }
  
  public void setUnit(String unit, Object source) {
    if(!StringUtils.equals(this.unit, unit)) {
      String temp = this.unit;
      this.unit = unit;
      this.notifyObservers(new DataModelChange(source, UNIT, temp, this.unit));     
    }
  }

  public String getLinkedObject() {
    return linkedObject;
  }

  public void setLinkedObject(String linkedObject, Object source) {
    if(!StringUtils.equals(this.linkedObject, linkedObject)) {
      String temp = this.linkedObject;
      this.linkedObject = linkedObject;
      this.notifyObservers(new DataModelChange(source, LINKED_OBJECT, temp, this.linkedObject));     
    }
  }	
  
  @Override
  public void setAlias(String alias, Object source) {
    // Set the alias in the key as well
    this.key.setAlias(alias, source);
    super.setAlias(alias, source);
  }


  //---------------------------------------------------------------------------
  // Label providers, TODO: Clean this up?
  //---------------------------------------------------------------------------
  
  
  /**
   * Returns the value and units (if they are not null)
   * @return
   */
  public String getSimpleLabel() {
    String valueS = value != null && !value.isEmpty() ? value : " ";
    String unitS = unit != null ? unit : "";
    String valueAndUnits = valueS + " " + unitS;
    if(valueAndUnits.trim().isEmpty() && !getAlias().equals(key.key))
      return getAlias();
    return valueAndUnits;
  }
  
  /**
   * Returnsalias (linkedObject?): <b>value unit</b> <br>
   * @return
   */
  public String getHTMLView(String linkedObjectAlias) {
    String value = this.value;
    if(value != null && linkedObjectAlias != null && value.contains(linkedObject))
      value = value.replaceAll(linkedObject, "").trim();
    if(value != null && value.matches("[a-zA-Z\\_ ]*"))
      value = new Key(value, null).getAlias();
    return "<b></b>" +  getAlias() + 
            (linkedObjectAlias != null ? " (" + linkedObjectAlias + ")" : "") +": <b>" + 
            (value != null ? value + (unit != null ? " " + unit : "") : "") + "</b><br>";    
  }
  
  public String getAllValues() {
    // Build a not so UI friendly string
    String toString = "[";
    if(key != null && !key.toString().isEmpty())
      toString += ("key: " + key + ", ");

    if(value != null && !value.isEmpty())
      toString += ("value: " + value + ", ");

    if(unit != null && !unit.isEmpty())
      toString += ("units: " + unit+ ", ");
    if(linkedObject != null)
      toString += ("linkedObject: " + linkedObject);

    if(toString.endsWith(", "))
      toString = toString.substring(0, toString.length()-2);

    toString += "]";

    return toString;
  }
    
  @Override
  public String toString() {

    // Return a ui friendly string if we have one
    if(getAlias() != null)
      return getAlias();

    // Build a not so UI friendly string
    return getAllValues();
  }

  public boolean matches(Object obj) {

    if(obj instanceof Key) {
      //			System.out.println("**** matching key "+((Key)obj).key+"=="+key.key);
      // Just check the key part, ignore the alias, or check the linked object			
      return key.key.equals(((Key)obj).key) || obj.equals(linkedObject); // Check the uuid
    }

    if(!(obj instanceof DataItem))
      return false;

    DataItem toCompare = (DataItem)obj;

    if(key != null && toCompare.getKey() != null) {
      if(key.compareTo(toCompare.getKey()) != 0)
        return false;
    } else if(key != null || toCompare.getKey() != null) {
      return false;
    }

    if(value != null && toCompare.getValue() != null) {
      if(value.compareTo(toCompare.getValue()) != 0)
        return false;
    } else if(value != null || toCompare.getValue() != null) {
      return false;
    }

    if(unit != null && toCompare.getUnit() != null) {
      if(unit.compareTo(toCompare.getUnit()) != 0)
        return false;
    } else if(unit != null || toCompare.getUnit() != null) {
      return false;
    }

    if(linkedObject != null && toCompare.getLinkedObject() != null) {
      if(linkedObject.compareTo(toCompare.getLinkedObject()) != 0)
        return false;
    } else if(linkedObject != null || toCompare.getLinkedObject() != null) {
      return false;
    }

    return true;
  }
  public void print(String tabs) {
    System.out.println(tabs + toString() + ", observers: " + countObservers());
    for(DataModelObserver observer: getObservers()) {
      System.out.println(tabs + "\t" + observer);
    }
  }

  @Override
  public int compareTo(Object object) {
    // Compare by alias first
    if(object instanceof DataItem) {
      DataItem otherItem = (DataItem)object;
      // Numerical values vs. String values
      
      // If the value has no numbers, don't try parsing it, just do a string compare
      if(unit == null && (value != null && (value.isEmpty() || !value.matches(".*[\\d]+.*")))) {
        String a = value == null || value.toString().isEmpty() ? "z" : value; // z so elements go to bottom
        String b = otherItem.getValue() == null || otherItem.getValue().isEmpty() ? "z" : otherItem.getValue();
        return a.compareTo(b);
      }
      
      try {
        Double a = Double.MAX_VALUE;
        Double b = Double.MAX_VALUE; // Some large number, so empty goes to bottom

        if(value != null)
          a = Double.parseDouble(value);
        if(otherItem.getValue() != null)
          b = Double.parseDouble(otherItem.getValue());

        // Check for units
        if(unit != null && otherItem.getUnit() != null && !unit.equals(otherItem.getUnit())) {
          b = ABCUnitFactory.getABCUnits().convertValue(b.toString(), otherItem.getUnit() , unit).asDouble();
          return a.compareTo(b); // Compare a to b now that they are in the same units
        }
        return a.compareTo(b); // Units not available, comparing the values directly

      } catch (NumberFormatException e) {
        // One of or both of the two values are not numerical, performing a string comparison
        String a = value == null || value.toString().isEmpty() ? "z" : value; // z so elements go to bottom
        String b = otherItem.getValue() == null || otherItem.getValue().isEmpty() ? "z" : otherItem.getValue();
        return a.compareTo(b);
      }
    }
    return -1;
  }

}
