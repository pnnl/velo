package datamodel.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import datamodel.NamedItem;

public class DataItemMapList extends NamedItem implements Iterable<DataItemMap> {

  private static final long serialVersionUID = 1L;

  /**
   * Used for DataModelChanges when a child dataItemMap has been modified
   * @DataModelChange.getChange()
   */
  public static final String LIST_ITEM = "listItem";

  /**
   * Used for DataModelChanges when a child dataItemMap has been added
   * @DataModelChange.getDetail()
   */
  public static final String ADDED = "added";


  /**
   * Used for DataModelChanges when a child dataItemMap has been removed
   * @DataModelChange.getDetail()
   */
  public static final String REMOVED = "removed";

  /**
   * Our children
   */
  List<DataItemMap> list;

  public DataItemMapList() {
    super(UUID.randomUUID().toString()); // For debugging
    list = new ArrayList<DataItemMap>();
  }

  public void add(DataItemMap dataItemMap, Object source) {
    list.add(dataItemMap);
    this.notifyObservers(new DataModelChange(source, LIST_ITEM, ADDED, null, dataItemMap));

    // Move over the observers
    for(DataModelObserver observer: getObservers()) {
      dataItemMap.addObserver(observer);
    }  
  }

  public void remove(DataItemMap dataItemMap, Object source) {

    dataItemMap.removeObservers();
    list.remove(dataItemMap);
    // Send an event
    this.notifyObservers(new DataModelChange(source, LIST_ITEM, REMOVED, dataItemMap, null));
    
  }

  @Override
  public void removeObservers() {
    super.removeObservers();
    for(DataItemMap map: list)
      map.removeObservers();
  }
  
  @Override
  public void addObserver(DataModelObserver o) {
    super.addObserver(o);     
    for(DataItemMap map: list)
      map.addObserver(o);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  /* (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<DataItemMap> iterator() {
    return list.iterator();
  }

  public DataItemMap[] toArray() {
    return list.toArray(new DataItemMap[list.size()]);
  }
  
  public int size() {
    return list.size();
  }
}
