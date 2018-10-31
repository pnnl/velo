package datamodel.collections;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import vabc.IABCUserObject;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.Key;
import datamodel.NamedItem;

public class RecursiveDataItemMap extends NamedItem {

  private static final long serialVersionUID = 1L;

  /**
   * Used for DataModelChanges when a child collection has been modified
   * @DataModelChange.getChange()
   */
  public static final String CHILD_COLLECTIONS = "childCollections";

  /**
   * Used for DataModelChanges when a child collection has been added
   * @DataModelChange.getDetail()
   */
  public static final String ADDED = "added";


  /**
   * Used for DataModelChanges when a child collection has been removed
   * @DataModelChange.getDetail()
   */
  public static final String REMOVED = "removed";

  /**
   * Our children
   */
  Map<DataItem, RecursiveDataItemMap> childCollections;

  /**
   * Our data
   */
  DataItemMap collection;

  public RecursiveDataItemMap() {
    super(UUID.randomUUID().toString()); // For debugging
    childCollections = new LinkedHashMap<DataItem, RecursiveDataItemMap>();
    collection = new DataItemMap(null, true, false);
  }


  /**-------------- Exposed methods from the map */	

  public RecursiveDataItemMap get(DataItem key) {
    return childCollections.get(key);
  }

  public Set<DataItem> keySet() {
    return childCollections.keySet();
  }

  /**-------------- Exposed methods from the collection */	

  public java.util.Collection<DataItem> values() {
    return collection.values();
  }

  /**-------------- Modify data */	

  public void put(DataItem dataItem, DataItemMap collection, Object source) {
    if(dataItem instanceof ABCDataItem) {
      System.err.println("ABCDATAITEM snuck in there: " + dataItem);
      dataItem = ((ABCDataItem)dataItem).getHijacked();
    }
    if(dataItem == null) {
      this.collection.merge(collection); // Merge at this level
    } else {
      if(!childCollections.containsKey(dataItem)) {// Create a child and merge there
        put(dataItem, new RecursiveDataItemMap(), source);
      }
      childCollections.get(dataItem).collection.merge(collection);
    }
  }	

  public void merge(DataItemMap data, Object source) {
    collection.merge(data);
  }

  private void merge(RecursiveDataItemMap rCollection, Object source) {
    for(String theirKey: rCollection.collection.keySet()) {
      boolean foundMatch = false;
      for(String ourKey: collection.keySet()) {
        if(theirKey.matches(ourKey)) {
          foundMatch = true;
          collection.get(ourKey).setAs(rCollection.collection.get(theirKey), source);
        }
      }
      if(!foundMatch) {
        // Do nothing?
      }
    }
    for(DataItem theirKey: rCollection.childCollections.keySet()) {
      boolean foundMatch = false;
      for(DataItem ourKey: childCollections.keySet()) {
        if(theirKey.matches(ourKey)) {
          System.out.println("Found a match...");
          foundMatch = true;
          childCollections.get(ourKey).merge(rCollection.childCollections.get(theirKey), source);
        }
      }
      if(!foundMatch) {
        // Do nothing?
      }
    }
  }

  public void put(DataItem dataItem, RecursiveDataItemMap rCollection, Object source) {
    if(childCollections.containsKey(dataItem)) {
      rCollection.merge(childCollections.get(dataItem), source); // Get and merge the current?
      childCollections.put(dataItem, rCollection); // Replace it			
      } else {
      childCollections.put(dataItem, rCollection != null ? rCollection : new RecursiveDataItemMap());
      this.notifyObservers(new DataModelChange(source, CHILD_COLLECTIONS, ADDED, null, dataItem));
    }
    // Move over the observers
    for(DataModelObserver observer: getObservers()) {
      dataItem.addObserver(observer);
      rCollection.addObserver(observer);
    }
  }

  /**
   * Requires the UI component to catch it
   * @param userObject
   * @param source
   */
  public void put(IABCUserObject userObject, Object source) {
    
    this.notifyObservers(new DataModelChange(source, CHILD_COLLECTIONS, ADDED, null, userObject));
    
  }

  public void remove(DataItem dataItem, Object source) {
    // Remove any observers
    if(dataItem == null)
      return;
    
    dataItem.removeObservers();
    childCollections.get(dataItem).removeObservers();
    // Remove the item
    childCollections.remove(dataItem);
    // Send an event
    this.notifyObservers(new DataModelChange(source, CHILD_COLLECTIONS, REMOVED, dataItem, null));
  }

  @Override
  public void removeObservers() {
    super.removeObservers();
    collection.removeObservers();
    for(DataItem item: childCollections.keySet()) {
      item.removeObservers();
      this.get(item).removeObservers();    // Remove observers
    }
  }

  /**-------------- Getters */	

  /**
   * Get an item with the specified key.
   * Note that this method WILL match on the alias. 
   * @param key
   * @return
   */
  public DataItem getItem(String key) {
    if(collection.containsKey(key)) {
      return collection.get(key);
    }
    for(DataItem childIdentifier: childCollections.keySet()) {
      if(childIdentifier.getKey().equals(key)) {
        return childIdentifier;
      }
      DataItem item = childCollections.get(childIdentifier).getItem(key);
      if(item != null) {
        return item;
      }
    }
    return null;
  }

  /**
   * Get an item with the specified key.
   * Note that this method NOT match on the key alias. 
   * @param key
   * @return
   */
  public DataItem getItem(Key key) {
    if(collection.containsKey(key.getKey())) {
      return collection.get(key.getKey());
    }
    for(DataItem childIdentifier: childCollections.keySet()) {
      if(childIdentifier.getKey().equals(key)) {
        return childIdentifier;
      }
      DataItem item = childCollections.get(childIdentifier).getItem(key);
      if(item != null) {
        return item;
      }
    }
    return null;
  }

  public DataItem getMapKeyOf(DataItem trigger) {
    for(DataItem key: childCollections.keySet()) {
      if(childCollections.get(key).getCollection().contains(trigger)) {
        return key;
      }
      if(childCollections.get(key).keySet().contains(trigger)) {
        return key;
      }
      DataItem child = childCollections.get(key).getMapKeyOf(trigger); // Recurse
      if(child != null)
        return child;
    }
    return null;		
  } 

  public RecursiveDataItemMap rCollectionOf(DataItem trigger) {
    if(collection.contains(trigger))
      return this;
    for(RecursiveDataItemMap child: childCollections.values()) {
      RecursiveDataItemMap atChild = child.rCollectionOf(trigger);
      if(atChild != null)
        return atChild;
    }
    return null;
  }

  public DataItemMap getCollection() {
    return collection;
  }

  public Map<DataItem, RecursiveDataItemMap> getSubCollection() {
    return childCollections;
  }

  public boolean contains(DataItemMap data) {
    if(collection.equals(data))
      return true;
    for(RecursiveDataItemMap child: childCollections.values()) {
      if(child.contains(data))
        return true;
    }
    return false;
  }	

  public boolean contains(DataItem item) {
    if(collection.contains(item))
      return true;
    if(childCollections.keySet().contains(item))
      return true;
    for(RecursiveDataItemMap child: childCollections.values()) {
      if(child.contains(item))
        return true;
    }
    return false;
  }

  public void print(String tabs) {
    System.out.println(tabs + "id: " + getAlias());
    tabs += "\t";
    System.out.println(tabs + "Recursive collection data: ");
    if(!collection.asMap().isEmpty())
      collection.print(tabs);
    System.out.println(tabs + "Recursive collection children: ");
    for(DataItem dataItem: childCollections.keySet()) {
      System.out.println(tabs + "[" + dataItem.getKey().getAlias() + ", " + dataItem.getKey().getKey() + "]:" + dataItem.getValue() + ", observers: " + collection.countObservers());
      childCollections.get(dataItem).print(tabs + "\t");
    }
  }

  public RecursiveDataItemMap copy(Object source) {
    RecursiveDataItemMap copyobj = new RecursiveDataItemMap();
    copyobj.getCollection().addAll(this.getCollection().copy().values());
    for (DataItem di: childCollections.keySet()) {
      RecursiveDataItemMap child = childCollections.get(di);
      copyobj.put(di, child.copy(source), source);
    }
    return copyobj;
  }

  public void copyObserversFrom(RecursiveDataItemMap rCollection) {
    for(DataModelObserver observer: rCollection.getObservers())
      addObserver(observer);
  }

  @Override
  public void addObserver(DataModelObserver o) {
    super.addObserver(o);       
    collection.addObserver(o);
    for(DataItem item: childCollections.keySet()) {
      item.addObserver(o);
      childCollections.get(item).addObserver(o);
    }
  }

  public boolean isEmpty() {
    return collection.isEmpty() && childCollections.isEmpty();
  }


}
