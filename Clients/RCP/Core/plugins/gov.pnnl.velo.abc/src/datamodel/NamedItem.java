package datamodel;

import java.io.Serializable;

import vabc.StringUtils;

public class NamedItem extends DataModelObservable implements Serializable {

	private static final long serialVersionUID = 1L;
  
  
  /**
   * Used for DataModelChanges when alias has been modified
   * @DataModelChange.getChange()
   */
	public final static String ALIAS = "alias";
	
	/**
	 * Name of the named item
	 */
	private String alias;	
	
	//---------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------
	
	public NamedItem() {	}
	
	public NamedItem(String alias) {
		this.alias = alias;
	}
	
	//---------------------------------------------------------------------------
  // Getters/Setters
  //---------------------------------------------------------------------------

	public String getAlias() {
    return alias == null ? "" : alias;
  }
  
	/**
	 * Given a new alias, alias is set and observers are notified.
	 * @param alias
	 * @param source
	 */
  public void setAlias(String alias, Object source) {
    if(!StringUtils.equals(this.alias, alias)) {
      String temp = this.alias;
      this.alias = alias;
      this.notifyObservers(new DataModelChange(source, ALIAS, temp, this.alias));
    }
  }
			
  //---------------------------------------------------------------------------
  // Overrides
  //---------------------------------------------------------------------------
  
	@Override 
	public String toString() {
		return alias != null ? alias : super.toString();
	}
	
}
