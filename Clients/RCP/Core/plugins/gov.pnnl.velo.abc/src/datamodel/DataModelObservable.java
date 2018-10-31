package datamodel;

import java.util.Vector;

public class DataModelObservable {

  /**
   * Used for DataModelChanges when observers has been added/removed
   * @DataModelChange.getChange()
   */
  public static final String OBSERVERS = "observers";

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
   * Used for DataModelChanges when a child collection has been cleared
   * @DataModelChange.getDetail()
   */
  public static final String CLEARED = "cleared";

  transient private Vector<DataModelObserver> observers;

  public DataModelObservable() { }

  public synchronized Vector<DataModelObserver> getObservers() {
    if(observers == null) {
      observers = new Vector<DataModelObserver>() ;
    }
    return observers;
  }

  public synchronized void addObserver(DataModelObserver o) {
    if (o == null)
      throw new NullPointerException();

    if (!getObservers().contains(o)) {
      // Check if this is a data item, if so,
      // check if we should be observing this item
      if(this instanceof DataItem && !(o.shouldObserve(this)))
          return;
      notifyObservers(new DataModelChange(this, OBSERVERS, ADDED, null, o));
      getObservers().addElement(o); // Go ahead and add it
      o.startObserving(this); // And let the observer know
    }
  }

  public synchronized void deleteObserver(DataModelObserver o) {
    // Let the other observers know a change is coming
    notifyObservers(new DataModelChange(this, OBSERVERS, REMOVED, o, null));
    getObservers().removeElement(o);
    o.stopObserving(this);      
  }


  public synchronized void removeObservers() {
    // Let the other observers know a change is coming
    Object[] arrLocal;
    synchronized (this) {
      arrLocal = getObservers().toArray();
    }
    for (int i = arrLocal.length-1; i>=0; i--) {
      DataModelObserver o = ((DataModelObserver)arrLocal[i]);
      o.update(this, new DataModelChange(this, OBSERVERS, CLEARED, o, null));
      o.stopObserving(this);
    }
    observers.clear(); 
  }

  public void notifyObservers(DataModelChange change) {
    Object[] arrLocal;
    synchronized (this) {
      arrLocal = getObservers().toArray();
    }
    for (int i = arrLocal.length-1; i>=0; i--)
      ((DataModelObserver)arrLocal[i]).update(this, change);
  }

  public synchronized int countObservers() {
    return getObservers().size();
  }

  public interface DataModelObserver {    

    /**
     * Called before an observer is added to a new observable
     * @param observable
     * @return
     */
    public boolean shouldObserve(DataModelObservable observable);

    /**
     * Called when an observer is added to a new observable
     * @param o
     */
    public void startObserving(DataModelObservable observable);

    /**
     * Called when an object an observer is observing is deleted
     * @param o
     */
    public void stopObserving(DataModelObservable observable);

    /**
     * Called when an object an observer is observing has changed
     * @param o
     * @param arg
     */
    public void update(DataModelObservable observable, DataModelChange dataModelChange);


  }

  public class DataModelChange {

    private final Object source;
    private final String change;
    private final String detail;
    private final Object oldValue;
    private final Object newValue;

    public DataModelChange(Object source, String change, Object oldValue, Object newValue) {
      this(source, change, null, oldValue, newValue);
    }

    public DataModelChange(Object source, String change, String detail, Object oldValue, Object newValue) {
      this.source = source; this.change = change; this.detail = detail; this.oldValue = oldValue; this.newValue = newValue;
    }

    public Object getSource() {  return source;  }

    public String getChange() {  return change;  }

    public String getDetail() {  return detail;  }  

    public Object getOldValue() {  return oldValue;  }  

    public Object getNewValue() {  return newValue;  }  

    /**
     * TODO: How should we print the source?
     * DataModelChange [Source class, change, detail] [oldvalue, newValue]
     */
    @Override
    public String toString() {
      return this.getClass().toString() + " [" + source.getClass().toString() + ", " + change 
          + (detail != null ? ", " + detail : "") + "] [" + oldValue + ", " + newValue + "]";
    }
  }

}
