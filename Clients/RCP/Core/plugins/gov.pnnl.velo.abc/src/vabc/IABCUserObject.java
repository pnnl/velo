package vabc;

import datamodel.DataItem;

public interface IABCUserObject {
  
  /**
   * A unique identifier that can be used to identify 
   * this particular instance of the object.
   * @return
   */
  public DataItem getIdentifier();

  /**
   * Will be called of the user object does not contain a key
   * that abc is looking for
   * 
   * @param key
   * @param item
   * @return
   */
  public void initializeItem(String key, DataItem item);
  
  /**
   * Will be an item in the collections collection
   * @param key
   * @return
   */
  public DataItem getItem(String key);
  
  /**
   * Needed to be able to copy items
   * @return
   */
  public IABCUserObject copy();
  
  
}
