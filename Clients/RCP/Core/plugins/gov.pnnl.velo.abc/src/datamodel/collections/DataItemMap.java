package datamodel.collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import abc.validation.ABCDataItem;
import vabc.ABCConstants;
import vabc.IABCUserObject;
import datamodel.DataItem;
import datamodel.Key;
import datamodel.NamedItem;

/**
 * So I don't have to do new HashMap<.. everywhere
 *  
 * And so I can quit calling utility methods to compare
 * 
 * Iterates over the DataItem children in the map
 * 
 * @author port091
 *
 */
public class DataItemMap extends NamedItem implements Comparable<DataItemMap>, Iterable<DataItem>, IABCUserObject {

  private static final long serialVersionUID = 1L;

  private Map<String, DataItem> map;
  private DataItem identifier; // When this is used in a map

  public DataItemMap() {
    this(null,false,false);
  }

  public DataItemMap(DataItem identifier) {
    this(identifier, false, false);
  }

  public DataItemMap(DataItem identifier, boolean preserveInsertionOrder, boolean sortedMap) {
    if(preserveInsertionOrder) 
      map = new LinkedHashMap<String, DataItem>();
    else if(sortedMap)
      map = new TreeMap<String, DataItem>();
    else
      map = new HashMap<String, DataItem>();
    if(identifier != null)
      this.identifier = identifier;
    else
      this.identifier = new DataItem(new Key(UUID.randomUUID().toString()));
  }

  /**-------------- Modify data */	

  public void setIdentifier(DataItem identifier) {
    this.identifier = identifier;
  }
  
  public void merge(DataItemMap data) {
    for(String key: data.keySet()) {
      put(key, data.get(key));
    }
  }

  public void setAs(DataItemMap collection, Object source) {
    for(String key: collection.keySet()) {
      if(map.containsKey(key))
        map.get(key).setAs(collection.get(key), source);
    }
  }	

  public void add(DataItem value) {
    put(null, value);
  }

  public void addAll(Iterable<DataItem> dataItems) {
    for(DataItem item: dataItems)
      put(null, item);
  }

  public void put(String key, DataItem value) {

    if(value instanceof ABCDataItem) {
      System.out.println("ABCDATAITEM: " + value);
      value = ((ABCDataItem)value).getHijacked();
    } 

    String mapKey = key != null ? key : getKey(value);

    if(map.containsKey(mapKey) && map.get(mapKey).equals(value)) {
      // Duplicate key, not the same data item... TODO: debug why duplicate keys make it into map
      System.out.println("TODO: debug this... adding data item into collection more than once: " + key + ", " + value);
      return; // Do nothing
    }

    if (map.containsKey(mapKey) && !map.get(mapKey).equals(value)) {
      // TODO: We have a problem here
      //	throw new ABCException("Illegal duplicate key: "+ mapKey);
    }

    map.put(getKey(value), value);//mapKey, value);	
    for(DataModelObserver observer: getObservers()) {
      value.addObserver(observer);
    }
  }

  public void setLinkedObject(String linkedObject, Object source) {
    for(DataItem item: this) {
      item.setLinkedObject(linkedObject, source);
    }
  }

  /**-------------- Getters */

  public DataItem get(String key, String objectReference) {
    return map.get(makeKey(key, objectReference));
  }

  public DataItem get(String key) {
		return map.get(key);
  }

  public Map<String, DataItem> getData() {
    return asMap();
  }

  private String getKey(DataItem dataItem) {
    String key = dataItem.getUnlinkedKey();
    if(dataItem.getLinkedObject() != null && !key.equals(dataItem.getLinkedObject())) {
      key = makeKey(key, dataItem.getLinkedObject());
    }
    return key;
  }		
  
  public static String makeKey(String key, String objectIdentifier) {
    return key + ABCConstants.Key.UUID_SEPERATOR + objectIdentifier;
  }

  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  public Set<String> keySet() {
    return map.keySet();
  }

  public java.util.Collection<DataItem> values() {
    return map.values();
  }

  public Map<String, DataItem> asMap() {
    return new HashMap<String, DataItem>(map);
  }

  public DataItem first() {
    return map.values().iterator().next();
  }

  public boolean contains(DataItem trigger) {
    return map.values().contains(trigger);
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }
  /**-------------- @Override */


  @Override
  public String toString() {
    return map.toString();
  }

  @Override
  public Iterator<DataItem> iterator() {
    return map.values().iterator(); // We will lose the keys of the map this way
  }

  @Override
  public boolean equals(Object object) {
    if(object instanceof DataItemMap) 
      return ((DataItemMap)object).compareTo(this) == 0;
    return false; 
  }

  @Override
  public int compareTo(DataItemMap compareTo) {

    Map<String, DataItem> compareToCopy = new HashMap<String, DataItem>(compareTo.map);
    Map<String, DataItem> myCopy = new HashMap<String, DataItem>(map);				

    // My members have more elements		
    for(String key: compareTo.map.keySet()) {
      if(myCopy.containsKey(key) && myCopy.get(key).equals(compareTo.map.get(key))) {
        myCopy.remove(key);
      }
    }
    if(!myCopy.isEmpty())
      return 1;

    // Copy'd members have more elements
    for(String key: map.keySet()) {
      if(compareToCopy.containsKey(key) && compareToCopy.get(key).equals(map.get(key))) {
        compareToCopy.remove(key);
      }
    }
    if(!compareToCopy.isEmpty())
      return -1;

    // Match
    return 0;
  }

  public DataItemMap copy() {
    DataItemMap copyobj = new DataItemMap(getIdentifier().copy());
    for (String key: map.keySet()) {
      copyobj.add(map.get(key).copy());
    }
    return copyobj;
  }
  
  public DataItemMap copy(String newUUID) {
    DataItem identifierCopy = identifier.copy(newUUID);
    String currentUUID = identifier.getUnlinkedKey();
    identifierCopy.setKey(new Key(newUUID), DataItemMap.this);
    DataItemMap copyobj = new DataItemMap(identifierCopy);
    for (DataItem item: map.values()) {
      DataItem dataItemCopy = item.copy();
      if(dataItemCopy.getLinkedObject() != null && dataItemCopy.getLinkedObject().equals(currentUUID))
        dataItemCopy.setLinkedObject(newUUID, DataItemMap.this);      
      copyobj.add(dataItemCopy);
    }
    return copyobj;
  }
  

  @Override
  public void addObserver(DataModelObserver o) {
    super.addObserver(o);
    for(DataItem item: values()) {
      item.addObserver(o);
    }
  }

  public void print(String tabs) {
    System.out.println(tabs + "Collection: observers: " + countObservers());
    for(String key: map.keySet()) {
      System.out.println(tabs + "\t" + key + ", " + map.get(key).getValue() + " " +map.get(key).getClass().toString());
      // map.get(key).print(tabs + "\t\t");
    }
  }


  @Override
  public DataItem getItem(String key) {
    return map.get(key);
  }

  @Override
  public void initializeItem(String key, DataItem item) {
    map.put(key, item);
  }

  @Override
  public DataItem getIdentifier() {
    return identifier;
  }

  public void removeObservers() {
    identifier.removeObservers();
    for(DataItem item: this) {
      item.removeObservers();
    }
  }

}
